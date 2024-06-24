/**
 * Copyright (c) 2023-2024 Olivier Sannier
 ** See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file,
 * you can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package com.obones.binding.openmeteo.internal.connection;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.RawType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeteo.sdk.WeatherApiResponse;

/**
 * The {@link OpenMeteoHttpConnection} represents an HTTP connection to the OpenMeteo API and provides forecast answers
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class OpenMeteoHttpConnection implements OpenMeteoConnection {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(OpenMeteoHttpConnection.class);

    private String baseURI;
    private String APIKey;

    public OpenMeteoHttpConnection(String baseURI, String APIKey) {
        this.baseURI = baseURI;
        this.APIKey = APIKey;
    }

    private String getForecastValueFieldName(ForecastValue forecastValue) {
        switch (forecastValue) {
            case TEMPERATURE:
                return "temperature_2m";
            case HUMIDITY:
                return "relative_humidity_2m";
            case DEW_POINT:
                return "dew_point_2m";
            case APPARENT_TEMPERATURE:
                return "apparent_temperature";
            case PRESSURE:
                return "surface_pressure";
            case CLOUDINESS:
                return "cloud_cover";
            case WIND_SPEED:
                return "wind_speed_10m";
            case WING_DIRECTION:
                return "wind_direction_10m";
            case GUST_SPEED:
                return "wind_gusts_10m";
            case SHORTWAVE_RADIATION:
                return "shortwave_radiation";
            case DIRECT_RADIATION:
                return "direct_radiation";
            case DIRECT_NORMAL_IRRADIANCE:
                return "direct_normal_irradiance";
            case DIFFUSE_RADIATION:
                return "diffuse_radiation";
            case VAPOUR_PRESSURE_DEFICIT:
                return "vapour_pressure_deficit";
            case CAPE:
                return "cape";
            case EVAPOTRANSPIRATION:
                return "evapotranspiration";
            case ET0_EVAPOTRANSPIRATION:
                return "et0_fao_evapotranspiration";
            case PRECIPITATION:
                return "precipitation";
            case SNOW:
                return "snowfall";
            case PRECIPITATION_PROBABILITY:
                return "precipitation_probability";
            case RAIN:
                return "rain";
            case SHOWERS:
                return "showers";
            case WEATHER_CODE:
                return "weather_code";
            case SNOW_DEPTH:
                return "snow_depth";
            case FREEZING_LEVEL_HEIGHT:
                return "freezing_level_height";
            case VISIBILITY:
                return "visibility";
            case IS_DAY:
                return "is_day";
            case SUNRISE:
                return "sunrise";
            case SUNSET:
                return "sunset";
            case SUNSHINE_DURATION:
                return "sunshine_duration";
            case DAYLIGHT_DURATION:
                return "daylight_duration";
            case UV_INDEX:
                return "uv_index_max";
            case UV_INDEX_CLEAR_SKY:
                return "uv_index_clear_sky_max";
        }
        return "";
    }

    private void addHourlyFields(ForecastValue forecastValue, ArrayList<String> fields) {
        switch (forecastValue) {
            // those fields are not available in the hourly forecast
            case SUNRISE:
            case SUNSET:
            case SUNSHINE_DURATION:
            case DAYLIGHT_DURATION:
            case UV_INDEX:
            case UV_INDEX_CLEAR_SKY:
                return;
            default:
                fields.add(getForecastValueFieldName(forecastValue));
        }
    }

    private void addDailyFields(ForecastValue forecastValue, ArrayList<String> fields) {
        String fieldName = getForecastValueFieldName(forecastValue);

        switch (forecastValue) {
            case PRECIPITATION_PROBABILITY:
                fields.add(fieldName + "_mean");
            case TEMPERATURE:
            case APPARENT_TEMPERATURE:
                fields.add(fieldName + "_min");
                fields.add(fieldName + "_max");
                break;
            case PRECIPITATION:
                fields.add(fieldName + "_hours");
            case RAIN:
            case SHOWERS:
            case SNOW:
            case SHORTWAVE_RADIATION:
                fields.add(fieldName + "_sum");
                break;
            case ET0_EVAPOTRANSPIRATION:
            case WEATHER_CODE:
            case SUNRISE:
            case SUNSET:
            case SUNSHINE_DURATION:
            case DAYLIGHT_DURATION:
            case UV_INDEX:
            case UV_INDEX_CLEAR_SKY:
                fields.add(fieldName);
                break;
            case WIND_SPEED:
            case GUST_SPEED:
                fields.add(fieldName + "_max");
                break;
            case WING_DIRECTION:
                fields.add(fieldName + "_dominant");
                break;
            default: // any other field is not supported in daily forecast
                break;
        }
    }

    private void addCurrentFields(ForecastValue forecastValue, ArrayList<String> fields) {
        switch (forecastValue) {
            // those fields are not available in the current conditions
            case DEW_POINT:
            case SHORTWAVE_RADIATION:
            case DIRECT_RADIATION:
            case DIRECT_NORMAL_IRRADIANCE:
            case DIFFUSE_RADIATION:
            case VAPOUR_PRESSURE_DEFICIT:
            case CAPE:
            case EVAPOTRANSPIRATION:
            case ET0_EVAPOTRANSPIRATION:
            case PRECIPITATION_PROBABILITY:
            case SNOW_DEPTH:
            case FREEZING_LEVEL_HEIGHT:
            case VISIBILITY:
            case SUNRISE:
            case SUNSET:
            case SUNSHINE_DURATION:
            case DAYLIGHT_DURATION:
            case UV_INDEX:
            case UV_INDEX_CLEAR_SKY:
                return;
            default:
                fields.add(getForecastValueFieldName(forecastValue));
        }
    }

    private void addMinutely15Fields(ForecastValue forecastValue, ArrayList<String> fields) {
        switch (forecastValue) {
            case TEMPERATURE:
            case HUMIDITY:
            case DEW_POINT:
            case APPARENT_TEMPERATURE:
            case SHORTWAVE_RADIATION:
            case DIRECT_RADIATION:
            case DIRECT_NORMAL_IRRADIANCE:
            case DIFFUSE_RADIATION:
            case SUNSHINE_DURATION:
            case PRECIPITATION:
            case SNOW:
            case RAIN:
            case SHOWERS:
            case SNOW_DEPTH:
            case FREEZING_LEVEL_HEIGHT:
            case CAPE:
            case WIND_SPEED:
            case WING_DIRECTION:
            case GUST_SPEED:
            case VISIBILITY:
            case WEATHER_CODE:
                fields.add(getForecastValueFieldName(forecastValue));
            default: // any other field is not supported in 15 minutely forecast
                break;
        }
    }

    private UriBuilder prepareUriBuilder(URI baseURI, String path, PointType location, //
            @Nullable Integer hourlyHours, ArrayList<String> requiredHourlyFields, //
            boolean current, ArrayList<String> requiredCurrentFields) {
        UriBuilder builder = UriBuilder.fromUri(baseURI).path(path) //
                .queryParam("format", "flatbuffers") //
                .queryParam("latitude", location.getLatitude()) //
                .queryParam("longitude", location.getLongitude()) //
                .queryParam("temperature_unit", "celsius") //
                .queryParam("wind_speed_unit", "ms") //
                .queryParam("precipitation_unit", "mm") //
                .queryParam("timezone", "UTC");

        if (location.getAltitude().longValue() != 0)
            builder.queryParam("elevation", location.getAltitude());

        if (!APIKey.isBlank())
            builder.queryParam("apikey", APIKey);

        if (hourlyHours != null) {
            builder.queryParam("forecast_hours", hourlyHours);
            builder.queryParam("hourly", String.join(",", requiredHourlyFields));
        }

        if (current) {
            builder.queryParam("current", String.join(",", requiredCurrentFields));
        }

        return builder;
    }

    private WeatherApiResponse getResponse(UriBuilder builder) {
        String url = builder.build().toString();

        logger.debug("Calling OpenMeteo on {}", url);
        RawType data = HttpUtil.downloadData(url, null, false, -1);
        if (data == null) {
            logger.warn("Data was null");
            return new WeatherApiResponse();
        }

        ByteBuffer buffer = ByteBuffer.wrap(data.getBytes()).order(ByteOrder.LITTLE_ENDIAN);

        // first 4 bytes are the buffer length and must be ignored
        return WeatherApiResponse.getRootAsWeatherApiResponse(buffer.position(4));
    }

    private @Nullable URI getUri() {
        try {
            return new URI(baseURI);
        } catch (URISyntaxException e) {
            logger.error("Invalid URI", e);
            return null;
        }
    }

    public WeatherApiResponse getForecast(PointType location, EnumSet<ForecastValue> forecastValues,
            @Nullable Integer hourlyHours, @Nullable Integer dailyDays, boolean current,
            @Nullable Integer minutely15Steps) {

        if (hourlyHours == null && dailyDays == null && !current && minutely15Steps == null) {
            logger.warn("No point in getting a forecast if no elements are required");
            return new WeatherApiResponse();
        }

        ArrayList<String> requiredHourlyFields = new ArrayList<>();
        ArrayList<String> requiredDailyFields = new ArrayList<>();
        ArrayList<String> requiredCurrentFields = new ArrayList<>();
        ArrayList<String> requiredMinutely15Fields = new ArrayList<>();
        for (ForecastValue forecastValue : forecastValues) {
            addHourlyFields(forecastValue, requiredHourlyFields);
            addDailyFields(forecastValue, requiredDailyFields);
            addCurrentFields(forecastValue, requiredCurrentFields);
            addMinutely15Fields(forecastValue, requiredMinutely15Fields);
        }

        @Nullable
        URI uri = getUri();
        if (uri == null)
            return new WeatherApiResponse();

        UriBuilder builder = prepareUriBuilder(uri, "forecast", location, hourlyHours, requiredHourlyFields, current,
                requiredCurrentFields);

        if (dailyDays != null) {
            builder.queryParam("forecast_days", dailyDays);
            builder.queryParam("daily", String.join(",", requiredDailyFields));
        }

        if (minutely15Steps != null) {
            builder.queryParam("forecast_minutely_15", minutely15Steps);
            builder.queryParam("minutely_15", String.join(",", requiredMinutely15Fields));
        }

        return getResponse(builder);
    }

    private String getAirQualityValueFieldName(AirQualityValue airQualityValue) {
        switch (airQualityValue) {
            case PARTICULATE_10:
                return "pm10";
            case PARTICULATE_2_5:
                return "pm2_5";
            case CARBON_MONOXIDE:
                return "carbon_monoxide";
            case NITROGEN_DIOXIDE:
                return "nitrogen_dioxide";
            case SULPHUR_DIOXIDE:
                return "sulphur_dioxide";
            case OZONE:
                return "ozone";
            case AEROSOL_OPTICAL_DEPTH:
                return "aerosol_optical_depth";
            case DUST:
                return "dust";
            case AMMONIA:
                return "ammonia";
            case ALDER_POLLEN:
                return "alder_pollen";
            case BIRCH_POLLEN:
                return "birch_pollen";
            case MUGWORT_POLLEN:
                return "mugword_pollen";
            case GRASS_POLLEN:
                return "grass_pollen";
            case OLIVE_POLLEN:
                return "olive_pollen";
            case RAGWEED_POLLEN:
                return "ragweed_pollen";
            case EUROPEAN_AQI:
                return "european_aqi_";
            case EUROPEAN_AQI_PM_2_5:
                return "european_aqi_pm2_5";
            case EUROPEAN_AQI_PM_10:
                return "european_aqi_pm10";
            case EUROPEAN_AQI_NITROGEN_DIOXIDE:
                return "european_aqi_nitrogen_dioxide";
            case EUROPEAN_AQI_OZONE:
                return "european_aqi_ozone";
            case EUROPEAN_AQI_SULPHUR_DIOXIDE:
                return "european_aqi_sulphur_dioxide";
            case US_AQI:
                return "us_aqi";
            case US_AQI_PM_2_5:
                return "us_aqi_pm2_5";
            case US_AQI_PM_10:
                return "us_aqi_pm10";
            case US_AQI_NITROGEN_DIOXIDE:
                return "us_aqi_nitrogen_dioxide";
            case US_AQI_OZONE:
                return "us_aqi_ozone";
            case US_AQI_SULPHUR_DIOXIDE:
                return "us_aqi_sulphur_dioxide";
            case US_AQI_CARBON_MONOXIDE:
                return "us_aqi_carbon_monoxide";
            case UV_INDEX:
                return "uv_index_max";
            case UV_INDEX_CLEAR_SKY:
                return "uv_index_clear_sky_max";
        }
        return "";
    }

    private void addHourlyFields(AirQualityValue airQualityValue, ArrayList<String> fields) {
        fields.add(getAirQualityValueFieldName(airQualityValue));
    }

    private void addCurrentFields(AirQualityValue airQualityValue, ArrayList<String> fields) {
        switch (airQualityValue) {
            // those fields are not available in the current conditions
            case EUROPEAN_AQI_PM_2_5:
            case EUROPEAN_AQI_PM_10:
            case EUROPEAN_AQI_NITROGEN_DIOXIDE:
            case EUROPEAN_AQI_OZONE:
            case EUROPEAN_AQI_SULPHUR_DIOXIDE:
            case US_AQI_PM_2_5:
            case US_AQI_PM_10:
            case US_AQI_NITROGEN_DIOXIDE:
            case US_AQI_OZONE:
            case US_AQI_SULPHUR_DIOXIDE:
            case US_AQI_CARBON_MONOXIDE:
                return;
            default:
                fields.add(getAirQualityValueFieldName(airQualityValue));
        }
    }

    public WeatherApiResponse getAirQuality(PointType location, EnumSet<AirQualityValue> airQualityValues,
            @Nullable Integer hourlyHours, boolean current) {
        if (hourlyHours == null && !current) {
            logger.warn("No point in getting an air quality report if no elements are required");
            return new WeatherApiResponse();
        }

        ArrayList<String> requiredHourlyFields = new ArrayList<>();
        ArrayList<String> requiredCurrentFields = new ArrayList<>();
        for (AirQualityValue airQualityValue : airQualityValues) {
            addHourlyFields(airQualityValue, requiredHourlyFields);
            addCurrentFields(airQualityValue, requiredCurrentFields);
        }

        @Nullable
        URI uri = getUri();
        if (uri == null)
            return new WeatherApiResponse();

        UriBuilder builder = prepareUriBuilder(uri, "air-quality", location, hourlyHours, requiredHourlyFields, current,
                requiredCurrentFields);

        builder.host("air-quality-" + uri.getHost());

        return getResponse(builder);
    }
}
