package objects;

public class Ranking {
	public Integer UserId;
	public Integer TeamId;
	public String TeamName;
	public Float Miles; 
	public Float MilesPerPerson;
	public Integer Locations; 
	public Integer TranscriptionCharacters; 
	public Integer DescriptionCharacters;
	public Integer Enrichments;
	
	
	public void setUserId(Integer userId) {
		UserId = userId;
	}
	public void setTeamId(Integer teamId) {
		TeamId = teamId;
	}
	public void setTeamName(String teamName) {
		TeamName = teamName;
	}
	public void setMilesPerPerson(Float milesPerPerson) {
		MilesPerPerson = milesPerPerson;
	}
	public void setMiles(Float miles) {
		Miles = miles;
	}
	public void setLocations(Integer locations) {
		Locations = locations;
	}
	public void setTranscriptionCharacters(Integer transcriptionCharacters) {
		TranscriptionCharacters = transcriptionCharacters;
	}
	public void setDescriptionCharacters(Integer descriptionCharacters) {
		DescriptionCharacters = descriptionCharacters;
	}
	public void setEnrichments(Integer enrichments) {
		Enrichments = enrichments;
	}	
}
