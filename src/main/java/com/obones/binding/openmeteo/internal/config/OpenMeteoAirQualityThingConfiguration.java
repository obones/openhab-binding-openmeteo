package com.obones.binding.openmeteo.internal.config;

public class OpenMeteoAirQualityThingConfiguration extends OpenMeteoBaseThingConfiguration {
    /*
     * Default values - should not be modified
     */
    public int hourlyHours = 48;
    public boolean hourlyTimeSeries = true;

    public boolean current = false;

    public boolean includePM10 = true;
    public boolean includePM2_5 = true;
    public boolean includeCarbonMonoxide = true;
    public boolean includeNitrogenDioxide = true;
    public boolean includeSulphurDioxide = true;
    public boolean includeOzone = true;
    public boolean includeAmmonia = true;
    public boolean includeAerosolOpticalDepth = true;
    public boolean includeDust = true;
    public boolean includeUVIndex = true;
    public boolean includeUVIndexClearSky = true;
    public boolean includeAlderPollen = true;
    public boolean includeBirchPollen = true;
    public boolean includeGrassPollen = true;
    public boolean includeMugwortPollen = true;
    public boolean includeOlivePollen = true;
    public boolean includeRagweedPollen = true;
    public boolean includeEuropeanAqi = true;
    public boolean includeEuropeanAqiPM10 = true;
    public boolean includeEuropeanAqiPM2_5 = true;
    public boolean includeEuropeanAqiNitrogenDioxide = true;
    public boolean includeEuropeanAqiOzone = true;
    public boolean includeEuropeanAqiSulphurDioxide = true;
    public boolean includeUSAqi = true;
    public boolean includeUSAqiPM10 = true;
    public boolean includeUSAqiPM2_5 = true;
    public boolean includeUSAqiNitrogenDioxide = true;
    public boolean includeUSAqiOzone = true;
    public boolean includeUSAqiSulphurDioxide = true;
    public boolean includeUSAqiCarbonMonoxide = true;
}
