BASIC WEATHER APP
=================

Files:
- weather_app.py
- README.txt

REQUIREMENT:
Python + requests + OpenWeatherMap API

1. Install requests
-------------------
Open the VS Code terminal and run:

pip install requests

2. Get an OpenWeatherMap API key
---------------------------------
Create a free account at OpenWeatherMap and get an API key.

3. Add your API key
-------------------
Open weather_app.py and replace:

API_KEY = "YOUR_OPENWEATHERMAP_API_KEY"

with your real API key, for example:

API_KEY = "123456789abcdef"

4. Run the program
------------------
In VS Code terminal:

python weather_app.py

5. Example
----------
Enter city name:
Kigali

The program displays:
- Temperature in Celsius
- Temperature in Fahrenheit
- Humidity
- Weather condition
- Wind speed

The program also handles:
- Empty city input
- City not found
- Invalid API key
- Internet/network errors
- Request timeout
