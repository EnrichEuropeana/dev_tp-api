package objects;

import java.sql.Timestamp;
import java.util.List;

public class Campaign {
	public Integer CampaignId;
	public String Name; 
	public Timestamp Start; 
	public Timestamp End; 
	public String Public; 
	public Integer DatasetId; 
	public String DatasetName; 
	public List<Team> Teams;

	public void setCampaignId (Integer CampaignId) {
		this.CampaignId = CampaignId;
	}
	
	public void setName (String Name) {
		this.Name = Name;
	}
	
	public void setStart (Timestamp Start) {
		this.Start = Start;
	}
	
	public void setEnd (Timestamp End) {
		this.End = End;
	}
	
	public void setPublic (String Public) {
		this.Public = Public;
	}
	public void setDatasetId(Integer datasetId) {
		DatasetId = datasetId;
	}

	public void setDatasetName(String datasetName) {
		DatasetName = datasetName;
	}

	public void setTeams(List<Team> teams) {
		Teams = teams;
	}
}
