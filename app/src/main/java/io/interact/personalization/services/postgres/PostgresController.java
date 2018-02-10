package io.interact.personalization.services.postgres;

import static java.lang.Math.toIntExact;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import io.interact.personalization.utils.Logger;

public class PostgresController {

	public static Connection conn = null;

	public static void connectDatabase() throws SQLException {
		// TODO Auto-generated method stub
		String url = "jdbc:postgresql://localhost/personalization-service";
		// if ssl issues, set url to:
		// "sslfactory=org.postgresql.ssl.NonValidatingFactory"
		Properties props = new Properties();
		props.setProperty("user", "mikus"); // TODO set up db with another user
		props.setProperty("password", "S!l@ta");
		props.setProperty("ssl", "true");
		conn = DriverManager.getConnection(url, props);
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

	public static void selectTable() throws SQLException {
		// TODO Auto-generated method stub
		java.sql.Statement st = null;
		ResultSet rs = null;
		try {
			st = conn.createStatement();
			rs = st.executeQuery("SELECT * FROM cards");
			List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
			results = convertResultSetToList(rs);
			Logger.printListOfHashMaps(results);
		} catch (Exception e) {
			e.printStackTrace();
		}
		rs.close();
		st.close();
	}

	public static void createTable(String tableName, List<String> columnNames) throws SQLException {
		// FIXME find way to transfer column constraints WHILE making table here
		/*
		 * CREATE TABLE user_segments ( user_id VARCHAR(50) PRIMARY KEY, language
		 * VARCHAR(50), age VARCHAR(50), gender VARCHAR(50), goal VARCHAR(50));
		 */
		String SQL = buildTableCreationStatement(tableName, columnNames, "VARCHAR(50)");
		executeSQL(SQL);
	}

	public static void createTableAsCopy(String tableToCopyFrom, String newTableName) throws SQLException {
		// CREATE TABLE table2 AS SELECT * FROM table1;
		String SQL = "CREATE TABLE IF NOT EXISTS " + newTableName + " AS SELECT * FROM " + tableToCopyFrom;
		executeSQL(SQL);
	}

	public static List<String> selectColumnCells(String tableName, List<String> columnsToCheck,
			List<String> valuesToCheck, String columnToReturn) throws SQLException {
		String checkStatement = buildCheckStatement(columnsToCheck, valuesToCheck);
		java.sql.Statement st = null;
		ResultSet rs = null;
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		List<String> resultsFormatted = new ArrayList<String>();
		try {
			st = conn.createStatement();
			String query = "SELECT " + columnToReturn + " FROM " + tableName + " WHERE " + checkStatement;
			// Logger.print(query);
			rs = st.executeQuery(query);
			results = convertResultSetToList(rs);
			// Logger.printListOfHashMaps(results);
			rs.close();
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		resultsFormatted = convertListOf1DHashMapsToListOfStrings(results, columnToReturn);

		// Logger.printArrayListOfStrings((ArrayList<String>) resultsFormatted);
		return resultsFormatted;
	}

	// public static List<String> selectColumnCells(String tableName, List<String>
	// columnsToCheck,
	// List<String> valuesToCheck, List<String> columnsToSelect) throws SQLException
	// {
	// String checkStatement = buildCheckStatement(columnsToCheck, valuesToCheck);
	// String selectStatement = buildSelectStatement(columnsToSelect);
	// java.sql.Statement st = null;
	// ResultSet rs = null;
	// List<HashMap<String, Object>> results = new ArrayList<HashMap<String,
	// Object>>();
	// List<String> resultsFormatted = new ArrayList<String>();
	// try {
	// st = conn.createStatement();
	// String query = "SELECT " + columnToReturn + " FROM " + tableName + " WHERE "
	// + checkStatement;
	// // Logger.print(query);
	// rs = st.executeQuery(query);
	// results = convertResultSetToList(rs);
	// // Logger.printListOfHashMaps(results);
	// rs.close();
	// st.close();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// resultsFormatted = convert1DListOfHashMapsToListOfStrings(results,
	// columnToReturn);
	//
	// // Logger.printArrayListOfStrings((ArrayList<String>) resultsFormatted);
	// return resultsFormatted;
	// }

	public static List<String> selectRow(String tableName, List<String> columnsToCheck, List<String> valuesToCheck,
			List<String> columnsToSelect) {
		String selectStatement = buildSelectStatement(columnsToSelect);
		String checkStatement = buildCheckStatement(columnsToCheck, valuesToCheck);
		java.sql.Statement st = null;
		ResultSet rs = null;
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		List<String> resultsFormatted = new ArrayList<String>();
		try {
			st = conn.createStatement();
			String query = "SELECT " + selectStatement + " FROM " + tableName + " WHERE " + checkStatement;
			Logger.print(query);
			rs = st.executeQuery(query);
			results = convertResultSetToList(rs);
			// Logger.printListOfHashMaps(results);
			rs.close();
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		resultsFormatted = convertListOfOneHashMapToListOfStrings(results);

		// Logger.printArrayListOfStrings((ArrayList<String>) resultsFormatted);
		return resultsFormatted;
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
				// System.out.println(key);
				String value = (String) listOfHashMaps.get(i).get(key).toString();
				values.add(value);
			}
		}
		// Logger.printArrayListOfStrings((ArrayList<String>) values);
		return values;
	}

	public static void executeSQL(String SQL) {
		// Logger.print(SQL);
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(SQL);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void truncateTable(String tableName) throws SQLException {
		String SQL = "TRUNCATE TABLE " + tableName;
		executeSQL(SQL);
	}

	public static void dropTable(String tableName) throws SQLException {
		String SQL = "DROP TABLE " + tableName;
		executeSQL(SQL);
	}

	public static void updateTable(String tableName, String column, String value, String condition)
			throws SQLException {
		String SQL = "UPDATE " + tableName + " SET " + column + " = " + value;
		if (condition != "") {
			SQL += " WHERE " + condition;
		}
		executeSQL(SQL);
	}

	public static void addColumn(String tableName, String columnName, String dataType) throws SQLException {
		String SQL = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + dataType;
		executeSQL(SQL);
	}

	public static void dropColumn(String tableName, String columnName) throws SQLException {
		String SQL = "ALTER TABLE " + tableName + " DROP COLUMN " + columnName;
		executeSQL(SQL);
	}

	public static void insertIntoTable(String tableName, String[] columnNames, String[] columnValues)
			throws SQLException {

		java.sql.Statement st = null;
		ResultSet rs = null;
		try {
			st = conn.createStatement();
			String query = buildInsertStatement(tableName, columnNames, columnValues);
			rs = st.executeQuery(query);
			List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
			results = convertResultSetToList(rs);
			// Logger.printListOfHashMaps(results);
		} catch (Exception e) {
			e.printStackTrace();
		}
		rs.close();
		st.close();
	}

	public static int getNumRowsInTable(String tableName) throws SQLException {
		// TODO Auto-generated method stub
		java.sql.Statement st = null;
		ResultSet rs = null;
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		try {
			st = conn.createStatement();
			rs = st.executeQuery("SELECT count(*) FROM " + tableName + ";");
			results = convertResultSetToList(rs);
			// Logger.printListOfHashMaps(results);
		} catch (Exception e) {
			e.printStackTrace();
		}
		rs.close();
		st.close();
		// postgres row count is returned as long, so cast as int
		int numRowsInTable = toIntExact((long) results.get(0).get("count"));
		return numRowsInTable;
	}

	public static List<HashMap<String, Object>> selectColumn(String tableName, String columnName) throws SQLException {
		// TODO Auto-generated method stub
		java.sql.Statement st = null;
		ResultSet rs = null;
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		try {
			st = conn.createStatement();
			rs = st.executeQuery("SELECT segment, " + columnName + " FROM " + tableName + ";");
			// FIXME "segment" should be passed in!
			results = convertResultSetToList(rs);
			// Logger.printListOfHashMaps(results);
		} catch (Exception e) {
			e.printStackTrace();
		}
		rs.close();
		st.close();
		return results;
	}

	public static List<String> selectDistinctFromColumn(String tableName, String columnName) throws SQLException {
		// TODO Auto-generated method stub
		java.sql.Statement st = null;
		ResultSet rs = null;
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		List<String> resultsFormatted = new ArrayList<String>();
		try {
			st = conn.createStatement();
			rs = st.executeQuery("SELECT DISTINCT " + columnName + " FROM " + tableName);
			results = convertResultSetToList(rs);
			// Logger.printListOfHashMaps(results);
		} catch (Exception e) {
			e.printStackTrace();
		}
		rs.close();
		st.close();

		resultsFormatted = convertListOf1DHashMapsToListOfStrings(results, columnName);

		return resultsFormatted;
	}

	public static boolean checkRowExists(String tableName, List<String> columnsToCheck, List<String> valuesToCheck) {
		// TODO Auto-generated method stub
		String checkStatement = buildCheckStatement(columnsToCheck, valuesToCheck);
		java.sql.Statement st = null;
		ResultSet rs = null;
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		try {
			st = conn.createStatement();
			String query = "SELECT EXISTS (SELECT 1 FROM " + tableName + " WHERE " + checkStatement + ")";
			// Logger.print(query);
			rs = st.executeQuery(query);
			results = convertResultSetToList(rs);
			// Logger.printListOfHashMaps(results);
			rs.close();
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean rowExists = (Boolean) results.get(0).get("exists");
		// Logger.print(rowExists);
		return rowExists;
	}

	public static List<String> getColumnNames(String tableName) {
		java.sql.Statement st = null;
		ResultSet rs = null;
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		List<String> resultsFormatted = new ArrayList<String>();
		try {
			st = conn.createStatement();
			String query = "SELECT column_name FROM information_schema.columns WHERE table_name = '" + tableName + "'";
			// Logger.print(query);
			rs = st.executeQuery(query);
			results = convertResultSetToList(rs);
			// Logger.printListOfHashMaps(results);
			rs.close();
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		resultsFormatted = convertListOf1DHashMapsToListOfStrings(results, "column_name");
		// Logger.printArrayListOfStrings((ArrayList<String>) resultsFormatted);
		return resultsFormatted;
	}

	public static void copyCSVToTable(String userActionsFilepath, String tableName) {
		// FIXME this requires VARCHAR initial table, import, & manual data type change
		// ... also, tables must be truncated before they can be imported again
		String SQL = "COPY " + tableName + " FROM '" + userActionsFilepath + "' WITH (FORMAT csv);";
		executeSQL(SQL);
	}

	public static void alterColumnDataType(String tableName, String columnName, String newDataType) {
		// ALTER TABLE user_segments ALTER COLUMN age TYPE SMALLINT using age::SMALLINT;

		String SQL = "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " TYPE " + newDataType + " USING "
				+ columnName + "::" + newDataType;
		executeSQL(SQL);
	}

	public static String buildTableCreationStatement(String tableName, List<String> columnNames,
			String initialDataType) {
		String creationStatement = "CREATE TABLE IF NOT EXISTS " + tableName + " (";

		try {
			for (int col = 0; col < columnNames.size(); col++) {
				creationStatement += columnNames.get(col) + " " + initialDataType;
				if (col != columnNames.size() - 1) {
					creationStatement += ",";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		creationStatement += ");";
		// Logger.print(creationStatement);
		return creationStatement;
	}

	public static String buildCheckStatement(List<String> columnsToCheck, List<String> valuesToCheck) {
		// TODO Auto-generated method stub
		String checkStatement = "";

		try {
			for (int col = 0; col < columnsToCheck.size(); col++) {
				checkStatement += columnsToCheck.get(col);
				if (valuesToCheck.get(col).contains("NULL")) {
					checkStatement += " IS ";
				} else {
					checkStatement += " = ";
				}
				checkStatement += valuesToCheck.get(col);
				if (col != columnsToCheck.size() - 1) {
					checkStatement += " AND ";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return checkStatement;
	}

	public static String buildInsertStatement(String tableName, String[] columnNames, String[] columnValues) {

		String insertStatement = "";
		String insertKeys = "(";
		String insertValues = "(";
		String insertReturning = "";

		try {
			for (int col = 0; col < columnNames.length; col++) {
				if (col == columnNames.length - 1) {
					insertKeys += columnNames[col];
					insertValues += "'" + columnValues[col] + "'";
					insertReturning += columnNames[col];
				} else {
					insertKeys += columnNames[col] + ", ";
					insertValues += "'" + columnValues[col] + "', ";
					insertReturning += columnNames[col] + ", ";
				}
			}
			insertKeys += ")";
			insertValues += ")";

			insertStatement += "INSERT INTO " + tableName + " ";
			insertStatement += insertKeys;
			insertStatement += " VALUES " + insertValues;
			insertStatement += " RETURNING " + insertReturning;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return insertStatement;
	}

	public static String buildSelectStatement(List<String> columnNames) {

		String selectStatement = "";

		try {
			for (int col = 0; col < columnNames.size(); col++) {
				selectStatement += columnNames.get(col);
				if (col != columnNames.size() - 1) {
					selectStatement += ", ";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return selectStatement;
	}

	// =====================================================================
	// ========================== MAIN =====================================
	// =====================================================================

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			connectDatabase();
			System.out.println("connected db");
			// selectTable();
			// System.out.println("selected table");
			// truncateTable("cards");
			// System.out.println("truncated table");
			//
			// String[] columnNames = new String[2];
			// columnNames[0] = "card_id";
			// columnNames[1] = "drink";
			// String[] columnValues = new String[2];
			// columnValues[0] = "somecardif1234123412341234";
			// columnValues[1] = "true";
			// insertIntoTable("cards", columnNames, columnValues);
			// System.out.println("inserted into table");

			List<String> testColNames = new ArrayList<String>();
			testColNames.add("age");
			testColNames.add("frogs");
			testColNames.add("etc");

			dropTable("testTable");
			createTable("testTable", testColNames);
			addColumn("testTable", "oasdfasdf", "VARCHAR(3)");
			getColumnNames("cards");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
