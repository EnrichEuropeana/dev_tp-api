package objects;

public class ItemProperty {
	public Integer ItemPropertyId;
	public Integer ItemId; 
	public Integer PropertyId; 
	public String UserGenerated;
	public Integer EditedVersion; 
	public Integer Original; 

	public void setItemPropertyId(Integer itemPropertyId) {
		ItemPropertyId = itemPropertyId;
	}
	public void setItemId (Integer ItemId) {
		this.ItemId = ItemId;
	}
	
	public void setPropertyId (Integer PropertyId) {
		this.PropertyId = PropertyId;
	}
	
	public void setUserGenerated (String UserGenerated) {
		this.UserGenerated = UserGenerated;
	}
	
	public void setEditedVersion(Integer EditedVersion) {
		this.EditedVersion = EditedVersion;
	}

	public void setOriginal(Integer original) {
		this.Original = original;
	}
}
