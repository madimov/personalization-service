package io.interact.personalization.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONObject;

import com.jayway.jsonpath.JsonPath;

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
			System.out.println(responseBody);
		}
	}

	public static void sendPostRequest() {
		// taken from:
		// https://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests

		String url = "http://example.com";
		String charset = java.nio.charset.StandardCharsets.UTF_8.name();
		String param1 = "value1";
		String param2 = "value2";
		String query = null;
		HttpURLConnection httpConnection = null;
		InputStream response = null;

		try {
			query = String.format("param1=%s&param2=%s", param1, param2);
			httpConnection = (HttpURLConnection) new URL(url).openConnection();
			httpConnection.setRequestMethod("POST");
			httpConnection.setDoOutput(true); // Triggers POST.
			httpConnection.setRequestProperty("Accept-Charset", charset);
			httpConnection.setRequestProperty("Content-Type", "application/json");

			OutputStream output = httpConnection.getOutputStream();
			output.write(query.getBytes(charset));

			response = httpConnection.getInputStream();

			Scanner scanner = new Scanner(response);
			String responseBody = scanner.useDelimiter("\\A").next();
			System.out.println(responseBody);

			int status = httpConnection.getResponseCode();
			System.out.println(status);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendPostRequestGetSearchResults() {
		// largely taken from:
		// https://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests

		String url = "https://contentmanager-lb.interact.io/content-cards/search?offset=0&limit=800";
		// TODO figure out why the limit turns out as 100 in the request
		String query = null;
		HttpURLConnection httpConnection = null;
		InputStream response = null;

		try {
			httpConnection = (HttpURLConnection) new URL(url).openConnection();
			httpConnection.setRequestMethod("POST");

			JSONObject jsonBody = new JSONObject();
			jsonBody.put("defaultOperator", "AND");
			jsonBody.put("filters", new String[] {});
			jsonBody.put("query", "");
			System.out.print(jsonBody);

			httpConnection.setRequestProperty("Content-Type", "application/json");
			httpConnection.setRequestProperty("authToken", "kss_0vYUvOQmiRw7TMk9DBrVmT");
			httpConnection.setRequestProperty("body", jsonBody.toString());

			response = httpConnection.getInputStream();

			Scanner scanner = new Scanner(response);
			String responseBody = scanner.useDelimiter("\\A").next();
			System.out.println(responseBody);

			int status = httpConnection.getResponseCode();
			System.out.println(status);

			ArrayList<String> ids = new ArrayList<String>();
			String id;
			for (int i = 0; i < 100; i++) {
				id = JsonPath.parse(responseBody).read("$.data.[" + i + "].id");
				ids.add(id);
			}
			Logger.printArrayListOfStrings(ids);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// sendGetRequest();
		sendPostRequestGetSearchResults();
	}

}
