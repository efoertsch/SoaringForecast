package org.soaringforecast.rasp.soaring.messages;

import android.graphics.Point;

import org.soaringforecast.rasp.soaring.json.Sounding;

public class DisplaySounding {

    private Sounding sounding;
    private Point point;

    public DisplaySounding(Sounding sounding, Point point) {
        this.sounding = sounding;
        this.point = point;
    }

    public Sounding getSounding() {
        return sounding;
    }

    public Point getPoint() {
        return point;
    }
}
