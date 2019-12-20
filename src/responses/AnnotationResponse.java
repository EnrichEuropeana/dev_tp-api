package responses;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import objects.Annotation;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import com.google.gson.*;
import com.google.gson.stream.MalformedJsonException;

@Path("/annotations")
public class AnnotationResponse {


	public String executeQuery(String query, String type) throws SQLException{
		   List<Annotation> annotations = new ArrayList<Annotation>();
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
			  Annotation annotation = new Annotation();
			  annotation.setAnnotationId(rs.getInt("AnnotationId"));
			  annotation.setText(rs.getString("Text"));
			  annotation.setTimestamp(rs.getString("Timestamp"));
			  annotation.setUserId(rs.getInt("UserId"));
			  annotation.setItemId(rs.getInt("ItemId"));
			  annotation.setX_Coord(rs.getFloat("X_Coord"));
			  annotation.setY_Coord(rs.getFloat("Y_Coord"));
			  annotation.setWidth(rs.getFloat("Width"));
			  annotation.setHeight(rs.getFloat("Height"));
			  annotation.setEuropeanaAnnotationId(rs.getInt("EuropeanaAnnotationId"));
			  annotations.add(annotation);
		   }
		
		   // Clean-up environment
		   rs.close();
		   stmt.close();
		   conn.close();
		   } catch(SQLException se) {
		       //Handle errors for JDBC
			   se.printStackTrace();
		   }  finally {
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
	    String result = gsonBuilder.toJson(annotations);
	    return result;
	}

	//Get entries
	@Path("")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response search(@Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT * FROM Annotation WHERE 1";
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
	@Path("")
	@POST
	public String add(String body) throws SQLException {	
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    Annotation annotation = gson.fromJson(body, Annotation.class);
	    
	    //Check if all mandatory fields are included
	    if (annotation.Text != null && annotation.UserId != null
	    		&& annotation.ItemId != null && annotation.X_Coord != null
	    		&& annotation.Y_Coord != null && annotation.Width != null
	    		&& annotation.Height != null) {
			String query = "INSERT INTO Annotation (Text, UserId, ItemId, X_Coord, Y_Coord, Width, Height) "
							+ "VALUES ('" + annotation.Text + "'"
									+ ", " + annotation.UserId
									+ ", " + annotation.ItemId
									+ ", " + annotation.X_Coord
									+ ", " + annotation.Y_Coord
									+ ", " + annotation.Width
								+ ", " + annotation.Height + ")";
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
	    JsonObject changes = gson.fromJson(body, JsonObject.class);
	    
	    //Check if field is allowed to be changed
	    if (changes.get("AnnotationId") != null || changes.get("Timestamp") != null ) {
	    	return "Prohibited change attempt";
	    }
	    
	    //Check if NOT NULL field is attempted to be changed to NULL
	    if ((changes.get("Text") == null || !changes.get("Text").isJsonNull())
	    		&& (changes.get("UserId") == null || !changes.get("UserId").isJsonNull())
	    		&& (changes.get("ItemId") == null || !changes.get("ItemId").isJsonNull())
				&& (changes.get("X_Coord") == null || !changes.get("X_Coord").isJsonNull())
				&& (changes.get("Y_Coord") == null || !changes.get("Y_Coord").isJsonNull())
				&& (changes.get("Width") == null || !changes.get("Width").isJsonNull())
				&& (changes.get("Height") == null || !changes.get("Height").isJsonNull())) {
		    String query = "UPDATE Annotation SET ";
		    int keyCount = changes.entrySet().size();
		    int i = 1;
			for(Map.Entry<String, JsonElement> entry : changes.entrySet()) {
			    query += entry.getKey() + " = '" + changes.get(entry.getKey()).getAsString() + "'";
			    if (i < keyCount) {
			    	query += ", ";
			    }
			    i++;
			}
			query += " WHERE AnnotationId = " + id;
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
		String resource = executeQuery("DELETE FROM Annotation WHERE AnnotationId = " + id, "Delete");
		return resource;
	}
	
	//Get entry by id
	@Path("/{id}")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getEntry(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("SELECT * FROM Annotation WHERE AnnotationId = " + id, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}
	
}


