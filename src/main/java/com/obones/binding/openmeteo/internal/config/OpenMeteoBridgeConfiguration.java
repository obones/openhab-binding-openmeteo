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
package com.obones.binding.openmeteo.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class OpenMeteoBridgeConfiguration {

    /*
     * Default values - should not be modified
     */
    public String baseURI = "https://api.open-meteo.com/v1/";
    public String APIKey = "";
    public int timeoutMsecs = 3000; // three seconds
    public int retries = 5;
}
