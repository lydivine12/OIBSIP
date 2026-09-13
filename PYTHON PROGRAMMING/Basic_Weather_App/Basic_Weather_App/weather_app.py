import requests

# =========================================================
# BASIC WEATHER APP
# =========================================================

API_KEY = "YOUR_OPENWEATHERMAP_API_KEY"


def get_weather():
    city = input("Enter city name: ").strip()

    if not city:
        print("Error: City name cannot be empty.")
        return

    if API_KEY == "YOUR_OPENWEATHERMAP_API_KEY":
        print("Error: Please add your OpenWeatherMap API key to the API_KEY variable.")
        return

    url = "https://api.openweathermap.org/data/2.5/weather"

    params = {
        "q": city,
        "appid": API_KEY,
        "units": "metric"
    }

    try:
        response = requests.get(url, params=params, timeout=10)

        if response.status_code == 404:
            print("Error: City not found. Please check the city name.")
            return

        if response.status_code == 401:
            print("Error: Invalid API key.")
            return

        response.raise_for_status()

        data = response.json()

        temperature_c = data["main"]["temp"]
        temperature_f = (temperature_c * 9 / 5) + 32
        humidity = data["main"]["humidity"]
        description = data["weather"][0]["description"].title()
        wind_speed = data["wind"]["speed"]

        print("\n" + "=" * 35)
        print(f"Weather in {data['name']}, {data['sys']['country']}")
        print("=" * 35)
        print(f"Temperature: {temperature_c:.1f} °C")
        print(f"Temperature: {temperature_f:.1f} °F")
        print(f"Humidity: {humidity}%")
        print(f"Condition: {description}")
        print(f"Wind Speed: {wind_speed} m/s")
        print("=" * 35)

    except requests.exceptions.Timeout:
        print("Error: The request timed out. Please try again.")

    except requests.exceptions.ConnectionError:
        print("Error: No internet connection or weather service unavailable.")

    except requests.exceptions.RequestException as error:
        print(f"Error: Could not get weather data.\n{error}")

    except (KeyError, ValueError):
        print("Error: Unexpected weather data received.")


if __name__ == "__main__":
    get_weather()
