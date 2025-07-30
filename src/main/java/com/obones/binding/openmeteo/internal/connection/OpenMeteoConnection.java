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
        CLOUD_COVER_HIGH,
        CLOUD_COVER_MID,
        CLOUD_COVER_LOW,
        WIND_SPEED,
        WING_DIRECTION,
        GUST_SPEED,
        SHORTWAVE_RADIATION,
        DIRECT_RADIATION,
        DIRECT_NORMAL_IRRADIANCE,
        DIFFUSE_RADIATION,
        GLOBAL_TILTED_IRRADIANCE,
        TERRESTRIAL_SOLAR_RADIATION,
        INSTANT_SHORTWAVE_RADIATION,
        INSTANT_DIRECT_RADIATION,
        INSTANT_DIRECT_NORMAL_IRRADIANCE,
        INSTANT_DIFFUSE_RADIATION,
        INSTANT_GLOBAL_TILTED_IRRADIANCE,
        INSTANT_TERRESTRIAL_SOLAR_RADIATION,
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
            @Nullable Integer minutely15Steps, @Nullable Double panelTilt, @Nullable Double panelAzimuth,
            @Nullable Integer pastHours, @Nullable Integer pastDays, @Nullable Integer pastMinutely15Steps);

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
            @Nullable Integer hourlyHours, boolean current, @Nullable Integer pastHours);

    enum MarineForecastValue {
        WAVE_HEIGHT,
        WIND_WAVE_HEIGHT,
        SWELL_WAVE_HEIGHT,
        SECONDARY_SWELL_WAVE_HEIGHT,
        TERTIARY_SWELL_WAVE_HEIGHT,
        WAVE_DIRECTION,
        WIND_WAVE_DIRECTION,
        SWELL_WAVE_DIRECTION,
        SECONDARY_SWELL_WAVE_DIRECTION,
        TERTIARY_SWELL_WAVE_DIRECTION,
        WAVE_PERIOD,
        WIND_WAVE_PERIOD,
        SWELL_WAVE_PERIOD,
        SECONDARY_SWELL_WAVE_PERIOD,
        TERTIARY_SWELL_WAVE_PERIOD,
        WIND_WAVE_PEAK_PERIOD,
        SWELL_WAVE_PEAK_PERIOD,
        OCEAN_CURRENT_VELOCITY,
        OCEAN_CURRENT_DIRECTION,
        SEA_SURFACE_TEMPERATURE,
        SEA_LEVEL_HEIGHT_MSL,
        INVERT_BAROMETER_HEIGHT
    }

    WeatherApiResponse getMarineForecast(PointType location, EnumSet<MarineForecastValue> marineForecastValues,
            @Nullable Integer hourlyHours, @Nullable Integer dailyDays, boolean current, //
            @Nullable Integer pastHours, @Nullable Integer pastDays);
}
