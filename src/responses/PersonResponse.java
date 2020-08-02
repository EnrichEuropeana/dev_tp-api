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

import objects.Person;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import com.google.gson.*;

@Path("/persons")
public class PersonResponse {


	public static String executeQuery(String query, String type) throws SQLException{
		   List<Person> personList = new ArrayList<Person>();
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
			   if (type == "PersonId") {
				   rs = stmt.executeQuery(query);
				   if(rs.next() == false){
					   rs.close();
					   stmt.close();
					   conn.close();
					   return "";
				   }
				   else {
					   String personId = rs.getString("PersonId");
					   rs.close();
					   stmt.close();
					   conn.close();
					   return personId;
				   }
			   }
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
			  Person Person = new Person();
			  Person.setPersonId(rs.getInt("PersonId"));
			  Person.setFirstName(rs.getString("FirstName"));
			  Person.setLastName(rs.getString("LastName"));
			  Person.setBirthPlace(rs.getString("BirthPlace"));
			  Person.setBirthDate(rs.getString("BirthDate"));
			  Person.setDeathPlace(rs.getString("DeathPlace"));
			  Person.setDeathDate(rs.getString("DeathDate"));
			  Person.setLink(rs.getString("Link"));
			  Person.setDescription(rs.getString("Description"));
			  Person.setItemId(rs.getInt("ItemId"));
			  personList.add(Person);
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
	    String result = gsonBuilder.toJson(personList);
	    return result;
	}

	//Search using custom filters
	
