"""
Tests for the "brain" of the assistant: intent parsing (nlu.py) and action
dispatch (actions.py). These run with no microphone, no speaker, and no
real network calls (network-dependent actions are mocked), so they can run
anywhere, including CI.

Run with:  python -m pytest tests/ -v
"""

import os
import sys
import tempfile
from pathlib import Path
from unittest.mock import patch

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from voice_assistant.nlu import parse_intent
from voice_assistant.actions import ActionDispatcher
from voice_assistant.custom_commands import CustomCommandStore
from voice_assistant.reminders import duration_to_seconds, ReminderError


# ---------------------------------------------------------------------------
# NLU: intent + slot extraction
# ---------------------------------------------------------------------------
@pytest.mark.parametrize(
    "text,expected_intent",
    [
        ("hello", "greeting"),
        ("hi there", "greeting"),
        ("what time is it", "get_time"),
        ("tell me the time", "get_time"),
        ("what's the date today", "get_date"),
        ("what day is it", "get_date"),
        ("search for the best pizza place", "web_search"),
        ("google python tutorials", "web_search"),
        ("weather in Kigali", "get_weather"),
        ("what's the weather like in Paris", "get_weather"),
        ("remind me to stretch in 10 minutes", "set_reminder"),
        ("who is the president of France", "answer_question"),
        ("quit", "exit"),
        ("goodbye", "exit"),
    ],
)
def test_intent_classification(text, expected_intent):
    intent = parse_intent(text)
    assert intent.name == expected_intent, f"'{text}' -> {intent.name}, expected {expected_intent}"


def test_web_search_slot_extraction():
    intent = parse_intent("search for the best pizza place near me")
    assert intent.name == "web_search"
    assert intent.slots["query"] == "the best pizza place near me"


def test_weather_slot_extraction():
    intent = parse_intent("what's the weather like in New York")
    assert intent.name == "get_weather"
    assert intent.slots["city"].strip().lower() == "new york"


def test_reminder_slot_extraction():
    intent = parse_intent("remind me to call mom in 15 minutes")
    assert intent.name == "set_reminder"
    assert intent.slots["task"] == "call mom"
    assert intent.slots["amount"] == "15"
    assert "minute" in intent.slots["unit"]


def test_email_slot_extraction():
    intent = parse_intent(
        "send an email to jane@example.com about the meeting saying let's reschedule"
    )
    assert intent.name == "send_email"
    assert intent.slots["to"] == "jane@example.com"
    assert intent.slots["subject"] == "the meeting"
    assert intent.slots["body"] == "let's reschedule"


def test_free_form_phrasing_falls_back_correctly():
    # Not an exact pattern match, but should still resolve via bag-of-words
    # fallback scoring rather than plain keyword matching.
    intent = parse_intent("could you tell me what day it is today")
    assert intent.name == "get_date"


def test_gibberish_is_unknown():
    intent = parse_intent("asdkjfh qwoeiru zzxcv")
    assert intent.name == "unknown"


def test_empty_string_is_unknown():
    intent = parse_intent("")
    assert intent.name == "unknown"


# ---------------------------------------------------------------------------
# Reminders: duration parsing
# ---------------------------------------------------------------------------
def test_duration_to_seconds_minutes():
    assert duration_to_seconds("10", "minutes") == 600


def test_duration_to_seconds_hours():
    assert duration_to_seconds("2", "hours") == 7200


def test_duration_to_seconds_rejects_bad_unit():
    with pytest.raises(ReminderError):
        duration_to_seconds("5", "fortnights")


def test_duration_to_seconds_rejects_non_positive():
    with pytest.raises(ReminderError):
        duration_to_seconds("0", "minutes")


# ---------------------------------------------------------------------------
# Custom commands
# ---------------------------------------------------------------------------
def test_custom_command_add_and_match():
    with tempfile.TemporaryDirectory() as tmp:
        path = Path(tmp) / "custom_commands.json"
        store = CustomCommandStore(path=path)
        store.add_command("play music", "Starting your playlist now.")

        assert store.match("hey can you play music please") == "Starting your playlist now."
        assert store.match("unrelated phrase") is None

        # Persisted to disk and reloadable
        reloaded = CustomCommandStore(path=path)
        assert reloaded.match("play music") == "Starting your playlist now."


# ---------------------------------------------------------------------------
# Action dispatch (text-only, no hardware; network calls mocked)
# ---------------------------------------------------------------------------
def _make_dispatcher(tmp_path):
    spoken = []
    store = CustomCommandStore(path=tmp_path / "custom_commands.json")
    return (
        ActionDispatcher(
            speak=spoken.append,
            listen=lambda prompt="": None,
            weather_api_key="dummy-key",
            command_store=store,
        ),
        spoken,
    )


