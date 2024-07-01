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
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.transformation.OpenMeteoEuropeanAirQualityIndicatorTransformationService;

/**
 * Profile to offer the OpenMeteoEuropeanAirQualityTransformationService on an ItemChannelLink
 *
 * @author Olivier Sannier - Initial contribution
 *
 */
@NonNullByDefault
public class OpenMeteoEuropeanAirQualityIndicatorTransformationProfile implements StateProfile {

    private final Logger logger = LoggerFactory
            .getLogger(OpenMeteoEuropeanAirQualityIndicatorTransformationProfile.class);

    private final TransformationService service;
    private final ProfileCallback callback;

    public OpenMeteoEuropeanAirQualityIndicatorTransformationProfile(ProfileCallback callback, ProfileContext context,
            OpenMeteoEuropeanAirQualityIndicatorTransformationService service) {

        this.service = service;
        this.callback = callback;

        logger.debug("{} Profile configured",
                OpenMeteoEuropeanAirQualityIndicatorTransformationProfile.class.getName());
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return EUROPEAN_AQI_PROFILE_TYPE_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
    }

    @Override
    public void onCommandFromItem(Command command) {
        callback.handleCommand(command);
    }

    @Override
    public void onCommandFromHandler(Command command) {
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        callback.sendUpdate((State) transformState(state));
    }

    private Type transformState(Type state) {
        String result = state.toFullString();
        try {
            result = TransformationHelper.transform(service, "", "", state.toFullString());
        } catch (TransformationException e) {
            logger.warn("Could not transform state '{}' with function '{}' and format '{}'", state, "", "");
        }
        StringType resultType = new StringType(result);
        logger.debug("Transformed '{}' into '{}'", state, resultType);
        return resultType;
    }
}
