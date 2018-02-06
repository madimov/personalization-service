package io.interact.personalization.services.user_handler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.interact.personalization.services.csv.CSVParser;
import io.interact.personalization.services.postgres.PostgresController;
import io.interact.personalization.utils.Logger;

public class UserHandler {

	// TODO decide: should user actions and segment data be re-uploaded to local
	// postgres?

	private static final String userActionsFilePath = "/home/mikus/user_actions.csv";
	private static final String userSegmentsFilePath = "/home/mikus/user_segments.csv";
	private static final Logger logger = new Logger();

	// =====================================================================
	// ===================== GETTERS & SETTERS =============================
	// =====================================================================

	public static String getUserActionsFilepath() {
		return userActionsFilePath;
	}

	public static String getUserSegmentsFilepath() {
		return userSegmentsFilePath;
	}

	// =====================================================================
	// ========================== METHODS ==================================
	// =====================================================================
	public static ArrayList<String[]> parseUserActionsData() { // unused for now
		CSVParser parser = new CSVParser();
		return parser.getArrayListFromCSV(getUserActionsFilepath());
	}

	public static ArrayList<String[]> parseUserSegmentsData() { // unused for now
		CSVParser parser = new CSVParser();
		return parser.getArrayListFromCSV(getUserSegmentsFilepath());
	}

	public static void importUserData() {
		// private static ArrayList<String[]> userActionsData = new
		// ArrayList<String[]>();
		// userActionsData = parseUserActionsData();
		// Logger.printArrayListOfStringArrays(userActionsData);
		importUserSegmentData();
		importUserActionsData();

		/*
		 * USE THIS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		 * http://www.postgresqltutorial.com/postgresql-cast/
		 * 
		 */
	}

	public static void importUserSegmentData() {

		List<String> segmentDataLabels = new ArrayList<String>();
		segmentDataLabels.add("user_id");
		segmentDataLabels.add("locale");
		segmentDataLabels.add("age_RAW");
		segmentDataLabels.add("gender");
		segmentDataLabels.add("goal");

		try {
			PostgresController.dropTable("user_segments_UNPROCESSED");
			PostgresController.createTable("user_segments_UNPROCESSED", segmentDataLabels);
			PostgresController.copyCSVToTable(getUserSegmentsFilepath(), "user_segments_UNPROCESSED");
			PostgresController.alterColumnDataType("user_segments_UNPROCESSED", "age_RAW", "SMALLINT");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void importUserActionsData() {

		List<String> actionDataLabels = new ArrayList<String>();
		actionDataLabels.add("action_id");
		actionDataLabels.add("user_id");
		actionDataLabels.add("card_id");
		actionDataLabels.add("initial_feedback");
		actionDataLabels.add("further_interest");
		actionDataLabels.add("detailed_feedback");

		try {
			PostgresController.dropTable("user_actions_UNPROCESSED");
			PostgresController.createTable("user_actions_UNPROCESSED", actionDataLabels);
			PostgresController.copyCSVToTable(getUserActionsFilepath(), "user_actions_UNPROCESSED");
			PostgresController.alterColumnDataType("user_actions_UNPROCESSED", "further_interest", "BOOLEAN");
			PostgresController.alterColumnDataType("user_actions_UNPROCESSED", "detailed_feedback", "SMALLINT");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void processUserSegmentData() {

		try {
			PostgresController.addColumn("user_segments_UNPROCESSED", "age_range", "VARCHAR");
			PostgresController.updateTable("user_segments_UNPROCESSED", "age_range", "'<20'", "age_RAW < 20");
			PostgresController.updateTable("user_segments_UNPROCESSED", "age_range", "'20-29'",
					"age_RAW >= 20 AND age_RAW < 30");

			PostgresController.dropTable("user_segments");
			PostgresController.createTableAsCopy("user_segments_UNPROCESSED", "user_segments");
			// PostgresController.alterColumnDataType("user_segments", "age",
			// "VARCHAR(50)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int getNumSubSegments() {
		// TODO Auto-generated method stub
		// TODO decide: move this to InterestHandler, as it accesses STINCI table?
		int numSubSegments = 0;
		try {
			numSubSegments = PostgresController.getNumRowsInTable("STINCI_recipes");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return numSubSegments;
	}

	public static List<HashMap<String, Object>> getSegmentsAndSubSegments() {
		// TODO Auto-generated method stub
		// TODO decide: move this to InterestHandler, as it accesses STINCI table?
		List<HashMap<String, Object>> segmentsAndSubSegments = new ArrayList<HashMap<String, Object>>();
		try {
			segmentsAndSubSegments = PostgresController.getColumn("STINCI_recipes", "sub_segment");
			// Logger.printListOfHashMaps(segmentsAndSubSegments);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return segmentsAndSubSegments;
	}

	public static boolean existsUserWithSubSegments(List<String> segments, List<String> subSegments) {
		return PostgresController.checkRowExists("user_segments", segments, subSegments);
	}

	// =====================================================================
	// ========================== MAIN =====================================
	// =====================================================================
	public static void main(String[] args) throws Exception {

		PostgresController.connectDatabase();

		getSegmentsAndSubSegments();

		try {
			importUserData();
			processUserSegmentData();
		} catch (Exception e) {
			e.getMessage();
		}

		// List<String> segmentsToCheck = new ArrayList<String>();
		// List<String> subSegmentsToCheck = new ArrayList<String>();
		// segmentsToCheck.add("age");
		// subSegmentsToCheck.add("20-30");
		// existsUserWithSubSegments(segmentsToCheck, subSegmentsToCheck);
	}
}