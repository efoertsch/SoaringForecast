package org.soaringforecast.rasp.one800wxbrief.options;

import org.soaringforecast.rasp.common.Constants;
import org.soaringforecast.rasp.utils.CSVUtils;

import java.util.List;

/**
 * Used to contain 1800WXBrief Product codes, NGBV2  tailoring options
 * When used for product codes, selecting a product code means to send that code to the api
 * BUT the opposite is true for tailoring options. They are all 'EXCLUDE...', i.e. a brief has all options EXCEPT for those
 * that you send in the api. To not have user think about selected a negative(EXCLUDE...) the tailoring option  display descriptions
 * are all Include... and when a user selects an option to include the related value  the  EXCLUDE... value is NOT sent to the api
 */
public class BriefingOption {
    private String wxBriefParameterName;
    private String wxBriefParamDescription;
    private String displayDescription;
    // if true it means this option should be displayed for standard brief
    private boolean briefOption;
    // Default value (whether displayed or not) for the selected type of briefing
    private boolean selectForBrief;

    private BriefingOption(){}

    public static BriefingOption createBriefingOptionFromCSVDetail(String productCodeString, Constants.TypeOfBrief selectedTypeOfBrief) {

        List<String> briefingOptionDetails = CSVUtils.parseLine(productCodeString);
        BriefingOption briefingOption = new BriefingOption();
        try {
            briefingOption.wxBriefParameterName = briefingOptionDetails.get(0);
            briefingOption.wxBriefParamDescription = briefingOptionDetails.get(1);
            briefingOption.displayDescription = briefingOptionDetails.get(2);
            switch (selectedTypeOfBrief){
                case OUTLOOK:
                    briefingOption.briefOption = Boolean.parseBoolean(briefingOptionDetails.get(3));
                    briefingOption.selectForBrief = Boolean.parseBoolean(briefingOptionDetails.get(4));
                    break;
                case STANDARD:
                    briefingOption.briefOption = Boolean.parseBoolean(briefingOptionDetails.get(5));
                    briefingOption.selectForBrief = Boolean.parseBoolean(briefingOptionDetails.get(6));
                    break;
                case ABBREVIATED:
                    briefingOption.briefOption = Boolean.parseBoolean(briefingOptionDetails.get(7));
                    briefingOption.selectForBrief = Boolean.parseBoolean(briefingOptionDetails.get(8));
                    break;
                case NOTAMS:
                    briefingOption.briefOption = Boolean.parseBoolean(briefingOptionDetails.get(9));
                    briefingOption.selectForBrief = Boolean.parseBoolean(briefingOptionDetails.get(10));
            }
        } catch (Exception nfe) {
            briefingOption = null;
        }
        return briefingOption;
    }

    public String getWxBriefParameterName() {
        return wxBriefParameterName;
    }

    public String getWxBriefParamDescription() {
        return wxBriefParamDescription;
    }

    public String getDisplayDescription() {
        return displayDescription;
    }

    public boolean isBriefOption() {
        return briefOption;
    }

    public boolean isSelectForBrief() {
        return selectForBrief;
    }

    public void setSelectForBrief(boolean selectForBrief) {
        this.selectForBrief = selectForBrief;
    }

}
