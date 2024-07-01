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
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.openmeteo.internal.transformation.OpenMeteoEuropeanAirQualityIndicatorTransformationService;
import com.obones.binding.openmeteo.internal.transformation.OpenMeteoUSAirQualityIndicatorTransformationService;
import com.obones.binding.openmeteo.internal.utils.Localization;

/**
 * Profile factory that creates the transformation profiles for the OpenMeteo transformation services
 *
 * @author Olivier Sannier - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class })
public class OpenMeteoTransformationProfileFactory implements ProfileFactory, ProfileTypeProvider {

    private @NonNullByDefault({}) final Logger logger = LoggerFactory
            .getLogger(OpenMeteoTransformationProfileFactory.class);

    private @NonNullByDefault({}) OpenMeteoEuropeanAirQualityIndicatorTransformationService europeanAQITransformationService;
    private @NonNullByDefault({}) OpenMeteoUSAirQualityIndicatorTransformationService usAQITransformationService;
    private @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;
    private Localization localization = Localization.UNKNOWN;

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

    private ProfileType getProfileType(ProfileTypeUID uid) {
        @Nullable
        ProfileTypeDetail detail = PROFILE_TYPE_DETAILS.get(uid);

        if (detail == null) {
            logger.error("Profile type details are missing for {}", uid.getAsString());
            return ProfileTypeBuilder.newState(uid, uid.getId()).build();
        } else {
            return ProfileTypeBuilder //
                    .newState(uid, localization.getText(detail.labelKey))
                    .withSupportedChannelTypeUIDs(detail.channelTypeUID) //
                    .build();
        }
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {

        Set<ProfileType> profileTypes = SUPPORTED_PROFILE_TYPE_UIDS.stream() //
                .map(uid -> getProfileType(uid)) //
                .collect(Collectors.toSet());

        return profileTypes;
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

    private void updateLocalization() {
        if (Localization.UNKNOWN.equals(localization) && (localeProvider != null) && (i18nProvider != null)) {
            logger.trace("updateLocalization(): creating Localization based on locale={},translation={}).",
                    localeProvider, i18nProvider);
            localization = new Localization(localeProvider, i18nProvider);
        }
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
}
