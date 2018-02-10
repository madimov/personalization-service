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
		importUserSegmentData();
		importUserActionsData();
		/*
		 * USE THIS for immediate casting from csv to postgres !!!!!!!!!!!!!!!!!!!
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
		actionDataLabels.add("initial_reaction");
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
			PostgresController.addColumn("user_segments_UNPROCESSED", "age", "VARCHAR(20)");
			processAgesIntoAgeRanges();
			PostgresController.dropTable("user_segments");
			PostgresController.createTableAsCopy("user_segments_UNPROCESSED", "user_segments");
			PostgresController.dropColumn("user_segments", "age_RAW");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void processAgesIntoAgeRanges() {
		try {
			PostgresController.updateTable("user_segments_UNPROCESSED", "age", "'<20'", "age_RAW < 20");
			PostgresController.updateTable("user_segments_UNPROCESSED", "age", "'20-29'",
					"age_RAW >= 20 AND age_RAW < 30");
			PostgresController.updateTable("user_segments_UNPROCESSED", "age", "'30-39'",
					"age_RAW >= 30 AND age_RAW < 40");
			PostgresController.updateTable("user_segments_UNPROCESSED", "age", "'40-49'",
					"age_RAW >= 40 AND age_RAW < 50");
			PostgresController.updateTable("user_segments_UNPROCESSED", "age", "'50-59'",
					"age_RAW >= 50 AND age_RAW < 60");
			PostgresController.updateTable("user_segments_UNPROCESSED", "age", "'60-69'",
					"age_RAW >= 60 AND age_RAW < 70");
			PostgresController.updateTable("user_segments_UNPROCESSED", "age", "'70-79'",
					"age_RAW >= 70 AND age_RAW < 80");
			PostgresController.updateTable("user_segments_UNPROCESSED", "age", "'>80'", "age_RAW >= 80");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void processUserActionsData() {

		try {
			PostgresController.addColumn("user_actions_UNPROCESSED", "interest_index", "NUMERIC (3, 2)");
			processFeedbackIntoInterestIndex();
			PostgresController.dropTable("user_actions");
			PostgresController.createTableAsCopy("user_actions_UNPROCESSED", "user_actions");
			// dropping these may be unnecessary and having them could be useful later on...
			// just dropping them now for clarity of tables
			PostgresController.dropColumn("user_actions", "initial_reaction");
			PostgresController.dropColumn("user_actions", "further_interest");
			PostgresController.dropColumn("user_actions", "detailed_feedback");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void processFeedbackIntoInterestIndex() {
		processNegativeInitialReactionFeedback();
		processNeutralInitialReactionFeedback();
		processPositiveInitialReactionFeedback();
	}

	// TODO refactor these 3 huge funcs into 1 with a for loop and many if-else-if's
	public static void processNegativeInitialReactionFeedback() {
		try {
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0",
					"initial_reaction = 'negative' AND further_interest = false");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.1",
					"initial_reaction = 'negative' AND further_interest IS NULL");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.2",
					"initial_reaction = 'negative' AND further_interest = true");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.1",
					"initial_reaction = 'negative' AND further_interest = true AND detailed_feedback = 0");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.2",
					"initial_reaction = 'negative' AND further_interest = true AND detailed_feedback >= 1 AND detailed_feedback < 3");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.3",
					"initial_reaction = 'negative' AND further_interest = true AND detailed_feedback >= 3 AND detailed_feedback < 5");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.4",
					"initial_reaction = 'negative' AND further_interest = true AND detailed_feedback >= 5 AND detailed_feedback < 7");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.5",
					"initial_reaction = 'negative' AND further_interest = true AND detailed_feedback >= 7 AND detailed_feedback < 9");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.6",
					"initial_reaction = 'negative' AND further_interest = true AND detailed_feedback = 9");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.7",
					"initial_reaction = 'negative' AND further_interest = true AND detailed_feedback = 10");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void processNeutralInitialReactionFeedback() {
		try {
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.4",
					"initial_reaction = 'neutral' AND further_interest = false");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.5",
					"initial_reaction = 'neutral' AND further_interest IS NULL");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.6",
					"initial_reaction = 'neutral' AND further_interest = true");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.2",
					"initial_reaction = 'neutral' AND further_interest = true AND detailed_feedback = 0");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.3",
					"initial_reaction = 'neutral' AND further_interest = true AND detailed_feedback >= 1 AND detailed_feedback < 3");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.4",
					"initial_reaction = 'neutral' AND further_interest = true AND detailed_feedback >= 3 AND detailed_feedback < 5");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.5",
					"initial_reaction = 'neutral' AND further_interest = true AND detailed_feedback >= 5 AND detailed_feedback < 7");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.6",
					"initial_reaction = 'neutral' AND further_interest = true AND detailed_feedback >= 7 AND detailed_feedback < 9");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.7",
					"initial_reaction = 'neutral' AND further_interest = true AND detailed_feedback = 9");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.8",
					"initial_reaction = 'neutral' AND further_interest = true AND detailed_feedback = 10");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void processPositiveInitialReactionFeedback() {
		try {
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.6",
					"initial_reaction = 'positive' AND further_interest = false");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.7",
					"initial_reaction = 'positive' AND further_interest IS NULL");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.8",
					"initial_reaction = 'positive' AND further_interest = true");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.4",
					"initial_reaction = 'positive' AND further_interest = true AND detailed_feedback = 0");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.5",
					"initial_reaction = 'positive' AND further_interest = true AND detailed_feedback >= 1 AND detailed_feedback < 3");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.6",
					"initial_reaction = 'positive' AND further_interest = true AND detailed_feedback >= 3 AND detailed_feedback < 5");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.7",
					"initial_reaction = 'positive' AND further_interest = true AND detailed_feedback >= 5 AND detailed_feedback < 7");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.8",
					"initial_reaction = 'positive' AND further_interest = true AND detailed_feedback >= 7 AND detailed_feedback < 9");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "0.9",
					"initial_reaction = 'positive' AND further_interest = true AND detailed_feedback = 9");
			PostgresController.updateTable("user_actions_UNPROCESSED", "interest_index", "1.0",
					"initial_reaction = 'positive' AND further_interest = true AND detailed_feedback = 10");
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
			segmentsAndSubSegments = PostgresController.selectColumn("STINCI_recipes", "sub_segment");
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

	public static boolean existsUserWhoGaveFeedbackToCard(String cardID) {
		List<String> columnsToCheck = new ArrayList<String>();
		columnsToCheck.add("card_id");
		List<String> valuesToCheck = new ArrayList<String>();
		valuesToCheck.add("'" + cardID + "'");
		return PostgresController.checkRowExists("user_actions", columnsToCheck, valuesToCheck);
	}

	public static boolean thisUserGaveFeedbackToThisCard(String userID, String cardID) {
		List<String> columnsToCheck = new ArrayList<String>();
		columnsToCheck.add("user_id");
		columnsToCheck.add("card_id");
		List<String> valuesToCheck = new ArrayList<String>();
		valuesToCheck.add("'" + userID + "'");
		valuesToCheck.add("'" + cardID + "'");
		return PostgresController.checkRowExists("user_actions", columnsToCheck, valuesToCheck);
	}

	public static List<String> getUsersWithSubSegments(List<String> segmentsToCheck, List<String> subSegmentsToCheck) {
		List<String> usersWithSubSegments = new ArrayList<String>();
		try {
			usersWithSubSegments = PostgresController.selectColumnCells("user_segments", segmentsToCheck,
					subSegmentsToCheck, "user_id");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return usersWithSubSegments;
	}

	public static List<String> getUsersWhoGaveFeedbackToCard(String cardID) {
		List<String> columnsToCheck = new ArrayList<String>();
		columnsToCheck.add("card_id");
		List<String> valuesToCheck = new ArrayList<String>();
		valuesToCheck.add("'" + cardID + "'");
		List<String> usersWhoGaveFeedbackToCard = new ArrayList<String>();
		try {
			usersWhoGaveFeedbackToCard = PostgresController.selectColumnCells("user_actions", columnsToCheck,
					valuesToCheck, "user_id");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return usersWhoGaveFeedbackToCard;
	}

	public static float getUserInterestInCard(String userID, String cardID) {
		List<String> columnsToCheck = new ArrayList<String>();
		columnsToCheck.add("user_id");
		columnsToCheck.add("card_id");

		List<String> valuesToCheck = new ArrayList<String>();
		valuesToCheck.add("'" + userID + "'");
		valuesToCheck.add("'" + cardID + "'");
		List<String> userInterestInCard = new ArrayList<String>();
		try {
			userInterestInCard = PostgresController.selectColumnCells("user_actions", columnsToCheck, valuesToCheck,
					"interest_index");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// Logger.print(userInterestInCard);
		return Float.parseFloat(userInterestInCard.get(0));
	}

	// =====================================================================
	// ========================== MAIN =====================================
	// =====================================================================
	public static void main(String[] args) throws Exception {

		PostgresController.connectDatabase();

		try {
			importUserData();
			processUserSegmentData();
			processUserActionsData();
		} catch (Exception e) {
			e.getMessage();
		}

		List<String> segmentsToCheck = new ArrayList<String>();
		List<String> subSegmentsToCheck = new ArrayList<String>();
		segmentsToCheck.add("age_range");
		subSegmentsToCheck.add("'20-29'");
		boolean existsUser = existsUserWithSubSegments(segmentsToCheck, subSegmentsToCheck);
	}
}