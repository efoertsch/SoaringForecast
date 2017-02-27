package com.fisincorporated.metar;

import com.fisincorporated.utils.TimeUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConvertGMTToLocalTimeTest {

        @Test
        public void conversionFromGmtToLocalTimeIsCorrect() throws Exception {
            //Hmmm - How to test when we go between EST and DST?
            assertEquals("Feb 27, 3:54 PM", TimeUtils.convertGMTToLocalTime("2017-02-27T20:54:00Z"));
        }

    }
