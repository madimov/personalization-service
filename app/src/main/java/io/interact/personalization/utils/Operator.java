package io.interact.personalization.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Operator {

	public static boolean equalsListOfHashMaps(List<HashMap<String, Object>> list1,
			List<HashMap<String, Object>> list2) {
		List<HashMap<String, Object>> largerList = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> smallerList = new ArrayList<HashMap<String, Object>>();
		int size1 = list1.size();
		int size2 = list2.size();
		if (size1 >= size2) {
			largerList = list1;
			smallerList = list2;
		} else {
			largerList = list2;
			smallerList = list1;
		}

		try {
			Set<String> keys;
			for (int i = 0; i < largerList.size(); i++) {

				int numKeys1 = largerList.get(i).keySet().size();
				int numKeys2 = smallerList.get(i).keySet().size();
				if (numKeys1 >= numKeys2) {
					keys = largerList.get(i).keySet();
				} else {
					keys = smallerList.get(i).keySet();
				}
				String value1 = "";
				String value2 = "";
				for (String key : keys) {
					value1 = (String) largerList.get(i).get(key).toString();
					value2 = (String) smallerList.get(i).get(key).toString();
					if (!value1.equals(value2)) {
						return false;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public static List<String> convertListOf1DHashMapsToListOfStrings(List<HashMap<String, Object>> listOfHashMaps,
			String key) {

		List<String> values = new ArrayList<String>();
		for (int i = 0; i < listOfHashMaps.size(); i++) {
			String value = (String) listOfHashMaps.get(i).get(key).toString();
			values.add(value);
		}
		return values;
	}

	public static List<String> convertListOfOneHashMapToListOfStrings(List<HashMap<String, Object>> listOfHashMaps) {

		List<String> values = new ArrayList<String>();
		for (int i = 0; i < listOfHashMaps.size(); i++) {
			Set<String> keys = listOfHashMaps.get(i).keySet();
			for (String key : keys) {
				// Logger.print(key);
				String value = (String) listOfHashMaps.get(i).get(key).toString();
				values.add(value);
			}
		}
		// Logger.printArrayListOfStrings((ArrayList<String>) values);
		return values;
	}

	public static List<HashMap<String, Object>> convertResultSetToList(ResultSet rs) throws SQLException {
		// taken from:
		// https://stackoverflow.com/questions/7507121/efficient-way-to-handle-resultset-in-java
		ResultSetMetaData md = rs.getMetaData();
		int columns = md.getColumnCount();
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

		while (rs.next()) {
			HashMap<String, Object> row = new HashMap<String, Object>(columns);
			for (int i = 1; i <= columns; ++i) {
				row.put(md.getColumnName(i), rs.getObject(i));
			}
			list.add(row);
		}

		return list;
	}

	public static String getKeyOfMaxValueFromStringFloatHashMap(HashMap<String, Object> hashMap) {
		String keyOfMaxValue = "";
		Set<String> keys = hashMap.keySet();
		float maxValue = 0;
		try {
			for (String key : keys) {
				float value = (float) hashMap.get(key);
				if (value > maxValue) {
					maxValue = value;
					keyOfMaxValue = key;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return keyOfMaxValue;
	}

	public static String getKeyOfMinValueFromStringFloatHashMap(HashMap<String, Object> hashMap) {
		String keyOfMinValue = "";
		Set<String> keys = hashMap.keySet();
		float minValue = 1;
		try {
			for (String key : keys) {
				float value = (float) hashMap.get(key);
				if (value < minValue) {
					minValue = value;
					keyOfMinValue = key;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return keyOfMinValue;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
