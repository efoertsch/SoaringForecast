package com.fisincorporated.soaringforecast.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;


/**
 * Contain a list of Model objects for a particular region and date
 * Created from call to e.g. http://www.soargbsc.com/rasp/NewEngland/2018-11-25/status.json
 *
 */
public class ForecastModels {

    @SerializedName("models")
    @Expose
    private List<Model> models = null;

    public List<Model> getModels() {
        return models;
    }

    public void setModels(List<Model> models) {
        this.models = models;
    }

    //--- Custom code ------------
    public Model getSelectedModel(String selectedModelName) {
        if (models != null) {
            for (Model model : models) {
                if (model.getName().equalsIgnoreCase(selectedModelName)) {
                    return model;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("models: ").append(Arrays.toString(models.toArray()))
                .toString();
    }

}
