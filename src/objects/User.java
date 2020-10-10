package objects;

import java.sql.Timestamp;

public class User {
	public Integer UserId; 
	public Integer WP_UserId; 
	public String WP_Role; 
	public Integer RoleId; 
	public String Role; 
	public Timestamp Timestamp;
	public String Token;
	public Double Miles;
	public Integer Locations;
	public Integer Characters;
	public Integer Enrichments;

	public void setUserId(Integer userId) {
		UserId = userId;
	}
	public void setWP_UserId(Integer wP_UserId) {
		WP_UserId = wP_UserId;
	}
	public void setWP_Role(String wP_Role) {
		WP_Role = wP_Role;
	}
	public void setRoleId(Integer roleId) {
		RoleId = roleId;
	}
	public void setRole(String role) {
		Role = role;
	}
	public void setTimestamp(Timestamp timestamp) {
		Timestamp = timestamp;
	}
	public void setToken(String token) {
		Token = token;
	}
	public void setMiles(Double miles) {
		Miles = miles;
	}
	public void setLocations(Integer locations) {
		Locations = locations;
	}
	public void setCharacters(Integer characters) {
		Characters = characters;
	}
	public void setEnrichments(Integer enrichments) {
		Enrichments = enrichments;
	} 
	
	
}
