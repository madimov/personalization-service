package io.interact.personalization.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Logger {

	public static void print(Object o) {

		System.out.println(o);
	}

	public static void printListOfHashMaps(List<HashMap<String, Object>> results) {
		// TODO rename argument

		try {
			for (int i = 0; i < results.size(); i++) {
				System.out.println(results.get(i));
			}
		} catch (Exception e) {
			System.out.println("exception!" + e);
		}
	}

	public static void printHashMap(HashMap<String, Object> hashMap) {

		Set<String> keys = hashMap.keySet();
		try {
			for (String key : keys) {
				String value = (String) hashMap.get(key).toString();
				System.out.println(key + ": " + value);
			}
		} catch (Exception e) {
			System.out.println("exception!" + e);
		}
	}

	public void printArrayListOfStringArrays(ArrayList<String[]> arrayList) {

		try {
			for (int i = 0; i < arrayList.size(); i++) {
				System.out.println(Arrays.deepToString(arrayList.get(i)));
			}
		} catch (Exception e) {
			System.out.println("exception!" + e);
		}
	}

	public static void printArrayListOfStrings(ArrayList<String> arrayList) {

		try {
			for (int i = 0; i < arrayList.size(); i++) {
				System.out.println(arrayList.get(i));
			}
		} catch (Exception e) {
			System.out.println("exception!" + e);
		}
	}

	public static void printArrayOfStrings(String[] stringArray) {

		try {
			for (int i = 0; i < stringArray.length; i++) {
				System.out.println(stringArray[i]);
			}
		} catch (Exception e) {
			System.out.println("exception!" + e);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
}
