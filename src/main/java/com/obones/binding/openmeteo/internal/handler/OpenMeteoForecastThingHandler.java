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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.config.OpenMeteoForecastThingConfiguration;
import com.obones.binding.openmeteo.internal.connection.OpenMeteoConnection;
import com.obones.binding.openmeteo.internal.connection.OpenMeteoConnection.ForecastValue;
import com.obones.binding.openmeteo.internal.utils.Localization;
import com.openmeteo.sdk.Aggregation;
import com.openmeteo.sdk.Variable;
import com.openmeteo.sdk.VariableWithValues;
import com.openmeteo.sdk.VariablesSearch;
import com.openmeteo.sdk.VariablesWithTime;
import com.openmeteo.sdk.WeatherApiResponse;

@NonNullByDefault
public class OpenMeteoForecastThingHandler extends BaseThingHandler {

    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(OpenMeteoBridgeHandler.class);
    private @Nullable ChannelTypeRegistry channelTypeRegistry;
    private @Nullable WeatherApiResponse forecastData = null;
    private final TimeZoneProvider timeZoneProvider;

    private static final Pattern CHANNEL_GROUP_HOURLY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_HOURLY_PREFIX + "([0-9]*)");
    private static final Pattern CHANNEL_GROUP_DAILY_FORECAST_PREFIX_PATTERN = Pattern
            .compile(CHANNEL_GROUP_DAILY_PREFIX + "([0-9]*)");

    public Localization localization;

    protected @Nullable PointType location;

    public OpenMeteoForecastThingHandler(Thing thing, Localization localization,
            final TimeZoneProvider timeZoneProvider, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.localization = localization;
        this.timeZoneProvider = timeZoneProvider;
        this.channelTypeRegistry = channelTypeRegistry;
        logger.trace("OpenMeteoForecastHandler(thing={},localization={}) constructor called.", thing, localization);
    }

    @Override
    public void initialize() {
        logger.trace("initialize() called.");
        Bridge thisBridge = getBridge();
        logger.debug("initialize(): Initializing thing {} in combination with bridge {}.", getThing().getUID(),
                thisBridge);

        // Initialize the channels early on as they don't require the bridge to be present
        // This allows seeing the effect of the various configuration switches without needing
        // to activate the bridge
        initializeChannels();

        if (thisBridge == null) {
            logger.trace("initialize() updating ThingStatus to OFFLINE/CONFIGURATION_PENDING.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);

        } else if (thisBridge.getStatus() == ThingStatus.ONLINE) {
            logger.trace("initialize() checking for configuration validity.");

            boolean configValid = true;
            OpenMeteoForecastThingConfiguration config = getConfigAs(OpenMeteoForecastThingConfiguration.class);
            if (config.location == null || config.location.trim().isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-missing-location");
                configValid = false;
            }

            try {
                location = new PointType(config.location);
            } catch (IllegalArgumentException e) {
                logger.warn("Error parsing 'location' parameter: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-parsing-location");
                location = null;
                configValid = false;
            }

            if (configValid) {
                initializeProperties();

                logger.trace("initialize() updating ThingStatus to ONLINE.");
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            logger.trace("initialize() updating ThingStatus to OFFLINE/BRIDGE_OFFLINE.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
        logger.trace("initialize() done.");
    }

    protected synchronized void initializeProperties() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            OpenMeteoBridgeHandler bridgeHandler = (OpenMeteoBridgeHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                initializeProperties(bridgeHandler);
            }
        }
        logger.trace("initializeProperties() done.");
    }

    protected void initializeProperties(OpenMeteoBridgeHandler bridgeHandler) {
    }

    protected synchronized void initializeChannels() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            OpenMeteoBridgeHandler bridgeHandler = (OpenMeteoBridgeHandler) bridge.getHandler();
            if (bridgeHandler == null) {
                logger.warn("initializeOptionalChannels: Could not get bridge handler");
                return;
            }

            initializeChannels(bridgeHandler);
        }
    }

    protected void initializeChannels(OpenMeteoBridgeHandler bridgeHandler) {
        ThingHandlerCallback callback = getCallback();
        if (callback == null) {
            logger.warn("initializeOptionalChannels: Could not get callback.");
            return;
        }

        OpenMeteoForecastThingConfiguration config = getConfigAs(OpenMeteoForecastThingConfiguration.class);

        ThingBuilder builder = editThing();
        ThingUID thingUID = thing.getUID();

        // Remove every channel and rebuild only the required ones, this makes for easier to read code
        // and has no impact until the build() method is called
        builder.withoutChannels(thing.getChannels());

        if (config.hourlyTimeSeries) {
            String labelSuffix = localization
                    .getText("channel-type.openmeteo.forecast.label-suffix.hourly.time-series");
            initializeHourlyGroupOptionalChannels(callback, builder, thingUID, config, CHANNEL_GROUP_HOURLY_TIME_SERIES,
                    labelSuffix);
        }

        if (config.hourlySplit) {
            DecimalFormat hourlyFormatter = new DecimalFormat("00");
            String labelSuffix = localization.getText("channel-type.openmeteo.forecast.label-suffix.hourly.split");

            for (int hour = 1; hour <= config.hourlyHours; hour++)
                initializeHourlyGroupOptionalChannels(callback, builder, thingUID, config,
                        CHANNEL_GROUP_HOURLY_PREFIX + hourlyFormatter.format(hour), String.format(labelSuffix, hour));
        }

        if (config.dailyTimeSeries) {
            String labelSuffix = localization.getText("channel-type.openmeteo.forecast.label-suffix.daily.time-series");
            initializeDailyGroupOptionalChannels(callback, builder, thingUID, config, CHANNEL_GROUP_DAILY_TIME_SERIES,
                    labelSuffix);
        }

        if (config.dailySplit) {
            initializeDailyGroupOptionalChannels(callback, builder, thingUID, config, CHANNEL_GROUP_DAILY_TODAY,
                    localization.getText("channel-type.openmeteo.forecast.label-suffix.daily.today"));
            if (config.dailyDays >= 2)
                initializeDailyGroupOptionalChannels(callback, builder, thingUID, config, CHANNEL_GROUP_DAILY_TOMORROW,
                        localization.getText("channel-type.openmeteo.forecast.label-suffix.daily.tomorrow"));

            DecimalFormat dailyFormatter = new DecimalFormat("00");
            String labelSuffix = localization.getText("channel-type.openmeteo.forecast.label-suffix.daily.split");
            for (int day = 2; day < config.dailyDays; day++)
                initializeDailyGroupOptionalChannels(callback, builder, thingUID, config,
                        CHANNEL_GROUP_DAILY_PREFIX + dailyFormatter.format(day), String.format(labelSuffix, day));
        }

        if (config.current) {
            initializeCurrentGroupOptionalChannels(callback, builder, thingUID, config);
        }

        if (config.minutely15) {
            initializeMinutely15GroupOptionalChannels(callback, builder, thingUID, config);
        }

        updateThing(builder.build());
    }

    protected ThingBuilder initializeHourlyGroupOptionalChannels(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, OpenMeteoForecastThingConfiguration config, String channelGroupId, String labelSuffix) {

        Object[] labelArguments = { labelSuffix };

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
            ThingUID thingUID, OpenMeteoForecastThingConfiguration config, String channelGroupId, String labelSuffix) {

        Object[] labelArguments = { labelSuffix };

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

        Object[] labelArguments = { localization.getText("channel-type.openmeteo.forecast.label-suffix.current") };
        String channelGroupId = CHANNEL_GROUP_CURRENT;

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

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_APPARENT_TEMPERATURE,
                CHANNEL_TYPE_UID_APPARENT_TEMPERATURE, config.includeApparentTemperature, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_IS_DAY,
                CHANNEL_TYPE_UID_IS_DAY, config.includeIsDay, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_PRECIPITATION,
                CHANNEL_TYPE_UID_PRECIPITATION, config.includePrecipitation, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_RAIN,
                CHANNEL_TYPE_UID_RAIN, config.includeRain, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SHOWERS,
                CHANNEL_TYPE_UID_SHOWERS, config.includeShowers, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SNOW,
                CHANNEL_TYPE_UID_SNOW, config.includeSnow, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_WEATHER_CODE,
                CHANNEL_TYPE_UID_WEATHER_CODE, config.includeWeatherCode, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_CLOUDINESS,
                CHANNEL_TYPE_UID_CLOUDINESS, config.includeCloudiness, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_PRESSURE,
                SYSTEM_CHANNEL_TYPE_UID_BAROMETRIC_PRESSURE, config.includePressure, //
                "channel-type.openmeteo.forecast.pressure.label",
                "channel-type.openmeteo.forecast.pressure.description", //
                labelArguments, null);

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

        return builder;
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
                CHANNEL_TYPE_UID_SHORTWAVE_RADIATION, config.includeShortwaveRadiation, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_DIRECT_RADIATION,
                CHANNEL_TYPE_UID_DIRECT_RADIATION, config.includeDirectRadiation, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_FORECAST_DIRECT_NORMAL_IRRADIANCE, CHANNEL_TYPE_UID_DIRECT_NORMAL_IRRADIANCE,
                config.includeDirectNormalIrradiance, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_DIFFUSE_RADIATION,
                CHANNEL_TYPE_UID_DIFFUSE_RADIATION, config.includeDiffuseRadiation, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SUNSHINE_DURATION,
                CHANNEL_TYPE_UID_SUNSHINE_DURATION, config.includeSunshineDuration, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_PRECIPITATION,
                CHANNEL_TYPE_UID_PRECIPITATION, config.includePrecipitation, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SNOW,
                CHANNEL_TYPE_UID_SNOW, config.includeSnow, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_RAIN,
                CHANNEL_TYPE_UID_RAIN, config.includeRain, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_FORECAST_SHOWERS,
                CHANNEL_TYPE_UID_SHOWERS, config.includeShowers, labelArguments);

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
    }

    protected ThingBuilder initializeOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, String channelGroupId, String channelId, ChannelTypeUID channelTypeUID, boolean isActive,
            AutoUpdatePolicy autoUpdatePolicy, @Nullable String labelKey, @Nullable String descriptionKey,
            Object @Nullable [] labelArguments, Object @Nullable [] descriptionArguments) {
        ChannelUID channelUID = new ChannelUID(thing.getUID(), channelGroupId, channelId);
        ChannelBuilder channelBuilder = callback.createChannelBuilder(channelUID, channelTypeUID);
        ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID);

        String labelText = (labelKey != null) ? localization.getText(labelKey) : channelType.getLabel();
        if (labelArguments != null)
            labelText = String.format(labelText, labelArguments);

        @Nullable
        String descriptionText = (descriptionKey != null) ? localization.getText(descriptionKey)
                : channelType.getDescription();
        if (descriptionText != null) {
            if (descriptionArguments != null)
                descriptionText = String.format(descriptionText, descriptionArguments);

            channelBuilder.withDescription(descriptionText);
        }

        channelBuilder.withAutoUpdatePolicy(autoUpdatePolicy).withLabel(labelText);

        Channel channel = channelBuilder.build();

        builder = builder.withoutChannel(channelUID);

        return (isActive) ? builder.withChannel(channel) : builder;
    }

    protected ThingBuilder initializeOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, String channelGroupId, String channelId, ChannelTypeUID channelTypeUID, boolean isActive,
            @Nullable String labelKey, @Nullable String descriptionKey, Object @Nullable [] labelArguments,
            Object @Nullable [] descriptionArguments) {
        return initializeOptionalChannel(callback, builder, thingUID, channelGroupId, channelId, channelTypeUID,
                isActive, AutoUpdatePolicy.DEFAULT, labelKey, descriptionKey, labelArguments, descriptionArguments);
    }

    protected ThingBuilder initializeOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, String channelGroupId, String channelId, ChannelTypeUID channelTypeUID, boolean isActive,
            @Nullable String labelKey, @Nullable String descriptionKey) {
        return initializeOptionalChannel(callback, builder, thingUID, channelGroupId, channelId, channelTypeUID,
                isActive, labelKey, descriptionKey, null, null);
    }

    protected ThingBuilder initializeOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, String channelGroupId, String channelId, ChannelTypeUID channelTypeUID, boolean isActive,
            Object @Nullable [] labelArguments) {
        return initializeOptionalChannel(callback, builder, thingUID, channelGroupId, channelId, channelTypeUID,
                isActive, null, null, labelArguments, null);
    }

    protected ThingBuilder initializeOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, String channelGroupId, String channelId, ChannelTypeUID channelTypeUID,
            boolean isActive) {
        return initializeOptionalChannel(callback, builder, thingUID, channelGroupId, channelId, channelTypeUID,
                isActive, null);
    }

    @Override
    public void dispose() {
        logger.trace("dispose() called.");
        super.dispose();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.trace("channelLinked({}) called.", channelUID.getAsString());

        if (thing.getStatus() == ThingStatus.ONLINE) {
            handleCommand(channelUID, RefreshType.REFRESH);
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (isInitialized()) { // prevents change of address
            validateConfigurationParameters(configurationParameters);
            Configuration configuration = editConfiguration();
            for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
                logger.trace("handleConfigurationUpdate(): found modified config entry {}.",
                        configurationParameter.getKey());
                configuration.put(configurationParameter.getKey(), configurationParameter.getValue());
            }
            // persist new configuration and reinitialize handler
            dispose();
            updateConfiguration(configuration);
            initialize();
        } else {
            super.handleConfigurationUpdate(configurationParameters);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo info) {
        switch (info.getStatus()) {
            case OFFLINE:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                break;

            case ONLINE:
                if (location == null)
                    initialize();
                else
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                break;

            default:
                super.bridgeStatusChanged(info);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand({},{}) initiated by {}.", channelUID.getAsString(), command,
                Thread.currentThread());
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.trace("handleCommand() nothing yet to do as there is no bridge available.");
        } else {
            BridgeHandler handler = bridge.getHandler();
            if (handler == null) {
                logger.trace("handleCommand() nothing yet to do as thing is not initialized.");
            } else {
                if (command instanceof RefreshType) {
                    updateChannel(channelUID);
                } else {
                    logger.debug("The Open Meteo binding is a read-only binding and cannot handle command '{}'.",
                            command);
                }
            }
        }
    }

    /**
     * Updates OpenMeteo data for this location.
     *
     * @param connection {@link OpenMeteoConnection} instance
     */
    public void updateData(OpenMeteoConnection connection) {
        try {
            if (requestData(connection)) {
                updateChannels();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (CommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getRawMessage());
        } catch (ConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getRawMessage());
        }
    }

    /**
     * Requests the data from Open Meteo API.
     *
     * @param connection {@link OpenMeteoConnection} instance
     * @return true, if the request for the Open Meteo data was successful
     * @throws CommunicationException if there is a problem retrieving the data
     * @throws ConfigurationException if there is a configuration error
     */
    protected boolean requestData(OpenMeteoConnection connection)
            throws CommunicationException, ConfigurationException {
        logger.debug("Update weather and forecast data of thing '{}'.", getThing().getUID());
        OpenMeteoForecastThingConfiguration config = getConfigAs(OpenMeteoForecastThingConfiguration.class);

        var location = this.location;
        if (location != null)
            forecastData = connection.getForecast(location, getForecastValues(),
                    (config.hourlyTimeSeries || config.hourlySplit) ? config.hourlyHours : null, //
                    (config.dailyTimeSeries || config.dailySplit) ? config.dailyDays : null, //
                    config.current, //
                    (config.minutely15) ? config.minutely15Steps : null);

        return true;
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
     * Updates all channels of this handler from the latest Open Meteo data retrieved.
     */
    private void updateChannels() {
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (ChannelKind.STATE.equals(channel.getKind()) && channelUID.isInGroup() && channelUID.getGroupId() != null
                    && isLinked(channelUID)) {
                updateChannel(channelUID);
            }
        }
    }

    /**
     * Updates the channel with the given UID from the latest Open Meteo data retrieved.
     *
     * @param channelUID UID of the channel
     */
    protected void updateChannel(ChannelUID channelUID) {
        String channelGroupId = channelUID.getGroupId();
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

    private void updateForecastTimeSeries(ChannelUID channelUID, @Nullable VariablesWithTime forecast) {
        StringBuilder channelId = new StringBuilder(channelUID.getIdWithoutGroup());
        String channelGroupId = channelUID.getGroupId();

        if (forecast != null) {
            VariableWithValues values = getVariableValues(channelId, forecast);
            if (values != null) {
                TimeSeries timeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
                long time = forecast.time();
                int valuesLength = Math.max(values.valuesLength(), values.valuesInt64Length());
                for (int valueIndex = 0; valueIndex < valuesLength; valueIndex++) {
                    Instant timestamp = Instant.ofEpochSecond(time);
                    State state = getForecastState(channelId.toString(), values, valueIndex);
                    timeSeries.add(timestamp, state);

                    time += forecast.interval();
                }

                logger.debug("Update channel '{}' of group '{}' with new time-series '{}'.", channelId, channelGroupId,
                        timeSeries);
                sendTimeSeries(channelUID, timeSeries);
            } else {
                logger.warn("No values for channel '{}' of group '{}'", channelId, channelGroupId);
            }
        }
    }

    private void updateHourlyTimeSeries(ChannelUID channelUID) {
        var forecastData = this.forecastData;
        if (forecastData != null) {
            updateForecastTimeSeries(channelUID, forecastData.hourly());
        }
    }

    private void updateDailyTimeSeries(ChannelUID channelUID) {
        var forecastData = this.forecastData;
        if (forecastData != null) {
            updateForecastTimeSeries(channelUID, forecastData.daily());
        }
    }

    private void updateMinutely15TImeSeries(ChannelUID channelUID) {
        var forecastData = this.forecastData;
        if (forecastData != null) {
            updateForecastTimeSeries(channelUID, forecastData.minutely15());
        }
    }

    private void updateForecastChannel(ChannelUID channelUID, @Nullable VariablesWithTime forecast,
            @Nullable Integer index) {
        StringBuilder channelId = new StringBuilder(channelUID.getIdWithoutGroup());
        String channelGroupId = channelUID.getGroupId();

        if (forecast != null) {
            VariableWithValues values = getVariableValues(channelId, forecast);
            if (values != null) {
                State state = getForecastState(channelId.toString(), values, index);
                logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId,
                        state);
                updateState(channelUID, state);
            } else {
                logger.warn("No values for channel '{}' of group '{}'", channelId, channelGroupId);
            }
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

    private void updateCurrentChannel(ChannelUID channelUID) {
        var forecastData = this.forecastData;
        if (forecastData != null) {
            updateForecastChannel(channelUID, forecastData.current(), null);
        }
    }

    private @Nullable VariableWithValues getVariableValues(StringBuilder channelId,
            VariablesWithTime variablesWithTime) {
        if (variablesWithTime != null && variablesWithTime.variablesLength() > 0) {
            int aggregation = Aggregation.none;
            int suffixPosition = -1;
            if ((suffixPosition = channelId.lastIndexOf("-min")) >= 0) {
                aggregation = Aggregation.minimum;
                channelId.setLength(suffixPosition);
            } else if ((suffixPosition = channelId.lastIndexOf("-max")) >= 0) {
                aggregation = Aggregation.maximum;
                channelId.setLength(suffixPosition);
            } else if ((suffixPosition = channelId.lastIndexOf("-mean")) >= 0) {
                aggregation = Aggregation.mean;
                channelId.setLength(suffixPosition);
            } else if ((suffixPosition = channelId.lastIndexOf("-sum")) >= 0) {
                aggregation = Aggregation.sum;
                channelId.setLength(suffixPosition);
            } else if ((suffixPosition = channelId.lastIndexOf("-dominant")) >= 0) {
                aggregation = Aggregation.dominant;
                channelId.setLength(suffixPosition);
            }

            int variable = switch (channelId.toString()) {
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

            return new VariablesSearch(variablesWithTime).variable(variable).aggregation(aggregation).first();
        }

        return null;
    }

    protected State getDecimalTypeState(@Nullable Float value) {
        return (value == null) ? UnDefType.UNDEF : new DecimalType(value);
    }

    protected State getQuantityTypeState(@Nullable Number value, Unit<?> unit) {
        return (value == null) ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    protected State getQuantityTypeState(@Nullable Float value, int multiplier, Unit<?> unit) {
        return (value == null) ? UnDefType.UNDEF : new QuantityType<>(value * multiplier, unit);
    }

    protected State getDateTimeTypeState(@Nullable Long value) {
        return (value == null) ? UnDefType.UNDEF
                : new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochSecond(value.longValue()),
                        timeZoneProvider.getTimeZone()));
    }

    protected State getOnOffState(@Nullable Float value) {
        return (value == null) ? UnDefType.UNDEF : (value == 1) ? OnOffType.ON : OnOffType.OFF;
    }

    private State getForecastState(String channelId, VariableWithValues values, @Nullable Integer valueIndex) {
        State state = UnDefType.UNDEF;

        @Nullable
        Float floatValue = null;
        if (valueIndex == null)
            floatValue = values.value();
        else if (valueIndex < values.valuesLength())
            floatValue = values.values(valueIndex);

        @Nullable
        Long longValue = null;
        if ((valueIndex != null) && (valueIndex < values.valuesInt64Length()))
            longValue = values.valuesInt64(valueIndex);

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
                state = getQuantityTypeState(floatValue, 100, Units.MICROWATT_PER_SQUARE_CENTIMETRE);
                break;
            case CHANNEL_FORECAST_DIRECT_RADIATION:
                state = getQuantityTypeState(floatValue, 100, Units.MICROWATT_PER_SQUARE_CENTIMETRE);
                break;
            case CHANNEL_FORECAST_DIRECT_NORMAL_IRRADIANCE:
                state = getQuantityTypeState(floatValue, 100, Units.MICROWATT_PER_SQUARE_CENTIMETRE);
                break;
            case CHANNEL_FORECAST_DIFFUSE_RADIATION:
                state = getQuantityTypeState(floatValue, 100, Units.MICROWATT_PER_SQUARE_CENTIMETRE);
                break;
            case CHANNEL_FORECAST_VAPOUR_PRESSURE_DEFICIT:
                state = getQuantityTypeState(floatValue, MetricPrefix.HECTO(SIUnits.PASCAL));
                break;
            case CHANNEL_FORECAST_CAPE:
                state = getDecimalTypeState(floatValue); // no J/kg unit
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
                logger.warn("Unknown channel id {} in hourly weather data", channelId);
                break;
        }
        return state;
    }
}
