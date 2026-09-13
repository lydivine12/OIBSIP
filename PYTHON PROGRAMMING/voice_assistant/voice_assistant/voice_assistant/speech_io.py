"""
speech_io.py
============
Wraps voice INPUT (speech_recognition + microphone) and voice OUTPUT
(pyttsx3 text-to-speech) behind one small interface, `SpeechIO`.

Why wrap them?
- Keeps hardware/network concerns (microphone, Google's speech API) out of
  the NLU and action-dispatch code, which makes those parts unit-testable.
- Lets the whole assistant run in a "text mode" (type instead of speak, read
  instead of hear) on machines without a working mic/speaker, or in CI.

Privacy note (see README.md for the full privacy section):
`speech_recognition`'s default `recognize_google()` call sends the recorded
audio snippet to Google's public Web Speech API for transcription. No audio
is stored on disk by this code, but it does leave the machine. If that's not
acceptable for your use case, swap `recognize_google` for an offline engine
such as Vosk or Whisper.cpp — the rest of the assistant doesn't care how
`listen()` gets its text.
"""

from __future__ import annotations

import sys
from typing import Optional

try:
    import speech_recognition as sr
except ImportError:  # pragma: no cover - exercised only when dependency missing
    sr = None

try:
    import pyttsx3
except ImportError:  # pragma: no cover
    pyttsx3 = None


class SpeechIO:
    """Handles capturing spoken commands and speaking responses aloud.

    Parameters
    ----------
    text_mode:
        If True, bypass the microphone and TTS engine entirely and instead
        read commands from stdin / print responses to stdout. Useful for
        testing, headless servers, or users who prefer typing.
    energy_threshold:
        Passed to the recognizer to tune microphone sensitivity. The default
        works for most laptop microphones in a quiet room; raise it in noisy
        environments.
    """

    def __init__(self, text_mode: bool = False, energy_threshold: int = 300):
        self.text_mode = text_mode or sr is None or pyttsx3 is None

        if self.text_mode and sr is None:
            print(
                "[speech_io] 'speech_recognition' is not installed — "
                "falling back to text mode.",
                file=sys.stderr,
            )
        if self.text_mode and pyttsx3 is None:
            print(
                "[speech_io] 'pyttsx3' is not installed — "
                "falling back to text mode.",
                file=sys.stderr,
            )

        self._recognizer: Optional["sr.Recognizer"] = None
        self._microphone: Optional["sr.Microphone"] = None
        self._tts_engine = None

        if not self.text_mode:
            self._recognizer = sr.Recognizer()
            self._recognizer.energy_threshold = energy_threshold
            self._microphone = sr.Microphone()
            self._tts_engine = pyttsx3.init()
            # A calmer default rate than pyttsx3's sometimes-too-fast default.
            self._tts_engine.setProperty("rate", 175)

    # ------------------------------------------------------------------
    # Output
    # ------------------------------------------------------------------
    def speak(self, text: str) -> None:
        """Speak `text` aloud (or print it, in text mode)."""
        print(f"Assistant: {text}")
        if self.text_mode:
            return
        self._tts_engine.say(text)
        self._tts_engine.runAndWait()

    # ------------------------------------------------------------------
    # Input
    # ------------------------------------------------------------------
    def listen(self, prompt: str = "Listening...") -> Optional[str]:
        """Capture one spoken command and return the recognized text.

        Returns None (rather than raising) if the audio couldn't be
        understood or the recognition service couldn't be reached, so
        callers can implement graceful "please repeat that" handling
        without a try/except at every call site.
        """
        if self.text_mode:
            try:
                return input(f"{prompt} (type your command) > ").strip()
            except EOFError:
                return None

        with self._microphone as source:
            print(prompt)
            # Briefly sample ambient noise so short/quiet commands aren't
            # cut off or misheard because of a stale energy threshold.
            self._recognizer.adjust_for_ambient_noise(source, duration=0.5)
            try:
                audio = self._recognizer.listen(source, timeout=6, phrase_time_limit=12)
            except sr.WaitTimeoutError:
                return None  # nobody spoke — treat like "didn't catch that"

        try:
            return self._recognizer.recognize_google(audio)
        except sr.UnknownValueError:
            # Audio was captured but speech wasn't intelligible.
            return None
        except sr.RequestError as exc:
            print(f"[speech_io] Speech recognition service error: {exc}", file=sys.stderr)
            return None
