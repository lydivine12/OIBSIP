"""
nlu.py
======
Lightweight natural-language-understanding layer.

Goal: turn a free-form spoken sentence like

    "hey can you please search the web for the best pizza place near me"

into a structured result:

    Intent(name="web_search", slots={"query": "the best pizza place near me"})

This is NOT a full statistical/ML NLU pipeline — no heavyweight model
download is required, so the assistant works fully offline for intent
parsing (only the *actions* triggered by an intent, like weather or QA,
need the internet). Instead it uses two complementary techniques that go
beyond plain keyword matching:

1. **Pattern + slot extraction** — each intent has one or more regexes that
   match a variety of phrasings and pull out the meaningful "slot" (the
   search query, the city name, the reminder duration, the email
   recipient/subject/body, etc.) directly from the sentence structure.

2. **Bag-of-words fallback scoring** — if no regex matches, the sentence is
   compared against a set of representative example utterances for every
   intent using token overlap. The intent with the highest overlap score
   wins, provided it clears a minimum-confidence threshold. This lets the
   assistant handle sentences that are phrased in an unanticipated way
   ("could you tell me what day it is today") without needing an exact
   pattern for every possible wording.

If nothing matches with reasonable confidence, intent "unknown" is
returned so the caller can ask the user to repeat/rephrase.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from typing import Dict, List, Optional, Pattern


@dataclass
class Intent:
    name: str
    slots: Dict[str, str] = field(default_factory=dict)
    confidence: float = 1.0


@dataclass
class _IntentSpec:
    name: str
    patterns: List[Pattern]
    examples: List[str]  # used only for the bag-of-words fallback


def _p(pattern: str) -> Pattern:
    return re.compile(pattern, re.IGNORECASE)


# ---------------------------------------------------------------------------
# Intent definitions
# ---------------------------------------------------------------------------
# Order matters only in that more specific patterns are listed first so a
# sentence that could plausibly match two intents prefers the more precise
# one (e.g. "what time is it" should never fall into web_search).
_INTENT_SPECS: List[_IntentSpec] = [
    _IntentSpec(
        name="exit",
        patterns=[
            _p(r"\b(quit|exit|stop listening|goodbye|good bye|shut down|shutdown)\b"),
        ],
        examples=["quit", "exit the program", "goodbye", "stop listening now"],
    ),
    _IntentSpec(
        name="greeting",
        patterns=[
            _p(r"^\s*(hi|hello|hey|hiya|yo)\b"),
        ],
        examples=["hello", "hi there", "hey assistant", "hiya"],
    ),
    _IntentSpec(
        name="get_time",
        patterns=[
            _p(r"\bwhat(?:'s| is)?\s+the\s+time\b"),
            _p(r"\bwhat\s+time\s+is\s+it\b"),
            _p(r"\btell\s+me\s+the\s+time\b"),
        ],
        examples=["what time is it", "tell me the time", "what's the time right now"],
    ),
    _IntentSpec(
        name="get_date",
        patterns=[
            _p(r"\bwhat(?:'s| is)?\s+(?:the\s+)?date\b"),
            _p(r"\bwhat\s+day\s+is\s+it\b"),
            _p(r"\btell\s+me\s+(?:the\s+)?date\b"),
            _p(r"\bwhat\s+day\s+of\s+the\s+week\b"),
        ],
        examples=["what's the date today", "what day is it", "tell me today's date"],
    ),
    _IntentSpec(
        name="web_search",
        patterns=[
            _p(r"\b(?:search|google|look up|lookup)\s+(?:for\s+|the\s+web\s+for\s+)?(?P<query>.+)"),
            _p(r"\bfind\s+(?:me\s+)?(?:information\s+on|info\s+on|results\s+for)\s+(?P<query>.+)"),
        ],
        examples=["search for the best pizza place", "google python tutorials", "look up nearby coffee shops"],
    ),
    _IntentSpec(
        name="send_email",
        patterns=[
            _p(
                r"\bsend\s+(?:an?\s+)?email\s+to\s+(?P<to>[\w.\-+]+@[\w\-]+\.[\w.\-]+)"
                r"(?:\s+(?:about|with subject|subject)\s+(?P<subject>.+?))?"
                r"(?:\s+(?:saying|that says|with message|body)\s+(?P<body>.+))?$"
            ),
        ],
        examples=[
            "send an email to jane@example.com about the meeting saying let's reschedule",
            "send email to bob@example.com subject hello saying how are you",
        ],
    ),
    _IntentSpec(
        name="set_reminder",
        patterns=[
            _p(
                r"\bremind\s+me\s+to\s+(?P<task>.+?)\s+in\s+"
                r"(?P<amount>\d+)\s*(?P<unit>seconds?|minutes?|mins?|hours?|hrs?)\b"
            ),
            _p(
                r"\bset\s+a\s+reminder\s+(?:to\s+)?(?P<task>.+?)\s+(?:for|in)\s+"
                r"(?P<amount>\d+)\s*(?P<unit>seconds?|minutes?|mins?|hours?|hrs?)\b"
            ),
        ],
        examples=["remind me to take a break in 10 minutes", "set a reminder to call mom in 2 hours"],
    ),
    _IntentSpec(
        name="get_weather",
        patterns=[
            _p(r"\bweather\s+(?:in|for|at)\s+(?P<city>[a-zA-Z\s]+?)(?:\?|$)"),
            _p(r"\bwhat(?:'s| is)\s+the\s+weather\s+(?:like\s+)?(?:in|for|at)\s+(?P<city>[a-zA-Z\s]+?)(?:\?|$)"),
            _p(r"\bhow(?:'s| is)\s+the\s+weather\s+(?:in|for|at)\s+(?P<city>[a-zA-Z\s]+?)(?:\?|$)"),
        ],
        examples=["what's the weather in Kigali", "how's the weather in Paris today", "weather for Tokyo"],
    ),
    _IntentSpec(
        name="add_custom_command",
        patterns=[
            _p(r"\badd\s+a\s+(?:new\s+)?command\b"),
            _p(r"\bcreate\s+a\s+(?:new\s+)?(?:custom\s+)?command\b"),
            _p(r"\bteach\s+you\s+a\s+new\s+command\b"),
        ],
        examples=["add a new command", "teach you a new command", "create a custom command"],
    ),
    _IntentSpec(
        name="answer_question",
        patterns=[
            _p(r"^\s*(?:who|what|when|where|why|how)\b.+"),
        ],
        examples=[
            "who is the president of France",
            "what is photosynthesis",
            "when was the Eiffel Tower built",
        ],
    ),
]


def _tokenize(text: str) -> set:
    return set(re.findall(r"[a-z0-9']+", text.lower()))


def _fallback_score(text: str, spec: _IntentSpec) -> float:
    """Bag-of-words overlap between `text` and an intent's example utterances.

    Returns a score in [0, 1]: the fraction of the input's tokens that also
    appear in the best-matching example. This is intentionally simple (no
    external NLP model needed) but still captures "meaning" better than a
    single-keyword check, since it considers the whole sentence.
    """
    input_tokens = _tokenize(text)
    if not input_tokens:
        return 0.0
    best = 0.0
    for example in spec.examples:
        example_tokens = _tokenize(example)
        if not example_tokens:
            continue
        overlap = len(input_tokens & example_tokens) / len(input_tokens)
        best = max(best, overlap)
    return best


def parse_intent(text: str, fallback_threshold: float = 0.34) -> Intent:
    """Parse `text` into an Intent.

    First tries exact regex/slot patterns (high precision). If none match,
    falls back to bag-of-words scoring so free-form phrasing still has a
    chance of being understood. If nothing clears the confidence threshold,
    returns intent "unknown".
    """
    text = text.strip()
    if not text:
        return Intent(name="unknown", confidence=0.0)

    # --- Stage 1: precise pattern matching with slot extraction ---
    for spec in _INTENT_SPECS:
        for pattern in spec.patterns:
            match = pattern.search(text)
            if match:
                slots = {k: v.strip() for k, v in match.groupdict().items() if v}
                # answer_question has no named slot; use the whole sentence
                if spec.name == "answer_question" and "query" not in slots:
                    slots["query"] = text
                return Intent(name=spec.name, slots=slots, confidence=1.0)

    # --- Stage 2: bag-of-words fallback ---
    best_spec: Optional[_IntentSpec] = None
    best_score = 0.0
    for spec in _INTENT_SPECS:
        score = _fallback_score(text, spec)
        if score > best_score:
            best_score = score
            best_spec = spec

    if best_spec is not None and best_score >= fallback_threshold:
        slots = {}
        if best_spec.name in ("web_search", "answer_question"):
            slots["query"] = text
        return Intent(name=best_spec.name, slots=slots, confidence=best_score)

    return Intent(name="unknown", confidence=best_score)
