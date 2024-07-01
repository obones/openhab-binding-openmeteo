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
package com.obones.binding.openmeteo.internal.transformation;

import static com.obones.binding.openmeteo.internal.OpenMeteoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.handler.OpenMeteoBridgeHandler;

/***
 * The{@link USAirQualityIndicatorTransformationService} is responsible for providing a transformation from
 * US Air Quality indicator as number to a string representation of various air quality levels
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
@Component(property = { "openhab.transform=OPENMETEO_US_AQI" })
public class USAirQualityIndicatorTransformationService implements TransformationService {

    protected @NonNullByDefault({}) final Logger logger = LoggerFactory
            .getLogger(EuropeanAirQualityIndicatorTransformationService.class);

    @Override
    public @Nullable String transform(String config, String value) throws TransformationException {
        logger.debug("about to transform '{}' by '{}'", value, OpenMeteoBridgeHandler.class.getName());

        try {
            double numberValue = Double.parseDouble(value);

            return transform(numberValue);

        } catch (NumberFormatException e) {
            throw new TransformationException("The given value is not a number", e);
        }
    }

    public static String transform(double value) throws TransformationException {
        if (value < 0)
            throw new TransformationException("A negative value is not supported");
        if (value < 50)
            return CHANNEL_AIR_QUALITY_AQI_LEVEL_GOOD;
        if (value < 100)
            return CHANNEL_AIR_QUALITY_AQI_LEVEL_MODERATE;
        if (value < 150)
            return CHANNEL_AIR_QUALITY_AQI_LEVEL_UNHEALTHY_FOR_SENSITIVE_GROUPS;
        if (value < 200)
            return CHANNEL_AIR_QUALITY_AQI_LEVEL_UNHEALTHY;
        if (value < 300)
            return CHANNEL_AIR_QUALITY_AQI_LEVEL_VERY_UNHEALTHY;

        return CHANNEL_AIR_QUALITY_AQI_LEVEL_HAZARDOUS;
    }
}
