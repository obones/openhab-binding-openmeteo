/**
 * Copyright (c) 2023-2024 Olivier Sannier 
 ** See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, 
 * you can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package com.obones.binding.openmeteo.internal.factory;

import static com.obones.binding.openmeteo.internal.OpenMeteoBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.discovery.OpenMeteoDiscoveryService;
import com.obones.binding.openmeteo.internal.handler.OpenMeteoAirQualityThingHandler;
import com.obones.binding.openmeteo.internal.handler.OpenMeteoBridgeHandler;
import com.obones.binding.openmeteo.internal.handler.OpenMeteoForecastThingHandler;
import com.obones.binding.openmeteo.internal.utils.Localization;

/**
 * The {@link OpenMeteoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, name = "binding.openmeteo")
public class OpenMeteoHandlerFactory extends BaseThingHandlerFactory {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(ThingHandlerFactory.class);

    private @Nullable ServiceRegistration<?> discoveryServiceRegistration = null;
    private @Nullable OpenMeteoDiscoveryService discoveryService = null;

    private @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;
    private ChannelTypeRegistry channelTypeRegistry;
    private TimeZoneProvider timeZoneProvider;
    private final LocationProvider locationProvider;
    private Localization localization = Localization.UNKNOWN;

    // Private

    private void updateLocalization() {
        if (Localization.UNKNOWN.equals(localization) && (localeProvider != null) && (i18nProvider != null)) {
            logger.trace("updateLocalization(): creating Localization based on locale={},translation={}).",
                    localeProvider, i18nProvider);
            localization = new Localization(localeProvider, i18nProvider);
        }
    }

    private void registerDeviceDiscoveryService(OpenMeteoBridgeHandler bridgeHandler) {
        logger.trace("registerDeviceDiscoveryService({}) called.", bridgeHandler);

        OpenMeteoDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService == null) {
            discoveryService = this.discoveryService = new OpenMeteoDiscoveryService(bridgeHandler, locationProvider,
                    localeProvider, i18nProvider);
        }
        if (discoveryServiceRegistration == null) {
            discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                    discoveryService, new Hashtable<>());
        }
    }

    private synchronized void unregisterDeviceDiscoveryService(OpenMeteoBridgeHandler bridgeHandler) {
        logger.trace("unregisterDeviceDiscoveryService({}) called.", bridgeHandler);

        OpenMeteoDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            ServiceRegistration<?> discoveryServiceRegistration = this.discoveryServiceRegistration;
            if (discoveryServiceRegistration != null) {
                discoveryServiceRegistration.unregister();
                this.discoveryServiceRegistration = null;
            }
        }
    }

    private @Nullable ThingHandler createBridgeHandler(Thing thing) {
        logger.trace("createBridgeHandler({}) called for thing named '{}'.", thing.getUID(), thing.getLabel());
        OpenMeteoBridgeHandler openMeteoBridgeHandler = new OpenMeteoBridgeHandler((Bridge) thing, localization);
        registerDeviceDiscoveryService(openMeteoBridgeHandler);
        return openMeteoBridgeHandler;
    }

    private @Nullable ThingHandler createForecastThingHandler(Thing thing) {
        logger.trace("createForecastThingHandler({}) called for thing named '{}'.", thing.getUID(), thing.getLabel());
        return new OpenMeteoForecastThingHandler(thing, localization, timeZoneProvider, channelTypeRegistry);
    }

    private @Nullable ThingHandler createAirQualityThingHandler(Thing thing) {
        logger.trace("createAirQualityThingHandler({}) called for thing named '{}'.", thing.getUID(), thing.getLabel());
        return new OpenMeteoAirQualityThingHandler(thing, localization, timeZoneProvider, channelTypeRegistry);
    }

    // Constructor

    @Activate
    public OpenMeteoHandlerFactory(final @Reference LocaleProvider givenLocaleProvider,
            final @Reference TranslationProvider givenI18nProvider,
            final @Reference TimeZoneProvider givenTimeZoneProvider,
            final @Reference ChannelTypeRegistry givenChannelTypeRegistry,
            final @Reference LocationProvider givenLocationProvider) {
        logger.trace("OpenMeteoHandlerFactory(locale={},translation={}) called.", givenLocaleProvider,
                givenI18nProvider);
        localeProvider = givenLocaleProvider;
        i18nProvider = givenI18nProvider;
        timeZoneProvider = givenTimeZoneProvider;
        channelTypeRegistry = givenChannelTypeRegistry;
        locationProvider = givenLocationProvider;
    }

    @Reference
    protected void setLocaleProvider(final LocaleProvider givenLocaleProvider) {
        logger.trace("setLocaleProvider(): provided locale={}.", givenLocaleProvider);
        localeProvider = givenLocaleProvider;
        updateLocalization();
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider givenI18nProvider) {
        logger.trace("setTranslationProvider(): provided translation={}.", givenI18nProvider);
        i18nProvider = givenI18nProvider;
        updateLocalization();
    }

    // Utility methods

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        boolean result = SUPPORTED_THINGS_BRIDGE.contains(thingTypeUID)
                || SUPPORTED_THINGS_ITEMS.contains(thingTypeUID);
        logger.trace("supportsThingType({}) called and returns {}.", thingTypeUID, result);
        return result;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingHandler resultHandler = null;
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        // Handle Binding creation
        // Handle Bridge creation
        if (SUPPORTED_THINGS_BRIDGE.contains(thingTypeUID)) {
            resultHandler = createBridgeHandler(thing);
        }
        // Handle creation of Things behind the Bridge
        else if (THING_TYPE_OPENMETEO_FORECAST.equals(thingTypeUID)) {
            resultHandler = createForecastThingHandler(thing);
        } else if (THING_TYPE_OPENMETEO_AIR_QUALITY.equals(thingTypeUID)) {
            resultHandler = createAirQualityThingHandler(thing);
        } else {
            logger.warn("createHandler({}) failed: ThingHandler not found for {}.", thingTypeUID, thing.getLabel());
        }

        return resultHandler;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        // Handle Bridge removal
        if (thingHandler instanceof OpenMeteoBridgeHandler) {
            logger.trace("removeHandler() removing bridge '{}'.", thingHandler.toString());
            unregisterDeviceDiscoveryService((OpenMeteoBridgeHandler) thingHandler);
        }

        super.removeHandler(thingHandler);
    }
}
