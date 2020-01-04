package org.soaringforecast.rasp.test;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class RegexTest {
    String regex = "[^A-za-z0-9_-]+";
    String input = "NewEngland/2018-03-31/gfs/wstar_bsratio.curr.1500lst.d2.body.png?11:15:44";
    String targetOutput = "NewEngland-2018-03-31-gfs-wstar_bsratio-curr-1500lst-d2-body-png-11-15-44";
    String regexReplaceChar = "-";

    @Test
    public void LatitudeStringIsCorrect(){
        //Latitude
        String regex = "(9000\\.000|[0-8][0-9][0-5][0-9]\\.[0-9]{3})[NS]";
        Pattern p = Pattern.compile(regex);
        testMatcher (p, "9000.000N", true, regex );
        testMatcher (p, "0959.999N", true, regex );
        testMatcher (p, "0000.999S", true, regex );
        testMatcher (p,"9000.000S", true, regex );

        //fail
        testMatcher (p,"9990.000E", false, regex );
        testMatcher (p,"8960.000E", false, regex );
        testMatcher (p,"8960.00EE", false, regex );
        testMatcher(p,"8960.0E",false, regex);

        //Longitude
        regex = "(18000\\.000|(([0-1][0-7])|([0][0-9]))[0-9][0-5][0-9]\\.[0-9]{3})[EW]";
        p = Pattern.compile(regex);
        testMatcher (p, "18000.000E", true, regex );
        testMatcher (p, "18000.000W", true, regex );
        testMatcher (p, "17959.999E", true, regex );
        testMatcher (p, "09000.000E", true, regex );

        testMatcher (p, "18999.000E", false, regex );
        testMatcher (p, "09999.000E", false, regex );




    }

    public void testMatcher(Pattern p, String matchValue, boolean match, String regex){
        Matcher m = p.matcher(matchValue);
        if (match) {
            assertTrue("Should match:" + matchValue + " using: " + regex, m.matches());
        }
        else {
            assertFalse("Shouldn't match:" + matchValue + " using: " + regex, m.matches());
        }
    }

    @Test
    public void convertUrlToCacheStringIsCorrect() {
        String output;
        Pattern p = Pattern.compile(regex);
        // get a matcher object
        Matcher m = p.matcher(input);
        output = m.replaceAll(regexReplaceChar);
        System.out.println(output);
        assertEquals(targetOutput, output );
    }

    @Test
    public void getLast30CharsTest() {
        String output;
        Pattern p = Pattern.compile(regex);
        // get a matcher object
        Matcher m = p.matcher(input);
        output = m.replaceAll(regexReplaceChar);
        String last30 = m.replaceAll(regexReplaceChar).substring(output.length() - 30);
        System.out.println(last30);
        assertEquals(30, last30.length() );
        assertEquals(last30,(output.substring(output.length() - 30)));

    }

}
