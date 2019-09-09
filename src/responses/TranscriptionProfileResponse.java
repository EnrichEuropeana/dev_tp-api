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

import objects.TranscriptionProfile;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import com.google.gson.*;

@Path("/transcriptionProfile")
public class TranscriptionProfileResponse {


	public String executeQuery(String query, String type) throws SQLException{
		   List<TranscriptionProfile> transcriptionProfileList = new ArrayList<TranscriptionProfile>();
	       try (InputStream input = new FileInputStream("/home/enrich/tomcat/apache-tomcat-9.0.13/webapps/dev_tp-api/WEB-INF/config.properties")) {

	            Properties prop = new Properties();

	            // load a properties file
	            prop.load(input);

	            // get the property value and print it out
	            final String DB_URL = prop.getProperty("DB_URL");
	            final String USER = prop.getProperty("USER");
	            final String PASS = prop.getProperty("PASS");
		   // Register JDBC driver
				Class.forName("com.mysql.jdbc.Driver");
				
				   // Open a connection
				   Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
				   // Execute SQL query
				   Statement stmt = conn.createStatement();
		   try {
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
		      //Retrieve by column name
			  TranscriptionProfile transcriptionProfile = new TranscriptionProfile();
			  transcriptionProfile.setTimestamp(rs.getTimestamp("Timestamp"));
			  transcriptionProfile.setUserId(rs.getInt("UserId"));
			  transcriptionProfile.setWP_UserId(rs.getInt("WP_UserId"));
			  transcriptionProfile.setItemId(rs.getInt("ItemId"));
			  transcriptionProfile.setAmount(rs.getInt("Amount"));
			  transcriptionProfile.setItemImageLink(rs.getString("ItemImageLink"));
			  transcriptionProfile.setItemTitle(rs.getString("ItemTitle"));
			  transcriptionProfile.setCompletionStatus(rs.getString("CompletionStatus"));
			  transcriptionProfile.setScoreType(rs.getString("ScoreType"));
			  transcriptionProfileList.add(transcriptionProfile);
		   }
		
		   // Clean-up environment
		   rs.close();
		   stmt.close();
		   conn.close();
		   } catch(SQLException se) {
		       //Handle errors for JDBC
			   se.printStackTrace();
		   } finally {
			    try { stmt.close(); } catch (Exception e) { /* ignored */ }
			    try { conn.close(); } catch (Exception e) { /* ignored */ }
		   }
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    Gson gsonBuilder = new GsonBuilder().create();
	    String result = gsonBuilder.toJson(transcriptionProfileList);
	    return result;
	}

	
	//Search using custom filters
	@Path("")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response search(@Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT  " + 
						"    * " + 
						"FROM " + 
						"    (SELECT  " + 
						"			st.Name as ScoreType, " + 
						"            s.Amount, " + 
						"            s.Timestamp, " + 
						"            s.UserId, " + 
						"            s.ItemId, " + 
						"            u.WP_UserId, " + 
						"            i.ImageLink AS ItemImageLink, " + 
						"            i.Title AS ItemTitle, " + 
						"            c.Name AS CompletionStatus " + 
						"    FROM " + 
						"        Score s " + 
						"    JOIN ScoreType st ON s.ScoreTypeId = st.ScoreTypeId " + 
						"    JOIN User u ON s.UserId = u.UserId " + 
						"    JOIN Item i ON s.ItemId = i.ItemId " + 
						"    JOIN CompletionStatus c ON i.CompletionStatusId = c.CompletionStatusId) a " + 
						"WHERE 1";

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
		String resource = executeQuery(query, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}
}