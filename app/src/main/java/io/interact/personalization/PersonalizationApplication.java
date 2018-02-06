package io.interact.personalization;

import io.interact.personalization.services.card_handler.CardHandler;
import io.interact.personalization.services.interest_handler.InterestHandler;
import io.interact.personalization.services.user_handler.UserHandler;

public class PersonalizationApplication {

	// =====================================================================
	// ===================== GETTERS & SETTERS =============================
	// =====================================================================

	// =====================================================================
	// ========================== METHODS ==================================
	// =====================================================================

	public static void setupInputTables() {
		UserHandler.importUserData();
		CardHandler.importCards();
	}

	public static void setupSegmentTagInterestTable() {
		InterestHandler.fillSTINCItable();
	}

	// =====================================================================
	// ========================== MAIN =====================================
	// =====================================================================
	public static void main(String[] args) throws Exception {
		setupInputTables();
		setupSegmentTagInterestTable();
	}
}