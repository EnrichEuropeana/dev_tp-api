package responses;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import objects.AnnotationExport;
import objects.ApiKey;
import objects.Language;
import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.ParseException;

import com.google.gson.*;

@Path("/enrichments")
public class AnnotationExportResponse {

	public String executeQuery(String query, String type) throws SQLException{
		   List<AnnotationExport> annotationExports = new ArrayList<AnnotationExport>();
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
				   return "Failed";
			   }
		   }
		   rs = stmt.executeQuery(query);
		   
		   // Extract data from result set
		   while(rs.next()){
		      //Retrieve by column name
			  AnnotationExport annotationExport = new AnnotationExport();
			  annotationExport.setEuropeanaAnnotationId(rs.getInt("EuropeanaAnnotationId"));
			  annotationExport.setAnnotationId(rs.getInt("AnnotationId"));
			  annotationExport.setText(rs.getString("Text"));
			  annotationExport.setTextNoTags(rs.getString("TextNoTags"));
			  annotationExport.setTimestamp(rs.getTimestamp("Timestamp"));
			  annotationExport.setX_Coord(rs.getFloat("X_Coord"));
			  annotationExport.setY_Coord(rs.getFloat("Y_Coord"));
			  annotationExport.setWidth(rs.getFloat("Width"));
			  annotationExport.setHeight(rs.getFloat("Height"));
			  annotationExport.setMotivation(rs.getString("Motivation"));
			  annotationExport.setOrderIndex(rs.getInt("OrderIndex"));
			  annotationExport.setTranscribathonItemId(rs.getInt("TranscribathonItemId"));
			  annotationExport.setTranscribathonStoryId(rs.getInt("TranscribathonStoryId"));
			  annotationExport.setStoryUrl(rs.getString("StoryUrl"));
			  annotationExport.setStoryId(rs.getString("StoryId"));
			  annotationExport.setImageLink(rs.getString("ImageLink"));
			  
			  // Add Languages
			  List<Language> LanguageList = new ArrayList<Language>();
			  if (rs.getString("LanguageName") != null) {
				  String[] LanguageNames = rs.getString("LanguageName").split(",");
				  String[] LanguageCodes = rs.getString("LanguageCode").split(",");
				  for (int i = 0; i < LanguageNames.length; i++) {
					  Language language = new Language();
					  language.setName(LanguageNames[i]);
					  language.setCode(LanguageCodes[i]);
					  LanguageList.add(language);
				  }
			  }
			  annotationExport.setLanguages(LanguageList);
			  
			  annotationExports.add(annotationExport);
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
	    String result = gsonBuilder.toJson(annotationExports);
	    return result;
	}

	public String getApiKeys() throws SQLException{
			String query = "SELECT * FROM ApiKey";
		   List<ApiKey> apiKeys = new ArrayList<ApiKey>();
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
		   rs = stmt.executeQuery(query);
		   
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
	    String result = gsonBuilder.toJson(apiKeys);
	    return result;
	}

	//Get all Entries
	
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

		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String query = "SELECT \r\n" + 
				"    *\r\n" + 
				"FROM\r\n" + 
				"    ((SELECT \r\n" + 
				"        a.AnnotationId,\r\n" + 
				"            a.Text,\r\n" + 
				"            a.TextNoTags,\r\n" + 
				"            a.Timestamp,\r\n" + 
				"            a.X_Coord,\r\n" + 
				"            a.Y_Coord,\r\n" + 
				"            a.Width,\r\n" + 
				"            a.Height,\r\n" + 
				"            a.EuropeanaAnnotationId,\r\n" + 
				"            m.Name AS Motivation,\r\n" + 
				"            i.ProjectItemId AS ItemId,\r\n" + 
				"            i.OrderIndex AS OrderIndex,\r\n" + 
				"            i.ItemId AS TranscribathonItemId,\r\n" + 
				"            i.`edm:WebResource` AS ImageLink,\r\n" + 
				"            s.StoryId AS TranscribathonStoryId,\r\n" + 
				"            s.`edm:landingPage` AS StoryUrl,\r\n" + 
				"            CONCAT(s.RecordId, IFNULL(i.EuropeanaAttachment, '')) AS StoryId,\r\n" + 
				"            NULL AS LanguageCode,\r\n" + 
				"            NULL AS LanguageName\r\n" + 
				"    FROM\r\n" + 
				"        (SELECT \r\n" + 
				"        *\r\n" + 
				"    FROM\r\n" + 
				"        Story ) s\r\n" + 
				"    LEFT JOIN (SELECT * FROM Item WHERE ";
				if (!queryParams.containsKey("includeExported") || !queryParams.getFirst("includeExported").equals("1")) {
					query +=  " Exported = 0 AND";
				}
				query += " TranscriptionStatusId = 4) i ON s.StoryId = i.StoryId\r\n" + 
				"    LEFT JOIN Annotation a ON i.ItemId = a.ItemId\r\n" + 
				"    LEFT JOIN AnnotationType at ON a.AnnotationTypeId = at.AnnotationTypeId\r\n" + 
				"    LEFT JOIN Motivation m ON at.MotivationId = m.MotivationId\r\n" + 
				"    WHERE 1 \r\n";
				if (queryParams.containsKey("storyId")) {
					query +=  " AND RecordId = '" + queryParams.getFirst("storyId") + "'";
				}
				query +=  " AND AnnotationId is not null) UNION (SELECT \r\n" + 
				"        t.TranscriptionId,\r\n" + 
				"            t.Text,\r\n" + 
				"            t.TextNoTags,\r\n" + 
				"            t.Timestamp,\r\n" + 
				"            0 AS X_Coord,\r\n" + 
				"            0 AS Y_Coord,\r\n" + 
				"            0 AS Width,\r\n" + 
				"            0 AS Height,\r\n" + 
				"            t.EuropeanaAnnotationId,\r\n" + 
				"            'transcribing' AS Motivation,\r\n" + 
				"            i.ProjectItemId,\r\n" + 
				"            i.OrderIndex,\r\n" + 
				"            i.ItemId,\r\n" + 
				"            i.`edm:WebResource`,\r\n" + 
				"            s.StoryId,\r\n" + 
				"            s.`edm:landingPage`,\r\n" + 
				"            CONCAT(s.RecordId, IFNULL(i.EuropeanaAttachment, '')) AS StoryId,\r\n" + 
				"            a.LanguageCode,\r\n" + 
				"            a.LanguageName\r\n" + 
				"    FROM\r\n" + 
				"        (SELECT \r\n" + 
				"        *\r\n" + 
				"    FROM\r\n" + 
				"        Story\r\n" + 
				"    WHERE 1 \r\n";
				if (queryParams.containsKey("storyId")) {
					query +=  " AND RecordId = '" + queryParams.getFirst("storyId") + "'";
				}
				query +=  ") s\r\n" + 
				"    LEFT JOIN (SELECT * FROM Item WHERE ";
				if (!queryParams.containsKey("includeExported") || !queryParams.getFirst("includeExported").equals("1")) {
					query +=  " Exported = 0 AND";
				}
				query +=  " TranscriptionStatusId = 4) i ON s.StoryId = i.StoryId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        *\r\n" + 
				"    FROM\r\n" + 
				"        Transcription\r\n" + 
				"    WHERE\r\n" + 
				"        1) t ON i.ItemId = t.ItemId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        t.TranscriptionId,\r\n" + 
				"            GROUP_CONCAT(l.Code) LanguageCode,\r\n" + 
				"            GROUP_CONCAT(l.Name) LanguageName\r\n" + 
				"    FROM\r\n" + 
				"        Transcription t\r\n" + 
				"    JOIN TranscriptionLanguage tl ON t.TranscriptionId = tl.TranscriptionId\r\n" + 
				"    JOIN Language l ON tl.LanguageId = l.LanguageId\r\n" + 
				"    GROUP BY TranscriptionId) a ON a.TranscriptionId = t.TranscriptionId\r\n" + 
				"    WHERE\r\n" + 
				"        CurrentVersion = 1 AND NoText = 0\r\n";
				if (queryParams.containsKey("storyId")) {
					query +=  " AND RecordId like '" + queryParams.getFirst("storyId") + "%'";
				}
				query +=  " AND t.TranscriptionId is not null)) a\r\n" + 
				"WHERE\r\n" + 
				"    1\r\n";
		
		for(String key : queryParams.keySet()){
	    	if (!key.equals("includeExported") && !key.equals("storyId")) {
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
		}
		String resource = executeQuery(query, "Select");
		if (resource == "Failed") {
			ResponseBuilder rBuild = Response.status(Response.Status.BAD_REQUEST);
	        return rBuild.build();
		}
		ResponseBuilder rBuild = Response.ok(resource);
		//ReponseBuilder rBuild = Response.ok(query);
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
		
        // Set Exported to 1 to prevent multiple exports
        String ExportUpdateQuery = "UPDATE Item SET Exported = 1 WHERE ItemId = (SELECT ItemId FROM Transcription WHERE TranscriptionId = " + id + ")";
		executeQuery(ExportUpdateQuery, "Update");
		
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

        // Set Exported to 1 to prevent multiple exports
        String ExportUpdateQuery = "UPDATE Item SET Exported = 1 WHERE ItemId = (SELECT ItemId FROM Annotation WHERE AnnotationId = " + id + ")";
		executeQuery(ExportUpdateQuery, "Update");
		
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}
}
