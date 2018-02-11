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
							// Logger.print(id);
							// Logger.print(metaDataContentType);
							String labeledCardTag = metaDataContentType.replaceAll(" ", "_").toLowerCase();
							// TODO if this tag isn't already in cards table, add it as a new column
							addCardToTable(id, labeledCardTag);
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

	public static void addCardToTable(String id, String labeledCardTag) {
		// Logger.print("adding card " + id + " with tag " + labeledCardTag);
		List<String> allCardTags = new ArrayList<String>();
		allCardTags = getAllCardTags();
		List<String> columnNames = new ArrayList<String>();
		List<String> columnValues = new ArrayList<String>();
		columnNames.add("card_id");
		columnValues.add("'" + id.toLowerCase() + "'"); // is toLowerCase necessary?
		for (int i = 0; i < allCardTags.size(); i++) {
			String cardTag = allCardTags.get(i);
			columnNames.add(cardTag);
			// Logger.print("cardTag = " + cardTag + ", labeledCardTag = " +
			// labeledCardTag);
			if (cardTag.equals(labeledCardTag)) {
				// Logger.print(cardTag + " == " + labeledCardTag);
				columnValues.add("true");
			} else { // FIXME avoid this else by just setting false tag values as default in postgres
				columnValues.add("false");
			}
		}
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

	public static boolean cardHasTag(String cardID) {

		boolean cardHasTag;
		List<String> columnToCheck = new ArrayList<String>();
		columnToCheck.add("card_id");
		List<String> valueToCheck = new ArrayList<String>();
		valueToCheck.add("'" + cardID + "'");

		cardHasTag = PostgresController.checkRowExists("cards", columnToCheck, valueToCheck);
		// Logger.print(cardHasTag);
		return cardHasTag;
	}

	public static List<String> getTagValuesOfCard(String cardID) {

		List<String> tagValuesOfCard = new ArrayList<String>();
		List<String> columnsToReturn = new ArrayList<String>();
		List<String> columnToCheck = new ArrayList<String>();
		columnToCheck.add("card_id");
		List<String> valueToCheck = new ArrayList<String>();
		valueToCheck.add("'" + cardID + "'");

		columnsToReturn = getAllCardTags();
		tagValuesOfCard = PostgresController.selectRow("cards", columnToCheck, valueToCheck, columnsToReturn);
		// Logger.printArrayListOfStrings((ArrayList<String>) tagValuesOfCard);
		List<String> tagsOfCard = new ArrayList<String>();
		for (int i = 0; i < tagValuesOfCard.size(); i++) {
			String tagValue = tagValuesOfCard.get(i);
			if (tagValue.equals("true")) {
				String tag = columnsToReturn.get(i);
				tagsOfCard.add(tag);
			}
		}
		return tagsOfCard;
	}

	// =====================================================================
	// ========================== MAIN =====================================
	// =====================================================================
	public static void main(String[] args) throws Exception {
		importCards();
	}
}