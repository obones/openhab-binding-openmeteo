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
/**
 *
 * The {@link OpenMeteoDiscoveryService} creates things based on the configured location.
 *
 * @author Olivier Sannier - Initial contribution
 */
package com.obones.binding.openmeteo.internal.discovery;

import static com.obones.binding.openmeteo.internal.OpenMeteoBindingConstants.*;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.handler.OpenMeteoBridgeHandler;

/**
 * The {@link OpenMeteoDiscoveryService} is responsible for discovering the system location as defined in global
 * options.
 *
 * @author Olivier Sannier - Initial contribution.
 */
@NonNullByDefault
public class OpenMeteoDiscoveryService extends AbstractDiscoveryService {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(OpenMeteoBridgeHandler.class);

    private static final int DISCOVERY_TIMEOUT_SECONDS = 2;
    private static final int DISCOVERY_INTERVAL_SECONDS = 60;
    private @Nullable ScheduledFuture<?> discoveryJob;
    private final LocationProvider locationProvider;
    private @Nullable PointType previousLocation;

    private final OpenMeteoBridgeHandler bridgeHandler;

    /**
     * Creates an OpenMeteoDiscoveryService.
     */
    public OpenMeteoDiscoveryService(OpenMeteoBridgeHandler bridgeHandler, LocationProvider locationProvider,
            LocaleProvider localeProvider, TranslationProvider i18nProvider) {
        super(SUPPORTED_THINGS_ITEMS, DISCOVERY_TIMEOUT_SECONDS);
        this.bridgeHandler = bridgeHandler;
        this.locationProvider = locationProvider;
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
        activate(null);
    }

    @Override
    protected void activate(@Nullable Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    public void deactivate() {
        logger.debug("Removing older discovery services.");
        removeOlderResults(Instant.now(), bridgeHandler.getThing().getUID());
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Start manual OpenWeatherMap Location discovery scan.");
        scanForNewLocation(false);
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop manual OpenWeatherMap Location discovery scan.");
        super.stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob == null || localDiscoveryJob.isCancelled()) {
            logger.debug("Start OpenWeatherMap Location background discovery job at interval {} s.",
                    DISCOVERY_INTERVAL_SECONDS);
            localDiscoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                scanForNewLocation(true);
            }, 0, DISCOVERY_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null && !localDiscoveryJob.isCancelled()) {
            logger.debug("Stop OpenWeatherMap Location background discovery job.");
            if (localDiscoveryJob.cancel(true)) {
                discoveryJob = null;
            }
        }
    }

    private void scanForNewLocation(boolean updateOnlyIfNewLocation) {
        PointType currentLocation = locationProvider.getLocation();
        if (currentLocation == null) {
            logger.debug("Location is not set -> Will not provide any discovery results.");
        } else if (!Objects.equals(currentLocation, previousLocation)) {
            logger.debug("Location has been changed from {} to {} -> Creating new discovery results.", previousLocation,
                    currentLocation);
            createResults(currentLocation);
            previousLocation = currentLocation;
        } else if (!updateOnlyIfNewLocation) {
            createResults(currentLocation);
        }
    }

    private void createResults(PointType location) {
        String locationString = location.toFullString();
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();

        logger.debug("Creating results for {} with bridge {}.", locationString, bridgeUID.getAsString());
        createWeatherForecastResult(locationString, bridgeUID);
        createAirQualityResult(locationString, bridgeUID);
    }

    private void createWeatherForecastResult(String location, ThingUID bridgeUID) {
        thingDiscovered(DiscoveryResultBuilder
                .create(new ThingUID(THING_TYPE_OPENMETEO_FORECAST, bridgeUID, SYSTEM_LOCATION_THING_ID))
                .withLabel("@text/discovery.forecast.system.label").withProperty(PROPERTY_THING_LOCATION, location)
                .withRepresentationProperty(PROPERTY_THING_LOCATION).withBridge(bridgeUID).build());
    }

    private void createAirQualityResult(String location, ThingUID bridgeUID) {
        thingDiscovered(DiscoveryResultBuilder
                .create(new ThingUID(THING_TYPE_OPENMETEO_AIR_QUALITY, bridgeUID, SYSTEM_LOCATION_THING_ID))
                .withLabel("@text/discovery.air-quality.system.label").withProperty(PROPERTY_THING_LOCATION, location)
                .withRepresentationProperty(PROPERTY_THING_LOCATION).withBridge(bridgeUID).build());
    }
}
