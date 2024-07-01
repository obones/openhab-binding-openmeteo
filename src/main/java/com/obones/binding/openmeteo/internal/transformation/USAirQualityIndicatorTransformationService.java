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
