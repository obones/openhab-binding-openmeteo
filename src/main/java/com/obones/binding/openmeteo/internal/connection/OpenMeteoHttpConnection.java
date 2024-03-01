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
        }
        return "";
    }

    public WeatherApiResponse getForecast(PointType location, EnumSet<ForecastValue> forecastValues,
            @Nullable Integer hourlyHours, @Nullable Integer dailyDays) {

        if (hourlyHours == null && dailyDays == null) {
            logger.warn("No point in getting a forecast if no elements are required");
            return new WeatherApiResponse();
        }

        UriBuilder builder = UriBuilder.fromPath(baseURI).path("forecast") //
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

        ArrayList<String> requiredFields = new ArrayList<>();
        for (ForecastValue forecastValue : forecastValues)
            requiredFields.add(getForecastValueFieldName(forecastValue));

        if (hourlyHours != null) {
            builder.queryParam("forecast_hours", hourlyHours);
            builder.queryParam("hourly", String.join(",", requiredFields));
        }

        if (dailyDays != null) {
            builder.queryParam("forecast_days", dailyDays);
            builder.queryParam("daily", String.join(",", requiredFields));
        }

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
}
