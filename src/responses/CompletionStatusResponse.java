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


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import objects.CompletionStatus;

import java.util.*;
import java.sql.*;

import com.google.gson.*;

@Path("/completionStatus")
public class CompletionStatusResponse {

	public static String executeQuery(String query, String type) throws SQLException{
		   List<CompletionStatus> completionStatusList = new ArrayList<CompletionStatus>();
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
				Class.forName("com.mysql.jdbc.Driver");
				
				   // Open a connection
				   conn = DriverManager.getConnection(DB_URL, USER, PASS);
				   // Execute SQL query
				   stmt = conn.createStatement();
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
			   rs = stmt.executeQuery(query);
			   
			   // Extract data from result set
			   while(rs.next()){
			      //Retrieve by column name
				  CompletionStatus CompletionStatus = new CompletionStatus();
				  CompletionStatus.setCompletionStatusId(rs.getInt("CompletionStatusId"));
				  CompletionStatus.setName(rs.getString("Name"));
				  CompletionStatus.setColorCode(rs.getString("ColorCode"));
				  CompletionStatus.setColorCodeGradient(rs.getString("ColorCodeGradient"));
				  completionStatusList.add(CompletionStatus);
			   }
			
			   // Clean-up environment
			   rs.close();
			   stmt.close();
			   conn.close();
			   } catch(SQLException se) {
			       //Handle errors for JDBC
				   se.printStackTrace();
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  finally {
		    try { rs.close(); } catch (Exception e) { /* ignored */ }
		    try { stmt.close(); } catch (Exception e) { /* ignored */ }
		    try { conn.close(); } catch (Exception e) { /* ignored */ }
	   }
	    Gson gsonBuilder = new GsonBuilder().create();
	    
	    String result = gsonBuilder.toJson(completionStatusList);
	    return result;
	}

	// Get entries
	
	@Produces("application/json;charset=utf-8")
	@GET
	public Response search(@Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT * FROM CompletionStatus WHERE 1";
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

	//Add new entry
	
	@POST
	public String add(String body) throws SQLException {	
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    CompletionStatus completionStatus = gson.fromJson(body, CompletionStatus.class);
	    
	    //Check if all mandatory fields are included
	    if (completionStatus.CompletionStatusId != null && completionStatus.Name != null) {
			String query = "INSERT INTO CompletionStatus (CompletionStatusId, Name) "
							+ "VALUES (" + completionStatus.CompletionStatusId
							+ ", '" + completionStatus.Name + "')";
			String resource = executeQuery(query, "Insert");
			return resource;
	    } else {
	    	return "Fields missing";
	    }
	}
	

	//Edit entry by id
	@Path("/{id}")
	@POST
	public String update(@PathParam("id") int id, String body) throws SQLException {
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    JsonObject  changes = gson.fromJson(body, JsonObject.class);
	    
	    //Check if field is allowed to be changed
	    if (changes.get("CompletionStatusId") != null) {
	    	return "Prohibited change attempt";
	    }
	    
	    //Check if NOT NULL field is attempted to be changed to NULL
	    if (changes.get("Name") == null || !changes.get("Name").isJsonNull()) {
		    String query = "UPDATE CompletionStatus SET ";
		    
		    int keyCount = changes.entrySet().size();
		    int i = 1;
			for(Map.Entry<String, JsonElement> entry : changes.entrySet()) {
			    query += entry.getKey() + " = '" + changes.get(entry.getKey()).getAsString() + "'";
			    if (i < keyCount) {
			    	query += ", ";
			    }
			    i++;
			}
			query += " WHERE CompletionStatusId = " + id;
			String resource = executeQuery(query, "Update");
			return resource;
	    } else {
	    	return "Prohibited change to null";
	    }
	}
	

	//Delete entry by id
	@Path("/{id}")
	@DELETE
	public String delete(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("DELETE FROM CompletionStatus WHERE CompletionStatusId = " + id, "Delete");
		return resource;
	}
	

	//Get entry by id
	@Path("/{id}")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getEntry(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("SELECT * FROM CompletionStatus WHERE CompletionStatusId = " + id, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}
}


