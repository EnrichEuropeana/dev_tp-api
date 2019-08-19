package objects;

public class PropertyType {
	public Integer PropertyTypeId;
	public String Name;
	public String MotivationId;
	public String Editable;
	
	public void setPropertyTypeId(Integer propertyTypeId) {
		PropertyTypeId = propertyTypeId;
	}
	public void setName(String name) {
		Name = name;
	}
	public void setMotivationId(String motivationId) {
		MotivationId = motivationId;
	}
	public void setEditable(String editable) {
		Editable = editable;
	}
}
