package io.interact.personalization.services.postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

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
		// while (rs.next()) {
		// System.out.print("Column 1 returned ");
		// System.out.println(rs.getString(1));
		// System.out.println(rs);
		// }
		rs.close();
		st.close();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			connectDatabase();
			System.out.println("connected db");
			selectTable();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
