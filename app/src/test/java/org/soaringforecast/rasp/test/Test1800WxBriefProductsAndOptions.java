package org.soaringforecast.rasp.test;

import org.junit.Test;
import org.soaringforecast.rasp.one800wxbrief.options.BriefingOption;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Test1800WxBriefProductsAndOptions {

    @Test
    public void testReadOf1800WXBrief_Products_CSV_file() throws IOException {
        BufferedReader reader = null;
        String line;
        int linesRead = 0;
        BriefingOption briefingOption;
        ArrayList<BriefingOption> briefingOptions = new ArrayList<>();
        try {
            File file = new File(getClass().getResource("/wxbrief_product_codes.csv").getPath());
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            line = reader.readLine();
            while (line != null && !line.isEmpty()) {
                if (linesRead > 0) {
                    briefingOption = BriefingOption.createBriefingOptionFromCSVDetail(line);
                    if (briefingOption != null) {
                        briefingOptions.add(briefingOption);
                    }

                }
                linesRead++;
                line = reader.readLine();
            }
            System.out.println(String.format("Lines read: %1$d   Number of product codes  %2$d", linesRead, briefingOptions.size()));

        }  finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
    }
}
