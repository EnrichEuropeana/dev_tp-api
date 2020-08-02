package responses;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import objects.CompletionStatus;
import objects.FieldMapping;
import objects.Item;
import objects.ItemPageData;
import objects.Language;
import objects.Property;

@Path("/itemPage")
public class ItemPageDataResponse {

	//Get entry by id
	@Path("/{id}")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getData(@PathParam("id") int id) throws SQLException, ParseException, IOException {
		ItemPageData data = new ItemPageData();
        Gson gson = new Gson();
        
		String propertyQuery = "SELECT " +
						"			p.PropertyId, " +
						"			p.Value AS PropertyValue, " +
						"			pt.Name AS PropertyType,\r\n" + 
						"			null AS PropertyTypeId,\r\n" + 
						"			null AS Motivation,\r\n" + 
						"			null AS MotivationId,\r\n" + 
						"			null AS Editable,\r\n" + 
						"			null AS X_Coord,\r\n" + 
						"			null AS Y_Coord,\r\n" + 
						"			null AS Width,\r\n" + 
						"			null AS Height,\r\n" + 
						"			null AS PropertyDescription,\r\n" + 
						"			null AS PropertyEditable\r\n" + 
						"		FROM " + 
						"			Property p " +
						"		LEFT JOIN " + 
						"			PropertyType pt ON p.PropertyTypeId = pt.PropertyTypeId " +
						"		WHERE pt.Name = 'Category'";
		String propertyData = PropertyResponse.executeQuery(propertyQuery, "Select");		
		Type propertyType = new TypeToken<List<Property>>(){}.getType();
		List<Property> categories = gson.fromJson(propertyData, propertyType);
		data.Categories = categories;
        
		String completionStatusQuery = "SELECT * FROM CompletionStatus";
		String completionStatusData = CompletionStatusResponse.executeQuery(completionStatusQuery, "Select");		
		Type completionStatusType = new TypeToken<List<CompletionStatus>>(){}.getType();
		List<CompletionStatus> completionStatus = gson.fromJson(completionStatusData, completionStatusType);
		data.CompletionStatus = completionStatus;
        
		String languageQuery = "SELECT * FROM Language";
		String languageData = LanguageResponse.executeQuery(languageQuery, "Select");		
		Type languageType = new TypeToken<List<Language>>(){}.getType();
		List<Language> language = gson.fromJson(languageData, languageType);
		data.Languages = language;
        
		String fieldMappingQuery = "SELECT * FROM FieldMapping";
		String fieldMappingData = FieldMappingResponse.executeQuery(fieldMappingQuery, "Select");		
		Type fieldMappingType = new TypeToken<List<FieldMapping>>(){}.getType();
		List<FieldMapping> fieldMapping = gson.fromJson(fieldMappingData, fieldMappingType);
		data.FieldMappings = fieldMapping;
		
		String itemImageQuery = "SELECT i.ItemId, i.ImageLink, c.ColorCode AS CompletionStatusColorCode " +
								"FROM Item i " + 
								"JOIN CompletionStatus c ON i.CompletionStatusId = c.CompletionStatusId " +
								"WHERE i.StoryId = " + id +
								" ORDER BY i.OrderIndex ASC";
		String itemImageData = getItemImages(itemImageQuery);		
		Type itemType = new TypeToken<List<Item>>(){}.getType();
		List<Item> itemImages = gson.fromJson(itemImageData, itemType);
		data.ItemImages = itemImages;

	    Gson gsonBuilder = new GsonBuilder().create();
	    String result = gsonBuilder.toJson(data);
		ResponseBuilder rBuild = Response.ok(result);
		
        return rBuild.build();
	}
	
	public static String getItemImages(String query) throws SQLException{
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
				Class.forName("com.mysql.jdbc.Driver");
				
				   // Open a connection
				   conn = DriverManager.getConnection(DB_URL, USER, PASS);
				   // Execute SQL query
				   stmt = conn.createStatement();
			   try {
				   rs = stmt.executeQuery(query);
				   
				   // Extract data from result set
				   while(rs.next()){
				      //Retrieve by column name
					  Item item = new Item();
					  item.setItemId(rs.getInt("ItemId"));
					  item.setImageLink(rs.getString("ImageLink"));
					  item.setCompletionStatusColorCode(rs.getString("CompletionStatusColorCode"));
					  itemList.add(item);
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
	    
	    String result = gsonBuilder.toJson(itemList);
	    return result;
	}
}
