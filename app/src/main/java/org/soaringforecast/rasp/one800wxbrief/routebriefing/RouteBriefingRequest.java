package org.soaringforecast.rasp.one800wxbrief.routebriefing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import timber.log.Timber;

/**
 * A much simplified class to hold values needed to make the routeBriefing call to the 1800wxbrief
 * routeBriefing api
 * See https://www.1800wxbrief.com/Website/resources/doc/WebService.xml#op.idp140237337565664 for
 * valid values and definition
 */
public class RouteBriefingRequest {

    private static final String AMPERSAND = "&";
    public static final  SimpleDateFormat wxbriefTimeFormatter =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");


    private ArrayList<String> turnpointNames;

    private RouteBriefingRequest() {
    }

    public static RouteBriefingRequest newInstance() {
        RouteBriefingRequest routeBriefingRequest = new RouteBriefingRequest();
        return routeBriefingRequest;
    }

    public void setTurnpointNames(ArrayList<String> turnpointNames) {
        turnpointNames = turnpointNames;
        if (turnpointNames.size() > 0) {
            departure = turnpointNames.get(0);
        }
        if (turnpointNames.size() > 1) {
            destination = turnpointNames.get(turnpointNames.size() - 1);
        }
        setRoute(turnpointNames);
        createProductCodeList();
        createTailoringOptionList();

    }

    private enum TypeOfBrief{
        OUTLOOK("Outlook"),
        STANDARD("Standard"),
        ABBREVIATED("Abbreviated");

        private final String displayValue;

        TypeOfBrief(String displayValue) {
            this.displayValue = displayValue;
        }
    }

    public List<String> getTypeOfBriefs(){
        ArrayList<String> typeOfBriefs = new ArrayList<>();
        for (TypeOfBrief typeOfBrief: TypeOfBrief.values()){
            typeOfBriefs.add(typeOfBrief.displayValue);
        }
        return typeOfBriefs;
    }

    public void setTypeOfBrief(int position){

    }


    /**
     * The briefing preferences element is a JSON string containing the desired briefing products, tailoring options, and a plain text parameter.
     * Usage:
     * {"items":["productCode","productCode",...,"productCode"],"plainText":true,"tailoring":["tailoringOption","tailoringOption",...,"tailoringOption"]}
     * <p>
     * Note: The items parameter is not supported by SIMPLE briefingType. If it is not specified, all briefing products will be included.
     * The plainText parameter is not supported by NGBv2 briefingType.
     * If it is not specified, it will default to false. If it is true, then plain text will be included regardless of the tailoring options.
     * <p>
     * Example for NGBv2 briefingType:
     * {"items":["DD_NTM","SEV_WX","METAR","PIREP"],"tailoring":["EXCLUDE_GRAPHICS","EXCLUDE_HISTORICAL_METARS"]}
     * The returned briefing will include only the following products: Closed/Unsafe NOTAMs, Severe Weather, METARs, and Pilot Reports. Also, it will not include graphics nor historical METARs.
     * <p>
     * Note: If there is a conflict between a tailoring option and a product item, the tailoring option takes precedence.
     * For example, a briefing with the following briefingPreferences will include only Synopsis:
     * {"items":["SYNS","WH"],"tailoring":["EXCLUDE_NHC_BULLETIN"]}
     */
    private String briefingPreferences;

