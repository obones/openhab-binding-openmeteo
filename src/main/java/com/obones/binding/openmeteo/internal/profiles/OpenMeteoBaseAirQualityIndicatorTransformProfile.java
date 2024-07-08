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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.config.OpenMeteoAirQualityIndicatorProfileConfiguration;
import com.obones.binding.openmeteo.internal.utils.Localization;

/**
 * Base class for profiles that offer Air Quality transformation service on an ItemChannelLink
 *
 * @author Olivier Sannier - Initial contribution
 *
 */
@NonNullByDefault
public abstract class OpenMeteoBaseAirQualityIndicatorTransformProfile implements StateProfile {

    private final Logger logger = LoggerFactory
            .getLogger(OpenMeteoEuropeanAirQualityIndicatorTransformationProfile.class);

    protected final TransformationService service;
    protected final ProfileCallback callback;
    protected final ProfileContext context;
    protected final Localization localization;

    public OpenMeteoBaseAirQualityIndicatorTransformProfile(ProfileCallback callback, ProfileContext context,
            TransformationService service, Localization localization) {

        this.service = service;
        this.callback = callback;
        this.context = context;
        this.localization = localization;
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

    protected abstract String getHumanReadableStringPrefix();

    private String getStateAsHumanReadableString(String state) {
        return localization.getText(getHumanReadableStringPrefix() + state.toLowerCase().replace("_", "-"));
    }

    private Type transformState(Type state) {
        var config = context.getConfiguration().as(OpenMeteoAirQualityIndicatorProfileConfiguration.class);

        @Nullable // transform has Nullable annotation on its return type
        String result = state.toFullString();
        try {
            result = service.transform("", result);

            if ((result != null) && !config.stateAsOption)
                result = getStateAsHumanReadableString(result);
        } catch (TransformationException e) {
            logger.warn("Could not transform state '{}': {}", state, e.getMessage());
        }
        StringType resultType = new StringType(result);
        logger.debug("Transformed '{}' into '{}'", state, resultType);
        return resultType;
    }
}
