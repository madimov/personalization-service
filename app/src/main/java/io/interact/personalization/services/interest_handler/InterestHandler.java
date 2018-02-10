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
		} catch (Exception e) {
			e.getMessage();
		}
	}
}