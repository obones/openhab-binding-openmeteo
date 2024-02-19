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
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.RawType;

import com.openmeteo.sdk.WeatherApiResponse;

/**
 * The {@link OpenMeteoHttpConnection} represents an HTTP connection to the OpenMeteo API and provides forecast answers
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class OpenMeteoHttpConnection implements OpenMeteoConnection {

    private String getForecastValueFieldName(ForecastValue forecastValue) {
        switch (forecastValue) {
            case TEMPERATURE:
                return "temperature_2m";
            case HUMIDITY:
                return "relative_humidity_2m";
            case PRESSURE:
                return "surface_pressure";
            case WIND_SPEED:
                return "wind_speed_10m";
            case WING_DIRECTION:
                return "wind_direction_10m";
            default:
                return "";
        }
    }

    public WeatherApiResponse getForecast(String baseURI, String APIKey, PointType point,
            EnumSet<ForecastValue> forecastValues) {
        UriBuilder builder = UriBuilder.fromPath(baseURI).path("forecast") //
                .queryParam("format", "flatbuffers") //
                .queryParam("latitude", point.getLatitude()) //
                .queryParam("longitude", point.getLongitude());

        if (point.getAltitude().longValue() != 0)
            builder.queryParam("elevation", point.getAltitude());

        ArrayList<String> requiredFields = new ArrayList<>();
        for (ForecastValue forecastValue : forecastValues)
            requiredFields.add(getForecastValueFieldName(forecastValue));

        builder.queryParam("hourly", String.join(",", requiredFields));

        String url = builder.build().toString();

        RawType data = HttpUtil.downloadData(url, null, false, 0);

        ByteBuffer buffer = ByteBuffer.wrap(data.getBytes()).order(ByteOrder.LITTLE_ENDIAN);

        // first 4 bytes are the buffer length and must be ignored
        return WeatherApiResponse.getRootAsWeatherApiResponse(buffer.position(4));
    }
}
