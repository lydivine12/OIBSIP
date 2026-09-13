# Python Voice Assistant

A modular Python voice assistant. The **beginner tier** covers voice capture,
greetings, time/date, web search, graceful error handling, and text-to-speech.
The **advanced tier** adds free-form natural language understanding, email
sending, timed reminders, live weather, general-knowledge Q&A, and
user-extensible custom commands.

## Architecture

The project is split so the "brain" (understanding + deciding what to do) is
completely independent of the "body" (microphone/speaker hardware):

```
voice_assistant/
├── speech_io.py         # microphone capture (speech_recognition) + TTS (pyttsx3)
│                         # also provides a --text mode with no hardware needed
├── nlu.py                # free-form sentence -> Intent(name, slots)
├── actions.py             # Intent -> concrete action -> spoken response
├── weather.py             # OpenWeatherMap API client
├── knowledge.py            # Wikipedia summary API + small local knowledge base
├── email_sender.py          # smtplib wrapper
├── reminders.py              # threading.Timer based reminder scheduling
├── custom_commands.py         # load/save user-defined trigger -> response pairs
└── main.py                     # listen -> parse -> act -> speak loop

config/
└── custom_commands.json   # editable JSON config for custom commands

tests/
└── test_nlu_and_actions.py  # unit tests, no hardware/network required
```

Because `nlu.py` and `actions.py` only deal with plain text in and text out,
they're fully unit-testable (see `tests/`) and the entire assistant can also
run keyboard-only via `--text` mode — useful for development, CI, screen
readers, or machines without a working mic/speaker.

## Setup

1. **Python dependencies:**

   ```bash
   pip install -r requirements.txt
   ```

   `PyAudio` (needed by `speech_recognition` for microphone access) requires
   a system-level audio library:
   - macOS: `brew install portaudio` before `pip install pyaudio`
   - Debian/Ubuntu: `sudo apt-get install portaudio19-dev python3-pyaudio`
   - Windows: the prebuilt `PyAudio` wheel usually installs without extra steps

   If you only want to try the assistant without dealing with audio setup at
   all, skip straight to **text mode** below — `speech_recognition` and
   `pyttsx3` are optional and the assistant automatically falls back to
   keyboard/print I/O if they aren't installed.

