# Open Meteo Binding

openHAB binding for [Open Meteo](https://open-meteo.com/) weather forecast service

- [Open Meteo Binding](#open-meteo-binding)
  - [Supported Things](#supported-things)
    - [Open Meteo bridge](#open-meteo-bridge)
    - [Weather forecast](#weather-forecast)
    - [Air quality forecast](#air-quality-forecast)
  - [Discovery](#discovery)
  - [Thing Configuration](#thing-configuration)
    - [Open Meteo bridge](#open-meteo-bridge-1)
    - [Weather forecast](#weather-forecast-1)
    - [Air quality forecast](#air-quality-forecast-1)
  - [Properties](#properties)
  - [Channels](#channels)
    - [Open Meteo bridge](#open-meteo-bridge-2)
    - [Hourly weather forecast](#hourly-weather-forecast)
    - [Daily weather forecast](#daily-weather-forecast)
    - [15 minutely weather forecast](#15-minutely-weather-forecast)
    - [Current weather conditions](#current-weather-conditions)
    - [Hourly air quality forecast](#hourly-air-quality-forecast)
    - [Current air quality conditions](#current-air-quality-conditions)
  - [Persisting Time Series](#persisting-time-series)
    - [Configuration](#configuration)
    - [Usage](#usage)
  - [Transformation profiles](#transformation-profiles)

## Supported Things

There are three supported things.

### Open Meteo bridge

The bridge represents the connection to the Open Meteo service.
If you are a paid user of this service, you will want to use the advanced properties to set your custom URL and API key.

### Weather forecast

The second thing `forecast` supports the following forecasts for a specific location: hourly, daily, 15-minutely. It also offers the current weather conditions, extrapolated from the 15-minutely forecast.

It requires coordinates of the location of your interest.

You can add as many `forecast` things for different locations to your setup as you like to observe.

Note: The first item in any forecast always include the current time period. For instance, an hourly weather forecast for 4 hours retrieved at 14:12 provides 4 values at the following times: 14:00, 15:00, 16:00, 17:00

### Air quality forecast

The third thing `air-quality` supports the following air-quality forecasts for a specific location: hourly, daily. It also offers the current air-quality conditions, extrapolated from the hourly forecast.

It requires coordinates of the location of your interest.

You can add as many `air-quality` things for different locations to your setup as you like to observe.

## Discovery

If a system location is set the following things will be automatically discovered for this location:

* a "System OpenMeteo weather forecast" (`forecast`) thing
* a "System OpenMeteo air quality report" (`air-quality`) thing

If the system location was to be changed, the background discovery would update the configuration of the things accordingly within one minute.

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
| hourlyTimeSeries | Whether to create a hourly time series channel group or not. Time series are new in 4.1 (default = true)          |
| hourlySplit      | Whether to create one channel group per future hour to accommodate widgets that are not capable of using time series. (default = false) |
| dailyDays        | Number of days for daily forecast (including todays forecast). Optional, the default value is 5 (min="1", max="16", step="1"). |
| dailyTimeSeries  | Whether to create a daily time series channel group or not. Time series are new in 4.1 (default = true)          |
| dailySplit       | Whether to create one channel group per future day to accommodate widgets that are not capable of using time series. (default = false) |
| current          | Whether to create a channel group for the current weather conditions. (default = false) |
| minutely15       | Whether to create a 15 minutely time series channel group or not. Time series are new in 4.1 (default = false) |
| minutely15Steps  | Number of 15 minutes steps to get the forecast for. Optional, the default value is 48 (min="1" max="288" step="1") |
| panelTilt        | The solar panel tilt (0° horizontal) |
| panelAzimuth     | The solar panel azimuth (0° S, -90° E, 90° W) |
| includeTimeStamp           | Create a channel in split groups for the forecast date time |
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
| includeGlobalTiltedIrradiance | Create a channel for global tilted irradiance, requires panelTilt and panelAzimuth to be defined (default: false) |
| includeTerrestrialSolarRadiation | Create a channel for terrestrial solar radiation (default: false) |
| includeInstantShortwaveRadiation  | Create a channel for instant shortwave radiation (default: false) |
| includeInstantDirectRadiation     | Create a channel for instant direct radiation (default: false) |
| includeInstantDirectNormalIrradiance | Create a channel for instant normal irradiance (default: false) |
| includeInstantDiffuseRadiation       | Create a channel for instant diffuse radiation (default: false) |
| includeInstantGlobalTiltedIrradiance | Create a channel for instant global tilted irradiance, requires panelTilt and panelAzimuth to be defined (default: false) |
| includeInstantTerrestrialSolarRadiation | Create a channel for instant terrestrial solar radiation (default: false) |
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
| includeIconId                   | Create a channel for icon id, compatible with OpenWeatherMap icon ids |
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

Any change to the parameters will recreate channels and channel groups with the same ids, thus not breaking any item link.

### Air quality forecast

| Parameter        | Description                                                                                                                    |
|------------------|--------------------------------------------------------------------------------------------------------------------------------|
| location         | Location of air quality in geographical coordinates (latitude/longitude/altitude). **Mandatory**                                   |
| airQualityIndicatorsAsString | Create Air Quality Indicators as string channels, showing an appreciation rather than a number (default: true) |
| airQualityIndicatorsAsNumber | Create Air Quality Indicators as number channels, see Open Meteo's [documentation](https://open-meteo.com/en/docs) for ranges  (default: false) |
| hourlyHours      | Number of hours for hourly forecast. Optional, the default value is 48 (min="1", max="168", step="1").                         |
| hourlyTimeSeries | Whether to create a hourly time series channel group or not. Time series are new in 4.1 (default: true)          |
| current          | Whether to create a channel group for the current air quality conditions. (default: false) |
| includePM10                       | Create a channel for Particulate Matter PM10 concentration (default: true) |
| includePM2_5                      | Create a channel for Particulate Matter PM2.5 concentration (default: true) |
| includeCarbonMonoxide             | Create a channel for Carbon Monoxide CO concentration (default: false) |
| includeNitrogenDioxide            | Create a channel for Nitrogen Dioxide NO2 concentration (default: false) |
| includeSulphurDioxide             | Create a channel for Sulphur Dioxide SO2 concentration (default: false) |
| includeOzone                      | Create a channel for Ozone O3 concentration (default: false) |
| includeAmmonia                    | Create a channel for Ammonia NH3 concentration (default: false) |
| includeAerosolOpticalDepth        | Create a channel for Aerosol Optical Depth (default: false) |
| includeDust                       | Create a channel for Dust concentration (default: false) |
| includeUVIndex                    | Create a channel for UV Index (default: false) |
| includeUVIndexClearSky            | Create a channel for UV index if there was no cloud (default: false) |
| includeAlderPollen                | Create a channel for Alder pollen concentration (default: false) |
| includeBirchPollen                | Create a channel for Birch pollen concentration (default: false) |
| includeGrassPollen                | Create a channel for Grass pollen concentration (default: false) |
| includeMugwortPollen              | Create a channel for Mugwort pollen concentration (default: false) |
| includeOlivePollen                | Create a channel for Oliver pollen concentration (default: true) |
| includeRagweedPollen              | Create a channel for Ragweed pollen concentration (default: false) |
| includeEuropeanAqi                | Create a channel for European Air Quality Indicator (default: true) |
| includeEuropeanAqiPM10            | Create a channel for European Air Quality PM10 Indicator (only for hourly forecast, default: false) |
| includeEuropeanAqiPM2_5           | Create a channel for European Air Quality PM2.5 Indicator (only for hourly forecast, default: false) |
| includeEuropeanAqiNitrogenDioxide | Create a channel for European Air Quality Nitrogen Dioxide Indicator (only for hourly forecast, default: false) |
| includeEuropeanAqiOzone           | Create a channel for European Air Quality Ozone Indicator (only for hourly forecast, default: false) |
| includeEuropeanAqiSulphurDioxide  | Create a channel for European Air Quality Sulphur Dioxide Indicator (only for hourly forecast, default: false) |
| includeUSAqi                      | Create a channel for US Air Quality Indicator (default: true) |
| includeUSAqiPM10                  | Create a channel for US Air Quality PM10 Indicator (only for hourly forecast, default: false) |
| includeUSAqiPM2_5                 | Create a channel for US Air Quality PM2.5 Indicator (only for hourly forecast, default: false) |
| includeUSAqiNitrogenDioxide       | Create a channel for US Air Quality Nitrogen Dioxide Indicator (only for hourly forecast, default: false) |
| includeUSAqiOzone                 | Create a channel for US Air Quality Ozone Indicator (only for hourly forecast, default: false) |
| includeUSAqiSulphurDioxide        | Create a channel for US Air Quality Sulphur Dioxide Indicator (only for hourly forecast, default: false) |
| includeUSAqiCarbonMonoxide        | Create a channel for US Air Quality Carbon Monoxide Indicator (only for hourly forecast, default: false) |

## Properties

All things but the bridge offer a `last-updated` property that contains the last time the thing retrieved its values from the bridge.

This is a string in ISO 8601 format, such as: 2024-07-03T14:17:37Z

## Channels

### Open Meteo bridge

The bridge provides the following channels.

| Channel ID                 | Item Type            | Description |
|----------------------------|----------------------|-----------------------------------|
| last-updated               | DateTime             |  Date and time when the bridge last triggered an update of all things connected to it |

### Hourly weather forecast

The channels are placed in groups named `forecastHourly` for [time series support](#persisting-time-series), and  `forecastHours01` to `forecastHours384` for split channels support.

| Channel ID                 | Item Type            | Description |
|----------------------------|----------------------|-----------------------------------|
| time-stamp                 | DateTime             |  The forecast date time (only for split channels) |
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
| global-tilted-irradiance   | Number:Intensity     |  Global tilted irradiance as average of the preceding hour.  |
| terrestrial-solar-radiation| Number:Intensity     |  Terrestrial solar radiation as average of the preceding hour.  |
| shortwave-radiation-instant        | Number:Intensity     |  Shortwave solar radiation at the indicated time. |
| direct-radiation-instant           | Number:Intensity     |  Direct solar radiation at the indicated time. |
| direct-normal-irradiance-instant   | Number:Intensity     |  Direct solar irradiance at the indicated time. |
| diffuse-radiation-instant          | Number:Intensity     |  Diffuse solar radiation at the indicated time.  |
| global-tilted-irradiance-instant   | Number:Intensity     |  Global tilted irradiance at the indicated time.  |
| terrestrial-solar-radiation-instant| Number:Intensity     |  Terrestrial solar radiation at the indicated time.  |
| vapour-pressure-deficit    | Number:Pressure      |  For high VPD (>1.6), water transpiration of plants increases. For low VPD (<0.4), transpiration decreases |
| cape                       | Number               |  Convective available potential energy |
| evapotranspiration         | Number:Length        |  Evapotranspiration from land surface and plants that weather models assumes for this location. | |
| et0-fao-evapotranspiration | Number:Length        |  Based on FAO-56 Penman-Monteith equations ET₀ is calculated from temperature, wind speed, humidity and solar radiation. |
| precipitation                  | Number:Length        |  Total precipitation (rain, showers, snow) sum of the preceding hour |
| snow                           | Number:Length        |  Snow volume of the last hour. |
| precipitation-probability      | Number:Dimensionless |  Forecast precipitation probability. |
| rain                           | Number:Length        |  Rain volume of the last hour. |
| showers                        | Number:Length        |  Showers from convective precipitation from the preceding hour |
| weather-code                   | Number               |  Weather condition as a numeric code. Follow WMO weather interpretation codes.  |
| icon-id                        | String               |  Weather condition as an Icon Id. Follows OpenWeatherMap icon Ids. |
| snow-depth                     | Number:Length        | Snow depth on the ground |
| freezing-level-height          | Number:Length        | Altitude above sea level of the 0°C level |
| visibility                     | Number:Length        |  Current visibility. |
| is-day                         | Switch               |  Active if daylight, inactive at night |

### Daily weather forecast

The channels are placed in groups named `forecastDaily` for [time series support](#persisting-time-series), and  `forecastDayToday`, `forecastDayTomorrow`, `forecastDay02` to `forecastDay16` for split channels support.


| Channel ID                     | Item Type            | Description |
|--------------------------------|----------------------|-----------------------------------|
| time-stamp                     | DateTime             |  The forecast date time (only for split channels) |
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
| icon-id                        | String               |  Weather condition as an Icon Id. Follows OpenWeatherMap icon Ids. |
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

### 15 minutely weather forecast

The channels are placed in a group named `forecastMinutely15` as a [time series](#persisting-time-series).

| Channel ID                 | Item Type            | Description |
|----------------------------|----------------------|-----------------------------------|
| temperature                | Number:Temperature   |  Forecast outdoor temperature |
| humidity                   | Number:Dimensionless |  Forecast atmospheric relative humidity |
| dew-point                  | Number:Temperature   |  Forecasted dew-point temperature. |
| apparent-temperature       | Number:Temperature   |  Forecast apparent temperature. |
| shortwave-radiation        | Number:Intensity     |  Shortwave solar radiation as average of the preceding 15 minutes. |
| direct-radiation           | Number:Intensity     |  Direct solar radiation as average of the preceding 15 minutes. |
| direct-normal-irradiance   | Number:Intensity     |  Direct solar irradiance as average of the preceding 15 minutes. |
| diffuse-radiation          | Number:Intensity     |  Diffuse solar radiation as average of the preceding 15 minutes.  |
| global-tilted-irradiance   | Number:Intensity     |  Global tilted irradiance as average of the preceding 15 minutes.  |
| terrestrial-solar-radiation| Number:Intensity     |  Terrestrial solar radiation as average of the preceding 15 minutes.  |
| shortwave-radiation-instant        | Number:Intensity     |  Shortwave solar radiation at the indicated time. |
| direct-radiation-instant           | Number:Intensity     |  Direct solar radiation at the indicated time. |
| direct-normal-irradiance-instant   | Number:Intensity     |  Direct solar irradiance at the indicated time. |
| diffuse-radiation-instant          | Number:Intensity     |  Diffuse solar radiation at the indicated time.  |
| global-tilted-irradiance-instant   | Number:Intensity     |  Global tilted irradiance at the indicated time.  |
| terrestrial-solar-radiation-instant| Number:Intensity     |  Terrestrial solar radiation at the indicated time.  |
| sunshine-duration          | Number:Time          |  The number of seconds of sunshine for the preceding 15 minutes. |
| precipitation              | Number:Length        |  Total precipitation (rain, showers, snow) sum of the preceding 15 minutes |
| snow                       | Number:Length        |  Snow volume of the last 15 minutes. |
| rain                       | Number:Length        |  Rain volume of the last 15 minutes. |
| showers                    | Number:Length        |  Showers from convective precipitation from the preceding 15 minutes |
| snow-depth                 | Number:Length        | Snow depth on the ground |
| freezing-level-height      | Number:Length        | Altitude above sea level of the 0°C level |
| cape                       | Number               |  Convective available potential energy |
| wind-speed                 | Number:Speed         |  Forecast wind speed |
| wind-direction             | Number:Angle         |  Forecast wind direction |
| gust-speed                 | Number:Speed         |  Forecast gust speed. |
| visibility                 | Number:Length        |  Current visibility. |
| weather-code               | Number               |  Weather condition as a numeric code. Follow WMO weather interpretation codes.  |
| icon-id                    | String               |  Weather condition as an Icon Id. Follows OpenWeatherMap icon Ids. |

### Current weather conditions

The channels are placed in a group named `current`.

| Channel ID                 | Item Type            | Description |
|----------------------------|----------------------|-----------------------------------|
| time-stamp                 | DateTime             |  The date time for which the current observations were computed |
| temperature                | Number:Temperature   |  Forecast outdoor temperature |
| humidity                   | Number:Dimensionless |  Forecast atmospheric relative humidity |
| apparent-temperature       | Number:Temperature   |  Forecast apparent temperature. |
| is-day                     | Switch               |  Active if daylight, inactive at night |
| precipitation              | Number:Length        |  Total precipitation (rain, showers, snow) sum of the preceding hour |
| rain                       | Number:Length        |  Rain volume of the last hour. |
| showers                    | Number:Length        |  Showers from convective precipitation from the preceding hour |
| snow                       | Number:Length        |  Snow volume of the last hour. |
| weather-code               | Number               |  Weather condition as a numeric code. Follow WMO weather interpretation codes.  |
| icon-id                    | String               |  Weather condition as an Icon Id. Follows OpenWeatherMap icon Ids. |
| cloudiness                 | Number:Dimensionless |  Forecast cloudiness. |
| pressure                   | Number:Pressure      |  Forecast barometric surface pressure |
| wind-speed                 | Number:Speed         |  Forecast wind speed |
| wind-direction             | Number:Angle         |  Forecast wind direction |
| gust-speed                 | Number:Speed         |  Forecast gust speed. |

### Hourly air quality forecast

The channels are placed in groups named `forecastHourly` with [time series support](#persisting-time-series)

| Channel ID                              | Item Type            | Description |
|-----------------------------------------|----------------------|-----------------------------------|
| uv-index                                | Number               | UV Index starting from 0 |
| uv-index-clear-sky                      | Number               | UV Index starting from 0 assuming cloud free conditions |
| particulate-10                          | Number:Density       | Particulate matter with diameter smaller than 10 µm close to surface (10 meter above ground) |
| particulate-2_5                         | Number:Density       | Particulate matter with diameter smaller than 2.5 µm close to surface (10 meter above ground) |
| carbon-monoxide                         | Number:Density       | Carbon Monoxide CO concentration close to surface (10 meter above ground) |
| nitrogen-dioxide                        | Number:Density       | Nitrogen Dioxide NO2 concentration close to surface (10 meter above ground) |
| sulphur-dioxide                         | Number:Density       | Sulphur Dioxide SO2 concentration close to surface (10 meter above ground) |
| ozone                                   | Number:Density       | Ozone O3 concentration close to surface (10 meter above ground) |
| aerosol-optical-depth                   | Number:Dimensionless | Aerosol optical depth at 550 nm of the entire atmosphere to indicate haze |
| dust                                    | Number:Density       | Saharan dust particles close to surface level (10 meter above ground). |
| ammonia                                 | Number:Density       | Ammonia NH3 concentration close to surface (10 meter above ground), Europe only |
| alder-pollen                            | Number               | Alder Pollen, Europe only |
| birch-pollen                            | Number               | Birch Pollen, Europe only |
| mugwort-pollen                          | Number               | Mugwort Pollen, Europe only |
| grass-pollen                            | Number               | Grass Pollen, Europe only |
| olive-pollen                            | Number               | Olive Pollen, Europe only |
| ragweed-pollen                          | Number               | Ragweed Pollen, Europe only |
| european-aqi                            | Number               | European AQI. Ranges from 0-20 (good), 20-40 (fair), 40-60 (moderate), 60-80 (poor), 80-100 (very poor) and exceeds 100 for extremely poor conditions. |
| european-aqi-pm2_5                      | Number               | European AQI PM2.5 concentration |
| european-aqi-pm10                       | Number               | European AQI PM10 concentration |
| european-aqi-nitrogen-dioxide           | Number               | European AQI Nitrogen Dioxide concentration |
| european-aqi-ozone                      | Number               | European AQI Ozone concentration |
| european-aqi-sulphur-dioxide            | Number               | European AQI Sulphur Dioxide concentration |
| european-aqi-as-string                  | String               | European AQI as a human readable string. Possible values are good, fair, moderate, poor, very poor and  extremely poor conditions. |
| european-aqi-as-string-pm2_5            | String               | European AQI PM2.5 concentration as a human readable string. Same values as European AQI |
| european-aqi-as-string-pm10             | String               | European AQI PM10 concentration as a human readable string. Same values as European AQI |
| european-aqi-as-string-nitrogen-dioxide | String               | European AQI Nitrogen concentration as a human readable string. Same values as European AQI |
| european-aqi-as-string-ozone            | String               | European AQI Ozone concentration as a human readable string. Same values as European AQI |
| european-aqi-as-string-sulphur-dioxide  | String               | European AQI Sulphur Dioxide concentration as a human readable string. Same values as European AQI |
| us-aqi                                  | Number               | United States AQI. Ranges from 0-50 (good), 51-100 (moderate), 101-150 (unhealthy for sensitive groups), 151-200 (unhealthy), 201-300 (very unhealthy) and 301-500 (hazardous). |
| us-aqi-pm2_5                            | Number               | United States AQI PM2.5 concentration |
| us-aqi-pm10                             | Number               | United States AQI PM10 concentration |
| us-aqi-nitrogen-dioxide                 | Number               | United States Nitrogen Dioxide concentration |
| us-aqi-ozone                            | Number               | United States Ozone concentration |
| us-aqi-sulphur-dioxide                  | Number               | United States Sulphur Dioxide concentration |
| us-aqi-carbon-monoxide                  | Number               | United States Carbon Monoxide concentration |
| us-aqi-as-string                        | String               | United States AQI as a human readable string. Possible values are good, moderate, unhealthy for sensitive groups, unhealthy, very unhealthy and hazardous. |
| us-aqi-as-string-pm2_5                  | String               | United States AQI PM2.5 concentration as a human readable string. Same values as United States AQI |
| us-aqi-as-string-pm10                   | String               | United States AQI PM10 concentration as a human readable string. Same values as United States AQI |
| us-aqi-as-string-nitrogen-dioxide       | String               | United States Nitrogen Dioxide concentration as a human readable string. Same values as United States AQI |
| us-aqi-as-string-ozone                  | String               | United States Ozone concentration as a human readable string. Same values as United States AQI |
| us-aqi-as-string-sulphur-dioxide        | String               | United States Sulphur Dioxide concentration as a human readable string. Same values as United States AQI |
| us-aqi-as-string-carbon-monoxide        | String               | United States Carbon Monoxide concentration as a human readable string. Same values as United States AQI |


### Current air quality conditions

The channels are placed in a group named `current`.

| Channel ID                              | Item Type            | Description |
|-----------------------------------------|----------------------|-----------------------------------|
| uv-index                                | Number               | UV Index starting from 0 |
| uv-index-clear-sky                      | Number               | UV Index starting from 0 assuming cloud free conditions |
| particulate-10                          | Number:Density       | Particulate matter with diameter smaller than 10 µm close to surface (10 meter above ground) |
| particulate-2_5                         | Number:Density       | Particulate matter with diameter smaller than 2.5 µm close to surface (10 meter above ground) |
| carbon-monoxide                         | Number:Density       | Carbon Monoxide CO concentration close to surface (10 meter above ground) |
| nitrogen-dioxide                        | Number:Density       | Nitrogen Dioxide NO2 concentration close to surface (10 meter above ground) |
| sulphur-dioxide                         | Number:Density       | Sulphur Dioxide SO2 concentration close to surface (10 meter above ground) |
| ozone                                   | Number:Density       | Ozone O3 concentration close to surface (10 meter above ground) |
| aerosol-optical-depth                   | Number:Dimensionless | Aerosol optical depth at 550 nm of the entire atmosphere to indicate haze |
| dust                                    | Number:Density       | Saharan dust particles close to surface level (10 meter above ground). |
| ammonia                                 | Number:Density       | Ammonia NH3 concentration close to surface (10 meter above ground), Europe only |
| alder-pollen                            | Number               | Alder Pollen, Europe only |
| birch-pollen                            | Number               | Birch Pollen, Europe only |
| mugwort-pollen                          | Number               | Mugwort Pollen, Europe only |
| grass-pollen                            | Number               | Grass Pollen, Europe only |
| olive-pollen                            | Number               | Olive Pollen, Europe only |
| ragweed-pollen                          | Number               | Ragweed Pollen, Europe only |
| european-aqi                            | Number               | European AQI. Ranges from 0-20 (good), 20-40 (fair), 40-60 (moderate), 60-80 (poor), 80-100 (very poor) and exceeds 100 for extremely poor conditions. |
| european-aqi-as-string                  | String               | European AQI as a human readable string. Possible values are good, fair, moderate, poor, very poor and  extremely poor conditions. |
| us-aqi                                  | Number               | United States AQI. Ranges from 0-50 (good), 51-100 (moderate), 101-150 (unhealthy for sensitive groups), 151-200 (unhealthy), 201-300 (very unhealthy) and 301-500 (hazardous). |
| us-aqi-as-string                        | String               | United States AQI as a human readable string. Possible values are good, moderate, unhealthy for sensitive groups, unhealthy, very unhealthy and hazardous. |

## Persisting Time Series

The binding offers support for persisting forecast values.
The recommended persistence strategy is `forecast`, as it ensures a clean history without redundancy.

### Configuration

Make sure you have a persistence service installed and ready for use.

To configure persisting forecast data, first create and link Items to those channels with time series support (as usual).
Next, enable persistence for these Items using the `forecast` persistence strategy:
* Settings, Add-ons Settings, Your persistence addon, Configure persistence
* Add configuration
* Select group/items from the forecast
* Select the `forecast` strategy
* Save your configuration

Finally, open the UI, search for one of the newly created Items, open the analyzer and select a future time range.

Please note that if you apply a strategy to some items, the “default strategy” will no longer apply and you’ll need to create a “catch all” strategy yourself, as discussed [here](https://community.openhab.org/t/default-persistence-strategy-is-not-applied-if-a-group-configuration-is-defined/155022/3 )

### Usage

To access forecast data stored in persistence from scripts and rules, use the [Persistence Extensions](https://www.openhab.org/docs/configuration/persistence.html#persistence-extensions-in-scripts-and-rules).

Here is an example in Javascript, extracted from the [ohab-weather-display](https://github.com/obones/ohab-weather-display/blob/master/doc/OpenHABRule.md) project:

```javascript
// retrieve the current time and use it to get our desired forecast boundaries
const now = time.toZDT();
const forecastStart = now.withHour(0).withMinute(0).withSecond(0);
const forecastEnd = forecastStart.plusDays(6);

// retrieve historic states from the time series item
const dailyWMOCodeItem = items.weather_forecast_WMO_code_Daily;
const dailyWMOCodeHistoricItems = dailyWMOCodeItem.history.getAllStatesBetween(forecastStart, forecastEnd);

// Declare a method that returns an numeric value from an historic item state.
// This avoids duplicating this code should you want to work on multiple items at once, like what is done
// in the ohab-weather-display project for instance.
function getNumericValue(historicItems, forecastIndex, unit)
{
  const historicItem = historicItems[forecastIndex];
  if (historicItem)
  {
    const quantityState = historicItem.quantityState;
    const numericState = historicItem.numericState;
    if (quantityState && unit)
    {
      return quantityState.toUnit(unit).float;
    }
    else if (numericState)
    {
      return historicItem.numericState;
    }
  }

  return NaN;
}

// loop over all historic states
for (let forecastIndex = 0; forecastIndex < dailyWMOCodeHistoricItems.length; forecastIndex++)
{
  const conditionCode = getNumericValue(dailyWMOCodeHistoricItems, forecastIndex);

  // ...
}
```

## Transformation profiles

Should you want to use the Air Quality Indicators both as a number and as a string, you may want to implement the mapping from one to the other manually.

You can either do it via scripts, or use one of the two transformation profiles provided by this binding.

They appear on item / channel links of the `Number` type.

For your convenience, you can choose whether you want human readable strings (Good, Fair, Very poor...) or options values, identical to the values output by the "as strings" AQI channels (GOOD, FAIR, VERY_POOR...)