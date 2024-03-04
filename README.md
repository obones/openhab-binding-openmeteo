# Open Meteo Binding

openHAB binding for [Open Meteo](https://open-meteo.com/) weather forecast service 

## Supported Things

There are two supported things.

### Open Meteo bridge

The bridge represents the connection to the Open Meteo service.
If you are a paid user of this service, you will want to use the advanced properties to set your custom URL and API key.

### Weather forecast

The second thing `forecast` supports the hourly and daily forecast for a specific location.
It requires coordinates of the location of your interest.
You can add as many `forecast` things for different locations to your setup as you like to observe.

## Thing Configuration

### Open Meteo bridge

| Parameter       | Description                                                                                             |
|-----------------|---------------------------------------------------------------------------------------------------------|
| refreshInterval | Specifies the refresh interval (in minutes). Optional, the default value is 60, the minimum value is 1. |
| baseURI         | The base URI to connect to. The default value is fine for connecting to the free tier API.              |
| apikey          | API key to access the OpenWeatherMap API.                                                               |

### Weather forecast

| Parameter        | Description                                                                                                                    |
|------------------|--------------------------------------------------------------------------------------------------------------------------------|
| location         | Location of weather in geographical coordinates (latitude/longitude/altitude). **Mandatory**                                   |
| hourlyHours      | Number of hours for hourly forecast. Optional, the default value is 48 (min="1", max="384", step="1").                         |
| hourlyTimeSeries | Whether to create a hourly time series channel group or not. Time series are new in 4.1 (default=true)          |
| hourlySplit      | Whether to create one channel group per future hour to accommodate widgets that are not capable of using time series. (default = false) |
| dailyDays        | Number of days for daily forecast (including todays forecast). Optional, the default value is 5 (min="1", max="16", step="1"). |
| dailyTimeSeries  | Whether to create a daily time series channel group or not. Time series are new in 4.1 (default=true)          |
| dailySplit       | Whether to create one channel group per future day to accommodate widgets that are not capable of using time series. (default = false) |
| includeTemperature         | Create channels for temperature, instant on hourly, min and max on daily (default: true) |
| includeHumidity            | Create a channel for humidity (default: true) |
| includeDewPoint            | Create a channel for dew point (default: true) |
| includeApparentTemperature | Create a channel for apparent temperature, instant on hourly, min and max on daily (default: true) |
| includePressure            | Create a channel for surface pressure (default: true) |
| includeCloudiness          | Create a channel for cloudiness (default: true) |
| includeWindSpeed           | Create a channel for wind speed (default: true) |
| includeWindDirection       | Create a channel for wind direction (default: true) |
| includeGustSpeed           | Create a channel for gusts speed (default: true) |
| includeShortwaveRadiation  | Create a channel for shortwave radiation (default: false) |
| includeDirectRadiation     | Create a channel for direct radiation (default: false) |
| includeDirectNormalIrradiance | Create a channel for normal irradiance (default: false) |
| includeDiffuseRadiation       | Create a channel for diffuse radiation (default: false) |
| includeVapourPressureDeficit  | Create a channel for vapour pressure deficit (VPD) (default: false) |
| includeCape                   | Create a channel for Convective available potential energy (CAPE)  (default: false) |
| includeEvapotranspiration     | Create a channel for evapotranspiration (default: false) |
| includeEt0FAOEvapotranspiration | Create a channel for ET₀ Reference evapotranspiration of a well watered grass field (default: false) |
| includePrecipitation            | Create a channel for precipitation (rain + showers + snow) (default: false) |
| includeSnow                     | Create a channel for snow fall (default: true) |
| includePrecipitationProbability | Create a channel for precipitation probability (default: true) |
| includeRain                     | Create a channel for rain (default: true) |
| includeShowers                  | Create a channel for convective showers (default: false) |
| includeWeatherCode              | Create a channel for weather code, using the WMO table values (default: true) |
| includeSnowDepth                | Create a channel for snow depth (default: false) |
| includeFreezingLevelHeight      | Create a channel for 0°C altitude (default: false) |
| includeVisibility               | Create a channel for visibility distance (default: true) |
| includeIsDay                    | Create a channel indicating wether it's day or night for the given moment (default: false) |
| includeSunrise                  | Create a channel for sunrise hour (default: true) |
| includeSunset                   | Create a channel for sunset hour (default: true) |
| includeSunshineDuration         | Create a channel for sunshine duration, always shorter than daylight because of dusk and dawn (default: false) |
| includeDaylightDuration         | Create a channel for daylight duration (default: false) |
| includeUVIndex                  | Create a channel for UV index (default: true) |
| includeUVIndexClearSky          | Create a channel for UV index if there was no cloud (default: false) |

Additional details on the various possible channel values are available in Open Meteo's [documentation](https://open-meteo.com/en/docs)

Any change to the parameters will recreate channels and channel groups.

## Channels

### Hourly forecast

The channels are placed in groups named `forecastHourly` for [time series support](#persisting-time-series), and  `forecastHours01` to `forecastHours384` for split channels support.

| Channel ID                 | Item Type            | Description |
|----------------------------|----------------------|-----------------------------------|
| temperature                | Number:Temperature   |  Forecast outdoor temperature |
| humidity                   | Number:Dimensionless |  Forecast atmospheric relative humidity |
| dew-point                  | Number:Temperature   |  Forecasted dew-point temperature. |
| apparent-temperature       | Number:Temperature   |  Forecast apparent temperature. |
| pressure                   | Number:Pressure      |  Forecast barometric surface pressure |
| cloudiness                 | Number:Dimensionless |  Forecast cloudiness. |
| wind-speed                 | Number:Speed         |  Forecast wind speed |
| wind-direction             | Number:Angle         |  Forecast wind direction |
| gust-speed                 | Number:Speed         |  Forecast gust speed. |
| shortwave-radiation        | Number:Intensity     |  Shortwave solar radiation as average of the preceding hour. |
| direct-radiation           | Number:Intensity     |  Direct solar radiation as average of the preceding hour. |
| direct-normal-irradiance   | Number:Intensity     |  Direct solar irradiance as average of the preceding hour. |
| diffuse-radiation          | Number:Intensity     |  Diffuse solar radiation as average of the preceding hour.  |
| vapour-pressure-deficit    | Number:Pressure      | For high VPD (>1.6), water transpiration of plants increases. For low VPD (<0.4), transpiration decreases |
| cape                       | Number               |  Convective available potential energy |
| evapotranspiration         | Number:Length        |  Evapotranspiration from land surface and plants that weather models assumes for this location. | |
| et0-fao-evapotranspiration | Number:Length        |  Based on FAO-56 Penman-Monteith equations ET₀ is calculated from temperature, wind speed, humidity and solar radiation. |
| precipitation                  | Number:Length        |  Total precipitation (rain, showers, snow) sum of the preceding hour |
| snow                           | Number:Length        |  Snow volume of the last hour. |
| precipitation-probability      | Number:Dimensionless |  Forecast precipitation probability. |
| rain                           | Number:Length        |  Rain volume of the last hour. |
| showers                        | Number:Length        |  Showers from convective precipitation from the preceding hour |
| weather-code                   | Number               |  Weather condition as a numeric code. Follow WMO weather interpretation codes.  |
| snow-depth                     | Number:Length        | Snow depth on the ground |
| freezing-level-height          | Number:Length        | Altitude above sea level of the 0°C level |
| visibility                     | Number:Length        |  Current visibility. |
| is-day                         | Switch               |  Active if daylight, inactive at night |

### Daily forecast

The channels are placed in groups named `forecastDaily` for [time series support](#persisting-time-series), and  `forecastDayToday`, `forecastDayTomorrow`, `forecastDay02` to `forecastDay16` for split channels support.


| Channel ID                     | Item Type            | Description |
|--------------------------------|----------------------|-----------------------------------|
| temperature-min                | Number:Temperature   |  Forecast minimum outdoor temperature |
| temperature-max                | Number:Temperature   |  Forecast maximum outdoor temperature |
| apparent-temperature-min       | Number:Temperature   |  Forecast minimum apparent temperature. |
| apparent-temperature-max       | Number:Temperature   |  Forecast maximum apparent temperature. |
| precipitation-sum              | Number:Length        |  Total precipitation (rain, showers, snow) for the day |
| rain-sum                       | Number:Length        |  Rain volume for the day |
| snow-sum                       | Number:Length        |  Snow volume for the day |
| showers-sum                    | Number:Length        |  Showers volume for the day |
| precipitation-hours            | Number               |  Number of hours with precipitation for the day |
| precipitation-probability-min  | Number:Dimensionless |  Forecast minimum precipitation probability. |
| precipitation-probability-max  | Number:Dimensionless |  Forecast maximum precipitation probability. |
| precipitation-probability-mean | Number:Dimensionless |  Forecast mean precipitation probability. |
| weather-code                   | Number               |  Weather condition as a numeric code. Follow WMO weather interpretation codes.  |
| sunrise                        | DateTime             |  Sunrise time |
| sunset                         | DateTime             |  Sunset time |
| sunshine-duration              | Number:Time          |  The number of seconds of sunshine per day, always less than daylight duration due to dawn and dusk. |
| daylight-duration              | Number:Time          |  Number of seconds of daylight per day |
| wind-speed-max                 | Number:Speed         |  Forecast maximum wind speed |
| gust-speed-max                 | Number:Speed         |  Forecast maximum gust speed. |
| wind-direction-dominant        | Number:Angle         |  Forecast dominant wind direction |
| shortwave-radiation-max        | Number:Intensity     |  Shortwave solar radiation as a maximum for the day. |
| et0-fao-evapotranspiration     | Number:Length        |  Based on FAO-56 Penman-Monteith equations ET₀ is calculated from temperature, wind speed, humidity and solar radiation. |
| uv-index                       | Number               |  Daily maximum in UV Index starting from 0 |
| uv-index-clear-sky             | Number               |  Daily maximum in UV Index starting from 0 assuming cloud free conditions |

## Persisting Time Series

The binding offers support for persisting forecast values.
The recommended persistence strategy is `forecast`, as it ensures a clean history without redundancy.

### Configuration

Make sure you have a persistence service installed and ready for use.

To configure persisting forecast data, create and link Items to those channels with time series support (as usual).
Next, enable persistence for these Items using the `forecast` persistence strategy.
Finally, open the UI, search for one of the newly created Items, open the analyzer and select a future time range.

To access forecast data stored in persistence from scripts and rules, use the [Persistence Extensions]({{base}}/configuration/persistence.html#persistence-extensions-in-scripts-and-rules).
