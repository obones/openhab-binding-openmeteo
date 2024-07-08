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
package com.obones.binding.openmeteo.internal.profiles;

import static com.obones.binding.openmeteo.internal.OpenMeteoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.transformation.OpenMeteoUSAirQualityIndicatorTransformationService;
import com.obones.binding.openmeteo.internal.utils.Localization;

/**
 * Profile to offer the OpenMeteOUSAirQualityTransformationService on an ItemChannelLink
 *
 * @author Olivier Sannier - Initial contribution
 *
 */
@NonNullByDefault
public class OpenMeteoUSAirQualityIndicatorTransformationProfile
        extends OpenMeteoBaseAirQualityIndicatorTransformProfile {

    private final Logger logger = LoggerFactory
            .getLogger(OpenMeteoEuropeanAirQualityIndicatorTransformationProfile.class);

    public OpenMeteoUSAirQualityIndicatorTransformationProfile(ProfileCallback callback, ProfileContext context,
            OpenMeteoUSAirQualityIndicatorTransformationService service, Localization localization) {
        super(callback, context, service, localization);

        logger.debug("{} Profile configured",
                OpenMeteoEuropeanAirQualityIndicatorTransformationProfile.class.getName());
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return US_AQI_PROFILE_TYPE_UID;
    }

    @Override
    protected String getHumanReadableStringPrefix() {
        return "channel-type.openmeteo.air-quality.us-aqi-as-string.";
    }
}
