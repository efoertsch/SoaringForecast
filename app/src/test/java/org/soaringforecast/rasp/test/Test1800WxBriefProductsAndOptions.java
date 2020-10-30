package org.soaringforecast.rasp.test;

import org.junit.Test;
import org.soaringforecast.rasp.one800wxbrief.options.ProductCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import timber.log.Timber;

public class Test1800WxBriefProductsAndOptions {

    @Test
    public void testReadOf1800WXBrief_Products_CSV_file() throws IOException {
        BufferedReader reader = null;
        String line;
        int linesRead = 0;
        ProductCode productCode;
        ArrayList<ProductCode> productCodes = new ArrayList<>();
        try {
            File file = new File("wxbrief_product_codes.csv");
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            line = reader.readLine();
            while (line != null && !line.isEmpty()) {
                linesRead++;
                if (linesRead > 1) {
                    productCode = ProductCode.createProductCodeFromCSVDetail(line);
                    if (productCode != null) {
                        productCodes.add(productCode);
                    }
                    linesRead++;
                }
                line = reader.readLine();
            }
            Timber.d("Lines read: %1$d   Number of product codes  %2$d", linesRead, productCodes.size());

        }  finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
    }
}
