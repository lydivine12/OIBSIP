"""
main.py
=======
Entry point for the voice assistant. Wires together:

    speech_io.SpeechIO   (hear commands / speak responses)
    nlu.parse_intent     (understand what was said)
    actions.ActionDispatcher  (do something about it, get a reply)

and runs the classic assistant loop:

    listen -> parse -> act -> speak -> repeat (until "exit")

Run modes
---------
    python -m voice_assistant.main            # microphone + speaker
    python -m voice_assistant.main --text      # type commands, read replies

The --text flag is also how this project can be exercised in environments
without a working microphone/speaker (CI, containers, this very notebook's
sandbox, screen-reader-only setups, etc.) — the NLU and action-dispatch
logic is identical either way.
"""

from __future__ import annotations

import argparse
import os

from .actions import ActionDispatcher
from .nlu import parse_intent
from .speech_io import SpeechIO

MAX_CONSECUTIVE_MISUNDERSTANDINGS = 3


def run(text_mode: bool = False) -> None:
    io = SpeechIO(text_mode=text_mode)
    dispatcher = ActionDispatcher(
        speak=io.speak,
        listen=io.listen,
        weather_api_key=os.environ.get("OPENWEATHER_API_KEY"),
    )

    io.speak(
        "Voice assistant ready. Say 'hello' to get started, or 'quit' to exit."
    )

    consecutive_misses = 0
    while True:
        heard = io.listen()

        if heard is None or not heard.strip():
            consecutive_misses += 1
            if consecutive_misses >= MAX_CONSECUTIVE_MISUNDERSTANDINGS:
                io.speak(
                    "I'm having trouble hearing you. Check your microphone "
                    "and try again, or type --text mode to use the keyboard."
                )
                consecutive_misses = 0
            else:
                io.speak("Sorry, I didn't catch that. Could you repeat that?")
            continue

        consecutive_misses = 0
        print(f"You said: {heard}")

        intent = parse_intent(heard)
        response, should_exit = dispatcher.handle(intent, heard)
        io.speak(response)

        if should_exit:
            break


def main() -> None:
    parser = argparse.ArgumentParser(description="A Python voice assistant.")
    parser.add_argument(
        "--text",
        action="store_true",
        help="Run in text mode (type commands, read replies) instead of using the microphone/speaker.",
    )
    args = parser.parse_args()
    run(text_mode=args.text)


if __name__ == "__main__":
    main()
