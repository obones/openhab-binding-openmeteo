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
package com.obones.binding.openmeteo.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class OpenMeteoForecastThingConfiguration extends OpenMeteoBaseThingConfiguration {

    /*
     * Default values - should not be modified
     */
    public int hourlyHours = 48;
    public boolean hourlyTimeSeries = true;
    public boolean hourlySplit = false;

    public int dailyDays = 5;
    public boolean dailyTimeSeries = true;
    public boolean dailySplit = false;

    public boolean current = false;

    public boolean minutely15 = false;
    public int minutely15Steps = 48;

    public boolean includeTemperature = true;
    public boolean includeHumidity = true;
    public boolean includeDewPoint = true;
    public boolean includeApparentTemperature = true;
    public boolean includePressure = true;
    public boolean includeCloudiness = true;
    public boolean includeWindSpeed = true;
    public boolean includeWindDirection = true;
    public boolean includeGustSpeed = true;
    public boolean includeShortwaveRadiation = false;
    public boolean includeDirectRadiation = false;
    public boolean includeDirectNormalIrradiance = false;
    public boolean includeDiffuseRadiation = false;
    public boolean includeVapourPressureDeficit = false;
    public boolean includeCape = false;
    public boolean includeEvapotranspiration = false;
    public boolean includeEt0FAOEvapotranspiration = false;
    public boolean includePrecipitation = false;
    public boolean includeSnow = true;
    public boolean includePrecipitationProbability = true;
    public boolean includeRain = true;
    public boolean includeShowers = false;
    public boolean includeWeatherCode = true;
    public boolean includeSnowDepth = false;
    public boolean includeFreezingLevelHeight = false;
    public boolean includeVisibility = true;
    public boolean includeIsDay = false;
    public boolean includeSunrise = true;
    public boolean includeSunset = true;
    public boolean includeSunshineDuration = false;
    public boolean includeDaylightDuration = false;
    public boolean includeUVIndex = true;
    public boolean includeUVIndexClearSky = false;
}
