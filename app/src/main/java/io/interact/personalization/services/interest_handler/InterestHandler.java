package io.interact.personalization.services.interest_handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	public static void fillSTINCItable() {
		// pseudocode:
		// * for every user sub-segment,
		// * ..if there are any users that fit the sub-segment
		// * ....for every such user
		// * ......for every card tag,
		// * ........if any cards with that tag that these users have seen,
		// * ..........for every such card,
		// * ............evaluate feedback these users gave to those cards
		// * ............update average feedback for this sub-segment for this tag

		int numSubSegments = UserHandler.getNumSubSegments();
		List<HashMap<String, Object>> segmentsAndSubSegments = new ArrayList<HashMap<String, Object>>();
		segmentsAndSubSegments = UserHandler.getSegmentsAndSubSegments();
		Logger.printListOfHashMaps(segmentsAndSubSegments);

		for (int i = 0; i < numSubSegments; i++) {
			String segment = (String) segmentsAndSubSegments.get(i).get("segment");
			String subSegment = (String) segmentsAndSubSegments.get(i).get("sub_segment");
			// Logger.print(segment + ": " + subSegment);
			List<String> segmentsToCheck = new ArrayList<String>();
			List<String> subSegmentsToCheck = new ArrayList<String>();
			segmentsToCheck.add(segment);
			subSegmentsToCheck.add(subSegment);
			if (UserHandler.existsUserWithSubSegments(segmentsToCheck, subSegmentsToCheck)) {

			}
		}
	}

	// =====================================================================
	// ========================== MAIN =====================================
	// =====================================================================

	public static void main(String[] args) throws Exception {
		PostgresController.connectDatabase();
		fillSTINCItable();
	}
}