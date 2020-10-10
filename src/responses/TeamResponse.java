package responses;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import objects.Team;
import objects.User;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import com.google.gson.*;

import Utilities.Util;

@Path("/teams")
public class TeamResponse {


	public String executeQuery(String query, String type) throws SQLException{
		   List<Team> teamList = new ArrayList<Team>();
		   ResultSet rs = null;
		   Connection conn = null;
		   Statement stmt = null;
	       try (InputStream input = new FileInputStream("/home/enrich/tomcat/apache-tomcat-9.0.13/webapps/dev_tp-api/WEB-INF/config.properties")) {

	            Properties prop = new Properties();

	            // load a properties file
	            prop.load(input);

	            // get the property value and print it out
	            final String DB_URL = prop.getProperty("DB_URL");
	            final String USER = prop.getProperty("USER");
	            final String PASS = prop.getProperty("PASS");
		   // Register JDBC driver
		   try {
			Class.forName("com.mysql.jdbc.Driver");
		
		   // Open a connection
		   conn = DriverManager.getConnection(DB_URL, USER, PASS);
		   // Execute SQL query
		   stmt = conn.createStatement();
		   if (type != "Select") {
			   int success = stmt.executeUpdate(query);
			   if (success > 0) {
				   stmt.close();
				   conn.close();
				   return type +" succesful";
			   }
			   else {
				   stmt.close();
				   conn.close();
				   return type +" could not be executed";
			   }
		   }
		   rs = stmt.executeQuery(query);
		   
		   // Extract data from result set
		   while(rs.next()){
			  // Add Users
			  List<User> UserList = new ArrayList<User>();
			  if (Util.hasColumn(rs, "UserId") && rs.getString("UserId") != null) {
				  String[] UserIds = rs.getString("UserId").split(",");
				  String[] WP_UserIds = rs.getString("WP_UserId").split(",");
				  String[] Roles = rs.getString("Role").split(",");
				  String[] WP_Roles = rs.getString("WP_Role").split(",");
				  String[] Locations = rs.getString("Locations").split(",");
				  String[] Characters = rs.getString("Characters").split(",");
				  String[] Enrichments = rs.getString("Enrichments").split(",");
				  String[] Miles = rs.getString("Miles").split(",");
				  for (int i = 0; i < UserIds.length; i++) {
					  User user = new User();
					  user.setUserId(Integer.parseInt(UserIds[i]));
					  user.setWP_UserId(Integer.parseInt(WP_UserIds[i]));
					  user.setRole(Roles[i]);
					  user.setWP_Role(WP_Roles[i]);
					  user.setLocations(Integer.parseInt(Locations[i]));
					  user.setCharacters(Integer.parseInt(Characters[i]));
					  user.setEnrichments(Integer.parseInt(Enrichments[i]));
					  user.setMiles(Double.parseDouble(Miles[i]));
					  UserList.add(user);
				  }
			  }
				  
		      //Retrieve by column name
			  Team team = new Team();
			  team.setUsers(UserList);
			  team.setTeamId(rs.getInt("teamId"));
			  team.setName(rs.getString("Name"));
			  team.setShortName(rs.getString("ShortName"));
			  team.setCode(rs.getString("Code"));
			  team.setDescription(rs.getString("Description"));
			  if (Util.hasColumn(rs, "ItemCount")) {
				  team.setItemCount(rs.getInt("ItemCount"));
			  }
			  teamList.add(team);
		   }
		
		   // Clean-up environment
		   rs.close();
		   stmt.close();
		   conn.close();
		   } catch(SQLException se) {
		       //Handle errors for JDBC
			   se.printStackTrace();
		   } catch (ClassNotFoundException e) {
			   e.printStackTrace();
		}  finally {
		    try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { conn.close(); } catch (Exception e) { /* ignored */ }
	   }
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}  finally {
			    try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { conn.close(); } catch (Exception e) { /* ignored */ }
		   }
	    Gson gsonBuilder = new GsonBuilder().create();
	    String result = gsonBuilder.toJson(teamList);
	    return result;
	}

	//Search using custom filters

