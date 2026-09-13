"""
custom_commands.py
===================
Lets users extend the assistant with their own trigger-phrase -> response
pairs, stored in a plain JSON config file (config/custom_commands.json by
default). Commands can be added two ways, per the project requirements:

1. **Editing the config file directly** — open config/custom_commands.json
   and add another {"trigger": ..., "response": ...} entry.
2. **Via voice/text prompt** — the "add_custom_command" intent walks the
   user through providing a trigger phrase and a response, then calls
   `add_command()` to persist it.

Matching is a simple case-insensitive substring check against the trigger
phrase. Custom command responses are plain spoken text only (no arbitrary
code execution), which keeps voice-driven configuration safe.
"""

from __future__ import annotations

import json
from pathlib import Path
from typing import Dict, List, Optional

DEFAULT_CONFIG_PATH = Path(__file__).resolve().parent.parent / "config" / "custom_commands.json"


class CustomCommandStore:
    def __init__(self, path: Path = DEFAULT_CONFIG_PATH):
        self.path = Path(path)
        self._commands: List[Dict[str, str]] = self._load()

    def _load(self) -> List[Dict[str, str]]:
        if not self.path.exists():
            return []
        try:
            with self.path.open("r", encoding="utf-8") as f:
                data = json.load(f)
            if isinstance(data, list):
                return data
        except (json.JSONDecodeError, OSError):
            pass
        return []

    def _save(self) -> None:
        self.path.parent.mkdir(parents=True, exist_ok=True)
        with self.path.open("w", encoding="utf-8") as f:
            json.dump(self._commands, f, indent=2, ensure_ascii=False)

    def match(self, text: str) -> Optional[str]:
        """Return the response for the first custom command whose trigger
        phrase appears in `text`, or None if no custom command matches."""
        lowered = text.lower()
        for entry in self._commands:
            trigger = entry.get("trigger", "").lower().strip()
            if trigger and trigger in lowered:
                return entry.get("response", "")
        return None

    def add_command(self, trigger: str, response: str) -> None:
        trigger = trigger.strip()
        response = response.strip()
        if not trigger or not response:
            raise ValueError("Both a trigger phrase and a response are required.")
        # Replace an existing entry with the same trigger rather than duplicating.
        self._commands = [c for c in self._commands if c.get("trigger", "").lower() != trigger.lower()]
        self._commands.append({"trigger": trigger, "response": response})
        self._save()

    def list_commands(self) -> List[Dict[str, str]]:
        return list(self._commands)