    /**
     * Product codes that can go into briefingPrefences  items array
     * {"items":["productCode","productCode",...,"productCode"], ...
     * <p>
     * Does not apply to  SIMPLE briefingType.
     */
    private enum ProductCode {
        DD_NTM("Closed/Unsafe NOTAMs"),
        TFR("Temporary Flight Restrictions"),
        FA("Area Forecast"),
        FB("Winds Aloft"),
        METAR("METARs"),
        TAF("Terminal Forecast"),
        UOA("UAS Operating Area"),
        DEP_NTM("Departure NOTAM"),
        SEV_WX("Severe Weather"),
        WST("Convective SIGMET"),
        IFR("IFR AIRMET"),
        ICING("Icing AIRMET"),
        TURBO_LO("Turbulence Low Altitude AIRMET"),
        WINDS_GT_30("Winds Over 30 Knots AIRMET"),
        OTHER("Other AIRMET"),
        UUA("Urgent Pilot Report"),
        CC("Graphical Area Forecast Cloud Coverage"),
        DEST_NTM("Destination NOTAM"),
        ALTN2_NTM("Alternate 2 NOTAM"),
        ENROUTE_NTM_COM("Communication NOTAM"),
        ENROUTE_NTM_OBST("Obstruction NOTAM"),
        ENROUTE_NTM_SUA("Special Use Airspace NOTAM"),
        ENROUTE_NTM_OTHER("Other/Unverified NOTAM"),
        GENFDC_NTM("General FDC NOTAM"),
        UNCATEGORIZED_NTM("Uncategorized NOTAM"),
        WH("NHC Bulletins"),
        WS("SIGMET"),
        MTN_OBSCN("Mountain Obscuration AIRMET"),
        FZLVL("Freezing Level AIRMET"),
        TURBO_HI("Turbulence High Altitude AIRMET"),
        LLWS("Low Level Wind Shear AIRMET"),
        CWA("Center Weather Advisory"),
        SYNS("Synopsis"),
        PIREP("Pilot Reports"),
        VIS("Graphical Area Forecast Vis, Sfc. Winds, and Precip"),
        ALTN1_NTM("Alternate 1 NOTAM"),
        ENROUTE_NTM_NAV("Navigation NOTAM"),
        ENROUTE_NTM_SVC("Service NOTAM"),
        ENROUTE_NTM_AIRSPACE("Airspace NOTAM"),
        ENROUTE_NTM_RWY_TWY_APRON_AD_FDC("Runway/Taxiway/Apron/Aerodome/FDC NOTAM"),
        ENROUTE_NTM_MIL("Military NOTAM"),
        INTL_NTM("International NOTAM"),
        ATCSCC("Air Traffic Control System Command Center"),
        AC("Convective Outlook"),
        VAA("Volcanic Ash Advisory (applicable to NGBV2 briefing type, or versions prior to 20171207)");

        private final String description;

        ProductCode(String description) {
            this.description = description;
        }
    }

    /**
     * User selected productCodes (NGBV2 or non-NGB) to generate the items in briefingPreferences
     * eg.
     * {"items":["productCode","productCode",...,"productCode"], ...
     * {"items":["DD_NTM","SEV_WX","METAR","PIREP"], ...
     */
    private ArrayList<String> productCodes = new ArrayList<>();

    private static final ArrayList<ProductCode> defaultProductCodes = new ArrayList<>(Arrays.asList(
            ProductCode.DD_NTM,
            ProductCode.TFR,
            ProductCode.FA));

    private ArrayList<String> productCodeList = new ArrayList<>();
    private ArrayList<String> productCodeDescriptionList = new ArrayList<>();
    private boolean[] selectedProductCodes = new boolean[]{};

    /**
     * Return list of tailoring options based on the selected briefing type
     * Also note which options are default selected
     *
     * @return
     */
    public synchronized void createProductCodeList() {
        productCodeList = new ArrayList<>();
        productCodeDescriptionList = new ArrayList<>();
        selectedProductCodes = new boolean[ProductCode.values().length];
        ProductCode[] productCodes = ProductCode.values();
        for (int i = 0; i < productCodes.length; ++i) {
            productCodeList.add(productCodes[i].name());
            productCodeDescriptionList.add(productCodes[i].description);
            selectedProductCodes[i] = defaultProductCodes.contains(productCodes[i]);
        }
    }

    public List<String> getProductCodeDescriptionList() {
        return productCodeDescriptionList;
    }

    public synchronized boolean[] getSelectedProductCodes() {
        if (selectedProductCodes.length == productCodeList.size()) {
            return selectedProductCodes;
        }
        // something went wrong
        selectedProductCodes = new boolean[productCodeList.size()];
        for (int i = 0; i < productCodeList.size(); ++i) {
            selectedProductCodes[i] = false;
        }
        return selectedProductCodes;
    }

    public synchronized void setSelectedProductCodes(boolean[] selected) {
        if (selectedProductCodes.length == selected.length) {
            selectedProductCodes = selected.clone();
        }
    }

    /**
     * Tailoring options for non-NGBv2 briefing type (Email,NGB) briefingType that can go into the  tailoring array in briefingPreferences
     * {"items":[...],"plainText":true,"tailoring":["tailoringOption","tailoringOption",...,"tailoringOption"]}
     */

