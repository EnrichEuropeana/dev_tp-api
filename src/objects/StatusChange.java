package objects;

import java.sql.Timestamp;

public class StatusChange {
	public Integer StatusChangeId; 
	public Integer UserId; 
	public Integer ItemId; 
	public Integer OldStatus; 
	public Integer NewStatus; 
	public Timestamp Timestamp;
	
	public void setStatusChangeId (Integer StatusChangeId) {
		this.StatusChangeId = StatusChangeId;
	}
	
	public void setUserId (Integer UserId) {
		this.UserId = UserId;
	}
	
	public void setItemId (Integer ItemId) {
		this.ItemId = ItemId;
	}

	public void setOldStatus (Integer OldStatus) {
		this.OldStatus = OldStatus;
	}

	public void setNewStatus (Integer NewStatus) {
		this.NewStatus = NewStatus;
	}
	
	public void setTimestamp (Timestamp Timestamp) {
		this.Timestamp = Timestamp;
	}

}
