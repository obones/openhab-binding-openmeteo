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
package com.obones.binding.openmeteo.internal.provider;

import static com.obones.binding.openmeteo.internal.OpenMeteoBindingConstants.*;

import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingTypeProvider;
import org.openhab.core.thing.i18n.ThingTypeI18nLocalizationService;
import org.openhab.core.thing.type.ChannelGroupDefinition;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@Component(service = ThingTypeProvider.class)
public class OpenMeteoForecastThingTypeProvider implements ThingTypeProvider {

    private final Logger logger = LoggerFactory.getLogger(OpenMeteoForecastThingTypeProvider.class);
    private final ThingTypeI18nLocalizationService localizationService;
    private final Bundle bundle;

    @Activate
    public OpenMeteoForecastThingTypeProvider(final @Reference ThingTypeI18nLocalizationService localizationService,
            ComponentContext componentContext) {
        this.bundle = componentContext.getBundleContext().getBundle();
        this.localizationService = localizationService;
    }

    @Override
    public Collection<ThingType> getThingTypes(@Nullable Locale locale) {
        @Nullable
        ThingType thingType = getThingType(THING_TYPE_OPENMETEO_FORECAST, locale);

        if (thingType != null)
            return Arrays.asList(thingType);
        else
            return Arrays.asList();
    }

    @Override
    public @Nullable ThingType getThingType(ThingTypeUID thingTypeUID, @Nullable Locale locale) {
        if (THING_TYPE_OPENMETEO_FORECAST.equals(thingTypeUID)) {
            logger.debug("Creating type for {}", thingTypeUID.getAsString());

            ThingTypeBuilder builder = ThingTypeBuilder.instance(thingTypeUID, thingTypeUID.toString())
                    .withSupportedBridgeTypeUIDs(Arrays.asList(THING_TYPE_BRIDGE.getId()))
                    .withLabel("@text/thing-type.openmeteo.forecast.label")
                    .withDescription("@text/thing-type.openmeteo.forecast.description") //
                    .withCategory("forecast") //
                    .withChannelGroupDefinitions(getChannelGroupDefinitions()) //
                    .withRepresentationProperty("location")
                    .withConfigDescriptionURI(URI.create("thing-type:openmeteo:forecast"));

            return localizationService.createLocalizedThingType(bundle, builder.build(), locale);
        }
        return null;
    }

    private List<ChannelGroupDefinition> getChannelGroupDefinitions() {
        ArrayList<ChannelGroupDefinition> result = new ArrayList<ChannelGroupDefinition>();

        result.add(new ChannelGroupDefinition(CHANNEL_GROUP_HOURLY_TIME_SERIES, CHANNEL_GROUP_TYPE_HOURLY_TIME_SERIES));

        DecimalFormat hourlyFormatter = new DecimalFormat("00");
        for (int hour = 1; hour < 24 * 16; hour++) {
            result.add(new ChannelGroupDefinition(CHANNEL_GROUP_HOURLY_PREFIX + hourlyFormatter.format(hour),
                    CHANNEL_GROUP_TYPE_HOURLY));
        }

        return result;
    }
}