2. **Configuration (advanced features):** copy `.env.example` to `.env` and
   fill in the values you need (or export them as real environment
   variables):

   ```bash
   cp .env.example .env
   # then edit .env
   export $(grep -v '^#' .env | xargs)   # or use a tool like python-dotenv
   ```

   - `OPENWEATHER_API_KEY` — free key from https://openweathermap.org/api,
     required for weather requests.
   - `SENDER_EMAIL` / `SENDER_APP_PASSWORD` — a **test/dummy** email account
     used to send email on your behalf. For Gmail, turn on 2FA and generate
     an **App Password** (https://myaccount.google.com/apppasswords) —
     never put your real account password here.
   - `SMTP_SERVER` / `SMTP_PORT` — defaults to Gmail's SMTP settings.

   Beginner-tier features (greeting, time, date, web search) need no
   configuration at all.

## Running it

```bash
# Microphone + speaker mode
python -m voice_assistant.main

# Text mode (type commands, read replies) — no mic/speaker needed
python -m voice_assistant.main --text
```

## Example commands

| Say / type                                                             | Tier      |
|--------------------------------------------------------------------------|-----------|
| "hello"                                                                   | Beginner  |
| "what time is it" / "what's the date"                                    | Beginner  |
| "search for the best pizza place near me"                                | Beginner  |
| "what's the weather like in Tokyo"                                       | Advanced  |
| "who is Ada Lovelace" / "what is photosynthesis"                         | Advanced  |
| "remind me to take a break in 10 minutes"                                | Advanced  |
| "send an email to jane@example.com about hi saying how are you"          | Advanced  |
| "add a new command" (then follow the prompts)                            | Advanced  |
| "quit" / "goodbye"                                                       | Both      |

If a spoken command isn't understood, the assistant asks you to repeat it
rather than crashing or guessing silently (see `main.py`'s
`MAX_CONSECUTIVE_MISUNDERSTANDINGS` handling and `actions.py`'s
`_handle_unknown`).

## Natural language understanding

`nlu.py` parses free-form sentences in two stages, rather than requiring an
exact keyword:

1. **Pattern + slot extraction** — regexes pull structured data out of the
   sentence (e.g. the city name out of "what's the weather like in Paris",
   or the recipient/subject/body out of an email request).
2. **Bag-of-words fallback** — if no pattern matches, the sentence is scored
   against representative example phrases for each intent, so unanticipated
   phrasing (e.g. "could you tell me what day it is today") still resolves
   correctly.

No large ML model download is required, which keeps the project runnable
fully offline for the understanding step (only the actions that need
external data — weather, search, Q&A, email — need internet access).

## Custom commands

Add your own trigger phrase → response pairs two ways:

- **Edit the config file directly:** open `config/custom_commands.json` and
  add another entry:
  ```json
  {"trigger": "play music", "response": "Starting your playlist now."}
  ```
- **Voice/text prompt:** say "add a new command" and follow the assistant's
  prompts for a trigger phrase and a response. This is handled by
  `custom_commands.py`'s `add_command()`, which persists to the same JSON
  file.

Custom command responses are plain spoken text only — they can't execute
arbitrary code — which keeps voice-configured commands safe.

## Testing

```bash
python -m pytest tests/ -v
```

The test suite covers intent classification, slot extraction, reminder
duration parsing, custom command storage, and action dispatch — all without
a microphone, speaker, or live network calls (network-dependent functions
are mocked).

## Privacy considerations

This section documents what data this assistant processes, where it goes,
and how to reduce that footprint if you're privacy-sensitive.

**Voice audio**
- When running in microphone mode, `speech_recognition`'s default
  `recognize_google()` call sends short audio clips of your speech to
  **Google's public Web Speech API** for transcription. This happens for
  every listened command, whether or not it's understood.
- No audio is written to disk by this project's own code.
- **To avoid sending audio to a third party:** swap the recognizer in
  `speech_io.py` for an offline engine, e.g. [Vosk](https://alphacephei.com/vosk/)
  or a local Whisper.cpp binding. `speech_io.SpeechIO.listen()` is the only
  place that would need to change — nothing downstream cares how the text
  was obtained.
- **To avoid audio entirely:** use `--text` mode, which never touches the
  microphone.

**Weather requests**
- Only the city name you ask about (plus your API key) is sent to
  OpenWeatherMap. No audio, location coordinates, or other personal data is
  transmitted.

**General-knowledge questions**
- The extracted topic/question text (not audio) is sent to Wikipedia's
  public REST summary API. Questions about the assistant itself ("what's
  your name") are answered from a local, offline dictionary and never leave
  your machine.

**Web search**
- The search query is opened in your default browser via a standard Google
  search URL — the same as if you'd typed it into the address bar yourself.
  This project does not scrape or store search results.

**Email**
- Recipient, subject, and body are sent directly to your configured SMTP
  server (e.g. Gmail) to deliver the message — that's an inherent part of
  sending an email. Credentials are read only from environment variables,
  are never logged, and should be a dedicated test account with an
  app-specific password rather than your primary email account.

**Reminders and custom commands**
- Reminder text lives only in memory (via `threading.Timer`) and is lost on
  restart — nothing is written to disk.
- Custom commands you add are stored locally in
  `config/custom_commands.json`. Nothing about them is sent anywhere.

**What's never collected**
- This project does not log conversation history, does not phone home to
  any analytics service, and does not persist audio recordings anywhere.

## Extending further

- Swap the intent parser for a transformer-based classifier if you want
  more robust NLU and don't mind the extra dependency weight and model
  download.
- Add a wake word (e.g. via `pvporcupine`) so the assistant only starts
  listening after hearing a trigger phrase like "hey assistant."
- Persist reminders to disk (e.g. SQLite) if you need them to survive a
  restart — trade-off against the current "nothing written to disk" privacy
  stance for reminders.
