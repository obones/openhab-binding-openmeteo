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

/***
 * The{@link OpenMeteoEuropeanAirQualityIndicatorTransformationService} is responsible for providing a transformation
 * from European Air Quality indicator as number to a string representation of various air quality levels
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
@Component(service = OpenMeteoEuropeanAirQualityIndicatorTransformationService.class)
public class OpenMeteoEuropeanAirQualityIndicatorTransformationService implements TransformationService {
    protected @NonNullByDefault({}) final Logger logger = LoggerFactory
            .getLogger(OpenMeteoEuropeanAirQualityIndicatorTransformationService.class);

    @Override
    public @Nullable String transform(String config, String value) throws TransformationException {
        logger.debug("about to transform '{}' by '{}'", value,
                OpenMeteoEuropeanAirQualityIndicatorTransformationService.class.getName());

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
        if (value < 20)
            return CHANNEL_AIR_QUALITY_AQI_LEVEL_GOOD;
        if (value < 40)
            return CHANNEL_AIR_QUALITY_AQI_LEVEL_FAIR;
        if (value < 60)
            return CHANNEL_AIR_QUALITY_AQI_LEVEL_MODERATE;
        if (value < 80)
            return CHANNEL_AIR_QUALITY_AQI_LEVEL_POOR;
        if (value < 100)
            return CHANNEL_AIR_QUALITY_AQI_LEVEL_VERY_POOR;

        return CHANNEL_AIR_QUALITY_AQI_LEVEL_EXTREMELY_POOR;
    }
}
