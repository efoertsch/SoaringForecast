package com.fisincorporated.soaringforecast.windy;

public class WindyModel {

    private static final String COMMA_DELIMITER = ",";

    private int id;
    private String code;
    private String name;

    public WindyModel (String codeCommaName) {
        String[] values = codeCommaName.split(COMMA_DELIMITER);
        id =  values.length > 0  ? Integer.parseInt(values[0]) : 0;
        code = values.length > 1 ? values[1].trim() : "";
        name = values.length > 2 ? values[2].trim() : "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
        if (obj instanceof WindyModel) {
            WindyModel c = (WindyModel) obj;
            return c.getCode().equals(code) && c.getName().equals(name);
        }

        return false;
    }

    // For storing selected model in SharedPreferences
    public String toStore(){
        return id + COMMA_DELIMITER + code.trim() + COMMA_DELIMITER + name.trim();
    }
}
