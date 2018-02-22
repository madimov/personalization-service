package io.interact.personalization.services.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.interact.personalization.db.PostgresController;
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
		testColValues.add("'1'");
		testColValues.add("'2'");

		PostgresController.connectDatabase();
		boolean testTableExists = PostgresController.checkTableExists("testTable");
		if (testTableExists == true) {
			PostgresController.dropTable("testTable");
		}
		PostgresController.createTable("testTable", testColNames);
		PostgresController.getColumnNames("testTable");
		PostgresController.insertIntoTable("testTable", testColNames, testColValues);
	}

	@After
	public void tearDown() throws Exception {
		boolean testDatabaseConnected = PostgresController.checkDatabaseConnected();
		if (testDatabaseConnected == true) {
			boolean testTableExists = PostgresController.checkTableExists("testTable");
			if (testTableExists == true) {
				PostgresController.dropTable("testTable");
			}
			PostgresController.disconnectDatabase();
		}
	}

	// TODO add failing test cases for each func!!!

	// =====================================================================
	// ========================== DATABASE =================================
	// =====================================================================

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
	public void testCheckDatabaseConnected() {
		boolean connectedBeforeDisconnection = false; // initialized to wrong value
		boolean connectedAfterDisconnection = true; // initialized to wrong value
		try {
			connectedBeforeDisconnection = PostgresController.checkDatabaseConnected();
			PostgresController.disconnectDatabase();
			connectedAfterDisconnection = PostgresController.checkDatabaseConnected();
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		// Logger.print(PostgresController.conn);
		assertTrue("connection still exists", connectedBeforeDisconnection);
		assertFalse("connection doesn't exist anymore", connectedAfterDisconnection);
	}

	// =====================================================================
	// ========================== SQL UPDATES ==============================
	// =====================================================================

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

		try {
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
	public void testDropTable() {
		boolean existsBeforeDeletion = false; // initialized to wrong value
		boolean existsAfterDeletion = true; // initialized to wrong value
		try {
			existsBeforeDeletion = PostgresController.checkTableExists("testTable");
			PostgresController.dropTable("testTable");
			existsAfterDeletion = PostgresController.checkTableExists("testTable");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertTrue("table still exists", existsBeforeDeletion);
		assertFalse("table doesn't exist anymore", existsAfterDeletion);
	}

	@Test
	public void testTruncateTable() {
		boolean hasContentBeforeTruncation = false; // initialized to wrong value
		boolean hasContentAfterTruncation = true; // initialized to wrong value
		try {
			hasContentBeforeTruncation = PostgresController.checkTableHasContent("testTable");
			PostgresController.truncateTable("testTable");
			hasContentAfterTruncation = PostgresController.checkTableHasContent("testTable");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertTrue("table doesn't exist yet", hasContentBeforeTruncation);
		assertFalse("table exists now", hasContentAfterTruncation);
	}

	// TODO write test
	@Test
	public void testCopyCSVToTable() {
		// FIXME this requires a CSV file creator to create a temporary CSV file in the
		// current directory which can be imported as a table, and the file should be
		// deleted at the end of this test... leaving this for later as it'll take some
		// time but isn't a top priority.

		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testAlterColumnDataType() {
		String newDataType = "numeric";
		String dataTypeBeforeAlteration = "";
		String dataTypeAfterAlteration = "";
		try {
			PostgresController.truncateTable("testTable");
			dataTypeBeforeAlteration = PostgresController.getColumnDataType("testTable", "a");
			PostgresController.alterColumnDataType("testTable", "a", "SMALLINT");
			dataTypeAfterAlteration = PostgresController.getColumnDataType("testTable", "a");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertEquals("character varying", dataTypeBeforeAlteration);
		assertEquals("smallint", dataTypeAfterAlteration);
	}

	@Test
	public void testInsertIntoTable() {
		List<String> expected = new ArrayList<String>();
		expected.add("3");
		expected.add("4");
		List<String> actual = new ArrayList<String>();
		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("'3'");
		testColValues.add("'4'");
		try {
			PostgresController.insertIntoTable("testTable", testColNames, testColValues);
			actual = PostgresController.selectRow("testTable", testColNames, testColValues, testColNames);
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(actual);
		assertTrue("both lists are equal", expected.equals(actual));
	}

	// TODO write test
	@Test
	public void testDeleteRows() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testUpdateTable() {
		List<String> expected = new ArrayList<String>();
		expected.add("9");
		expected.add("2");
		List<String> actual = new ArrayList<String>();
		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("'9'");
		testColValues.add("'2'");
		try {
			PostgresController.updateTable("testTable", "a", "'9'", "a = '1'");
			actual = PostgresController.selectRow("testTable", testColNames, testColValues, testColNames);
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(actual);
		assertTrue("both lists are equal", expected.equals(actual));
	}

	@Test
	public void testAddColumn() {
		List<String> actualColumnNames = new ArrayList<String>();
		List<String> expectedColumnNames = new ArrayList<String>();
		expectedColumnNames.add("a");
		expectedColumnNames.add("b");
		expectedColumnNames.add("c");

		String actualDataTypeOfNewColumn = "";
		String expectedDataTypeOfNewColumn = "smallint";
		try {
			// Logger.print("...");
			PostgresController.addColumn("testTable", "c", "SMALLINT");
			actualColumnNames = PostgresController.getColumnNames("testTable");
			actualDataTypeOfNewColumn = PostgresController.getColumnDataType("testTable", "c");
			// Logger.print("...");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(actualColumnNames);
		assertTrue("column names are as expected", expectedColumnNames.equals(actualColumnNames));
		assertTrue("new column data type is as expected",
				expectedDataTypeOfNewColumn.equals(actualDataTypeOfNewColumn));
	}

	@Test
	public void testDropColumn() {
		List<String> actualColumnNames = new ArrayList<String>();
		List<String> expectedColumnNames = new ArrayList<String>();
		expectedColumnNames.add("a");

		try {
			// Logger.print("...");
			PostgresController.dropColumn("testTable", "b");
			actualColumnNames = PostgresController.getColumnNames("testTable");
			// Logger.print("...");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(actualColumnNames);
		assertTrue("column names are as expected", expectedColumnNames.equals(actualColumnNames));
	}

	// =====================================================================
	// ========================== SQL QUERIES ==============================
	// =====================================================================

	@Test
	public void testcheckTableExists() {
		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		boolean existsBeforeCreation = true; // initialized to wrong value
		boolean existsAfterCreation = false; // initialized to wrong value
		try {
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
	public void testcheckTableHasContent() {
		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("1");
		testColValues.add("2");
		boolean hasContentBeforeTruncation = false; // initialized to wrong value
		boolean hasContentAfterTruncation = true; // initialized to wrong value
		boolean hasContentAfterInsertion = false; // initialized to wrong value
		try {
			hasContentBeforeTruncation = PostgresController.checkTableHasContent("testTable");
			PostgresController.truncateTable("testTable");
			hasContentAfterTruncation = PostgresController.checkTableHasContent("testTable");
			PostgresController.insertIntoTable("testTable", testColNames, testColValues);
			hasContentAfterInsertion = PostgresController.checkTableHasContent("testTable");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertTrue("table doesn't exist yet", hasContentBeforeTruncation);
		assertFalse("table exists now", hasContentAfterTruncation);
		assertTrue("table exists now", hasContentAfterInsertion);
	}

	@Test
	public void testSelectTable() {
		List<HashMap<String, Object>> expected = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> actual = new ArrayList<HashMap<String, Object>>();

		HashMap mMap = new HashMap();
		mMap.put("a", 1);
		mMap.put("b", 2);
		expected.add(mMap);

		try {
			actual = PostgresController.selectTable("testTable");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(actual);
		assertTrue("both lists of hash maps are equal", Operator.equalsListOfHashMaps(expected, actual));
	}

	@Test
	public void testGetNumRowsInTable() {
		int expectedNumRowsInTable = 1;
		int actualNumRowsInTable = 0;
		try {
			// Logger.print("...");
			actualNumRowsInTable = PostgresController.getNumRowsInTable("testTable");
			// Logger.print("...");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertEquals(expectedNumRowsInTable, actualNumRowsInTable);
	}

	@Test
	public void testGetColumnNames() {
		List<String> actualColumnNames = new ArrayList<String>();
		List<String> expectedColumnNames = new ArrayList<String>();
		expectedColumnNames.add("a");
		expectedColumnNames.add("b");

		// Logger.print("...");
		actualColumnNames = PostgresController.getColumnNames("testTable");
		// Logger.print("...");
		assertNotNull(actualColumnNames);
		assertTrue("column names are as expected", expectedColumnNames.equals(actualColumnNames));
	}

	@Test
	public void testGetColumnDataType() {
		String expectedDataType = "character varying";
		String actual = PostgresController.getColumnDataType("testTable", "a");
		assertNotNull(actual);
		assertEquals(expectedDataType, actual);
	}

	// TODO write test
	@Test
	public void testSelectColumn() {
		// TODO write test after merging this func with selectColumnCells
		// ...returning List of Hash Maps
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSelectDistinctFromColumn() {
		List<String> expected = new ArrayList<String>();
		expected.add("3");
		expected.add("1");
		List<String> actual = new ArrayList<String>();

		// TODO make and call test helper functions that set up these recurring lists
		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues1 = new ArrayList<String>();
		testColValues1.add("'1'");
		testColValues1.add("'2'");

		List<String> testColValues2 = new ArrayList<String>();
		testColValues2.add("'3'");
		testColValues2.add("'4'");

		try {
			// Logger.print("...");
			PostgresController.insertIntoTable("testTable", testColNames, testColValues1);
			PostgresController.insertIntoTable("testTable", testColNames, testColValues2);
			PostgresController.insertIntoTable("testTable", testColNames, testColValues1);
			actual = PostgresController.selectDistinctFromColumn("testTable", "a");
			// Logger.print("...");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(actual);
		assertTrue("both lists are equal", expected.equals(actual));
	}

	@Test
	public void testCheckRowExists() {
		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("'1'");
		testColValues.add("'2'");
		boolean existsBeforeDeletion = false; // initialized to wrong value
		boolean existsAfterDeletion = true; // initialized to wrong value
		try {
			// Logger.print("...");
			existsBeforeDeletion = PostgresController.checkRowExists("testTable", testColNames, testColValues);
			PostgresController.truncateTable("testTable");
			existsAfterDeletion = PostgresController.checkRowExists("testTable", testColNames, testColValues);
			// Logger.print("...");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertTrue("row still exists", existsBeforeDeletion);
		assertFalse("row doesn't exist anymore", existsAfterDeletion);
	}

	@Test
	public void testSelectRow() {
		List<String> expected = new ArrayList<String>();
		expected.add("1");
		expected.add("2");
		List<String> actual = new ArrayList<String>();

		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("'1'");
		testColValues.add("'2'");
		actual = PostgresController.selectRow("testTable", testColNames, testColValues, testColNames);
		assertNotNull(actual);
		assertTrue("both lists are equal", expected.equals(actual));
	}

	// TODO rewrite test
	@Test
	public void testSelectColumnCells() {
		// TODO rewrite test after merging this func with selectColumnCells
		// ...returning List of Hash Maps
		List<String> expected = new ArrayList<String>();
		expected.add("1");
		List<String> actual = new ArrayList<String>();

		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("'1'");
		testColValues.add("'2'");

		try { // TODO move these to setup() once it's clear what setup all the tests need
			actual = PostgresController.selectColumnCells("testTable", testColNames, testColValues, "a");
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(actual);
		assertTrue("both lists are equal", expected.equals(actual));
	}

	// =====================================================================
	// ========================== HELPERS ==================================
	// =====================================================================

	// =====================================================================
	// ========================== HELPERS ==================================
	// =====================================================================

	@Test
	public void testBuildTableCreationStatement() {
		String expectedStatement = "CREATE TABLE IF NOT EXISTS testTable (a SMALLINT,b SMALLINT);";
		String actualStatement = "";

		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");

		actualStatement = PostgresController.buildTableCreationStatement("testTable", testColNames, "SMALLINT");
		assertNotNull(actualStatement);
		assertTrue("table creation statement is as expected", expectedStatement.equals(actualStatement));
	}

	@Test
	public void testBuildCheckStatement() {
		String expectedStatement = "a = '1' AND b = '2'";
		String actualStatement = "";

		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("'1'");
		testColValues.add("'2'");

		// Logger.print("...");
		actualStatement = PostgresController.buildCheckStatement(testColNames, testColValues);
		// Logger.print("...");
		assertNotNull(actualStatement);
		assertTrue("table creation statement is as expected", expectedStatement.equals(actualStatement));
	}

	@Test
	public void testBuildInsertStatement() {
		String expectedStatement = "INSERT INTO testTable (a, b) VALUES ('1', '2')";
		String actualStatement = "";

		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");
		List<String> testColValues = new ArrayList<String>();
		testColValues.add("'1'");
		testColValues.add("'2'");

		// Logger.print("...");
		actualStatement = PostgresController.buildInsertStatement("testTable", testColNames, testColValues);
		// Logger.print("...");
		assertNotNull(actualStatement);
		assertTrue("table creation statement is as expected", expectedStatement.equals(actualStatement));
	}

	@Test
	public void testBuildSelectStatement() {
		String expectedStatement = "a, b";
		String actualStatement = "";

		List<String> testColNames = new ArrayList<String>();
		testColNames.add("a");
		testColNames.add("b");

		// Logger.print("...");
		actualStatement = PostgresController.buildSelectStatement(testColNames);
		// Logger.print("...");
		assertNotNull(actualStatement);
		assertTrue("table creation statement is as expected", expectedStatement.equals(actualStatement));
	}

}