def test_greeting_action(tmp_path):
    dispatcher, spoken = _make_dispatcher(tmp_path)
    intent = parse_intent("hello")
    response, should_exit = dispatcher.handle(intent, "hello")
    assert "Hello" in response
    assert should_exit is False


def test_time_and_date_actions(tmp_path):
    dispatcher, _ = _make_dispatcher(tmp_path)
    response, _ = dispatcher.handle(parse_intent("what time is it"), "what time is it")
    assert "currently" in response
    response, _ = dispatcher.handle(parse_intent("what's the date"), "what's the date")
    assert "Today is" in response


def test_exit_action(tmp_path):
    dispatcher, _ = _make_dispatcher(tmp_path)
    response, should_exit = dispatcher.handle(parse_intent("quit"), "quit")
    assert should_exit is True
    assert "Goodbye" in response


def test_web_search_opens_browser(tmp_path):
    dispatcher, _ = _make_dispatcher(tmp_path)
    with patch("voice_assistant.actions.webbrowser.open") as mock_open:
        response, _ = dispatcher.handle(
            parse_intent("search for python tutorials"), "search for python tutorials"
        )
    mock_open.assert_called_once()
    assert "python tutorials" in mock_open.call_args[0][0].replace("+", " ")
    assert "python tutorials" in response


def test_weather_action_success(tmp_path):
    dispatcher, _ = _make_dispatcher(tmp_path)
    with patch("voice_assistant.actions.weather.get_weather", return_value="Sunny and 25C in Kigali."):
        response, _ = dispatcher.handle(
            parse_intent("weather in Kigali"), "weather in Kigali"
        )
    assert response == "Sunny and 25C in Kigali."


def test_weather_action_missing_key_message(tmp_path):
    dispatcher, _ = _make_dispatcher(tmp_path)
    from voice_assistant import weather as weather_module

    with patch(
        "voice_assistant.actions.weather.get_weather",
        side_effect=weather_module.WeatherError("no key configured"),
    ):
        response, _ = dispatcher.handle(parse_intent("weather in Kigali"), "weather in Kigali")
    assert "no key configured" in response


def test_send_email_action_success(tmp_path):
    dispatcher, _ = _make_dispatcher(tmp_path)
    with patch("voice_assistant.actions.email_sender.send_email") as mock_send:
        response, _ = dispatcher.handle(
            parse_intent("send an email to jane@example.com about hi saying how are you"),
            "send an email to jane@example.com about hi saying how are you",
        )
    mock_send.assert_called_once()
    assert "jane@example.com" in response


def test_reminder_action_schedules(tmp_path):
    dispatcher, spoken = _make_dispatcher(tmp_path)
    response, _ = dispatcher.handle(
        parse_intent("remind me to stretch in 1 seconds"), "remind me to stretch in 1 seconds"
    )
    assert "stretch" in response
    assert dispatcher.reminder_manager.pending_count() == 1
    dispatcher.reminder_manager.cancel_all()


def test_answer_question_uses_local_kb(tmp_path):
    dispatcher, _ = _make_dispatcher(tmp_path)
    response, _ = dispatcher.handle(
        parse_intent("what is your name"), "what is your name"
    )
    assert "voice assistant" in response.lower()


def test_answer_question_network_error_message(tmp_path):
    dispatcher, _ = _make_dispatcher(tmp_path)
    from voice_assistant import knowledge as knowledge_module

    with patch(
        "voice_assistant.actions.knowledge.answer_question",
        side_effect=knowledge_module.KnowledgeError("couldn't find anything"),
    ):
        response, _ = dispatcher.handle(
            parse_intent("who is Ada Lovelace"), "who is Ada Lovelace"
        )
    assert "couldn't find anything" in response


def test_custom_command_takes_precedence(tmp_path):
    dispatcher, _ = _make_dispatcher(tmp_path)
    dispatcher.command_store.add_command("play music", "Playing your favorites.")
    response, _ = dispatcher.handle(parse_intent("play music"), "play music")
    assert response == "Playing your favorites."


def test_unknown_intent_asks_to_repeat(tmp_path):
    dispatcher, _ = _make_dispatcher(tmp_path)
    response, should_exit = dispatcher.handle(parse_intent("asdkjfh qwoeiru"), "asdkjfh qwoeiru")
    assert "didn't catch" in response.lower() or "didn't understand" in response.lower()
    assert should_exit is False
