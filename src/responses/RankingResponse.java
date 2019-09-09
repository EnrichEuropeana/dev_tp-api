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

import objects.Ranking;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import com.google.gson.*;

@Path("/rankings")
public class RankingResponse {


	public String executeQuery(String query, String type) throws SQLException{
		   List<Ranking> rankingList = new ArrayList<Ranking>();
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
			   if (type == "UserCount") {
				   ResultSet rs = stmt.executeQuery(query);
				   rs.next();
				   return rs.getString("UserCount");
			   }
			   int success = stmt.executeUpdate(query);
			   if (success > 0) {
				   stmt.close();
				   conn.close();
				   return type + " succesful";
			   }
			   else {
				   stmt.close();
				   conn.close();
				   return type + " could not be executed";
			   }
		   }
		   ResultSet rs = stmt.executeQuery(query);
		   
		   // Extract data from result set
		   while(rs.next()){
		      //Retrieve by column name
			  Ranking ranking = new Ranking();
			  ranking.setUserId(rs.getInt("UserId"));
			  ranking.setTeamId(rs.getInt("TeamId"));
			  ranking.setTeamName(rs.getString("TeamName"));
			  ranking.setMiles(rs.getFloat("Miles"));
			  ranking.setMilesPerPerson(rs.getFloat("MilesPerPerson"));
			  ranking.setLocations(rs.getInt("Locations"));
			  ranking.setTranscriptionCharacters(rs.getInt("TranscriptionCharacters"));
			  ranking.setDescriptionCharacters(rs.getInt("DescriptionCharacters"));
			  ranking.setEnrichments(rs.getInt("Enrichments"));
			  rankingList.add(ranking);
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
	    String result = gsonBuilder.toJson(rankingList);
	    return result;
	}

