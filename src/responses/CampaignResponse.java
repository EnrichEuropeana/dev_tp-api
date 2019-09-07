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

import objects.Campaign;
import objects.Team;
import objects.User;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import com.google.gson.*;

@Path("/campaigns")
public class CampaignResponse {


	public String executeQuery(String query, String type) throws SQLException{
		   List<Campaign> campaignList = new ArrayList<Campaign>();
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
				  // Add Teams
				  List<Team> TeamList = new ArrayList<Team>();
				  if (rs.getString("TeamId") != null) {
					  String[] TeamIds = rs.getString("TeamId").split(",");
					  String[] TeamNames = rs.getString("TeamName").split(",");
					  String[] TeamShortNames = rs.getString("TeamShortName").split(",");
					  for (int i = 0; i < TeamIds.length; i++) {
						  Team team = new Team();
						  team.setTeamId(Integer.parseInt(TeamIds[i]));
						  team.setName(TeamNames[i]);
						  team.setShortName(TeamShortNames[i]);
						  TeamList.add(team);
					  }
				  }
		      //Retrieve by column name
			  Campaign campaign = new Campaign();
			  campaign.setTeams(TeamList);
			  campaign.setCampaignId(rs.getInt("CampaignId"));
			  campaign.setName(rs.getString("Name"));
			  campaign.setStart(rs.getTimestamp("Start"));
			  campaign.setEnd(rs.getTimestamp("End"));
			  campaign.setPublic(rs.getString("Public"));
			  campaignList.add(campaign);
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
	    String result = gsonBuilder.toJson(campaignList);
	    return result;
	}

	//Search using custom filters
	@Path("")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response search(@Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT * FROM   \r\n" + 
				"				(  \r\n" + 
				"				SELECT  \r\n" + 
				"					c.CampaignId AS CampaignId,\r\n" + 
				"				    c.Name AS Name,  \r\n" + 
				"				    c.Start AS Start,  \r\n" + 
				"				    c.End AS End,  \r\n" + 
				"				    c.Public AS Public,  \r\n" + 
				"				    t.TeamId AS TeamId,  \r\n" + 
				"				    t.Name AS TeamName,  \r\n" + 
				"				    t.ShortName AS TeamShortName\r\n" + 
				"				FROM  \r\n" + 
				"				    Campaign c  \r\n" + 
				"				        LEFT JOIN  \r\n" + 
				"					(  \r\n" + 
				"						SELECT   \r\n" + 
				"							tc.CampaignId,  \r\n" + 
				"							group_concat(t.TeamId) as TeamId,   \r\n" + 
				"							group_concat(t.Name) as Name,   \r\n" + 
				"							group_concat(t.ShortName) as ShortName   \r\n" + 
				"						FROM TeamCampaign tc   \r\n" + 
				"							JOIN  \r\n" + 
				"						Team t ON tc.TeamId = t.TeamId  \r\n" + 
				"				        GROUP BY tc.CampaignId  \r\n" + 
				"					) t ON c.CampaignId = t.CampaignId  \r\n" + 
				"				) a   \r\n" + 
				"				WHERE  \r\n" + 
				"				    1";
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		
		for(String key : queryParams.keySet()){
			String[] values = queryParams.getFirst(key).split(",");
			query += " AND (";
		    int valueCount = values.length;
		    int i = 1;
		    for(String value : values) {
		    	query += key + " = '" + value + "'";
			    if (i < valueCount) {
			    	query += " OR ";
			    }
			    i++;
		    }
		    query += ")";
		}
		query += " ORDER BY CampaignId DESC";
		String resource = executeQuery(query, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}

	//Add new entry
	@Path("")
	@POST
	public Response add(String body) throws SQLException {	
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    Campaign campaign = gson.fromJson(body, Campaign.class);
	    
		String query = "INSERT INTO Campaign (Name, Start, End, Public) "
						+ "VALUES ('" + campaign.Name + "'"
							+ ", '" + campaign.Start + "'"
							+ ", '" + campaign.End + "'"
							+ ", " + campaign.Public + ")";
		String resource = executeQuery(query, "Insert");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}
	

	//Edit entry by id
	@Path("/{id}")
	@POST
	public Response update(@PathParam("id") int id, String body) throws SQLException {
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    Campaign changes = gson.fromJson(body, Campaign.class);
	    
	    
	    //Check if NOT NULL field is attempted to be changed to NULL
	    String query = "UPDATE Campaign "
	    				+ "SET Name = '" + changes.Name + "', "
   	    				 + "Start = '" + changes.Start + "', "
 	    				 + "End = '" + changes.End + "', "
	    				 + "Public = " + changes.Public;
		query += " WHERE CampaignId = " + id;
		String resource = executeQuery(query, "Update");
		ResponseBuilder rBuild = Response.ok(resource);
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}
	

	//Delete entry by id
	@Path("/{id}")
	@DELETE
	public String delete(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("DELETE FROM Campaign WHERE CampaignId = " + id, "Delete");
		return resource;
	}
	

	//Get entry by id
	@Path("/{id}")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getEntry(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("SELECT * FROM Campaign WHERE CampaignId = " + id, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}

}