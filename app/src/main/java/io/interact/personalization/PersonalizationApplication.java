package io.interact.personalization;

import java.sql.SQLException;

import io.interact.personalization.db.PostgresController;
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
		UserHandler.processUserSegmentData();
		UserHandler.processUserActionsData();

		CardHandler.importCards();
	}

	public static void setupSegmentTagInterestTable() {
		InterestHandler.resetProbabilitiesInSTINCItable();
		InterestHandler.generateInterestProbabilities();
	}

	public static void runPersonalizationApp() {
		try {
			PostgresController.connectDatabase();
			setupInputTables();
			setupSegmentTagInterestTable();
			// ...output predictions!
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// =====================================================================
	// ========================== MAIN =====================================
	// =====================================================================
	public static void main(String[] args) throws Exception {
		runPersonalizationApp();
	}
}