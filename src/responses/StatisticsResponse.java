package responses;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

@Path("/statistics")
public class StatisticsResponse {


	public String executeNumberQuery(String query, String type) throws SQLException{
		   ResultSet rs = null;
		   Connection conn = null;
		   Statement stmt = null;
	       try (InputStream input = new FileInputStream("/home/enrich/tomcat/apache-tomcat-9.0.13/webapps/dev_tp-api/WEB-INF/config.properties")) {

				Properties prop = new Properties();
				
				// Load a properties file
				prop.load(input);
				
				// Save property values
				final String DB_URL = prop.getProperty("DB_URL");
				final String USER = prop.getProperty("USER");
				final String PASS = prop.getProperty("PASS");
				
				// Register JDBC driver
				Class.forName("com.mysql.jdbc.Driver");
				
				// Open a connection
			    conn = DriverManager.getConnection(DB_URL, USER, PASS);
			    // Execute SQL query
			    stmt = conn.createStatement();
			    try {
				    rs = stmt.executeQuery(query);
				    if(rs.next() == false){
					    rs.close();
		 			    stmt.close();
		 			    conn.close();
					    return "0";
				    }
				    else {
				    	String result = rs.getString("Amount");
					    rs.close();
		 			    stmt.close();
		 			    conn.close();
					    return result;
				    }
				
				} catch(SQLException se) {
				    se.printStackTrace();
				    return "";
				}  finally {
				    try { rs.close(); } catch (Exception e) { /* ignored */ }
				    try { stmt.close(); } catch (Exception e) { /* ignored */ }
				    try { conn.close(); } catch (Exception e) { /* ignored */ }
			   }
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}  finally {
			    try { rs.close(); } catch (Exception e) { /* ignored */ }
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { conn.close(); } catch (Exception e) { /* ignored */ }
		   }
	    // Build Json from query results
	    return "0";
	}

	// Total transcribed characters
	@Path("/characters")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response characters(@Context UriInfo uriInfo) throws SQLException {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String query = "SELECT SUM(Amount) as Amount FROM Score WHERE ScoreTypeId = 2 ";
		if (queryParams.containsKey("dateStart")) {
			query +=  " AND Timestamp >= '" + queryParams.getFirst("dateStart") + "' ";
		}
		if (queryParams.containsKey("dateEnd")) {
			query +=  " AND Timestamp <= '" + queryParams.getFirst("dateEnd") + "' ";
		}
		String result = executeNumberQuery(query, "Select");

		ResponseBuilder rBuild = Response.ok(result);
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}

	// Total enrichments
	@Path("/enrichments")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response enrichments(@Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT SUM(Amount) as Amount FROM Score WHERE ScoreTypeId = 1 OR ScoreTypeId = 3";
		String result = executeNumberQuery(query, "Select");

		ResponseBuilder rBuild = Response.ok(result);
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}
	
	// Total items
	@Path("/items")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response items(@Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT count(*) as Amount FROM Item";
		String result = executeNumberQuery(query, "Select");

		ResponseBuilder rBuild = Response.ok(result);
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}
	
