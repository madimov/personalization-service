package io.interact.personalization.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONObject;

import io.interact.personalization.db.PostgresController;

public class Requester {

	private static String internalApiUsername = "miko@interact.io";
	private static String internalApiPassword = "100%JuicePlus";

	public static void sendGetRequest() {
		// taken from:
		// https://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests

		String url = "http://example.com";
		String charset = java.nio.charset.StandardCharsets.UTF_8.name();
		String param1 = "value1";
		String param2 = "value2";
		String query = null;
		URLConnection connection = null;
		InputStream response = null;

		try {
			query = String.format("param1=%s&param2=%s", URLEncoder.encode(param1, charset),
					URLEncoder.encode(param2, charset));
			connection = new URL(url + "?" + query).openConnection();
			connection.setRequestProperty("Accept-Charset", charset);
			response = connection.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try (Scanner scanner = new Scanner(response)) {
			String responseBody = scanner.useDelimiter("\\A").next();
			Logger.print(responseBody);
		}
	}

	public static String sendPostRequest(HttpURLConnection connection) throws IOException {
		// largely taken from:
		// https://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests

		InputStream response = null;
		String responseBody = null;

		try {
			connection.setDoOutput(true);
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("POST");

			JSONObject jsonBody = new JSONObject();
			jsonBody.put("username", internalApiUsername);
			jsonBody.put("password", internalApiPassword);
			Logger.print("jsonBody = " + jsonBody.toString());
			// connection.setRequestProperty("body", jsonBody.toString());
			connection.setRequestProperty("Content-Type", "application/json");

			Map<String, List<String>> requestProperties = connection.getRequestProperties();
			Logger.print("requestProperties = " + requestProperties.toString());

			Map<String, List<String>> headerFields = connection.getHeaderFields();
			Logger.print("headerFields = " + headerFields.toString());

			OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
			wr.write("body = " + jsonBody.toString());
			wr.close();

			// response = connection.getInputStream();
			// Logger.print("response = " + response);

			BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
			int i = 0;
			while ((i = in.read()) != -1) {
				System.out.write(i);
			}
			in.close();

			Scanner scanner = new Scanner(response);
			responseBody = scanner.useDelimiter("\\A").next();
			scanner.close();
			Logger.print("responseBody = " + responseBody);

			int status = connection.getResponseCode();
			Logger.print("status = " + status);

		} catch (Exception e) {
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				Logger.print("~~~" + line);
			}
			// e.printStackTrace();
		}
		return responseBody;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// sendGetRequest();
		try {
			PostgresController.connectDatabase();
			System.out.println("connected db");
			PostgresController.truncateTable("cards");
			System.out.println("truncated cards table");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
