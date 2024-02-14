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

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
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
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.config.OpenMeteoForecastThingConfiguration;
import com.obones.binding.openmeteo.internal.utils.Localization;

@NonNullByDefault
public class OpenMeteoForecastThingHandler extends BaseThingHandler {

    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(OpenMeteoBridgeHandler.class);

    public Localization localization;

    public OpenMeteoForecastThingHandler(Thing thing, Localization localization) {
        super(thing);
        this.localization = localization;
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
            logger.trace("initialize() updating ThingStatus to ONLINE.");
            createOptionalChannels();
            initializeProperties();
            updateStatus(ThingStatus.ONLINE);
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

    protected synchronized void createOptionalChannels() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            OpenMeteoBridgeHandler bridgeHandler = (OpenMeteoBridgeHandler) bridge.getHandler();
            if (bridgeHandler == null) {
                logger.warn("createOptionalChannels: Could not get bridge handler");
                return;
            }

            createOptionalChannels(bridgeHandler);
        }
    }

    protected void createOptionalChannels(OpenMeteoBridgeHandler bridgeHandler) {
    }

    protected ThingBuilder createOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder, ThingUID thingUID,
            String ChannelId, ChannelTypeUID channelTypeUID, AutoUpdatePolicy autoUpdatePolicy,
            @Nullable String labelKey, @Nullable String descriptionKey) {
        ChannelUID channelUID = new ChannelUID(thing.getUID(), ChannelId);
        ChannelBuilder channelBuilder = callback.createChannelBuilder(channelUID, channelTypeUID);

        channelBuilder.withAutoUpdatePolicy(autoUpdatePolicy);
        if (labelKey != null)
            channelBuilder = channelBuilder.withLabel(localization.getText(labelKey));
        if (descriptionKey != null)
            channelBuilder = channelBuilder.withDescription(localization.getText(descriptionKey));

        Channel channel = channelBuilder.build();

        // Add channel (remove it beforehand to avoid duplicates, it's a no-op if it did not exist)
        return builder.withoutChannel(channelUID).withChannel(channel);
    }

    protected ThingBuilder createOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder, ThingUID thingUID,
            String ChannelId, ChannelTypeUID channelTypeUID, AutoUpdatePolicy autoUpdatePolicy) {
        return createOptionalChannel(callback, builder, thingUID, ChannelId, channelTypeUID, autoUpdatePolicy, null,
                null);
    }

    protected ThingBuilder createOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder, ThingUID thingUID,
            String ChannelId, ChannelTypeUID channelTypeUID, @Nullable String labelKey,
            @Nullable String descriptionKey) {
        return createOptionalChannel(callback, builder, thingUID, ChannelId, channelTypeUID, AutoUpdatePolicy.DEFAULT,
                labelKey, descriptionKey);
    }

    protected ThingBuilder createOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder, ThingUID thingUID,
            String ChannelId, ChannelTypeUID channelTypeUID) {
        return createOptionalChannel(callback, builder, thingUID, ChannelId, channelTypeUID, null, null);
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
}