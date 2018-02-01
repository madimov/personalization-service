package io.interact.personalization;

import java.util.ArrayList;

import io.interact.personalization.services.csv.CSVParser;
import io.interact.personalization.utils.Logger;

public class PersonalizationApplication {
	private static final String userActionsFilePath = "/home/mikus/user_actions.csv";
	private static final String userSegmentsFilePath = "/home/mikus/user_segments.csv";
	private static final Logger logger = new Logger();

	private static ArrayList<String[]> userActions = new ArrayList<String[]>();
	private static ArrayList<String[]> userSegments = new ArrayList<String[]>();

	// =====================================================================
	// ===================== GETTERS & SETTERS =============================
	// =====================================================================
	public static ArrayList<String[]> getUserActions() {
		return userActions;
	}

	public static void setUserActions(ArrayList<String[]> userActions) {
		PersonalizationApplication.userActions = userActions;
	}

	public static ArrayList<String[]> getUserSegments() {
		return userSegments;
	}

	public static void setUserSegments(ArrayList<String[]> userSegments) {
		PersonalizationApplication.userSegments = userSegments;
	}

	// =====================================================================
	// ========================== METHODS ==================================
	// =====================================================================
	public static ArrayList<String[]> parseUserActionsData() {
		CSVParser parser = new CSVParser();
		return parser.getArrayListFromCSV(userActionsFilePath);
	}

	public static ArrayList<String[]> parseUserSegmentsData() {
		CSVParser parser = new CSVParser();
		return parser.getArrayListFromCSV(userSegmentsFilePath);
	}

	// =====================================================================
	// ========================== MAIN =====================================
	// =====================================================================
	public static void main(String[] args) throws Exception {

		setUserActions(parseUserActionsData());
		logger.printArrayListOfStringArrays(userActions);

		setUserSegments(parseUserSegmentsData());
		logger.printArrayListOfStringArrays(userSegments);

	}
}