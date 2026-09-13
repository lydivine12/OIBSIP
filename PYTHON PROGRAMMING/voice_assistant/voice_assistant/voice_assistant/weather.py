"""
weather.py
==========
Fetches current weather conditions from the OpenWeatherMap free-tier API.

Only the city name and API key ever leave the machine here — no audio, no
personal data beyond whatever city the user asks about.

Get a free API key at https://openweathermap.org/api and set it as the
OPENWEATHER_API_KEY environment variable (see .env.example).
"""

from __future__ import annotations

import os
from typing import Optional

import requests

OPENWEATHERMAP_URL = "https://api.openweathermap.org/data/2.5/weather"


class WeatherError(Exception):
    """Raised when the weather lookup fails for a user-facing reason."""


def get_weather(city: str, api_key: Optional[str] = None, units: str = "metric") -> str:
    """Return a short spoken-friendly weather summary for `city`.

    Parameters
    ----------
    city: e.g. "Kigali", "New York"
    api_key: OpenWeatherMap API key. Falls back to the OPENWEATHER_API_KEY
        environment variable if not passed explicitly.
    units: "metric" (Celsius) or "imperial" (Fahrenheit).

    Raises
    ------
    WeatherError with a human-readable message on any failure (missing key,
    unknown city, network problem) so the caller can just speak the message.
    """
    api_key = api_key or os.environ.get("OPENWEATHER_API_KEY")
    if not api_key:
        raise WeatherError(
            "I don't have a weather API key configured. Set the "
            "OPENWEATHER_API_KEY environment variable to enable weather lookups."
        )

    try:
        response = requests.get(
            OPENWEATHERMAP_URL,
            params={"q": city, "appid": api_key, "units": units},
            timeout=8,
        )
    except requests.RequestException as exc:
        raise WeatherError(f"I couldn't reach the weather service: {exc}") from exc

    if response.status_code == 404:
        raise WeatherError(f"I couldn't find weather data for {city}.")
    if response.status_code == 401:
        raise WeatherError("The weather API key seems to be invalid.")
    if not response.ok:
        raise WeatherError(f"The weather service returned an error (status {response.status_code}).")

    data = response.json()
    try:
        description = data["weather"][0]["description"]
        temp = data["main"]["temp"]
        feels_like = data["main"]["feels_like"]
    except (KeyError, IndexError) as exc:
        raise WeatherError("The weather service returned an unexpected response.") from exc

    unit_symbol = "C" if units == "metric" else "F"
    return (
        f"It's currently {temp:.0f}°{unit_symbol} in {city.title()} with {description}, "
        f"feels like {feels_like:.0f}°{unit_symbol}."
    )
