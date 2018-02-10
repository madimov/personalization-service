package io.interact.personalization.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Scanner;

import org.json.JSONObject;

import io.interact.personalization.services.postgres.PostgresController;

public class Requester {

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

	public static String sendPostRequest(String url) {
		// largely taken from:
		// https://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests

		String query = null;
		HttpURLConnection httpConnection = null;
		InputStream response = null;
		String responseBody = null;

		try {
			httpConnection = (HttpURLConnection) new URL(url).openConnection();
			httpConnection.setRequestMethod("POST");

			JSONObject jsonBody = new JSONObject();
			jsonBody.put("defaultOperator", "AND");
			jsonBody.put("filters", new String[] {});
			jsonBody.put("query", "");
			System.out.println(jsonBody);

			httpConnection.setRequestProperty("Content-Type", "application/json");
			httpConnection.setRequestProperty("authToken", "kss_0vYbm3ldqVk1AqVaDb2t5X");
			// TODO move tokens and domains to config
			httpConnection.setRequestProperty("body", jsonBody.toString());

			response = httpConnection.getInputStream();

			Scanner scanner = new Scanner(response);
			responseBody = scanner.useDelimiter("\\A").next();
			Logger.print(responseBody);

			int status = httpConnection.getResponseCode();
			Logger.print(status);

		} catch (Exception e) {
			e.printStackTrace();
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