    private enum TailoringOptionNonNGBV2 {
        ENCODED_ONLY("Include encoded data"),
        PLAINTEXT_ONLY("Include plaintext"),
        NO_GEOMETRY("Do not include Geometry data"),
        NO_SUMMARIZATION_PASSING_TIMES("Do not include Summarization and passing times"),
        NO_NOTAM_CATEGORIZATION("Do not include inside / outside corridor categorization for NOTAMs"),
        NO_TFR_NOTAM_RECORD("Do not include NotamRecord for TFRs"),
        NO_FIX_LIST("Do not include the fix list in a NGB Weather Briefing object");

        private final String description;

        TailoringOptionNonNGBV2(String description) {
            this.description = description;
        }
    }

    private static final ArrayList<TailoringOptionNonNGBV2> defaultTailorOptionNonNGBV2 = new ArrayList<>(Arrays.asList(
            TailoringOptionNonNGBV2.ENCODED_ONLY
    ));


    /**
     * * Tailoring options for NGBv2 briefingType that can go into the  tailoring array in briefingPreferences
     * {"items":["productCode","productCode",...,"productCode"],"plainText":true,"tailoring":["tailoringOption","tailoringOption",...,"tailoringOption"]}
     */
    private enum TailoringOptionNGBV2 {
        EXCLUDE_GRAPHICS("", "Exclude graphics"),
        EXCLUDE_HISTORICAL_METARS("", "Exclude historical METARs"),
        EXCLUDE_NEXTGEN("", "Exclude Nextgen content"),
        EXCLUDE_PLAINTEXT("", "Exclude plaintext"),
        EXCLUDE_ENROUTE_METARS_TAFS("", "Exclude en route METARs and TAFs (only if the filed altitude is at least 18,000ft)"),
        EXCLUDE_FAR_WINDS_ALOFT("", "Exclude Winds Aloft data not within 4000ft of the filed altitude"),
        EXCLUDE_LOW_ENROUTE_OBSTRUCTIONS("", "Exclude en route obstructions more than 1000ft below the filed altitude"),
        EXCLUDE_GFA_BEYOND_DEP_TIME("", "Exclude Graphical Forecast beyond the departure time"),
        EXCLUDE_FLOW_CONTROL("", "Exclude Flow Control Messages"),
        EXCLUDE_NHC_BULLETIN("", "Exclude NHC Bulletin"),
        EXCLUDE_NON_LOCATION_FDC_NOTAM("", "Exclude non-location FDC NOTAMs"),
        EXCLUDE_STATE_DEPARTMENT_NOTAM("", "Exclude State Department NOTAMs"),
        EXCLUDE_MILITARY_NOTAM("", "Exclude Military NOTAMs"),
        EXCLUDE_ENROUTE_NAV_VOR("", "Exclude en route navigational VOR NOTAMs"),
        // Note the following parm should actually be EXCLUDE_ENROUTE_NAV_VOR-DME ( - between VOR-DME)
        // hence need to have actualParmValue field
        EXCLUDE_ENROUTE_NAV_VOR_DME("EXCLUDE_ENROUTE_NAV_VOR-DME", "Exclude en route navigational VOR-DME NOTAMs"),
        EXCLUDE_ENROUTE_NAV_VORTAC("", "Exclude en route navigational VORTAC NOTAMs"),
        EXCLUDE_ENROUTE_NAV_NDB("", "Exclude en route navigational NDB NOTAMs"),
        EXCLUDE_ENROUTE_NAV_DME("", "Exclude en route navigational DME NOTAMs"),
        EXCLUDE_ENROUTE_NAV_TACAN("", "Exclude en route navigational TACAN NOTAMs"),
        EXCLUDE_ENROUTE_NAV_ILS("", "Exclude en route navigational ILS NOTAMs"),
        EXCLUDE_ENROUTE_NAV_OTHER("", "Exclude other en route navigational NOTAMs");

        private final String actualParmValue;
        private final String description;

        TailoringOptionNGBV2(String actualParmValue, String description) {
            this.actualParmValue = actualParmValue;
            this.description = description;
        }
    }

