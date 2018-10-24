package com.fisincorporated.soaringforecast.soaring.forecast;

public class SoaringForecastImageSet {

    private SoaringForecastImage bodyImage;
    private SoaringForecastImage headerImage;
    private SoaringForecastImage sideImage;
    private SoaringForecastImage footerImage;

    public SoaringForecastImage getBodyImage() {
        return bodyImage;
    }

    public void setBodyImage(SoaringForecastImage bodyImage) {
        this.bodyImage = bodyImage;
    }

    public SoaringForecastImage getHeaderImage() {
        return headerImage;
    }

    public void setHeaderImage(SoaringForecastImage headerImage) {
        this.headerImage = headerImage;
    }

    public SoaringForecastImage getSideImage() {
        return sideImage;
    }

    public void setSideImage(SoaringForecastImage scaleImage) {
        this.sideImage = scaleImage;
    }

    public SoaringForecastImage getFooterImage() {
        return footerImage;
    }

    public void setFooterImage(SoaringForecastImage footerImage) {
        this.footerImage = footerImage;
    }
}
