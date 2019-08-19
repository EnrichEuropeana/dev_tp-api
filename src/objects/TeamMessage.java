package objects;

import java.sql.Timestamp;

public class TeamMessage {
	public Integer TeamMessageId;
	public Integer SenderId;
	public Integer TeamId;
	public Timestamp Timestamp; 
	public String Text; 
	
	public void setTeamMessageId (Integer TeamMessageId) {
		this.TeamMessageId = TeamMessageId;
	}
	
	public void setSenderId (Integer SenderId) {
		this.SenderId = SenderId;
	}
	
	public void setTeamId (Integer TeamId) {
		this.TeamId = TeamId;
	}
	
	public void setTimestamp (Timestamp Timestamp) {
		this.Timestamp = Timestamp;
	}
	
	public void setText (String Text) {
		this.Text = Text;
	}
}