	@Produces("application/json;charset=utf-8")
	@GET
	public Response search(@Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT * FROM " + 
				"( " +
				"SELECT \r\n" + 
				"    t.TeamId AS TeamId,\r\n" + 
				"    t.Name AS Name,\r\n" + 
				"    t.ShortName AS ShortName,\r\n" + 
				"    t.Description AS Description,\r\n" + 
				"    t.Code AS Code,\r\n" + 
				"    UserId AS UserId,\r\n" + 
				"    WP_UserId AS WP_UserId,\r\n" + 
				"    Role AS Role,\r\n" + 
				"    WP_Role AS WP_Role,\r\n" + 
				"    Locations AS Locations,\r\n" + 
				"    Characters AS Characters,\r\n" + 
				"    Enrichments AS Enrichments,\r\n" + 
				"    Miles AS Miles " + 
				"FROM\r\n" + 
				"    Team t\r\n" + 
				"        LEFT JOIN\r\n" + 
				"	(\r\n" + 
				"		SELECT \r\n" + 
				"			tu.TeamId,\r\n" + 
				"			group_concat(u.UserId) as UserId, \r\n" + 
				"			group_concat(u.WP_UserId) as WP_UserId, \r\n" + 
				"			group_concat(r.Name) as Role, \r\n" + 
				"			group_concat(u.WP_Role) as WP_Role, \r\n" + 
				"            group_concat(IFNULL((SELECT sum(Amount) FROM Score WHERE UserId = u.UserId AND ScoreTypeId = 1), 0)) as Locations,\r\n" + 
				"            group_concat(IFNULL((SELECT sum(Amount) FROM Score WHERE UserId = u.UserId AND ScoreTypeId = 2), 0)) as Characters,\r\n" + 
				"            group_concat(IFNULL((SELECT sum(Amount) FROM Score WHERE UserId = u.UserId AND (ScoreTypeId = 3 OR ScoreTypeId = 4)), 0)) as Enrichments,\r\n" + 
				"            group_concat(IFNULL(ROUND((SELECT sum(Amount * Rate) FROM Score s JOIN ScoreType st ON s.ScoreTypeId = st.ScoreTypeId WHERE UserId = u.UserId), 2), 0)) as Miles" + 
				"		FROM TeamUser tu \r\n" + 
				"			JOIN\r\n" + 
				"		User u ON tu.UserId = u.UserId\r\n" + 
				"			JOIN\r\n" + 
				"		Role r ON u.RoleId = r.RoleId\r\n" + 
				"        GROUP BY tu.TeamId\r\n" + 
				"	) u ON t.TeamId = u.TeamId\r\n " +
				") a " + 
				"WHERE\r\n" + 
				"    1";
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		
		for(String key : queryParams.keySet()){
			String[] values = queryParams.getFirst(key).split(",");
			query += " AND (";
		    int valueCount = values.length;
		    int i = 1;
		    for(String value : values) {
		    	if (key.equals("UserId") || key.equals("WP_UserId")) {
			    	query += " FIND_IN_SET('" + value + "', " + key + ")";
		    	}
		    	else {
			    	query += key + " = " + value;
		    	}
			    if (i < valueCount) {
			    	query += " OR ";
			    }
			    i++;
		    }
		    query += ")";
		}
		query += " ORDER BY TeamId DESC";
		String resource = executeQuery(query, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}
	

	//Add new entry
	
	@POST
	public Response add(String body, @Context UriInfo uriInfo) throws SQLException {	
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    Team team = gson.fromJson(body, Team.class);
	    
	    //Check if all mandatory fields are included
	    if (team.Name != null && team.ShortName != null) {
			String query = "INSERT INTO Team (Name, ShortName, Code, Description) "
							+ "VALUES ('" + team.Name + "'"
							+ ", '" + team.ShortName + "'"
							+ ", '" + team.Code + "'"
							+ ", '" + team.Description + "')";
			String resource = executeQuery(query, "Insert");
			
			MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
			if (queryParams.containsKey("UserId")) {
				String userQuery = "INSERT INTO TeamUser (TeamId, UserId) "
								+ "VALUES ("
									+ "(SELECT TeamId FROM Team WHERE Name = '" + team.Name + "')"
									+ ", (SELECT UserId FROM User WHERE WP_UserId = " + queryParams.getFirst("UserId") + "))";
				String userQueryResource = executeQuery(userQuery, "Insert");
				//ResponseBuilder rBuild = Response.ok(userQueryResource);
				ResponseBuilder rBuild = Response.ok(userQuery);
		        return rBuild.build();
			}

			ResponseBuilder rBuild = Response.ok(resource);
	        return rBuild.build();
	    } else {
			ResponseBuilder rBuild = Response.status(Response.Status.BAD_REQUEST);
	        return rBuild.build();
	    }
	}
	

	//Edit entry by id
	@Path("/{id}")
	@POST
	public Response update(@PathParam("id") int id, String body) throws SQLException {
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    Team changes = gson.fromJson(body, Team.class);
	    
	    
	    //Check if NOT NULL field is attempted to be changed to NULL
	    String query = "UPDATE Team "
	    				+ "SET Name = '" + changes.Name + "', "
   	    				 + "ShortName = '" + changes.ShortName + "', "
 	    				 + "Description = '" + changes.Description + "', "
	    				 + "Code = '" + changes.Code + "' ";
		query += " WHERE TeamId = " + id;
		String resource = executeQuery(query, "Update");
		ResponseBuilder rBuild = Response.ok(resource);
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}
	

	//Delete entry by id
	@Path("/{id}")
	@DELETE
	public String delete(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("DELETE FROM Team WHERE TeamId = " + id, "Delete");
		return resource;
	}
	

	//Get entry by id
	@Path("/{id}")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getEntry(@PathParam("id") int id, @Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT * FROM " + 
				"( " +
				"SELECT \r\n" + 
				"    t.TeamId AS TeamId,\r\n" + 
				"    t.Name AS Name,\r\n" + 
				"    t.ShortName AS ShortName,\r\n" + 
				"    t.Description AS Description,\r\n" + 
				"    t.Code AS Code,\r\n" + 
				"    UserId AS UserId,\r\n" + 
				"    WP_UserId AS WP_UserId,\r\n" + 
				"    Role AS Role,\r\n" + 
				"    WP_Role AS WP_Role,\r\n" + 
				"    Locations AS Locations,\r\n" + 
				"    Characters AS Characters,\r\n" + 
				"    Enrichments AS Enrichments,\r\n" + 
				"    Miles AS Miles, " + 
				"	 (SELECT count(*) FROM (SELECT DISTINCT(ItemId) FROM Score WHERE UserId IN (SELECT UserId FROM TeamUser WHERE TeamId = " + id + ")) a) as ItemCount " + 
				"FROM\r\n" + 
				"    Team t\r\n" + 
				"        LEFT JOIN\r\n" + 
				"	(\r\n" + 
				"		SELECT \r\n" + 
				"			tu.TeamId,\r\n" + 
				"			group_concat(u.UserId) as UserId, \r\n" + 
				"			group_concat(u.WP_UserId) as WP_UserId, \r\n" + 
				"			group_concat(r.Name) as Role, \r\n" + 
				"			group_concat(u.WP_Role) as WP_Role, \r\n" + 
				"            group_concat(IFNULL((SELECT sum(Amount) FROM Score WHERE UserId = u.UserId AND ScoreTypeId = 1), 0)) as Locations,\r\n" + 
				"            group_concat(IFNULL((SELECT sum(Amount) FROM Score WHERE UserId = u.UserId AND ScoreTypeId = 2), 0)) as Characters,\r\n" + 
				"            group_concat(IFNULL((SELECT sum(Amount) FROM Score WHERE UserId = u.UserId AND (ScoreTypeId = 3 OR ScoreTypeId = 4)), 0)) as Enrichments,\r\n" + 
				"            group_concat(IFNULL(ROUND((SELECT sum(Amount * Rate) FROM Score s JOIN ScoreType st ON s.ScoreTypeId = st.ScoreTypeId WHERE UserId = u.UserId), 2), 0)) as Miles " +
				"		FROM TeamUser tu \r\n" + 
				"			JOIN\r\n" + 
				"		User u ON tu.UserId = u.UserId\r\n" + 
				"			JOIN\r\n" + 
				"		Role r ON u.RoleId = r.RoleId\r\n" + 
				"        GROUP BY tu.TeamId\r\n" + 
				"	) u ON t.TeamId = u.TeamId\r\n " +
				") a " + 
				"WHERE\r\n" + 
				"    TeamId = " + id;
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		
		for(String key : queryParams.keySet()){
			String[] values = queryParams.getFirst(key).split(",");
			query += " AND (";
		    int valueCount = values.length;
		    int i = 1;
		    for(String value : values) {
		    	if (key.equals("UserId") || key.equals("WP_UserId")) {
			    	query += " FIND_IN_SET('" + value + "', " + key + ")";
		    	}
		    	else {
			    	query += key + " = " + value;
		    	}
			    if (i < valueCount) {
			    	query += " OR ";
			    }
			    i++;
		    }
		    query += ")";
		}
		query += " ORDER BY TeamId DESC";
		String resource = executeQuery(query, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}

}


