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

import objects.Property;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import com.google.gson.*;

@Path("/properties")
public class PropertyResponse {


	public String executeQuery(String query, String type) throws SQLException{
		   List<Property> propertyList = new ArrayList<Property>();
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
			   if (type == "PropertyId") {
				   ResultSet rs = stmt.executeQuery(query);
				   if(rs.next() == false){
					   return "";
				   }
				   else {
					   return rs.getString("PropertyId");
				   }
			   }
			   else {
				   int success = stmt.executeUpdate(query);
				   if (success > 0) {
					   return type +" succesful";
				   }
				   else {
					   return type +" could not be executed";
				   }
			   }
		   }
		   ResultSet rs = stmt.executeQuery(query);
		   
		   // Extract data from result set
		   while(rs.next()){
		      //Retrieve by column name
			  Property property = new Property();
			  property.setPropertyId(rs.getInt("PropertyId"));
			  property.setPropertyValue(rs.getString("PropertyValue"));
			  property.setPropertyDescription(rs.getString("PropertyDescription"));
			  property.setPropertyTypeId(rs.getInt("PropertyTypeId"));
			  property.setPropertyType(rs.getString("PropertyType"));
			  property.setMotivationId(rs.getInt("MotivationId"));
			  property.setMotivation(rs.getString("Motivation"));
			  property.setEditable(rs.getString("Editable"));
			  propertyList.add(property);
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
	    String result = gsonBuilder.toJson(propertyList);
	    return result;
	}

	//Search using custom filters
	@Path("")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response search(@Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT * FROM (" +
				"SELECT \r\n" + 
				"	p.PropertyId as PropertyId,\r\n" + 
				"	p.Value as PropertyValue,\r\n" + 
				"	p.Description as PropertyDescription,\r\n" + 
				"	pt.PropertyTypeId as PropertyTypeId,\r\n" + 
				"    pt.Name as PropertyType,\r\n" + 
				"    m.MotivationId as MotivationId,\r\n" + 
				"    m.Name as Motivation,\r\n" + 
				"    pt.Editable as Editable\r\n" + 
				"FROM Property p\r\n" + 
				"JOIN PropertyType pt\r\n" + 
				"ON p.PropertyTypeId = pt.PropertyTypeId\r\n" + 
				"JOIN Motivation m\r\n" + 
				"ON pt.MotivationId = m.MotivationId) a " +
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
	

	//Add new entry
	@Path("")
	@POST
	public Response add(String body, @Context UriInfo uriInfo) throws SQLException {	
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    Property property = gson.fromJson(body, Property.class);
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

	    
	    //Check if all mandatory fields are included
	    if (property.PropertyValue != null && property.PropertyType != null) {
	    	// Check if property exists already
	    	String checkQuery = "SELECT PropertyId FROM Property "
	    						+ "WHERE Value = '" + property.PropertyValue + "'"
	    						+ "AND PropertyTypeId = (SELECT PropertyTypeID FROM PropertyType WHERE Name = '" + property.PropertyType + "')";
			String PropertyId = executeQuery(checkQuery, "PropertyId");
			if (PropertyId == "") {
				// Property doesn't exist yet

				// Add property
				String propertyInsert = "";
				propertyInsert += "INSERT INTO Property (Value, Description, PropertyTypeId) "
							+ "VALUES ('" + property.PropertyValue + "'";
				if(property.PropertyDescription != null && !property.PropertyDescription.equals("")) {
					propertyInsert += ",'" + property.PropertyDescription + "'";
				}
				else {
					propertyInsert += ",null";
				}
				propertyInsert += ", (SELECT PropertyTypeID "
							+ "FROM PropertyType "
							+ "WHERE Name = '" + property.PropertyType + "'))";
				String propertyInsertResponse = executeQuery(propertyInsert, "Insert");

				if (queryParams.keySet().contains("ItemId")) {
					// Add Property to Item
					
					PropertyId = executeQuery(checkQuery, "PropertyId");
					String itemPropertyInsert = "INSERT INTO ItemProperty (ItemId, PropertyId, UserGenerated) "
												+ "VALUES (" + queryParams.get("ItemId").get(0) + ", " + PropertyId + ", 1)";
					String itemPropertyInsertResponse = executeQuery(itemPropertyInsert, "Insert");
					ResponseBuilder rBuild = Response.ok(itemPropertyInsertResponse);
			        return rBuild.build();
				}
				else {
					ResponseBuilder rBuild = Response.ok(propertyInsertResponse);
			        return rBuild.build();
				}
			}
			else {
				// Property already exists
				
				if (queryParams.keySet().contains("ItemId")) {
					// Add Property to Item
					
					String itemPropertyInsert = "INSERT INTO ItemProperty (ItemId, PropertyId, UserGenerated) "
												+ "VALUES (" + queryParams.get("ItemId").get(0) + ", " + PropertyId + ", 1)";
					String itemPropertyInsertResponse = executeQuery(itemPropertyInsert, "Insert");
					ResponseBuilder rBuild = Response.ok(itemPropertyInsertResponse);
			        return rBuild.build();
				}
				else {
					
					ResponseBuilder rBuild = Response.ok("Property already exists");
			        return rBuild.build();
				}
			}
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
	    Property changes = gson.fromJson(body, Property.class);

	    //Check if NOT NULL field is attempted to be changed to NULL
	    String query = "UPDATE Property "
	    				+ "SET Value = '" + changes.PropertyValue + "', "
   	    				 + "Description = '" + changes.PropertyDescription + "' ";
		query += " WHERE PropertyId = " + id;
		String resource = executeQuery(query, "Update");
		//ResponseBuilder rBuild = Response.ok(resource);
		ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}
	

	//Delete entry by id
	@Path("/{id}")
	@DELETE
	public String delete(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("DELETE FROM Property WHERE PropertyId = " + id, "Delete");
		return resource;
	}
	

	//Get entry by id
	@Path("/{id}")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getEntry(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("SELECT * FROM Property WHERE PropertyId = " + id, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}

}