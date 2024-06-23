package com.obones.binding.openmeteo.internal.handler;

import static com.obones.binding.openmeteo.internal.OpenMeteoBindingConstants.*;

import java.util.EnumSet;
import java.util.Optional;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.dimension.Density;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.unit.Units;
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

import tech.units.indriya.unit.ProductUnit;

@NonNullByDefault
public class OpenMeteoAirQualityThingHandler extends OpenMeteoBaseThingHandler {
    private static Unit<Density> GRAINS_PER_CUBICMETRE = new ProductUnit<Density>(
            Units.ONE.divide(tech.units.indriya.unit.Units.CUBIC_METRE));

    public OpenMeteoAirQualityThingHandler(Thing thing, Localization localization,
            final TimeZoneProvider timeZoneProvider, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, localization, timeZoneProvider, channelTypeRegistry);
        logger.trace("OpenMeteoAirQualityThingHandler(thing={},localization={}) constructor called.", thing,
                localization);
    }

    protected void initializeChannels(ThingHandlerCallback callback, ThingBuilder builder, ThingUID thingUID) {
        OpenMeteoAirQualityThingConfiguration config = getConfigAs(OpenMeteoAirQualityThingConfiguration.class);

        if (config.hourlyTimeSeries) {
            String labelSuffix = localization
                    .getText("channel-type.openmeteo.air-quality.label-suffix.hourly.time-series");
            initializeHourlyGroupOptionalChannels(callback, builder, thingUID, config, CHANNEL_GROUP_HOURLY_TIME_SERIES,
                    labelSuffix);
        }

        if (config.current) {
            initializeCurrentGroupOptionalChannels(callback, builder, thingUID, config);
        }
    }

    protected ThingBuilder initializeHourlyGroupOptionalChannels(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, OpenMeteoAirQualityThingConfiguration config, String channelGroupId,
            String labelSuffix) {

        Object[] labelArguments = { labelSuffix };

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_UV_INDEX,
                CHANNEL_TYPE_UID_UV_INDEX, config.includeUVIndex, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_UV_INDEX_CLEAR_SKY,
                CHANNEL_TYPE_UID_UV_INDEX_CLEAR_SKY, config.includeUVIndexClearSky, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_PARTICULATE_10,
                CHANNEL_TYPE_UID_PARTICULATE_CONCENTRATION, config.includePM10, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_PARTICULATE_2_5,
                CHANNEL_TYPE_UID_PARTICULATE_CONCENTRATION, config.includePM2_5, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_CARBON_MONOXIDE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeCarbonMonoxide, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_NITROGEN_DIOXIDE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeNitrogenDioxide, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_SULPHUR_DIOXIDE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeSulphurDioxide, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_OZONE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeOzone, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_AMMONIA,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeAmmonia, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_AEROSOL_OPTICAL_DEPTH, CHANNEL_TYPE_UID_AEROSOL_OPTICAL_DEPTH,
                config.includeAerosolOpticalDepth, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_DUST,
                CHANNEL_TYPE_UID_DUST, config.includeDust, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_ALDER_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeAlderPollen, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_BIRCH_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeBirchPollen, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_GRASS_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeGrassPollen, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_MUGWORT_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeMugwortPollen, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_OLIVE_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeOlivePollen, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_RAGWEED_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeRagweedPollen, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_EUROPEAN_AQI,
                CHANNEL_TYPE_UID_EUROPEAN_AQI, config.includeEuropeanAqi, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_EUROPEAN_AQI_PM_10,
                CHANNEL_TYPE_UID_EUROPEAN_AQI, config.includeEuropeanAqiPM10, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_EUROPEAN_AQI_PM_2_5,
                CHANNEL_TYPE_UID_EUROPEAN_AQI, config.includeEuropeanAqiPM2_5, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_NITROGEN_DIOXIDE,
                CHANNEL_TYPE_UID_EUROPEAN_AQI, config.includeEuropeanAqiNitrogenDioxide, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_EUROPEAN_AQI_OZONE,
                CHANNEL_TYPE_UID_EUROPEAN_AQI, config.includeEuropeanAqiOzone, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_EUROPEAN_AQI_SULPHUR_DIOXIDE, CHANNEL_TYPE_UID_EUROPEAN_AQI,
                config.includeEuropeanAqiSulphurDioxide, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_US_AQI,
                CHANNEL_TYPE_UID_US_AQI, config.includeUSAqi, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_US_AQI_PM_10,
                CHANNEL_TYPE_UID_US_AQI, config.includeUSAqiPM10, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_US_AQI_PM_2_5,
                CHANNEL_TYPE_UID_US_AQI, config.includeUSAqiPM2_5, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_US_AQI_NITROGEN_DIOXIDE, CHANNEL_TYPE_UID_US_AQI,
                config.includeUSAqiNitrogenDioxide, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_US_AQI_OZONE,
                CHANNEL_TYPE_UID_US_AQI, config.includeUSAqiOzone, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_US_AQI_SULPHUR_DIOXIDE, CHANNEL_TYPE_UID_US_AQI, //
                config.includeUSAqiSulphurDioxide, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_US_AQI_CARBON_MONOXIDE, CHANNEL_TYPE_UID_US_AQI, //
                config.includeUSAqiCarbonMonoxide, labelArguments);

        return builder;
    }

    protected ThingBuilder initializeCurrentGroupOptionalChannels(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, OpenMeteoAirQualityThingConfiguration config) {

        Object[] labelArguments = { localization.getText("channel-type.openmeteo.air-quality.label-suffix.current") };
        String channelGroupId = CHANNEL_GROUP_CURRENT;

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_UV_INDEX,
                CHANNEL_TYPE_UID_UV_INDEX, config.includeUVIndex, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_UV_INDEX_CLEAR_SKY,
                CHANNEL_TYPE_UID_UV_INDEX_CLEAR_SKY, config.includeUVIndexClearSky, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_PARTICULATE_10,
                CHANNEL_TYPE_UID_PARTICULATE_CONCENTRATION, config.includePM10, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_PARTICULATE_2_5,
                CHANNEL_TYPE_UID_PARTICULATE_CONCENTRATION, config.includePM2_5, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_CARBON_MONOXIDE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeCarbonMonoxide, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_NITROGEN_DIOXIDE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeNitrogenDioxide, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_SULPHUR_DIOXIDE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeSulphurDioxide, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_OZONE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeOzone, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_AMMONIA,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeAmmonia, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_AEROSOL_OPTICAL_DEPTH, CHANNEL_TYPE_UID_AEROSOL_OPTICAL_DEPTH,
                config.includeAerosolOpticalDepth, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_DUST,
                CHANNEL_TYPE_UID_DUST, config.includeDust, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_ALDER_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeAlderPollen, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_BIRCH_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeBirchPollen, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_GRASS_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeGrassPollen, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_MUGWORT_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeMugwortPollen, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_OLIVE_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeOlivePollen, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_RAGWEED_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeRagweedPollen, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_EUROPEAN_AQI,
                CHANNEL_TYPE_UID_EUROPEAN_AQI, config.includeEuropeanAqi, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_US_AQI,
                CHANNEL_TYPE_UID_US_AQI, config.includeUSAqi, labelArguments);

        return builder;
    }

    protected void updateChannel(ChannelUID channelUID) {
        String channelGroupId = Optional.ofNullable(channelUID.getGroupId()).orElse("");
        logger.debug("OpenMeteoForecastThingHandler: updateChannel {}, groupID {}", channelUID, channelGroupId);

        switch (channelGroupId) {
            case CHANNEL_GROUP_HOURLY_TIME_SERIES:
                updateHourlyTimeSeries(channelUID);
                break;
            case CHANNEL_GROUP_CURRENT:
                updateCurrentChannel(channelUID);
                break;
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
            case CHANNEL_AIR_QUALITY_PARTICULATE_10 -> Variable.pm10;
            case CHANNEL_AIR_QUALITY_PARTICULATE_2_5 -> Variable.pm2p5;
            case CHANNEL_AIR_QUALITY_CARBON_MONOXIDE -> Variable.carbon_monoxide;
            case CHANNEL_AIR_QUALITY_NITROGEN_DIOXIDE -> Variable.nitrogen_dioxide;
            case CHANNEL_AIR_QUALITY_SULPHUR_DIOXIDE -> Variable.sulphur_dioxide;
            case CHANNEL_AIR_QUALITY_OZONE -> Variable.ozone;
            case CHANNEL_AIR_QUALITY_AEROSOL_OPTICAL_DEPTH -> Variable.aerosol_optical_depth;
            case CHANNEL_AIR_QUALITY_DUST -> Variable.dust;
            case CHANNEL_AIR_QUALITY_AMMONIA -> Variable.ammonia;
            case CHANNEL_AIR_QUALITY_ALDER_POLLEN -> Variable.alder_pollen;
            case CHANNEL_AIR_QUALITY_BIRCH_POLLEN -> Variable.birch_pollen;
            case CHANNEL_AIR_QUALITY_MUGWORT_POLLEN -> Variable.mugwort_pollen;
            case CHANNEL_AIR_QUALITY_GRASS_POLLEN -> Variable.grass_pollen;
            case CHANNEL_AIR_QUALITY_OLIVE_POLLEN -> Variable.olive_pollen;
            case CHANNEL_AIR_QUALITY_RAGWEED_POLLEN -> Variable.ragweed_pollen;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI -> Variable.european_aqi;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_PM_2_5 -> Variable.european_aqi_pm2p5;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_PM_10 -> Variable.european_aqi_pm10;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_NITROGEN_DIOXIDE -> Variable.european_aqi_nitrogen_dioxide;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_OZONE -> Variable.european_aqi_ozone;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_SULPHUR_DIOXIDE -> Variable.european_aqi_sulphur_dioxide;
            case CHANNEL_AIR_QUALITY_US_AQI -> Variable.us_aqi;
            case CHANNEL_AIR_QUALITY_US_AQI_PM_2_5 -> Variable.us_aqi_pm2p5;
            case CHANNEL_AIR_QUALITY_US_AQI_PM_10 -> Variable.us_aqi_pm10;
            case CHANNEL_AIR_QUALITY_US_AQI_NITROGEN_DIOXIDE -> Variable.us_aqi_nitrogen_dioxide;
            case CHANNEL_AIR_QUALITY_US_AQI_OZONE -> Variable.us_aqi_ozone;
            case CHANNEL_AIR_QUALITY_US_AQI_SULPHUR_DIOXIDE -> Variable.us_aqi_sulphur_dioxide;
            case CHANNEL_AIR_QUALITY_US_AQI_CARBON_MONOXIDE -> Variable.us_aqi_carbon_monoxide;
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
            case CHANNEL_AIR_QUALITY_PARTICULATE_10:
                state = getQuantityTypeState(floatValue, Units.MICROGRAM_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_PARTICULATE_2_5:
                state = getQuantityTypeState(floatValue, Units.MICROGRAM_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_CARBON_MONOXIDE:
                state = getQuantityTypeState(floatValue, Units.MICROGRAM_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_NITROGEN_DIOXIDE:
                state = getQuantityTypeState(floatValue, Units.MICROGRAM_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_SULPHUR_DIOXIDE:
                state = getQuantityTypeState(floatValue, Units.MICROGRAM_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_OZONE:
                state = getQuantityTypeState(floatValue, Units.MICROGRAM_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_AEROSOL_OPTICAL_DEPTH:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_DUST:
                state = getQuantityTypeState(floatValue, Units.MICROGRAM_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_AMMONIA:
                state = getQuantityTypeState(floatValue, Units.MICROGRAM_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_ALDER_POLLEN:
                state = getQuantityTypeState(floatValue, GRAINS_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_BIRCH_POLLEN:
                state = getQuantityTypeState(floatValue, GRAINS_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_MUGWORT_POLLEN:
                state = getQuantityTypeState(floatValue, GRAINS_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_GRASS_POLLEN:
                state = getQuantityTypeState(floatValue, GRAINS_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_OLIVE_POLLEN:
                state = getQuantityTypeState(floatValue, GRAINS_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_RAGWEED_POLLEN:
                state = getQuantityTypeState(floatValue, GRAINS_PER_CUBICMETRE);
                break;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_PM_2_5:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_PM_10:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_NITROGEN_DIOXIDE:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_OZONE:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_SULPHUR_DIOXIDE:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_US_AQI:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_US_AQI_PM_2_5:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_US_AQI_PM_10:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_US_AQI_NITROGEN_DIOXIDE:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_US_AQI_OZONE:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_US_AQI_SULPHUR_DIOXIDE:
                state = getDecimalTypeState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_US_AQI_CARBON_MONOXIDE:
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
