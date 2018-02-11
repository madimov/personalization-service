package io.interact.personalization.services.interest_handler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.interact.personalization.services.card_handler.CardHandler;
import io.interact.personalization.services.postgres.PostgresController;
import io.interact.personalization.services.user_handler.UserHandler;
import io.interact.personalization.utils.Logger;

public class InterestHandler {
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
		List<String> columnsToCheck = new ArrayList<String>();
		columnsToCheck.add("sub_segment");
		columnsToCheck.add(cardTag);
		List<String> valuesToCheck = new ArrayList<String>();
		valuesToCheck.add(subSegment);
		valuesToCheck.add("NOT NULL");
		boolean probabilityExists = false;
		probabilityExists = PostgresController.checkRowExists("STINCI_recipes", columnsToCheck, valuesToCheck);
		// Logger.print(probabilityExists);
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

		float userInterestProbability = 0; // set to ZERO by default which is the output if no data for any segments
		float subSegmentInterestProbabilitySum = 0;
		int numSubSegmentsWithData = 0;
		float subSegmentInterestProbability;

		for (int k = 0; k < subSegments.size(); k++) {
			String subSegment = subSegments.get(k);
			if (checkExistsSubSegmentTagInterestProbability("'" + subSegment + "'", cardTag)) {
				subSegmentInterestProbability = getSubSegmentTagInterestProbability("'" + subSegment + "'", cardTag);
				subSegmentInterestProbabilitySum += subSegmentInterestProbability;
				numSubSegmentsWithData++;
			}
		}
		if (numSubSegmentsWithData > 0) {
			userInterestProbability = subSegmentInterestProbabilitySum / numSubSegmentsWithData;
		}
		Logger.print(userInterestProbability);
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

		List<String> tagsOfCard = new ArrayList<String>();
		tagsOfCard = CardHandler.getTagValuesOfCard(cardID);
		// Logger.printArrayListOfStrings((ArrayList<String>) tagsOfCard);

		List<String> subSegments = new ArrayList<String>();
		subSegments = UserHandler.getUserSubSegments(userID);

		float userCardInterestProbability;
		float userTagInterestProbabilitySum = 0;
		int numTags = tagsOfCard.size();
		Logger.print(numTags);

		for (int k = 0; k < numTags; k++) {
			String cardTag = tagsOfCard.get(k);
			Logger.print(cardTag);
			userTagInterestProbabilitySum += generateTagInterestProbabilityForUserWithSubSegments(cardTag, subSegments);
		}
		userCardInterestProbability = userTagInterestProbabilitySum / numTags;
		Logger.print(userCardInterestProbability);
		return userCardInterestProbability;
	}

	// =====================================================================
	// ========================== MAIN =====================================
	// =====================================================================

	public static void main(String[] args) throws Exception {
		PostgresController.connectDatabase();

		try {
			UserHandler.importUserData();
			UserHandler.processUserSegmentData();
			UserHandler.processUserActionsData();
			resetProbabilitiesInSTINCItable();
			generateInterestProbabilities();

			checkExistsSubSegmentTagInterestProbability("'20-29'", "drink");
			getSubSegmentTagInterestProbability("'20-29'", "drink");
			checkExistsSubSegmentTagInterestProbability("'lifestyle'", "drink");
			getSubSegmentTagInterestProbability("'lifestyle'", "drink");

			String userID = "1515541645148868";
			generateOverallUserInterestProbability(userID);

			String cardID = "c14716e2-cf79-491d-8c46-169cbab60944";
			if (CardHandler.cardHasTag(cardID)) {
				generateCardInterestProbabilityForUser(cardID, userID);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}