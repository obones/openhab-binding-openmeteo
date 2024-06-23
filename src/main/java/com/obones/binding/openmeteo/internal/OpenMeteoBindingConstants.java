/**
 * Copyright (c) 2023-2024 Olivier Sannier
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, 
 * you can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package com.obones.binding.openmeteo.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link OpenMeteoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 * <P>
 * This class contains the Thing identifications.
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class OpenMeteoBindingConstants {
    /** Basic binding identification. */
    public static final String BINDING_ID = "openmeteo";

    /**
     * The Thing identification of the <B>OpenMeteo</B> bridge.
     */
    private static final String THING_OPENMETEO_BRIDGE = "openmeteo";
    /**
     * The Thing identification of a forecast defined on the <B>OpenMeteo</B> bridge.
     */
    private static final String THING_OPENMETEO_FORECAST = "forecast";
    /**
     * The Thing identification of an air quality indicators defined on the <B>OpenMeteo</B> bridge.
     */
    private static final String THING_OPENMETEO_AIR_QUALITY = "air-quality";

    // Channel group type ids
    private static final String CHANNEL_GROUP_TYPE_ID_HOURLY_TIME_SERIES = "hourlyTimeSeries";
    private static final String CHANNEL_GROUP_TYPE_ID_HOURLY = "hourly";
    private static final String CHANNEL_GROUP_TYPE_ID_DAILY_TIME_SERIES = "dailyTimeSeries";
    private static final String CHANNEL_GROUP_TYPE_ID_DAILY = "daily";
    private static final String CHANNEL_GROUP_TYPE_ID_CURRENT = "current";
    private static final String CHANNEL_GROUP_TYPE_ID_MINUTELY_15 = "minutely15";

    // Discovered things id
    public static final String SYSTEM_LOCATION_THING_ID = "system";

    // List of all Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, THING_OPENMETEO_BRIDGE);

    // List of all Thing Type UIDs beyond the bridge(s)
    public static final ThingTypeUID THING_TYPE_OPENMETEO_FORECAST = new ThingTypeUID(BINDING_ID,
            THING_OPENMETEO_FORECAST);
    public static final ThingTypeUID THING_TYPE_OPENMETEO_AIR_QUALITY = new ThingTypeUID(BINDING_ID,
            THING_OPENMETEO_AIR_QUALITY);

    // List of all Channel Group Type UIDs
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_HOURLY_TIME_SERIES = new ChannelGroupTypeUID(BINDING_ID,
            CHANNEL_GROUP_TYPE_ID_HOURLY_TIME_SERIES);
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_HOURLY = new ChannelGroupTypeUID(BINDING_ID,
            CHANNEL_GROUP_TYPE_ID_HOURLY);
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_DAILY_TIME_SERIES = new ChannelGroupTypeUID(BINDING_ID,
            CHANNEL_GROUP_TYPE_ID_DAILY_TIME_SERIES);
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_DAILY = new ChannelGroupTypeUID(BINDING_ID,
            CHANNEL_GROUP_TYPE_ID_DAILY);
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_CURRENT = new ChannelGroupTypeUID(BINDING_ID,
            CHANNEL_GROUP_TYPE_ID_CURRENT);
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_MINUTELY_15 = new ChannelGroupTypeUID(BINDING_ID,
            CHANNEL_GROUP_TYPE_ID_MINUTELY_15);

    // Definitions of different set of Things
    public static final Set<ThingTypeUID> SUPPORTED_THINGS_BRIDGE = new HashSet<>(Arrays.asList(THING_TYPE_BRIDGE));
    public static final Set<ThingTypeUID> SUPPORTED_THINGS_ITEMS = new HashSet<>(
            Arrays.asList(THING_TYPE_OPENMETEO_FORECAST, THING_TYPE_OPENMETEO_AIR_QUALITY));

    // List of all bridge channel ids

    /** Channel/Property identifier describing the current Bridge State. */
    public static final String PROPERTY_BRIDGE_API_VERSION = "apiVersion";

    // Thing properties
    public static final String PROPERTY_THING_LOCATION = "location";

    // List of all forecast channel group ids
    public static final String CHANNEL_GROUP_HOURLY_TIME_SERIES = "forecastHourly";
    public static final String CHANNEL_GROUP_HOURLY_PREFIX = "forecastHours";
    public static final String CHANNEL_GROUP_DAILY_TIME_SERIES = "forecastDaily";
    public static final String CHANNEL_GROUP_DAILY_TODAY = "forecastToday";
    public static final String CHANNEL_GROUP_DAILY_TOMORROW = "forecastTomorrow";
    public static final String CHANNEL_GROUP_DAILY_PREFIX = "forecastDay";
    public static final String CHANNEL_GROUP_CURRENT = "current";
    public static final String CHANNEL_GROUP_MINUTELY_15 = "minutely15";

    // List of all forecast channel/property ids
    public static final String CHANNEL_FORECAST_TEMPERATURE = "temperature";
    public static final String CHANNEL_FORECAST_TEMPERATURE_MIN = "temperature-min";
    public static final String CHANNEL_FORECAST_TEMPERATURE_MAX = "temperature-max";
    public static final String CHANNEL_FORECAST_HUMIDITY = "humidity";
    public static final String CHANNEL_FORECAST_DEW_POINT = "dew-point";
    public static final String CHANNEL_FORECAST_APPARENT_TEMPERATURE = "apparent-temperature";
    public static final String CHANNEL_FORECAST_APPARENT_TEMPERATURE_MIN = "apparent-temperature-min";
    public static final String CHANNEL_FORECAST_APPARENT_TEMPERATURE_MAX = "apparent-temperature-max";
    public static final String CHANNEL_FORECAST_PRESSURE = "pressure";
    public static final String CHANNEL_FORECAST_CLOUDINESS = "cloudiness";
    public static final String CHANNEL_FORECAST_WIND_SPEED = "wind-speed";
    public static final String CHANNEL_FORECAST_WIND_DIRECTION = "wind-direction";
    public static final String CHANNEL_FORECAST_GUST_SPEED = "gust-speed";
    public static final String CHANNEL_FORECAST_SHORTWAVE_RADIATION = "shortwave-radiation";
    public static final String CHANNEL_FORECAST_DIRECT_RADIATION = "direct-radiation";
    public static final String CHANNEL_FORECAST_DIRECT_NORMAL_IRRADIANCE = "direct-normal-irradiance";
    public static final String CHANNEL_FORECAST_DIFFUSE_RADIATION = "diffuse-radiation";
    public static final String CHANNEL_FORECAST_VAPOUR_PRESSURE_DEFICIT = "vapour-pressure-deficit";
    public static final String CHANNEL_FORECAST_CAPE = "cape";
    public static final String CHANNEL_FORECAST_EVAPOTRANSPIRATION = "evapotranspiration";
    public static final String CHANNEL_FORECAST_ET0_EVAPOTRANSPIRATION = "et0-fao-evapotranspiration";
    public static final String CHANNEL_FORECAST_PRECIPITATION = "precipitation";
    public static final String CHANNEL_FORECAST_PRECIPITATION_SUM = "precipitation-sum";
    public static final String CHANNEL_FORECAST_PRECIPITATION_HOURS = "precipitation-hours";
    public static final String CHANNEL_FORECAST_SNOW = "snow";
    public static final String CHANNEL_FORECAST_SNOW_SUM = "snow-sum";
    public static final String CHANNEL_FORECAST_PRECIPITATION_PROBABILITY = "precipitation-probability";
    public static final String CHANNEL_FORECAST_PRECIPITATION_PROBABILITY_MIN = "precipitation-probability-min";
    public static final String CHANNEL_FORECAST_PRECIPITATION_PROBABILITY_MAX = "precipitation-probability-max";
    public static final String CHANNEL_FORECAST_PRECIPITATION_PROBABILITY_MEAN = "precipitation-probability-mean";
    public static final String CHANNEL_FORECAST_RAIN = "rain";
    public static final String CHANNEL_FORECAST_RAIN_SUM = "rain-sum";
    public static final String CHANNEL_FORECAST_SHOWERS = "showers";
    public static final String CHANNEL_FORECAST_SHOWERS_SUM = "showers-sum";
    public static final String CHANNEL_FORECAST_WEATHER_CODE = "weather-code";
    public static final String CHANNEL_FORECAST_SNOW_DEPTH = "snow-depth";
    public static final String CHANNEL_FORECAST_FREEZING_LEVEL_HEIGHT = "freezing-level-height";
    public static final String CHANNEL_FORECAST_VISIBILITY = "visibility";
    public static final String CHANNEL_FORECAST_IS_DAY = "is-day";
    public static final String CHANNEL_FORECAST_SUNRISE = "sunrise";
    public static final String CHANNEL_FORECAST_SUNSET = "sunset";
    public static final String CHANNEL_FORECAST_SUNSHINE_DURATION = "sunshine-duration";
    public static final String CHANNEL_FORECAST_DAYLIGHT_DURATION = "daylight-duration";
    public static final String CHANNEL_FORECAST_UV_INDEX = "uv-index";
    public static final String CHANNEL_FORECAST_UV_INDEX_CLEAR_SKY = "uv-index-clear-sky";

    // Forecast channel type ids
    public static final String CHANNEL_TYPE_DEW_POINT = "dew-point";
    public static final String CHANNEL_TYPE_APPARENT_TEMPERATURE = "apparent-temperature";
    public static final String CHANNEL_TYPE_CLOUDINESS = "cloudiness";
    public static final String CHANNEL_TYPE_GUST_SPEED = "gust-speed";
    public static final String CHANNEL_TYPE_SHORTWAVE_RADIATION = "shortwave-radiation";
    public static final String CHANNEL_TYPE_DIRECT_RADIATION = "direct-radiation";
    public static final String CHANNEL_TYPE_DIRECT_NORMAL_IRRADIANCE = "direct-normal-irradiance";
    public static final String CHANNEL_TYPE_DIFFUSE_RADIATION = "diffuse-radiation";
    public static final String CHANNEL_TYPE_VAPOUR_PRESSURE_DEFICIT = "vapour-pressure-deficit";
    public static final String CHANNEL_TYPE_CAPE = "cape";
    public static final String CHANNEL_TYPE_EVAPOTRANSPIRATION = "evapotranspiration";
    public static final String CHANNEL_TYPE_ET0_EVAPOTRANSPIRATION = "et0-fao-evapotranspiration";
    public static final String CHANNEL_TYPE_PRECIPITATION = "precipitation";
    public static final String CHANNEL_TYPE_PRECIPITATION_HOURS = "precipitation-hours";
    public static final String CHANNEL_TYPE_SNOW = "snow";
    public static final String CHANNEL_TYPE_PRECIPITATION_PROBABILITY = "precipitation-probability";
    public static final String CHANNEL_TYPE_RAIN = "rain";
    public static final String CHANNEL_TYPE_SHOWERS = "showers";
    public static final String CHANNEL_TYPE_WEATHER_CODE = "weather-code";
    public static final String CHANNEL_TYPE_SNOW_DEPTH = "snow-depth";
    public static final String CHANNEL_TYPE_FREEZING_LEVEL_HEIGHT = "freezing-level-height";
    public static final String CHANNEL_TYPE_VISIBILITY = "visibility";
    public static final String CHANNEL_TYPE_IS_DAY = "is-day";
    public static final String CHANNEL_TYPE_SUNRISE = "sunrise";
    public static final String CHANNEL_TYPE_SUNSET = "sunset";
    public static final String CHANNEL_TYPE_SUNSHINE_DURATION = "sunshine-duration";
    public static final String CHANNEL_TYPE_DAYLIGHT_DURATION = "daylight-duration";
    public static final String CHANNEL_TYPE_UV_INDEX = "uv-index";
    public static final String CHANNEL_TYPE_UV_INDEX_CLEAR_SKY = "uv-index-clear-sky";

    // Forecast channel type UIDs
    public static final ChannelTypeUID CHANNEL_TYPE_UID_DEW_POINT = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_DEW_POINT);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_APPARENT_TEMPERATURE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_APPARENT_TEMPERATURE);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_CLOUDINESS = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_CLOUDINESS);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_GUST_SPEED = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_GUST_SPEED);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_SHORTWAVE_RADIATION = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_SHORTWAVE_RADIATION);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_DIRECT_RADIATION = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_DIRECT_RADIATION);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_DIRECT_NORMAL_IRRADIANCE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_DIRECT_NORMAL_IRRADIANCE);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_DIFFUSE_RADIATION = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_DIFFUSE_RADIATION);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_VAPOUR_PRESSURE_DEFICIT = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_VAPOUR_PRESSURE_DEFICIT);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_CAPE = new ChannelTypeUID(BINDING_ID, //
            CHANNEL_TYPE_CAPE);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_EVAPOTRANSPIRATION = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_EVAPOTRANSPIRATION);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_ET0_EVAPOTRANSPIRATION = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_ET0_EVAPOTRANSPIRATION);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_PRECIPITATION = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_PRECIPITATION);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_PRECIPITATION_HOURS = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_PRECIPITATION_HOURS);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_SNOW = new ChannelTypeUID(BINDING_ID, //
            CHANNEL_TYPE_SNOW);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_PRECIPITATION_PROBABILITY = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_PRECIPITATION_PROBABILITY);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_RAIN = new ChannelTypeUID(BINDING_ID, //
            CHANNEL_TYPE_RAIN);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_SHOWERS = new ChannelTypeUID(BINDING_ID, //
            CHANNEL_TYPE_SHOWERS);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_WEATHER_CODE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_WEATHER_CODE);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_SNOW_DEPTH = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_SNOW_DEPTH);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_FREEZING_LEVEL_HEIGHT = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_FREEZING_LEVEL_HEIGHT);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_VISIBILITY = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_VISIBILITY);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_IS_DAY = new ChannelTypeUID(BINDING_ID, //
            CHANNEL_TYPE_IS_DAY);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_SUNRISE = new ChannelTypeUID(BINDING_ID, //
            CHANNEL_TYPE_SUNRISE);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_SUNSET = new ChannelTypeUID(BINDING_ID, //
            CHANNEL_TYPE_SUNSET);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_SUNSHINE_DURATION = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_SUNSHINE_DURATION);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_DAYLIGHT_DURATION = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_DAYLIGHT_DURATION);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_UV_INDEX = new ChannelTypeUID(BINDING_ID, //
            CHANNEL_TYPE_UV_INDEX);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_UV_INDEX_CLEAR_SKY = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_UV_INDEX_CLEAR_SKY);

    // List of all air quality channel/property ids
    public static final String CHANNEL_AIR_QUALITY_UV_INDEX = "uv-index";
    public static final String CHANNEL_AIR_QUALITY_UV_INDEX_CLEAR_SKY = "uv-index-clear-sky";
    public static final String CHANNEL_AIR_QUALITY_PARTICULATE_10 = "particulate-10";
    public static final String CHANNEL_AIR_QUALITY_PARTICULATE_2_5 = "particulate-2_5";
    public static final String CHANNEL_AIR_QUALITY_CARBON_MONOXIDE = "carbon-monoxide";
    public static final String CHANNEL_AIR_QUALITY_NITROGEN_DIOXIDE = "nitrogen-dioxide";
    public static final String CHANNEL_AIR_QUALITY_SULPHUR_DIOXIDE = "sulphur-dioxide";
    public static final String CHANNEL_AIR_QUALITY_OZONE = "ozone";
    public static final String CHANNEL_AIR_QUALITY_AEROSOL_OPTICAL_DEPTH = "aerosol-optical-depth";
    public static final String CHANNEL_AIR_QUALITY_DUST = "dust";
    public static final String CHANNEL_AIR_QUALITY_AMMONIA = "ammonia";
    public static final String CHANNEL_AIR_QUALITY_ALDER_POLLEN = "alder-pollen";
    public static final String CHANNEL_AIR_QUALITY_BIRCH_POLLEN = "birch-pollen";
    public static final String CHANNEL_AIR_QUALITY_MUGWORT_POLLEN = "mugwort-pollen";
    public static final String CHANNEL_AIR_QUALITY_GRASS_POLLEN = "grass-pollen";
    public static final String CHANNEL_AIR_QUALITY_OLIVE_POLLEN = "olive-pollen";
    public static final String CHANNEL_AIR_QUALITY_RAGWEED_POLLEN = "ragweed-pollen";
    public static final String CHANNEL_AIR_QUALITY_EUROPEAN_AQI = "european-aqi";
    public static final String CHANNEL_AIR_QUALITY_EUROPEAN_AQI_PM_2_5 = "european-aqi-pm2_5";
    public static final String CHANNEL_AIR_QUALITY_EUROPEAN_AQI_PM_10 = "european-aqi-pm10";
    public static final String CHANNEL_AIR_QUALITY_EUROPEAN_AQI_NITROGEN_DIOXIDE = "european-aqi-nitrogen-dioxide";
    public static final String CHANNEL_AIR_QUALITY_EUROPEAN_AQI_OZONE = "european-aqi-ozone";
    public static final String CHANNEL_AIR_QUALITY_EUROPEAN_AQI_SULPHUR_DIOXIDE = "european-aqi-sulphur-dioxide";
    public static final String CHANNEL_AIR_QUALITY_US_AQI = "us-aqi";
    public static final String CHANNEL_AIR_QUALITY_US_AQI_PM_2_5 = "us-aqi-pm2_5";
    public static final String CHANNEL_AIR_QUALITY_US_AQI_PM_10 = "us-aqi-pm10";
    public static final String CHANNEL_AIR_QUALITY_US_AQI_NITROGEN_DIOXIDE = "us-aqi-nitrogen-dioxide";
    public static final String CHANNEL_AIR_QUALITY_US_AQI_OZONE = "us-aqi-ozone";
    public static final String CHANNEL_AIR_QUALITY_US_AQI_SULPHUR_DIOXIDE = "us-aqi-sulphur-dioxide";
    public static final String CHANNEL_AIR_QUALITY_US_AQI_CARBON_MONOXIDE = "us-aqi-carbon-monoxide";

    // Air quality channel type ids
    public static final String CHANNEL_TYPE_PARTICULATE_CONCENTRATION = "particulate-concentration";
    public static final String CHANNEL_TYPE_GAZ_CONCENTRATION = "gaz-concentration";
    public static final String CHANNEL_TYPE_AEROSOL_OPTICAL_DEPTH = "aerosol-optical-depth";
    public static final String CHANNEL_TYPE_DUST = "dust";
    public static final String CHANNEL_TYPE_POLLEN = "pollen";
    public static final String CHANNEL_TYPE_EUROPEAN_AQI = "european-aqi";
    public static final String CHANNEL_TYPE_US_AQI = "us-aqi";

    // Air quality channel type UIDs
    public static final ChannelTypeUID CHANNEL_TYPE_UID_PARTICULATE_CONCENTRATION = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_PARTICULATE_CONCENTRATION);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_GAZ_CONCENTRATION = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_GAZ_CONCENTRATION);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_AEROSOL_OPTICAL_DEPTH = new ChannelTypeUID(BINDING_ID,
            CHANNEL_TYPE_AEROSOL_OPTICAL_DEPTH);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_DUST = new ChannelTypeUID(BINDING_ID, //
            CHANNEL_TYPE_DUST);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_POLLEN = new ChannelTypeUID(BINDING_ID, //
            CHANNEL_TYPE_POLLEN);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_EUROPEAN_AQI = new ChannelTypeUID(BINDING_ID, //
            CHANNEL_TYPE_EUROPEAN_AQI);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_US_AQI = new ChannelTypeUID(BINDING_ID, //
            CHANNEL_TYPE_US_AQI);
}
