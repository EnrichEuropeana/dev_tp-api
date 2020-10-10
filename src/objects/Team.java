package objects;

import java.util.List;

public class Team {
	public Integer TeamId;
	public String Name; 
	public String ShortName; 
	public String Code; 
	public String Description;
	public List<User> Users;
	public Integer ItemCount;
	
	public void setTeamId (Integer TeamId) {
		this.TeamId = TeamId;
	}
	
	public void setName (String Name) {
		this.Name = Name;
	}
	
	public void setShortName (String ShortName) {
		this.ShortName = ShortName;
	}
	
	public void setCode (String Code) {
		this.Code = Code;
	}
	
	public void setDescription (String Description) {
		this.Description = Description;
	}
	public void setUsers(List<User> users) {
		Users = users;
	}

	public void setItemCount(Integer itemCount) {
		ItemCount = itemCount;
	}
	
}