    private static final ArrayList<TailoringOptionNGBV2> defaultTailoringOptionNGBV2 = new ArrayList<>(Arrays.asList(
            TailoringOptionNGBV2.EXCLUDE_GRAPHICS,
            TailoringOptionNGBV2.EXCLUDE_HISTORICAL_METARS));

    private ArrayList<String> tailoringOptionList = new ArrayList<>();
    private ArrayList<String> tailoringOptionDescriptionList = new ArrayList<>();
    private boolean[] selectedTrailoringOptions = new boolean[]{};

    /**
     * Return list of tailoring options based on the selected briefing type
     * Also not which options are default selected
     *
     * @return
     */
    public synchronized void createTailoringOptionList() {
        tailoringOptionList = new ArrayList<>();
        tailoringOptionDescriptionList = new ArrayList<>();
        if (selectedBriefingType.equals(BriefingType.NGBV2)) {
            selectedTrailoringOptions = new boolean[TailoringOptionNGBV2.values().length];
            TailoringOptionNGBV2[] tailoringOptions = TailoringOptionNGBV2.values();
            for (int i = 0; i < tailoringOptions.length; ++i) {
                tailoringOptionList.add(tailoringOptions[i].name());
                tailoringOptionDescriptionList.add(tailoringOptions[i].description);
                selectedTrailoringOptions[i] = defaultTailoringOptionNGBV2.contains(tailoringOptions[i]);
            }
        } else {
            selectedTrailoringOptions = new boolean[TailoringOptionNonNGBV2.values().length];
            TailoringOptionNonNGBV2[] tailoringOptions = TailoringOptionNonNGBV2.values();
            for (int i = 0; i < tailoringOptions.length; ++i) {
                tailoringOptionList.add(tailoringOptions[i].name());
                tailoringOptionDescriptionList.add(tailoringOptions[i].description);
                selectedTrailoringOptions[i] = defaultTailoringOptionNGBV2.contains(tailoringOptions[i]);
            }

        }
    }

    public List<String> getTailoringOptionDescriptionsList() {
        return tailoringOptionDescriptionList;
    }

    /**
     * Should be called immediately after getTailoringOptions()
     *
     * @return
     */
    public synchronized boolean[] getSelectedTailoringOptions() {
        if (selectedTrailoringOptions.length == tailoringOptionList.size()) {
            return selectedTrailoringOptions;
        }
        // something went wrong
        if (selectedBriefingType.equals(BriefingType.NGBV2)) {
            selectedTrailoringOptions = new boolean[TailoringOptionNGBV2.values().length];
        } else {
            selectedTrailoringOptions = new boolean[TailoringOptionNonNGBV2.values().length];
        }

        for (int i = 0; i < tailoringOptionList.size(); ++i) {
            selectedTrailoringOptions[i] = false;
        }
        return selectedTrailoringOptions;
    }

    public synchronized void setSelectedTailoringOptions(boolean[] selected) {
        if (selected.length == selectedTrailoringOptions.length) {
            selectedTrailoringOptions = selected.clone();
        }
    }


    /**
     * User selected tailoring Options to generate the tailoringOptions list in briefingPreferences
     * <p>
     * eg.
     * ... ,"tailoring":["tailoringOption","tailoringOption",...,"tailoringOption"]
     * ... ,"tailoring":["EXCLUDE_GRAPHICS","EXCLUDE_HISTORICAL_METARS"]
     */
    private ArrayList<String> tailoringOptions = new ArrayList<>();


    /**
     * One of
     * enum { 'RAW', 'HTML', 'SIMPLE', 'NGB', 'EMAIL', 'SUMMARY', 'NGBV2' }
     * numeration to indicate format of the briefing response.
     * SUMMARY and HTML types are for internal Leidos use only, and are disabled for
     * external customers. RAW type has been deprecated.
     */
    private BriefingType selectedBriefingType;

    public enum BriefingType {
        EMAIL("EMail"),
        SIMPLE("Simple"),
        NGB("Next Gen Brief"),
        NGBV2("Online(PDF)");

        private String displayValue;

        public String getDisplayValue() {
            return displayValue;
        }

        BriefingType(String displayValue) {
            this.displayValue = displayValue;
        }
    }

