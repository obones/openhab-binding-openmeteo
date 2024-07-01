package com.obones.binding.openmeteo.internal.config;

public class OpenMeteoAirQualityThingConfiguration extends OpenMeteoBaseThingConfiguration {
    /*
     * Default values - should not be modified
     */
    public boolean airQualityIndicatorsAsString = true;

    public int hourlyHours = 48;
    public boolean hourlyTimeSeries = true;

    public boolean current = false;

    public boolean includePM10 = true;
    public boolean includePM2_5 = true;
    public boolean includeCarbonMonoxide = false;
    public boolean includeNitrogenDioxide = false;
    public boolean includeSulphurDioxide = false;
    public boolean includeOzone = false;
    public boolean includeAmmonia = false;
    public boolean includeAerosolOpticalDepth = false;
    public boolean includeDust = false;
    public boolean includeUVIndex = false;
    public boolean includeUVIndexClearSky = false;
    public boolean includeAlderPollen = false;
    public boolean includeBirchPollen = false;
    public boolean includeGrassPollen = false;
    public boolean includeMugwortPollen = false;
    public boolean includeOlivePollen = true;
    public boolean includeRagweedPollen = false;
    public boolean includeEuropeanAqi = true;
    public boolean includeEuropeanAqiPM10 = false;
    public boolean includeEuropeanAqiPM2_5 = false;
    public boolean includeEuropeanAqiNitrogenDioxide = false;
    public boolean includeEuropeanAqiOzone = false;
    public boolean includeEuropeanAqiSulphurDioxide = false;
    public boolean includeUSAqi = true;
    public boolean includeUSAqiPM10 = false;
    public boolean includeUSAqiPM2_5 = false;
    public boolean includeUSAqiNitrogenDioxide = false;
    public boolean includeUSAqiOzone = false;
    public boolean includeUSAqiSulphurDioxide = false;
    public boolean includeUSAqiCarbonMonoxide = false;
}
