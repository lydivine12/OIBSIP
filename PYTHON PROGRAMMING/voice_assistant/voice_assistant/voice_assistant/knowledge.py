"""
knowledge.py
============
Answers general-knowledge questions two ways:

1. **Local knowledge base** (`LOCAL_KB`) — a tiny dictionary of canned
   answers for questions about the assistant itself ("what's your name",
   "who made you"). Checked first because it's instant and needs no
   network access.

2. **Wikipedia REST summary API** — for everything else, we extract a
   likely topic from the question (stripping leading words like "who is",
   "what is", "tell me about") and fetch a short summary. This is a free,
   keyless API, which keeps setup simple for this project.

Only the extracted topic/question text is sent over the network — never
audio or any other personal data.
"""

from __future__ import annotations

import re
from typing import Optional

import requests

WIKIPEDIA_SUMMARY_URL = "https://en.wikipedia.org/api/rest_v1/page/summary/{title}"

# A tiny local knowledge base for questions about the assistant itself.
# Extend this dict (or load extra entries from a config file) to add more
# offline-answerable questions without any network dependency.
LOCAL_KB = {
    "what is your name": "I'm your Python voice assistant. You can call me Assistant.",
    "who are you": "I'm a voice assistant built with Python, speech_recognition, and pyttsx3.",
    "who made you": "I was built as a Python voice assistant project.",
    "what can you do": (
        "I can tell the time and date, search the web, send emails, set reminders, "
        "check the weather, and answer general knowledge questions."
    ),
}

_LEADING_PHRASES = re.compile(
    r"^\s*(who\s+is|who\s+was|what\s+is|what\s+are|what\s+was|tell\s+me\s+about|"
    r"when\s+(?:was|is|did)|where\s+is|why\s+(?:is|does|did)|how\s+(?:is|does|did))\s+",
    re.IGNORECASE,
)


class KnowledgeError(Exception):
    """Raised when no answer could be found for a user-facing reason."""


def _extract_topic(question: str) -> str:
    topic = _LEADING_PHRASES.sub("", question).strip()
    topic = topic.rstrip("?.! ")
    return topic or question


def answer_question(question: str) -> str:
    """Return a short spoken-friendly answer to a general-knowledge question."""
    normalized = question.strip().rstrip("?.! ").lower()

    if normalized in LOCAL_KB:
        return LOCAL_KB[normalized]

    topic = _extract_topic(question)
    if not topic:
        raise KnowledgeError("I'm not sure what you're asking about.")

    try:
        response = requests.get(
            WIKIPEDIA_SUMMARY_URL.format(title=topic.replace(" ", "_")),
            headers={"User-Agent": "python-voice-assistant/1.0"},
            timeout=8,
        )
    except requests.RequestException as exc:
        raise KnowledgeError(f"I couldn't reach the knowledge service: {exc}") from exc

    if response.status_code == 404:
        raise KnowledgeError(f"I couldn't find anything about {topic}.")
    if not response.ok:
        raise KnowledgeError("The knowledge service returned an error.")

    data = response.json()
    extract: Optional[str] = data.get("extract")
    if not extract:
        raise KnowledgeError(f"I couldn't find a summary for {topic}.")

    # Keep spoken answers short: first one or two sentences.
    sentences = re.split(r"(?<=[.!?])\s+", extract)
    return " ".join(sentences[:2])
