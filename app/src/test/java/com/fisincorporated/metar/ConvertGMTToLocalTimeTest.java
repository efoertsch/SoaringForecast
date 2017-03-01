package com.fisincorporated.metar;

import com.fisincorporated.aviationweather.utils.ConversionUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConvertGMTToLocalTimeTest {

        @Test
        public void conversionFromGmtToLocalTimeIsCorrect() throws Exception {
            //Hmmm - How to test when we go between EST and DST?
            assertEquals("Feb 27, 3:54 PM", ConversionUtils.convertGMTToLocalTime("2017-02-27T20:54:00Z"));
        }

    }
