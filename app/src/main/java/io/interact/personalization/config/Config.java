package io.interact.personalization.config;

public class Config {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private static int hundredsOfCardsAvailable = 9;

	// TODO change these to useful values
	private String apiKey;
	private Boolean migrateDbOnStartup;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public Boolean getMigrateDbOnStartup() {
		return migrateDbOnStartup;
	}

	public void setMigrateDbOnStartup(Boolean migrateDbOnStartup) {
		this.migrateDbOnStartup = migrateDbOnStartup;
	}

	public static int getHundredsOfCardsAvailable() {
		return hundredsOfCardsAvailable;
	}

	public void setHundredsOfCardsAvailable(int hundredsOfCardsAvailable) {
		this.hundredsOfCardsAvailable = hundredsOfCardsAvailable;
	}

}