    public enum TimeZoneAbbrev{AST, ADT, EST, EDT, CST, CDT,  MST,  MDT,  PST
        , PDT, AKST, AKDT, HST, UTC}



    /**
     * REST calls require type, DOMESTIC (being deprecated or ICAO)
     */
    private static String type = "ICAO";

    /**
     * Of course only VFR
     */
    private static String flightRules = "VFR";

    private String departure;

    private String departureInstant;

    private String destination;

    /**
     * Default to 5 hr flight
     */
    private String flightDuration = "PT05H";

    /**
     * Route  (airports along task
     * eg. KAFN, KEEN  (Jaffrey and Dillant-Hopkins)
     */
    private String route;

    /**
     * Glider N number
     */
    private String aircraftIdentifier;

    /**
     * Nautical miles
     * Integer with restriction minInclusive(25) maxInclusive(100)
     */
    private String routeCorridorWidth = "25";

    private String speedKnots = "50";

    /**
     * Optional but we want more definitive error info if error does occur
     */
    private Boolean includeCodedMessages = true;

    /**
     * Optional
     * When briefingType of NGBV2 is requested and the outLookBriefing parameter is included and
     * set to TRUE, it allows the vendor to request NGBV2 briefing with the briefing format is
     * NGBv2 HTML or NGBv2 PDF, as specified by the briefingResultFormat parameter and the briefing
     * contains only Outlook products. When briefingType of NGBV2 is requested and the
     * outLookBriefing parameter is included and set to false or not included at all,
     * Standard briefing is returned.
     */
    private Boolean outlookBriefing = false;

    /**
     * Optional - but we set to false
     * If the notABriefing parameter is set to true, it allows a vendor to request weather and
     * NOTAM data without an associated pilot or flight plan. A web service call made with
     * notABriefing set to true will not be retained on our system for the purposes of retaining
     * historical briefing data for a pilot or accident reconstruction. If set to false or not
     * provided, the aircraftIdentifier is required and a briefing is recorded in our system.
     * If the notABriefing parameter is set to either true or false, it will result in a minimum
     * versionRequested value of 20160225.
     */

    private Boolean notABriefing = false;

    /**
     * Optional but must be valid version
     */
    private String versionRequested = "99999999";

    /**
     * The briefingResultFormat is an optional parameter that applies only to briefingType of NGBV2
     * and for this type of briefing it must be specified. If briefingResultFormat is specified as
     * PDF, the returned briefing will be a base64 encoded string of the PDF, containing the NGBv2
     * briefing. The HTML format is for internal Leidos use only, and is disabled for external
     * customers. If briefingResultFormat is specified as HTML, the returned briefing will be HTML
     * with NGBv2 contents and formatting
     */
    private String briefingResultFormat = "PDF";

    /**
     * Required only if record of briefing to be filed with 1800WXBrief
     * Email address
     */
    private String webUserName;

    /**
     * The emailAddress element is a comma separated list of email addresses.
     * These are the addresses that the email will be sent to.
     * This parameter applies to EMAIL briefings only.
     * For our purposes emailAddress = webUserName
     */
    private String emailAddress;

    /**
     * Nautical Miles
     * Integer with restriction minInclusive(100) maxInclusive(600)
     */
    private String windsAloftCorridorWidth = "100";

    /**
     * Optional
     * If includeFlowControlMessages is set to true, flow control messages are included in the briefing response. If set to false or not provided, flow control messages are not included in the briefing response. This parameter applies to SIMPLE briefings.
     * This parameter will be deprecated in the future. If items are specified in briefingPreferences, add the product code "ATCSCC" to include flow control messages.
     */
    private Boolean includeFlowControlMessages;

    /**
     * Optional
     * If includeNhcBulletins is set to true, NHC Bulletins are included in the briefing response. If set to false or not provided, NHC Bulletins are not included in the briefing response. This parameter applies to SIMPLE briefings.
     * This parameter will be deprecated in the future. If items are specified in briefingPreferences, add the product code "WH" to include NHC Bulletins.
     */
    private Boolean includeNhcBulletins;

    /**
     * Optional
     * If includeNonLocationFdcNotams is set to true, FDC NOTAMs that have no location are included in the briefing response. If set to false or not provided, FDC NOTAMs that have no location are not included in the briefing response. This parameter applies to SIMPLE briefings.
     * If items are specified in briefingPreferences, the product code "GENFDC_NTM" must be included.
     */
    private Boolean includeNonLocationFdcNotams;

