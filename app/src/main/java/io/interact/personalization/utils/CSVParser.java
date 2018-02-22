package io.interact.personalization.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.opencsv.CSVReader;

public class CSVParser {

	public ArrayList<String[]> getArrayListFromCSV(String filePath) {

		CSVReader reader = null;
		ArrayList<String[]> data = new ArrayList<String[]>();

		try {
			reader = new CSVReader(new FileReader(filePath));
			String[] nextLine;
			// int lineNumber = 0;

			while ((nextLine = reader.readNext()) != null) {
				// lineNumber++;
				// System.out.println("Line # " + lineNumber);
				// nextLine[] is an array of values from the line
				// System.out.println(Arrays.toString(nextLine));
				data.add(nextLine);
			}

			// for (int i = 0; i < data.size(); i++) {
			// System.out.println(Arrays.deepToString(data.get(i)));
			// }

		} catch (FileNotFoundException e) {
			System.out.println("file not found exception!");
		} catch (IOException e) {
			System.out.println("IOE exception!");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
}
