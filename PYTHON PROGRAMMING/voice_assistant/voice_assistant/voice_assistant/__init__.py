"""
voice_assistant
================
A modular Python voice assistant.

The package is deliberately split into small, single-purpose modules so that
the "brain" of the assistant (intent parsing + action dispatch) can be tested
and reused independently of the speech I/O layer (microphone + text-to-speech).
This also means the assistant can run in a text-only "keyboard mode" on
machines without a microphone/speaker, which is useful for development,
automated testing, and accessibility.
"""

__version__ = "1.0.0"