	//Search using custom filters
	@Path("")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response search(@Context UriInfo uriInfo) throws SQLException {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String query = "SELECT \r\n" + 
						"	u.WP_UserId as UserId, \r\n" + 
						"	0 as TeamId, \r\n" + 
					    " 	0 as TeamName, \r\n" +
						"    SUM(s.Miles) as Miles,\r\n" + 
						"    0 as MilesPerPerson,\r\n" + 
						"    SUM(s.Locations) Locations,\r\n" + 
						"    SUM(s.TranscriptionCharacters) TranscriptionCharacters,\r\n" + 
						"    SUM(s.DescriptionCharacters) DescriptionCharacters,\r\n" + 
						"    SUM(s.Enrichments) Enrichments\r\n" + 
						"FROM \r\n" + 
						"(\r\n" + 
						"	SELECT \r\n" + 
						"		s.UserId as UserId, \r\n" + 
						"		s.Amount * st.Rate as Miles,\r\n" + 
						"		CASE WHEN st.Name = \"Location\" THEN Amount ELSE 0 END Locations,\r\n" + 
						"		CASE WHEN st.Name = \"Transcription\" THEN (Amount - 10) ELSE 0 END TranscriptionCharacters,\r\n" + 
						"		CASE WHEN st.Name = \"Description\" THEN (Amount - 10) ELSE 0 END DescriptionCharacters,\r\n" + 
						"		CASE WHEN st.Name = \"Enrichment\" THEN Amount ELSE 0 END Enrichments,\r\n" + 
						"        s.Timestamp as Timestamp\r\n" + 
						"	From Score s\r\n" + 
						"	JOIN ScoreType st On s.ScoreTypeId = st.ScoreTypeId\r\n";
		if (queryParams.containsKey("campaign")) {
			query +=  " WHERE s.Timestamp >= (SELECT Start FROM Campaign WHERE CampaignId = " + queryParams.getFirst("campaign") + ") "
					+ " AND s.Timestamp <= (SELECT End FROM Campaign WHERE CampaignId = " + queryParams.getFirst("campaign") + ") ";
		}
		query +=		") s " + 
						"JOIN User u ON s.UserId = u.UserId  \r\n" + 
						"GROUP BY UserId\r\n" + 
						"ORDER BY Miles DESC ";
		for(String key : queryParams.keySet()){
			if (key.equals("limit") || key.equals("offset") || key.equals("campaign")) {
				continue;
			}
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
				
		if (queryParams.getFirst("limit") != null) {
			query += "LIMIT " + queryParams.getFirst("limit") + " ";
		}
		if (queryParams.getFirst("offset") != null) {
			query += "OFFSET " + queryParams.getFirst("offset") + " ";
		}
		String resource = executeQuery(query, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}

	//Search using custom filters
	@Path("/userCount")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getUserCount(@Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT count(DISTINCT(UserId)) as UserCount FROM Score";
		String resource = executeQuery(query, "UserCount");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}

	//Search using custom filters
	@Path("/teams")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response teams(@Context UriInfo uriInfo) throws SQLException {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String query = "SELECT \r\n" + 
						"	0 as UserId, \r\n" + 
						"	s.TeamId as TeamId, \r\n" + 
					    " 	s.TeamName as TeamName, \r\n" +
						"    SUM(s.Miles) as Miles,\r\n" + 
						"    SUM(s.Miles) / (SELECT COUNT(*) FROM TeamUser WHERE TeamId = s.TeamId) as MilesPerPerson,\r\n" + 
						"    SUM(s.Locations) Locations,\r\n" + 
						"    SUM(s.TranscriptionCharacters) TranscriptionCharacters,\r\n" + 
						"    SUM(s.DescriptionCharacters) DescriptionCharacters,\r\n" + 
						"    SUM(s.Enrichments) Enrichments\r\n" + 
						"FROM \r\n" + 
						"(\r\n" + 
						"	SELECT \r\n" + 
						"		tc.TeamId as TeamId, \r\n" + 
						"		t.Name as TeamName, \r\n" + 
						"        u.UserId as UserId,\r\n" + 
						"		s.Amount * st.Rate as Miles,\r\n" + 
						"		CASE WHEN st.Name = \"Location\" THEN Amount ELSE 0 END Locations,\r\n" + 
						"		CASE WHEN st.Name = \"Transcription\" THEN (Amount - 10) ELSE 0 END TranscriptionCharacters,\r\n" + 
						"		CASE WHEN st.Name = \"Description\" THEN (Amount - 10) ELSE 0 END DescriptionCharacters,\r\n" + 
						"		CASE WHEN st.Name = \"Enrichment\" THEN Amount ELSE 0 END Enrichments,\r\n" + 
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
		}
		query +=		" ) s \r\n" + 
						"GROUP BY TeamId\r\n" + 
						"ORDER BY Miles DESC ";
		for(String key : queryParams.keySet()){
			if (key.equals("limit") || key.equals("offset") || key.equals("campaign")) {
				continue;
			}
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
				
		if (queryParams.getFirst("limit") != null) {
			query += "LIMIT " + queryParams.getFirst("limit") + " ";
		}
		if (queryParams.getFirst("offset") != null) {
			query += "OFFSET " + queryParams.getFirst("offset") + " ";
		}
		String resource = executeQuery(query, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}

	//Search using custom filters
	@Path("/teamCount")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getTeamCount(@Context UriInfo uriInfo) throws SQLException {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String query = "SELECT \r\n" + 
						"	COUNT(DISTINCT(s.TeamId)) as UserCount " + 
						"FROM \r\n" + 
						"(\r\n" + 
						"	SELECT \r\n" + 
						"		tc.TeamId as TeamId, \r\n" + 
						"		t.Name as TeamName, \r\n" + 
						"        u.UserId as UserId,\r\n" + 
						"		s.Amount * st.Rate as Miles,\r\n" + 
						"		CASE WHEN st.Name = \"Location\" THEN Amount ELSE 0 END Locations,\r\n" + 
						"		CASE WHEN st.Name = \"Transcription\" THEN (Amount - 10) ELSE 0 END TranscriptionCharacters,\r\n" + 
						"		CASE WHEN st.Name = \"Description\" THEN (Amount - 10) ELSE 0 END DescriptionCharacters,\r\n" + 
						"		CASE WHEN st.Name = \"Enrichment\" THEN Amount ELSE 0 END Enrichments,\r\n" + 
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
		}
		query +=		" ) s \r\n";
		for(String key : queryParams.keySet()){
			if (key.equals("limit") || key.equals("offset") || key.equals("campaign")) {
				continue;
			}
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
		String resource = executeQuery(query, "UserCount");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}

}


