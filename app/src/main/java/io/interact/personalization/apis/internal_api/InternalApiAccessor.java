package io.interact.personalization.apis.internal_api;

import java.net.HttpURLConnection;
import java.net.URL;

import io.interact.personalization.utils.Logger;
import io.interact.personalization.utils.Requester;

public class InternalApiAccessor {

	private static String internalApiUsername = "miko@interact.io";
	private static String internalApiPassword = "100%JuicePlus";

	public static String login() {
		String authToken = null;

		try {
			String url = "https://internal-api-staging-lb.interact.io/v2/login";
			HttpURLConnection httpConnection = (HttpURLConnection) new URL(url).openConnection();
			// JSONObject jsonBody = new JSONObject();
			// jsonBody.put("username", internalApiUsername);
			// jsonBody.put("password", internalApiPassword);
			// Logger.print("jsonBody = " + jsonBody.toString());
			// httpConnection.setRequestProperty("body", jsonBody.toString());
			// httpConnection.setRequestProperty("Content-Type", "application/json");

			authToken = Requester.sendPostRequest(httpConnection);
			Logger.print(authToken);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return authToken;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		login();
	}

}
