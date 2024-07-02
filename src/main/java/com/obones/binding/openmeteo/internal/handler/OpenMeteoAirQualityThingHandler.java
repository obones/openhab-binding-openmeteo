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
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.obones.binding.openmeteo.internal.config.OpenMeteoAirQualityThingConfiguration;
import com.obones.binding.openmeteo.internal.connection.OpenMeteoConnection;
import com.obones.binding.openmeteo.internal.connection.OpenMeteoConnection.AirQualityValue;
import com.obones.binding.openmeteo.internal.transformation.OpenMeteoEuropeanAirQualityIndicatorTransformationService;
import com.obones.binding.openmeteo.internal.transformation.OpenMeteoUSAirQualityIndicatorTransformationService;
import com.obones.binding.openmeteo.internal.utils.Localization;
import com.openmeteo.sdk.Variable;
import com.openmeteo.sdk.WeatherApiResponse;

/***
 * The{@link OpenMeteoAirQualityThingHandler} is responsible for updating air quality related channels, which are
 * retrieved via {@link OpenMeteoBridgeHandler}.
 *
 * @author Olivier Sannier - Initial contribution
 */
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
                CHANNEL_TYPE_UID_PARTICULATE_CONCENTRATION, config.includePM10, //
                "channel.openmeteo.air-quality.particulate-10.label", //
                "channel.openmeteo.air-quality.particulate-10.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_PARTICULATE_2_5,
                CHANNEL_TYPE_UID_PARTICULATE_CONCENTRATION, config.includePM2_5, //
                "channel.openmeteo.air-quality.particulate-2_5.label", //
                "channel.openmeteo.air-quality.particulate-2_5.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_CARBON_MONOXIDE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeCarbonMonoxide, //
                "channel.openmeteo.air-quality.carbon-monoxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_NITROGEN_DIOXIDE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeNitrogenDioxide, //
                "channel.openmeteo.air-quality.nitrogen-dioxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_SULPHUR_DIOXIDE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeSulphurDioxide, //
                "channel.openmeteo.air-quality.sulphur-dioxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_OZONE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeOzone, //
                "channel.openmeteo.air-quality.ozone.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_AMMONIA,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeAmmonia, //
                "channel.openmeteo.air-quality.ammonia.label", "channel.openmeteo.air-quality.ammonia.description",
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_AEROSOL_OPTICAL_DEPTH, CHANNEL_TYPE_UID_AEROSOL_OPTICAL_DEPTH,
                config.includeAerosolOpticalDepth, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_DUST,
                CHANNEL_TYPE_UID_DUST, config.includeDust, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_ALDER_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeAlderPollen, //
                "channel.openmeteo.air-quality.alder-pollen.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_BIRCH_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeBirchPollen, //
                "channel.openmeteo.air-quality.birch-pollen.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_GRASS_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeGrassPollen, //
                "channel.openmeteo.air-quality.grass-pollen.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_MUGWORT_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeMugwortPollen, //
                "channel.openmeteo.air-quality.mugwort-pollen.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_OLIVE_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeOlivePollen, //
                "channel.openmeteo.air-quality.olive-pollen.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_RAGWEED_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeRagweedPollen, //
                "channel.openmeteo.air-quality.ragweed-pollen.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_EUROPEAN_AQI,
                CHANNEL_TYPE_UID_EUROPEAN_AQI, config.includeEuropeanAqi && config.airQualityIndicatorsAsNumber,
                labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_EUROPEAN_AQI_PM_10,
                CHANNEL_TYPE_UID_EUROPEAN_AQI, config.includeEuropeanAqiPM10 && config.airQualityIndicatorsAsNumber, //
                "channel.openmeteo.air-quality.european-aqi-pm10.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_EUROPEAN_AQI_PM_2_5,
                CHANNEL_TYPE_UID_EUROPEAN_AQI, config.includeEuropeanAqiPM2_5 && config.airQualityIndicatorsAsNumber, //
                "channel.openmeteo.air-quality.european-aqi-pm2_5.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_EUROPEAN_AQI_NITROGEN_DIOXIDE, CHANNEL_TYPE_UID_EUROPEAN_AQI,
                config.includeEuropeanAqiNitrogenDioxide && config.airQualityIndicatorsAsNumber, //
                "channel.openmeteo.air-quality.european-aqi-nitrogen-dioxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_EUROPEAN_AQI_OZONE,
                CHANNEL_TYPE_UID_EUROPEAN_AQI, config.includeEuropeanAqiOzone && config.airQualityIndicatorsAsNumber, //
                "channel.openmeteo.air-quality.european-aqi-ozone.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_EUROPEAN_AQI_SULPHUR_DIOXIDE, CHANNEL_TYPE_UID_EUROPEAN_AQI,
                config.includeEuropeanAqiSulphurDioxide && config.airQualityIndicatorsAsNumber, //
                "channel.openmeteo.air-quality.european-aqi-sulphur-dioxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_EUROPEAN_AQI_AS_STRING, CHANNEL_TYPE_UID_EUROPEAN_AQI_AS_STRING,
                config.includeEuropeanAqi && config.airQualityIndicatorsAsString, //
                labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_EUROPEAN_AQI_AS_STRING_PM_10, CHANNEL_TYPE_UID_EUROPEAN_AQI_AS_STRING,
                config.includeEuropeanAqiPM10 && config.airQualityIndicatorsAsString, //
                "channel.openmeteo.air-quality.european-aqi-pm10.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_EUROPEAN_AQI_AS_STRING_PM_2_5, CHANNEL_TYPE_UID_EUROPEAN_AQI_AS_STRING,
                config.includeEuropeanAqiPM2_5 && config.airQualityIndicatorsAsString, //
                "channel.openmeteo.air-quality.european-aqi-pm2_5.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_EUROPEAN_AQI_AS_STRING_NITROGEN_DIOXIDE, CHANNEL_TYPE_UID_EUROPEAN_AQI_AS_STRING,
                config.includeEuropeanAqiNitrogenDioxide && config.airQualityIndicatorsAsString, //
                "channel.openmeteo.air-quality.european-aqi-nitrogen-dioxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_EUROPEAN_AQI_AS_STRING_OZONE, CHANNEL_TYPE_UID_EUROPEAN_AQI_AS_STRING,
                config.includeEuropeanAqiOzone && config.airQualityIndicatorsAsString, //
                "channel.openmeteo.air-quality.european-aqi-ozone.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_EUROPEAN_AQI_AS_STRING_SULPHUR_DIOXIDE, CHANNEL_TYPE_UID_EUROPEAN_AQI_AS_STRING,
                config.includeEuropeanAqiSulphurDioxide && config.airQualityIndicatorsAsString, //
                "channel.openmeteo.air-quality.european-aqi-sulphur-dioxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_US_AQI,
                CHANNEL_TYPE_UID_US_AQI, config.includeUSAqi && config.airQualityIndicatorsAsNumber, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_US_AQI_PM_10,
                CHANNEL_TYPE_UID_US_AQI, config.includeUSAqiPM10 && config.airQualityIndicatorsAsNumber, //
                "channel.openmeteo.air-quality.us-aqi-pm10.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_US_AQI_PM_2_5,
                CHANNEL_TYPE_UID_US_AQI, config.includeUSAqiPM2_5 && config.airQualityIndicatorsAsNumber, //
                "channel.openmeteo.air-quality.us-aqi-pm2_5.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_US_AQI_NITROGEN_DIOXIDE, CHANNEL_TYPE_UID_US_AQI,
                config.includeUSAqiNitrogenDioxide && config.airQualityIndicatorsAsNumber, //
                "channel.openmeteo.air-quality.us-aqi-nitrogen-dioxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_US_AQI_OZONE,
                CHANNEL_TYPE_UID_US_AQI, config.includeUSAqiOzone && config.airQualityIndicatorsAsNumber, //
                "channel.openmeteo.air-quality.us-aqi-ozone.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_US_AQI_SULPHUR_DIOXIDE, CHANNEL_TYPE_UID_US_AQI, //
                config.includeUSAqiSulphurDioxide && config.airQualityIndicatorsAsNumber, //
                "channel.openmeteo.air-quality.us-aqi-sulphur-dioxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_US_AQI_CARBON_MONOXIDE, CHANNEL_TYPE_UID_US_AQI, //
                config.includeUSAqiCarbonMonoxide && config.airQualityIndicatorsAsNumber, //
                "channel.openmeteo.air-quality.us-aqi-carbon-monoxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_US_AQI_AS_STRING,
                CHANNEL_TYPE_UID_US_AQI_AS_STRING, config.includeUSAqi && config.airQualityIndicatorsAsString,
                labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_US_AQI_AS_STRING_PM_10, CHANNEL_TYPE_UID_US_AQI_AS_STRING,
                config.includeUSAqiPM10 && config.airQualityIndicatorsAsString, //
                "channel.openmeteo.air-quality.us-aqi-pm10.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_US_AQI_AS_STRING_PM_2_5, CHANNEL_TYPE_UID_US_AQI_AS_STRING,
                config.includeUSAqiPM2_5 && config.airQualityIndicatorsAsString, //
                "channel.openmeteo.air-quality.us-aqi-pm2_5.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_US_AQI_AS_STRING_NITROGEN_DIOXIDE, CHANNEL_TYPE_UID_US_AQI_AS_STRING,
                config.includeUSAqiNitrogenDioxide && config.airQualityIndicatorsAsString, //
                "channel.openmeteo.air-quality.us-aqi-nitrogen-dioxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_US_AQI_AS_STRING_OZONE, CHANNEL_TYPE_UID_US_AQI_AS_STRING,
                config.includeUSAqiOzone && config.airQualityIndicatorsAsString, //
                "channel.openmeteo.air-quality.us-aqi-ozone.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_US_AQI_AS_STRING_SULPHUR_DIOXIDE, CHANNEL_TYPE_UID_US_AQI_AS_STRING, //
                config.includeUSAqiSulphurDioxide && config.airQualityIndicatorsAsString, //
                "channel.openmeteo.air-quality.us-aqi-sulphur-dioxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_US_AQI_AS_STRING_CARBON_MONOXIDE, CHANNEL_TYPE_UID_US_AQI_AS_STRING, //
                config.includeUSAqiCarbonMonoxide && config.airQualityIndicatorsAsString, //
                "channel.openmeteo.air-quality.us-aqi-carbon-monoxide.label", null, //
                labelArguments, null);

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
                CHANNEL_TYPE_UID_PARTICULATE_CONCENTRATION, config.includePM10, //
                "channel.openmeteo.air-quality.particulate-10.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_PARTICULATE_2_5,
                CHANNEL_TYPE_UID_PARTICULATE_CONCENTRATION, config.includePM2_5, //
                "channel.openmeteo.air-quality.particulate-2_5.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_CARBON_MONOXIDE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeCarbonMonoxide, //
                "channel.openmeteo.air-quality.carbon-monoxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_NITROGEN_DIOXIDE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeNitrogenDioxide, //
                "channel.openmeteo.air-quality.nitrogen-dioxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_SULPHUR_DIOXIDE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeSulphurDioxide, //
                "channel.openmeteo.air-quality.sulphur-dioxide.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_OZONE,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeOzone, //
                "channel.openmeteo.air-quality.ozone.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_AMMONIA,
                CHANNEL_TYPE_UID_GAZ_CONCENTRATION, config.includeAmmonia, //
                "channel.openmeteo.air-quality.ammonia.label", "channel.openmeteo.air-quality.ammonia.description",
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_AEROSOL_OPTICAL_DEPTH, CHANNEL_TYPE_UID_AEROSOL_OPTICAL_DEPTH,
                config.includeAerosolOpticalDepth, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_DUST,
                CHANNEL_TYPE_UID_DUST, config.includeDust, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_ALDER_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeAlderPollen, //
                "channel.openmeteo.air-quality.alder-pollen.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_BIRCH_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeBirchPollen, //
                "channel.openmeteo.air-quality.birch-pollen.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_GRASS_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeGrassPollen, //
                "channel.openmeteo.air-quality.grass-pollen.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_MUGWORT_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeMugwortPollen, //
                "channel.openmeteo.air-quality.mugwort-pollen.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_OLIVE_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeOlivePollen, //
                "channel.openmeteo.air-quality.olive-pollen.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_RAGWEED_POLLEN,
                CHANNEL_TYPE_UID_POLLEN, config.includeRagweedPollen, //
                "channel.openmeteo.air-quality.ragweed-pollen.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_EUROPEAN_AQI,
                CHANNEL_TYPE_UID_EUROPEAN_AQI, config.includeEuropeanAqi && config.airQualityIndicatorsAsNumber,
                labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_AIR_QUALITY_EUROPEAN_AQI_AS_STRING, CHANNEL_TYPE_UID_EUROPEAN_AQI_AS_STRING,
                config.includeEuropeanAqi && config.airQualityIndicatorsAsString, //
                labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_US_AQI,
                CHANNEL_TYPE_UID_US_AQI, config.includeUSAqi && config.airQualityIndicatorsAsNumber, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_AIR_QUALITY_US_AQI_AS_STRING,
                CHANNEL_TYPE_UID_US_AQI_AS_STRING, config.includeUSAqi && config.airQualityIndicatorsAsString,
                labelArguments);

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
        // The "AQI as string" channels get their value from the number AQI which gets later
        // transformed to an option string
        channelId = channelId.replace("aqi-as-string", "aqi");

        return switch (channelId) {
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

    private State getEuropeanStringAirQualityState(@Nullable Float floatValue) {
        if (floatValue == null)
            return UnDefType.NULL;

        try {
            String value = OpenMeteoEuropeanAirQualityIndicatorTransformationService.transform(floatValue);
            return new StringType(value);

        } catch (TransformationException e) {
            logger.error("Error while getting european AQI as string", e);
            return UnDefType.UNDEF;
        }
    }

    private State getUSStringAirQualityState(@Nullable Float floatValue) {
        if (floatValue == null)
            return UnDefType.NULL;

        try {
            String value = OpenMeteoUSAirQualityIndicatorTransformationService.transform(floatValue);
            return new StringType(value);

        } catch (TransformationException e) {
            logger.error("Error while getting US AQI as string", e);
            return UnDefType.UNDEF;
        }
    }

    private State getPollenState(@Nullable Float floatValue) {
        return getDecimalTypeState(floatValue); // , OpenMeteoBindingUnits.GRAINS_PER_CUBICMETRE);
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
                state = getPollenState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_BIRCH_POLLEN:
                state = getPollenState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_MUGWORT_POLLEN:
                state = getPollenState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_GRASS_POLLEN:
                state = getPollenState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_OLIVE_POLLEN:
                state = getPollenState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_RAGWEED_POLLEN:
                state = getPollenState(floatValue);
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
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_AS_STRING:
                state = getEuropeanStringAirQualityState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_AS_STRING_PM_2_5:
                state = getEuropeanStringAirQualityState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_AS_STRING_PM_10:
                state = getEuropeanStringAirQualityState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_AS_STRING_NITROGEN_DIOXIDE:
                state = getEuropeanStringAirQualityState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_AS_STRING_OZONE:
                state = getEuropeanStringAirQualityState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_EUROPEAN_AQI_AS_STRING_SULPHUR_DIOXIDE:
                state = getEuropeanStringAirQualityState(floatValue);
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
            case CHANNEL_AIR_QUALITY_US_AQI_AS_STRING:
                state = getUSStringAirQualityState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_US_AQI_AS_STRING_PM_2_5:
                state = getUSStringAirQualityState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_US_AQI_AS_STRING_PM_10:
                state = getUSStringAirQualityState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_US_AQI_AS_STRING_NITROGEN_DIOXIDE:
                state = getUSStringAirQualityState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_US_AQI_AS_STRING_OZONE:
                state = getUSStringAirQualityState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_US_AQI_AS_STRING_SULPHUR_DIOXIDE:
                state = getUSStringAirQualityState(floatValue);
                break;
            case CHANNEL_AIR_QUALITY_US_AQI_AS_STRING_CARBON_MONOXIDE:
                state = getUSStringAirQualityState(floatValue);
                break;
            default:
                // This should not happen
                logger.warn("Unknown channel id {} in weather data", channelId);
                break;
        }
        return state;
    }
}
