package com.fisincorporated.soaringforecast.soaring.json;

/**
 * ModelForecastDate will contain everything needed to construct the url for the forecast bitmap and map the results
 * Note this is not a JSON object
 */
public class ModelForecastDate  {
    private int index;
    private String regionName;    // "NewEngland"
    private Model model;          // GFS, times, center, corners
    private String printDate;    //"Friday March 30"
    private String date;        //"2018-03-30"

    public ModelForecastDate(int index, String regionName,Model model, String printDate, String date) {
       this.index = index;
       this.regionName = regionName;
        this.model = model;
        this.printDate = printDate;
        this.date = date;
    }

    public int getIndex() {
        return index;
    }

    public Model getModel() {
        return model;
    }

    public String getPrintDate() {
        return printDate;
    }

    public String getDate() {
        return date;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModelForecastDate) {
            ModelForecastDate c = (ModelForecastDate) obj;
            return c.model.equals(model) && c.printDate.equals(printDate);
        }
        return false;
    }

    public String getRegionName() {
        return regionName;
    }

    // For spinner date display
    @Override
    public String toString() {
        return printDate;
    }



}
