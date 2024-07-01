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
package com.obones.binding.openmeteo.internal.profiles;

import static com.obones.binding.openmeteo.internal.OpenMeteoBindingConstants.*;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.obones.binding.openmeteo.internal.transformation.OpenMeteoEuropeanAirQualityIndicatorTransformationService;
import com.obones.binding.openmeteo.internal.transformation.OpenMeteoUSAirQualityIndicatorTransformationService;

/**
 * Profile factory that creates the transformation profiles for the OpenMeteo transformation services
 *
 * @author Olivier Sannier - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
public class OpenMeteoTransformationProfileFactory implements ProfileFactory, ProfileTypeProvider {

    @NonNullByDefault({})
    private OpenMeteoEuropeanAirQualityIndicatorTransformationService europeanAQITransformationService;
    @NonNullByDefault({})
    private OpenMeteoUSAirQualityIndicatorTransformationService usAQITransformationService;

    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {

        if (EUROPEAN_AQI_PROFILE_TYPE_UID.equals(profileTypeUID))
            return new OpenMeteoEuropeanAirQualityIndicatorTransformationProfile(callback, profileContext,
                    europeanAQITransformationService);
        else if (US_AQI_PROFILE_TYPE_UID.equals(profileTypeUID))
            return new OpenMeteoUSAirQualityIndicatorTransformationProfile(callback, profileContext,
                    usAQITransformationService);

        return null;
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return SUPPORTED_PROFILE_TYPE_UIDS;
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return SUPPORTED_PROFILE_TYPES;
    }

    @Reference
    public void setEuropeanAQITransformationService(
            OpenMeteoEuropeanAirQualityIndicatorTransformationService europeanAQITransformationService) {

        this.europeanAQITransformationService = europeanAQITransformationService;
    }

    @Reference
    public void setUsAQITransformationService(
            OpenMeteoUSAirQualityIndicatorTransformationService usAQITransformationService) {

        this.usAQITransformationService = usAQITransformationService;
    }
}
