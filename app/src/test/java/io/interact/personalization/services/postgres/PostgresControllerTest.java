package io.interact.personalization.services.postgres;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.interact.personalization.utils.Operator;

public class PostgresControllerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("1");
		testColValues.add("2");

		PostgresController.connectDatabase();
		PostgresController.dropTable("testTable");
		PostgresController.createTable("testTable", testColNames);
		PostgresController.getColumnNames("testTable");
		PostgresController.insertIntoTable("testTable", testColNames, testColValues);
	}

	@After
	public void tearDown() throws Exception {
	}

	// TODO reorder tests to match order of funcs in Postgres Controller

	@Test
	public void testConnectDatabase() {
		try {
			PostgresController.disconnectDatabase();
			PostgresController.connectDatabase();
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(PostgresController.conn);
	}

	@Test
	public void testDisconnectDatabase() {
		try {
			PostgresController.disconnectDatabase();
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		// Logger.print(PostgresController.conn);
		assertNull(PostgresController.conn);
	}

	@Test
	public void testConvertResultSetToList() {
		List<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> actual = new ArrayList<HashMap<String, Object>>();
		java.sql.Statement st = null;
		ResultSet rs = null;

		HashMap mMap = new HashMap();
		mMap.put("a", 1);
		mMap.put("b", 2);
		expected.add(mMap);

		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("1");
		testColValues.add("2");

		try {
			PostgresController.connectDatabase();
			st = PostgresController.conn.createStatement();
			PostgresController.dropTable("testTable");
			PostgresController.createTable("testTable", testColNames);
			PostgresController.getColumnNames("testTable");
			PostgresController.insertIntoTable("testTable", testColNames, testColValues);
			rs = st.executeQuery("SELECT * FROM testTable");
			actual = Operator.convertResultSetToList(rs);
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(actual);
		assertTrue("both lists of hash maps are equal", Operator.equalsListOfHashMaps(expected, actual));
	}

	@Test
	public void testSelectTable() {
		List<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> actual = new ArrayList<HashMap<String, Object>>();

		HashMap mMap = new HashMap();
		mMap.put("a", 1);
		mMap.put("b", 2);
		expected.add(mMap);

		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("1");
		testColValues.add("2");

		try {
			PostgresController.connectDatabase();
			PostgresController.dropTable("testTable");
			PostgresController.createTable("testTable", testColNames);
			PostgresController.getColumnNames("testTable");
			PostgresController.insertIntoTable("testTable", testColNames, testColValues);
			actual = PostgresController.selectTable("testTable");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(actual);
		assertTrue("both lists of hash maps are equal", Operator.equalsListOfHashMaps(expected, actual));
	}

	@Test
	public void testCreateTable() {

		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		boolean existsBeforeCreation = true; // initialized to wrong value
		boolean existsAfterCreation = false; // initialized to wrong value
		try {
			PostgresController.connectDatabase();
			PostgresController.dropTable("testTable");
			existsBeforeCreation = PostgresController.checkTableExists("testTable");
			PostgresController.createTable("testTable", testColNames);
			existsAfterCreation = PostgresController.checkTableExists("testTable");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertFalse("table doesn't exist yet", existsBeforeCreation);
		assertTrue("table exists now", existsAfterCreation);
	}

	@Test
	public void testCreateTableAsCopy() {
		List<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> actual = new ArrayList<HashMap<String, Object>>();

		HashMap mMap = new HashMap();
		mMap.put("a", 1);
		mMap.put("b", 2);
		expected.add(mMap);

		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("1");
		testColValues.add("2");

		try {
			PostgresController.connectDatabase();
			PostgresController.dropTable("testTable");
			PostgresController.createTable("testTable", testColNames);
			PostgresController.getColumnNames("testTable");
			PostgresController.insertIntoTable("testTable", testColNames, testColValues);
			PostgresController.createTableAsCopy("testTable", "copyTestTable");
			actual = PostgresController.selectTable("copyTestTable");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(actual);
		assertTrue("both lists of hash maps are equal", Operator.equalsListOfHashMaps(expected, actual));
	}

	@Test
	public void testcheckTableExists() {
		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		boolean existsBeforeCreation = true; // initialized to wrong value
		boolean existsAfterCreation = false; // initialized to wrong value
		try {
			PostgresController.connectDatabase();
			PostgresController.dropTable("testTable");
			existsBeforeCreation = PostgresController.checkTableExists("testTable");
			PostgresController.createTable("testTable", testColNames);
			existsAfterCreation = PostgresController.checkTableExists("testTable");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertFalse("table doesn't exist yet", existsBeforeCreation);
		assertTrue("table exists now", existsAfterCreation);
	}

	@Test
	public void testSelectColumnCells() {
		List<String> expected = new ArrayList<String>();
		expected.add("a1");
		List<String> actual = new ArrayList<String>();

		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("'a1'");
		testColValues.add("'b2'");

		try { // TODO move these to setup() once it's clear what setup all the tests need
			PostgresController.connectDatabase();
			PostgresController.dropTable("testTable");
			PostgresController.createTable("testTable", testColNames);
			PostgresController.getColumnNames("testTable");
			PostgresController.insertIntoTable("testTable", testColNames, testColValues);
			actual = PostgresController.selectColumnCells("testTable", testColNames, testColValues, "a");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(actual);
		assertTrue("both lists are equal", expected.equals(actual));
	}

	@Test
	public void testSelectRow() {
		List<String> expected = new ArrayList<String>();
		expected.add("a1");
		expected.add("b2");
		List<String> actual = new ArrayList<String>();

		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("'a1'");
		testColValues.add("'b2'");

		try { // TODO move these to setup() once it's clear what setup all the tests need
			PostgresController.connectDatabase();
			PostgresController.dropTable("testTable");
			PostgresController.createTable("testTable", testColNames);
			PostgresController.getColumnNames("testTable");
			PostgresController.insertIntoTable("testTable", testColNames, testColValues);
			actual = PostgresController.selectRow("testTable", testColNames, testColValues, testColNames);
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(actual);
		assertTrue("both lists are equal", expected.equals(actual));
	}

	@Test
	public void testExecuteSQL() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testTruncateTable() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testDropTable() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testUpdateTable() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testAddColumn() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testDropColumn() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testInsertIntoTable() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetNumRowsInTable() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSelectColumn() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCheckRowExists() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetColumnNames() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCopyCSVToTable() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testAlterColumnDataType() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testBuildTableCreationStatement() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testBuildCheckStatement() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testBuildInsertStatement() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testMain() {
		fail("Not yet implemented"); // TODO
	}

}
