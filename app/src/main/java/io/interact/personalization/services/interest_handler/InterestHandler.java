package io.interact.personalization.services.interest_handler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.interact.personalization.db.PostgresController;
import io.interact.personalization.services.card_handler.CardHandler;
import io.interact.personalization.services.user_handler.UserHandler;
import io.interact.personalization.utils.Logger;
import io.interact.personalization.utils.Operator;

public class InterestHandler {

	private static final String interestPredictionsFilePath = "/home/mikus/interest_predictions.csv";

	// =====================================================================
	// ===================== GETTERS & SETTERS =============================
	// =====================================================================

	// =====================================================================
	// ========================== METHODS ==================================
	// =====================================================================

	public static void generateInterestProbabilities() {
		// for every user sub-segment,.............................................
		// ..if there are any users that fit the sub-segment.......................
		// ....for every card tag..................................................
		// ......for every card with that tag......................................
		// ........for every user from this sub-segment............................
		// ..........if this user gave this card feedback..........................
		// ............get this user's feedback for this card......................
		// ............update average feedback for this sub-segment for this tag...

		int numSubSegments = UserHandler.getNumSubSegments();
		List<HashMap<String, Object>> segmentsAndSubSegments = new ArrayList<HashMap<String, Object>>();
		segmentsAndSubSegments = UserHandler.getSegmentsAndSubSegments();
		Logger.printListOfHashMaps(segmentsAndSubSegments);
		Logger.print("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		try {
			for (int i = 0; i < numSubSegments; i++) {
				String segment = (String) segmentsAndSubSegments.get(i).get("segment");
				String subSegment = (String) segmentsAndSubSegments.get(i).get("sub_segment");
				Logger.print(segment + ": " + subSegment);
				List<String> segmentToCheck = new ArrayList<String>();
				List<String> subSegmentToCheck = new ArrayList<String>();
				segmentToCheck.add(segment);
				subSegmentToCheck.add("'" + subSegment + "'");
				if (UserHandler.existsUserWithSubSegments(segmentToCheck, subSegmentToCheck)) {
					List<String> usersWithSubSegment = new ArrayList<String>();
					usersWithSubSegment = UserHandler.getUsersWithSubSegments(segmentToCheck, subSegmentToCheck);
					generateSubSegmentTagInterestProbabilities(subSegment, usersWithSubSegment);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void generateSubSegmentTagInterestProbabilities(String subSegment, List<String> usersWithSubSegment) {
		List<String> cardTags = new ArrayList<String>();
		cardTags = CardHandler.getAllCardTags();
		for (int k = 0; k < cardTags.size(); k++) {
			String cardTag = cardTags.get(k);
			Logger.print("..." + cardTag);
			List<String> cardsWithTag = new ArrayList<String>();
			List<String> tagToCheck = new ArrayList<String>();
			tagToCheck.add(cardTag);
			cardsWithTag = CardHandler.getCardsWithTags(tagToCheck);
			generateSubSegmentCardInterestProbabilities(subSegment, usersWithSubSegment, cardTag, cardsWithTag);

		}
	}

	public static void generateSubSegmentCardInterestProbabilities(String subSegment, List<String> usersWithSubSegment,
			String cardTag, List<String> cardsWithTag) {
		float averageTagInterest = 0;
		float sumOfTagInterest = 0;
		int sampleSizeForTag = 0;
		for (int p = 0; p < cardsWithTag.size(); p++) {
			String cardID = cardsWithTag.get(p);
			float averageCardInterest = 0;
			float sumOfCardInterest = 0;
			int sampleSizeForCard = 0;
			for (int j = 0; j < usersWithSubSegment.size(); j++) {
				String userID = usersWithSubSegment.get(j);
				if (UserHandler.thisUserGaveFeedbackToThisCard(userID, cardID)) {
					float userInterestInCard = UserHandler.getUserInterestInCard(userID, cardID);
					Logger.print("......" + cardID);
					Logger.print("........." + userID);
					Logger.print("............" + userInterestInCard);
					sumOfCardInterest += userInterestInCard;
					sampleSizeForCard++;
					sampleSizeForTag++;
				}
			}
			if (sampleSizeForCard > 0) {
				averageCardInterest = sumOfCardInterest / sampleSizeForCard;
				sumOfTagInterest += averageCardInterest;
			}
		}
		if (sampleSizeForTag > 0) {
			averageTagInterest = sumOfTagInterest / (float) sampleSizeForTag;
			updateSTINCItable(subSegment, cardTag, averageTagInterest);
		}
	}

	public static void updateSTINCItable(String subSegment, String cardTag, float averageTagInterest) {
		Logger.print("~~~~~~averageTagInterest = " + averageTagInterest);
		Logger.print(
				"about to update " + subSegment + "-" + cardTag + " interest probability as " + averageTagInterest);
		try {
			PostgresController.updateTable("STINCI_recipes", cardTag, Float.toString(averageTagInterest),
					"sub_segment = '" + subSegment + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void resetProbabilitiesInSTINCItable() {
		List<String> cardTags = new ArrayList<String>();
		cardTags = CardHandler.getAllCardTags();
		for (int k = 0; k < cardTags.size(); k++) {
			String cardTag = cardTags.get(k);
			try {
				PostgresController.updateTable("STINCI_recipes", cardTag, "NULL", "");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static boolean checkExistsSubSegmentTagInterestProbability(String subSegment, String cardTag) {
		// Logger.print("from checkExistsSubSegmentTagInterestProbability... cardTag = "
		// + cardTag);
		// Logger.print("...");
		// Logger.print("subSegment = " + subSegment);
		List<String> columnsToCheck = new ArrayList<String>();
		columnsToCheck.add("sub_segment");
		columnsToCheck.add(cardTag);
		List<String> valuesToCheck = new ArrayList<String>();
		valuesToCheck.add("'" + subSegment + "'");
		valuesToCheck.add("NOT NULL");
		boolean probabilityExists = false;

		probabilityExists = PostgresController.checkRowExists("STINCI_recipes", columnsToCheck, valuesToCheck);
		// Logger.print(probabilityExists);
		return probabilityExists;
	}

	public static boolean checkExistsUserTagInterestProbability(String userID, String cardTag) {
		List<String> subSegments = new ArrayList<String>();
		subSegments = UserHandler.getUserSubSegments(userID);
		boolean probabilityExists = false;
		for (int k = 0; k < subSegments.size(); k++) {
			String subSegment = subSegments.get(k);
			probabilityExists = checkExistsSubSegmentTagInterestProbability(subSegment, cardTag);
		}
		return probabilityExists;
	}

	public static float getSubSegmentTagInterestProbability(String subSegment, String cardTag) {
		List<String> columnsToCheck = new ArrayList<String>();
		columnsToCheck.add("sub_segment");
		List<String> valuesToCheck = new ArrayList<String>();
		valuesToCheck.add(subSegment);
		List<String> interestProbability = new ArrayList<String>();
		try {
			interestProbability = PostgresController.selectColumnCells("STINCI_recipes", columnsToCheck, valuesToCheck,
					cardTag);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		float probability = Float.parseFloat(interestProbability.get(0));
		// Logger.print(probability);
		return probability;
	}

	public static float generateTagInterestProbabilityForUserWithSubSegments(String cardTag, List<String> subSegments) {
		// Logger.print("... from
		// generateTagInterestProbabilityForUserWithSubSegments... cardTag = " + cardTag
		// + ", subSegments = " + subSegments);
		float userInterestProbability = 0; // set to ZERO by default which is the output if no data for any segments
		float subSegmentInterestProbabilitySum = 0;
		int numSubSegmentsWithData = 0;
		float subSegmentInterestProbability;

		for (int k = 0; k < subSegments.size(); k++) {
			String subSegment = subSegments.get(k);
			boolean existsSubSegmentTagInterestProbability = checkExistsSubSegmentTagInterestProbability(subSegment,
					cardTag);
			if (existsSubSegmentTagInterestProbability == true) {
				subSegmentInterestProbability = getSubSegmentTagInterestProbability("'" + subSegment + "'", cardTag);
				subSegmentInterestProbabilitySum += subSegmentInterestProbability;
				numSubSegmentsWithData++;
			}
		}
		if (numSubSegmentsWithData > 0) {
			userInterestProbability = subSegmentInterestProbabilitySum / numSubSegmentsWithData;
		}
		// Logger.print(userInterestProbability);
		return userInterestProbability;
	}

	public static float generateOverallUserInterestProbability(String userID) {

		List<String> subSegments = new ArrayList<String>();
		subSegments = UserHandler.getUserSubSegments(userID);

		float overallUserTagInterestProbability = 0;
		float overallUserTagInterestProbabilitySum = 0;
		int numTagsThatHaveInterestProbability = 0;

		List<String> cardTags = new ArrayList<String>();
		cardTags = CardHandler.getAllCardTags();
		for (int k = 0; k < cardTags.size(); k++) {
			String cardTag = cardTags.get(k);
			float userTagInterestProbability = generateTagInterestProbabilityForUserWithSubSegments(cardTag,
					subSegments);
			if (!Float.isNaN(userTagInterestProbability)) {
				overallUserTagInterestProbabilitySum += userTagInterestProbability;
				numTagsThatHaveInterestProbability++;
			}
		}

		overallUserTagInterestProbability = overallUserTagInterestProbabilitySum / numTagsThatHaveInterestProbability;
		Logger.print(overallUserTagInterestProbability);
		return overallUserTagInterestProbability;
	}

	public static float generateCardInterestProbabilityForUser(String cardID, String userID) {
		// Logger.print("... from generateTagInterestProbabilityForUser... cardID = " +
		// cardID + ", userID = " + userID);

		List<String> tagsOfCard = new ArrayList<String>();
		tagsOfCard = CardHandler.getTagValuesOfCard(cardID);
		// Logger.printArrayListOfStrings((ArrayList<String>) tagsOfCard);

		List<String> subSegments = new ArrayList<String>();
		subSegments = UserHandler.getUserSubSegments(userID);

		float userCardInterestProbability;
		float userTagInterestProbabilitySum = 0;
		int numTags = tagsOfCard.size();
		// Logger.print(numTags);

		for (int k = 0; k < numTags; k++) {
			String cardTag = tagsOfCard.get(k);
			// Logger.print(cardTag);
			userTagInterestProbabilitySum += generateTagInterestProbabilityForUserWithSubSegments(cardTag, subSegments);
		}
		userCardInterestProbability = userTagInterestProbabilitySum / numTags;
		// Logger.print(userCardInterestProbability);
		return userCardInterestProbability;
	}

	public static float generateTagInterestProbabilityForUser(String cardTag, String userID) {
		// Logger.print("... from generateTagInterestProbabilityForUser... cardTag = " +
		// cardTag + ", userID = " + userID);
		// Logger.print(cardTag);
		List<String> subSegments = new ArrayList<String>();
		subSegments = UserHandler.getUserSubSegments(userID);

		float userTagInterestProbability;
		userTagInterestProbability = generateTagInterestProbabilityForUserWithSubSegments(cardTag, subSegments);
		// Logger.print(userTagInterestProbability);
		return userTagInterestProbability;
	}

	public static HashMap<String, Object> generateAllTagInterestProbabilitiesForUser(String userID) {
		// Logger.print("... from generateAllTagInterestProbabilitiesForUser... userID =
		// " + userID);
		List<String> allCardTags = new ArrayList<String>();
		allCardTags = CardHandler.getAllCardTags();

		HashMap<String, Object> allTagInterestProbabilities = new HashMap<String, Object>();

		float userTagInterest;
		String cardTag;
		int numTags = allCardTags.size();

		boolean existsUserTagInterestProbability;

		for (int k = 0; k < numTags; k++) {
			cardTag = allCardTags.get(k);
			existsUserTagInterestProbability = checkExistsUserTagInterestProbability(userID, cardTag);
			if (existsUserTagInterestProbability == true) {
				userTagInterest = generateTagInterestProbabilityForUser(cardTag, userID);
				// Logger.print(cardTag);
				allTagInterestProbabilities.put(cardTag, userTagInterest);
			}
		}
		// Logger.printHashMap(allTagInterestProbabilities);
		return allTagInterestProbabilities;
	}

	public static String getTagWithMaxInterestProbabilityForUser(String userID) {
		// Logger.print("... from getTagWithMaxInterestProbabilityForUser... userID = "
		// + userID);
		HashMap<String, Object> allTagInterestProbabilities = generateAllTagInterestProbabilitiesForUser(userID);
		String keyOfMaxValue = Operator.getKeyOfMaxValueFromStringFloatHashMap(allTagInterestProbabilities);
		Logger.print("\t user " + userID + " is predicted to MOST LIKE cards with tag: " + keyOfMaxValue);
		// Logger.print("\t" + keyOfMaxValue);
		return keyOfMaxValue;
	}

	public static String getTagWithMinInterestProbabilityForUser(String userID) {
		// Logger.print("... from getTagWithMinInterestProbabilityForUser... userID = "
		// + userID);
		HashMap<String, Object> allTagInterestProbabilities = generateAllTagInterestProbabilitiesForUser(userID);
		String keyOfMinValue = Operator.getKeyOfMinValueFromStringFloatHashMap(allTagInterestProbabilities);
		Logger.print("\t user " + userID + " is predicted to LEAST LIKE cards with tag: " + keyOfMinValue);
		// Logger.print("\t" + keyOfMinValue);
		return keyOfMinValue;
	}

	public static List<String> generatePositiveInterestPredictionForUser(String userID) {
		// Logger.print("... from generatePositiveInterestPredictionForUser... userID =
		// " + userID);
		String tagPredictedToBeMostLiked = getTagWithMaxInterestProbabilityForUser(userID);
		// Logger.print("\t tagPredictedToBeLiked = " + tagPredictedToBeLiked);
		List<String> tagsToSearch = new ArrayList<String>();
		tagsToSearch.add(tagPredictedToBeMostLiked);
		float tagInterestProbability = generateTagInterestProbabilityForUser(tagPredictedToBeMostLiked, userID);
		Logger.print("\tinterest probability = " + tagInterestProbability);
		List<String> cardsPredictedToBeLiked = CardHandler.getCardsWithTags(tagsToSearch);
		addPredictionsToTable(userID, cardsPredictedToBeLiked, tagInterestProbability);
		// Logger.printArrayListOfStrings((ArrayList<String>) cardsPredictedToBeLiked);
		return cardsPredictedToBeLiked;
	}

	public static List<String> generateNegativeInterestPredictionForUser(String userID) {
		// Logger.print("... from generateNegativeInterestPredictionForUser... userID =
		// " + userID);
		String tagPredictedToBeLeastLiked = getTagWithMinInterestProbabilityForUser(userID);
		// Logger.print("\t tagPredictedToBeDisliked = " + tagPredictedToBeDisliked);
		List<String> tagsToSearch = new ArrayList<String>();
		tagsToSearch.add(tagPredictedToBeLeastLiked);
		float tagInterestProbability = generateTagInterestProbabilityForUser(tagPredictedToBeLeastLiked, userID);
		Logger.print("\tinterest probability = " + tagInterestProbability);

		List<String> cardsPredictedToBeDisliked = CardHandler.getCardsWithTags(tagsToSearch);
		addPredictionsToTable(userID, cardsPredictedToBeDisliked, tagInterestProbability);
		// exportPredictionsToCSV();
		// cardsPredictedToBeDisliked);
		return cardsPredictedToBeDisliked;
	}

	public static void addPredictionsToTable(String userID, List<String> cards, float tagInterestProbability) {
		// Logger.print("... from generateNegativeInterestPredictionForUser... userID =

		List<String> columnNames = new ArrayList<String>();
		columnNames.add("user_id");
		columnNames.add("card_id");
		columnNames.add("interest_probability");

		for (int i = 0; i < cards.size(); i++) {
			String cardID = cards.get(i);

			List<String> columnValues = new ArrayList<String>();
			columnValues.add("'" + userID + "'");
			columnValues.add("'" + cardID + "'");
			columnValues.add(Float.toString(tagInterestProbability));

			try {
				PostgresController.insertIntoTable("interest_predictions", columnNames, columnValues);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void exportPredictionsToCSV() {
		// Logger.print("... from generateNegativeInterestPredictionForUser... userID =
		PostgresController.copyTableToCSV(interestPredictionsFilePath, "interest_predictions");
	}

	public static void generateInterestPredictionsForUser(String userID) {
		// Logger.print("... from printInterestPredictionsForUser... userID = " +
		// userID);
		Logger.print("user " + userID + " is predicted to MOST LIKE these cards: ");
		List<String> cardsPredictedToBeMostLiked = generatePositiveInterestPredictionForUser(userID);
		Logger.printArrayListOfStrings((ArrayList<String>) cardsPredictedToBeMostLiked);
		Logger.print("user " + userID + " is predicted to LEAST LIKE these cards: ");
		List<String> cardsPredictedToBeLeastLiked = generateNegativeInterestPredictionForUser(userID);
		Logger.printArrayListOfStrings((ArrayList<String>) cardsPredictedToBeLeastLiked);
		Logger.print("");
	}

	public static void generateInterestPredictionsForAllUsers() {
		// Logger.print("... from printInterestPredictionsForAllUsers");
		try {
			PostgresController.truncateTable("interest_predictions");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<String> allUsers = UserHandler.getAllUsers();
		for (int i = 0; i < allUsers.size(); i++) {
			String userID = allUsers.get(i);
			if (UserHandler.checkUserHasAtleastOneSegment(userID)
					&& generateAllTagInterestProbabilitiesForUser(userID).size() > 0) {
				generateInterestPredictionsForUser(userID);
			}
		}
	}

	// TODO finish writing

	// public static void createSTACITable() {
	// // Logger.print("... from createSTACITable");
	// List<String> allSubgments = UserHandler.getSubSegmentLabels();
	// List<String> allTags = CardHandler.getAllCardTags();
	//
	// for (int i = 0; i < allSubgments.size(); i++) {
	// String subSegment1 = allSubgments.get(i);
	// for (int j = 0; j < allSubgments.size(); j++) {
	// String subSegment2 = allSubgments.get(j);
	// for (int k = 0; k < allTags.size(); k++) {
	// String cardTag = allTags.get(k);
	// }
	// }
	// }
	// }

	// =====================================================================
	// ========================== MAIN =====================================
	// =====================================================================

	public static void main(String[] args) throws Exception {
		PostgresController.connectDatabase();

		try {
			// UserHandler.importUserData();
			// UserHandler.processUserSegmentData();
			// UserHandler.processUserActionsData();
			// resetProbabilitiesInSTINCItable();
			// generateInterestProbabilities();
			//
			// checkExistsSubSegmentTagInterestProbability("'20-29'", "drink");
			// getSubSegmentTagInterestProbability("'20-29'", "drink");
			// checkExistsSubSegmentTagInterestProbability("'lifestyle'", "drink");
			// getSubSegmentTagInterestProbability("'lifestyle'", "drink");

			String userID = "1515541645148868";
			// generateOverallUserInterestProbability(userID);

			String cardID = "c14716e2-cf79-491d-8c46-169cbab60944";
			// if (CardHandler.cardHasTag(cardID)) {
			// generateCardInterestProbabilityForUser(cardID, userID);
			// }ArrayListOfStrings((ArrayList<String>) columnsToCheck);
			// Logger.printArrayListOfStrings((ArrayList<String>) valuesToCheck);
			// Logger.print

			// String keyOfMaxValue = getTagWithMaxInterestProbabilityForUser(userID);
			// String keyOfMinValue = getTagWithMinInterestProbabilityForUser(userID);

			generateInterestPredictionsForAllUsers();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}