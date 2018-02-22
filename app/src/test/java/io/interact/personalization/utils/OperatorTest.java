package io.interact.personalization.utils;

import static org.junit.Assert.assertNotNull;
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

import io.interact.personalization.db.PostgresController;

public class OperatorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEqualsListOfHashMaps() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testConvertListOf1DHashMapsToListOfStrings() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testConvertListOfOneHashMapToListOfStrings() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testMain() {
		fail("Not yet implemented"); // TODO
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
			boolean testTableExists = PostgresController.checkTableExists("testTable");
			if (testTableExists == true) {
				PostgresController.dropTable("testTable");
			}
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

}
