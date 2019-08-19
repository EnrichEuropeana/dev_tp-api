package objects;

import java.sql.Timestamp;

public class ItemVisit {
	public Integer ItemVisitId;
	public Integer UserId; 
	public Integer ItemId; 
	public Timestamp Timestamp;
	
	public void setItemVisitId (Integer ItemVisitId) {
		this.ItemVisitId = ItemVisitId;
	}
	
	public void setUserId (Integer UserId) {
		this.UserId = UserId;
	}
	
	public void setItemId (Integer ItemId) {
		this.ItemId = ItemId;
	}
	
	public void setTimestamp (Timestamp Timestamp) {
		this.Timestamp = Timestamp;
	}

}
