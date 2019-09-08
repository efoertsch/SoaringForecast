package retrofit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Returns the same METAR/TAF based on what is in resources directory
 */
public class MockInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) {
        final URI uri = chain.request().url().uri();
        final String query = uri.getQuery();
        String responseString = getMockXml(query);
        Response response = new Response.Builder()
                .code(200)
                .message(responseString)
                .request(chain.request())
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/xml"), responseString.getBytes()))
                .addHeader("content-type", "application/xml")
                .build();

        return response;
    }

    private String getMockXml(String api) {
        String reply = "";
        String mockXML = "";
        StringBuilder sb = new StringBuilder();
        StringWriter writer = new StringWriter();
        InputStream in = null;

        if (api.contains("=metars")){
                mockXML = "metar.xml";
        } else if (api.contains("=tafs")){
                mockXML = "taf.xml";
        }

        if (!mockXML.isEmpty()) {
            URL urL = this.getClass().getResource("/"  + mockXML);
            String strLine;
            try {
                in = urL.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                while ((strLine = reader.readLine()) != null) {
                    sb.append(strLine);
                }
            } catch (IOException ignore) {
                //ignoreÎ
            }
            finally {
                if (in != null) {
                    try {in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("Mocking:\n" + sb.toString());
        return sb.toString();
    }
}
