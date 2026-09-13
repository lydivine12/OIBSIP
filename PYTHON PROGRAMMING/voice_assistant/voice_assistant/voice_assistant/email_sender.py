"""
email_sender.py
================
Sends an email via SMTP on behalf of a voice command.

SECURITY / PRIVACY NOTE
------------------------
Credentials are read ONLY from environment variables (see .env.example) —
never hardcode a real password in source code, and never use your primary
personal email password here. Use a dedicated test/dummy account, and if
using Gmail, generate an "App Password" rather than your real account
password (Google blocks plain-password SMTP login by default).

Nothing about the email (recipient, subject, body) is logged to disk by
this module; it only appears transiently in memory and in whatever the
assistant speaks/prints as a confirmation.
"""

from __future__ import annotations

import os
import smtplib
from email.message import EmailMessage
from typing import Optional


class EmailError(Exception):
    """Raised when sending fails for a user-facing reason."""


def send_email(
    to: str,
    subject: str,
    body: str,
    sender_email: Optional[str] = None,
    app_password: Optional[str] = None,
    smtp_server: Optional[str] = None,
    smtp_port: Optional[int] = None,
) -> None:
    """Send a plain-text email.

    All connection details fall back to environment variables if not passed
    explicitly:
        SENDER_EMAIL, SENDER_APP_PASSWORD, SMTP_SERVER, SMTP_PORT

    Defaults to Gmail's SMTP settings if SMTP_SERVER/SMTP_PORT aren't set,
    since that's the most common free option for a test account — but any
    SMTP provider works.
    """
    sender_email = sender_email or os.environ.get("SENDER_EMAIL")
    app_password = app_password or os.environ.get("SENDER_APP_PASSWORD")
    smtp_server = smtp_server or os.environ.get("SMTP_SERVER", "smtp.gmail.com")
    smtp_port = smtp_port or int(os.environ.get("SMTP_PORT", "587"))

    if not sender_email or not app_password:
        raise EmailError(
            "Email isn't configured. Set SENDER_EMAIL and SENDER_APP_PASSWORD "
            "environment variables (use a test account and an app password, "
            "not your real password)."
        )

    message = EmailMessage()
    message["From"] = sender_email
    message["To"] = to
    message["Subject"] = subject or "(no subject)"
    message.set_content(body or "")

    try:
        with smtplib.SMTP(smtp_server, smtp_port, timeout=10) as smtp:
            smtp.starttls()
            smtp.login(sender_email, app_password)
            smtp.send_message(message)
    except smtplib.SMTPAuthenticationError as exc:
        raise EmailError(
            "Email login failed. Double check SENDER_EMAIL/SENDER_APP_PASSWORD "
            "(Gmail requires an App Password, not your normal password)."
        ) from exc
    except (smtplib.SMTPException, OSError) as exc:
        raise EmailError(f"I couldn't send the email: {exc}") from exc