	// Total items
	@Path("/itemsStarted")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response itemsStarted(@Context UriInfo uriInfo) throws SQLException {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String query = "SELECT count(DISTINCT(i.ItemId)) as Amount "
				+ "FROM Item i "
				+ "JOIN Score sc ON i.ItemId = sc.ItemId "
				+ "JOIN Story s ON i.StoryId = s.StoryId "
				+ "WHERE s.CompletionStatusId != 1";

		if (queryParams.containsKey("campaign")) {
			query +=  " AND sc.Timestamp >= (SELECT Start FROM Campaign WHERE CampaignId = " + queryParams.getFirst("campaign") + ")";
			query +=  " AND sc.Timestamp <= (SELECT End FROM Campaign WHERE CampaignId = " + queryParams.getFirst("campaign") + ")";
			query +=  " AND (s.DatasetId = (SELECT DatasetId FROM Campaign WHERE CampaignId = " + queryParams.getFirst("campaign") + ")"
					+ " OR (SELECT DatasetId FROM Campaign WHERE CampaignId = " + queryParams.getFirst("campaign") + ") is null)";
		}
		else if (queryParams.containsKey("dataset")) {
			query +=  " AND (s.DatasetId = " + queryParams.getFirst("dataset") + ")";
		}
		String result = executeNumberQuery(query, "Select");

		ResponseBuilder rBuild = Response.ok(result);
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}

	
	// Transcribed character amount by team
	@Path("/teamsCharacters")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response teamCharacters(@Context UriInfo uriInfo) throws SQLException {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		// Build base query
		String query = "SELECT \r\n" + 
				"	s.TeamId as TeamId, \r\n" + 
			    " 	s.TeamName as TeamName, \r\n" +
				"    SUM(s.TranscriptionCharacters) as Amount\r\n" + 
				"FROM \r\n" + 
				"(\r\n" + 
				"	SELECT \r\n" + 
				"		tc.TeamId as TeamId, \r\n" + 
				"		t.Name as TeamName, \r\n" + 
				"       u.UserId as UserId,\r\n" + 
				"		CASE WHEN st.Name = \"Transcription\" THEN Amount ELSE 0 END TranscriptionCharacters,\r\n" + 
				"        s.Timestamp as Timestamp\r\n" + 
				"	From Score s\r\n" + 
				"	JOIN ScoreType st On s.ScoreTypeId = st.ScoreTypeId\r\n" + 
				"	JOIN User u ON s.UserId = u.UserId  \r\n" + 
				"	JOIN TeamUser tu ON tu.UserId = u.UserId  \r\n" + 
				"	JOIN Team t ON t.TeamId = tu.TeamId \r\n" + 
				"	JOIN TeamCampaign tc ON tu.TeamId = tc.TeamId " + 
				"	JOIN Campaign c ON c.CampaignId = tc.CampaignId ";
		if (queryParams.containsKey("campaign")) {
			query +=  " WHERE tc.CampaignId = " + queryParams.getFirst("campaign") + " AND s.Timestamp >= c.Start AND s.Timestamp <= c.End ";
			if (queryParams.containsKey("team")) {
				query +=  " AND t.TeamId = " + queryParams.getFirst("team") + " ";
			}
		}
		else {
			if (queryParams.containsKey("team")) {
				query +=  " WHERE t.TeamId = " + queryParams.getFirst("team") + " ";
			}
		}
		
		query +=		" ) s \r\n";
		String result = executeNumberQuery(query, "Select");

		ResponseBuilder rBuild = Response.ok(result);
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}
	
	
	// Transcribed character amount by person
	@Path("/personsCharacters")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response personCharacters(@Context UriInfo uriInfo) throws SQLException {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		// Build base query
		String query = "SELECT \r\n" + 
				"	s.WP_UserId as UserId, \r\n" + 
				"    SUM(s.TranscriptionCharacters) as Amount\r\n" + 
				"FROM \r\n" + 
				"(\r\n" + 
				"	SELECT \r\n" + 
				"       u.WP_UserId as WP_UserId,\r\n" + 
				"		CASE WHEN st.Name = \"Transcription\" THEN Amount ELSE 0 END TranscriptionCharacters,\r\n" + 
				"        s.Timestamp as Timestamp\r\n" + 
				"	From Score s\r\n" + 
				"	JOIN ScoreType st On s.ScoreTypeId = st.ScoreTypeId\r\n" + 
				"	JOIN User u ON s.UserId = u.UserId  \r\n" + 
				"	JOIN Item i ON s.ItemId = i.ItemId \r\n" + 
				"	JOIN Story story ON i.StoryId = story.StoryId  \r\n" + 
				"	JOIN Campaign c ON story.DatasetId = c.DatasetId ";
		if (queryParams.containsKey("campaign")) {
			query +=  " WHERE c.CampaignId = " + queryParams.getFirst("campaign") + " AND s.Timestamp >= c.Start AND s.Timestamp <= c.End ";
			if (queryParams.containsKey("person")) {
				query +=  " AND u.WP_UserId = " + queryParams.getFirst("person");
			}
		}
		else {
			if (queryParams.containsKey("person")) {
				query +=  " WHERE u.WP_UserId = " + queryParams.getFirst("person");
			}
		}
		
		query +=		" ) s \r\n";
		String result = executeNumberQuery(query, "Select");

		ResponseBuilder rBuild = Response.ok(result);
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}
}