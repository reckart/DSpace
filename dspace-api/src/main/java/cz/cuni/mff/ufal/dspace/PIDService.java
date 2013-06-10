/*
 * PIDService.java
 *
 * Version: 0.1
 *
 */

/**
 * Interface to the <a href="http://www.pidconsortium.eu" target=_new>European Persistent Identifier Consortium</a> service.
 *
 * <p>This class provides a simple interface to the UFAL account on the PID service hosted at
 *    <a href="http://handle.gwdg.de:8080/pidservice/">http://handle.gwdg.de:8080/pidservice/</a>.
 *    It provides the following methods:
 *      createPID(String URL)                // returns String PID
 *      modifyPID(String PID, String newURL) // returns URL
 *      resolvePID(String PID)               // returns URL
 * </p>
 *
 * @author Petr Pajas
 * @version 0.1
 */

package cz.cuni.mff.ufal.dspace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dspace.core.ConfigurationManager;
import org.opensaml.xml.util.Base64;

public class PIDService {
	private static String PIDServiceURL;
	private static String PIDServiceUSER;
	private static String PIDServicePASS;
	private static String PIDServiceVERSION;

	private static boolean initialized;

	static class PIDServiceAuthenticator extends Authenticator {
		@Override
        public PasswordAuthentication getPasswordAuthentication() {
			return (new PasswordAuthentication(PIDServiceUSER,
					PIDServicePASS.toCharArray()));
		}
	}

	private static void initialize() throws IOException {
		initialized = true;
		PIDServiceURL = ConfigurationManager.getProperty("pidservice.url");
		PIDServiceUSER = ConfigurationManager.getProperty("pidservice.user");
		PIDServicePASS = ConfigurationManager.getProperty("pidservice.pass");
		PIDServiceVERSION = ConfigurationManager.getProperty("pidservice.ver");

		if (PIDServiceURL == null || PIDServiceURL.length() == 0) {
            throw new IOException("PIDService URL not configured.");
        }
	}

	private static enum HTTPMethod {
		GET, POST, PUT
	}

	static PIDServiceAuthenticator authenticator = new PIDServiceAuthenticator();

	/**
	 * EPIC PID API version 2
	 * Docu: http://epic.gwdg.de/wiki/index.php?title=EPIC:API:v2:contribution#VIEW
	 * Executes View, Create or Update method
	 * @param method
	 * @param pid
	 * @param url
	 * @param auth
	 * @return returns request result
	 * @throws IOException
	 */
    private static String sendPIDCommandV2(HTTPMethod method, String pid, String url, boolean auth)
        throws IOException
    {
        {
            HttpClient httpClient = new DefaultHttpClient();
            try {
                if (method == HTTPMethod.GET) {// GET
                    //NOT TESTET
                    String getUrl = PIDServiceURL+pid;
                    HttpGet request = new HttpGet(getUrl);
                    request.addHeader(
                            "Authorization",
                            "Basic "
                                    + Base64.encodeBytes((PIDServiceUSER + ":" + PIDServicePASS)
                                            .getBytes()));
                    request.addHeader("Accept", "application/json");

                    HttpResponse response = httpClient.execute(request);

                    //get JSON from response
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    StringBuilder builder = new StringBuilder();
                    for (String line = null; (line = reader.readLine()) != null;) {
                        builder.append(line).append("\n");
                    }

                    return builder.toString();
                }
                else if (method == HTTPMethod.POST) {// CREATE

                    HttpPost request = new HttpPost(PIDServiceURL);
                    request.addHeader(
                            "Authorization",
                            "Basic "
                                    + Base64.encodeBytes((PIDServiceUSER + ":" + PIDServicePASS)
                                            .getBytes()));
                    String json = "[{\"type\":\"URL\",\"parsed_data\":\"" + url + "\"}]";

                    StringEntity params = new StringEntity(json);
                    request.addHeader("Content-Type", "application/json");
                    request.addHeader("Accept", "application/json");

                    request.setEntity(params);
                    HttpResponse response = httpClient.execute(request);
                    if (response.getStatusLine().getStatusCode() != 201) {
                        return null;
                    }

                    return getIdFromResponse(response);

                }

                else if (method == HTTPMethod.PUT) {// MODIFY
                    String putUrl = PIDServiceURL+pid;

                    HttpPut request = new HttpPut(putUrl);
                    request.addHeader(
                            "Authorization",
                            "Basic "
                                    + Base64.encodeBytes((PIDServiceUSER + ":" + PIDServicePASS)
                                            .getBytes()));
                    String json = "[{\"type\":\"URL\",\"parsed_data\":\"" + url + "\"}]";

                    StringEntity params = new StringEntity(json);
                    request.addHeader("Content-Type", "application/json");
                    request.addHeader("Accept", "application/json");

                    request.setEntity(params);
                    HttpResponse response = httpClient.execute(request);

                    if (response.getStatusLine().getStatusCode() != 204) {
                        return null;
                    }

                    return url;
                }
             }
            catch (Exception ex) {
                ex.printStackTrace();
                // handle exception here
            }
            finally {
                httpClient.getConnectionManager().shutdown();
            }
        }
        return null;
    }

