package com.fisincorporated.soaringforecast.messages;

import com.fisincorporated.soaringforecast.soaring.json.Sounding;

public class DisplaySounding {

    private Sounding sounding;

    public DisplaySounding(Sounding sounding) {
        this.sounding = sounding;
    }

    public Sounding getSounding() {
        return sounding;
    }
}
