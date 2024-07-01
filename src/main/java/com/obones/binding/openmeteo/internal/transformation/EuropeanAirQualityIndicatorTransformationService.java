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
@Component(property = { "openhab.transform=OPENMETEO_EUROPEAN_AQI" })
public class EuropeanAirQualityIndicatorTransformationService implements TransformationService {
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
