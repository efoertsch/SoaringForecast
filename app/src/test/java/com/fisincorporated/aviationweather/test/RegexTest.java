package com.fisincorporated.aviationweather.test;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;

public class RegexTest {
    String regex = "[^A-za-z0-9_-]+";
    String input = "NewEngland/2018-03-31/gfs/wstar_bsratio.curr.1500lst.d2.body.png?11:15:44";
    String targetOutput = "NewEngland-2018-03-31-gfs-wstar_bsratio-curr-1500lst-d2-body-png-11-15-44";
    String regexReplaceChar = "-";

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
