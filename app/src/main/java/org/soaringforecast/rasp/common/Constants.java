package org.soaringforecast.rasp.common;

public class Constants {
    // used to create soaring forecast URLs
    //TODO Create ENUM
    public static final String BODY = "body";
    public static final String HEAD = "head";
    public static final String SIDE = "side";
    public static final String FOOT = "foot";

    public static final String SELECTED_TASK = "SELECTED_TASK";

    public enum STEP_ACTION {
        FORWARD,
        BACKWARD,
        LOOP
    }

    public enum FORECAST_SOUNDING {
        FORECAST, SOUNDING
    }

    public static final String TURNPOINT_FILE_NAME = "TURNPOINT_FILE_NAME ";
    public static final String TURNPOINT_FILE_URL = "TURNPOINT_FILE_URL";

    public static final String NEWENGLAND_REGION = "NewEngland";


    public enum TypeOfBrief {
        OUTLOOK("Outlook"),
        STANDARD("Standard"),
        ABBREVIATED("Abbreviated"),
        NOTAMS("NOTAMS");   // not an offical 1800WXBrief type of brief but an app specific type
        public static String BufferedReader;
        public final String displayValue;

        TypeOfBrief(String displayValue) {
            this.displayValue = displayValue;
        }
    }

    public enum BriefingFormat {
        NGBV2("Online(PDF)", true),
        EMAIL("EMail", true),
        SIMPLE("Simple", true);
        private String displayValue;
        private boolean validForNotABrief;
        public String getDisplayValue() {
            return displayValue;
        }
        public boolean isValidForNotABrief() {
            return validForNotABrief;
        }

        BriefingFormat(String displayValue, boolean validForNotABrief) {
            this.displayValue = displayValue;
            this.validForNotABrief = validForNotABrief;
        }
    }

    public static final String CRASH_REPORT_EMAIL = "ericfoertsch@gmail.com";

}
