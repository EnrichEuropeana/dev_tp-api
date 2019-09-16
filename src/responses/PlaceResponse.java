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

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import com.google.gson.*;

@Path("/places")
public class PlaceResponse {


	public String executeQuery(String query, String type) throws SQLException{
		   List<Place> placeList = new ArrayList<Place>();
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
			  Place Place = new Place();
			  Place.setPlaceId(rs.getInt("PlaceId"));
			  Place.setName(rs.getString("Name"));
			  Place.setLatitude(rs.getFloat("Latitude"));
			  Place.setLongitude(rs.getFloat("Longitude"));
			  Place.setItemId(rs.getInt("ItemId"));
			  Place.setLink(rs.getString("Link"));
			  Place.setZoom(rs.getInt("Zoom"));
			  Place.setComment(rs.getString("Comment"));
			  Place.setUserId(rs.getInt("UserId"));
			  Place.setUserGenerated(rs.getString("UserGenerated"));
			  placeList.add(Place);
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
	    String result = gsonBuilder.toJson(placeList);
	    return result;
	}

	//Search using custom filters
	@Path("")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response search(@Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT * FROM Place WHERE 1";
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
	public Response add(String body) throws SQLException {	
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    Place place = gson.fromJson(body, Place.class);
	    
	    //Check if all mandatory fields are included
	    if (place.Latitude != null && place.Longitude != null && place.ItemId != null) {
			String query = "INSERT INTO Place (Name, Latitude, Longitude, ItemId, Link, Zoom, Comment, UserId, UserGenerated) "
							+ "VALUES ('" + place.Name + "'"
							+ ", " + place.Latitude
							+ ", " + place.Longitude
							+ ", " + place.ItemId
							+ ", '" + place.Link + "'"
							+ ", " + place.Zoom
							+ ", '" + place.Comment + "'"
							+ ", (SELECT UserId FROM User " 
							+ "		WHERE WP_UserId = " + place.UserId + ")"
							+ ", " + place.UserGenerated + ")";
			String resource = executeQuery(query, "Insert");
			ResponseBuilder rBuild = Response.ok(resource);
			//ResponseBuilder rBuild = Response.ok(query);
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
	    Place  changes = gson.fromJson(body, Place.class);
	    
	    String query = "UPDATE Place "
	    				+ "SET Name = '" + changes.Name + "', "
   	    				 + "Latitude = " + changes.Latitude + ", "
 	    				 + "Longitude = " + changes.Longitude + ", "
	    				 + "Comment = '" + changes.Comment + "' ";
		query += " WHERE PlaceId = " + id;
		String resource = executeQuery(query, "Update");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}
	

	//Delete entry by id
	@Path("/{id}")
	@DELETE
	public String delete(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("DELETE FROM Place WHERE PlaceId = " + id, "Delete");
		return resource;
	}
	

	//Get entry by id
	@Path("/{id}")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getEntry(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("SELECT * FROM Place WHERE PlaceId = " + id, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}

}