    /**
     * Optional
     * f includeStateDeptNotams is set to true, KZZZ NOTAMs are included in the briefing response. If set to false or not provided, KZZZ NOTAMs are not included in the briefing response. This parameter applies to SIMPLE briefings.
     * If items are specified in briefingPreferences, the product code "GENFDC_NTM" must be included.
     */
    private Boolean includeStateDeptNotams;

    /**
     * Optional
     * If includeMilitaryNotams is set to true, Military NOTAMs are included in the briefing response. If set to false or not provided, Military NOTAMs are not included in the briefing response. This parameter applies to SIMPLE briefings.
     * This parameter will be deprecated in the future. If items are specified in briefingPreferences, add the product code "ENROUTE_NTM_MIL" to include Military NOTAMs.
     */
    private Boolean includeMilitaryNotams;

    /**
     * Not documented but was part of wdsl. New field to be implemented?
     */
    private String includeDecisionTool;

    /**
     * Optional
     * If briefingType is SIMPLE, wrapColumn specifies column at which to wrap long lines.
     * Use 0 to indicate no wrap. Default wrap is column 78
     */
    private String wrapColumn;

    /**
     * Optional
     * If plainText is set to true, the briefing output is returned with plain text translation.
     * If set to false or not provided, the briefing output will remain encoded.
     * This parameter applies to SIMPLE briefings only.
     */
    private Boolean plainText;

    /**
     * Optional
     * type string with restriction - enum { 'AST', 'ADT', 'EST', 'EDT', 'CST', 'CDT', 'MST', 'MDT'
     * , 'PST', 'PDT', 'AKST', 'AKDT', 'HST', 'UTC' }
     * <p>
     * For NGB and EMAIL briefings, if plainTextTimeZone is set to a supported timezone
     * , the briefing output is returned with plain text translations containing both the zulu time
     * and the supplied timezone for dates and times. For SIMPLE and HTML briefings,
     * if plainTextTimeZone is set to a supported timezone, and plainText is set to true,
     * the briefing output is returned with plain text translations containing both the zulu time
     * and the supplied timezone for dates and times. If not provided or set to "UTC",
     * all times will be in zulu time.
     */
    private String plainTextTimeZone;

    public Boolean getNotABriefing() {
        return notABriefing;
    }

    public void setNotABriefing(Boolean notABriefing) {
        this.notABriefing = notABriefing;
    }

    public ArrayList<String> getBriefingTypeList(){
        ArrayList<String> briefingTypes = new ArrayList<>();
        for(BriefingType briefingType : BriefingType.values()){
            if (notABriefing
                    && (briefingType == BriefingType.EMAIL || briefingType == BriefingType.SIMPLE)){
                // bypass
            } else {
                briefingTypes.add(briefingType.getDisplayValue());
            }
        }
        return briefingTypes;
    }

    public BriefingType getBriefTypeBasedOnDisplayValue(String displayValue){
        for (BriefingType briefingType : BriefingType.values()){
            if (briefingType.displayValue.equals(displayValue)){
                return briefingType;
            }
        }
        // Oh-oh something really wrong
        return null;
    };

    public void setTailoringOptions(ArrayList<String> tailoringOptions) {
        this.tailoringOptions = tailoringOptions;
    }

    public void setProductCodes(ArrayList<String> productCodes) {
        this.productCodes = productCodes;
    }

    /**
     * Departure airport
     * eg 3B3
     *
     * @param departure
     */
    public void setDeparture(String departure) {
        this.departure = departure;
    }

    /**
     * Destination airport
     * eg. 3B3
     *
     * @param destination
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Approx departure time (Zulu)
     * eg. 2020-08-08T23:59:00.0
     *
     * @param departureInstant
     */
    public void setDepartureInstant(String departureInstant) {
        this.departureInstant = departureInstant;
    }

    /**
     * Format eg. PT04H
     *
     * @param flightDuration
     */
    public void setFlightDuration(String flightDuration) {
        this.flightDuration = flightDuration;
    }

    /**
     * Comma separated flight route (not including departure and destination
     * eg. KAFN,KEEN
     *
     * @param route
     */
    public void setRoute(String route) {
        this.route = route;
    }

