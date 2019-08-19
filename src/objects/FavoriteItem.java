package objects;

import java.sql.Timestamp;

public class FavoriteItem {
	public Integer UserId; 
	public Integer ItemId; 
	public String Note;
	public Timestamp Timestamp;
	
	public void setUserId (Integer UserId) {
		this.UserId = UserId;
	}
	
	public void setItemId (Integer ItemId) {
		this.ItemId = ItemId;
	}
	
	public void setNote (String Note) {
		this.Note = Note;
	}
	
	public void setTimestamp (Timestamp Timestamp) {
		this.Timestamp = Timestamp;
	}

}
