package com.obones.binding.openmeteo.internal.config;

public class OpenMeteoMarineForecastThingConfiguration extends OpenMeteoBaseThingConfiguration {
    /*
     * Default values - should not be modified
     */
    public int hourlyHours = 48;
    public boolean hourlyTimeSeries = true;

    public int dailyDays = 5;
    public boolean dailyTimeSeries = true;

    public boolean current = false;

    public boolean includeWaveHeight = true;
    public boolean includeWindWaveHeight = true;
    public boolean includeSwellWaveHeight = true;
    public boolean includeSecondarySwellWaveHeight = false;
    public boolean includeTertiarySwellWaveHeight = false;
    public boolean includeWaveDirection = true;
    public boolean includeWindWaveDirection = true;
    public boolean includeSwellWaveDirection = true;
    public boolean includeSecondarySwellWaveDirection = false;
    public boolean includeTertiarySwellWaveDirection = false;
    public boolean includeWavePeriod = true;
    public boolean includeWindWavePeriod = true;
    public boolean includeSwellWavePeriod = true;
    public boolean includeSecondarySwellWavePeriod = false;
    public boolean includeTertiarySwellWavePeriod = false;
    public boolean includeWindWavePeakPeriod = true;
    public boolean includeSwellWavePeakPeriod = true;
    public boolean includeOceanCurrentVelocity = true;
    public boolean includeOceanCurrentDirection = true;
    public boolean includeSeaSurfaceTemperature = true;
    public boolean includeSeaLevelHeightMsl = false;
    public boolean includeInvertBarometerHeight = false;
}
