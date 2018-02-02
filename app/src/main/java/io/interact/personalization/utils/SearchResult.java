package io.interact.personalization.utils;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class SearchResult {

	private int value1 = 1;
	private String value2 = "abc";
	private transient int value3 = 3;

	SearchResult() {
		// no-args constructor
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		Collection collection = new ArrayList();
		collection.add("hello");
		collection.add(5);
		String json = gson.toJson(collection);
		System.out.println("Using Gson.toJson() on a raw collection: " + json);
		JsonParser parser = new JsonParser();
		JsonArray array = parser.parse(json).getAsJsonArray();
		String message = gson.fromJson(array.get(0), String.class);
		int number = gson.fromJson(array.get(1), int.class);
		System.out.printf("Using Gson.fromJson() to get: %s, %d", message, number);
	}

}
