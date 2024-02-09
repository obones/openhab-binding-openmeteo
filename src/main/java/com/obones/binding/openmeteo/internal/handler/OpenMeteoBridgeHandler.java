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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.utils.Localization;

/**
 * <B>Common interaction with the </B><I>OpenMeteo</I><B> bridge.</B>
 * <P>
 * It implements the communication between <B>OpenHAB</B> and the <I>OpenMeteo</I> Bridge:
 * <UL>
 * <LI><B>OpenHAB</B> Event Bus &rarr; <I>OpenMeteo</I> <B>bridge</B>
 * <P>
 * Sending commands and value updates.</LI>
 * </UL>
 * <UL>
 * <LI><I>OpenMeteo</I> <B>bridge</B> &rarr; <B>OpenHAB</B>:
 * <P>
 * Retrieving information by sending a Refresh command.</LI>
 * </UL>
 * <P>
 * Entry point for this class is the method
 * {@link OpenMeteoBridgeHandler#handleCommand handleCommand}.
 *
 * @author Olivier Sannier - Initial contribution.
 */
public class OpenMeteoBridgeHandler extends BaseBridgeHandler {

    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(OpenMeteoBridgeHandler.class);

    public Localization localization;

    /*
     * ************************
     * ***** Constructors *****
     */

    public OpenMeteoBridgeHandler(final Bridge bridge, Localization localization) {
        super(bridge);
        logger.trace("OpenMeteoBridgeHandler(constructor with bridge={}, localization={}) called.", bridge,
                localization);
        this.localization = localization;
        logger.debug("Creating a OpenMeteoBridgeHandler for thing '{}'.", getThing().getUID());
    }

    // Provisioning/Deprovisioning methods *****

    @Override
    public void initialize() {
        // set the thing status to UNKNOWN temporarily and let the background task decide the real status
        updateStatus(ThingStatus.UNKNOWN);

        // take care of unusual situations...
        if (scheduler.isShutdown()) {
            logger.warn("initialize(): scheduler is shutdown, aborting initialization.");
            return;
        }

        logger.trace("initialize(): initialize bridge configuration parameters.");
    }

    /**
     * NOTE: It takes care by calling {@link #handleCommand} with the REFRESH command, that every used channel is
     * initialized.
     */
    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (thing.getStatus() == ThingStatus.ONLINE) {
            logger.trace("channelLinked({}) refreshing channel value with help of handleCommand as Thing is online.",
                    channelUID.getAsString());
            handleCommand(channelUID, RefreshType.REFRESH);
        } else {
            logger.trace("channelLinked({}) doing nothing as Thing is not online.", channelUID.getAsString());
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        logger.trace("channelUnlinked({}) called.", channelUID.getAsString());
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand({}): command {} on channel {} will be scheduled.", Thread.currentThread(), command,
                channelUID.getAsString());
        logger.debug("handleCommand({},{}) called.", channelUID.getAsString(), command);

        logger.trace("handleCommand({}) done.", Thread.currentThread());
    }
}
