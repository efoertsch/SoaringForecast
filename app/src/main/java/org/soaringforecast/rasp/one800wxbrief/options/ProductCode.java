package org.soaringforecast.rasp.one800wxbrief.options;

import org.soaringforecast.rasp.utils.CSVUtils;

import java.util.List;

public class ProductCode {
    private String wxBriefParameterName;
    private String additionalParamDescription;
    private String displayDescription;
    private boolean outLookBriefOption;
    private boolean selectForOutLookBrief;
    private boolean standardBriefOption;
    private boolean selectForStandardBrief;
    private boolean abbreviatedBriefOption;
    private boolean selectForAbbreviatedBrief;

    private ProductCode(){}

    public static ProductCode createProductCodeFromCSVDetail(String productCodeString) {

        List<String> productCodeDetails = CSVUtils.parseLine(productCodeString);
        ProductCode productCode = new ProductCode();
        try {
            productCode.wxBriefParameterName = productCodeDetails.get(0);
            productCode.additionalParamDescription = productCodeDetails.get(1);
            productCode.displayDescription = productCodeDetails.get(2);
            productCode.outLookBriefOption = Boolean.parseBoolean(productCodeDetails.get(3));
            productCode.selectForOutLookBrief = Boolean.parseBoolean(productCodeDetails.get(4));
            productCode.standardBriefOption = Boolean.parseBoolean(productCodeDetails.get(5));
            productCode.selectForStandardBrief = Boolean.parseBoolean(productCodeDetails.get(6));
            productCode.abbreviatedBriefOption = Boolean.parseBoolean(productCodeDetails.get(7));
            productCode.selectForAbbreviatedBrief = Boolean.parseBoolean(productCodeDetails.get(8));
        } catch (Exception nfe) {
            productCode = null;
        }
        return productCode;
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

    public boolean isStandardBriefOption() {
        return standardBriefOption;
    }

    public boolean isSelectForStandardBrief() {
        return selectForStandardBrief;
    }

    public boolean isAbbreviatedBriefOption() {
        return abbreviatedBriefOption;
    }

    public boolean isSelectForAbbreviatedBrief() {
        return selectForAbbreviatedBrief;
    }
}
