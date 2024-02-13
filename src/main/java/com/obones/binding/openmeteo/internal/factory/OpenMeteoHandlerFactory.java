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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.OpenMeteoBindingConstants;
import com.obones.binding.openmeteo.internal.handler.OpenMeteoBridgeHandler;
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

    private @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;
    private Localization localization = Localization.UNKNOWN;

    private @Nullable static ThingHandlerFactory activeInstance = null;

    // Private

    private void updateLocalization() {
        if (Localization.UNKNOWN.equals(localization) && (localeProvider != null) && (i18nProvider != null)) {
            logger.trace("updateLocalization(): creating Localization based on locale={},translation={}).",
                    localeProvider, i18nProvider);
            localization = new Localization(localeProvider, i18nProvider);
        }
    }

    private @Nullable ThingHandler createBridgeHandler(Thing thing) {
        logger.trace("createBridgeHandler({}) called for thing named '{}'.", thing.getUID(), thing.getLabel());
        OpenMeteoBridgeHandler openMeteoBridgeHandler = new OpenMeteoBridgeHandler((Bridge) thing, localization);
        return openMeteoBridgeHandler;
    }

    // Constructor

    @Activate
    public OpenMeteoHandlerFactory(final @Reference LocaleProvider givenLocaleProvider,
            final @Reference TranslationProvider givenI18nProvider) {
        logger.trace("AirZoneHandlerFactory(locale={},translation={}) called.", givenLocaleProvider, givenI18nProvider);
        localeProvider = givenLocaleProvider;
        i18nProvider = givenI18nProvider;
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
        boolean result = OpenMeteoBindingConstants.SUPPORTED_THINGS_BRIDGE.contains(thingTypeUID);
        logger.trace("supportsThingType({}) called and returns {}.", thingTypeUID, result);
        return result;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingHandler resultHandler = null;
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        // Handle Binding creation
        // Handle Bridge creation
        if (OpenMeteoBindingConstants.SUPPORTED_THINGS_BRIDGE.contains(thingTypeUID)) {
            resultHandler = createBridgeHandler(thing);
        }

        return resultHandler;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        activeInstance = this;
        super.activate(componentContext);
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        activeInstance = null;
        super.deactivate(componentContext);
    }
}
