"""
actions.py
==========
Turns a parsed `Intent` (see nlu.py) into a concrete action and a spoken
response string. This is the "brain" of the assistant — deliberately kept
separate from speech I/O so it can be unit-tested with plain text in/out,
no microphone or speaker required.

`ActionDispatcher.handle()` is the single entry point used by main.py. It
returns a tuple (response_text, should_exit) so the main loop knows both
what to say and whether the conversation should end.
"""

from __future__ import annotations

import webbrowser
from dataclasses import dataclass, field
from datetime import datetime
from typing import Callable, Optional, Tuple

from . import email_sender, knowledge, weather
from .custom_commands import CustomCommandStore
from .nlu import Intent
from .reminders import ReminderManager, duration_to_seconds, ReminderError


@dataclass
class ActionDispatcher:
    speak: Callable[[str], None]
    listen: Callable[[str], Optional[str]]
    weather_api_key: Optional[str] = None
    command_store: CustomCommandStore = field(default_factory=CustomCommandStore)
    reminder_manager: ReminderManager = field(init=False)

    def __post_init__(self):
        self.reminder_manager = ReminderManager(on_alert=self._on_reminder_fired)

    # ------------------------------------------------------------------
    def handle(self, intent: Intent, raw_text: str) -> Tuple[str, bool]:
        """Dispatch on intent.name and return (response_text, should_exit).

        User-defined custom commands are checked first: they're an explicit
        opt-in from the user, so a phrase they configured on purpose should
        win even if it happens to also resemble a built-in intent (e.g. a
        custom command starting with "what is my wifi password").
        """
        custom_response = self.command_store.match(raw_text)
        if custom_response is not None:
            return custom_response, False

        handler = getattr(self, f"_handle_{intent.name}", None)
        if handler is None:
            return self._handle_unknown(intent)

        return handler(intent)

    # ------------------------------------------------------------------
    # Individual intent handlers
    # ------------------------------------------------------------------
    def _handle_greeting(self, intent: Intent) -> Tuple[str, bool]:
        return "Hello! How can I help you today?", False

    def _handle_exit(self, intent: Intent) -> Tuple[str, bool]:
        self.reminder_manager.cancel_all()
        return "Goodbye!", True

    def _handle_get_time(self, intent: Intent) -> Tuple[str, bool]:
        now = datetime.now().strftime("%I:%M %p").lstrip("0")
        return f"It's currently {now}.", False

    def _handle_get_date(self, intent: Intent) -> Tuple[str, bool]:
        today = datetime.now().strftime("%A, %B %d, %Y")
        return f"Today is {today}.", False

    def _handle_web_search(self, intent: Intent) -> Tuple[str, bool]:
        query = intent.slots.get("query", "").strip()
        if not query:
            return "What would you like me to search for?", False
        url = "https://www.google.com/search?q=" + query.replace(" ", "+")
        webbrowser.open(url)
        return f"Here's what I found for {query}.", False

    def _handle_send_email(self, intent: Intent) -> Tuple[str, bool]:
        to = intent.slots.get("to", "")
        subject = intent.slots.get("subject", "")
        body = intent.slots.get("body", "")
        if not to:
            return "I need a recipient email address to send that.", False
        try:
            email_sender.send_email(to=to, subject=subject, body=body)
        except email_sender.EmailError as exc:
            return str(exc), False
        return f"Email sent to {to}.", False

    def _handle_set_reminder(self, intent: Intent) -> Tuple[str, bool]:
        task = intent.slots.get("task", "").strip()
        amount = intent.slots.get("amount")
        unit = intent.slots.get("unit")
        if not task or amount is None or unit is None:
            return "I need a task and a duration, like 'remind me to stretch in 10 minutes'.", False
        try:
            seconds = duration_to_seconds(amount, unit)
        except ReminderError as exc:
            return str(exc), False
        self.reminder_manager.schedule(task, seconds)
        return f"Okay, I'll remind you to {task} in {amount} {unit}.", False

    def _on_reminder_fired(self, task: str) -> None:
        # An audible alert: a couple of terminal bell characters plus TTS,
        # so the user notices even if they've stepped away from the screen.
        print("\a\a", end="", flush=True)
        self.speak(f"Reminder: {task}")

    def _handle_get_weather(self, intent: Intent) -> Tuple[str, bool]:
        city = intent.slots.get("city", "").strip()
        if not city:
            return "Which city's weather would you like?", False
        try:
            summary = weather.get_weather(city, api_key=self.weather_api_key)
        except weather.WeatherError as exc:
            return str(exc), False
        return summary, False

    def _handle_answer_question(self, intent: Intent) -> Tuple[str, bool]:
        question = intent.slots.get("query", "").strip()
        if not question:
            return "What would you like to know?", False
        try:
            answer = knowledge.answer_question(question)
        except knowledge.KnowledgeError as exc:
            return str(exc), False
        return answer, False

    def _handle_add_custom_command(self, intent: Intent) -> Tuple[str, bool]:
        self.speak("Sure — what phrase should trigger the new command?")
        trigger = self.listen("Waiting for the trigger phrase...")
        if not trigger:
            return "I didn't catch a trigger phrase, so I cancelled adding a command.", False

        self.speak("Got it. And what should I say when I hear that?")
        response = self.listen("Waiting for the response text...")
        if not response:
            return "I didn't catch a response, so I cancelled adding a command.", False

        self.command_store.add_command(trigger, response)
        return f"Done — when you say '{trigger}', I'll say '{response}'.", False

    def _handle_unknown(self, intent: Intent) -> Tuple[str, bool]:
        return (
            "Sorry, I didn't catch that. Could you say it again?",
            False,
        )
