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

import objects.Place;
import objects.Property;
import objects.Team;
import objects.User;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import com.google.gson.*;

@Path("/teams")
public class TeamResponse {


	public String executeQuery(String query, String type) throws SQLException{
		   List<Team> teamList = new ArrayList<Team>();
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
		   Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		   // Execute SQL query
		   Statement stmt = conn.createStatement();
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
		   ResultSet rs = stmt.executeQuery(query);
		   
		   // Extract data from result set
		   while(rs.next()){
			  // Add Users
			  List<User> UserList = new ArrayList<User>();
			  if (rs.getString("UserId") != null) {
				  String[] UserIds = rs.getString("UserId").split(",");
				  String[] WP_UserIds = rs.getString("WP_UserId").split(",");
				  String[] Roles = rs.getString("Role").split(",");
				  String[] WP_Roles = rs.getString("WP_Role").split(",");
				  for (int i = 0; i < UserIds.length; i++) {
					  User user = new User();
					  user.setUserId(Integer.parseInt(UserIds[i]));
					  user.setWP_UserId(Integer.parseInt(WP_UserIds[i]));
					  user.setRole(Roles[i]);
					  user.setWP_Role(WP_Roles[i]);
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
		}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	    Gson gsonBuilder = new GsonBuilder().create();
	    String result = gsonBuilder.toJson(teamList);
	    return result;
	}

	//Search using custom filters
	@Path("")
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
				"    WP_Role AS WP_Role\r\n" + 
				"FROM\r\n" + 
				"    Team t\r\n" + 
				"        LEFT JOIN\r\n" + 
				"	(\r\n" + 
				"		SELECT \r\n" + 
				"			tu.TeamId,\r\n" + 
				"			group_concat(u.UserId) as UserId, \r\n" + 
				"			group_concat(u.WP_UserId) as WP_UserId, \r\n" + 
				"			group_concat(r.Name) as Role, \r\n" + 
				"			group_concat(u.WP_Role) as WP_Role\r\n" + 
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
		    	query += key + " LIKE " + "'%" + value + "%'";
			    if (i < valueCount) {
			    	query += " OR ";
			    }
			    i++;
		    }
		    query += ")";
		}
		String resource = executeQuery(query, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}
	

	//Add new entry
	@Path("")
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
	public Response getEntry(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("SELECT * FROM Team WHERE TeamId = " + id, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}

}