    public void setRoute(ArrayList<String> turnpointNames) {
        StringBuilder sb = new StringBuilder();
        if (turnpointNames.size() > 2) {
            for (int i = 1; i < turnpointNames.size() - 1; ++i) {
                sb.append(turnpointNames.get(i)).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        route = sb.toString();
    }

    /**
     * Aircraft N number
     * eg 'N68RM'
     *
     * @param aircraftIdentifier
     */
    public void setAircraftIdentifier(String aircraftIdentifier) {
        this.aircraftIdentifier = aircraftIdentifier;
    }

    /**
     * In positive decimal number in nautical miles
     *
     * @param routeCorridorWidth
     */
    public void setRouteCorridorWidth(String routeCorridorWidth) {
        this.routeCorridorWidth = routeCorridorWidth;
    }

    public void setOutlookBriefing(Boolean outlookBriefing) {
        this.outlookBriefing = outlookBriefing;
    }

    public void setSelectedBriefingType(BriefingType selectedBriefingType) {
        this.selectedBriefingType = selectedBriefingType;
    }

    public BriefingType getSelectedBriefingType() {
        return selectedBriefingType;
    }

    public void setBriefingResultFormat(String briefingResultFormat) {
        this.briefingResultFormat = briefingResultFormat;
    }

    public void setWebUserName(String webUserName) {
        this.webUserName = webUserName;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setWindsAloftCorridorWidth(String windsAloftCorridorWidth) {
        this.windsAloftCorridorWidth = windsAloftCorridorWidth;
    }

    public void setPlainTextTimeZone(String plainTextTimeZone) {
        this.plainTextTimeZone = plainTextTimeZone;
    }

    /**
     * Create the parm string for a REST routeBriefing API call. Create string like
     * includeCodedMessages=true&routeCorridorWidth=25&briefingPreferences={"tailoring":["ENCODED_ONLY"]}
     * &type=DOMESTIC&outlookBriefing=false&flightRules=VFR&departure=3B3
     * &departureInstant=2020-08-08T23:59:00.0&destination=3B3&flightDuration=PT04H
     * &route=KAFN, KEEN&aircraftIdentifier=N68RM&webUserName=flightservice@soaringforecast.org
     * &speedKnots=50&versionRequested=99999999&notABriefing=false&briefingType=NGBV2
     * &briefingResultFormat=PDF&emailAddress=flightservice%40soaringforecast.org
     */
    public String getRestParmString() {
        StringBuffer sb = new StringBuffer();
        sb.append("notABriefing=").append(notABriefing);
        sb.append(AMPERSAND).append("includeCodedMessages=").append(includeCodedMessages);
        sb.append(AMPERSAND).append("type=").append(type);
        // Only send id if user wants record of briefing filed at
        // 1800wxbrief ((!notABriefing) == false)
        if (!notABriefing) {
            sb.append(AMPERSAND).append("aircraftIdentifier=").append(aircraftIdentifier);
        }
        sb.append(AMPERSAND).append("routeCorridorWidth=").append(routeCorridorWidth);
        sb.append(AMPERSAND).append("briefingPreferences=").append(getBriefingPreferences());
        sb.append(AMPERSAND).append("outlookBriefing=").append(((outlookBriefing != null) ? outlookBriefing : false));
        sb.append(AMPERSAND).append("flightRules=").append(flightRules);
        sb.append(AMPERSAND).append("departure=").append(departure);
        sb.append(AMPERSAND).append("departureInstant=").append(departureInstant);
        sb.append(AMPERSAND).append("destination=").append(destination);
        sb.append(AMPERSAND).append("route=").append(route);
        sb.append(AMPERSAND).append("flightDuration=").append(flightDuration);
        if (!notABriefing) {
            sb.append(AMPERSAND).append("webUserName=").append(webUserName);
        }
        sb.append(AMPERSAND).append("speedKnots=").append(speedKnots);
        sb.append(AMPERSAND).append("versionRequested=").append("99999999");
        sb.append(AMPERSAND).append("briefingType=").append(selectedBriefingType);
        if (selectedBriefingType != null && selectedBriefingType.equals(BriefingType.NGBV2)) {
            sb.append(AMPERSAND).append("briefingResultFormat=").append(briefingResultFormat);
        }
        if (selectedBriefingType != null & selectedBriefingType.equals(BriefingType.EMAIL)) {
            sb.append(AMPERSAND).append("emailAddress=").append(emailAddress);
        }
        String plainTextTimeZone = TimeZoneAbbrev.UTC.name();
        try {
            // make sure current timezone valid
            plainTextTimeZone= TimeZoneAbbrev.valueOf(getLocalTimeZoneAbbrev()).name();
        } catch (IllegalArgumentException ex) {
            //nope
        }
        sb.append(AMPERSAND).append("plainTextTimeZone=").append(plainTextTimeZone);
        return sb.toString();
    }

    /**
     * Formatted briefingPreferences string
     * {"items":["productCode","productCode",...,"productCode"],"plainText":true,"tailoring":["tailoringOption","tailoringOption",...,"tailoringOption"]}
     * Note "plainText" parm not included
     *
     * @return
     */
    private String getBriefingPreferences() {
        StringBuilder sb = new StringBuilder();
        sb.append('{')
                .append(getItemsList())
                .append(',')
                .append(getTailorOptions())
                .append('}');
        return sb.toString();
    }

    private synchronized String getItemsList() {
        StringBuilder sb = new StringBuilder();
        boolean atLeastOne = false;
        sb.append("\"items\":[");
        if (productCodeList.size() > 0) {
            for (int i = 0; i < productCodeList.size(); ++i) {
                if (selectedProductCodes[i]) {
                    atLeastOne = true;
                    sb.append("\"")
                            .append(productCodeList.get(i))
                            .append("\",");
                }
            }
            if (atLeastOne) {
                sb.deleteCharAt(sb.length() - 1);
            }
        }

        sb.append(']');
        return sb.toString();

    }

    private synchronized String getTailorOptions() {
        StringBuilder sb = new StringBuilder();
        boolean atLeastOne = false;
        sb.append("\"tailoring\":[");
        if (tailoringOptionList.size() > 0) {
            for (int i = 0; i < tailoringOptionList.size(); ++i) {
                if (selectedTrailoringOptions[i]) {
                    atLeastOne = true;
                    sb.append("\"")
                            .append(tailoringOptionList.get(i))
                            .append("\",");
                }
            }
            if (atLeastOne) {
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        sb.append(']');
        return sb.toString();
    }


    /**
     * Convert a local date/time to a zulu date time.
     * Note that localTime actually gets converted to Zulu time but need to add time zone offset
     * between localTime zone and Zulu (GMT) timezon

     * @param localTime
     * @return
     */
    public static String convertLocalTimeToZulu(String localTime){
        Timber.d("Local time: %1$s", localTime);
        long zuluTimeMillis = convertDateToMillis(localTime);
        // add in current time difference
        int  offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
        long realZuluTIme = zuluTimeMillis + ((offset > 0) ? offset : -1 * offset) ;
        String zuluTime  =  wxbriefTimeFormatter.format(realZuluTIme );
        Timber.d("Zulu time: %1$s", zuluTime);
        return zuluTime;
    }

    /**
     * Convert formatted date to milliseconds
     * The formatted date must be in the form of
     * "yyyy-MM-ddTHH:mm:ss.S"
     * e.g. 2020-08-15T23:00:00.0
      */

    public static long convertDateToMillis(String date) {
        try
        {
            Date mDate =  wxbriefTimeFormatter.parse(date);
            long timeInMilliseconds = mDate.getTime();
            System.out.println("Date in millis : " + timeInMilliseconds);
            return timeInMilliseconds;
        }
        catch (ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }


    public static String getLocalTimeZoneAbbrev(){
        String zoneAbbrev;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ZoneId zone = ZoneId.systemDefault();
            DateTimeFormatter zoneAbbreviationFormatter
                    = DateTimeFormatter.ofPattern("zzz", Locale.ENGLISH);
            zoneAbbrev = ZonedDateTime.now(zone).format(zoneAbbreviationFormatter);
            Timber.d("Current abbreviation for either standard or summer time: %1$s "
                    , zoneAbbrev);

        } else {
           zoneAbbrev =  TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
        }
        return  zoneAbbrev;
    }



}
