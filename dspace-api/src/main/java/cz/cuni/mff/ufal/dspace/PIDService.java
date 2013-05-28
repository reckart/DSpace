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

public class PIDService {
	private static String PIDServiceURL;
	private static String PIDServiceUSER;
	private static String PIDServicePASS;
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
		//PIDServiceURL = ConfigurationManager.getProperty("pidservice.url");
		//PIDServiceUSER = ConfigurationManager.getProperty("pidservice.user");
		//PIDServicePASS = ConfigurationManager.getProperty("pidservice.pass");

		PIDServiceURL = "http://demo.pidconsortium.eu:8444/handles/11148/";
		PIDServiceUSER = "1014-01";
		PIDServicePASS = "Feengae6";

		if (PIDServiceURL == null || PIDServiceURL.length() == 0) {
            throw new IOException("PIDService URL not configured.");
        }
	}

	private static enum HTTPMethod {
		GET, POST
	}

	static PIDServiceAuthenticator authenticator = new PIDServiceAuthenticator();

	private static String sendPIDCommand(HTTPMethod method, String command,
			String data, String match_regex, boolean auth) throws IOException {
		URL url;
		if (!initialized) {
            initialize();
        }
		if (auth) {
			Authenticator.setDefault(authenticator);
		}
		InputStream input;
		if (method == HTTPMethod.GET) {
			url = new URL(PIDServiceURL + command + '?' + data);
			input = url.openConnection().getInputStream();
		} else {
			url = new URL(PIDServiceURL + command);
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

	public static String resolvePID(String PID) throws IOException {
		return sendPIDCommand(HTTPMethod.GET, "read/view", "showmenu=no"
				+ "&pid=" + URLEncoder.encode(PID, "UTF-8"),
				"<tr><td>Location</td><td>([^<]+)</td>", false);
	}

	public static String modifyPID(String PID, String URL) throws IOException {
		return sendPIDCommand(
				HTTPMethod.POST,
				"write/modify",
				"pid=" + URLEncoder.encode(PID, "UTF-8") + "&url="
						+ URLEncoder.encode(URL, "UTF-8"),
				"<tr><td>Location</td><td>([^<]+)</td>", true);
	}

	public static String createPID(String URL) throws IOException {
		return sendPIDCommand(HTTPMethod.POST, "write/create", "url="
				+ URLEncoder.encode(URL, "UTF-8"),
				"<h2><a href=\"[^\"]*\">([^<]+)</a>", true);
	}
}