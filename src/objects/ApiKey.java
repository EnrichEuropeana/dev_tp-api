package objects;

public class ApiKey {
	public Integer ApiKeyId;
	public String KeyString;
	public Integer ProjectId;
	public Integer RoleId;

	public void setApiKeyId (Integer ApiKeyId) {
		this.ApiKeyId = ApiKeyId;
	}
	
	public void setKeyString (String KeyString) {
		this.KeyString = KeyString;
	}
	
	public void setProjectId (Integer ProjectId) {
		this.ProjectId = ProjectId;
	}
	
	public void setRoleId (Integer RoleId) {
		this.RoleId = RoleId;
	}
}
