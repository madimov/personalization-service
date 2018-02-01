package io.interact.personalization.utils;

import java.util.ArrayList;
import java.util.Arrays;

public class Logger {

	public void printArrayListOfStringArrays(ArrayList<String[]> arrayList) {

		try {
			for (int i = 0; i < arrayList.size(); i++) {
				System.out.println(Arrays.deepToString(arrayList.get(i)));
			}
		} catch (Exception e) {
			System.out.println("exception!" + e);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
}
