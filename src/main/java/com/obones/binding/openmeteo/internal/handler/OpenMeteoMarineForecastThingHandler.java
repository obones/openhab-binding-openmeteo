package com.obones.binding.openmeteo.internal.handler;

import static com.obones.binding.openmeteo.internal.OpenMeteoBindingConstants.*;
import static org.openhab.core.thing.DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_OUTDOOR_TEMPERATURE;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.config.OpenMeteoMarineForecastThingConfiguration;
import com.obones.binding.openmeteo.internal.connection.OpenMeteoConnection;
import com.obones.binding.openmeteo.internal.connection.OpenMeteoConnection.MarineForecastValue;
import com.obones.binding.openmeteo.internal.utils.Localization;
import com.openmeteo.sdk.Variable;
import com.openmeteo.sdk.WeatherApiResponse;

/***
 * The{@link OpenMeteoMarineForecastThingHandler} is responsible for updating marine weather forecast related channels,
 * which are retrieved via {@link OpenMeteoBridgeHandler}.
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class OpenMeteoMarineForecastThingHandler extends OpenMeteoBaseThingHandler {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(OpenMeteoBridgeHandler.class);

    public OpenMeteoMarineForecastThingHandler(Thing thing, Localization localization,
            final TimeZoneProvider timeZoneProvider, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, localization, timeZoneProvider, channelTypeRegistry);
        logger.trace("OpenMeteoMarineForecastHandler(thing={},localization={}) constructor called.", thing,
                localization);
    }

    protected void initializeChannels(ThingHandlerCallback callback, ThingBuilder builder, ThingUID thingUID) {
        OpenMeteoMarineForecastThingConfiguration config = getConfigAs(OpenMeteoMarineForecastThingConfiguration.class);

        if (config.hourlyTimeSeries) {
            String labelSuffix = localization
                    .getText("channel-type.openmeteo.marine-forecast.label-suffix.hourly.time-series");
            initializeHourlyGroupOptionalChannels(callback, builder, thingUID, config, CHANNEL_GROUP_HOURLY_TIME_SERIES,
                    labelSuffix);
        }

        if (config.dailyTimeSeries) {
            String labelSuffix = localization
                    .getText("channel-type.openmeteo.marine-forecast.label-suffix.daily.time-series");
            initializeDailyGroupOptionalChannels(callback, builder, thingUID, config, CHANNEL_GROUP_DAILY_TIME_SERIES,
                    labelSuffix);
        }

        if (config.current) {
            initializeCurrentGroupOptionalChannels(callback, builder, thingUID, config);
        }
    }

    protected ThingBuilder initializeHourlyGroupOptionalChannels(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, OpenMeteoMarineForecastThingConfiguration config, String channelGroupId,
            String labelSuffix) {

        Object[] labelArguments = { labelSuffix };

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_MARINE_FORECAST_WAVE_HEIGHT,
                CHANNEL_TYPE_UID_WAVE_HEIGHT, config.includeWaveHeight, //
                "channel.openmeteo.marine-forecast.mean-wave-height.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_MARINE_FORECAST_WIND_WAVE_HEIGHT,
                CHANNEL_TYPE_UID_WAVE_HEIGHT, config.includeWindWaveHeight, //
                "channel.openmeteo.marine-forecast.wind-wave-height.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_SWELL_WAVE_HEIGHT, CHANNEL_TYPE_UID_WAVE_HEIGHT, //
                config.includeSwellWaveHeight, //
                "channel.openmeteo.marine-forecast.swell-wave-height.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_SECONDARY_SWELL_WAVE_HEIGHT, CHANNEL_TYPE_UID_WAVE_HEIGHT, //
                config.includeSecondarySwellWaveHeight, //
                "channel.openmeteo.marine-forecast.secondary-swell-wave-height.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_TERTIARY_SWELL_WAVE_HEIGHT, CHANNEL_TYPE_UID_WAVE_HEIGHT, //
                config.includeTertiarySwellWaveHeight, //
                "channel.openmeteo.marine-forecast.tertiary-swell-wave-height.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_MARINE_FORECAST_WAVE_DIRECTION,
                CHANNEL_TYPE_UID_WAVE_DIRECTION, config.includeWaveDirection, //
                "channel.openmeteo.marine-forecast.mean-wave-direction.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_WIND_WAVE_DIRECTION, CHANNEL_TYPE_UID_WAVE_DIRECTION, //
                config.includeWindWaveDirection, //
                "channel.openmeteo.marine-forecast.wind-wave-direction.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_SWELL_WAVE_DIRECTION, CHANNEL_TYPE_UID_WAVE_DIRECTION, //
                config.includeSwellWaveDirection, //
                "channel.openmeteo.marine-forecast.swell-wave-direction.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_SECONDARY_SWELL_WAVE_DIRECTION, CHANNEL_TYPE_UID_WAVE_DIRECTION, //
                config.includeSecondarySwellWaveDirection, //
                "channel.openmeteo.marine-forecast.secondary-swell-wave-direction.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_TERTIARY_SWELL_WAVE_DIRECTION, CHANNEL_TYPE_UID_WAVE_DIRECTION, //
                config.includeTertiarySwellWaveDirection, //
                "channel.openmeteo.marine-forecast.tertiary-swell-wave-direction.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_MARINE_FORECAST_WAVE_PERIOD,
                CHANNEL_TYPE_UID_WAVE_PERIOD, config.includeWavePeriod, //
                "channel.openmeteo.marine-forecast.mean-wave-period.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_MARINE_FORECAST_WIND_WAVE_PERIOD,
                CHANNEL_TYPE_UID_WAVE_PERIOD, config.includeWindWavePeriod, //
                "channel.openmeteo.marine-forecast.wind-wave-period.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_SWELL_WAVE_PERIOD, CHANNEL_TYPE_UID_WAVE_PERIOD, //
                config.includeSwellWavePeriod, //
                "channel.openmeteo.marine-forecast.swell-wave-period.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_SECONDARY_SWELL_WAVE_PERIOD, CHANNEL_TYPE_UID_WAVE_PERIOD, //
                config.includeSecondarySwellWavePeriod, //
                "channel.openmeteo.marine-forecast.secondary-swell-wave-period.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_TERTIARY_SWELL_WAVE_PERIOD, CHANNEL_TYPE_UID_WAVE_PERIOD, //
                config.includeTertiarySwellWavePeriod, //
                "channel.openmeteo.marine-forecast.tertiary-swell-wave-period.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_WIND_WAVE_PEAK_PERIOD, CHANNEL_TYPE_UID_WAVE_PERIOD, //
                config.includeWindWavePeakPeriod, //
                "channel.openmeteo.marine-forecast.wind-wave-peak-period.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_SWELL_WAVE_PEAK_PERIOD, CHANNEL_TYPE_UID_WAVE_PERIOD, //
                config.includeSwellWavePeakPeriod, //
                "channel.openmeteo.marine-forecast.swell-wave-peak-period.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_OCEAN_CURRENT_VELOCITY, CHANNEL_TYPE_UID_OCEAN_CURRENT_VELOCITY, //
                config.includeOceanCurrentVelocity, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_OCEAN_CURRENT_DIRECTION, CHANNEL_TYPE_UID_OCEAN_CURRENT_DIRECTION, //
                config.includeOceanCurrentDirection, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_SEA_SURFACE_TEMPERATURE, SYSTEM_CHANNEL_TYPE_UID_OUTDOOR_TEMPERATURE, //
                config.includeSeaSurfaceTemperature, //
                "channel.openmeteo.marine-forecast.sea-surface-temperature.label", //
                "channel.openmeteo.marine-forecast.sea-surface-temperature.description", //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_SEA_LEVEL_HEIGHT_MSL, CHANNEL_TYPE_UID_SEA_LEVEL_HEIGHT_MSL,
                config.includeSeaLevelHeightMsl, labelArguments);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_INVERT_BAROMETER_HEIGHT, CHANNEL_TYPE_UID_INVERT_BAROMETER_HEIGHT,
                config.includeInvertBarometerHeight, labelArguments);

        return builder;
    }

    protected ThingBuilder initializeCurrentGroupOptionalChannels(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, OpenMeteoMarineForecastThingConfiguration config) {

        String labelSuffix = localization.getText("channel-type.openmeteo.marine-forecast.label-suffix.current");
        String channelGroupId = CHANNEL_GROUP_CURRENT;

        return initializeHourlyGroupOptionalChannels(callback, builder, thingUID, config, channelGroupId, labelSuffix);
    }

    protected ThingBuilder initializeDailyGroupOptionalChannels(ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, OpenMeteoMarineForecastThingConfiguration config, String channelGroupId,
            String labelSuffix) {

        Object[] labelArguments = { labelSuffix };

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_MARINE_FORECAST_WAVE_HEIGHT,
                CHANNEL_TYPE_UID_WAVE_HEIGHT, config.includeWaveHeight, //
                "channel.openmeteo.marine-forecast.mean-wave-height-max.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_MARINE_FORECAST_WIND_WAVE_HEIGHT,
                CHANNEL_TYPE_UID_WAVE_HEIGHT, config.includeWindWaveHeight, //
                "channel.openmeteo.marine-forecast.wind-wave-height-max.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_SWELL_WAVE_HEIGHT, CHANNEL_TYPE_UID_WAVE_HEIGHT, //
                config.includeSwellWaveHeight, //
                "channel.openmeteo.marine-forecast.swell-wave-height-max.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_MARINE_FORECAST_WAVE_DIRECTION,
                CHANNEL_TYPE_UID_WAVE_DIRECTION, config.includeWaveDirection, //
                "channel.openmeteo.marine-forecast.mean-wave-direction-dominant.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_WIND_WAVE_DIRECTION, CHANNEL_TYPE_UID_WAVE_DIRECTION, //
                config.includeWindWaveDirection, //
                "channel.openmeteo.marine-forecast.wind-wave-direction-dominant.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_SWELL_WAVE_DIRECTION, CHANNEL_TYPE_UID_WAVE_DIRECTION, //
                config.includeSwellWaveDirection, //
                "channel.openmeteo.marine-forecast.swell-wave-direction-dominant.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_MARINE_FORECAST_WAVE_PERIOD,
                CHANNEL_TYPE_UID_WAVE_PERIOD, config.includeWavePeriod, //
                "channel.openmeteo.marine-forecast.mean-wave-period-max.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId, CHANNEL_MARINE_FORECAST_WIND_WAVE_PERIOD,
                CHANNEL_TYPE_UID_WAVE_PERIOD, config.includeWindWavePeriod, //
                "channel.openmeteo.marine-forecast.wind-wave-period-max.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_SWELL_WAVE_PERIOD, CHANNEL_TYPE_UID_WAVE_PERIOD, //
                config.includeSwellWavePeriod, //
                "channel.openmeteo.marine-forecast.swell-wave-period-max.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_WIND_WAVE_PEAK_PERIOD, CHANNEL_TYPE_UID_WAVE_PERIOD, //
                config.includeWindWavePeakPeriod, //
                "channel.openmeteo.marine-forecast.wind-wave-peak-period-max.label", null, //
                labelArguments, null);

        initializeOptionalChannel(callback, builder, thingUID, channelGroupId,
                CHANNEL_MARINE_FORECAST_SWELL_WAVE_PEAK_PERIOD, CHANNEL_TYPE_UID_WAVE_PERIOD, //
                config.includeSwellWavePeakPeriod, //
                "channel.openmeteo.marine-forecast.swell-wave-peak-period-max.label", null, //
                labelArguments, null);

        return builder;
    }

    protected void updateChannel(ChannelUID channelUID) {
        String channelGroupId = Optional.ofNullable(channelUID.getGroupId()).orElse("");
        logger.debug("OpenMeteoMarineForecastThingHandler: updateChannel {}, groupID {}", channelUID, channelGroupId);

        switch (channelGroupId) {
            case CHANNEL_GROUP_HOURLY_TIME_SERIES:
                updateHourlyTimeSeries(channelUID);
                break;
            case CHANNEL_GROUP_DAILY_TIME_SERIES:
                updateDailyTimeSeries(channelUID);
                break;
            case CHANNEL_GROUP_CURRENT:
                updateCurrentChannel(channelUID);
                break;
        }
    }

    protected WeatherApiResponse requestData(OpenMeteoConnection connection, PointType location)
            throws CommunicationException, ConfigurationException {
        OpenMeteoMarineForecastThingConfiguration config = getConfigAs(OpenMeteoMarineForecastThingConfiguration.class);

        return connection.getMarineForecast(location, getMarineForecastValues(),
                (config.hourlyTimeSeries) ? config.hourlyHours : null, //
                (config.dailyTimeSeries) ? config.dailyDays : null, //
                config.current, //
                config.pastHours, config.pastDays);
    }

    private EnumSet<OpenMeteoConnection.MarineForecastValue> getMarineForecastValues() {
        EnumSet<OpenMeteoConnection.MarineForecastValue> result = EnumSet
                .noneOf(OpenMeteoConnection.MarineForecastValue.class);
        OpenMeteoMarineForecastThingConfiguration config = getConfigAs(OpenMeteoMarineForecastThingConfiguration.class);

        if (config.includeWaveHeight)
            result.add(MarineForecastValue.WAVE_HEIGHT);
        if (config.includeWindWaveHeight)
            result.add(MarineForecastValue.WIND_WAVE_HEIGHT);
        if (config.includeSwellWaveHeight)
            result.add(MarineForecastValue.SWELL_WAVE_HEIGHT);
        if (config.includeSecondarySwellWaveHeight)
            result.add(MarineForecastValue.SECONDARY_SWELL_WAVE_HEIGHT);
        if (config.includeTertiarySwellWaveHeight)
            result.add(MarineForecastValue.TERTIARY_SWELL_WAVE_HEIGHT);
        if (config.includeWaveDirection)
            result.add(MarineForecastValue.WAVE_DIRECTION);
        if (config.includeWindWaveDirection)
            result.add(MarineForecastValue.WIND_WAVE_DIRECTION);
        if (config.includeSwellWaveDirection)
            result.add(MarineForecastValue.SWELL_WAVE_DIRECTION);
        if (config.includeSecondarySwellWaveDirection)
            result.add(MarineForecastValue.SECONDARY_SWELL_WAVE_DIRECTION);
        if (config.includeTertiarySwellWaveDirection)
            result.add(MarineForecastValue.TERTIARY_SWELL_WAVE_DIRECTION);
        if (config.includeWavePeriod)
            result.add(MarineForecastValue.WAVE_PERIOD);
        if (config.includeWindWavePeriod)
            result.add(MarineForecastValue.WIND_WAVE_PERIOD);
        if (config.includeSwellWavePeriod)
            result.add(MarineForecastValue.SWELL_WAVE_PERIOD);
        if (config.includeSecondarySwellWavePeriod)
            result.add(MarineForecastValue.SECONDARY_SWELL_WAVE_PERIOD);
        if (config.includeTertiarySwellWavePeriod)
            result.add(MarineForecastValue.TERTIARY_SWELL_WAVE_PERIOD);
        if (config.includeWindWavePeakPeriod)
            result.add(MarineForecastValue.WIND_WAVE_PEAK_PERIOD);
        if (config.includeSwellWavePeakPeriod)
            result.add(MarineForecastValue.SWELL_WAVE_PEAK_PERIOD);
        if (config.includeOceanCurrentVelocity)
            result.add(MarineForecastValue.OCEAN_CURRENT_VELOCITY);
        if (config.includeOceanCurrentDirection)
            result.add(MarineForecastValue.OCEAN_CURRENT_DIRECTION);
        if (config.includeSeaSurfaceTemperature)
            result.add(MarineForecastValue.SEA_SURFACE_TEMPERATURE);
        if (config.includeSeaLevelHeightMsl)
            result.add(MarineForecastValue.SEA_LEVEL_HEIGHT_MSL);
        if (config.includeInvertBarometerHeight)
            result.add(MarineForecastValue.INVERT_BAROMETER_HEIGHT);

        return result;
    }

    protected int getVariableIndex(String channelId) {
        return switch (channelId) {
            case CHANNEL_MARINE_FORECAST_WAVE_HEIGHT -> Variable.wave_height;
            case CHANNEL_MARINE_FORECAST_WIND_WAVE_HEIGHT -> Variable.wind_wave_height;
            case CHANNEL_MARINE_FORECAST_SWELL_WAVE_HEIGHT -> Variable.swell_wave_height;
            case CHANNEL_MARINE_FORECAST_SECONDARY_SWELL_WAVE_HEIGHT -> 141; // secondary_swell_wave_height
            case CHANNEL_MARINE_FORECAST_TERTIARY_SWELL_WAVE_HEIGHT -> 145; // tertiary_swell_wave_height
            case CHANNEL_MARINE_FORECAST_WAVE_DIRECTION -> Variable.wave_direction;
            case CHANNEL_MARINE_FORECAST_WIND_WAVE_DIRECTION -> Variable.wind_wave_direction;
            case CHANNEL_MARINE_FORECAST_SWELL_WAVE_DIRECTION -> Variable.swell_wave_direction;
            case CHANNEL_MARINE_FORECAST_SECONDARY_SWELL_WAVE_DIRECTION -> 144; // secondary_swell_wave_direction
            case CHANNEL_MARINE_FORECAST_TERTIARY_SWELL_WAVE_DIRECTION -> 148; // tertiary_swell_wave_direction
            case CHANNEL_MARINE_FORECAST_WAVE_PERIOD -> Variable.wave_period;
            case CHANNEL_MARINE_FORECAST_WIND_WAVE_PERIOD -> Variable.wind_wave_period;
            case CHANNEL_MARINE_FORECAST_SWELL_WAVE_PERIOD -> Variable.swell_wave_period;
            case CHANNEL_MARINE_FORECAST_SECONDARY_SWELL_WAVE_PERIOD -> 142; // secondary_swell_wave_period
            case CHANNEL_MARINE_FORECAST_TERTIARY_SWELL_WAVE_PERIOD -> 146; // tertiary_swell_wave_period
            case CHANNEL_MARINE_FORECAST_WIND_WAVE_PEAK_PERIOD -> Variable.wind_wave_peak_period;
            case CHANNEL_MARINE_FORECAST_SWELL_WAVE_PEAK_PERIOD -> Variable.swell_wave_peak_period;
            case CHANNEL_MARINE_FORECAST_OCEAN_CURRENT_VELOCITY -> 108; // ocean_current_velocity
            case CHANNEL_MARINE_FORECAST_OCEAN_CURRENT_DIRECTION -> 109; // ocean_current_direction
            case CHANNEL_MARINE_FORECAST_SEA_SURFACE_TEMPERATURE -> 133; // sea_surface_temperature
            case CHANNEL_MARINE_FORECAST_SEA_LEVEL_HEIGHT_MSL -> 132; // sea_level_height_msl
            case CHANNEL_MARINE_FORECAST_INVERT_BAROMETER_HEIGHT -> 134; // invert_barometer_height
            default -> Variable.undefined;
        };
    }

    protected State getForecastState(String channelId, @Nullable Float floatValue, @Nullable Long longValue) {
        State state = UnDefType.UNDEF;

        switch (channelId) {
            case CHANNEL_MARINE_FORECAST_WAVE_HEIGHT:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_WIND_WAVE_HEIGHT:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_SWELL_WAVE_HEIGHT:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_SECONDARY_SWELL_WAVE_HEIGHT:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_TERTIARY_SWELL_WAVE_HEIGHT:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_WAVE_DIRECTION:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_WIND_WAVE_DIRECTION:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_SWELL_WAVE_DIRECTION:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_SECONDARY_SWELL_WAVE_DIRECTION:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_TERTIARY_SWELL_WAVE_DIRECTION:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_WAVE_PERIOD:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_WIND_WAVE_PERIOD:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_SWELL_WAVE_PERIOD:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_SECONDARY_SWELL_WAVE_PERIOD:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_TERTIARY_SWELL_WAVE_PERIOD:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_WIND_WAVE_PEAK_PERIOD:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_SWELL_WAVE_PEAK_PERIOD:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_OCEAN_CURRENT_VELOCITY:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_OCEAN_CURRENT_DIRECTION:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_SEA_SURFACE_TEMPERATURE:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_SEA_LEVEL_HEIGHT_MSL:
                return getDecimalTypeState(floatValue);
            case CHANNEL_MARINE_FORECAST_INVERT_BAROMETER_HEIGHT:
                return getDecimalTypeState(floatValue);
            default:
                // This should not happen
                logger.warn("Unknown channel id {} in marine weather data", channelId);
                break;
        }
        return state;
    }
}
