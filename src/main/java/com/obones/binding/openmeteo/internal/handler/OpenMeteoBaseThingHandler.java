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

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Map.Entry;

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

import com.obones.binding.openmeteo.internal.config.OpenMeteoBaseThingConfiguration;
import com.obones.binding.openmeteo.internal.connection.OpenMeteoConnection;
import com.obones.binding.openmeteo.internal.utils.Localization;
import com.openmeteo.sdk.Aggregation;
import com.openmeteo.sdk.VariableWithValues;
import com.openmeteo.sdk.VariablesSearch;
import com.openmeteo.sdk.VariablesWithTime;
import com.openmeteo.sdk.WeatherApiResponse;

@NonNullByDefault
public abstract class OpenMeteoBaseThingHandler extends BaseThingHandler {

    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(OpenMeteoBridgeHandler.class);
    private @Nullable ChannelTypeRegistry channelTypeRegistry;
    private @Nullable WeatherApiResponse forecastData = null;
    private final TimeZoneProvider timeZoneProvider;

    public Localization localization;

    protected @Nullable PointType location;

    public OpenMeteoBaseThingHandler(Thing thing, Localization localization, final TimeZoneProvider timeZoneProvider,
            ChannelTypeRegistry channelTypeRegistry) {
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
            OpenMeteoBaseThingConfiguration config = getConfigAs(OpenMeteoBaseThingConfiguration.class);
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

    protected abstract void initializeChannels(ThingHandlerCallback callback, ThingBuilder builder, ThingUID thingUID);

    protected void initializeChannels(OpenMeteoBridgeHandler bridgeHandler) {
        ThingHandlerCallback callback = getCallback();
        if (callback == null) {
            logger.warn("initializeOptionalChannels: Could not get callback.");
            return;
        }

        ThingBuilder builder = editThing();
        ThingUID thingUID = thing.getUID();

        // Remove every channel and rebuild only the required ones, this makes for easier to read code
        // and has no impact until the build() method is called
        builder.withoutChannels(thing.getChannels());

        initializeChannels(callback, builder, thingUID);

        updateThing(builder.build());
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

    protected abstract WeatherApiResponse requestData(OpenMeteoConnection connection, PointType location)
            throws CommunicationException, ConfigurationException;

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
            forecastData = requestData(connection, location);

        return true;
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
    protected abstract void updateChannel(ChannelUID channelUID);

    protected void updateForecastTimeSeries(ChannelUID channelUID, @Nullable VariablesWithTime forecast) {
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

    protected abstract int getVariableIndex(String channelId);

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

            int variable = getVariableIndex(channelId.toString());

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

    protected abstract State getForecastState(String channelId, @Nullable Float floatValue, @Nullable Long longValue);

    private State getForecastState(String channelId, VariableWithValues values, @Nullable Integer valueIndex) {
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

        return getForecastState(channelId, floatValue, longValue);
    }
}
