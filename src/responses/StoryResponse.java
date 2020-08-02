package responses;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import objects.ApiKey;
import objects.Item;
import objects.Place;
import objects.Story;
import objects.Transcription;

import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import javafx.util.Pair;

@Path("/stories")
public class StoryResponse {


	public String executeQuery(String query, String type) throws SQLException{
		   List<Story> storyList = new ArrayList<Story>();;
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
		   stmt.execute("SET group_concat_max_len = 10000000;");
		   rs = stmt.executeQuery(query);

		   // Extract data from result set
		   while(rs.next()){
			  Story story = new Story();
			  story.setStoryId(rs.getInt("StoryId")); 
			  story.setdcTitle(rs.getString("StorydcTitle"));
			  story.setdcDescription(rs.getString("StorydcDescription"));
			  story.setedmLandingPage(rs.getString("StoryedmLandingPage"));
			  story.setExternalRecordId(rs.getString("StoryExternalRecordId"));
			  story.setPlaceName(rs.getString("StoryPlaceName"));
			  story.setPlaceLatitude(rs.getFloat("StoryPlaceLatitude"));
			  story.setPlaceLongitude(rs.getFloat("StoryPlaceLongitude"));
			  story.setPlaceZoom(rs.getString("StoryPlaceZoom"));
			  story.setPlaceLink(rs.getString("StoryPlaceLink"));
			  story.setPlaceComment(rs.getString("StoryPlaceComment"));
			  story.setPlaceUserId(rs.getInt("StoryPlaceUserId"));
			  story.setPlaceUserGenerated(rs.getString("StoryPlaceUserGenerated"));
			  story.setdcCreator(rs.getString("StorydcCreator"));
			  story.setdcSource(rs.getString("StoryedmRights"));
			  story.setdcSource(rs.getString("StorydcSource"));
			  story.setedmCountry(rs.getString("StoryedmCountry"));
			  story.setedmDataProvider(rs.getString("StoryedmDataProvider"));
			  story.setedmAgent(rs.getString("StoryedmAgent"));
			  story.setedmProvider(rs.getString("StoryedmProvider"));
			  story.setedmYear(rs.getString("StoryedmYear"));
			  story.setdcPublisher(rs.getString("StorydcPublisher"));
			  story.setdcCoverage(rs.getString("StorydcCoverage"));
			  story.setdcDate(rs.getString("StorydcDate"));
			  story.setdcType(rs.getString("StorydcType"));
			  story.setdcRelation(rs.getString("StorydcRelation"));
			  story.setdctermsMedium(rs.getString("StorydctermsMedium"));
			  story.setdctermsProvenance(rs.getString("StorydctermsProvenance"));
			  story.setedmDatasetName(rs.getString("StoryedmDatasetName"));
			  story.setdcContributor(rs.getString("StorydcContributor"));
			  story.setdcIdentifier(rs.getString("StorydcIdentifier"));
			  story.setedmRights(rs.getString("StoryedmRights"));
			  story.setedmBegin(rs.getString("StoryedmBegin"));
			  story.setedmEnd(rs.getString("StoryedmEnd"));
			  story.setedmIsShownAt(rs.getString("StoryedmIsShownAt"));
			  story.setdcRights(rs.getString("StorydcRights"));
			  story.setdcLanguage(rs.getString("StorydcLanguage"));
			  story.setedmLanguage(rs.getString("StoryedmLanguage"));
			  story.setProjectId(rs.getInt("StoryProjectId"));
			  story.setSummary(rs.getString("StorySummary"));
			  story.setParentStory(rs.getInt("StoryParentStory"));
			  story.setSearchText(rs.getString("StorySearchText"));
			  story.setDateStart(rs.getTimestamp("StoryDateStart"));
			  story.setDateEnd(rs.getTimestamp("StoryDateEnd"));
			  story.setOrderIndex(rs.getInt("StoryOrderIndex"));
			  story.setPreviewImage(rs.getString("StoryPreviewImage"));
			   
			  
				  // Iterate through Items of the Story
				  List<Item> ItemList = new ArrayList<Item>();
				  if (rs.getString("ItemId") != null) {
					  String[] ItemIds = rs.getString("ItemId").split("§~§");
					  String[] ItemTitles = rs.getString("Title").split("§~§");
					  String[] ItemCompletionStatusColorCodes = rs.getString("CompletionStatusColorcode").split("§~§");
					  String[] ItemCompletionStatusNames = rs.getString("CompletionStatusName").split("§~§");
					  String[] ItemCompletionStatusIds = rs.getString("CompletionStatusId").split("§~§");
					  String[] ItemOldItemIds = null;
					  if (rs.getString("OldItemId") != null) {
						  ItemOldItemIds = rs.getString("OldItemId").split("§~§");
					  }
					  String[] ItemDescriptions = null;
					  if (rs.getString("Description") != null) {
						  ItemDescriptions = rs.getString("Description").split("§~§");
					  }
					  String[] ItemDateStarts = null;
					  if (rs.getString("DateStart") != null) {
						  ItemDateStarts = rs.getString("DateStart").split("§~§");
					  }
					  String[] ItemDateEnds = null;
					  if (rs.getString("DateEnd") != null) {
						  ItemDateEnds = rs.getString("DateEnd").split("§~§");
					  }
					  String[] ItemDatasetIds = null;
					  if (rs.getString("DatasetId") != null) {
						  ItemDatasetIds = rs.getString("DatasetId").split("§~§");
					  }
					  String[] ItemImageLinks = rs.getString("ImageLink").split("§~§");
					  String[] ItemOrderIndexs = rs.getString("OrderIndex").split("§~§");
					  String[] ItemTimestamps = rs.getString("Timestamp").split("§~§");
	
					  // Initialize lists split by Stories
					  String[] PlaceIdList = new String[ItemIds.length];
					  String[] PlaceNameList = new String[ItemIds.length];
					  String[] PlaceLatitudeList = new String[ItemIds.length];
					  String[] PlaceLongitudeList = new String[ItemIds.length];
					  String[] PlaceLinkList = new String[ItemIds.length];
					  String[] PlaceZoomList = new String[ItemIds.length];
					  String[] PlaceCommentList = new String[ItemIds.length];
					  String[] PlaceUserIdList = new String[ItemIds.length];
					  String[] PlaceUserGeneratedList = new String[ItemIds.length];
					  if (rs.getString("PlaceId") != null) {
						  PlaceIdList = rs.getString("PlaceId").split("§~§", -1);
					  }
					  if (rs.getString("PlaceName") != null && rs.getString("PlaceName") != "NULL") {
						  PlaceNameList = rs.getString("PlaceName").split("§~§", -1);
					  }
					  if (rs.getString("PlaceLatitude") != null && rs.getString("PlaceLatitude") != "NULL") {
						  PlaceLatitudeList = rs.getString("PlaceLatitude").split("§~§", -1);
					  }
					  if (rs.getString("PlaceLongitude") != null && rs.getString("PlaceLongitude") != "NULL") {
						  PlaceLongitudeList = rs.getString("PlaceLongitude").split("§~§", -1);
					  }
					  if (rs.getString("PlaceLink") != null && rs.getString("PlaceLink") != "NULL") {
						  PlaceLinkList = rs.getString("PlaceLink").split("§~§", -1);
					  }
					  if (rs.getString("PlaceZoom") != null && rs.getString("PlaceZoom") != "NULL") {
						  PlaceZoomList = rs.getString("PlaceZoom").split("§~§", -1);
					  }
					  if (rs.getString("PlaceComment") != null && rs.getString("PlaceComment") != "NULL") {
						  PlaceCommentList = rs.getString("PlaceComment").split("§~§", -1);
					  }
					  if (rs.getString("PlaceUserId") != null && rs.getString("PlaceUserId") != "NULL") {
						  PlaceUserIdList = rs.getString("PlaceUserId").split("§~§", -1);
					  }
					  if (rs.getString("PlaceUserGenerated") != null && rs.getString("PlaceUserGenerated") != "NULL") {
						  PlaceUserGeneratedList = rs.getString("PlaceUserGenerated").split("§~§", -1);
					  }
					  
	
					  for (int j = 0; j < ItemIds.length; j++) {
						  Item item = new Item();
						  item.setItemId(Integer.parseInt(ItemIds[j]));
						  item.setTitle(ItemTitles[j]);
						  item.setCompletionStatusColorCode(ItemCompletionStatusColorCodes[j]);
						  item.setCompletionStatusName(ItemCompletionStatusNames[j]);
						  item.setCompletionStatusId(Integer.parseInt(ItemCompletionStatusIds[j]));
						  if (!ItemOldItemIds[j].contentEquals("NULL")) {
							  item.setOldItemId(Integer.parseInt(ItemOldItemIds[j]));
						  }
						  if (!ItemDescriptions[j].contentEquals("NULL")) {
							  item.setDescription(ItemDescriptions[j]);
						  }
						  if (!ItemDateStarts[j].contentEquals("NULL")) {
							  try {
						            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						            Date date = formatter.parse(ItemDateStarts[j]);
						            Timestamp timeStampDate = new Timestamp(date.getTime());
						            item.setDateStart(timeStampDate);
								} catch (ParseException e) {
								    System.out.println("Exception :" + e);
								    return "Exception :" + e;
								}
						  }
						  if (!ItemDateEnds[j].contentEquals("NULL")) {
							  try {
						            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						            Date date = formatter.parse(ItemDateEnds[j]);
						            Timestamp timeStampDate = new Timestamp(date.getTime());
						            item.setDateEnd(timeStampDate);
						        } catch (ParseException e) {
						            System.out.println("Exception :" + e);
						            return "Exception :" + e;
						        }
						  }
						  if (!ItemDatasetIds[j].contentEquals("NULL")) {
							  item.setDatasetId(Integer.parseInt(ItemDatasetIds[j]));
						  }
						  item.setImageLink(ItemImageLinks[j]);
						  item.setOrderIndex(Integer.parseInt(ItemOrderIndexs[j]));
					      item.setTimestamp(ItemTimestamps[j]);
					            
						  //Add Places
						  List<Place> PlaceList = new ArrayList<Place>();
						  if (rs.getString("PlaceId") != null) {
							  // Intitialize lists grouped by items
							  String[] PlaceIds = PlaceIdList[j].split("&~&", -1);
							  String[] PlaceNames = PlaceNameList[j].split("&~&", -1);
							  String[] PlaceLatitudes = PlaceLatitudeList[j].split("&~&", -1);
							  String[] PlaceLongitudes = PlaceLongitudeList[j].split("&~&", -1);
							  String[] PlaceLink = PlaceLinkList[j].split("&~&", -1);
							  String[] PlaceZoom = PlaceZoomList[j].split("&~&", -1);
							  String[] PlaceComment = PlaceCommentList[j].split("&~&", -1);
							  String[] PlaceUserGenerated = PlaceUserGeneratedList[j].split("&~&", -1);
							  for (int i = 0; i < PlaceIds.length; i++) {
								  if (!isNumeric(PlaceIds[i])) {
									  continue;
								  }
								  Place place = new Place();
								  place.setPlaceId(Integer.parseInt(PlaceIds[i]));
								  place.setName(PlaceNames[i]);
								  place.setLatitude(Float.parseFloat(PlaceLatitudes[i]));
								  place.setLongitude(Float.parseFloat(PlaceLongitudes[i]));
								  if (PlaceLink[i] != null && !PlaceLink[i].equals("NULL")) {
									  place.setLink(PlaceLink[i]);
								  }
								  if (PlaceZoom[i] != null && !PlaceZoom[i].equals("NULL")) {
									  place.setZoom(Integer.parseInt(PlaceZoom[i]));
								  }
								  if (PlaceComment[i] != null && !PlaceComment[i].equals("NULL")) {
									  place.setComment(PlaceComment[i]);
								  }
								  PlaceList.add(place);
							  }
						  }
						
						  item.setPlaces(PlaceList);
						  ItemList.add(item);
					  }
				  story.setItems(ItemList);
			  }
			  storyList.add(story);
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
	    String result = gsonBuilder.toJson(storyList);
	    return result;
	}
	


	public List<Story> getStoryData(String query) throws SQLException{
		   List<Story> storyList = new ArrayList<Story>();
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
		   stmt.execute("SET group_concat_max_len = 1000000;");
		   rs = stmt.executeQuery(query);

		   // Extract data from result set
		   while(rs.next()){
			  Story story = new Story();
			  story.setStoryId(rs.getInt("StoryId")); 
			  story.setdcTitle(rs.getString("StorydcTitle"));
			  story.setdcDescription(rs.getString("StorydcDescription"));
			  story.setedmLandingPage(rs.getString("StoryedmLandingPage"));
			  story.setExternalRecordId(rs.getString("StoryExternalRecordId"));
			  story.setPlaceName(rs.getString("StoryPlaceName"));
			  story.setPlaceLatitude(rs.getFloat("StoryPlaceLatitude"));
			  story.setPlaceLongitude(rs.getFloat("StoryPlaceLongitude"));
			  story.setPlaceZoom(rs.getString("StoryPlaceZoom"));
			  story.setPlaceLink(rs.getString("StoryPlaceLink"));
			  story.setPlaceComment(rs.getString("StoryPlaceComment"));
			  story.setPlaceUserId(rs.getInt("StoryPlaceUserId"));
			  story.setPlaceUserGenerated(rs.getString("StoryPlaceUserGenerated"));
			  story.setdcCreator(rs.getString("StorydcCreator"));
			  story.setdcSource(rs.getString("StoryedmRights"));
			  story.setdcSource(rs.getString("StorydcSource"));
			  story.setedmCountry(rs.getString("StoryedmCountry"));
			  story.setedmDataProvider(rs.getString("StoryedmDataProvider"));
			  story.setedmAgent(rs.getString("StoryedmAgent"));
			  story.setedmProvider(rs.getString("StoryedmProvider"));
			  story.setedmYear(rs.getString("StoryedmYear"));
			  story.setdcPublisher(rs.getString("StorydcPublisher"));
			  story.setdcCoverage(rs.getString("StorydcCoverage"));
			  story.setdcDate(rs.getString("StorydcDate"));
			  story.setdcType(rs.getString("StorydcType"));
			  story.setdcRelation(rs.getString("StorydcRelation"));
			  story.setdctermsMedium(rs.getString("StorydctermsMedium"));
			  story.setdctermsProvenance(rs.getString("StorydctermsProvenance"));
			  story.setedmDatasetName(rs.getString("StoryedmDatasetName"));
			  story.setdcContributor(rs.getString("StorydcContributor"));
			  story.setdcIdentifier(rs.getString("StorydcIdentifier"));
			  story.setedmRights(rs.getString("StoryedmRights"));
			  story.setedmBegin(rs.getString("StoryedmBegin"));
			  story.setedmEnd(rs.getString("StoryedmEnd"));
			  story.setedmIsShownAt(rs.getString("StoryedmIsShownAt"));
			  story.setdcRights(rs.getString("StorydcRights"));
			  story.setdcLanguage(rs.getString("StorydcLanguage"));
			  story.setedmLanguage(rs.getString("StoryedmLanguage"));
			  story.setProjectId(rs.getInt("StoryProjectId"));
			  story.setSummary(rs.getString("StorySummary"));
			  story.setParentStory(rs.getInt("StoryParentStory"));
			  story.setSearchText(rs.getString("StorySearchText"));
			  story.setDateStart(rs.getTimestamp("StoryDateStart"));
			  story.setDateEnd(rs.getTimestamp("StoryDateEnd"));
			  story.setOrderIndex(rs.getInt("StoryOrderIndex"));
			  story.setPreviewImage(rs.getString("StoryPreviewImage"));
			  
			  storyList.add(story);
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
    return storyList;
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


	//GET entries
	
	@Produces("application/json;charset=utf-8")
	@GET
	public Response search(@DefaultValue("true") @QueryParam("items") String showItems, @Context UriInfo uriInfo, String body) throws SQLException {

		String query = "";
		if (showItems.contentEquals("false")) {
			query = "SELECT s.StoryId as StoryId" + 
					", s.`dc:Title` as StorydcTitle" +
					", s.`dc:description` as StorydcDescription" +
					", s.`edm:landingPage` as StoryedmLandingPage" +
					", s.ExternalRecordId as StoryExternalRecordId" +
					", s.PlaceName as StoryPlaceName" +
					", s.PlaceLatitude as StoryPlaceLatitude" +
					", s.PlaceLongitude as StoryPlaceLongitude" +
					"	, s.PlaceZoom as StoryPlaceZoom \r\n" + 
					"	, s.PlaceLink as StoryPlaceLink \r\n" + 
					"	, s.PlaceComment as StoryPlaceComment \r\n" + 
					"	, s.PlaceUserId as StoryPlaceUserId \r\n" + 
					", s.PlaceUserGenerated as StoryPlaceUserGenerated" +
					", s.`dc:creator` as StorydcCreator" +
					", s.`dc:source` as StorydcSource" +
					", s.`edm:country` as StoryedmCountry" +
					", s.`edm:dataProvider` as StoryedmDataProvider" +
					", s.`edm:agent` as StoryedmAgent" +
					", s.`edm:provider` as StoryedmProvider" +
					", s.`edm:year` as StoryedmYear" +
					", s.`dc:publisher` as StorydcPublisher" +
					", s.`dc:coverage` as StorydcCoverage" +
					", s.`dc:date` as StorydcDate" +
					", s.`dc:type` as StorydcType" +
					", s.`dc:relation` as StorydcRelation" +
					", s.`dcterms:medium` as StorydctermsMedium" +
					", s.`dcterms:provenance` as StorydctermsProvenance" +
					", s.`edm:datasetName` as StoryedmDatasetName" +
					", s.`dc:contributor` as StorydcContributor" +
					", s.`dc:identifier` as StorydcIdentifier" +
					", s.`edm:rights` as StoryedmRights" +
					", s.`edm:begin` as StoryedmBegin" +
					", s.`edm:end` as StoryedmEnd" +
					", s.`edm:isShownAt` as StoryedmIsShownAt" +
					", s.`dc:rights` as StorydcRights" +
					", s.`dc:language` as StorydcLanguage" +
					", s.`edm:language` as StoryedmLanguage" +
					", s.ProjectId as StoryProjectId" +
					", s.Summary as StorySummary" +
					", s.ParentStory as StoryParentStory" +
					", s.SearchText as StorySearchText" +
					", s.DateStart as StoryDateStart" +
					", s.DateEnd as StoryDateEnd" +
					", s.OrderIndex as StoryOrderIndex " +
					", s.PreviewImage as StoryPreviewImage " +
					"FROM Story s " +
					"WHERE 1";
			MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
			
			for(String key : queryParams.keySet()){
				if (key == "items") {
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
			List<Story> storyList = getStoryData(query);
		    Gson gsonBuilder = new GsonBuilder().create();
		    String response = gsonBuilder.toJson(storyList);
			ResponseBuilder rBuild = Response.ok(response);
			//ResponseBuilder rBuild = Response.ok(query);
	        return rBuild.build();
		}
		else {
			query = "SELECT * FROM " +
					"(SELECT s.StoryId as StoryId" + 
					", s.`dc:Title` as StorydcTitle" +
					", s.`dc:description` as StorydcDescription" +
					", s.`edm:landingPage` as StoryedmLandingPage" +
					", s.ExternalRecordId as StoryExternalRecordId" +
					", s.PlaceName as StoryPlaceName" +
					", s.PlaceLatitude as StoryPlaceLatitude" +
					", s.PlaceLongitude as StoryPlaceLongitude" +
					"	, s.PlaceZoom as StoryPlaceZoom \r\n" + 
					"	, s.PlaceLink as StoryPlaceLink \r\n" + 
					"	, s.PlaceComment as StoryPlaceComment \r\n" + 
					"	, s.PlaceUserId as StoryPlaceUserId \r\n" + 
					", s.PlaceUserGenerated as StoryPlaceUserGenerated" +
					", s.`dc:creator` as StorydcCreator" +
					", s.`dc:source` as StorydcSource" +
					", s.`edm:country` as StoryedmCountry" +
					", s.`edm:dataProvider` as StoryedmDataProvider" +
					", s.`edm:agent` as StoryedmAgent" +
					", s.`edm:provider` as StoryedmProvider" +
					", s.`edm:year` as StoryedmYear" +
					", s.`dc:publisher` as StorydcPublisher" +
					", s.`dc:coverage` as StorydcCoverage" +
					", s.`dc:date` as StorydcDate" +
					", s.`dc:type` as StorydcType" +
					", s.`dc:relation` as StorydcRelation" +
					", s.`dcterms:medium` as StorydctermsMedium" +
					", s.`dcterms:provenance` as StorydctermsProvenance" +
					", s.`edm:datasetName` as StoryedmDatasetName" +
					", s.`dc:contributor` as StorydcContributor" +
					", s.`dc:identifier` as StorydcIdentifier" +
					", s.`edm:rights` as StoryedmRights" +
					", s.`edm:begin` as StoryedmBegin" +
					", s.`edm:end` as StoryedmEnd" +
					", s.`edm:isShownAt` as StoryedmIsShownAt" +
					", s.`dc:rights` as StorydcRights" +
					", s.`dc:language` as StorydcLanguage" +
					", s.`edm:language` as StoryedmLanguage" +
					", s.ProjectId as StoryProjectId" +
					", s.Summary as StorySummary" +
					", s.ParentStory as StoryParentStory" +
					", s.SearchText as StorySearchText" +
					", s.DateStart as StoryDateStart" +
					", s.DateEnd as StoryDateEnd" +
					", s.OrderIndex as StoryOrderIndex" +
					", s.PreviewImage as StoryPreviewImage" +
					", group_concat(i.ItemId SEPARATOR '§~§') as ItemId" +
					", group_concat(IFNULL(i.Title, 'NULL') SEPARATOR '§~§') as Title" +
					", group_concat(i.CompletionStatusColorCode SEPARATOR '§~§') as CompletionStatusColorCode" +
					", group_concat(i.CompletionStatusName SEPARATOR '§~§') as CompletionStatusName" +
					", group_concat(i.CompletionStatusId SEPARATOR '§~§') as CompletionStatusId" +
					", group_concat(IFNULL(i.OldItemId, 'NULL') SEPARATOR '§~§') as OldItemId" +
					", group_concat(IFNULL(i.Description, 'NULL') SEPARATOR '§~§') as Description" +
					", group_concat(IFNULL(i.DateStart, 'NULL') SEPARATOR '§~§') as DateStart" +
					", group_concat(IFNULL(i.DateEnd, 'NULL') SEPARATOR '§~§') as DateEnd" +
					", group_concat(IFNULL(i.DatasetId, 'NULL') SEPARATOR '§~§') as DatasetId" +
					", group_concat(IFNULL(i.ImageLink, 'NULL') SEPARATOR '§~§') as ImageLink" +
					", group_concat(IFNULL(i.OrderIndex, 'NULL') SEPARATOR '§~§') as OrderIndex" +
					", group_concat(IFNULL(i.Timestamp, 'NULL') SEPARATOR '§~§') as Timestamp" +
					", group_concat(IFNULL(c.PlaceId, 'NULL') SEPARATOR '§~§') as PlaceId " +
					", group_concat(IFNULL(c.PlaceName, 'NULL') SEPARATOR '§~§') as PlaceName " +
					", group_concat(IFNULL(c.PlaceLatitude, 'NULL') SEPARATOR '§~§') as PlaceLatitude " +
					", group_concat(IFNULL(c.PlaceLongitude, 'NULL') SEPARATOR '§~§') as PlaceLongitude " +
					", group_concat(IFNULL(c.PlaceLink, 'NULL') SEPARATOR '§~§') as PlaceLink " +
					", group_concat(IFNULL(c.PlaceZoom, 'NULL') SEPARATOR '§~§') as PlaceZoom " +
					", group_concat(IFNULL(c.PlaceComment, 'NULL') SEPARATOR '§~§') as PlaceComment " +
					", group_concat(IFNULL(c.PlaceUserId, 'NULL') SEPARATOR '§~§') as PlaceUserId " +
					", group_concat(IFNULL(c.PlaceUserGenerated, 'NULL') SEPARATOR '§~§') as PlaceUserGenerated " +
					"FROM " +
						"(" +
						"SELECT * " +
					    "FROM Item i " +
					    "LEFT JOIN ( " +
							"SELECT i.ItemId as CompletionStatusItemId" +
							", c.Name as CompletionStatusName " + 
							", c.ColorCode as CompletionStatusColorCode " + 
					        "FROM CompletionStatus c " +
					        "JOIN Item i " +
					        "ON i.CompletionStatusId = c.CompletionStatusId " +
					        ") c  " +
					        "ON i.ItemId = c.CompletionStatusItemId " +
						") i " +
					"LEFT JOIN " + 
					"(" + 
						"SELECT i.ItemId as ItemId" +
						", group_concat(IFNULL(pl.PlaceId, 'NULL') SEPARATOR '&~&') as PlaceId " +
						", group_concat(IFNULL(pl.Name, 'NULL') SEPARATOR '&~&') as PlaceName " +
						", group_concat(IFNULL(pl.Latitude, 'NULL') SEPARATOR '&~&') as PlaceLatitude " +
						", group_concat(IFNULL(pl.Longitude, 'NULL') SEPARATOR '&~&') as PlaceLongitude " +
						", group_concat(IFNULL(pl.Link, 'NULL') SEPARATOR '&~&') as PlaceLink " +
						", group_concat(IFNULL(pl.Zoom, 'NULL') SEPARATOR '&~&') as PlaceZoom " +
						", group_concat(IFNULL(pl.Comment, 'NULL') SEPARATOR '&~&') as PlaceComment " +
						", group_concat(IFNULL(pl.UserId, 'NULL') SEPARATOR '&~&') as PlaceUserId " +
						", group_concat(IFNULL(pl.UserGenerated + 0, 'NULL') SEPARATOR '&~&') as PlaceUserGenerated " +
						"FROM Item i " + 
						"LEFT JOIN Place pl on i.ItemId = pl.ItemId " +  
						"GROUP BY i.ItemId " +
					") c " + 
					"ON i.ItemId = c.ItemId " +
					"LEFT JOIN " + 
					"(" +
						"SELECT * " +
						"FROM Story " + 
					") s " +
					"ON i.StoryId = s.StoryId " +
					"WHERE 1";
			MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
			
			for(String key : queryParams.keySet()){
				if (key == "items") {
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
			query += " GROUP BY s.StoryId) s " +
					"ORDER BY RIGHT(ItemId, 6) + 0 asc, LEFT(ItemId,length(ItemId)-6) + 0 asc ";
			String resource = executeQuery(query, "Select");
			ResponseBuilder rBuild = Response.ok(resource);
			//ResponseBuilder rBuild = Response.ok(query);
	        return rBuild.build();
		}
	}

	//Add new entry
	
	@POST
	public Response add(@Context UriInfo uriInfo, String body, @Context HttpHeaders headers) throws SQLException {	
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
		
		JsonObject data = new JsonParser().parse(body).getAsJsonObject();
		JsonArray dataArray = data.getAsJsonObject().get("@graph").getAsJsonArray();
		List<String> fields = new ArrayList<String>();
		fields.add("dc:title");
		fields.add("dc:description");
		fields.add("dc:creator");
		fields.add("dc:source");
		fields.add("edm:country");
		fields.add("edm:dataProvider");
		fields.add("edm:agent");
		fields.add("edm:provider");
		fields.add("edm:rights");
		fields.add("edm:begin");
		fields.add("edm:end");
		fields.add("edm:isShownAt");
		fields.add("dc:rights");
		fields.add("dc:contributor");
		fields.add("dc:identifier");
		fields.add("edm:year");
		fields.add("dc:publisher");
		fields.add("dc:coverage");
		fields.add("dc:date");
		fields.add("dc:type");
		fields.add("dc:relation");
		fields.add("dcterms:medium");
		fields.add("dcterms:provenance");
		fields.add("edm:datasetName");
		boolean placeAdded = false;
	    int keyCount = dataArray.size();

		List<String> keys = new ArrayList<String>();
		List<String> values = new ArrayList<String>();

		for (int i = 0; i < keyCount; i++) {
			for(Map.Entry<String, JsonElement> entry : dataArray.get(i).getAsJsonObject().entrySet()) {
				if (fields.contains(entry.getKey())) {
					if (!entry.getValue().isJsonObject()) {
						if (!keys.contains(entry.getKey())) {
							keys.add(entry.getKey());
							values.add("\"" + entry.getValue().toString().replace(",", " | ").replaceAll("[\"{}\\[\\]]", "") + "\"");
						}
					}
					else {
						if (entry.getValue().getAsJsonObject().has("@value")) {
							if (!keys.contains(entry.getKey())) {
								keys.add(entry.getKey());
								values.add(entry.getValue().getAsJsonObject().get("@value").toString());
							}
						}
						else if (entry.getValue().getAsJsonObject().has("@id")) {
							if (!keys.contains(entry.getKey())) {
								keys.add(entry.getKey());
								values.add(entry.getValue().getAsJsonObject().get("@id").toString());
							}
						}
					}
				}
				else {
					if (entry.getKey().equals("@type") && entry.getValue().getAsString().equals("edm:Place") && placeAdded == false) {
						if (dataArray.get(i).getAsJsonObject().keySet().contains("geo:lat")
								&& dataArray.get(i).getAsJsonObject().keySet().contains("geo:long")) {
							if (!keys.contains("PlaceLatitude")) {
								keys.add("PlaceLatitude");
								keys.add("PlaceLongitude");
								values.add(dataArray.get(i).getAsJsonObject().get("geo:lat").toString());
								values.add(dataArray.get(i).getAsJsonObject().get("geo:long").toString());
							}
						}
						if (dataArray.get(i).getAsJsonObject().keySet().contains("skos:prefLabel")) {
							JsonArray placeName = new JsonArray();
							if (dataArray.get(i).getAsJsonObject().get("skos:prefLabel").isJsonArray()) {
								if (!keys.contains("PlaceName")) {
									placeName = dataArray.get(i).getAsJsonObject().get("skos:prefLabel").getAsJsonArray();
									for (int j = 0; j < placeName.size(); j++) {
										if (placeName.get(j) instanceof JsonObject && placeName.get(j).getAsJsonObject().get("@language").toString() == "en") {
											keys.add("PlaceName");
											values.add(placeName.get(j).getAsJsonObject().get("@value").toString());
										}
									}
								}
							}
						}
					}
				}
			}
		}

		keys.add("PlaceUserGenerated");
		values.add("0");
		
		String query = "";
		query += "INSERT INTO Story (";

		Iterator<String> keysIterator = keys.iterator();
	    while (keysIterator.hasNext()) {
			query += "`" + keysIterator.next() + "`";
	        if (keysIterator.hasNext()) {
	        	query += ", ";
	        }
		}
	    query += ") VALUES (";
		Iterator<String> valuesIterator = values.iterator();
	    while (valuesIterator.hasNext()) {
			query += valuesIterator.next();
	        if (valuesIterator.hasNext()) {
	        	query += ", ";
	        }
		}
	    query += ")";
		String resource = executeQuery(query, "Insert");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}

	//Edit entry by id
	@Path("/{id}")
	@POST
	public String update(@PathParam("id") int id, String body) throws SQLException {
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    JsonObject  changes = gson.fromJson(body, JsonObject.class);
	    
	    String query = "UPDATE Story SET ";
	    
	    int keyCount = changes.entrySet().size();
	    int i = 1;
		for(Map.Entry<String, JsonElement> entry : changes.entrySet()) {
		    query += entry.getKey() + " = " + entry.getValue();
		    if (i < keyCount) {
		    	query += ", ";
		    }
		    i++;
		}
		query += " WHERE StoryId = " + id;
		String resource = executeQuery(query, "Update");
		return resource;
	}

/*
	//Delete entry by id
	@Path("/{id}")
	@DELETE
	public String delete(@PathParam("id") int id) throws SQLException {
		String query =  "DELETE FROM Story " +
						"WHERE i.StoryId = " + id;
		String resource = executeQuery(query, "Delete");
		return resource;
	}
	*/
	

	//Get entry by id
	@Path("/{id}")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getEntry(@DefaultValue("true") @QueryParam("items") String showItems, @PathParam("id") int id, String body) throws SQLException {
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        
		String storyQuery = "SELECT " + 
				"        s.StoryId AS StoryId,\r\n" + 
				"            s.`dc:Title` AS StorydcTitle,\r\n" + 
				"            s.`dc:description` AS StorydcDescription,\r\n" + 
				"            s.`edm:landingPage` AS StoryedmLandingPage,\r\n" + 
				"            s.ExternalRecordId AS StoryExternalRecordId,\r\n" + 
				"            s.PlaceName AS StoryPlaceName,\r\n" + 
				"            s.PlaceLatitude AS StoryPlaceLatitude,\r\n" + 
				"            s.PlaceLongitude AS StoryPlaceLongitude,\r\n" + 
				"			 s.PlaceZoom as StoryPlaceZoom, \r\n" + 
				"			 s.PlaceLink as StoryPlaceLink, \r\n" + 
				"			 s.PlaceComment as StoryPlaceComment, \r\n" + 
				"			 s.PlaceUserId as StoryPlaceUserId, \r\n" + 
				"            s.PlaceUserGenerated AS StoryPlaceUserGenerated,\r\n" + 
				"            s.`dc:creator` AS StorydcCreator,\r\n" + 
				"            s.`dc:source` AS StorydcSource,\r\n" + 
				"            s.`edm:country` AS StoryedmCountry,\r\n" + 
				"            s.`edm:dataProvider` AS StoryedmDataProvider,\r\n" + 
				"            s.`edm:agent` AS StoryedmAgent,\r\n" + 
				"            s.`edm:provider` AS StoryedmProvider,\r\n" + 
				"            s.`edm:year` AS StoryedmYear,\r\n" + 
				"            s.`dc:publisher` AS StorydcPublisher,\r\n" + 
				"            s.`dc:coverage` AS StorydcCoverage,\r\n" + 
				"            s.`dc:date` AS StorydcDate,\r\n" + 
				"            s.`dc:type` AS StorydcType,\r\n" + 
				"            s.`dc:relation` AS StorydcRelation,\r\n" + 
				"            s.`dcterms:medium` AS StorydctermsMedium,\r\n" + 
				"            s.`dcterms:provenance` AS StorydctermsProvenance,\r\n" + 
				"            s.`edm:datasetName` AS StoryedmDatasetName,\r\n" + 
				"            s.`dc:contributor` AS StorydcContributor,\r\n" + 
				"            s.`dc:identifier` AS StorydcIdentifier,\r\n" + 
				"            s.`edm:rights` AS StoryedmRights,\r\n" + 
				"            s.`edm:begin` AS StoryedmBegin,\r\n" + 
				"            s.`edm:end` AS StoryedmEnd,\r\n" + 
				"			 s.`edm:isShownAt` as StoryedmIsShownAt,\r\n" +
				"			 s.`dc:rights` as StorydcRights,\r\n" +
				"			 s.`dc:language` as StorydcLanguage,\r\n" +
				"			 s.`edm:language` as StoryedmLanguage,\r\n" +
				"            s.ProjectId AS StoryProjectId,\r\n" + 
				"            s.Summary AS StorySummary,\r\n" + 
				"            s.ParentStory AS StoryParentStory,\r\n" + 
				"            s.SearchText AS StorySearchText,\r\n" + 
				"            s.DateStart AS StoryDateStart,\r\n" + 
				"            s.DateEnd AS StoryDateEnd,\r\n" + 
				"            s.OrderIndex AS StoryOrderIndex, " + 
				"            s.PreviewImage AS StoryPreviewImage" + 
				"	FROM\r\n" + 
				"		Story s " +
				"	WHERE StoryId = " + id;
		List<Story> storyList = getStoryData(storyQuery);
		if (showItems.contentEquals("false")) {
		    Gson gsonBuilder = new GsonBuilder().create();
		    String response = gsonBuilder.toJson(storyList);
			ResponseBuilder rBuild = Response.ok(response);
			//ResponseBuilder rBuild = Response.ok(storyQuery);
	        return rBuild.build();
		}
		else {
			String itemQuery = "SELECT \r\n" + 
							"			i.ItemId,  \r\n" + 
							"			i.Title,  \r\n" + 
							"			i.CompletionStatusId,\r\n" + 
							"			c.Name as CompletionStatusName,\r\n" + 
							"			c.ColorCode as CompletionStatusColorCode, \r\n" + 
							"			i.OldItemId, \r\n" + 
							"			i.Description,  \r\n" + 
							"			i.DescriptionLanguage,  \r\n" + 
							"			i.DateStart, \r\n" + 
							"			i.DateEnd,  \r\n" + 
							"			i.DateStartDisplay, \r\n" + 
							"			i.DateEndDisplay,  \r\n" + 
							"			i.DatasetId,  \r\n" + 
							"			i.ImageLink, \r\n" + 
							"			i.OrderIndex,  \r\n" + 
							"			i.Timestamp, \r\n" + 
							"			i.LockedTime,  \r\n" + 
							"			i.LockedUser, \r\n" + 
							"			i.Manifest, \r\n" + 
							"			GROUP_CONCAT(IFNULL(pl.PlaceId, 'NULL')\r\n" + 
							"				SEPARATOR '&~&') AS PlaceId,\r\n" + 
							"			GROUP_CONCAT(IFNULL(pl.Name, 'NULL')\r\n" + 
							"				SEPARATOR '&~&') AS PlaceName,\r\n" + 
							"			GROUP_CONCAT(IFNULL(pl.Latitude, 'NULL')\r\n" + 
							"				SEPARATOR '&~&') AS PlaceLatitude,\r\n" + 
							"			GROUP_CONCAT(IFNULL(pl.Longitude, 'NULL')\r\n" + 
							"				SEPARATOR '&~&') AS PlaceLongitude,\r\n" + 
							"			GROUP_CONCAT(IFNULL(pl.Link, 'NULL')\r\n" + 
							"				SEPARATOR '&~&') AS PlaceLink,\r\n" + 
							"			GROUP_CONCAT(IFNULL(pl.Zoom, 'NULL')\r\n" + 
							"				SEPARATOR '&~&') AS PlaceZoom,\r\n" + 
							"			GROUP_CONCAT(IFNULL(pl.Comment, 'NULL')\r\n" + 
							"				SEPARATOR '&~&') AS PlaceComment,\r\n" + 
							"			GROUP_CONCAT(IFNULL(pl.WikidataName, 'NULL')\r\n" + 
							"				SEPARATOR '&~&') AS PlaceWikidataName,\r\n" + 
							"			GROUP_CONCAT(IFNULL(pl.WikidataId, 'NULL')\r\n" + 
							"				SEPARATOR '&~&') AS PlaceWikidataId,\r\n" + 
							"			GROUP_CONCAT(IFNULL(pl.Comment, 'NULL')\r\n" + 
							"				SEPARATOR '&~&') AS PlaceComment,\r\n" + 
							"			GROUP_CONCAT(IFNULL(pl.UserId, 'NULL')\r\n" + 
							"				SEPARATOR '&~&') AS PlaceUserId,\r\n" + 
							"			GROUP_CONCAT(IFNULL(pl.UserGenerated + 0, 'NULL')\r\n" + 
							"				SEPARATOR '&~&') AS PlaceUserGenerated\r\n" + 
							"	FROM\r\n" + 
							"		(SELECT * FROM Item WHERE StoryId = " + id + ") i \r\n" + 
							"	JOIN \r\n" + 
							"	CompletionStatus c\r\n" + 
							"	ON i.CompletionStatusId = c.CompletionStatusId\r\n" + 
							"	LEFT JOIN Place pl \r\n" + 
							"	ON i.ItemId = pl.ItemId\r\n" + 
							"	GROUP BY i.ItemId " +
							"	ORDER BY OrderIndex";
			String itemData = ItemResponse.executeQuery(itemQuery, "Select");				
			Type itemType = new TypeToken<List<Item>>(){}.getType();
			List<Item> items = gson.fromJson(itemData, itemType);
			
			storyList.get(0).setItems(items);
			
			ResponseBuilder rBuild = Response.ok(storyList);
			//ResponseBuilder rBuild = Response.ok(showItems);
	        return rBuild.build();
		}
	}


	//Add new entry
	@Path("/update")
	@POST
	public static Response solrUpdate() throws SQLException, IOException {
	    HttpURLConnection con = null;
	    BufferedReader in = null;
		try {
		    URL storySolr = new URL("http://fresenia.man.poznan.pl:8983/solr/Stories/dataimport?command=delta-import&commit=true");
		    con = (HttpURLConnection) storySolr.openConnection();
		    con.setRequestMethod("GET");
		    in = new BufferedReader(
		    new InputStreamReader(con.getInputStream()));
		    String inputLine;
		    StringBuffer content = new StringBuffer();
		    while ((inputLine = in.readLine()) != null) {
		        content.append(inputLine);
		    }
		    in.close();
		    con.disconnect();
		    
		    URL itemSolr = new URL("http://fresenia.man.poznan.pl:8983/solr/Items/dataimport?command=delta-import&commit=true");
		    con = (HttpURLConnection) itemSolr.openConnection();
		    con.setRequestMethod("GET");
		    in = new BufferedReader(
		    new InputStreamReader(con.getInputStream()));
		    content = new StringBuffer();
		    while ((inputLine = in.readLine()) != null) {
		        content.append(inputLine);
		    }
		    in.close();
		    con.disconnect();
		}  catch (Exception e) { 
        } finally {
			in.close();
			con.disconnect();
	   }
		
		ResponseBuilder rBuild = Response.ok("Solr update successful");
        return rBuild.build();
	}
	


	public List<String> getItemIds(String StoryId) throws SQLException{
			String query = "SELECT ItemId FROM Item WHERE StoryId = " + StoryId + " ORDER BY OrderIndex ASC";
		   List<String> itemIds = new ArrayList<String>();
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
			  String itemId = rs.getString("ItemId");
			  itemIds.add(itemId);
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
	    return itemIds;
	}

	
	public List<Pair<String, String>> getStoryIds() throws SQLException{
			String query = "SELECT StoryId, RecordId FROM Story ORDER BY StoryId ASC";
			   List<Pair<String, String>> storyIds = new ArrayList<Pair<String, String>>();
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
			  String storyId = rs.getString("StoryId");
			  String recordId = rs.getString("RecordId");
			  Pair<String, String> story = new Pair<String, String>(storyId, recordId);
			  storyIds.add(story);
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
	    return storyIds;
	}

	//Add new entry
	@Path("/imageLinks")
	@POST
	public Response getImageLinks(String body) throws SQLException, IOException, InterruptedException {
		List<Pair<String, String>> stories = getStoryIds();

		int total = 0;
		int countAdded;
		for (int j = 0; j < stories.size(); j++) {
			String storyId = stories.get(j).getKey();
			String recordId = stories.get(j).getValue();

			File file = new File("/home/enrich/log/imagelinks/" + storyId + ".txt");
			file.getParentFile().mkdirs();
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write("https://www.europeana.eu/api/v2/record" + recordId + ".jsonld?wskey=api2demo");
		    fileWriter.close();
		    URL europeanaStory = new URL("https://www.europeana.eu/api/v2/record" + recordId + ".jsonld?wskey=api2demo");
		    HttpURLConnection con = (HttpURLConnection) europeanaStory.openConnection();
		    con.setRequestMethod("GET");
		    BufferedReader in = new BufferedReader(
		    		new InputStreamReader(con.getInputStream())
		    );
		    String inputLine;
		    StringBuffer content = new StringBuffer();
		    while ((inputLine = in.readLine()) != null) {
		        content.append(inputLine);
		    }

			List<String> itemIds = getItemIds(storyId);
			JsonObject data = new JsonParser().parse(content.toString()).getAsJsonObject();
			JsonArray dataArray = data.getAsJsonObject().get("@graph").getAsJsonArray();
			countAdded = 0;
			for (int i = 0; i < dataArray.size(); i++) {
				for(Map.Entry<String, JsonElement> entry : dataArray.get(i).getAsJsonObject().entrySet()) {
					if (entry.getKey().equals("@type") && !entry.getValue().isJsonArray() && entry.getValue().getAsString().equals("edm:WebResource")) {
						if (!dataArray.get(i).getAsJsonObject().keySet().contains("dcterms:isReferencedBy")){
							String imageLink = dataArray.get(i).getAsJsonObject().get("@id").toString();
							if (countAdded < itemIds.size()) {
								String updateQuery = "UPDATE Item SET `edm:WebResource` = " + imageLink + " WHERE ItemId = " + itemIds.get(countAdded);
								executeQuery(updateQuery, "Update");
								total += 1;
								countAdded += 1;
							}
						}
					}
				}
			}
			
		    in.close();
		    con.disconnect();
		    TimeUnit.SECONDS.sleep(1);
		}
		
	    
		ResponseBuilder rBuild = Response.ok(total + " items updated");
        return rBuild.build();
	}

	public static boolean isNumeric(String str)
	{
	    for (char c : str.toCharArray())
	    {
	        if (!Character.isDigit(c)) return false;
	    }
	    return true;
	}
}