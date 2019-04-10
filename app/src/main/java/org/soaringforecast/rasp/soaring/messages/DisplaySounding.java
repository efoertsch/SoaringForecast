package org.soaringforecast.rasp.soaring.messages;

import org.soaringforecast.rasp.soaring.json.Sounding;

public class DisplaySounding {

    private Sounding sounding;

    public DisplaySounding(Sounding sounding) {
        this.sounding = sounding;
    }

    public Sounding getSounding() {
        return sounding;
    }
}
