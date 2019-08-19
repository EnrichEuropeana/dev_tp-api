package objects;

public class CompletionStatus {
	public Integer CompletionStatusId;
	public String Name; 
	public String ColorCode; 
	public String ColorCodeGradient; 
	
	public void setCompletionStatusId (Integer CompletionStatusId) {
		this.CompletionStatusId = CompletionStatusId;
	}
	
	public void setName (String Name) {
		this.Name = Name;
	}
	public void setColorCode(String colorCode) {
		ColorCode = colorCode;
	}

	public void setColorCodeGradient(String colorCodeGradient) {
		ColorCodeGradient = colorCodeGradient;
	}
}
