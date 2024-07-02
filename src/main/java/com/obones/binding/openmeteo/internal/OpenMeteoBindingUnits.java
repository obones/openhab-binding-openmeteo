package com.obones.binding.openmeteo.internal;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.spi.SystemOfUnits;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.Units;

import tech.units.indriya.AbstractSystemOfUnits;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.unit.ProductUnit;

@NonNullByDefault
public class OpenMeteoBindingUnits extends AbstractSystemOfUnits {
    private static final OpenMeteoBindingUnits INSTANCE = new OpenMeteoBindingUnits();

    public static final Unit<Dimensionless> GRAINS;
    public static Unit<?> GRAINS_PER_CUBICMETRE;
    public static Unit<?> JOULES_PER_KILOGRAM;

    static {
        GRAINS = addUnit(new ProductUnit<Dimensionless>());
        GRAINS_PER_CUBICMETRE = addUnit(ProductUnit.ofQuotient(GRAINS, tech.units.indriya.unit.Units.CUBIC_METRE));
        JOULES_PER_KILOGRAM = addUnit(ProductUnit.ofQuotient(Units.JOULE, tech.units.indriya.unit.Units.KILOGRAM));

        SimpleUnitFormat.getInstance().label(GRAINS, "grains");
        SimpleUnitFormat.getInstance().label(GRAINS_PER_CUBICMETRE, "grains/m³");
        SimpleUnitFormat.getInstance().label(JOULES_PER_KILOGRAM, "J/kg");
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    private OpenMeteoBindingUnits() {
    }

    public static SystemOfUnits getInstance() {
        return INSTANCE;
    }

    private static <U extends Unit<?>> U addUnit(U unit) {
        INSTANCE.units.add(unit);
        return unit;
    }
}
