package com.fisincorporated.aviationweather.data.metars;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "sky_condition", strict = false)
public class SkyCondition {

    @Attribute(name = "sky_cover", required = false)
    private String skyCover;

    @Attribute(name = "cloud_base_ft_agl", required = false)
    private String cloudBaseFtAgl;

    public String getSkyCover() {
        return this.skyCover;
    }

    public void setSkyCover(String value) {
        this.skyCover = value;
    }

    public String getCloudBaseFtAgl() {
        return this.cloudBaseFtAgl;
    }

    public void setCloudBaseFtAgl(String value) {
        this.cloudBaseFtAgl = value;
    }

}
