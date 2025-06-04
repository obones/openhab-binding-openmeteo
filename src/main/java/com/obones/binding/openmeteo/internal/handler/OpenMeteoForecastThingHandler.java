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
package com.obones.binding.openmeteo.internal.handler;

import static com.obones.binding.openmeteo.internal.OpenMeteoBindingConstants.*;
import static org.openhab.core.thing.DefaultSystemChannelTypeProvider.*;

import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.config.OpenMeteoForecastThingConfiguration;
import com.obones.binding.openmeteo.internal.connection.OpenMeteoConnection;
import com.obones.binding.openmeteo.internal.connection.OpenMeteoConnection.ForecastValue;
import com.obones.binding.openmeteo.internal.utils.Localization;
import com.openmeteo.sdk.Variable;
import com.openmeteo.sdk.VariableWithValues;
import com.openmeteo.sdk.VariablesWithTime;
import com.openmeteo.sdk.WeatherApiResponse;

/***
 * The{@link OpenMeteoForecastThingHandler} is responsible for updating weather forecast related channels, which are
 * retrieved via {@link OpenMeteoBridgeHandler}.
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class OpenMeteoForecastThingHandler extends OpenMeteoBaseThingHandler {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(OpenMeteoBridgeHandler.class);

    private static final Pattern CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_HOURLY_PREFIX + "([0-9]*)");
    private static final Pattern CHANNEL_GROUP_DAILY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_DAILY_PREFIX + "([0-9]*)");

    public OpenMeteoForecastThingHandler(Thing thing, Localization localization,
            final TimeZoneProvider timeZoneProvider, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, localization, timeZoneProvider, channelTypeRegistry);
        logger.trace("OpenMeteoForecastHandler(thing={},localization={}) constructor called.", thing, localization);
    }

    @Override
    protected synchronized boolean validateConfig() {
        boolean result = super.validateConfig();

        if (result) {
            OpenMeteoForecastThingConfiguration config = getConfigAs(OpenMeteoForecastThingConfiguration.class);

            if (config.includeGlobalTiltedIrradiance) {
                if (config.panelAzimuth == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.conf-error-missing-panel-azimuth");
                    return false;
                }

                if (config.panelTilt == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.conf-error-missing-panel-tilt");
                    return false;
                }
            }
        }

        return result;
    }

    protected void initializeChannels(ThingHandlerCallback callback, ThingBuilder builder, ThingUID thingUID) {
        OpenMeteoForecastThingConfiguration config = getConfigAs(OpenMeteoForecastThingConfiguration.class);

        if (config.hourlyTimeSeries) {
            String labelSuffix = localization
                    .getText("channel-type.openmeteo.forecast.label-suffix.hourly.time-series");
            initializeHourlyGroupOptionalChannels(callback, builder, thingUID, config, CHANNEL_GROUP_HOURLY_TIME_SERIES,
                    labelSuffix, true);
        }

        if (config.hourlySplit) {
            DecimalFormat hourlyFormatter = new DecimalFormat("00");
            String labelSuffix = localization.getText("channel-type.openmeteo.forecast.label-suffix.hourly.split");

            for (int hour = 1; hour <= config.hourlyHours; hour++)
                initializeHourlyGroupOptionalChannels(callback, builder, thingUID, config,
                        CHANNEL_GROUP_HOURLY_PREFIX + hourlyFormatter.format(hour), String.format(labelSuffix, hour),
                        false);
        }

        if (config.dailyTimeSeries) {
            String labelSuffix = localization.getText("channel-type.openmeteo.forecast.label-suffix.daily.time-series");
            initializeDailyGroupOptionalChannels(callback, builder, thingUID, config, CHANNEL_GROUP_DAILY_TIME_SERIES,
                    labelSuffix, true);
        }

        if (config.dailySplit) {
            initializeDailyGroupOptionalChannels(callback, builder, thingUID, config, CHANNEL_GROUP_DAILY_TODAY,
                    localization.getText("channel-type.openmeteo.forecast.label-suffix.daily.today"), false);
            if (config.dailyDays >= 2)
                initializeDailyGroupOptionalChannels(callback, builder, thingUID, config, CHANNEL_GROUP_DAILY_TOMORROW,
                        localization.getText("channel-type.openmeteo.forecast.label-suffix.daily.tomorrow"), false);

            DecimalFormat dailyFormatter = new DecimalFormat("00");
            String labelSuffix = localization.getText("channel-type.openmeteo.forecast.label-suffix.daily.split");
            for (int day = 2; day < config.dailyDays; day++)
                initializeDailyGroupOptionalChannels(callback, builder, thingUID, config,
                        CHANNEL_GROUP_DAILY_PREFIX + dailyFormatter.format(day), String.format(labelSuffix, day),
                        false);
        }

        if (config.current) {
            initializeCurrentGroupOptionalChannels(callback, builder, thingUID, config);
        }

        if (config.minutely15) {
            initializeMinutely15GroupOptionalChannels(callback, builder, thingUID, config);
        }
    }

    protected ThingBuilder initializeHourlyGroupOptionalChannels(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, OpenMeteoForecastThingConfiguration config, String channelGroupId, String labelSuffix,
            boolean isTimeSeries) {

        Object[] labelArguments = { labelSuffix };

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_TIME_STAMP,
                CHANNEL_TYPE_UID_TIME_STAMP, config.includeTimeStamp && !isTimeSeries, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_TEMPERATURE,
                SYSTEM_CHANNEL_TYPE_UID_OUTDOOR_TEMPERATURE, config.includeTemperature, //
                "channel-type.openmeteo.forecast.temperature.label",
                "channel-type.openmeteo.forecast.temperature.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_HUMIDITY,
                SYSTEM_CHANNEL_TYPE_UID_ATMOSPHERIC_HUMIDITY, config.includeHumidity, //
                "channel-type.openmeteo.forecast.humidity.label",
                "channel-type.openmeteo.forecast.humidity.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_DEW_POINT,
                CHANNEL_TYPE_UID_DEW_POINT, config.includeDewPoint, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_APPARENT_TEMPERATURE,
                CHANNEL_TYPE_UID_APPARENT_TEMPERATURE, config.includeApparentTemperature, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_PRESSURE,
                SYSTEM_CHANNEL_TYPE_UID_BAROMETRIC_PRESSURE, config.includePressure, //
                "channel-type.openmeteo.forecast.pressure.label",
                "channel-type.openmeteo.forecast.pressure.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_CLOUDINESS,
                CHANNEL_TYPE_UID_CLOUDINESS, config.includeCloudiness, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_WIND_SPEED,
                SYSTEM_CHANNEL_TYPE_UID_WIND_SPEED, config.includeWindSpeed, //
                "channel-type.openmeteo.forecast.wind-speed.label",
                "channel-type.openmeteo.forecast.wind-speed.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_WIND_DIRECTION,
                SYSTEM_CHANNEL_TYPE_UID_WIND_DIRECTION, config.includeWindDirection, //
                "channel-type.openmeteo.forecast.wind-direction.label",
                "channel-type.openmeteo.forecast.wind-direction.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_GUST_SPEED,
                CHANNEL_TYPE_UID_GUST_SPEED, config.includeGustSpeed, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SHORTWAVE_RADIATION,
                CHANNEL_TYPE_UID_SHORTWAVE_RADIATION, config.includeShortwaveRadiation, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_DIRECT_RADIATION,
                CHANNEL_TYPE_UID_DIRECT_RADIATION, config.includeDirectRadiation, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_DIRECT_NORMAL_IRRADIANCE, CHANNEL_TYPE_UID_DIRECT_NORMAL_IRRADIANCE,
                config.includeDirectNormalIrradiance, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_DIFFUSE_RADIATION,
                CHANNEL_TYPE_UID_DIFFUSE_RADIATION, config.includeDiffuseRadiation, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_GLOBAL_TILTED_IRRADIANCE, CHANNEL_TYPE_UID_GLOBAL_TILTED_IRRADIANCE,
                config.includeGlobalTiltedIrradiance, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_TERRESTRIAL_SOLAR_RADIATION, CHANNEL_TYPE_UID_TERRESTRIAL_SOLAR_RADIATION,
                config.includeTerrestrialSolarRadiation, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_INSTANT_SHORTWAVE_RADIATION, CHANNEL_TYPE_UID_SHORTWAVE_RADIATION,
                config.includeInstantShortwaveRadiation, //
                "channel-type.openmeteo.forecast.shortwave-radiation-instant.label",
                "channel-type.openmeteo.forecast.shortwave-radiation-instant.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_INSTANT_DIRECT_RADIATION, CHANNEL_TYPE_UID_DIRECT_RADIATION,
                config.includeInstantDirectRadiation, //
                "channel-type.openmeteo.forecast.direct-radiation-instant.label",
                "channel-type.openmeteo.forecast.direct-radiation-instant.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_INSTANT_DIRECT_NORMAL_IRRADIANCE, CHANNEL_TYPE_UID_DIRECT_NORMAL_IRRADIANCE,
                config.includeInstantDirectNormalIrradiance, //
                "channel-type.openmeteo.forecast.direct-normal-irradiance-instant.label",
                "channel-type.openmeteo.forecast.direct-normal-irradiance-instant", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_INSTANT_DIFFUSE_RADIATION, CHANNEL_TYPE_UID_DIFFUSE_RADIATION,
                config.includeInstantDiffuseRadiation, //
                "channel-type.openmeteo.forecast.diffuse-radiation-instant.label",
                "channel-type.openmeteo.forecast.diffuse-radiation-instant.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_INSTANT_GLOBAL_TILTED_IRRADIANCE, CHANNEL_TYPE_UID_GLOBAL_TILTED_IRRADIANCE,
                config.includeInstantGlobalTiltedIrradiance, //
                "channel-type.openmeteo.forecast.global-tilted-irradiance-instant.label",
                "channel-type.openmeteo.forecast.global-tilted-irradiance-instant.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_INSTANT_TERRESTRIAL_SOLAR_RADIATION, CHANNEL_TYPE_UID_TERRESTRIAL_SOLAR_RADIATION,
                config.includeInstantTerrestrialSolarRadiation, //
                "channel-type.openmeteo.forecast.terrestrial-solar-radiation-instant.label",
                "channel-type.openmeteo.forecast.terrestrial-solar-radiation-instant.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_INSTANT_TERRESTRIAL_SOLAR_RADIATION, CHANNEL_TYPE_UID_TERRESTRIAL_SOLAR_RADIATION,
                config.includeInstantTerrestrialSolarRadiation, //
                "channel-type.openmeteo.forecast.shortwave-radiation-instant.label",
                "channel-type.openmeteo.forecast.shortwave-radiation-instant.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_VAPOUR_PRESSURE_DEFICIT,
                CHANNEL_TYPE_UID_VAPOUR_PRESSURE_DEFICIT, config.includeVapourPressureDeficit, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_CAPE,
                CHANNEL_TYPE_UID_CAPE, config.includeCape, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_EVAPOTRANSPIRATION,
                CHANNEL_TYPE_UID_EVAPOTRANSPIRATION, config.includeEvapotranspiration, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_ET0_EVAPOTRANSPIRATION,
                CHANNEL_TYPE_UID_ET0_EVAPOTRANSPIRATION, config.includeEt0FAOEvapotranspiration, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_PRECIPITATION,
                CHANNEL_TYPE_UID_PRECIPITATION, config.includePrecipitation, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SNOW,
                CHANNEL_TYPE_UID_SNOW, config.includeSnow, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_PRECIPITATION_PROBABILITY, CHANNEL_TYPE_UID_PRECIPITATION_PROBABILITY,
                config.includePrecipitationProbability, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_RAIN,
                CHANNEL_TYPE_UID_RAIN, config.includeRain, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SHOWERS,
                CHANNEL_TYPE_UID_SHOWERS, config.includeShowers, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_WEATHER_CODE,
                CHANNEL_TYPE_UID_WEATHER_CODE, config.includeWeatherCode, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_ICON_ID,
                CHANNEL_TYPE_UID_ICON_ID, config.includeIconId, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SNOW_DEPTH,
                CHANNEL_TYPE_UID_SNOW_DEPTH, config.includeSnowDepth, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_FREEZING_LEVEL_HEIGHT,
                CHANNEL_TYPE_UID_FREEZING_LEVEL_HEIGHT, config.includeFreezingLevelHeight, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_VISIBILITY,
                CHANNEL_TYPE_UID_VISIBILITY, config.includeVisibility, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_IS_DAY,
                CHANNEL_TYPE_UID_IS_DAY, config.includeIsDay, labelArguments);

        return builder;
    }

    protected ThingBuilder initializeDailyGroupOptionalChannels(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, OpenMeteoForecastThingConfiguration config, String channelGroupId, String labelSuffix,
            boolean isTimeSeries) {

        Object[] labelArguments = { labelSuffix };

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_TIME_STAMP,
                CHANNEL_TYPE_UID_TIME_STAMP, config.includeTimeStamp && !isTimeSeries, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_TEMPERATURE_MIN,
                SYSTEM_CHANNEL_TYPE_UID_OUTDOOR_TEMPERATURE, config.includeTemperature, //
                "channel-type.openmeteo.forecast.temperature-min.label",
                "channel-type.openmeteo.forecast.temperature-min.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_TEMPERATURE_MAX,
                SYSTEM_CHANNEL_TYPE_UID_OUTDOOR_TEMPERATURE, config.includeTemperature, //
                "channel-type.openmeteo.forecast.temperature-max.label",
                "channel-type.openmeteo.forecast.temperature-max.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_APPARENT_TEMPERATURE_MIN, CHANNEL_TYPE_UID_APPARENT_TEMPERATURE,
                config.includeApparentTemperature, //
                "channel-type.openmeteo.forecast.apparent-temperature-min.label",
                "channel-type.openmeteo.forecast.apparent-temperature-min.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_APPARENT_TEMPERATURE_MAX, CHANNEL_TYPE_UID_APPARENT_TEMPERATURE,
                config.includeApparentTemperature, //
                "channel-type.openmeteo.forecast.apparent-temperature-max.label",
                "channel-type.openmeteo.forecast.apparent-temperature-max.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_PRECIPITATION_SUM,
                CHANNEL_TYPE_UID_PRECIPITATION, config.includePrecipitation, //
                null, "channel-type.openmeteo.forecast.precipitation-sum.description", labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_RAIN_SUM,
                CHANNEL_TYPE_UID_RAIN, config.includeRain, //
                null, "channel-type.openmeteo.forecast.rain-sum.description", labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SNOW_SUM,
                CHANNEL_TYPE_UID_SNOW, config.includeSnow, //
                null, "channel-type.openmeteo.forecast.snow-sum.description", labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SHOWERS_SUM,
                CHANNEL_TYPE_UID_SHOWERS, config.includeShowers, //
                null, "channel-type.openmeteo.forecast.showers-sum.description", labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_PRECIPITATION_HOURS,
                CHANNEL_TYPE_UID_PRECIPITATION_HOURS, config.includePrecipitation, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_PRECIPITATION_PROBABILITY_MAX, CHANNEL_TYPE_UID_PRECIPITATION_PROBABILITY,
                config.includePrecipitationProbability, //
                "channel-type.openmeteo.forecast.precipitation-probability-max.label",
                "channel-type.openmeteo.forecast.precipitation-probability-max.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_PRECIPITATION_PROBABILITY_MIN, CHANNEL_TYPE_UID_PRECIPITATION_PROBABILITY,
                config.includePrecipitationProbability, //
                "channel-type.openmeteo.forecast.precipitation-probability-min.label",
                "channel-type.openmeteo.forecast.precipitation-probability-min.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_PRECIPITATION_PROBABILITY_MEAN, CHANNEL_TYPE_UID_PRECIPITATION_PROBABILITY,
                config.includePrecipitationProbability, //
                "channel-type.openmeteo.forecast.precipitation-probability-mean.label",
                "channel-type.openmeteo.forecast.precipitation-probability-mean.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_WEATHER_CODE,
                CHANNEL_TYPE_UID_WEATHER_CODE, config.includeWeatherCode, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_ICON_ID,
                CHANNEL_TYPE_UID_ICON_ID, config.includeIconId, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SUNRISE,
                CHANNEL_TYPE_UID_SUNRISE, config.includeSunrise, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SUNSET,
                CHANNEL_TYPE_UID_SUNSET, config.includeSunset, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SUNSHINE_DURATION,
                CHANNEL_TYPE_UID_SUNSHINE_DURATION, config.includeSunshineDuration, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_DAYLIGHT_DURATION,
                CHANNEL_TYPE_UID_DAYLIGHT_DURATION, config.includeDaylightDuration, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_WIND_SPEED,
                SYSTEM_CHANNEL_TYPE_UID_WIND_SPEED, config.includeWindSpeed, //
                "channel-type.openmeteo.forecast.wind-speed.label",
                "channel-type.openmeteo.forecast.wind-speed-max.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_GUST_SPEED,
                CHANNEL_TYPE_UID_GUST_SPEED, config.includeGustSpeed, //
                null, "channel-type.openmeteo.forecast.gust-speed-max.description", labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_WIND_DIRECTION,
                SYSTEM_CHANNEL_TYPE_UID_WIND_DIRECTION, config.includeWindDirection, //
                "channel-type.openmeteo.forecast.wind-direction.label",
                "channel-type.openmeteo.forecast.wind-direction-dominant.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SHORTWAVE_RADIATION,
                CHANNEL_TYPE_UID_SHORTWAVE_RADIATION, config.includeShortwaveRadiation, //
                null, "channel-type.openmeteo.forecast.shortwave-radiation-max.description", labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_ET0_EVAPOTRANSPIRATION,
                CHANNEL_TYPE_UID_ET0_EVAPOTRANSPIRATION, config.includeEt0FAOEvapotranspiration, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_UV_INDEX,
                CHANNEL_TYPE_UID_UV_INDEX, config.includeUVIndex, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_UV_INDEX_CLEAR_SKY,
                CHANNEL_TYPE_UID_UV_INDEX_CLEAR_SKY, config.includeUVIndexClearSky, labelArguments);

        return builder;
    }

    protected ThingBuilder initializeCurrentGroupOptionalChannels(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, OpenMeteoForecastThingConfiguration config) {

        String labelSuffix = localization.getText("channel-type.openmeteo.forecast.label-suffix.current");
        String channelGroupId = CHANNEL_GROUP_CURRENT;

        return initializeHourlyGroupOptionalChannels(callback, builder, thingUID, config, channelGroupId, labelSuffix,
                false);
    }

    private void initializeMinutely15GroupOptionalChannels(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, OpenMeteoForecastThingConfiguration config) {
        Object[] labelArguments = { localization.getText("channel-type.openmeteo.forecast.label-suffix.minutely15") };
        String channelGroupId = CHANNEL_GROUP_MINUTELY_15;

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_TEMPERATURE,
                SYSTEM_CHANNEL_TYPE_UID_OUTDOOR_TEMPERATURE, config.includeTemperature, //
                "channel-type.openmeteo.forecast.temperature.label",
                "channel-type.openmeteo.forecast.temperature.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_HUMIDITY,
                SYSTEM_CHANNEL_TYPE_UID_ATMOSPHERIC_HUMIDITY, config.includeHumidity, //
                "channel-type.openmeteo.forecast.humidity.label",
                "channel-type.openmeteo.forecast.humidity.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_DEW_POINT,
                CHANNEL_TYPE_UID_DEW_POINT, config.includeDewPoint, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_APPARENT_TEMPERATURE,
                CHANNEL_TYPE_UID_APPARENT_TEMPERATURE, config.includeApparentTemperature, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SHORTWAVE_RADIATION,
                CHANNEL_TYPE_UID_SHORTWAVE_RADIATION, config.includeShortwaveRadiation, //
                null, "channel-type.openmeteo.forecast.shortwave-radiation-fifteen-minutes.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_DIRECT_RADIATION,
                CHANNEL_TYPE_UID_DIRECT_RADIATION, config.includeDirectRadiation, //
                null, "channel-type.openmeteo.forecast.direct-radiation-fifteen-minutes.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_DIRECT_NORMAL_IRRADIANCE, CHANNEL_TYPE_UID_DIRECT_NORMAL_IRRADIANCE,
                config.includeDirectNormalIrradiance, //
                null, "channel-type.openmeteo.forecast.direct-normal-irradiance-fifteen-minutes.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_DIFFUSE_RADIATION,
                CHANNEL_TYPE_UID_DIFFUSE_RADIATION, config.includeDiffuseRadiation, //
                null, "channel-type.openmeteo.forecast.diffuse-radiation-fifteen-minutes.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_GLOBAL_TILTED_IRRADIANCE, CHANNEL_TYPE_UID_GLOBAL_TILTED_IRRADIANCE,
                config.includeGlobalTiltedIrradiance, //
                null, "channel-type.openmeteo.forecast.global-tilted-irradiance-fifteen-minutes.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_TERRESTRIAL_SOLAR_RADIATION, CHANNEL_TYPE_UID_TERRESTRIAL_SOLAR_RADIATION,
                config.includeTerrestrialSolarRadiation, //
                null, "channel-type.openmeteo.forecast.terrestrial-solar-radiation-fifteen-minutes.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_INSTANT_SHORTWAVE_RADIATION, CHANNEL_TYPE_UID_SHORTWAVE_RADIATION,
                config.includeInstantShortwaveRadiation, //
                "channel-type.openmeteo.forecast.shortwave-radiation-instant.label",
                "channel-type.openmeteo.forecast.shortwave-radiation-instant.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_INSTANT_DIRECT_RADIATION, CHANNEL_TYPE_UID_DIRECT_RADIATION,
                config.includeInstantDirectRadiation, //
                "channel-type.openmeteo.forecast.direct-radiation-instant.label",
                "channel-type.openmeteo.forecast.direct-radiation-instant.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_INSTANT_DIRECT_NORMAL_IRRADIANCE, CHANNEL_TYPE_UID_DIRECT_NORMAL_IRRADIANCE,
                config.includeInstantDirectNormalIrradiance, //
                "channel-type.openmeteo.forecast.direct-normal-irradiance-instant.label",
                "channel-type.openmeteo.forecast.direct-normal-irradiance-instant", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_INSTANT_DIFFUSE_RADIATION, CHANNEL_TYPE_UID_DIFFUSE_RADIATION,
                config.includeInstantDiffuseRadiation, //
                "channel-type.openmeteo.forecast.diffuse-radiation-instant.label",
                "channel-type.openmeteo.forecast.diffuse-radiation-instant.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_INSTANT_GLOBAL_TILTED_IRRADIANCE, CHANNEL_TYPE_UID_GLOBAL_TILTED_IRRADIANCE,
                config.includeInstantGlobalTiltedIrradiance, //
                "channel-type.openmeteo.forecast.global-tilted-irradiance-instant.label",
                "channel-type.openmeteo.forecast.global-tilted-irradiance-instant.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_INSTANT_TERRESTRIAL_SOLAR_RADIATION, CHANNEL_TYPE_UID_TERRESTRIAL_SOLAR_RADIATION,
                config.includeInstantTerrestrialSolarRadiation, //
                "channel-type.openmeteo.forecast.terrestrial-solar-radiation-instant.label",
                "channel-type.openmeteo.forecast.terrestrial-solar-radiation-instant.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SUNSHINE_DURATION,
                CHANNEL_TYPE_UID_SUNSHINE_DURATION, config.includeSunshineDuration, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_PRECIPITATION,
                CHANNEL_TYPE_UID_PRECIPITATION, config.includePrecipitation, //
                null, "channel-type.openmeteo.forecast.precipitation-fifteen-minutes.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SNOW,
                CHANNEL_TYPE_UID_SNOW, config.includeSnow, //
                null, "channel-type.openmeteo.forecast.snow-fifteen-minutes.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_RAIN,
                CHANNEL_TYPE_UID_RAIN, config.includeRain, //
                null, "channel-type.openmeteo.forecast.rain-fifteen-minutes.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SHOWERS,
                CHANNEL_TYPE_UID_SHOWERS, config.includeShowers, //
                null, "channel-type.openmeteo.forecast.showers-fifteen-minutes.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SNOW_DEPTH,
                CHANNEL_TYPE_UID_SNOW_DEPTH, config.includeSnowDepth, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_FREEZING_LEVEL_HEIGHT,
                CHANNEL_TYPE_UID_FREEZING_LEVEL_HEIGHT, config.includeFreezingLevelHeight, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_CAPE,
                CHANNEL_TYPE_UID_CAPE, config.includeCape, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_WIND_SPEED,
                SYSTEM_CHANNEL_TYPE_UID_WIND_SPEED, config.includeWindSpeed, //
                "channel-type.openmeteo.forecast.wind-speed.label",
                "channel-type.openmeteo.forecast.wind-speed.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_WIND_DIRECTION,
                SYSTEM_CHANNEL_TYPE_UID_WIND_DIRECTION, config.includeWindDirection, //
                "channel-type.openmeteo.forecast.wind-direction.label",
                "channel-type.openmeteo.forecast.wind-direction.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_GUST_SPEED,
                CHANNEL_TYPE_UID_GUST_SPEED, config.includeGustSpeed, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_VISIBILITY,
                CHANNEL_TYPE_UID_VISIBILITY, config.includeVisibility, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_WEATHER_CODE,
                CHANNEL_TYPE_UID_WEATHER_CODE, config.includeWeatherCode, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_ICON_ID,
                CHANNEL_TYPE_UID_ICON_ID, config.includeIconId, labelArguments);
    }

    protected WeatherApiResponse requestData(OpenMeteoConnection connection, PointType location)
            throws CommunicationException, ConfigurationException {
        OpenMeteoForecastThingConfiguration config = getConfigAs(OpenMeteoForecastThingConfiguration.class);

        return connection.getForecast(location, getForecastValues(),
                (config.hourlyTimeSeries || config.hourlySplit) ? config.hourlyHours : null, //
                (config.dailyTimeSeries || config.dailySplit) ? config.dailyDays : null, //
                config.current, //
                (config.minutely15) ? config.minutely15Steps : null, //
                config.panelTilt, config.panelAzimuth);
    }

    private EnumSet<OpenMeteoConnection.ForecastValue> getForecastValues() {
        EnumSet<OpenMeteoConnection.ForecastValue> result = EnumSet.noneOf(OpenMeteoConnection.ForecastValue.class);
        OpenMeteoForecastThingConfiguration config = getConfigAs(OpenMeteoForecastThingConfiguration.class);

        if (config.includeTemperature)
            result.add(ForecastValue.TEMPERATURE);
        if (config.includeHumidity)
            result.add(ForecastValue.HUMIDITY);
        if (config.includeDewPoint)
            result.add(ForecastValue.DEW_POINT);
        if (config.includeApparentTemperature)
            result.add(ForecastValue.APPARENT_TEMPERATURE);
        if (config.includePressure)
            result.add(ForecastValue.PRESSURE);
        if (config.includeCloudiness)
            result.add(ForecastValue.CLOUDINESS);
        if (config.includeWindSpeed)
            result.add(ForecastValue.WIND_SPEED);
        if (config.includeWindDirection)
            result.add(ForecastValue.WING_DIRECTION);
        if (config.includeGustSpeed)
            result.add(ForecastValue.GUST_SPEED);
        if (config.includeShortwaveRadiation)
            result.add(ForecastValue.SHORTWAVE_RADIATION);
        if (config.includeDirectRadiation)
            result.add(ForecastValue.DIRECT_RADIATION);
        if (config.includeDirectNormalIrradiance)
            result.add(ForecastValue.DIRECT_NORMAL_IRRADIANCE);
        if (config.includeDiffuseRadiation)
            result.add(ForecastValue.DIFFUSE_RADIATION);
        if (config.includeGlobalTiltedIrradiance)
            result.add(ForecastValue.GLOBAL_TILTED_IRRADIANCE);
        if (config.includeTerrestrialSolarRadiation)
            result.add(ForecastValue.TERRESTRIAL_SOLAR_RADIATION);
        if (config.includeInstantShortwaveRadiation)
            result.add(ForecastValue.INSTANT_SHORTWAVE_RADIATION);
        if (config.includeInstantDirectRadiation)
            result.add(ForecastValue.INSTANT_DIRECT_RADIATION);
        if (config.includeInstantDirectNormalIrradiance)
            result.add(ForecastValue.INSTANT_DIRECT_NORMAL_IRRADIANCE);
        if (config.includeInstantDiffuseRadiation)
            result.add(ForecastValue.INSTANT_DIFFUSE_RADIATION);
        if (config.includeInstantGlobalTiltedIrradiance)
            result.add(ForecastValue.INSTANT_GLOBAL_TILTED_IRRADIANCE);
        if (config.includeInstantTerrestrialSolarRadiation)
            result.add(ForecastValue.INSTANT_TERRESTRIAL_SOLAR_RADIATION);
        if (config.includeVapourPressureDeficit)
            result.add(ForecastValue.VAPOUR_PRESSURE_DEFICIT);
        if (config.includeCape)
            result.add(ForecastValue.CAPE);
        if (config.includeEvapotranspiration)
            result.add(ForecastValue.EVAPOTRANSPIRATION);
        if (config.includeEt0FAOEvapotranspiration)
            result.add(ForecastValue.ET0_EVAPOTRANSPIRATION);
        if (config.includePrecipitation)
            result.add(ForecastValue.PRECIPITATION);
        if (config.includeSnow)
            result.add(ForecastValue.SNOW);
        if (config.includePrecipitationProbability)
            result.add(ForecastValue.PRECIPITATION_PROBABILITY);
        if (config.includeRain)
            result.add(ForecastValue.RAIN);
        if (config.includeShowers)
            result.add(ForecastValue.SHOWERS);
        if (config.includeWeatherCode)
            result.add(ForecastValue.WEATHER_CODE);
        if (config.includeIconId) {
            result.add(ForecastValue.WEATHER_CODE);
            result.add(ForecastValue.IS_DAY);
        }
        if (config.includeSnowDepth)
            result.add(ForecastValue.SNOW_DEPTH);
        if (config.includeFreezingLevelHeight)
            result.add(ForecastValue.FREEZING_LEVEL_HEIGHT);
        if (config.includeVisibility)
            result.add(ForecastValue.VISIBILITY);
        if (config.includeIsDay)
            result.add(ForecastValue.IS_DAY);
        if (config.includeSunrise)
            result.add(ForecastValue.SUNRISE);
        if (config.includeSunrise)
            result.add(ForecastValue.SUNSET);
        if (config.includeSunshineDuration)
            result.add(ForecastValue.SUNSHINE_DURATION);
        if (config.includeDaylightDuration)
            result.add(ForecastValue.DAYLIGHT_DURATION);
        if (config.includeUVIndex)
            result.add(ForecastValue.UV_INDEX);
        if (config.includeUVIndexClearSky)
            result.add(ForecastValue.UV_INDEX_CLEAR_SKY);

        return result;
    }

    /**
     * Updates the channel with the given UID from the latest Open Meteo data retrieved.
     *
     * @param channelUID UID of the channel
     */
    protected void updateChannel(ChannelUID channelUID) {
        String channelGroupId = Optional.ofNullable(channelUID.getGroupId()).orElse("");
        logger.debug("OpenMeteoForecastThingHandler: updateChannel {}, groupID {}", channelUID, channelGroupId);

        switch (channelGroupId) {
            case CHANNEL_GROUP_HOURLY_TIME_SERIES:
                updateHourlyTimeSeries(channelUID);
                break;
            case CHANNEL_GROUP_DAILY_TIME_SERIES:
                updateDailyTimeSeries(channelUID);
                break;
            case CHANNEL_GROUP_DAILY_TODAY:
                updateDailyChannel(channelUID, 0);
                break;
            case CHANNEL_GROUP_DAILY_TOMORROW:
                updateDailyChannel(channelUID, 1);
                break;
            case CHANNEL_GROUP_CURRENT:
                updateCurrentChannel(channelUID);
                break;
            case CHANNEL_GROUP_MINUTELY_15:
                updateMinutely15TImeSeries(channelUID);
                break;
            default:
                Matcher hourlyForecastMatcher = CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN.matcher(channelGroupId);
                if (hourlyForecastMatcher.find()) {
                    int i = Integer.parseInt(hourlyForecastMatcher.group(1));
                    updateHourlyChannel(channelUID, (i - 1));
                    break;
                }
                Matcher dailyForecastMatcher = CHANNEL_GROUP_DAILY_FORECAST_PREFIX_PATTERN.matcher(channelGroupId);
                if (dailyForecastMatcher.find()) {
                    int i = Integer.parseInt(dailyForecastMatcher.group(1));
                    updateDailyChannel(channelUID, i);
                    break;
                }
                break;
        }
    }

    /**
     * Update the hourly forecast channel from the last Open Meteo data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param index the index of the hourly data referenced by the channel (hour 1 is index 0)
     */
    private void updateHourlyChannel(ChannelUID channelUID, int index) {
        var forecastData = this.forecastData;
        if (forecastData != null) {
            updateForecastChannel(channelUID, forecastData.hourly(), index);
        }
    }

    /**
     * Update the daily forecast channel from the last Open Meteo data retrieved.
     *
     * @param channelUID the id identifying the channel to be updated
     * @param index the index of the daily data referenced by the channel (today is index 0, tomorrow is index 1...)
     */
    private void updateDailyChannel(ChannelUID channelUID, int index) {
        var forecastData = this.forecastData;
        if (forecastData != null) {
            updateForecastChannel(channelUID, forecastData.daily(), index);
        }
    }

    private State getIconIdState(int weatherCode, boolean isDay) {
        // inspired by this mapping: https://gist.github.com/stellasphere/9490c195ed2b53c707087c8c2db4ec0c
        String iconId;

        switch (weatherCode) {
            case 0:
            case 1:
                iconId = "01";
                break;
            case 2:
                iconId = "02";
                break;
            case 3:
                iconId = "03";
                break;
            case 45:
            case 48:
                iconId = "50";
                break;
            case 51:
            case 53:
            case 55:
            case 56:
            case 57:
                iconId = "09";
                break;
            case 61:
            case 63:
            case 65:
            case 66:
            case 67:
                iconId = "10";
                break;
            case 71:
            case 73:
            case 75:
            case 77:
                iconId = "13";
                break;
            case 80:
            case 81:
            case 82:
                iconId = "09";
                break;
            case 85:
            case 86:
                iconId = "13";
                break;
            case 95:
            case 96:
            case 99:
                iconId = "11";
                break;
            default:
                iconId = "01";
        }

        iconId += (isDay) ? "d" : "n";

        return new StringType(iconId);
    }

    protected int getVariableIndex(String channelId) {
        return switch (channelId.toString()) {
            case CHANNEL_FORECAST_TEMPERATURE -> Variable.temperature;
            case CHANNEL_FORECAST_HUMIDITY -> Variable.relative_humidity;
            case CHANNEL_FORECAST_DEW_POINT -> Variable.dew_point;
            case CHANNEL_FORECAST_APPARENT_TEMPERATURE -> Variable.apparent_temperature;
            case CHANNEL_FORECAST_PRESSURE -> Variable.surface_pressure;
            case CHANNEL_FORECAST_CLOUDINESS -> Variable.cloud_cover;
            case CHANNEL_FORECAST_WIND_SPEED -> Variable.wind_speed;
            case CHANNEL_FORECAST_WIND_DIRECTION -> Variable.wind_direction;
            case CHANNEL_FORECAST_GUST_SPEED -> Variable.wind_gusts;
            case CHANNEL_FORECAST_SHORTWAVE_RADIATION -> Variable.shortwave_radiation;
            case CHANNEL_FORECAST_DIRECT_RADIATION -> Variable.direct_radiation;
            case CHANNEL_FORECAST_DIRECT_NORMAL_IRRADIANCE -> Variable.direct_normal_irradiance;
            case CHANNEL_FORECAST_DIFFUSE_RADIATION -> Variable.diffuse_radiation;
            case CHANNEL_FORECAST_GLOBAL_TILTED_IRRADIANCE -> Variable.global_tilted_irradiance;
            case CHANNEL_FORECAST_TERRESTRIAL_SOLAR_RADIATION -> Variable.terrestrial_radiation;
            case CHANNEL_FORECAST_INSTANT_SHORTWAVE_RADIATION -> Variable.shortwave_radiation_instant;
            case CHANNEL_FORECAST_INSTANT_DIRECT_RADIATION -> Variable.direct_radiation_instant;
            case CHANNEL_FORECAST_INSTANT_DIRECT_NORMAL_IRRADIANCE -> Variable.direct_normal_irradiance_instant;
            case CHANNEL_FORECAST_INSTANT_DIFFUSE_RADIATION -> Variable.diffuse_radiation_instant;
            case CHANNEL_FORECAST_INSTANT_GLOBAL_TILTED_IRRADIANCE -> Variable.global_tilted_irradiance_instant;
            case CHANNEL_FORECAST_INSTANT_TERRESTRIAL_SOLAR_RADIATION -> Variable.terrestrial_radiation_instant;
            case CHANNEL_FORECAST_VAPOUR_PRESSURE_DEFICIT -> Variable.vapour_pressure_deficit;
            case CHANNEL_FORECAST_CAPE -> Variable.cape;
            case CHANNEL_FORECAST_EVAPOTRANSPIRATION -> Variable.evapotranspiration;
            case CHANNEL_FORECAST_ET0_EVAPOTRANSPIRATION -> Variable.et0_fao_evapotranspiration;
            case CHANNEL_FORECAST_PRECIPITATION -> Variable.precipitation;
            case CHANNEL_FORECAST_PRECIPITATION_HOURS -> Variable.precipitation_hours;
            case CHANNEL_FORECAST_SNOW -> Variable.snowfall;
            case CHANNEL_FORECAST_PRECIPITATION_PROBABILITY -> Variable.precipitation_probability;
            case CHANNEL_FORECAST_RAIN -> Variable.rain;
            case CHANNEL_FORECAST_SHOWERS -> Variable.showers;
            case CHANNEL_FORECAST_WEATHER_CODE -> Variable.weather_code;
            case CHANNEL_FORECAST_ICON_ID -> Variable.weather_code; // return the weather code to serve as the basis for
                                                                    // icon id mapping
            case CHANNEL_FORECAST_SNOW_DEPTH -> Variable.snow_depth;
            case CHANNEL_FORECAST_FREEZING_LEVEL_HEIGHT -> Variable.freezing_level_height;
            case CHANNEL_FORECAST_VISIBILITY -> Variable.visibility;
            case CHANNEL_FORECAST_IS_DAY -> Variable.is_day;
            case CHANNEL_FORECAST_SUNRISE -> Variable.sunrise;
            case CHANNEL_FORECAST_SUNSET -> Variable.sunset;
            case CHANNEL_FORECAST_SUNSHINE_DURATION -> 103; // Variable.sunshine_duration; defined in 1.6
            case CHANNEL_FORECAST_DAYLIGHT_DURATION -> Variable.daylight_duration;
            case CHANNEL_FORECAST_UV_INDEX -> Variable.uv_index;
            case CHANNEL_FORECAST_UV_INDEX_CLEAR_SKY -> Variable.uv_index_clear_sky;
            default -> Variable.undefined;
        };
    }

    @Override
    protected State getForecastState(String channelId, VariableWithValues values, @Nullable Integer valueIndex,
            @Nullable VariablesWithTime forecast) {
        State channelState = super.getForecastState(channelId, values, valueIndex, forecast);

        if (channelId.equals(CHANNEL_FORECAST_ICON_ID)) {
            int weatherCode = channelState.as(DecimalType.class).intValue();

            StringBuilder isDayChannelId = new StringBuilder(CHANNEL_FORECAST_IS_DAY);
            VariableWithValues isDayValues = getVariableValues(isDayChannelId, forecast);

            State isDayState = (isDayValues != null)
                    ? super.getForecastState(CHANNEL_FORECAST_IS_DAY, isDayValues, valueIndex, forecast)
                    : UnDefType.UNDEF;

            return getIconIdState(weatherCode, (isDayState != OnOffType.OFF));
        }

        return channelState;
    }

    protected State getForecastState(String channelId, @Nullable Float floatValue, @Nullable Long longValue) {
        State state = UnDefType.UNDEF;

        switch (channelId) {
            case CHANNEL_FORECAST_TEMPERATURE:
                state = getQuantityTypeState(floatValue, SIUnits.CELSIUS);
                break;
            case CHANNEL_FORECAST_HUMIDITY:
                state = getQuantityTypeState(floatValue, Units.PERCENT);
                break;
            case CHANNEL_FORECAST_DEW_POINT:
                state = getQuantityTypeState(floatValue, SIUnits.CELSIUS);
                break;
            case CHANNEL_FORECAST_APPARENT_TEMPERATURE:
                state = getQuantityTypeState(floatValue, SIUnits.CELSIUS);
                break;
            case CHANNEL_FORECAST_PRESSURE:
                state = getQuantityTypeState(floatValue, MetricPrefix.HECTO(SIUnits.PASCAL));
                break;
            case CHANNEL_FORECAST_CLOUDINESS:
                state = getQuantityTypeState(floatValue, Units.PERCENT);
                break;
            case CHANNEL_FORECAST_WIND_SPEED:
                state = getQuantityTypeState(floatValue, Units.METRE_PER_SECOND);
                break;
            case CHANNEL_FORECAST_WIND_DIRECTION:
                state = getQuantityTypeState(floatValue, Units.DEGREE_ANGLE);
                break;
            case CHANNEL_FORECAST_GUST_SPEED:
                state = getQuantityTypeState(floatValue, Units.METRE_PER_SECOND);
                break;
            case CHANNEL_FORECAST_SHORTWAVE_RADIATION:
            case CHANNEL_FORECAST_INSTANT_SHORTWAVE_RADIATION:
                state = getQuantityTypeState(floatValue, 100, Units.MICROWATT_PER_SQUARE_CENTIMETRE);
                break;
            case CHANNEL_FORECAST_DIRECT_RADIATION:
            case CHANNEL_FORECAST_INSTANT_DIRECT_RADIATION:
                state = getQuantityTypeState(floatValue, 100, Units.MICROWATT_PER_SQUARE_CENTIMETRE);
                break;
            case CHANNEL_FORECAST_DIRECT_NORMAL_IRRADIANCE:
            case CHANNEL_FORECAST_INSTANT_DIRECT_NORMAL_IRRADIANCE:
                state = getQuantityTypeState(floatValue, 100, Units.MICROWATT_PER_SQUARE_CENTIMETRE);
                break;
            case CHANNEL_FORECAST_DIFFUSE_RADIATION:
            case CHANNEL_FORECAST_INSTANT_DIFFUSE_RADIATION:
                state = getQuantityTypeState(floatValue, 100, Units.MICROWATT_PER_SQUARE_CENTIMETRE);
                break;
            case CHANNEL_FORECAST_GLOBAL_TILTED_IRRADIANCE:
            case CHANNEL_FORECAST_INSTANT_GLOBAL_TILTED_IRRADIANCE:
                state = getQuantityTypeState(floatValue, 100, Units.MICROWATT_PER_SQUARE_CENTIMETRE);
                break;
            case CHANNEL_FORECAST_TERRESTRIAL_SOLAR_RADIATION:
            case CHANNEL_FORECAST_INSTANT_TERRESTRIAL_SOLAR_RADIATION:
                state = getQuantityTypeState(floatValue, 100, Units.MICROWATT_PER_SQUARE_CENTIMETRE);
                break;
            case CHANNEL_FORECAST_VAPOUR_PRESSURE_DEFICIT:
                state = getQuantityTypeState(floatValue, MetricPrefix.HECTO(SIUnits.PASCAL));
                break;
            case CHANNEL_FORECAST_CAPE:
                state = getDecimalTypeState(floatValue); // OpenMeteoBindingUnits.JOULES_PER_KILOGRAM);
                break;
            case CHANNEL_FORECAST_EVAPOTRANSPIRATION:
                state = getQuantityTypeState(floatValue, MetricPrefix.MILLI(SIUnits.METRE));
                break;
            case CHANNEL_FORECAST_ET0_EVAPOTRANSPIRATION:
                state = getQuantityTypeState(floatValue, MetricPrefix.MILLI(SIUnits.METRE));
                break;
            case CHANNEL_FORECAST_PRECIPITATION:
                state = getQuantityTypeState(floatValue, MetricPrefix.MILLI(SIUnits.METRE));
                break;
            case CHANNEL_FORECAST_PRECIPITATION_HOURS:
                state = getQuantityTypeState(floatValue, Units.HOUR);
                break;
            case CHANNEL_FORECAST_SNOW:
                state = getQuantityTypeState(floatValue, MetricPrefix.CENTI(SIUnits.METRE));
                break;
            case CHANNEL_FORECAST_PRECIPITATION_PROBABILITY:
                state = getQuantityTypeState(floatValue, Units.PERCENT);
                break;
            case CHANNEL_FORECAST_RAIN:
                state = getQuantityTypeState(floatValue, MetricPrefix.MILLI(SIUnits.METRE));
                break;
            case CHANNEL_FORECAST_SHOWERS:
                state = getQuantityTypeState(floatValue, MetricPrefix.MILLI(SIUnits.METRE));
                break;
            case CHANNEL_FORECAST_WEATHER_CODE:
            case CHANNEL_FORECAST_ICON_ID: // same as above
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_FORECAST_SNOW_DEPTH:
                state = getQuantityTypeState(floatValue, SIUnits.METRE);
                break;
            case CHANNEL_FORECAST_FREEZING_LEVEL_HEIGHT:
                state = getQuantityTypeState(floatValue, SIUnits.METRE);
                break;
            case CHANNEL_FORECAST_VISIBILITY:
                state = getQuantityTypeState(floatValue, SIUnits.METRE);
                break;
            case CHANNEL_FORECAST_IS_DAY:
                state = getOnOffState(floatValue);
                break;
            case CHANNEL_FORECAST_SUNRISE:
                state = getDateTimeTypeState(longValue);
                break;
            case CHANNEL_FORECAST_SUNSET:
                state = getDateTimeTypeState(longValue);
                break;
            case CHANNEL_FORECAST_SUNSHINE_DURATION:
                state = getQuantityTypeState(floatValue, Units.SECOND);
                break;
            case CHANNEL_FORECAST_DAYLIGHT_DURATION:
                state = getQuantityTypeState(floatValue, Units.SECOND);
                break;
            case CHANNEL_FORECAST_UV_INDEX:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_FORECAST_UV_INDEX_CLEAR_SKY:
                state = getDecimalTypeState(floatValue);
                break;
            default:
                // This should not happen
                logger.warn("Unknown channel id {} in weather data", channelId);
                break;
        }
        return state;
    }
}
