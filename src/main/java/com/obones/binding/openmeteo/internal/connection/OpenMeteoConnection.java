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

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.PointType;

import com.openmeteo.sdk.WeatherApiResponse;

/**
 * The {@link OpenMeteoConnection} represents a connection to the OpenMeteo API and provides forecast answers
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public interface OpenMeteoConnection {
    enum ForecastValue {
        TEMPERATURE,
        HUMIDITY,
        DEW_POINT,
        APPARENT_TEMPERATURE,
        PRESSURE,
        CLOUDINESS,
        WIND_SPEED,
        WING_DIRECTION,
        GUST_SPEED,
        SHORTWAVE_RADIATION,
        DIRECT_RADIATION,
        DIRECT_NORMAL_IRRADIANCE,
        DIFFUSE_RADIATION,
        VAPOUR_PRESSURE_DEFICIT,
        CAPE,
        EVAPOTRANSPIRATION,
        ET0_EVAPOTRANSPIRATION,
        PRECIPITATION,
        SNOW,
        PRECIPITATION_PROBABILITY,
        RAIN,
        SHOWERS,
        WEATHER_CODE,
        SNOW_DEPTH,
        FREEZING_LEVEL_HEIGHT,
        VISIBILITY,
        IS_DAY,
        SUNRISE,
        SUNSET,
        SUNSHINE_DURATION,
        DAYLIGHT_DURATION,
        UV_INDEX,
        UV_INDEX_CLEAR_SKY
    }

    WeatherApiResponse getForecast(PointType location, EnumSet<ForecastValue> forecastValues,
            @Nullable Integer hourlyHours, @Nullable Integer dailyDays, boolean current,
            @Nullable Integer minutely15Steps);

    enum AirQualityValue {
        UV_INDEX,
        UV_INDEX_CLEAR_SKY,
        PARTICULATE_10,
        PARTICULATE_2_5,
        CARBON_MONOXIDE,
        NITROGEN_DIOXIDE,
        SULPHUR_DIOXIDE,
        OZONE,
        AEROSOL_OPTICAL_DEPTH,
        DUST,
        AMMONIA,
        ALDER_POLLEN,
        BIRCH_POLLEN,
        MUGWORT_POLLEN,
        GRASS_POLLEN,
        OLIVE_POLLEN,
        RAGWEED_POLLEN,
        EUROPEAN_AQI,
        EUROPEAN_AQI_PM_2_5,
        EUROPEAN_AQI_PM_10,
        EUROPEAN_AQI_NITROGEN_DIOXIDE,
        EUROPEAN_AQI_OZONE,
        EUROPEAN_AQI_SULPHUR_DIOXIDE,
        US_AQI,
        US_AQI_PM_2_5,
        US_AQI_PM_10,
        US_AQI_NITROGEN_DIOXIDE,
        US_AQI_OZONE,
        US_AQI_SULPHUR_DIOXIDE,
        US_AQI_CARBON_MONOXIDE
    }

    WeatherApiResponse getAirQuality(PointType location, EnumSet<AirQualityValue> airQualityValues,
            @Nullable Integer hourlyHours, boolean current);
}
