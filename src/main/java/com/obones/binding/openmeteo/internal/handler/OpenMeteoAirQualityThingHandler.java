package com.obones.binding.openmeteo.internal.handler;

import static com.obones.binding.openmeteo.internal.OpenMeteoBindingConstants.*;

import java.util.EnumSet;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.obones.binding.openmeteo.internal.config.OpenMeteoAirQualityThingConfiguration;
import com.obones.binding.openmeteo.internal.connection.OpenMeteoConnection;
import com.obones.binding.openmeteo.internal.connection.OpenMeteoConnection.AirQualityValue;
import com.obones.binding.openmeteo.internal.utils.Localization;
import com.openmeteo.sdk.Variable;
import com.openmeteo.sdk.WeatherApiResponse;

@NonNullByDefault
public class OpenMeteoAirQualityThingHandler extends OpenMeteoBaseThingHandler {

    public OpenMeteoAirQualityThingHandler(Thing thing, Localization localization,
            final TimeZoneProvider timeZoneProvider, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, localization, timeZoneProvider, channelTypeRegistry);
        logger.trace("OpenMeteoAirQualityThingHandler(thing={},localization={}) constructor called.", thing,
                localization);
    }

    protected void initializeChannels(ThingHandlerCallback callback, ThingBuilder builder, ThingUID thingUID) {
        OpenMeteoAirQualityThingConfiguration config = getConfigAs(OpenMeteoAirQualityThingConfiguration.class);
    }

    protected void updateChannel(ChannelUID channelUID) {
        String channelGroupId = Optional.ofNullable(channelUID.getGroupId()).orElse("");
        logger.debug("OpenMeteoForecastThingHandler: updateChannel {}, groupID {}", channelUID, channelGroupId);

        switch (channelGroupId) {
        }
    }

    protected WeatherApiResponse requestData(OpenMeteoConnection connection, PointType location)
            throws CommunicationException, ConfigurationException {
        OpenMeteoAirQualityThingConfiguration config = getConfigAs(OpenMeteoAirQualityThingConfiguration.class);

        return connection.getAirQuality(location, getAirQualityValues(),
                (config.hourlyTimeSeries) ? config.hourlyHours : null, //
                config.current);
    }

    private EnumSet<OpenMeteoConnection.AirQualityValue> getAirQualityValues() {
        EnumSet<OpenMeteoConnection.AirQualityValue> result = EnumSet.noneOf(OpenMeteoConnection.AirQualityValue.class);
        OpenMeteoAirQualityThingConfiguration config = getConfigAs(OpenMeteoAirQualityThingConfiguration.class);

        if (config.includeUVIndex)
            result.add(AirQualityValue.UV_INDEX);
        if (config.includeUVIndexClearSky)
            result.add(AirQualityValue.UV_INDEX_CLEAR_SKY);
        if (config.includePM10)
            result.add(AirQualityValue.PARTICULATE_10);
        if (config.includePM2_5)
            result.add(AirQualityValue.PARTICULATE_2_5);
        if (config.includeCarbonMonoxide)
            result.add(AirQualityValue.CARBON_MONOXIDE);
        if (config.includeNitrogenDioxide)
            result.add(AirQualityValue.NITROGEN_DIOXIDE);
        if (config.includeSulphurDioxide)
            result.add(AirQualityValue.SULPHUR_DIOXIDE);
        if (config.includeOzone)
            result.add(AirQualityValue.OZONE);
        if (config.includeAerosolOpticalDepth)
            result.add(AirQualityValue.AEROSOL_OPTICAL_DEPTH);
        if (config.includeDust)
            result.add(AirQualityValue.DUST);
        if (config.includeAmmonia)
            result.add(AirQualityValue.AMMONIA);
        if (config.includeAlderPollen)
            result.add(AirQualityValue.ALDER_POLLEN);
        if (config.includeBirchPollen)
            result.add(AirQualityValue.BIRCH_POLLEN);
        if (config.includeMugwortPollen)
            result.add(AirQualityValue.MUGWORT_POLLEN);
        if (config.includeGrassPollen)
            result.add(AirQualityValue.GRASS_POLLEN);
        if (config.includeOlivePollen)
            result.add(AirQualityValue.OLIVE_POLLEN);
        if (config.includeRagweedPollen)
            result.add(AirQualityValue.RAGWEED_POLLEN);
        if (config.includeEuropeanAqi)
            result.add(AirQualityValue.EUROPEAN_AQI);
        if (config.includeEuropeanAqiPM2_5)
            result.add(AirQualityValue.EUROPEAN_AQI_PM_2_5);
        if (config.includeEuropeanAqiPM10)
            result.add(AirQualityValue.EUROPEAN_AQI_PM_10);
        if (config.includeEuropeanAqiNitrogenDioxide)
            result.add(AirQualityValue.EUROPEAN_AQI_NITROGEN_DIOXIDE);
        if (config.includeEuropeanAqiOzone)
            result.add(AirQualityValue.EUROPEAN_AQI_OZONE);
        if (config.includeEuropeanAqiSulphurDioxide)
            result.add(AirQualityValue.EUROPEAN_AQI_SULPHUR_DIOXIDE);
        if (config.includeUSAqi)
            result.add(AirQualityValue.US_AQI);
        if (config.includeUSAqiPM2_5)
            result.add(AirQualityValue.US_AQI_PM_2_5);
        if (config.includeUSAqiPM10)
            result.add(AirQualityValue.US_AQI_PM_10);
        if (config.includeUSAqiNitrogenDioxide)
            result.add(AirQualityValue.US_AQI_NITROGEN_DIOXIDE);
        if (config.includeUSAqiOzone)
            result.add(AirQualityValue.US_AQI_OZONE);
        if (config.includeUSAqiSulphurDioxide)
            result.add(AirQualityValue.US_AQI_SULPHUR_DIOXIDE);
        if (config.includeUSAqiCarbonMonoxide)
            result.add(AirQualityValue.US_AQI_CARBON_MONOXIDE);

        return result;
    }

    protected int getVariableIndex(String channelId) {
        return switch (channelId.toString()) {
            case CHANNEL_AIR_QUALITY_UV_INDEX -> Variable.uv_index;
            case CHANNEL_AIR_QUALITY_UV_INDEX_CLEAR_SKY -> Variable.uv_index_clear_sky;
            default -> Variable.undefined;
        };
    }

    protected State getForecastState(String channelId, @Nullable Float floatValue, @Nullable Long longValue) {
        State state = UnDefType.UNDEF;

        switch (channelId) {
            case CHANNEL_AIR_QUALITY_UV_INDEX:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_UV_INDEX_CLEAR_SKY:
                state = getDecimalTypeState(floatValue);
                break;
            default:
                // This should not happen
                logger.warn("Unknown channel id {} in weather data", channelId);
                break;
        }
        return state;
    }
}
