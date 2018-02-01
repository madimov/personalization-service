package io.interact.personalization.config;

public class PersonalizationApplicationConfig {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
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

}
