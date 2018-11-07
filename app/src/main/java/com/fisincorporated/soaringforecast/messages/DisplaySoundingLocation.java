package com.fisincorporated.soaringforecast.messages;

import com.fisincorporated.soaringforecast.soaring.json.SoundingLocation;

public class DisplaySoundingLocation {

    private SoundingLocation soundingLocation;

    public DisplaySoundingLocation(SoundingLocation soundingLocation) {
        this.soundingLocation = soundingLocation;
    }
}
