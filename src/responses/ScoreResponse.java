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

import objects.Score;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import com.google.gson.*;

@Path("/scores")
public class ScoreResponse {


	public String executeQuery(String query, String type) throws SQLException{
		   List<Score> scoreList = new ArrayList<Score>();
	       try (InputStream input = new FileInputStream("/home/enrich/tomcat/apache-tomcat-9.0.13/webapps/dev_tp-api/WEB-INF/config.properties")) {

	            Properties prop = new Properties();

	            // load a properties file
	            prop.load(input);

	            // get the score value and print it out
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
			   if (type == "ScoreId") {
				   ResultSet rs = stmt.executeQuery(query);
				   if(rs.next() == false){
					   return "";
				   }
				   else {
					   return rs.getString("ScoreId");
				   }
			   }
			   else {
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
		   }
		   ResultSet rs = stmt.executeQuery(query);
		   
		   // Extract data from result set
		   while(rs.next()){
		      //Retrieve by column name
			  Score score = new Score();
			  score.setScoreId(rs.getInt("ScoreId"));
			  score.setAmount(rs.getInt("Amount"));
			  score.setUserId(rs.getInt("UserId"));
			  score.setScoreTypeId(rs.getInt("ScoreTypeId"));
			  score.setScoreType(rs.getString("ScoreType"));
			  score.setRate(rs.getFloat("Rate"));
			  score.setItemId(rs.getInt("ItemId"));
			  score.setTimestamp(rs.getString("Timestamp"));
			  scoreList.add(score);
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
	    String result = gsonBuilder.toJson(scoreList);
	    return result;
	}

	//Search using custom filters
	@Path("")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response search(@Context UriInfo uriInfo) throws SQLException {
		String query = "SELECT * FROM (" +
				"SELECT \r\n" + 
				"	s.ScoreId as ScoreId,\r\n" + 
				"	s.Amount as Amount,\r\n" + 
				"   s.ItemId as ItemId,\r\n" + 
				"   s.UserId as UserId,\r\n" + 
				"	st.ScoreTypeId as ScoreTypeId,\r\n" + 
				"    st.Name as ScoreType,\r\n" + 
				"    st.Rate as Rate,\r\n" + 
				"    s.Timestamp as Timestamp\r\n" + 
				"FROM Score s\r\n" + 
				"JOIN ScoreType st\r\n" + 
				"ON s.ScoreTypeId = st.ScoreTypeId) a\r\n" + 
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
	    Score score = gson.fromJson(body, Score.class);
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

	    
	    //Check if all mandatory fields are included
	    if (score.Amount != null && score.ScoreType != null) {
			String query = "INSERT INTO Score (ItemId, UserId, ScoreTypeId, Amount) "
					+ "VALUES (" + score.ItemId + ""
					+ ", (SELECT UserId FROM User WHERE WP_UserId = " + score.UserId + ")"
					+ ", ("
					+	"SELECT ScoreTypeId FROM ScoreType WHERE Name = '" + score.ScoreType + "'"
					+ ")"
					+ ", " + score.Amount + ")";
			String resource = executeQuery(query, "Insert");
			ResponseBuilder rBuild = Response.ok(resource);
	        return rBuild.build();
	    } else {
			ResponseBuilder rBuild = Response.status(Response.Status.BAD_REQUEST);
	        return rBuild.build();
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
	    if (changes.get("ScoreId") != null) {
	    	return "Prohibited change attempt";
	    }
	    
	    //Check if NOT NULL field is attempted to be changed to NULL
	    if ((changes.get("Amount") == null || !changes.get("Amount").isJsonNull())
	    		&& (changes.get("ScoreTypeId") == null || !changes.get("ScoreTypeId").isJsonNull())) {
		    String query = "UPDATE Score SET ";
		    
		    int keyCount = changes.entrySet().size();
		    int i = 1;
			for(Map.Entry<String, JsonElement> entry : changes.entrySet()) {
			    query += entry.getKey() + " = " + entry.getValue();
			    if (i < keyCount) {
			    	query += ", ";
			    }
			    i++;
			}
			query += " WHERE ScoreId = " + id;
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
		String resource = executeQuery("DELETE FROM Score WHERE ScoreId = " + id, "Delete");
		return resource;
	}
	

	//Get entry by id
	@Path("/{id}")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getEntry(@PathParam("id") int id) throws SQLException {
		String query = "SELECT * FROM (" +
				"SELECT \r\n" + 
				"	s.ScoreId as ScoreId,\r\n" + 
				"	s.Amount as Amount,\r\n" + 
				"   s.ItemId as ItemId,\r\n" + 
				"   s.UserId as UserId,\r\n" + 
				"	st.ScoreTypeId as ScoreTypeId,\r\n" + 
				"    st.Name as ScoreType,\r\n" + 
				"    st.Rate as Rate,\r\n" + 
				"    s.Timestamp as Timestamp\r\n" + 
				"FROM Score s\r\n" + 
				"JOIN ScoreType st\r\n" + 
				"ON s.ScoreTypeId = st.ScoreTypeId) a\r\n" + 
				"WHERE ScoreId = " + id;
		String resource = executeQuery(query, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}

}