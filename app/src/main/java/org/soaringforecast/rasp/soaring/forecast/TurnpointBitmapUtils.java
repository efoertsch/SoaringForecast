package org.soaringforecast.rasp.soaring.forecast;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.utils.BitmapImageUtils;

public class TurnpointBitmapUtils {

    private static TurnpointBitmapUtils turnpointBitmapUtils;
    private static Bitmap largeBlackBitmap;
    private static Bitmap smallBlackBitmap;
    private static Bitmap largeGreenBitmap;
    private static Bitmap smallGreenBitmap;
    private static Bitmap largeRedBitmap;
    private static Bitmap smallRedBitmap;

    private TurnpointBitmapUtils() {
    }

    public static TurnpointBitmapUtils getInstance() {
        if (turnpointBitmapUtils == null) {
            turnpointBitmapUtils = new TurnpointBitmapUtils();
        }
        return turnpointBitmapUtils;
    }


    public Drawable  getDrawableTurnpointImage(Context context, Turnpoint turnpoint) {
        if (turnpoint.isGrassOrGliderAirport()) {
            return BitmapImageUtils.getDrawableFromVectorDrawable(context, R.drawable.ic_turnpoint_green_32dp);
        } else if (turnpoint.isHardSurfaceAirport()) {
            return BitmapImageUtils.getDrawableFromVectorDrawable(context, R.drawable.ic_turnpoint_black_32dp);
        } else {
            return BitmapImageUtils.getDrawableFromVectorDrawable(context, R.drawable.ic_turnpoint_red_32dp);
        }
    }

    public Bitmap getSizedTurnpointBitmap(Context context, Turnpoint turnpoint, int zoomLevel) {
        Bitmap startingTurnpointBitmap;
        int sizingRatio = 1;
        if (zoomLevel <= 8 || zoomLevel > 9) {
            if (turnpoint.isGrassOrGliderAirport()) {
                if (largeGreenBitmap == null) {
                    largeGreenBitmap = BitmapImageUtils.getBitmapFromVectorDrawable(context, R.drawable.ic_turnpoint_green_48dp);
                }
                startingTurnpointBitmap = largeGreenBitmap;
            } else if (turnpoint.isHardSurfaceAirport()) {
                if (largeBlackBitmap == null) {
                    largeBlackBitmap = BitmapImageUtils.getBitmapFromVectorDrawable(context, R.drawable.ic_turnpoint_black_48dp);
                }
                startingTurnpointBitmap = largeBlackBitmap;
            } else {
                if (largeRedBitmap == null) {
                    largeRedBitmap = BitmapImageUtils.getBitmapFromVectorDrawable(context, R.drawable.ic_turnpoint_red_48dp);
                }
                startingTurnpointBitmap = largeRedBitmap;
            }

        } else {
            // zoomlevel 9 no resizing
            if (turnpoint.isGrassOrGliderAirport()) {
                if (smallGreenBitmap == null) {
                    smallGreenBitmap = BitmapImageUtils.getBitmapFromVectorDrawable(context, R.drawable.ic_turnpoint_green_32dp);
                }
                startingTurnpointBitmap = smallGreenBitmap;
            } else if (turnpoint.isHardSurfaceAirport()) {
                if (smallBlackBitmap == null) {
                    smallBlackBitmap = BitmapImageUtils.getBitmapFromVectorDrawable(context, R.drawable.ic_turnpoint_black_32dp);
                }
                startingTurnpointBitmap = smallBlackBitmap;
            } else {
                if (smallRedBitmap == null) {
                    smallRedBitmap = BitmapImageUtils.getBitmapFromVectorDrawable(context, R.drawable.ic_turnpoint_red_32dp);
                }
                startingTurnpointBitmap = smallRedBitmap;
            }

        }
        if (zoomLevel <= 7) {
            sizingRatio = 3;
        } else if (zoomLevel == 8) {
            sizingRatio = 2;
        } else if (zoomLevel >= 9) {
            sizingRatio = 1;
        }
        // note only bitmaps for zoom level  9 or more don't get resized
        return Bitmap.createScaledBitmap(startingTurnpointBitmap
                , startingTurnpointBitmap.getWidth() / sizingRatio, startingTurnpointBitmap.getHeight() / sizingRatio
                , true);

    }
}
