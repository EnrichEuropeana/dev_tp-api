package objects;

public class Property {
	public Integer PropertyId;
	public String PropertyValue;
	public String PropertyDescription;
	public Integer PropertyTypeId;
	public String PropertyType;
	public Integer MotivationId;
	public String Motivation;
	public String Editable;
	
	public void setPropertyId(Integer propertyId) {
		PropertyId = propertyId;
	}
	public void setPropertyValue(String propertyValue) {
		PropertyValue = propertyValue;
	}
	public void setPropertyDescription(String propertyDescription) {
		PropertyDescription = propertyDescription;
	}
	public void setPropertyTypeId(Integer propertyTypeId) {
		PropertyTypeId = propertyTypeId;
	}
	public void setPropertyType(String propertyType) {
		PropertyType = propertyType;
	}
	public void setMotivationId(Integer motivationId) {
		MotivationId = motivationId;
	}
	public void setMotivation(String motivation) {
		Motivation = motivation;
	}
	public void setEditable(String editable) {
		Editable = editable;
	}
}
