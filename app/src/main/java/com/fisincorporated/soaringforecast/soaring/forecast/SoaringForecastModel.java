package com.fisincorporated.soaringforecast.soaring.forecast;

public class SoaringForecastModel {

    private static final String COMMA_DELIMITER = ",";

    private int id;
    private String name;
    private int numberForecastDays;

    public SoaringForecastModel() {
    }

    public SoaringForecastModel(String codeCommaName) {
        String[] values = codeCommaName.split(COMMA_DELIMITER);
        id = values.length > 0 ? Integer.parseInt(values[0]) : 0;
        name = values.length > 1 ? values[1].trim() : "";
        numberForecastDays = values.length > 2 ? Integer.parseInt(values[2]) : 1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //to display this as a string in spinner
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SoaringForecastModel) {
            SoaringForecastModel c = (SoaringForecastModel) obj;
            if (c.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    // For storing selected region in SharedPreferences
    public String toStore() {
        return new StringBuilder()
                .append(id)
                .append(COMMA_DELIMITER)
                .append(name.trim())
                .append(COMMA_DELIMITER)
                .append(numberForecastDays)
                .toString();
    }

    public int getNumberForecastDays() {
        return numberForecastDays;
    }

    public void setNumberForecastDays(int numberForecastDays) {
        this.numberForecastDays = numberForecastDays;
    }
}
