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

    // List of all Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, THING_OPENMETEO_BRIDGE);

    // List of all Thing Type UIDs beyond the bridge(s)
    public static final ThingTypeUID THING_TYPE_OPENMETEO_FORECAST = new ThingTypeUID(BINDING_ID,
            THING_OPENMETEO_FORECAST);

    // Definitions of different set of Things
    public static final Set<ThingTypeUID> SUPPORTED_THINGS_BRIDGE = new HashSet<>(Arrays.asList(THING_TYPE_BRIDGE));
    public static final Set<ThingTypeUID> SUPPORTED_THINGS_ITEMS = new HashSet<>(
            Arrays.asList(THING_TYPE_OPENMETEO_FORECAST));

    // List of all bridge channel ids

    /** Channel/Property identifier describing the current Bridge State. */
    public static final String PROPERTY_BRIDGE_API_VERSION = "apiVersion";

    // List of all forecast channel group ids
    public static final String CHANNEL_GROUP_HOURLY_TIME_SERIES = "forecastHourly";
    public static final String CHANNEL_GROUP_HOURLY_PREFIX = "forecastHours";

    // List of all forecast channel/property ids
    public static final String CHANNEL_FORECAST_TEMPERATURE = "temperature";
    public static final String CHANNEL_FORECAST_PRESSURE = "pressure";
    public static final String CHANNEL_FORECAST_HUMIDITY = "humidity";
    public static final String CHANNEL_FORECAST_WIND_SPEED = "wind-speed";
    public static final String CHANNEL_FORECAST_WIND_DIRECTION = "wind-direction";
}
