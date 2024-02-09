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

    // List of all Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, THING_OPENMETEO_BRIDGE);

    // Definitions of different set of Things
    public static final Set<ThingTypeUID> SUPPORTED_THINGS_BRIDGE = new HashSet<>(Arrays.asList(THING_TYPE_BRIDGE));

    // List of all bridge channel ids

    /** Channel/Property identifier describing the current Bridge State. */
    public static final String PROPERTY_BRIDGE_API_VERSION = "apiVersion";
}
