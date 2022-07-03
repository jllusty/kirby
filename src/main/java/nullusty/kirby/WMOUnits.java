package nullusty.kirby;

import javax.measure.Quantity;
import javax.measure.Unit;

import static tec.units.ri.unit.Units.*;

// class of helper methods for working with WMOUnitCodes
public class WMOUnits {
    // non-instantiable
    private WMOUnits() {}

    public static <Q extends Quantity<Q>> Unit<Q> convertWMOUnitCodeStringToUnit(String wmoUnitCode) throws ClassNotFoundException {
        switch(wmoUnitCode) {
            case "wmoUnit:degC":
                return (Unit<Q>) CELSIUS;
            case "wmoUnit:Pa":
                return (Unit<Q>) PASCAL;
            case "wmoUnit:m":
                return (Unit<Q>) METRE;
            default:
                throw new ClassNotFoundException(String.format("wmoUnitCode: '%s' is unknown", wmoUnitCode));
        }
    }
}

