package objects;

public class Ranking {
	public Integer UserId;
	public Integer EventUser;
	public String Role;
	public Integer TeamId;
	public String TeamName;
	public Float Miles; 
	public Float MilesPerPerson;
	public Integer Locations; 
	public Integer TranscriptionCharacters; 
	public Integer Enrichments;
	
	
	public void setUserId(Integer userId) {
		UserId = userId;
	}
	public void setEventUser(Integer eventUser) {
		EventUser = eventUser;
	}
	public void setRole(String role) {
		Role = role;
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
	public void setEnrichments(Integer enrichments) {
		Enrichments = enrichments;
	}	
}
