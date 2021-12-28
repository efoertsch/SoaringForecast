package org.soaringforecast.rasp.one800wxbrief.routebriefing;

import org.soaringforecast.rasp.utils.TimeUtils;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * A much simplified class to hold values needed to make the routeBriefing call to the 1800wxbrief
 * routeBriefing api
 * See https://www.1800wxbrief.com/Website/resources/doc/WebService.xml#op.idp140237337565664 for
 * valid values and definition
 */
public class RouteBriefingRequest {

    private static final String AMPERSAND = "&";

    private ArrayList<String> productCodes = new ArrayList<>();

    /**
     * User selected tailoring Options to generate the tailoringOptions list in briefingPreferences
     * <p>
     * eg.
     * ... ,"tailoring":["tailoringOption","tailoringOption",...,"tailoringOption"]
     * ... ,"tailoring":["EXCLUDE_GRAPHICS","EXCLUDE_HISTORICAL_METARS"]
     */
    private ArrayList<String> tailoringOptions = new ArrayList<>();



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
    private String selectedBriefingType;



    public enum TimeZoneAbbrev {
        AST, ADT, EST, EDT, CST, CDT, MST, MDT, PST, PDT, AKST, AKDT, HST, UTC
    }


    /**
     * REST calls require type - DOMESTIC (being deprecated) or ICAO -
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
     * Default to 10 hr (so basically all day) flight
     */
    private String flightDuration = "PT12H";

    /**
     * Default flight to 6 thousand feet
     */
    private String flightLevel = "060";

    /**
     * Route  (airports along task, comma separated
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
     * Used to provide key/value error msg when errors occur
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

    }

    public void setNotABriefing(Boolean notABriefing) {
        this.notABriefing = notABriefing;
    }


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

    public void setSelectedBriefingType(String selectedBriefingType) {
        this.selectedBriefingType = selectedBriefingType;
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
        if (selectedBriefingType != null && selectedBriefingType.equals("NGBV2")) {
            sb.append(AMPERSAND).append("briefingResultFormat=").append(briefingResultFormat);
        }
        if (selectedBriefingType != null & selectedBriefingType.equals("EMAIL")) {
            sb.append(AMPERSAND).append("emailAddress=").append(emailAddress);
        }
        sb.append(AMPERSAND).append("altitudeFL=").append(flightLevel);
        String plainTextTimeZone = TimeZoneAbbrev.UTC.name();
        try {
            // make sure current timezone valid
            plainTextTimeZone = TimeZoneAbbrev.valueOf(TimeUtils.getLocalTimeZoneAbbrev()).name();
        } catch (IllegalArgumentException ex) {
            //nope
        }
        sb.append(AMPERSAND).append("plainTextTimeZone=").append(plainTextTimeZone);
        Timber.d("Briefing Request Options: %1$s", sb.toString());
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
                .append(getProductCodesJson())
                .append(',')
                .append(getTailorOptionsJson())
                .append('}');
        return sb.toString();
    }

    /**
     * Items are the list of product codes to be requested
     *
     * @return
     */
    private synchronized String getProductCodesJson() {
        StringBuilder sb = new StringBuilder();
        boolean atLeastOne = false;
        sb.append("\"items\":[");
        if (productCodes.size() > 0) {
            for (int i = 0; i < productCodes.size(); ++i) {
                sb.append(i > 0 ? ",\"" : "\"")
                        .append(productCodes.get(i))
                        .append("\"");
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private synchronized String getTailorOptionsJson() {
        StringBuilder sb = new StringBuilder();
        boolean atLeastOne = false;
        sb.append("\"tailoring\":[");
        if (tailoringOptions.size() > 0) {
            for (int i = 0; i < tailoringOptions.size(); ++i) {
                sb.append(i > 0 ? ",\"" : "\"")
                        .append(tailoringOptions.get(i))
                        .append("\"");
            }
        }
        sb.append(']');

        return sb.toString();
    }

}
