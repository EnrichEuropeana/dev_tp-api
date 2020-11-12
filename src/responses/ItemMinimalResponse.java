package responses;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import objects.ApiKey;
import objects.Item;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import com.google.gson.*;

@Path("/itemMinimal")
public class ItemMinimalResponse {


	public String executeQuery(String query, String type) throws SQLException{
		   List<Item> itemList = new ArrayList<Item>();
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
		      //Retrieve by column name
			  Item Item = new Item();
			  Item.setItemId(rs.getInt("ItemId"));
			  Item.setStoryId(rs.getInt("StoryId"));
			  Item.setImageLink(rs.getString("ImageLink"));
			  Item.setTitle(rs.getString("Title"));
			  Item.setDatasetId(rs.getInt("DatasetId"));
			  Item.setCompletionStatusId(rs.getInt("CompletionStatusId"));
			  Item.setTranscriptionStatusId(rs.getInt("TranscriptionStatusId"));
			  Item.setDescriptionStatusId(rs.getInt("DescriptionStatusId"));
			  Item.setLocationStatusId(rs.getInt("LocationStatusId"));
			  Item.setTaggingStatusId(rs.getInt("TaggingStatusId"));
			  Item.setAutomaticEnrichmentStatusId(rs.getInt("AutomaticEnrichmentStatusId"));
			  itemList.add(Item);
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
	    String result = gsonBuilder.toJson(itemList);
	    return result;
	}
	
	@Produces("application/json;charset=utf-8")
	@GET
	public Response search(@Context UriInfo uriInfo, String body, @Context HttpHeaders headers) throws SQLException {	
		String query = "SELECT "
				+ "ItemId, "
				+ "StoryId, "
				+ "ImageLink, "
				+ "Title, "
				+ "DatasetId, "
				+ "CompletionStatusId, "
				+ "TranscriptionStatusId, "
				+ "DescriptionStatusId, "
				+ "LocationStatusId, "
				+ "TaggingStatusId, "
				+ "AutomaticEnrichmentStatusId "
				+ "FROM Item  "
				+ "WHERE 1";
		
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
	
