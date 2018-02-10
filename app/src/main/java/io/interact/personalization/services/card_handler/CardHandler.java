package io.interact.personalization.services.card_handler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import io.interact.personalization.config.Config;
import io.interact.personalization.services.postgres.PostgresController;
import io.interact.personalization.utils.Logger;
import io.interact.personalization.utils.Requester;
import net.minidev.json.JSONArray;

public class CardHandler {

	// =====================================================================
	// ===================== GETTERS & SETTERS =============================
	// =====================================================================

	// =====================================================================
	// ========================== METHODS ==================================
	// =====================================================================

	public static void UpdateCardsTable() {
		String url = null;
		String cards = null;

		try {
			for (int i = 0; i < Config.getHundredsOfCardsAvailable(); i++) {
				url = "https://contentmanager-lb.interact.io/content-cards/search?offset=" + i * 100 + "&limit=100";
				// hard-set limit of Content Manager is 100 cards
				cards = Requester.sendPostRequest(url);

				String id;
				String metaDataContentType;

				for (int cardIndex = 0; cardIndex < 100; cardIndex++) {
					try {
						if (isCardToBeAdded(cards, cardIndex)) {
							id = JsonPath.parse(cards).read("$.data.[" + cardIndex + "].id");
							metaDataContentType = JsonPath.parse(cards)
									.read("$.data.[" + cardIndex + "].metadata.contentType");
							Logger.print(id);
							Logger.print(metaDataContentType);
							// TODO if this tag isn't already in cards table, add it as a new column
							addCardToTable(id, metaDataContentType);
						}
					} catch (Exception e) {
						// FIXME don't ignore exception here... it's bad practice
						// TODO make it so that JsonPath only checks up until there are cards
						// e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isCardToBeAdded(String cards, int cardIndex) {

		JSONArray metaData = new JSONArray();
		String metaDataContentType;

		metaData = JsonPath.parse(cards).read("$.data.[" + cardIndex + "].metadata[?(@ != null)]");
		if (metaData != null) {
			metaDataContentType = JsonPath.parse(cards).read("$.data.[" + cardIndex + "].metadata.contentType");
			if (metaDataContentType != null) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public static void addCardToTable(String id, String metaDataContentType) {

		String[] columnNames = new String[2];
		columnNames[0] = "card_id";
		columnNames[1] = metaDataContentType.replaceAll(" ", "_").toLowerCase();
		String[] columnValues = new String[2];
		columnValues[0] = id.toLowerCase();
		columnValues[1] = "true";
		try {
			PostgresController.insertIntoTable("cards", columnNames, columnValues);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void importCards() {
		try {
			PostgresController.connectDatabase();
			Logger.print("connected db");
			PostgresController.truncateTable("cards");
			Logger.print("truncated cards table");
			UpdateCardsTable();
			Logger.print("updated cards table");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<String> getAllCardTags() {
		List<String> cardTags = new ArrayList<String>();
		cardTags = PostgresController.getColumnNames("cards");
		cardTags.remove(0); // remove card_id column
		return cardTags;
	}

	public static List<String> getCardsWithTags(List<String> tagsToCheck) {
		List<String> valuesToCheck = new ArrayList<String>();
		for (int i = 0; i < tagsToCheck.size(); i++) {
			valuesToCheck.add("true");
		}
		List<String> cardsWithTags = new ArrayList<String>();
		try {
			cardsWithTags = PostgresController.selectColumnCells("cards", tagsToCheck, valuesToCheck, "card_id");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cardsWithTags;
	}

	// =====================================================================
	// ========================== MAIN =====================================
	// =====================================================================
	public static void main(String[] args) throws Exception {
		importCards();
	}

}