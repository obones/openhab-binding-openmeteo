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
package com.obones.binding.openmeteo.internal.utils;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a utility class for dealing with localization.
 *
 * It provides the following methods:
 * <ul>
 * <li>{@link #getText} returns the localized message.</li>
 * </ul>
 * <p>
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class Localization {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(Localization.class);

    // Public definition

    public static final Localization UNKNOWN = new Localization();

    /*
     * ***************************
     * ***** Private Objects *****
     */
    private static final String OPEN_BRACKET = "(";
    private static final String CLOSE_BRACKET = ")";
    private LocaleProvider localeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;

    /**
     * Class, which is needed to maintain a @NonNullByDefault for class {@link Localization}.
     */
    private class UnknownLocale implements LocaleProvider {
        @SuppressWarnings("null") // stupid @NonNull warning on Locale.ROOT which obviously is not null
        @Override
        public Locale getLocale() {
            return java.util.Locale.ROOT;
        }
    }

    /*
     * ************************
     * ***** Constructors *****
     */

    /**
     * Constructor
     * <P>
     * Initializes the {@link Localization} module without any framework information.
     */
    Localization() {
        this.localeProvider = new UnknownLocale();
    }

    /**
     * Constructor
     * <P>
     * Initializes the {@link Localization} module with framework information.
     *
     * @param localeProvider providing a locale,
     * @param i18nProvider as service interface for internationalization.
     */
    public Localization(final LocaleProvider localeProvider, final TranslationProvider i18nProvider) {
        logger.trace("Localization(Constructor w/ {},{}) called.", localeProvider, i18nProvider);
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
    }

    /**
     * Converts a given message into an equivalent localized message.
     *
     * @param key the message of type {@link String} to be converted,
     * @param arguments (optional) arguments being referenced within the messageString.
     * @return <B>localizedMessageString</B> the resulted message of type {@link String}.
     */
    @SuppressWarnings("null") // unexplained warnings on "arguments" use and return statement
    public String getText(String key, Object... arguments) {
        if (i18nProvider == null) {
            logger.trace("getText() returns default as no i18nProvider exists.");
            return key;
        }
        Bundle bundle = FrameworkUtil.getBundle(this.getClass()).getBundleContext().getBundle();
        Locale locale = localeProvider.getLocale();
        String defaultText = OPEN_BRACKET.concat(key).concat(CLOSE_BRACKET);

        String text = i18nProvider.getText(bundle, key, defaultText, locale, arguments);
        if (text == null) {
            logger.warn("Internal error: localization for key {} is missing.", key);
            text = defaultText;
        }
        logger.trace("getText() returns {}.", text);
        return text;
    }
}