	@Produces("application/json;charset=utf-8")
	@GET
	public Response search(@Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT * FROM ("
				+ "SELECT " +
				"   ip.PersonId as PersonId,\r\n" + 
				"	FirstName,\r\n" + 
				"    LastName,\r\n" + 
				"    BirthPLace,\r\n" + 
				"    BirthDate,\r\n" + 
				"    DeathPlace,\r\n" + 
				"    DeathDate,\r\n" + 
				"    Link,\r\n" + 
				"    Description,\r\n" + 
				"    ip.ItemId as ItemId\r\n" + 
				"FROM Person p\r\n" + 
				"JOIN ItemPerson ip 	ON p.PersonId = ip.PersonId\r\n) a " + 
				" WHERE 1";
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
		//ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}


	//Add new entry
	
	@POST
	public Response add(String body) throws SQLException {	
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    Person person = gson.fromJson(body, Person.class);

    	// Check if Person exists already
    	String checkQuery = "SELECT PersonId FROM Person "
    						+ "WHERE FirstName ";
		if(!person.FirstName.equals("")) {
			checkQuery += "= '" + person.FirstName + "' ";
		}
		else {
			checkQuery += "is null ";
		}
    	checkQuery += "AND LastName ";
		if(!person.LastName.equals("")) {
			checkQuery += "= '" + person.LastName + "' ";
		}
		else {
			checkQuery += "is null ";
		}
		checkQuery += " LIMIT 1";
		String PersonId = executeQuery(checkQuery, "PersonId");
		if (PersonId == "") {
			// Person doesn't exist yet
		    String query = "";
			query += "INSERT INTO Person (FirstName, LastName, BirthPlace, BirthDate, DeathPlace, DeathDate, Link, Description, ItemId) "
							+ "VALUES (";
			if(!person.FirstName.equals("")) {
				query += "'" + person.FirstName + "',";
			}
			else {
				query += "null,";
			}
			if(!person.LastName.equals("")) {
				query += "'" + person.LastName + "',";
			}
			else {
				query += "null,";
			}
			if(!person.BirthPlace.equals("")) {
				query += "'" + person.BirthPlace + "',";
			}
			else {
				query += "null,";
			}
			if(!person.BirthDate.equals("")) {
				query += "'" + person.BirthDate + "',";
			}
			else {
				query += "null,";
			}
			if(!person.DeathPlace.equals("")) {
				query += "'" + person.DeathPlace + "',";
			}
			else {
				query += "null,";
			}
			if(!person.DeathDate.equals("")) {
				query += "'" + person.DeathDate + "',";
			}
			else {
				query += "null,";
			}
			if(!person.Link.equals("")) {
				query += "'" + person.Link + "',";
			}
			else {
				query += "null,";
			}
			if(!person.Description.equals("")) {
				query += "'" + person.Description + "',";
			}
			else {
				query += "null,";
			}
			query += person.ItemId;
			query += ")";
			String resource = executeQuery(query, "Insert");
			
			// Add Person to Item
			
			PersonId = executeQuery(checkQuery, "PersonId");
			String itemPersonInsert = "INSERT INTO ItemPerson (ItemId, PersonId) "
										+ "VALUES (" + person.ItemId + ", " + PersonId + ")";
			String itemPersonInsertResponse = executeQuery(itemPersonInsert, "Insert");
			ResponseBuilder rBuild = Response.ok(query);
	        return rBuild.build();
		}
		else {
			// Add Person to Item
			
			String itemPersonInsert = "INSERT INTO ItemPerson (ItemId, PersonId) "
										+ "VALUES (" + person.ItemId + ", " + PersonId + ")";
			String itemPersonInsertResponse = executeQuery(itemPersonInsert, "Insert");
			ResponseBuilder rBuild = Response.ok(itemPersonInsertResponse);
	        return rBuild.build();
		}
	}


	//Edit entry by id
	@Path("/{id}")
	@POST
	public Response update(@PathParam("id") int id, String body) throws SQLException {
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    Person changes = gson.fromJson(body, Person.class);

	    String query = "UPDATE Person ";
	    		query += "SET FirstName = ";
	    		if(!changes.FirstName.equals("")) {
	    			query += "'" + changes.FirstName + "',";
	    		}
	    		else {
	    			query += "null,";
	    		}
	    		
	    		query += "LastName = ";
	    		if(!changes.LastName.equals("")) {
	    			query += "'" + changes.LastName + "',";
	    		}
	    		else {
	    			query += "null,";
	    		}

	    		query += "BirthPlace = ";
	    		if(!changes.BirthPlace.equals("")) {
	    			query += "'" + changes.BirthPlace + "',";
	    		}
	    		else {
	    			query += "null,";
	    		}
	    		query += "BirthDate = ";
	    		if(!changes.BirthDate.equals("")) {
	    			query += "'" + changes.BirthDate + "',";
	    		}
	    		else {
	    			query += "null,";
	    		}

	    		query += "DeathPlace = ";
	    		if(!changes.DeathPlace.equals("")) {
	    			query += "'" + changes.DeathPlace + "',";
	    		}
	    		else {
	    			query += "null,";
	    		}

	    		query += "DeathDate = ";
	    		if(!changes.DeathDate.equals("")) {
	    			query += "'" + changes.DeathDate + "',";
	    		}
	    		else {
	    			query += "null,";
	    		}

	    		query += "Link = ";
	    		if(!changes.Link.equals("")) {
	    			query += "'" + changes.Link + "',";
	    		}
	    		else {
	    			query += "null,";
	    		}

	    		query += "Description = ";
	    		if(!changes.Description.equals("")) {
	    			query += "'" + changes.Description + "'";
	    		}
	    		else {
	    			query += "null ";
	    		}
		query += " WHERE PersonId = " + id;
		
		String resource = executeQuery(query, "Update");
		//ResponseBuilder rBuild = Response.ok(resource);
		ResponseBuilder rBuild = Response.ok(query);
        return rBuild.build();
	}
	

	//Delete entry by id
	@Path("/{id}")
	@DELETE
	public String delete(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("DELETE FROM Person WHERE PersonId = " + id, "Delete");
		return resource;
	}
	

	//Get entry by id
	@Path("/{id}")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getEntry(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("SELECT * FROM ("
				+ "SELECT " +
				"   ip.PersonId as PersonId,\r\n" + 
				"	FirstName,\r\n" + 
				"    LastName,\r\n" + 
				"    BirthPLace,\r\n" + 
				"    BirthDate,\r\n" + 
				"    DeathPlace,\r\n" + 
				"    DeathDate,\r\n" + 
				"    Link,\r\n" + 
				"    Description,\r\n" + 
				"    ip.ItemId as ItemId\r\n" + 
				"FROM Person p\r\n" + 
				"JOIN ItemPerson ip 	ON p.PersonId = ip.PersonId\r\n) a " + 
				" WHERE PersonId = " + id, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}
}


