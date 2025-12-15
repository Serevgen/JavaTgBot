package eternum.bot.model;

import java.math.BigDecimal;

public class Currency {
    private String Name;
    private BigDecimal Value;
    private BigDecimal curs;
    private int code;
    private String CharCode;
    private double unitRate;

    public Currency(int code, String Name, String CharCode, double unitRate, BigDecimal curs, BigDecimal Value) {
        this.code = code;
        this.Name = Name;
        this.CharCode = CharCode;
        this.unitRate = unitRate;
        this.curs = curs;
        this.Value = Value;
    }

    public String getName() {
        return Name;
    }

    public double getUnitRate() {
        return unitRate;
    }

    public String getCharCode() {
        return CharCode;
    }

    public int getCode() {
        return code;
    }

    public BigDecimal getCurs() {
        return curs;
    }

    public BigDecimal getValue() {
        return Value;
    }
}
