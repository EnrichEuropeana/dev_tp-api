package objects;

import java.sql.Timestamp;

public class PrivateMessage {
	public Integer PrivateMessageId;
	public Integer SenderId;
	public Integer ReceiverId;
	public Timestamp Timestamp; 
	public String Text; 
	
	public void setPrivateMessageId (Integer PrivateMessageId) {
		this.PrivateMessageId = PrivateMessageId;
	}
	
	public void setSenderId (Integer SenderId) {
		this.SenderId = SenderId;
	}
	
	public void setReceiverId (Integer ReceiverId) {
		this.ReceiverId = ReceiverId;
	}
	
	public void setTimestamp (Timestamp Timestamp) {
		this.Timestamp = Timestamp;
	}
	
	public void setText (String Text) {
		this.Text = Text;
	}
}
