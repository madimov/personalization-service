package io.interact.personalization.services.postgres;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PostgresControllerTest {

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

	// @Test
	// public void performingUnsnoozeActionSholudSetContactSnoozedUntilToNull() {
	// ContactDTO contact = new ContactDTO();
	// contact.setSnoozedUntil(new Date());
	// Action action = new Action();
	// action.setActionType(ActionType.UNSNOOZE);
	//
	// actionPerformer.performAction(action, contact, mock(UserContextDTO.class));
	//
	// assertThat(contact.getSnoozedUntil()).isNull();
	// }

	@Test
	public void testConnectDatabase() {
		try {
			PostgresController.connectDatabase();
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
		}
		assertNotNull(PostgresController.conn);
	}

	@Test
	public void testConvertResultSetToList() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSelectTable() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCreateTable() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCreateTableAsCopy() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSelectColumnCells() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testConvert1DListOfHashMapsToListOfStrings() {
		fail("Not yet implemented"); // TODO
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
