package fr.insalyon.waso.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import fr.insalyon.waso.util.exception.ServiceIOException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author WASO Team
 */
public class JsonHttpClient {

    public static class Parameter extends BasicNameValuePair {

        public Parameter(String name, String value) {
            super(name, value);
        }
    }

    protected CloseableHttpClient httpclient;

    public JsonHttpClient() {
        httpclient = HttpClients.createDefault();
    }

    public void close() throws IOException {
        httpclient.close();
    }

    public JsonObject post(String url, NameValuePair... parameters) throws IOException {

        JsonElement responseElement = null;
        JsonObject responseContainer = null;
        Integer responseStatus = null;

        try {

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(Arrays.asList(parameters), JsonServletHelper.ENCODING_UTF8));
            CloseableHttpResponse response = httpclient.execute(httpPost);
            try {

                responseStatus = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(entity.getContent(), JsonServletHelper.ENCODING_UTF8));
                    try {

                        JsonParser parser = new JsonParser();
                        responseElement = parser.parse(jsonReader);

                    } finally {
                        jsonReader.close();
                    }
                }

            } finally {
                response.close();
            }

            if (responseStatus != null && responseStatus == 200 && responseElement != null) {
                responseContainer = responseElement.getAsJsonObject();
            }

        } catch (HttpHostConnectException ex) {
            throw new ServiceIOException("Service Request FAILED: Could NOT CONNECT to remote Server ~~> check target URL ???" + "\n******** Target URL =>  " + url + "  <= ********"+"\n");
        } catch (IllegalStateException ex) {
            throw new ServiceIOException("Service Request FAILED: Wrong HTTP Response FORMAT - not a JSON Object ~~> check target URL output ???" + "\n******** Target URL =>  " + url + "  <= ********" + "\n**** Parameters:\n" + debugParameters(" * ", parameters));
        }

        if (responseContainer == null) {
            String statusLine = "???";
            if (responseStatus != null) {
                statusLine = responseStatus.toString();
                if (responseStatus == 400) {
                    statusLine += " - BAD REQUEST ~~> check request parameters ???";
                }
                if (responseStatus == 404) {
                    statusLine += " - NOT FOUND ~~> check target URL ???";
                }
                if (responseStatus == 500) {
                    statusLine += " - INTERNAL SERVER ERROR ~~> check target Server Log ???";
                }
            }
            throw new ServiceIOException("Service Request FAILED with HTTP Error " + statusLine + "\n******** Target URL =>  " + url + "  <= ********" + "\n**** Parameters:\n" + debugParameters(" * ", parameters));
        }

        return responseContainer;
    }

    public static boolean checkJsonObject(JsonElement callResult) {

        return (callResult != null && callResult.isJsonObject());
    }

    public static String debugParameters(String alinea, NameValuePair... parameters) {

        StringBuilder debug = new StringBuilder();

        for (NameValuePair parameter : parameters) {
            debug.append(alinea).append(parameter.getName()).append(" = ").append(parameter.getValue()).append("\n");
        }

        return debug.toString();
    }

}
