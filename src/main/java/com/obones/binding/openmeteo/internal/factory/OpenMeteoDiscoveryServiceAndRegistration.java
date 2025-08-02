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
package com.obones.binding.openmeteo.internal.factory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.framework.ServiceRegistration;

import com.obones.binding.openmeteo.internal.discovery.OpenMeteoDiscoveryService;

/**
 * The {@link OpenMeteoDiscoveryServiceAndRegistration} stores both a discovery service and its associated registration.
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class OpenMeteoDiscoveryServiceAndRegistration {
    private OpenMeteoDiscoveryService discoveryService;
    private ServiceRegistration<?> discoveryServiceRegistration;

    public OpenMeteoDiscoveryServiceAndRegistration(OpenMeteoDiscoveryService discoveryService,
            ServiceRegistration<?> discoveryServiceRegistration) {
        this.discoveryService = discoveryService;
        this.discoveryServiceRegistration = discoveryServiceRegistration;
    }

    public OpenMeteoDiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    public ServiceRegistration<?> getDiscoveryServiceRegistration() {
        return discoveryServiceRegistration;
    }
}
