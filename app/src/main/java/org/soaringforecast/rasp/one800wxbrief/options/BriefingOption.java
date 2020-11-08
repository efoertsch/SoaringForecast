package org.soaringforecast.rasp.one800wxbrief.options;

import org.soaringforecast.rasp.utils.CSVUtils;

import java.util.List;

/**
 * Used to contain 1800WXBrief Product codes, NGBV2 or non-NGBV2 tailoring options
 * When used for product codes, selecting a product code means to send that code to the api
 * BUT the opposite is true for tailoring options. They are all 'EXCLUDE...', i.e. a brief has all options EXCEPT for those
 * that you send in the api. To not have user think about selected a negative(EXCLUDE...) the tailoring option  display descriptions
 * are all Include... and when a user selects an option to include the related value  the  EXCLUDE... value is NOT sent to the api
 */
public class BriefingOption {
    private String wxBriefParameterName;
    private String additionalParamDescription;
    private String displayDescription;
    // if true it means this option should be displayed for outlook brief
    private boolean outLookBriefOption;
    // Default value (whether displayed or not) for outlook brief
    private boolean selectForOutLookBrief;
    // if true it means this option should be displayed for standard brief
    private boolean standardBriefOption;
    // Default value (whether displayed or not) for standard brief
    private boolean selectForStandardBrief;
    // if true it means this option should be displayed for abbreviated brief
    private boolean abbreviatedBriefOption;
    // Default value (whether displayed or not) for abbreviated brief
    private boolean selectForAbbreviatedBrief;

    private BriefingOption(){}

    public static BriefingOption createBriefingOptionFromCSVDetail(String productCodeString) {

        List<String> briefingOptionDetails = CSVUtils.parseLine(productCodeString);
        BriefingOption briefingOption = new BriefingOption();
        try {
            briefingOption.wxBriefParameterName = briefingOptionDetails.get(0);
            briefingOption.additionalParamDescription = briefingOptionDetails.get(1);
            briefingOption.displayDescription = briefingOptionDetails.get(2);
            briefingOption.outLookBriefOption = Boolean.parseBoolean(briefingOptionDetails.get(3));
            briefingOption.selectForOutLookBrief = Boolean.parseBoolean(briefingOptionDetails.get(4));
            briefingOption.standardBriefOption = Boolean.parseBoolean(briefingOptionDetails.get(5));
            briefingOption.selectForStandardBrief = Boolean.parseBoolean(briefingOptionDetails.get(6));
            briefingOption.abbreviatedBriefOption = Boolean.parseBoolean(briefingOptionDetails.get(7));
            briefingOption.selectForAbbreviatedBrief = Boolean.parseBoolean(briefingOptionDetails.get(8));
        } catch (Exception nfe) {
            briefingOption = null;
        }
        return briefingOption;
    }

    public String getWxBriefParameterName() {
        return wxBriefParameterName;
    }

    public String getAdditionalParamDescription() {
        return additionalParamDescription;
    }

    public String getDisplayDescription() {
        return displayDescription;
    }

    public boolean isOutLookBriefOption() {
        return outLookBriefOption;
    }

    public boolean isSelectForOutLookBrief() {
        return selectForOutLookBrief;
    }

    public void setSelectForOutLookBrief(boolean selectForOutLookBrief) {
        this.selectForOutLookBrief = selectForOutLookBrief;
    }

    public boolean isStandardBriefOption() {
        return standardBriefOption;
    }

    public boolean isSelectForStandardBrief() {
        return selectForStandardBrief;
    }

    public void setSelectForStandardBrief(boolean selectForStandardBrief) {
        this.selectForStandardBrief = selectForStandardBrief;
    }

    public boolean isAbbreviatedBriefOption() {
        return abbreviatedBriefOption;
    }

    public boolean isSelectForAbbreviatedBrief() {
        return selectForAbbreviatedBrief;
    }
    public void setSelectForAbbreviatedBrief(boolean selectForAbbreviatedBrief) {
        this.selectForAbbreviatedBrief = selectForAbbreviatedBrief;
    }
}
