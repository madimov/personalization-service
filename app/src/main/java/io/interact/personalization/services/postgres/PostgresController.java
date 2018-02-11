package io.interact.personalization.services.postgres;

import static java.lang.Math.toIntExact;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import io.interact.personalization.utils.Operator;

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

	public static void disconnectDatabase() throws SQLException {
		// TODO Auto-generated method stub
		conn.close();
		conn = null;
	}

	// =====================================================================
	// ========================== SQL UPDATES ==============================
	// =====================================================================

	public static void executeSQLUpdate(String SQL) {
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

	public static void createTable(String tableName, List<String> columnNames) throws SQLException {
		// FIXME find way to transfer column constraints WHILE making table here
		/*
		 * CREATE TABLE user_segments ( user_id VARCHAR(50) PRIMARY KEY, language
		 * VARCHAR(50), age VARCHAR(50), gender VARCHAR(50), goal VARCHAR(50));
		 */
		String SQL = buildTableCreationStatement(tableName, columnNames, "VARCHAR(50)");
		executeSQLUpdate(SQL);
	}

	public static void createTableAsCopy(String tableToCopyFrom, String newTableName) throws SQLException {
		// CREATE TABLE table2 AS SELECT * FROM table1;
		String SQL = "CREATE TABLE IF NOT EXISTS " + newTableName + " AS SELECT * FROM " + tableToCopyFrom;
		executeSQLUpdate(SQL);
	}

	public static void dropTable(String tableName) throws SQLException {
		String SQL = "DROP TABLE " + tableName;
		executeSQLUpdate(SQL);
	}

	public static void truncateTable(String tableName) throws SQLException {
		String SQL = "TRUNCATE TABLE " + tableName;
		executeSQLUpdate(SQL);
	}

	public static void copyCSVToTable(String userActionsFilepath, String tableName) {
		// FIXME this requires VARCHAR initial table, import, & manual data type change
		// ... also, tables must be truncated before they can be imported again
		String SQL = "COPY " + tableName + " FROM '" + userActionsFilepath + "' WITH (FORMAT csv);";
		executeSQLUpdate(SQL);
	}

	public static void alterColumnDataType(String tableName, String columnName, String newDataType) {
		// ALTER TABLE user_segments ALTER COLUMN age TYPE SMALLINT using age::SMALLINT;

		String SQL = "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " TYPE " + newDataType + " USING "
				+ columnName + "::" + newDataType;
		executeSQLUpdate(SQL);
	}

	public static void insertIntoTable(String tableName, List<String> columnNames, List<String> columnValues)
			throws SQLException {
		String SQL = buildInsertStatement(tableName, columnNames, columnValues);
		executeSQLUpdate(SQL);
	}

	public static void updateTable(String tableName, String column, String value, String condition)
			throws SQLException {
		String SQL = "UPDATE " + tableName + " SET " + column + " = " + value;
		if (condition != "") {
			SQL += " WHERE " + condition;
		}
		executeSQLUpdate(SQL);
	}

	public static void addColumn(String tableName, String columnName, String dataType) throws SQLException {
		String SQL = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + dataType;
		executeSQLUpdate(SQL);
	}

	public static void dropColumn(String tableName, String columnName) throws SQLException {
		String SQL = "ALTER TABLE " + tableName + " DROP COLUMN " + columnName;
		executeSQLUpdate(SQL);
	}

	// =====================================================================
	// ========================== SQL QUERIES ==============================
	// =====================================================================

	public static List<HashMap<String, Object>> executeSQLQuery(String SQL) {
		// Logger.print(SQL);
		java.sql.Statement st = null;
		ResultSet rs = null;
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		try {
			st = conn.createStatement();
			rs = st.executeQuery(SQL);
			results = Operator.convertResultSetToList(rs);
			// Logger.printListOfHashMaps(results);
			rs.close();
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	public static boolean checkTableExists(String tableName) throws SQLException {
		String SQL = "SELECT EXISTS (SELECT 1 FROM information_schema.tables " + "WHERE table_name = '"
				+ tableName.toLowerCase() + "')";
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		results = executeSQLQuery(SQL);
		boolean tableExists = (Boolean) results.get(0).get("exists");
		// Logger.print(tableExists);
		return tableExists;
	}

	public static List<HashMap<String, Object>> selectTable(String tableName) throws SQLException {
		String SQL = "SELECT * FROM " + tableName;
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		results = executeSQLQuery(SQL);
		return results;
	}

	public static int getNumRowsInTable(String tableName) throws SQLException {
		String SQL = "SELECT count(*) FROM " + tableName + ";";
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		results = executeSQLQuery(SQL);
		// postgres row count is returned as long, so cast as int
		int numRowsInTable = toIntExact((long) results.get(0).get("count"));
		return numRowsInTable;
	}

	public static List<String> getColumnNames(String tableName) {
		String SQL = "SELECT column_name FROM information_schema.columns WHERE table_name = '" + tableName + "'";
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		results = executeSQLQuery(SQL);
		List<String> resultsFormatted = new ArrayList<String>();
		resultsFormatted = Operator.convertListOf1DHashMapsToListOfStrings(results, "column_name");
		// Logger.printArrayListOfStrings((ArrayList<String>) resultsFormatted);
		return resultsFormatted;
	}

	public static List<HashMap<String, Object>> selectColumn(String tableName, String columnName) throws SQLException {
		String SQL = "SELECT segment, " + columnName + " FROM " + tableName + ";";
		// FIXME "segment" should be passed in!
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		results = executeSQLQuery(SQL);
		return results;
	}

	public static List<String> selectDistinctFromColumn(String tableName, String columnName) throws SQLException {
		String SQL = "SELECT DISTINCT " + columnName + " FROM " + tableName;
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		results = executeSQLQuery(SQL);
		List<String> resultsFormatted = new ArrayList<String>();
		resultsFormatted = Operator.convertListOf1DHashMapsToListOfStrings(results, columnName);
		return resultsFormatted;
	}

	public static boolean checkRowExists(String tableName, List<String> columnsToCheck, List<String> valuesToCheck) {
		String checkStatement = buildCheckStatement(columnsToCheck, valuesToCheck);
		String SQL = "SELECT EXISTS (SELECT 1 FROM " + tableName + " WHERE " + checkStatement + ")";
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		results = executeSQLQuery(SQL);
		boolean rowExists = (Boolean) results.get(0).get("exists");
		// Logger.print(rowExists);
		return rowExists;
	}

	public static List<String> selectRow(String tableName, List<String> columnsToCheck, List<String> valuesToCheck,
			List<String> columnsToSelect) {
		String selectStatement = buildSelectStatement(columnsToSelect);
		String checkStatement = buildCheckStatement(columnsToCheck, valuesToCheck);
		String SQL = "SELECT " + selectStatement + " FROM " + tableName + " WHERE " + checkStatement;
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		results = executeSQLQuery(SQL);
		List<String> resultsFormatted = new ArrayList<String>();
		resultsFormatted = Operator.convertListOfOneHashMapToListOfStrings(results);
		// Logger.printArrayListOfStrings((ArrayList<String>) resultsFormatted);
		return resultsFormatted;
	}

	public static List<String> selectColumnCells(String tableName, List<String> columnsToCheck,
			List<String> valuesToCheck, String columnToReturn) throws SQLException {
		// TODO change to take in a List<String> for columnsToReturn
		String checkStatement = buildCheckStatement(columnsToCheck, valuesToCheck);
		String SQL = "SELECT " + columnToReturn + " FROM " + tableName.toLowerCase() + " WHERE " + checkStatement;
		List<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
		results = executeSQLQuery(SQL);
		List<String> resultsFormatted = new ArrayList<String>();
		resultsFormatted = Operator.convertListOf1DHashMapsToListOfStrings(results, columnToReturn);
		return resultsFormatted;
	}

	// =====================================================================
	// ========================== HELPERS ==================================
	// =====================================================================

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

	public static String buildInsertStatement(String tableName, List<String> columnNames, List<String> columnValues) {

		String insertStatement = "";
		String insertKeys = "(";
		String insertValues = "(";

		try {
			for (int col = 0; col < columnNames.size(); col++) {
				insertKeys += columnNames.get(col);
				insertValues += columnValues.get(col); // FIXME add single quotes around string vals in caller func
				if (col != columnNames.size() - 1) {
					insertKeys += ", ";
					insertValues += ", ";
				}
			}
			insertKeys += ")";
			insertValues += ")";

			insertStatement += "INSERT INTO " + tableName + " ";
			insertStatement += insertKeys;
			insertStatement += " VALUES " + insertValues;
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
			getColumnNames("testTable");
			selectTable("testTable");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}