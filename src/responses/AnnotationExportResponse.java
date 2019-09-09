package responses;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import objects.AnnotationExport;
import objects.ApiKey;
import objects.Person;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.ParseException;

import com.google.gson.*;
import com.google.gson.stream.MalformedJsonException;

@Path("/enrichments")
public class AnnotationExportResponse {

	public String executeQuery(String query, String type) throws SQLException{
		   List<AnnotationExport> annotationExports = new ArrayList<AnnotationExport>();
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
				   return "Failed";
			   }
		   }
		   ResultSet rs = stmt.executeQuery(query);
		   
		   // Extract data from result set
		   while(rs.next()){
		      //Retrieve by column name
			  AnnotationExport annotationExport = new AnnotationExport();
			  annotationExport.setEuropeanaAnnotationId(rs.getInt("EuropeanaAnnotationId"));
			  annotationExport.setAnnotationId(rs.getInt("AnnotationId"));
			  annotationExport.setText(rs.getString("Text"));
			  annotationExport.setTimestamp(rs.getTimestamp("Timestamp"));
			  annotationExport.setX_Coord(rs.getFloat("X_Coord"));
			  annotationExport.setY_Coord(rs.getFloat("Y_Coord"));
			  annotationExport.setWidth(rs.getFloat("Width"));
			  annotationExport.setHeight(rs.getFloat("Height"));
			  annotationExport.setMotivation(rs.getString("Motivation"));
			  annotationExport.setItemId(rs.getString("ItemId"));
			  annotationExport.setStoryUrl(rs.getString("StoryUrl"));
			  annotationExport.setStoryId(rs.getString("StoryId"));
			  annotationExports.add(annotationExport);
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
	    String result = gsonBuilder.toJson(annotationExports);
	    return result;
	}

	public String getApiKeys() throws SQLException{
			String query = "SELECT * FROM ApiKey";
		   List<ApiKey> apiKeys = new ArrayList<ApiKey>();
	       try (InputStream input = new FileInputStream("/home/enrich/tomcat/apache-tomcat-9.0.13/webapps/tp-api/WEB-INF/config.properties")) {

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
		   ResultSet rs = stmt.executeQuery(query);
		   
		   // Extract data from result set
		   while(rs.next()){
		      //Retrieve by column name
			  ApiKey apiKey = new ApiKey();
			  apiKey.setApiKeyId(rs.getInt("ApiKeyId"));
			  apiKey.setKeyString(rs.getString("KeyString"));
			  apiKey.setProjectId(rs.getInt("ProjectId"));
			  apiKey.setRoleId(rs.getInt("RoleId"));
			  apiKeys.add(apiKey);
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
	    String result = gsonBuilder.toJson(apiKeys);
	    return result;
	}

	//Get all Entries
	@Path("")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getAll(@Context UriInfo uriInfo, @Context HttpHeaders headers) throws SQLException {
		boolean auth = false;
		String authorizationToken = "";
		if (headers.getRequestHeader(HttpHeaders.AUTHORIZATION) != null) {
			List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
			authorizationToken = authHeaders.get(0);
			String tokens = getApiKeys();
			JsonArray data = new JsonParser().parse(tokens).getAsJsonArray();
			
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i).getAsJsonObject().get("KeyString").toString().replace("\"", "").equals(authorizationToken)) {
					auth = true;
					break;
				}
			}
		}
		if (auth != true) {
			ResponseBuilder authResponse = Response.status(Response.Status.UNAUTHORIZED);
			return authResponse.build();
		}
		
		String query = "SELECT * FROM (" + 
				"(SELECT  " + 
				"	 a.AnnotationId, " +
				"    a.Text, " + 
				"    a.Timestamp, " + 
				"    a.X_Coord, " + 
				"    a.Y_Coord, " + 
				"    a.Width, " + 
				"    a.Height, " + 
				"    a.EuropeanaAnnotationId, " + 
				"    m.Name AS Motivation, " + 
				"    i.ProjectItemId as ItemId, " + 
				"    s.`edm:landingPage` as StoryUrl, " + 
				"    s.ExternalRecordId as StoryId " + 
				"FROM " + 
				"    Annotation a " + 
				"        LEFT JOIN " + 
				"    AnnotationType at ON a.AnnotationTypeId = at.AnnotationTypeId " + 
				"        LEFT JOIN " + 
				"    Motivation m ON at.MotivationId = m.MotivationId " + 
				"        LEFT JOIN " + 
				"    Item i ON i.ItemId = a.ItemId " + 
				"        LEFT JOIN " + 
				"    Story s ON s.StoryId = i.StoryId)  " + 
				"UNION ( " + 
				"	SELECT  " + 
				"	 t.TranscriptionId, " +
				"    t.Text, " + 
				"    t.Timestamp, " + 
				"    t.EuropeanaAnnotationId, " + 
				"    0 AS X_Coord, " + 
				"    0 AS Y_Coord, " + 
				"    0 AS Width, " + 
				"    0 AS Height, " + 
				"    'transcribing' AS Motivation, " + 
				"    i.ProjectItemId, " + 
				"    s.`edm:landingPage`, " + 
				"    s.ExternalRecordId " + 
				"FROM " + 
				"    Transcription t " + 
				"        LEFT JOIN " + 
				"    Item i ON i.ItemId = t.ItemId " + 
				"        LEFT JOIN " + 
				"    Story s ON s.StoryId = i.StoryId " + 
				"WHERE " + 
				"    CurrentVersion = 1) "
				+ ") a WHERE 1";
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
		if (resource == "Failed") {
			ResponseBuilder rBuild = Response.status(Response.Status.BAD_REQUEST);
	        return rBuild.build();
		}
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}

	//Edit entry by id
	@Path("/transcription/{id}")
	@POST
	public Response transcriptionUpdate(@PathParam("id") int id, String body) throws SQLException, ParseException {
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    JsonObject changes = gson.fromJson(body, JsonObject.class);
	    
	    String query = "UPDATE Transcription SET ";
	    int keyCount = changes.entrySet().size();
	    int i = 1;
		for(Map.Entry<String, JsonElement> entry : changes.entrySet()) {
			/*
			if (entry.getKey().toString() != "EuropeanaAnnotationId") {
				ResponseBuilder rBuild = Response.status(Response.Status.BAD_REQUEST);
		        return rBuild.build();
			}
			*/
		    query += entry.getKey() + " = " + entry.getValue();
		    if (i < keyCount) {
		    	query += ", ";
		    }
		    i++;
		}
		query += " WHERE TranscriptionId = " + id;
		String resource = executeQuery(query, "Update");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}
	
	//Edit entry by id
	@Path("/annotation/{id}")
	@POST
	public Response annotationUpdate(@PathParam("id") int id, String body) throws SQLException, ParseException {
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    JsonObject changes = gson.fromJson(body, JsonObject.class);
	    
	    String query = "UPDATE Annotation SET ";
	    int keyCount = changes.entrySet().size();
	    int i = 1;
		for(Map.Entry<String, JsonElement> entry : changes.entrySet()) {
			/*
			if (entry.getKey().toString() != "EuropeanaAnnotationId") {
				ResponseBuilder rBuild = Response.status(Response.Status.BAD_REQUEST);
		        return rBuild.build();
			}
			*/
		    query += entry.getKey() + " = " + entry.getValue();
		    if (i < keyCount) {
		    	query += ", ";
		    }
		    i++;
		}
		query += " WHERE AnnotationId = " + id;
		String resource = executeQuery(query, "Update");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}
}