    /**
     * Parses file id from HTTP response
     */
    private static String getIdFromResponse(HttpResponse aRes)
    {
        Header value = aRes.getFirstHeader("Location");
        String valueString = value.getValue();
        String id = valueString.substring(valueString.lastIndexOf("/")+1);

        return id;
     }


    /**
     * EPIC PID API version 1, made by ufal
     * @param method
     * @param command
     * @param data
     * @param match_regex
     * @param auth
     * @return
     * @throws IOException
     */
	private static String sendPidCommandV1(HTTPMethod method, String command, String data,
            String match_regex, boolean auth) throws IOException
    {
        URL url;

        if (auth) {
            Authenticator.setDefault(authenticator);
        }
        InputStream input;
        if (method == HTTPMethod.GET) {
            url = new URL(PIDServiceURL + command + '?' + data);
            System.out.println(url.toString());
            input = url.openConnection().getInputStream();
        } else {
            url = new URL(PIDServiceURL + command);
            System.out.println(url.toString());

            // <OLD>
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            OutputStream out = conn.getOutputStream();
            OutputStreamWriter wr = new OutputStreamWriter(out);
            // </OLD>

            // <NEW>
            // Security.addProvider(new
            // com.sun.net.ssl.internal.ssl.Provider());
            // SSLSocketFactory factory =
            // (SSLSocketFactory)SSLSocketFactory.getDefault() ;
            // SSLSocket socket = (SSLSocket)factory.createSocket(url.getHost(),
            // 443);
            // OutputStreamWriter wr = new OutputStreamWriter (
            // socket.getOutputStream() ) ;
            // </NEW>

            wr.write(data);
            wr.flush();
            // <OLD>
            input = conn.getInputStream();
            // </OLD>

            // <NEW>
            // input = socket.getInputStream();
            // </NEW>
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(input));
        String response = "";
        String line;
        do {
            line = in.readLine();
            // log.
            if (line == null) {
                break;
            }
            else {
                response += line + "\n";
            }
        } while (response.length() < 5 * 1024); // reasonable threashold of 5KB

        Matcher m = Pattern.compile(match_regex).matcher(response);
        if (m.find()) {
            return m.group(1);
        }
        throw new IOException("Invalid PID service response:\n" + response);

    }

    public static String resolvePID(String PID)
        throws IOException
    {
        if (!initialized) {
            initialize();
        }

        if ("2".equals(PIDServiceVERSION)) {
            return sendPIDCommandV2(HTTPMethod.GET, PID, null, true);
        }
        else {

            return sendPidCommandV1(HTTPMethod.GET, "read/view", "showmenu=no" + "&pid="
                    + URLEncoder.encode(PID, "UTF-8"), "<tr><td>Location</td><td>([^<]+)</td>",
                    false);

        }
    }

    public static String modifyPID(String PID, String URL)
        throws IOException
    {
        if (!initialized) {
            initialize();
        }

        if ("2".equals(PIDServiceVERSION)) {
            return sendPIDCommandV2(HTTPMethod.PUT, PID, URL, true);
        }
        else {

            return sendPidCommandV1(
                    HTTPMethod.POST,
                    "write/modify",
                    "pid=" + URLEncoder.encode(PID, "UTF-8") + "&url="
                            + URLEncoder.encode(URL, "UTF-8"),
                    "<tr><td>Location</td><td>([^<]+)</td>", true);
        }
    }

    public static String createPID(String URL)
        throws IOException
    {
        if (!initialized) {
            initialize();
        }

        if ("2".equals(PIDServiceVERSION)) {
            return sendPIDCommandV2(HTTPMethod.POST, null, URL, true);
        }
        else {

            return sendPidCommandV1(HTTPMethod.POST, "write/create",
                    "url=" + URLEncoder.encode(URL, "UTF-8"), "<h2><a href=\"[^\"]*\">([^<]+)</a>",
                    true);
        }
    }
}