package objects;

import java.sql.Timestamp;

public class Campaign {
	public Integer CampaignId;
	public String Name; 
	public Timestamp Start; 
	public Timestamp End; 
	public String Public; 

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
}
