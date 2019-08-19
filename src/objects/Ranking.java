package objects;

public class Ranking {
	public Integer UserId;
	public Float Miles; 
	public Integer Locations; 
	public Integer TranscriptionCharacters; 
	public Integer DescriptionCharacters;
	public Integer Enrichments;
	
	
	public void setUserId(Integer userId) {
		UserId = userId;
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
