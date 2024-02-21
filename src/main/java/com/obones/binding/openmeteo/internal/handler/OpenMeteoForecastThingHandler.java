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

import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
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
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.OpenMeteoBindingConstants;
import com.obones.binding.openmeteo.internal.config.OpenMeteoForecastThingConfiguration;
import com.obones.binding.openmeteo.internal.connection.OpenMeteoConnection;
import com.obones.binding.openmeteo.internal.connection.OpenMeteoConnection.ForecastValue;
import com.obones.binding.openmeteo.internal.utils.Localization;
import com.openmeteo.sdk.WeatherApiResponse;

@NonNullByDefault
public class OpenMeteoForecastThingHandler extends BaseThingHandler {

    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(OpenMeteoBridgeHandler.class);
    private @Nullable ThingTypeRegistry thingTypeRegistry;
    private @Nullable WeatherApiResponse forecastData = null;

    public Localization localization;

    protected @Nullable PointType location;

    public OpenMeteoForecastThingHandler(Thing thing, Localization localization, ThingTypeRegistry thingTypeRegistry) {
        super(thing);
        this.localization = localization;
        this.thingTypeRegistry = thingTypeRegistry;
        logger.trace("OpenMeteoForecastHandler(thing={},localization={}) constructor called.", thing, localization);
    }

    @Override
    public void initialize() {
        logger.trace("initialize() called.");
        Bridge thisBridge = getBridge();
        logger.debug("initialize(): Initializing thing {} in combination with bridge {}.", getThing().getUID(),
                thisBridge);
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
                initializeChannels();
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
        OpenMeteoForecastThingConfiguration config = getConfigAs(OpenMeteoForecastThingConfiguration.class);
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

        if (config.hourlyTimeSeries)
            initializeGroupOptionalChannels(callback, builder, thingUID, config,
                    OpenMeteoBindingConstants.CHANNEL_GROUP_HOURLY_TIME_SERIES);

        if (config.hourlySplit) {
            DecimalFormat hourlyFormatter = new DecimalFormat("00");
            for (int hour = 1; hour <= config.hourlyHours; hour++) {
                initializeGroupOptionalChannels(callback, builder, thingUID, config,
                        OpenMeteoBindingConstants.CHANNEL_GROUP_HOURLY_PREFIX + hourlyFormatter.format(hour));
            }
        }

        updateThing(builder.build());
    }

    protected ThingBuilder initializeGroupOptionalChannels(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, OpenMeteoForecastThingConfiguration config, String channelGroupId) {

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                OpenMeteoBindingConstants.CHANNEL_FORECAST_TEMPERATURE,
                DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_OUTDOOR_TEMPERATURE,
                config.includeTemperature);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                OpenMeteoBindingConstants.CHANNEL_FORECAST_PRESSURE,
                DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_BAROMETRIC_PRESSURE, config.includePressure);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                OpenMeteoBindingConstants.CHANNEL_FORECAST_HUMIDITY,
                DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_ATMOSPHERIC_HUMIDITY, config.includeHumidity);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                OpenMeteoBindingConstants.CHANNEL_FORECAST_WIND_SPEED,
                DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_SPEED, config.includeWindSpeed);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                OpenMeteoBindingConstants.CHANNEL_FORECAST_WIND_DIRECTION,
                DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_WIND_DIRECTION, config.includeWindDirection);

        return builder;
    }

    protected ThingBuilder initializeOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, String channelGroupId, String channelId, ChannelTypeUID channelTypeUID, boolean isActive,
            AutoUpdatePolicy autoUpdatePolicy, @Nullable String labelKey, @Nullable String descriptionKey) {
        ChannelUID channelUID = new ChannelUID(thing.getUID(), channelGroupId, channelId);
        ChannelBuilder channelBuilder = callback.createChannelBuilder(channelUID, channelTypeUID);

        channelBuilder.withAutoUpdatePolicy(autoUpdatePolicy);
        if (labelKey != null)
            channelBuilder = channelBuilder.withLabel(localization.getText(labelKey));
        if (descriptionKey != null)
            channelBuilder = channelBuilder.withDescription(localization.getText(descriptionKey));

        Channel channel = channelBuilder.build();

        builder = builder.withoutChannel(channelUID);

        return (isActive) ? builder.withChannel(channel) : builder;
    }

    protected ThingBuilder initializeOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, String channelGroupId, String channelId, ChannelTypeUID channelTypeUID, boolean isActive,
            AutoUpdatePolicy autoUpdatePolicy) {
        return initializeOptionalChannel(callback, builder, thingUID, channelGroupId, channelId, channelTypeUID,
                isActive, autoUpdatePolicy, null, null);
    }

    protected ThingBuilder initializeOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, String channelGroupId, String channelId, ChannelTypeUID channelTypeUID, boolean isActive,
            @Nullable String labelKey, @Nullable String descriptionKey) {
        return initializeOptionalChannel(callback, builder, thingUID, channelGroupId, channelId, channelTypeUID,
                isActive, AutoUpdatePolicy.DEFAULT, labelKey, descriptionKey);
    }

    protected ThingBuilder initializeOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, String channelGroupId, String channelId, ChannelTypeUID channelTypeUID,
            boolean isActive) {
        return initializeOptionalChannel(callback, builder, thingUID, channelGroupId, channelId, channelTypeUID,
                isActive, null, null);
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
                OpenMeteoBridgeHandler bridgeHandler = (OpenMeteoBridgeHandler) handler;

                boolean commandHandled = false;
                if (command instanceof RefreshType) {
                    // commandHandled = refreshChannel(channelUID, apiManager);
                } else {
                    // channelsInActionCommand.add(channelUID.getAsString());
                    // try {
                    // commandHandled = handleActionCommand(channelUID, command, apiManager);
                    // } finally {
                    // channelsInActionCommand.remove(channelUID.getAsString());
                    // }
                }

                if (!commandHandled)
                    bridgeHandler.handleCommand(channelUID, command);
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

        var location = this.location;
        if (location != null)
            forecastData = connection.getForecast(location, getForecastValues());

        return true;
    }

    private EnumSet<OpenMeteoConnection.ForecastValue> getForecastValues() {
        EnumSet<OpenMeteoConnection.ForecastValue> result = EnumSet.noneOf(OpenMeteoConnection.ForecastValue.class);
        OpenMeteoForecastThingConfiguration config = getConfigAs(OpenMeteoForecastThingConfiguration.class);

        if (config.includeTemperature)
            result.add(ForecastValue.TEMPERATURE);
        if (config.includePressure)
            result.add(ForecastValue.PRESSURE);
        if (config.includeHumidity)
            result.add(ForecastValue.HUMIDITY);
        if (config.includeWindSpeed)
            result.add(ForecastValue.WIND_SPEED);
        if (config.includeWindDirection)
            result.add(ForecastValue.WING_DIRECTION);

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
            case OpenMeteoBindingConstants.CHANNEL_GROUP_HOURLY_TIME_SERIES:
                updateHourlyTimeSeries(channelUID);
                break;
            default:
                break;
        }
    }

    private void updateHourlyTimeSeries(ChannelUID channelUID) {
        var forecastData = this.forecastData;
        if (forecastData != null) {
            var hourlyForecast = forecastData.hourly();
            if (hourlyForecast != null && hourlyForecast.variablesLength() > 0) {
                var variables = hourlyForecast.variablesVector();
                for (int variableIndex = 0; variableIndex < variables.length(); variableIndex++) {
                    var variable = variables.get(variableIndex);

                    logger.info("working on variable {}, length = {}", variableIndex, variable.valuesLength());
                }
            }
        }
    }
}
