"""
reminders.py
============
Schedules "remind me to X in N minutes/hours/seconds" requests using
`threading.Timer`, so the assistant can keep listening for new commands
while a reminder counts down in the background.

Nothing here is persisted to disk — reminders are in-memory only and are
lost if the program exits before they fire. That's an intentional privacy
choice (see README): reminder text isn't written anywhere.
"""

from __future__ import annotations

import threading
from dataclasses import dataclass, field
from typing import Callable, Dict, List

_UNIT_TO_SECONDS = {
    "second": 1, "seconds": 1,
    "minute": 60, "minutes": 60, "min": 60, "mins": 60,
    "hour": 3600, "hours": 3600, "hr": 3600, "hrs": 3600,
}


class ReminderError(Exception):
    """Raised for user-facing reminder scheduling problems."""


def duration_to_seconds(amount: str, unit: str) -> int:
    """Convert a parsed (amount, unit) pair like ("10", "minutes") to seconds."""
    unit_key = unit.lower().rstrip(".")
    if unit_key not in _UNIT_TO_SECONDS:
        raise ReminderError(f"I don't recognize the time unit '{unit}'.")
    try:
        amount_int = int(amount)
    except ValueError as exc:
        raise ReminderError(f"'{amount}' doesn't look like a number.") from exc
    if amount_int <= 0:
        raise ReminderError("The reminder duration needs to be a positive number.")
    return amount_int * _UNIT_TO_SECONDS[unit_key]


@dataclass
class ReminderManager:
    """Tracks in-flight reminders and fires an alert callback when they're due."""

    on_alert: Callable[[str], None]
    _timers: List[threading.Timer] = field(default_factory=list)
    _lock: threading.Lock = field(default_factory=threading.Lock)

    def schedule(self, task: str, seconds: int) -> None:
        def _fire():
            self.on_alert(task)
            with self._lock:
                self._timers = [t for t in self._timers if t.is_alive()]

        timer = threading.Timer(seconds, _fire)
        timer.daemon = True  # don't block program exit on a pending reminder
        with self._lock:
            self._timers.append(timer)
        timer.start()

    def pending_count(self) -> int:
        with self._lock:
            return sum(1 for t in self._timers if t.is_alive())

    def cancel_all(self) -> None:
        with self._lock:
            for t in self._timers:
                t.cancel()
            self._timers.clear()
