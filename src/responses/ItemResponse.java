package responses;

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

import org.apache.http.client.ClientProtocolException;
import objects.Annotation;
import objects.AutomatedEnrichment;
import objects.Comment;
import objects.Item;
import objects.Language;
import objects.Person;
import objects.Place;
import objects.Property;
import objects.Transcription;

import Utilities.Util;

import java.util.*;
import java.util.Date;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

@Path("/items")
public class ItemResponse {


	public static String executeQuery(String query, String type) throws SQLException{
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
				   return type +" could not be executed";
			   }
		   }
		   stmt.execute("SET group_concat_max_len = 10000000;");
		   rs = stmt.executeQuery(query);
		   
		   // Extract data from result set
		   while(rs.next()){
		      //Retrieve by column name
			  Item item = new Item();
			  item.setItemId(rs.getInt("ItemId"));
			  
			  // Add Properties
			  List<Property> PropertyList = new ArrayList<Property>();
			  if (Util.hasColumn(rs, "PropertyId") && rs.getString("PropertyId") != null) {
				  String[] PropertyIds = rs.getString("PropertyId").split("&~&", -1);
				  String[] PropertyValues = rs.getString("PropertyValue").split("&~&", -1);
				  String[] PropertyDescriptions = new String[PropertyIds.length];
				  if (rs.getString("PropertyDescription") != null) {
					  PropertyDescriptions = rs.getString("PropertyDescription").split("&~&", -1);
				  }
				  String[] PropertyTypeNames = rs.getString("PropertyTypeName").split("&~&", -1);
				  String[] PropertyEditables = rs.getString("PropertyEditable").split("&~&", -1);
				  for (int i = 0; i < PropertyIds.length; i++) {
					  Property property = new Property();
					  property.setPropertyId(Integer.parseInt(PropertyIds[i]));
					  property.setPropertyValue(PropertyValues[i]);
					  if (PropertyDescriptions[i] != null) {
						  property.setPropertyDescription(PropertyDescriptions[i]);
					  }
					  property.setPropertyType(PropertyTypeNames[i]);
					  property.setEditable(PropertyEditables[i]);
					  PropertyList.add(property);
				  }
			  }
			  
			  //Add Places
			  List<Place> PlaceList = new ArrayList<Place>();
			  if (Util.hasColumn(rs, "PlaceId") && rs.getString("PlaceId") != null && !rs.getString("PlaceId").equals("NULL")) {
				  String[] PlaceIds = rs.getString("PlaceId").split("&~&", -1);
				  String[] PlaceNames = rs.getString("PlaceName").split("&~&", -1);
				  String[] PlaceLatitudes = rs.getString("PlaceLatitude").split("&~&", -1);
				  String[] PlaceLongitudes = rs.getString("PlaceLongitude").split("&~&", -1);
				  String[] PlaceLink = rs.getString("PlaceLink").split("&~&", -1);
				  String[] PlaceZoom = rs.getString("PlaceZoom").split("&~&", -1);
				  String[] PlaceComment = rs.getString("PlaceComment").split("&~&", -1);
				  String[] PlaceUserId = new String[PlaceIds.length];
				  if (rs.getString("PlaceUserId") != null) {
					  PlaceUserId = rs.getString("PlaceUserId").split("&~&", -1);
				  }
				  String[] PlaceUserGenerated = rs.getString("PlaceUserGenerated").split("&~&", -1);
				  String[] PlaceWikidataNames = rs.getString("PlaceWikidataName").split("&~&", -1);
				  String[] PlaceWikidataIds = rs.getString("PlaceWikidataId").split("&~&", -1);
				  for (int i = 0; i < PlaceIds.length; i++) {
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
					  if (PlaceUserId[i] != null && !PlaceUserId[i].equals("NULL")) {
						  place.setUserId(Integer.parseInt(PlaceUserId[i]));
					  }
					  place.setUserGenerated(PlaceUserGenerated[i]);
					  if (PlaceWikidataNames[i] != null && !PlaceWikidataNames[i].equals("NULL")) {
						  place.setWikidataName(PlaceWikidataNames[i]);
					  }
					  if (PlaceWikidataIds[i] != null && !PlaceWikidataIds[i].equals("NULL")) {
						  place.setWikidataId(PlaceWikidataIds[i]);
					  }
					  PlaceList.add(place);
				  }
			  }

			  //Add Transcriptions
			  List<Transcription> TranscriptionList = new ArrayList<Transcription>();
			  if (Util.hasColumn(rs, "TranscriptionId") && rs.getString("TranscriptionId") != null) {				  
				  String[] TranscriptionIds = rs.getString("TranscriptionId").split("&~&", -1);
				  String[] TranscriptionTexts = rs.getString("TranscriptionText").split("&~&", -1);
				  String[] TranscriptionTextNoTags = rs.getString("TranscriptionTextNoTags").split("&~&", -1);
				  String[] TranscriptionUserIds = rs.getString("TranscriptionUserId").split("&~&", -1);
				  String[] TranscriptionWP_UserIds = rs.getString("TranscriptionWP_UserId").split("&~&", -1);
				  String[] TranscriptionCurrentVersions = rs.getString("TranscriptionCurrentVersion").split("&~&", -1);
				  String[] TranscriptionTimestamps = rs.getString("TranscriptionTimestamp").split("&~&", -1);
				  String[] TranscriptionEuropeanaAnnotationIds = new String[TranscriptionIds.length];
				  String[] TranscriptionNoTexts = rs.getString("TranscriptionNoText").split("&~&", -1);
				  if (rs.getString("TranscriptionEuropeanaAnnotationId") != null) {
					  TranscriptionEuropeanaAnnotationIds = rs.getString("TranscriptionEuropeanaAnnotationId").split("&~&", -1);
				  }
				  
				  String[] LanguageIdList = new String[TranscriptionIds.length];
				  String[] LanguageNameList = new String[TranscriptionIds.length];
				  String[] LanguageNameEnglishList = new String[TranscriptionIds.length];
				  String[] LanguageShortNameList = new String[TranscriptionIds.length];
				  String[] LanguageCodeList = new String[TranscriptionIds.length];
				  if (rs.getString("TranscriptionLanguageId") != null) {
					  LanguageIdList = rs.getString("TranscriptionLanguageId").split("&~&", -1);
				  }
				  if (rs.getString("TranscriptionLanguageName") != null) {
					  LanguageNameList = rs.getString("TranscriptionLanguageName").split("&~&", -1);
				  }
				  if (rs.getString("TranscriptionLanguageNameEnglish") != null) {
					  LanguageNameEnglishList = rs.getString("TranscriptionLanguageNameEnglish").split("&~&", -1);
				  }
				  if (rs.getString("TranscriptionLanguageShortName") != null) {
					  LanguageShortNameList = rs.getString("TranscriptionLanguageShortName").split("&~&", -1);
				  }
				  if (rs.getString("TranscriptionLanguageCode") != null) {
					  LanguageCodeList = rs.getString("TranscriptionLanguageCode").split("&~&", -1);
				  }
				  
				  for (int i = 0; i < TranscriptionIds.length; i++) {
					  Transcription transcription = new Transcription();
					  transcription.setTranscriptionId(Integer.parseInt(TranscriptionIds[i]));
					  transcription.setText(TranscriptionTexts[i]);
					  transcription.setTextNoTags(TranscriptionTextNoTags[i]);
					  transcription.setUserId(Integer.parseInt(TranscriptionUserIds[i]));
					  transcription.setWP_UserId(Integer.parseInt(TranscriptionWP_UserIds[i]));
					  transcription.setCurrentVersion(TranscriptionCurrentVersions[i]);
				      transcription.setTimestamp(TranscriptionTimestamps[i]);
					  if (!TranscriptionEuropeanaAnnotationIds[i].equals("NULL")) {
						  transcription.setEuropeanaAnnotationId(Integer.parseInt(TranscriptionEuropeanaAnnotationIds[i]));
					  }
					  transcription.setNoText(TranscriptionNoTexts[i]);

					  List<Language> LanguageList = new ArrayList<Language>();
					  if (rs.getString("TranscriptionLanguageId") != null) {
						  // Intitialize lists grouped by items
						  String[] LanguageIds = LanguageIdList[i].split("&~&", -1);
						  String[] LanguageNames = LanguageNameList[i].split("&~&", -1);
						  String[] LanguageNameEnglishs = LanguageNameEnglishList[i].split("&~&", -1);
						  String[] LanguageShortNames = LanguageShortNameList[i].split("&~&", -1);
						  String[] LanguageCodes = LanguageCodeList[i].split("&~&", -1);
						  for (int j = 0; j < LanguageIds.length; j++) {
							  if (!isNumeric(LanguageIds[j])) {
								  continue;
							  }
							  Language language = new Language();
							  language.setLanguageId(Integer.parseInt(LanguageIds[j]));
							  language.setName(LanguageNames[j]);
							  language.setNameEnglish(LanguageNameEnglishs[j]);
							  language.setShortName(LanguageShortNames[j]);
							  language.setCode(LanguageCodes[j]);
							  LanguageList.add(language);
						  }
					  }
					  transcription.setLanguages(LanguageList);
					  TranscriptionList.add(transcription);
				  }
			  }
			  
			  
			  //Add Annotations
			  List<Annotation> AnnotationList = new ArrayList<Annotation>();
			  if (Util.hasColumn(rs, "AnnotationId") && rs.getString("AnnotationId") != null) {
				  String[] AnnotationIds = rs.getString("AnnotationId").split("&~&", -1);
				  String[] AnnotationTexts = rs.getString("AnnotationText").split("&~&", -1);
				  String[] AnnotationUserIds = rs.getString("AnnotationUserId").split("&~&", -1);
				  String[] AnnotationX_Coords = rs.getString("AnnotationX_Coord").split(",", -1);
				  String[] AnnotationY_Coords = rs.getString("AnnotationY_Coord").split(",", -1);
				  String[] AnnotationWidths = rs.getString("AnnotationWidth").split(",", -1);
				  String[] AnnotationHeights = rs.getString("AnnotationHeight").split(",", -1);
				  String[] AnnotationTypes = rs.getString("AnnotationType").split("&~&", -1);
				  for (int i = 0; i < AnnotationIds.length; i++) {
					  Annotation annotation = new Annotation();
					  annotation.setAnnotationId(Integer.parseInt(AnnotationIds[i]));
					  annotation.setText(AnnotationTexts[i]);
					  annotation.setUserId(Integer.parseInt(AnnotationUserIds[i]));
					  annotation.setX_Coord(Float.parseFloat(AnnotationX_Coords[i]));
					  annotation.setY_Coord(Float.parseFloat(AnnotationY_Coords[i]));
					  annotation.setHeight(Float.parseFloat(AnnotationWidths[i]));
					  annotation.setWidth(Float.parseFloat(AnnotationHeights[i]));
					  annotation.setAnnotationType(AnnotationTypes[i]);
					  AnnotationList.add(annotation);
				  }
			  }
			  

			  //Add Comments
			  List<Comment> CommentList = new ArrayList<Comment>();
			  if (Util.hasColumn(rs, "CommentId") && rs.getString("CommentId") != null) {
				  String[] CommentIds = rs.getString("CommentId").split("&~&", -1);
				  String[] CommentTexts = rs.getString("CommentText").split("&~&", -1);
				  String[] CommentUserIds = rs.getString("CommentUserId").split("&~&", -1);
				  String[] CommentTimestamps = rs.getString("CommentTimestamp").split("&~&", -1);
				  for (int i = 0; i < CommentIds.length; i++) {
					  Comment comment = new Comment();
					  comment.setCommentId(Integer.parseInt(CommentIds[i]));
					  comment.setText(CommentTexts[i]);
					  comment.setUserId(Integer.parseInt(CommentUserIds[i]));
					  
					  // String to Timestamp conversion
					  try {
				            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				            Date date = formatter.parse(CommentTimestamps[i]);
				            Timestamp timeStampDate = new Timestamp(date.getTime());
				            comment.setTimestamp(timeStampDate);
	
				        } catch (ParseException e) {
				            System.out.println("Exception :" + e);
				            return null;
				        }
					  CommentList.add(comment);
				  }
			  }
			  

			  //Add AutomatedEnrichments
			  List<AutomatedEnrichment> AutomatedEnrichmentList = new ArrayList<AutomatedEnrichment>();
			  if (Util.hasColumn(rs, "AutomatedEnrichmentId") && rs.getString("AutomatedEnrichmentId") != null) {
				  String[] AutomatedEnrichmentIds = rs.getString("AutomatedEnrichmentId").split("&~&", -1);
				  String[] AutomatedEnrichmentNames = rs.getString("AutomatedEnrichmentName").split("&~&", -1);
				  String[] AutomatedEnrichmentTypes = rs.getString("AutomatedEnrichmentType").split("&~&", -1);
				  String[] AutomatedEnrichmentExternalIds = rs.getString("AutomatedEnrichmentExternalId").split("&~&", -1);
				  String[] AutomatedEnrichmentWikiDatas = rs.getString("AutomatedEnrichmentWikiData").split("&~&", -1);
				  for (int i = 0; i < AutomatedEnrichmentIds.length; i++) {
					  AutomatedEnrichment automatedEnrichment = new AutomatedEnrichment();
					  automatedEnrichment.setAutomatedEnrichmentId(Integer.parseInt(AutomatedEnrichmentIds[i]));
					  automatedEnrichment.setName(AutomatedEnrichmentNames[i]);
					  automatedEnrichment.setType(AutomatedEnrichmentTypes[i]);
					  automatedEnrichment.setExternalId(AutomatedEnrichmentExternalIds[i]);
					  if (!AutomatedEnrichmentWikiDatas[i].equals("NULL")) {
						  automatedEnrichment.setWikiData(AutomatedEnrichmentWikiDatas[i]);
					  }
					  
					  AutomatedEnrichmentList.add(automatedEnrichment);
				  }
			  }
			  
			  //Add Persons
			  List<Person> PersonList = new ArrayList<Person>();
			  if (Util.hasColumn(rs, "PersonId") && rs.getString("PersonId") != null) {
				  String[] PersonIds = rs.getString("PersonId").split("&~&", -1);
				  String[] PersonFirstNames = new String[PersonIds.length];
				  if (rs.getString("PersonFirstName") != null) {
					  PersonFirstNames = rs.getString("PersonFirstName").split("&~&", -1);
				  }
				  String[] PersonLastNames = new String[PersonIds.length];
				  if (rs.getString("PersonLastName") != null) {
					  PersonLastNames = rs.getString("PersonLastName").split("&~&", -1);
				  }
				  String[] PersonBirthPlaces = new String[PersonIds.length];
				  if (rs.getString("PersonBirthPlace") != null) {
					  PersonBirthPlaces = rs.getString("PersonBirthPlace").split("&~&", -1);
				  }
				  String[] PersonBirthDates = new String[PersonIds.length];
				  if (rs.getString("PersonBirthDate") != null) {
					  PersonBirthDates = rs.getString("PersonBirthDate").split("&~&", -1);
				  }
				  String[] PersonDeathPlaces = new String[PersonIds.length];
				  if (rs.getString("PersonDeathPlace") != null) {
					  PersonDeathPlaces = rs.getString("PersonDeathPlace").split("&~&", -1);
				  }
				  String[] PersonDeathDates = new String[PersonIds.length];
				  if (rs.getString("PersonDeathDate") != null) {
					  PersonDeathDates = rs.getString("PersonDeathDate").split("&~&", -1);
				  }
				  String[] PersonLinks = new String[PersonIds.length];
				  if (rs.getString("PersonLink") != null) {
					  PersonLinks = rs.getString("PersonLink").split("&~&", -1);
				  }
				  String[] PersonDescriptions = new String[PersonIds.length];
				  if (rs.getString("PersonDescription") != null) {
					  PersonDescriptions = rs.getString("PersonDescription").split("&~&", -1);
				  }
				  for (int i = 0; i < PersonIds.length; i++) {
					  Person person = new Person();
					  person.setPersonId(Integer.parseInt(PersonIds[i]));
					  if (PersonFirstNames[i] != null) {
						  person.setFirstName(PersonFirstNames[i]);
					  }
					  if (PersonLastNames[i] != null) {
						  person.setLastName(PersonLastNames[i]);
					  }
					  if (PersonBirthPlaces[i] != null) {
						  person.setBirthPlace(PersonBirthPlaces[i]);
					  }
					  if (PersonBirthDates[i] != null) {
						  person.setBirthDate(PersonBirthDates[i]);
					  }
					  if (PersonDeathPlaces[i] != null) {
						  person.setDeathPlace(PersonDeathPlaces[i]);
					  }
					  if (PersonDeathDates[i] != null) {
						  person.setDeathDate(PersonDeathDates[i]);
					  }
					  if (PersonLinks[i] != null) {
						  person.setLink(PersonLinks[i]);
					  }
					  if (PersonDescriptions[i] != null) {
						  person.setDescription(PersonDescriptions[i]);
					  }
					  
					  PersonList.add(person);
				  }
			  }

			  item.setProperties(PropertyList);
			  item.setPlaces(PlaceList);
			  item.setComments(CommentList);
			  item.setAutomatedEnrichments(AutomatedEnrichmentList);
			  item.setPersons(PersonList);
			  item.setTranscriptions(TranscriptionList);
			  item.setAnnotations(AnnotationList);
			  item.setTitle(rs.getString("Title"));
			  item.setCompletionStatusColorCode(rs.getString("CompletionStatusColorCode"));
			  item.setCompletionStatusName(rs.getString("CompletionStatusName"));
			  item.setCompletionStatusId(rs.getInt("CompletionStatusId"));
			  if (Util.hasColumn(rs, "TranscriptionStatusId")) {
				  item.setTranscriptionStatusColorCode(rs.getString("TranscriptionStatusColorCode"));
				  item.setTranscriptionStatusName(rs.getString("TranscriptionStatusName"));
				  item.setTranscriptionStatusId(rs.getInt("TranscriptionStatusId"));
			  }
			  if (Util.hasColumn(rs, "DescriptionStatusId")) {
				  item.setDescriptionStatusColorCode(rs.getString("DescriptionStatusColorCode"));
				  item.setDescriptionStatusName(rs.getString("DescriptionStatusName"));
				  item.setDescriptionStatusId(rs.getInt("DescriptionStatusId"));
			  }
			  if (Util.hasColumn(rs, "LocationStatusId")) {
				  item.setLocationStatusColorCode(rs.getString("LocationStatusColorCode"));
				  item.setLocationStatusName(rs.getString("LocationStatusName"));
				  item.setLocationStatusId(rs.getInt("LocationStatusId"));
			  }
			  if (Util.hasColumn(rs, "TaggingStatusId")) {
				  item.setTaggingStatusColorCode(rs.getString("TaggingStatusColorCode"));
				  item.setTaggingStatusName(rs.getString("TaggingStatusName"));
				  item.setTaggingStatusId(rs.getInt("TaggingStatusId"));
			  }
			  if (Util.hasColumn(rs, "AutomaticEnrichmentStatusId")) {
				  item.setAutomaticEnrichmentStatusColorCode(rs.getString("AutomaticEnrichmentStatusColorCode"));
				  item.setAutomaticEnrichmentStatusName(rs.getString("AutomaticEnrichmentStatusName"));
				  item.setAutomaticEnrichmentStatusId(rs.getInt("AutomaticEnrichmentStatusId"));
			  }
			  item.setOldItemId(rs.getInt("OldItemId"));
			  item.setDescription(rs.getString("Description"));
			  item.setDescriptionLanguage(rs.getInt("DescriptionLanguage"));
			  item.setDateStart(rs.getTimestamp("DateStart"));
			  item.setDateEnd(rs.getTimestamp("DateEnd"));
			  item.setDateStartDisplay(rs.getString("DateStartDisplay"));
			  item.setDateEndDisplay(rs.getString("DateEndDisplay"));
			  item.setDatasetId(rs.getInt("DatasetId"));
			  item.setImageLink(rs.getString("ImageLink"));
			  item.setOrderIndex(rs.getInt("OrderIndex"));
			  item.setTimestamp(rs.getString("Timestamp"));
			  item.setLockedTime(rs.getString("LockedTime"));
			  item.setLockedUser(rs.getInt("LockedUser"));
			  item.setManifest(rs.getString("Manifest"));
			  if (Util.hasColumn(rs, "StoryId")) {
				  item.setStoryId(rs.getInt("StoryId"));
				  item.setStorydcTitle(rs.getString("StorydcTitle"));
				  item.setStorydcDescription(rs.getString("StorydcDescription"));
				  item.setStoryedmLandingPage(rs.getString("StoryedmLandingPage"));
				  item.setStoryExternalRecordId(rs.getString("StoryExternalRecordId"));
				  item.setStoryPlaceName(rs.getString("StoryPlaceName"));
				  item.setStoryPlaceLatitude(rs.getFloat("StoryPlaceLatitude"));
				  item.setStoryPlaceLongitude(rs.getFloat("StoryPlaceLongitude"));
				  item.setStoryPlaceZoom(rs.getString("StoryPlaceZoom"));
				  item.setStoryPlaceLink(rs.getString("StoryPlaceLink"));
				  item.setStoryPlaceComment(rs.getString("StoryPlaceComment"));
				  item.setStoryPlaceUserId(rs.getInt("StoryPlaceUserId"));
				  item.setStoryPlaceUserGenerated(rs.getString("StoryPlaceUserGenerated"));
				  item.setStorydcCreator(rs.getString("StorydcCreator"));
				  item.setStorydcSource(rs.getString("StoryedmRights"));
				  item.setStorydcSource(rs.getString("StorydcSource"));
				  item.setStoryedmCountry(rs.getString("StoryedmCountry"));
				  item.setStoryedmDataProvider(rs.getString("StoryedmDataProvider"));
				  item.setStoryedmProvider(rs.getString("StoryedmProvider"));
				  item.setStoryedmYear(rs.getString("StoryedmYear"));
				  item.setStorydcPublisher(rs.getString("StorydcPublisher"));
				  item.setStorydcCoverage(rs.getString("StorydcCoverage"));
				  item.setStorydcDate(rs.getString("StorydcDate"));
				  item.setStorydcType(rs.getString("StorydcType"));
				  item.setStorydcRelation(rs.getString("StorydcRelation"));
				  item.setStorydctermsMedium(rs.getString("StorydctermsMedium"));
				  item.setStoryedmDatasetName(rs.getString("StoryedmDatasetName"));
				  item.setStorydcContributor(rs.getString("StorydcContributor"));
				  item.setStoryedmRights(rs.getString("StoryedmRights"));
				  item.setStoryedmBegin(rs.getString("StoryedmBegin"));
				  item.setStoryedmEnd(rs.getString("StoryedmEnd"));
				  item.setStoryedmIsShownAt(rs.getString("StoryedmIsShownAt"));
				  item.setStorydcRights(rs.getString("StorydcRights"));
				  item.setStorydcLanguage(rs.getString("StorydcLanguage"));
				  item.setStoryedmLanguage(rs.getString("StoryedmLanguage"));
				  item.setStoryProjectId(rs.getInt("StoryProjectId"));
				  item.setStorySummary(rs.getString("StorySummary"));
				  item.setStoryParentStory(rs.getInt("StoryParentStory"));
				  item.setStorySearchText(rs.getString("StorySearchText"));
				  item.setStoryDateStart(rs.getTimestamp("StoryDateStart"));
				  item.setStoryDateEnd(rs.getTimestamp("StoryDateEnd"));
				  item.setStoryOrderIndex(rs.getInt("StoryOrderIndex"));
			  }

			  itemList.add(item);
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
	    String result = gsonBuilder.toJson(itemList);
	    return result;
	}
	
	public Item getItemData(String query) throws SQLException{
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
		   try {
			Class.forName("com.mysql.jdbc.Driver");
		
		   // Open a connection
		   conn = DriverManager.getConnection(DB_URL, USER, PASS);
		   // Execute SQL query
		   stmt = conn.createStatement();
		   stmt.execute("SET group_concat_max_len = 10000000;");
		   rs = stmt.executeQuery(query);
		   
		   // Extract data from result set
		   while(rs.next()){
		      //Retrieve by column name
			  Item item = new Item();
			  item.setItemId(rs.getInt("ItemId"));
			  item.setTitle(rs.getString("Title"));
			  item.setCompletionStatusColorCode(rs.getString("CompletionStatusColorCode"));
			  item.setCompletionStatusName(rs.getString("CompletionStatusName"));
			  item.setCompletionStatusId(rs.getInt("CompletionStatusId"));
			  item.setTranscriptionStatusColorCode(rs.getString("TranscriptionStatusColorCode"));
			  item.setTranscriptionStatusName(rs.getString("TranscriptionStatusName"));
			  item.setTranscriptionStatusId(rs.getInt("TranscriptionStatusId"));
			  item.setDescriptionStatusColorCode(rs.getString("DescriptionStatusColorCode"));
			  item.setDescriptionStatusName(rs.getString("DescriptionStatusName"));
			  item.setDescriptionStatusId(rs.getInt("DescriptionStatusId"));
			  item.setLocationStatusColorCode(rs.getString("LocationStatusColorCode"));
			  item.setLocationStatusName(rs.getString("LocationStatusName"));
			  item.setLocationStatusId(rs.getInt("LocationStatusId"));
			  item.setTaggingStatusColorCode(rs.getString("TaggingStatusColorCode"));
			  item.setTaggingStatusName(rs.getString("TaggingStatusName"));
			  item.setTaggingStatusId(rs.getInt("TaggingStatusId"));
			  item.setAutomaticEnrichmentStatusColorCode(rs.getString("AutomaticEnrichmentStatusColorCode"));
			  item.setAutomaticEnrichmentStatusName(rs.getString("AutomaticEnrichmentStatusName"));
			  item.setAutomaticEnrichmentStatusId(rs.getInt("AutomaticEnrichmentStatusId"));
			  item.setOldItemId(rs.getInt("OldItemId"));
			  item.setDescription(rs.getString("Description"));
			  item.setDescriptionLanguage(rs.getInt("DescriptionLanguage"));
			  item.setDateStart(rs.getTimestamp("DateStart"));
			  item.setDateEnd(rs.getTimestamp("DateEnd"));
			  item.setDateStartDisplay(rs.getString("DateStartDisplay"));
			  item.setDateEndDisplay(rs.getString("DateEndDisplay"));
			  item.setDatasetId(rs.getInt("DatasetId"));
			  item.setImageLink(rs.getString("ImageLink"));
			  item.setOrderIndex(rs.getInt("OrderIndex"));
			  item.setTimestamp(rs.getString("Timestamp"));
			  item.setLockedTime(rs.getString("LockedTime"));
			  item.setLockedUser(rs.getInt("LockedUser"));
			  item.setManifest(rs.getString("Manifest"));
			  item.setStoryId(rs.getInt("StoryId"));
			  item.setStorydcTitle(rs.getString("StorydcTitle"));
			  item.setStorydcDescription(rs.getString("StorydcDescription"));
			  item.setStoryedmLandingPage(rs.getString("StoryedmLandingPage"));
			  item.setStoryExternalRecordId(rs.getString("StoryExternalRecordId"));
			  item.setStoryPlaceName(rs.getString("StoryPlaceName"));
			  item.setStoryPlaceLatitude(rs.getFloat("StoryPlaceLatitude"));
			  item.setStoryPlaceLongitude(rs.getFloat("StoryPlaceLongitude"));
			  item.setStoryPlaceZoom(rs.getString("StoryPlaceZoom"));
			  item.setStoryPlaceLink(rs.getString("StoryPlaceLink"));
			  item.setStoryPlaceComment(rs.getString("StoryPlaceComment"));
			  item.setStoryPlaceUserId(rs.getInt("StoryPlaceUserId"));
			  item.setStoryPlaceUserGenerated(rs.getString("StoryPlaceUserGenerated"));
			  item.setStorydcCreator(rs.getString("StorydcCreator"));
			  item.setStorydcSource(rs.getString("StoryedmRights"));
			  item.setStorydcSource(rs.getString("StorydcSource"));
			  item.setStoryedmCountry(rs.getString("StoryedmCountry"));
			  item.setStoryedmDataProvider(rs.getString("StoryedmDataProvider"));
			  item.setStoryedmProvider(rs.getString("StoryedmProvider"));
			  item.setStoryedmYear(rs.getString("StoryedmYear"));
			  item.setStorydcPublisher(rs.getString("StorydcPublisher"));
			  item.setStorydcCoverage(rs.getString("StorydcCoverage"));
			  item.setStorydcDate(rs.getString("StorydcDate"));
			  item.setStorydcType(rs.getString("StorydcType"));
			  item.setStorydcRelation(rs.getString("StorydcRelation"));
			  item.setStorydctermsMedium(rs.getString("StorydctermsMedium"));
			  item.setStoryedmDatasetName(rs.getString("StoryedmDatasetName"));
			  item.setStorydcContributor(rs.getString("StorydcContributor"));
			  item.setStoryedmRights(rs.getString("StoryedmRights"));
			  item.setStoryedmBegin(rs.getString("StoryedmBegin"));
			  item.setStoryedmEnd(rs.getString("StoryedmEnd"));
			  item.setStoryedmIsShownAt(rs.getString("StoryedmIsShownAt"));
			  item.setStorydcRights(rs.getString("StorydcRights"));
			  item.setStorydcLanguage(rs.getString("StorydcLanguage"));
			  item.setStoryedmLanguage(rs.getString("StoryedmLanguage"));
			  item.setStoryProjectId(rs.getInt("StoryProjectId"));
			  item.setStorySummary(rs.getString("StorySummary"));
			  item.setStoryParentStory(rs.getInt("StoryParentStory"));
			  item.setStorySearchText(rs.getString("StorySearchText"));
			  item.setStoryDateStart(rs.getTimestamp("StoryDateStart"));
			  item.setStoryDateEnd(rs.getTimestamp("StoryDateEnd"));
			  item.setStoryOrderIndex(rs.getInt("StoryOrderIndex"));
			  
			  return item;
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
      return null;
	}

	

	public String executeDataQuery(String query, String field) throws SQLException{
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
			   String result = rs.getString(field);
			   rs.close();
			   stmt.close();
			   conn.close();
			   return result;
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
	    return "";
	}

	//Get all Entries
	
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getAll(@Context UriInfo uriInfo) throws SQLException {
		String query =  "SELECT \r\n" + 
				"    *\r\n" + 
				"FROM\r\n" + 
				"    (SELECT \r\n" + 
				"        i.ItemId AS ItemId,\r\n" + 
				"            i.Title AS Title,\r\n" + 
				"            i.CompletionStatusId AS CompletionStatusId,\r\n" + 
				"            i.CompletionStatusName AS CompletionStatusName,\r\n" + 
				"            i.CompletionStatusColorCode AS CompletionStatusColorCode,\r\n" + 
				"            i.TranscriptionStatusId AS TranscriptionStatusId,\r\n" + 
				"            i.TranscriptionStatusName AS TranscriptionStatusName,\r\n" + 
				"            i.TranscriptionStatusColorCode AS TranscriptionStatusColorCode,\r\n" + 
				"            i.DescriptionStatusId AS DescriptionStatusId,\r\n" + 
				"            i.DescriptionStatusName AS DescriptionStatusName,\r\n" + 
				"            i.DescriptionStatusColorCode AS DescriptionStatusColorCode,\r\n" + 
				"            i.LocationStatusId AS LocationStatusId,\r\n" + 
				"            i.LocationStatusName AS LocationStatusName,\r\n" + 
				"            i.LocationStatusColorCode AS LocationStatusColorCode,\r\n" + 
				"            i.TaggingStatusId AS TaggingStatusId,\r\n" + 
				"            i.TaggingStatusName AS TaggingStatusName,\r\n" + 
				"            i.TaggingStatusColorCode AS TaggingStatusColorCode,\r\n" + 
				"            i.AutomaticEnrichmentStatusId AS AutomaticEnrichmentStatusId,\r\n" + 
				"            i.AutomaticEnrichmentStatusName AS AutomaticEnrichmentStatusName,\r\n" + 
				"            i.AutomaticEnrichmentStatusColorCode AS AutomaticEnrichmentStatusColorCode,\r\n" + 
				"            i.OldItemId AS OldItemId,\r\n" + 
				"            i.Description AS Description,\r\n" + 
				"            i.DescriptionLanguage AS DescriptionLanguage,\r\n" + 
				"            i.DateStart AS DateStart,\r\n" + 
				"            i.DateEnd AS DateEnd,\r\n" + 
				"            i.DateStartDisplay AS DateStartDisplay,\r\n" + 
				"            i.DateEndDisplay AS DateEndDisplay,\r\n" + 
				"            i.DatasetId AS DatasetId,\r\n" + 
				"            i.ImageLink AS ImageLink,\r\n" + 
				"            i.OrderIndex AS OrderIndex,\r\n" + 
				"            i.Timestamp AS Timestamp,\r\n" + 
				"            i.LockedTime AS LockedTime,\r\n" + 
				"            i.LockedUser AS LockedUser,\r\n" + 
				"            i.Manifest AS Manifest,\r\n" + 
				"            a.PropertyId AS PropertyId,\r\n" + 
				"            a.PropertyTypeName AS PropertyTypeName,\r\n" + 
				"            a.PropertyValue AS PropertyValue,\r\n" + 
				"            a.PropertyDescription AS PropertyDescription,\r\n" + 
				"            a.PropertyEditable AS PropertyEditable,\r\n" + 
				"            b.CommentId AS CommentId,\r\n" + 
				"            b.CommentText AS CommentText,\r\n" + 
				"            b.CommentUserId AS CommentUserId,\r\n" + 
				"            b.CommentTimestamp AS CommentTimestamp,\r\n" + 
				"            c.PlaceId AS PlaceId,\r\n" + 
				"            c.PlaceName AS PlaceName,\r\n" + 
				"            c.PlaceLatitude AS PlaceLatitude,\r\n" + 
				"            c.PlaceLongitude AS PlaceLongitude,\r\n" + 
				"            c.PlaceLink AS PlaceLink,\r\n" + 
				"            c.PlaceZoom AS PlaceZoom,\r\n" + 
				"            c.PlaceComment AS PlaceComment,\r\n" + 
				"            c.PlaceUserId AS PlaceUserID,\r\n" + 
				"            c.PlaceUserGenerated AS PlaceUserGenerated,\r\n" + 
				"            c.PlaceWikidataName AS PlaceWikidataName,\r\n" + 
				"            c.PlaceWikidataId AS PlaceWikidataId,\r\n" + 
				"            d.TranscriptionId AS TranscriptionId,\r\n" + 
				"            d.TranscriptionText AS TranscriptionText,\r\n" + 
				"            d.TranscriptionTextNoTags AS TranscriptionTextNoTags,\r\n" + 
				"            d.TranscriptionUserId AS TranscriptionUserId,\r\n" + 
				"            d.TranscriptionWP_UserId AS TranscriptionWP_UserId,\r\n" + 
				"            d.TranscriptionCurrentVersion AS TranscriptionCurrentVersion,\r\n" + 
				"            d.TranscriptionTimestamp AS TranscriptionTimestamp,\r\n" + 
				"            d.TranscriptionEuropeanaAnnotationId AS TranscriptionEuropeanaAnnotationId,\r\n" + 
				"            d.TranscriptionNoText AS TranscriptionNoText,\r\n" + 
				"            d.TranscriptionLanguageId AS TranscriptionLanguageId,\r\n" + 
				"            d.TranscriptionLanguageName AS TranscriptionLanguageName,\r\n" + 
				"            d.TranscriptionLanguageNameEnglish AS TranscriptionLanguageNameEnglish,\r\n" + 
				"            d.TranscriptionLanguageShortName AS TranscriptionLanguageShortName,\r\n" + 
				"            d.TranscriptionLanguageCode AS TranscriptionLanguageCode,\r\n" + 
				"            e.AnnotationId AS AnnotationId,\r\n" + 
				"            e.AnnotationType AS AnnotationType,\r\n" + 
				"            e.AnnotationText AS AnnotationText,\r\n" + 
				"            e.AnnotationUserId AS AnnotationUserId,\r\n" + 
				"            e.AnnotationX_Coord AS AnnotationX_Coord,\r\n" + 
				"            e.AnnotationY_Coord AS AnnotationY_Coord,\r\n" + 
				"            e.AnnotationWidth AS AnnotationWidth,\r\n" + 
				"            e.AnnotationHeight AS AnnotationHeight,\r\n" + 
				"            f.PersonId AS PersonId,\r\n" + 
				"            f.PersonFirstName AS PersonFirstName,\r\n" + 
				"            f.PersonLastName AS PersonLastName,\r\n" + 
				"            f.PersonBirthPlace AS PersonBirthPlace,\r\n" + 
				"            f.PersonBirthDate AS PersonBirthDate,\r\n" + 
				"            f.PersonDeathPlace AS PersonDeathPlace,\r\n" + 
				"            f.PersonDeathDate AS PersonDeathDate,\r\n" + 
				"            f.PersonLink AS PersonLink,\r\n" + 
				"            f.PersonDescription AS PersonDescription,\r\n" + 
				"            s.StoryId AS StoryId,\r\n" + 
				"            s.`dc:title` AS StorydcTitle,\r\n" + 
				"            s.`dc:description` AS StorydcDescription,\r\n" + 
				"            s.`edm:landingPage` AS StoryedmLandingPage,\r\n" + 
				"            s.ExternalRecordId AS StoryExternalRecordId,\r\n" + 
				"            s.PlaceName AS StoryPlaceName,\r\n" + 
				"            s.PlaceLatitude AS StoryPlaceLatitude,\r\n" + 
				"            s.PlaceLongitude AS StoryPlaceLongitude,\r\n" + 
				"            s.PlaceZoom AS StoryPlaceZoom,\r\n" + 
				"            s.PlaceLink AS StoryPlaceLink,\r\n" + 
				"            s.PlaceComment AS StoryPlaceComment,\r\n" + 
				"            s.PlaceUserId AS StoryPlaceUserId,\r\n" + 
				"            s.PlaceUserGenerated AS StoryPlaceUserGenerated,\r\n" + 
				"            s.`dc:creator` AS StorydcCreator,\r\n" + 
				"            s.`dc:source` AS StorydcSource,\r\n" + 
				"            s.`edm:country` AS StoryedmCountry,\r\n" + 
				"            s.`edm:dataProvider` AS StoryedmDataProvider,\r\n" + 
				"            s.`edm:provider` AS StoryedmProvider,\r\n" + 
				"            s.`edm:year` AS StoryedmYear,\r\n" + 
				"            s.`dc:publisher` AS StorydcPublisher,\r\n" + 
				"            s.`dc:coverage` AS StorydcCoverage,\r\n" + 
				"            s.`dc:date` AS StorydcDate,\r\n" + 
				"            s.`dc:type` AS StorydcType,\r\n" + 
				"            s.`dc:relation` AS StorydcRelation,\r\n" + 
				"            s.`dcterms:medium` AS StorydctermsMedium,\r\n" + 
				"            s.`edm:datasetName` AS StoryedmDatasetName,\r\n" + 
				"            s.`dc:contributor` AS StorydcContributor,\r\n" + 
				"            s.`edm:rights` AS StoryedmRights,\r\n" + 
				"            s.`edm:begin` AS StoryedmBegin,\r\n" + 
				"            s.`edm:end` AS StoryedmEnd,\r\n" + 
				"            s.`edm:isShownAt` AS StoryedmIsShownAt,\r\n" + 
				"            s.`dc:Rights` AS StorydcRights,\r\n" + 
				"            s.`dc:language` AS StorydcLanguage,\r\n" + 
				"            s.`edm:language` AS StoryedmLanguage,\r\n" + 
				"            s.ProjectId AS StoryProjectId,\r\n" + 
				"            s.Summary AS StorySummary,\r\n" + 
				"            s.ParentStory AS StoryParentStory,\r\n" + 
				"            s.SearchText AS StorySearchText,\r\n" + 
				"            s.DateStart AS StoryDateStart,\r\n" + 
				"            s.DateEnd AS StoryDateEnd,\r\n" + 
				"            s.OrderIndex AS StoryOrderIndex\r\n" + 
				"    FROM\r\n" + 
				"        (SELECT \r\n" + 
				"        *\r\n" + 
				"    FROM\r\n" + 
				"        Item i\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        i.ItemId AS CompletionStatusItemId,\r\n" + 
				"            c.Name AS CompletionStatusName,\r\n" + 
				"            c.ColorCode AS CompletionStatusColorCode\r\n" + 
				"    FROM\r\n" + 
				"        CompletionStatus c\r\n" + 
				"    JOIN Item i ON i.CompletionStatusId = c.CompletionStatusId) status ON i.ItemId = status.CompletionStatusItemId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        i.ItemId AS TranscriptionStatusItemId,\r\n" + 
				"            c.Name AS TranscriptionStatusName,\r\n" + 
				"            c.ColorCode AS TranscriptionStatusColorCode\r\n" + 
				"    FROM\r\n" + 
				"        CompletionStatus c\r\n" + 
				"    JOIN Item i ON i.TranscriptionStatusId = c.CompletionStatusId) trStatus ON i.ItemId = trStatus.TranscriptionStatusItemId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        i.ItemId AS DescriptionStatusItemId,\r\n" + 
				"            c.Name AS DescriptionStatusName,\r\n" + 
				"            c.ColorCode AS DescriptionStatusColorCode\r\n" + 
				"    FROM\r\n" + 
				"        CompletionStatus c\r\n" + 
				"    JOIN Item i ON i.DescriptionStatusId = c.CompletionStatusId) deStatus ON i.ItemId = deStatus.DescriptionStatusItemId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        i.ItemId AS LocationStatusItemId,\r\n" + 
				"            c.Name AS LocationStatusName,\r\n" + 
				"            c.ColorCode AS LocationStatusColorCode\r\n" + 
				"    FROM\r\n" + 
				"        CompletionStatus c\r\n" + 
				"    JOIN Item i ON i.LocationStatusId = c.CompletionStatusId) loStatus ON i.ItemId = loStatus.LocationStatusItemId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        i.ItemId AS TaggingStatusItemId,\r\n" + 
				"            c.Name AS TaggingStatusName,\r\n" + 
				"            c.ColorCode AS TaggingStatusColorCode\r\n" + 
				"    FROM\r\n" + 
				"        CompletionStatus c\r\n" + 
				"    JOIN Item i ON i.TaggingStatusId = c.CompletionStatusId) taStatus ON i.ItemId = taStatus.TaggingStatusItemId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        i.ItemId AS AutomaticEnrichmentStatusItemId,\r\n" + 
				"            c.Name AS AutomaticEnrichmentStatusName,\r\n" + 
				"            c.ColorCode AS AutomaticEnrichmentStatusColorCode\r\n" + 
				"    FROM\r\n" + 
				"        CompletionStatus c\r\n" + 
				"    JOIN Item i ON i.AutomaticEnrichmentStatusId = c.CompletionStatusId) auStatus ON i.ItemId = auStatus.AutomaticEnrichmentStatusItemId) i\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        i.ItemId AS ItemId,\r\n" + 
				"            GROUP_CONCAT(p.PropertyId\r\n" + 
				"                SEPARATOR '&~&') AS PropertyId,\r\n" + 
				"            GROUP_CONCAT(pt.Name\r\n" + 
				"                SEPARATOR '&~&') AS PropertyTypeName,\r\n" + 
				"            GROUP_CONCAT(p.Value\r\n" + 
				"                SEPARATOR '&~&') AS PropertyValue,\r\n" + 
				"            GROUP_CONCAT(IFNULL(p.Description, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS PropertyDescription,\r\n" + 
				"            GROUP_CONCAT(pt.Editable + 0\r\n" + 
				"                SEPARATOR '&~&') AS PropertyEditable\r\n" + 
				"    FROM\r\n" + 
				"        Item i\r\n" + 
				"    LEFT JOIN ItemProperty ip ON i.ItemId = ip.ItemId\r\n" + 
				"    LEFT JOIN Property p ON ip.PropertyId = p.PropertyId\r\n" + 
				"    LEFT JOIN PropertyType pt ON p.PropertyTypeId = pt.PropertyTypeId\r\n" + 
				"    GROUP BY i.ItemId) a ON i.ItemId = a.ItemId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        i.ItemId AS ItemId,\r\n" + 
				"            GROUP_CONCAT(c.CommentId\r\n" + 
				"                SEPARATOR '&~&') AS CommentId,\r\n" + 
				"            GROUP_CONCAT(c.Text\r\n" + 
				"                SEPARATOR '&~&') AS CommentText,\r\n" + 
				"            GROUP_CONCAT(c.UserId\r\n" + 
				"                SEPARATOR '&~&') AS CommentUserId,\r\n" + 
				"            GROUP_CONCAT(c.Timestamp\r\n" + 
				"                SEPARATOR '&~&') AS CommentTimestamp\r\n" + 
				"    FROM\r\n" + 
				"        Item i\r\n" + 
				"    LEFT JOIN Comment c ON i.ItemId = c.ItemId\r\n" + 
				"    GROUP BY i.ItemId) b ON i.ItemId = b.ItemId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        i.ItemId AS ItemId,\r\n" + 
				"            GROUP_CONCAT(pl.PlaceId\r\n" + 
				"                SEPARATOR '&~&') AS PlaceId,\r\n" + 
				"            GROUP_CONCAT(pl.Name\r\n" + 
				"                SEPARATOR '&~&') AS PlaceName,\r\n" + 
				"            GROUP_CONCAT(pl.Latitude\r\n" + 
				"                SEPARATOR '&~&') AS PlaceLatitude,\r\n" + 
				"            GROUP_CONCAT(pl.Longitude\r\n" + 
				"                SEPARATOR '&~&') AS PlaceLongitude,\r\n" + 
				"            GROUP_CONCAT(IFNULL(pl.Link, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS PlaceLink,\r\n" + 
				"            GROUP_CONCAT(pl.Zoom\r\n" + 
				"                SEPARATOR '&~&') AS PlaceZoom,\r\n" + 
				"            GROUP_CONCAT(IFNULL(pl.Comment, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS PlaceComment,\r\n" + 
				"            GROUP_CONCAT(IFNULL(pl.UserId, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS PlaceUserId,\r\n" + 
				"            GROUP_CONCAT(pl.UserGenerated + 0\r\n" + 
				"                SEPARATOR '&~&') AS PlaceUserGenerated,\r\n" + 
				"            GROUP_CONCAT(IFNULL(pl.WikidataName, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS PlaceWikidataName,\r\n" + 
				"            GROUP_CONCAT(IFNULL(pl.WikidataId, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS PlaceWikidataId\r\n" + 
				"    FROM\r\n" + 
				"        Item i\r\n" + 
				"    LEFT JOIN Place pl ON i.ItemId = pl.ItemId\r\n" + 
				"    GROUP BY i.ItemId) c ON i.ItemId = c.ItemId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        i.ItemId AS ItemId,\r\n" + 
				"            GROUP_CONCAT(t.TranscriptionId\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionId,\r\n" + 
				"            GROUP_CONCAT(t.Text\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionText,\r\n" + 
				"            GROUP_CONCAT(t.TextNoTags\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionTextNoTags,\r\n" + 
				"            GROUP_CONCAT(t.UserId\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionUserId,\r\n" + 
				"            GROUP_CONCAT(t.CurrentVersion + 0\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionWP_UserId,\r\n" + 
				"            GROUP_CONCAT(t.CurrentVersion + 0\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionCurrentVersion,\r\n" + 
				"            GROUP_CONCAT(t.Timestamp\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionTimestamp,\r\n" + 
				"            GROUP_CONCAT(t.NoText\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionNoText,\r\n" + 
				"            GROUP_CONCAT(IFNULL(t.EuropeanaAnnotationId, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionEuropeanaAnnotationId,\r\n" + 
				"            GROUP_CONCAT(IFNULL(l.LanguageId, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionLanguageId,\r\n" + 
				"            GROUP_CONCAT(IFNULL(l.Name, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionLanguageName,\r\n" + 
				"            GROUP_CONCAT(IFNULL(l.NameEnglish, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionLanguageNameEnglish,\r\n" + 
				"            GROUP_CONCAT(IFNULL(l.ShortName, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionLanguageShortName,\r\n" + 
				"            GROUP_CONCAT(IFNULL(l.Code, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS TranscriptionLanguageCode\r\n" + 
				"    FROM\r\n" + 
				"        Item i\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        *\r\n" + 
				"    FROM\r\n" + 
				"        Transcription t) t ON i.ItemId = t.ItemId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        WP_UserId, UserId\r\n" + 
				"    FROM\r\n" + 
				"        User) u ON t.UserId = u.UserId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        tl.TranscriptionId,\r\n" + 
				"            GROUP_CONCAT(IFNULL(l.LanguageId, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS LanguageId,\r\n" + 
				"            GROUP_CONCAT(IFNULL(l.Name, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS Name,\r\n" + 
				"            GROUP_CONCAT(IFNULL(l.NameEnglish, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS NameEnglish,\r\n" + 
				"            GROUP_CONCAT(IFNULL(l.ShortName, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS ShortName,\r\n" + 
				"            GROUP_CONCAT(IFNULL(l.Code, 'NULL')\r\n" + 
				"                SEPARATOR '&~&') AS Code\r\n" + 
				"    FROM\r\n" + 
				"        TranscriptionLanguage tl\r\n" + 
				"    JOIN Language l ON l.LanguageId = tl.LanguageId\r\n" + 
				"    GROUP BY tl.TranscriptionId) l ON t.TranscriptionId = l.TranscriptionId\r\n" + 
				"    GROUP BY i.ItemId) d ON i.ItemId = d.ItemId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        i.ItemId AS ItemId,\r\n" + 
				"            GROUP_CONCAT(a.AnnotationId\r\n" + 
				"                SEPARATOR '&~&') AS AnnotationId,\r\n" + 
				"            GROUP_CONCAT(at.Name\r\n" + 
				"                SEPARATOR '&~&') AS AnnotationType,\r\n" + 
				"            GROUP_CONCAT(a.Text\r\n" + 
				"                SEPARATOR '&~&') AS AnnotationText,\r\n" + 
				"            GROUP_CONCAT(a.UserId\r\n" + 
				"                SEPARATOR '&~&') AS AnnotationUserId,\r\n" + 
				"            GROUP_CONCAT(a.X_Coord\r\n" + 
				"                SEPARATOR '&~&') AS AnnotationX_Coord,\r\n" + 
				"            GROUP_CONCAT(a.Y_Coord\r\n" + 
				"                SEPARATOR '&~&') AS AnnotationY_Coord,\r\n" + 
				"            GROUP_CONCAT(a.Width\r\n" + 
				"                SEPARATOR '&~&') AS AnnotationWidth,\r\n" + 
				"            GROUP_CONCAT(a.Height\r\n" + 
				"                SEPARATOR '&~&') AS AnnotationHeight\r\n" + 
				"    FROM\r\n" + 
				"        Item i\r\n" + 
				"    LEFT JOIN Annotation a ON i.ItemId = a.ItemId\r\n" + 
				"    LEFT JOIN AnnotationType at ON a.AnnotationTypeId = at.AnnotationTypeId\r\n" + 
				"    GROUP BY i.ItemId) e ON i.ItemId = e.ItemId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        i.ItemId AS ItemId,\r\n" + 
				"            GROUP_CONCAT(pe.PersonId\r\n" + 
				"                SEPARATOR '&~&') AS PersonId,\r\n" + 
				"            GROUP_CONCAT(pe.FirstName\r\n" + 
				"                SEPARATOR '&~&') AS PersonFirstName,\r\n" + 
				"            GROUP_CONCAT(pe.LastName\r\n" + 
				"                SEPARATOR '&~&') AS PersonLastName,\r\n" + 
				"            GROUP_CONCAT(pe.BirthPlace\r\n" + 
				"                SEPARATOR '&~&') AS PersonBirthPlace,\r\n" + 
				"            GROUP_CONCAT(pe.BirthDate\r\n" + 
				"                SEPARATOR '&~&') AS PersonBirthDate,\r\n" + 
				"            GROUP_CONCAT(pe.DeathPlace\r\n" + 
				"                SEPARATOR '&~&') AS PersonDeathPlace,\r\n" + 
				"            GROUP_CONCAT(pe.DeathDate\r\n" + 
				"                SEPARATOR '&~&') AS PersonDeathDate,\r\n" + 
				"            GROUP_CONCAT(pe.Link\r\n" + 
				"                SEPARATOR '&~&') AS PersonLink,\r\n" + 
				"            GROUP_CONCAT(pe.Description\r\n" + 
				"                SEPARATOR '&~&') AS PersonDescription\r\n" + 
				"    FROM\r\n" + 
				"        Item i\r\n" + 
				"    LEFT JOIN Person pe ON i.ItemId = pe.ItemId\r\n" + 
				"    GROUP BY i.ItemId) f ON i.ItemId = f.ItemId\r\n" + 
				"    LEFT JOIN (SELECT \r\n" + 
				"        *\r\n" + 
				"    FROM\r\n" + 
				"        Story) s ON i.StoryId = s.StoryId) a\r\n" + 
				"WHERE\r\n" + 
				"    1 ";

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

	//Edit entry by id
	@Path("/{id}")
	@POST
	public Response update(@PathParam("id") int id, String body) throws SQLException, ClientProtocolException, IOException {
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    JsonObject  changes = gson.fromJson(body, JsonObject.class);
	    
	    //Check if NOT NULL field is attempted to be changed to NULL
	    if ((changes.get("Name") == null || !changes.get("Name").isJsonNull())
	    		&& (changes.get("Public") == null || !changes.get("Public").isJsonNull())) {
		    String query = "UPDATE Item SET ";
		    
		    int keyCount = changes.entrySet().size();
		    int i = 1;
			for(Map.Entry<String, JsonElement> entry : changes.entrySet()) {
			    query += entry.getKey() + " = '" + changes.get(entry.getKey()).getAsString() + "'";
			    if (i < keyCount) {
			    	query += ", ";
			    }
			    i++;
			}
			query += " WHERE ItemId = " + id;
			String resource = executeQuery(query, "Update");

			String completionQuery = "SELECT * FROM Item WHERE ItemId = " + id;
			String completionStatus = executeDataQuery(completionQuery, "TranscriptionStatusId");
			String exportedQuery = "SELECT * FROM Item WHERE ItemId = " + id;
			String exported = executeDataQuery(exportedQuery, "Exported");
			String recordIdQuery = "SELECT * FROM Story WHERE StoryId = (SELECT StoryId FROM Item WHERE ItemId = " + id + ")";
			String recordId = executeDataQuery(recordIdQuery, "ExternalRecordId");
			String itemIdQuery = "SELECT * FROM Item WHERE ItemId = " + id;
			String itemId = executeDataQuery(itemIdQuery, "ItemId");
			String[] recordIdSplit = recordId.split("/");
			recordId =  "/" + recordIdSplit[recordIdSplit.length - 2] + "/" + recordIdSplit[recordIdSplit.length - 1];
			
			if (completionStatus.equals("4") && exported.equals("0") ) {
				/*
				try (InputStream input = new FileInputStream("/home/enrich/tomcat/apache-tomcat-9.0.13/webapps/dev_tp-api/WEB-INF/config.properties")) {

		            Properties prop = new Properties();

		            // load a properties file
		            prop.load(input);

		            
		    		HttpClient httpclient = HttpClients.createDefault();
		    		
		            HttpPost httppost = new HttpPost("https://sso.apps.paas-dev.psnc.pl/auth/realms/EnrichEuropeana/protocol/openid-connect/token");
	    	
	    	        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
	    	        params.add(new BasicNameValuePair("grant_type", "client_credentials"));
	    	        params.add(new BasicNameValuePair("client_secret", prop.getProperty("SECRET_KEY")));
	    	        params.add(new BasicNameValuePair("client_id", "tp-api-client"));
	    	        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
	    	        HttpResponse response = httpclient.execute(httppost);
	    	        HttpEntity entity = response.getEntity();

	    	        if (entity != null) {
	    	            try (InputStream instream = entity.getContent()) {
	    	                StringWriter writer = new StringWriter();
	    	                IOUtils.copy(instream, writer, StandardCharsets.UTF_8);
	    	                JsonObject authData = new JsonParser().parse(writer.toString()).getAsJsonObject();

	    	    	        String authHeader = authData.get("access_token").toString();

	        	            URL url = new URL("https://fresenia.man.poznan.pl/dei/api/transcription?recordId=" + recordId + "&itemId=" + itemId);
	        				HttpURLConnection con = (HttpURLConnection) url.openConnection();
							
							con.setRequestMethod("POST");
							con.setRequestProperty("Content-Type", "application/json");
						    con.setRequestProperty("Authorization", "Bearer " + authHeader.replace("\"", "") );
						    
							BufferedReader in = new BufferedReader(
							  new InputStreamReader(con.getInputStream(), "UTF-8"));
							String inputLine;
							StringBuffer content = new StringBuffer();
							while ((inputLine = in.readLine()) != null) {
							    content.append(inputLine);
							}
							in.close();
							con.disconnect();
	    	            }
	    	        }
				}    
				*/
            }
			String updateTimestampQuery = "UPDATE Item SET LastUpdated = NOW() WHERE ItemId = " + id;
			executeQuery(updateTimestampQuery, "Update");
			String updateStoryTimestampQuery = "UPDATE Story SET LastUpdated = NOW() WHERE StoryId = (SELECT StoryId FROM Item WHERE ItemId = " + id + ")";
			executeQuery(updateStoryTimestampQuery, "Update");
			StoryResponse.solrUpdate();

			ResponseBuilder rBuild = Response.ok(resource);
			//ResponseBuilder rBuild = Response.ok(query);
	        return rBuild.build();
	    } else {
			ResponseBuilder rBuild = Response.status(Response.Status.BAD_REQUEST);
	        return rBuild.build();
	    }
	}

/*
	//Delete entry by id
	@Path("/{id}")
	@DELETE
	public String delete(@PathParam("id") int id) throws SQLException {
		String query =  "DELETE FROM Item " +
				"WHERE ItemId = " + id;
		String resource = executeQuery(query, "Delete");
		return resource;
	}
	*/
		//Get entry by id
		@Path("/{id}")
		@Produces("application/json;charset=utf-8")
		@GET
		public Response getEntryPost(@PathParam("id") int id) throws SQLException, ParseException, IOException {
	        Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();
            /*
	        
			String query =  "SELECT \r\n" + 
					"    i.ItemId AS ItemId,\r\n" + 
					"    i.Title AS Title,\r\n" + 
					"    i.CompletionStatusId AS CompletionStatusId,\r\n" + 
					"    coStatus.Name AS CompletionStatusName,\r\n" + 
					"    coStatus.ColorCode AS CompletionStatusColorCode,\r\n" + 
					"    i.TranscriptionStatusId AS TranscriptionStatusId,\r\n" + 
					"    trStatus.Name AS TranscriptionStatusName,\r\n" + 
					"    trStatus.ColorCode AS TranscriptionStatusColorCode,\r\n" + 
					"    i.DescriptionStatusId AS DescriptionStatusId,\r\n" + 
					"    deStatus.Name AS DescriptionStatusName,\r\n" + 
					"    deStatus.ColorCode AS DescriptionStatusColorCode,\r\n" + 
					"    i.LocationStatusId AS LocationStatusId,\r\n" + 
					"    loStatus.Name AS LocationStatusName,\r\n" + 
					"    loStatus.ColorCode AS LocationStatusColorCode,\r\n" + 
					"    i.TaggingStatusId AS TaggingStatusId,\r\n" + 
					"    taStatus.Name AS TaggingStatusName,\r\n" + 
					"    taStatus.ColorCode AS TaggingStatusColorCode,\r\n" + 
					"    i.AutomaticEnrichmentStatusId AS AutomaticEnrichmentStatusId,\r\n" + 
					"    auStatus.Name AS AutomaticEnrichmentStatusName,\r\n" + 
					"    auStatus.ColorCode AS AutomaticEnrichmentStatusColorCode,\r\n" + 
					"    i.OldItemId AS OldItemId,\r\n" + 
					"    i.Description AS Description,\r\n" + 
					"    i.DescriptionLanguage AS DescriptionLanguage,\r\n" + 
					"    i.DateStart AS DateStart,\r\n" + 
					"    i.DateEnd AS DateEnd,\r\n" + 
					"    i.DateStartDisplay AS DateStartDisplay,\r\n" + 
					"    i.DateEndDisplay AS DateEndDisplay,\r\n" + 
					"    i.DatasetId AS DatasetId,\r\n" + 
					"    i.ImageLink AS ImageLink,\r\n" + 
					"    i.OrderIndex AS OrderIndex,\r\n" + 
					"    i.Timestamp AS Timestamp,\r\n" + 
					"    i.LockedTime AS LockedTime,\r\n" + 
					"    i.LockedUser AS LockedUser,\r\n" + 
					"    i.Manifest AS Manifest,\r\n\r\n" + 
					"    autEnrichments.AutomatedEnrichmentId AS AutomatedEnrichmentId,\r\n" + 
					"    autEnrichments.Name AS AutomatedEnrichmentName,\r\n" + 
					"    autEnrichments.Type AS AutomatedEnrichmentType,\r\n" + 
					"    autEnrichments.ExternalId AS AutomatedEnrichmentExternalId,\r\n" + 
					"    autEnrichments.WikiData AS AutomatedEnrichmentWikiData," + 
					"    prop.PropertyId AS PropertyId,\r\n" + 
					"    prop.PropertyTypeName AS PropertyTypeName,\r\n" + 
					"    prop.PropertyValue AS PropertyValue,\r\n" + 
					"    prop.PropertyDescription AS PropertyDescription,\r\n" + 
					"    prop.PropertyEditable AS PropertyEditable,\r\n" + 
					"    comments.CommentId AS CommentId,\r\n" + 
					"    comments.CommentText AS CommentText,\r\n" + 
					"    comments.CommentUserId AS CommentUserId,\r\n" + 
					"    comments.CommentTimestamp AS CommentTimestamp,\r\n" + 
					"    place.PlaceId AS PlaceId,\r\n" + 
					"    place.PlaceName AS PlaceName,\r\n" + 
					"    place.PlaceLatitude AS PlaceLatitude,\r\n" + 
					"    place.PlaceLongitude AS PlaceLongitude,\r\n" + 
					"    place.PlaceLink AS PlaceLink,\r\n" + 
					"    place.PlaceZoom AS PlaceZoom,\r\n" + 
					"    place.PlaceComment AS PlaceComment, \r\n" + 
					"    place.PlaceUserId AS PlaceUserId,\r\n" + 
					"    place.PlaceUserGenerated AS PlaceUserGenerated,\r\n" + 
					"    place.PlaceWikidataName AS PlaceWikidataName,\r\n" + 
					"    place.PlaceWikidataId AS PlaceWikidataId,\r\n" + 
					"    transc.TranscriptionId AS TranscriptionId,\r\n" + 
					"    transc.TranscriptionText AS TranscriptionText,\r\n" + 
					"    transc.TranscriptionTextNoTags AS TranscriptionTextNoTags,\r\n" + 
					"    transc.TranscriptionUserId AS TranscriptionUserId,\r\n" + 
					"    transc.TranscriptionCurrentVersion AS TranscriptionCurrentVersion,\r\n" + 
					"    transc.TranscriptionTimestamp AS TranscriptionTimestamp,\r\n" + 
					"    transc.TranscriptionWP_UserId AS TranscriptionWP_UserId,\r\n" + 
					"    transc.TranscriptionEuropeanaAnnotationId AS TranscriptionEuropeanaAnnotationId,\r\n" + 
					"    transc.TranscriptionNoText AS TranscriptionNoText,\r\n" + 
					"    transc.TranscriptionLanguageId AS TranscriptionLanguageId,\r\n" + 
					"    transc.TranscriptionLanguageName AS TranscriptionLanguageName,\r\n" + 
					"    transc.TranscriptionLanguageNameEnglish AS TranscriptionLanguageNameEnglish,\r\n" + 
					"    transc.TranscriptionLanguageShortName AS TranscriptionLanguageShortName,\r\n" + 
					"    transc.TranscriptionLanguageCode AS TranscriptionLanguageCode,\r\n" + 
					"    annot.AnnotationId AS AnnotationId,\r\n" + 
					"    annot.AnnotationType AS AnnotationType,\r\n" + 
					"    annot.AnnotationText AS AnnotationText,\r\n" + 
					"    annot.AnnotationUserId AS AnnotationUserId,\r\n" + 
					"    annot.AnnotationX_Coord AS AnnotationX_Coord,\r\n" + 
					"    annot.AnnotationY_Coord AS AnnotationY_Coord,\r\n" + 
					"    annot.AnnotationWidth AS AnnotationWidth,\r\n" + 
					"    annot.AnnotationHeight AS AnnotationHeight,\r\n" + 
					"    person.PersonId as PersonId,\r\n" + 
					"    person.PersonFirstName as PersonFirstName,\r\n" + 
					"    person.PersonLastName as PersonLastName,\r\n" + 
					"    person.PersonBirthPlace as PersonBirthPlace,\r\n" + 
					"    person.PersonBirthDate as PersonBirthDate,\r\n" + 
					"    person.PersonDeathPlace as PersonDeathPlace,\r\n" + 
					"    person.PersonDeathDate as PersonDeathDate,\r\n" +
					"    person.PersonLink as PersonLink,\r\n" + 
					"    person.PersonDescription as PersonDescription,\r\n" + 
					"    s.StoryId AS StoryId,\r\n" + 
					"    s.`dc:title` AS StorydcTitle,\r\n" + 
					"    s.`dc:description` AS StorydcDescription,\r\n" + 
					"    s.`edm:landingPage` AS StoryedmLandingPage,\r\n" + 
					"    s.ExternalRecordId AS StoryExternalRecordId,\r\n" + 
					"    s.PlaceName AS StoryPlaceName,\r\n" + 
					"    s.PlaceLatitude AS StoryPlaceLatitude,\r\n" + 
					"    s.PlaceLongitude AS StoryPlaceLongitude,\r\n" + 
					"    s.PlaceZoom AS StoryPlaceZoom,\r\n" + 
					"    s.PlaceLink AS StoryPlaceLink,\r\n" + 
					"    s.PlaceComment AS StoryPlaceComment,\r\n" + 
					"    s.PlaceUserId AS StoryPlaceUserId,\r\n" + 
					"    s.PlaceUserGenerated AS StoryPlaceUserGenerated,\r\n" + 
					"    s.`dc:creator` AS StorydcCreator,\r\n" + 
					"    s.`dc:source` AS StorydcSource,\r\n" + 
					"    s.`edm:country` AS StoryedmCountry,\r\n" + 
					"    s.`edm:dataProvider` AS StoryedmDataProvider,\r\n" + 
					"    s.`edm:provider` AS StoryedmProvider,\r\n" + 
					"    s.`edm:year` AS StoryedmYear,\r\n" + 
					"    s.`dc:publisher` AS StorydcPublisher,\r\n" + 
					"    s.`dc:coverage` AS StorydcCoverage,\r\n" + 
					"    s.`dc:date` AS StorydcDate,\r\n" + 
					"    s.`dc:type` AS StorydcType,\r\n" + 
					"    s.`dc:relation` AS StorydcRelation,\r\n" + 
					"    s.`dcterms:medium` AS StorydctermsMedium,\r\n" + 
					"    s.`edm:datasetName` AS StoryedmDatasetName,\r\n" + 
					"    s.`dc:contributor` AS StorydcContributor,\r\n" + 
					"    s.`edm:rights` AS StoryedmRights,\r\n" + 
					"    s.`edm:begin` AS StoryedmBegin,\r\n" + 
					"    s.`edm:end` AS StoryedmEnd,\r\n" + 
					"    s.`edm:isShownAt` AS StoryedmIsShownAt,\r\n" + 
					"    s.`dc:Rights` AS StorydcRights,\r\n" + 
					"    s.`dc:language` AS StorydcLanguage,\r\n" + 
					"    s.`edm:language` AS StoryedmLanguage,\r\n" + 
					"    s.ProjectId AS StoryProjectId,\r\n" + 
					"    s.Summary AS StorySummary,\r\n" + 
					"    s.ParentStory AS StoryParentStory,\r\n" + 
					"    s.SearchText AS StorySearchText,\r\n" + 
					"    s.DateStart AS StoryDateStart,\r\n" + 
					"    s.DateEnd AS StoryDateEnd,\r\n" + 
					"    s.OrderIndex AS StoryOrderIndex\r\n" + 
					"FROM\r\n" + 
					"    (SELECT \r\n" + 
					"        *\r\n" + 
					"    FROM\r\n" + 
					"        Item\r\n" + 
					"    WHERE\r\n" + 
					"        ItemId = " + id + ") i\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    CompletionStatus coStatus ON i.CompletionStatusId = coStatus.CompletionStatusId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    CompletionStatus trStatus ON i.TranscriptionStatusId = trStatus.CompletionStatusId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    CompletionStatus deStatus ON i.DescriptionStatusId = deStatus.CompletionStatusId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    CompletionStatus loStatus ON i.LocationStatusId = loStatus.CompletionStatusId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    CompletionStatus taStatus ON i.TaggingStatusId = taStatus.CompletionStatusId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    CompletionStatus auStatus ON i.AutomaticEnrichmentStatusId = auStatus.CompletionStatusId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"	(\r\n" + 
					"		SELECT \r\n" + 
					"			ip.ItemId as ItemId,\r\n" + 
					"			GROUP_CONCAT(p.PropertyId\r\n" + 
					"				SEPARATOR '&~&') AS PropertyId,\r\n" + 
					"			GROUP_CONCAT(pt.Name\r\n" + 
					"				SEPARATOR '&~&') AS PropertyTypeName,\r\n" + 
					"			GROUP_CONCAT(p.Value\r\n" + 
					"				SEPARATOR '&~&') AS PropertyValue,\r\n" + 
					"			GROUP_CONCAT(IFNULL(p.Description, 'NULL')" + 
					"				SEPARATOR '&~&') AS PropertyDescription,\r\n" + 
					"			GROUP_CONCAT(pt.Editable + 0\r\n" + 
					"				SEPARATOR '&~&') AS PropertyEditable\r\n" + 
					"		FROM\r\n" + 
					"			ItemProperty ip\r\n" + 
					"				LEFT JOIN\r\n" + 
					"			Property p ON ip.PropertyId = p.PropertyId\r\n" + 
					"				LEFT JOIN\r\n" + 
					"			PropertyType pt ON p.PropertyTypeId = pt.PropertyTypeId\r\n" + 
					"		WHERE ip.ItemId = " + id +
					"		GROUP BY ip.ItemId\r\n" + 
					"	) prop \r\n" + 
					"		ON prop.ItemId = i.ItemId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"	(\r\n" + 
					"		SELECT \r\n" + 
					"			c.ItemId,\r\n" + 
					"			GROUP_CONCAT(c.CommentId\r\n" + 
					"				SEPARATOR '&~&') AS CommentId,\r\n" + 
					"			GROUP_CONCAT(c.Text\r\n" + 
					"				SEPARATOR '&~&') AS CommentText,\r\n" + 
					"			GROUP_CONCAT(c.UserId\r\n" + 
					"				SEPARATOR '&~&') AS CommentUserId,\r\n" + 
					"			GROUP_CONCAT(c.Timestamp\r\n" + 
					"				SEPARATOR '&~&') AS CommentTimestamp\r\n" + 
					"		FROM\r\n" + 
					"			Comment c\r\n" + 
					"		WHERE c.ItemId = " + id +
					"		GROUP BY c.ItemId\r\n" + 
					"	) comments ON comments.ItemId = i.ItemId \r\n" + 
					"        LEFT JOIN\r\n\r\n" + 
					"	(\r\n" + 
					"		SELECT \r\n" + 
					"			ae.ItemId,\r\n" + 
					"			GROUP_CONCAT(ae.AutomatedEnrichmentId\r\n" + 
					"				SEPARATOR '&~&') AS AutomatedEnrichmentId,\r\n" + 
					"			GROUP_CONCAT(ae.Name\r\n" + 
					"				SEPARATOR '&~&') AS Name,\r\n" + 
					"			GROUP_CONCAT(ae.Type\r\n" + 
					"				SEPARATOR '&~&') AS Type,\r\n" + 
					"			GROUP_CONCAT(ae.ExternalId\r\n" + 
					"				SEPARATOR '&~&') AS ExternalId,\r\n" + 
					"			GROUP_CONCAT(IFNULL(ae.WikiData, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS WikiData\r\n" + 
					"		FROM\r\n" + 
					"			AutomatedEnrichment ae\r\n" + 
					"		WHERE ae.ItemId =  " + id + " GROUP BY ae.ItemId\r\n" + 
					"	) autEnrichments ON autEnrichments.ItemId = i.ItemId\r\n" + 
					"        LEFT JOIN" + 
					"	(\r\n" + 
					"		SELECT \r\n" + 
					"			pl.ItemId,\r\n" + 
					"			GROUP_CONCAT(pl.PlaceId\r\n" + 
					"				SEPARATOR '&~&') AS PlaceId,\r\n" + 
					"			GROUP_CONCAT(pl.Name\r\n" + 
					"				SEPARATOR '&~&') AS PlaceName,\r\n" + 
					"			GROUP_CONCAT(pl.Latitude\r\n" + 
					"				SEPARATOR '&~&') AS PlaceLatitude,\r\n" + 
					"			GROUP_CONCAT(pl.Longitude\r\n" + 
					"				SEPARATOR '&~&') AS PlaceLongitude,\r\n" + 
					"			GROUP_CONCAT(IFNULL(pl.Link, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS PlaceLink,\r\n" + 
					"			GROUP_CONCAT(pl.Zoom\r\n" + 
					"				SEPARATOR '&~&') AS PlaceZoom,\r\n" + 
					"			GROUP_CONCAT(IFNULL(pl.Comment, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS PlaceComment,\r\n" + 
					"			GROUP_CONCAT(IFNULL(pl.UserId, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS PlaceUserId,\r\n" + 
					"			GROUP_CONCAT(pl.UserGenerated + 0\r\n" + 
					"				SEPARATOR '&~&') AS PlaceUserGenerated,\r\n" + 
					"			GROUP_CONCAT(IFNULL(pl.WikidataName, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS PlaceWikidataName,\r\n" + 
					"			GROUP_CONCAT(IFNULL(pl.WikidataId, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS PlaceWikidataId\r\n" + 
					"		FROM\r\n" + 
					"			Place pl\r\n" + 
					"		WHERE pl.ItemId = " + id +
					"		GROUP BY pl.ItemId\r\n" + 
					"	) place ON place.ItemId = i.ItemId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"	(\r\n" + 
					"		SELECT \r\n" + 
					"			t.ItemId,\r\n" + 
					"			GROUP_CONCAT(t.TranscriptionId ORDER BY t.Timestamp DESC\r\n" + 
					"				SEPARATOR '&~&') AS TranscriptionId,\r\n" + 
					"			GROUP_CONCAT(t.Text ORDER BY t.Timestamp DESC\r\n" + 
					"				SEPARATOR '&~&') AS TranscriptionText,\r\n" + 
					"			GROUP_CONCAT(t.TextNoTags ORDER BY t.Timestamp DESC\r\n" + 
					"				SEPARATOR '&~&') AS TranscriptionTextNoTags,\r\n" + 
					"			GROUP_CONCAT(t.UserId ORDER BY t.Timestamp DESC\r\n" + 
					"				SEPARATOR '&~&') AS TranscriptionUserId,\r\n" + 
					"			GROUP_CONCAT(t.CurrentVersion + 0 ORDER BY t.Timestamp DESC\r\n" + 
					"				SEPARATOR '&~&') AS TranscriptionCurrentVersion,\r\n" + 
					"			GROUP_CONCAT(t.Timestamp ORDER BY t.Timestamp DESC\r\n" + 
					"				SEPARATOR '&~&') AS TranscriptionTimestamp,\r\n" + 
					"			GROUP_CONCAT(u.WP_UserId ORDER BY t.Timestamp DESC\r\n" + 
					"				SEPARATOR '&~&') AS TranscriptionWP_UserId,\r\n" + 
					"			GROUP_CONCAT(IFNULL(t.EuropeanaAnnotationId, 'NULL') ORDER BY t.Timestamp DESC\r\n" + 
					"				SEPARATOR '&~&') AS TranscriptionEuropeanaAnnotationId,\r\n" + 
					"			GROUP_CONCAT(t.NoText + 0 ORDER BY t.Timestamp DESC\r\n" + 
					"				SEPARATOR '&~&') AS TranscriptionNoText,\r\n" + 
					"    		GROUP_CONCAT(IFNULL(l.LanguageId, 'NULL') ORDER BY t.Timestamp DESC SEPARATOR '&~&') AS TranscriptionLanguageId,\r\n" + 
					"    		GROUP_CONCAT(IFNULL(l.Name, 'NULL') ORDER BY t.Timestamp DESC SEPARATOR '&~&') AS TranscriptionLanguageName,\r\n" + 
					"    		GROUP_CONCAT(IFNULL(l.NameEnglish, 'NULL') ORDER BY t.Timestamp DESC SEPARATOR '&~&') AS TranscriptionLanguageNameEnglish,\r\n" + 
					"    		GROUP_CONCAT(IFNULL(l.ShortName, 'NULL') ORDER BY t.Timestamp DESC SEPARATOR '&~&') AS TranscriptionLanguageShortName,\r\n" + 
					"    		GROUP_CONCAT(IFNULL(l.Code, 'NULL') ORDER BY t.Timestamp DESC SEPARATOR '&~&') AS TranscriptionLanguageCode " + 
					"		FROM\r\n" + 
					"    	(" +
					"			SELECT \r\n" + 
					"        		*\r\n" + 
					"    		FROM\r\n" + 
					"        		Transcription t\r\n" + 
					"		) t\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    	(SELECT \r\n" + 
					"        	WP_UserId, UserId\r\n" + 
					"    	FROM\r\n" + 
					"        	User) u ON t.UserId = u.UserId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    	(" +
					"			SELECT \r\n" + 
					"        		tl.TranscriptionId,\r\n" + 
					"            	GROUP_CONCAT(IFNULL(l.LanguageId, 'NULL') SEPARATOR '�~�') AS LanguageId,\r\n" + 
					"            	GROUP_CONCAT(IFNULL(l.Name, 'NULL') SEPARATOR '�~�') AS Name,\r\n" + 
					"            	GROUP_CONCAT(IFNULL(l.NameEnglish, 'NULL') SEPARATOR '�~�') AS NameEnglish,\r\n" + 
					"            	GROUP_CONCAT(IFNULL(l.ShortName, 'NULL') SEPARATOR '�~�') AS ShortName,\r\n" + 
					"            	GROUP_CONCAT(IFNULL(l.Code, 'NULL') SEPARATOR '�~�') AS Code\r\n" + 
					"    		FROM\r\n" + 
					"        		TranscriptionLanguage tl\r\n" + 
					"    		JOIN Language l ON l.LanguageId = tl.LanguageId\r\n" + 
					"    		GROUP BY tl.TranscriptionId" +	
					"		) " +
					"		l ON t.TranscriptionId = l.TranscriptionId " +
					"		WHERE t.ItemId = " + id +
					"    	GROUP BY t.ItemId\r\n" +
					"	) transc On transc.ItemId = i.ItemId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"	(\r\n" + 
					"		SELECT \r\n" + 
					"			a.ItemId,\r\n" + 
					"			GROUP_CONCAT(a.AnnotationId\r\n" + 
					"				SEPARATOR '&~&') AS AnnotationId,\r\n" + 
					"			GROUP_CONCAT(at.Name\r\n" + 
					"				SEPARATOR '&~&') AS AnnotationType,\r\n" + 
					"			GROUP_CONCAT(a.Text\r\n" + 
					"				SEPARATOR '&~&') AS AnnotationText,\r\n" + 
					"			GROUP_CONCAT(a.UserId\r\n" + 
					"				SEPARATOR '&~&') AS AnnotationUserId,\r\n" + 
					"			GROUP_CONCAT(a.X_Coord\r\n" + 
					"				SEPARATOR '&~&') AS AnnotationX_Coord,\r\n" + 
					"			GROUP_CONCAT(a.Y_Coord\r\n" + 
					"				SEPARATOR '&~&') AS AnnotationY_Coord,\r\n" + 
					"			GROUP_CONCAT(a.Width\r\n" + 
					"				SEPARATOR '&~&') AS AnnotationWidth,\r\n" + 
					"			GROUP_CONCAT(a.Height\r\n" + 
					"				SEPARATOR '&~&') AS AnnotationHeight\r\n" + 
					"		FROM\r\n" + 
					"			Annotation a\r\n" + 
					"				LEFT JOIN\r\n" + 
					"			AnnotationType at ON a.AnnotationTypeId = at.AnnotationTypeId\r\n" + 
					"		WHERE a.ItemId = " + id +
					"		GROUP BY a.ItemId\r\n" + 
					"	) annot ON annot.ItemId = i.ItemId\r\n" + 
					"		LEFT JOIN \r\n" + 
					"        (\r\n" + 
					"		SELECT \r\n" + 
					"			iperson.ItemId,\r\n" + 
					"			GROUP_CONCAT(pe.PersonId\r\n" + 
					"				SEPARATOR '&~&') AS PersonId,\r\n" + 
					"			GROUP_CONCAT(IFNULL(pe.FirstName, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS PersonFirstName,\r\n" + 
					"			GROUP_CONCAT(IFNULL(pe.LastName, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS PersonLastName,\r\n" + 
					"			GROUP_CONCAT(IFNULL(pe.BirthPlace, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS PersonBirthPlace,\r\n" + 
					"			GROUP_CONCAT(IFNULL(pe.BirthDate, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS PersonBirthDate,\r\n" + 
					"			GROUP_CONCAT(IFNULL(pe.DeathPlace, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS PersonDeathPlace,\r\n" + 
					"			GROUP_CONCAT(IFNULL(pe.DeathDate, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS PersonDeathDate,\r\n" + 
					"			GROUP_CONCAT(IFNULL(pe.Link, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS PersonLink,\r\n" + 
					"			GROUP_CONCAT(IFNULL(pe.Description, 'NULL')\r\n" + 
					"				SEPARATOR '&~&') AS PersonDescription\r\n" + 
					"		FROM\r\n" + 
					"			ItemPerson iperson \r\n" + 
					"		JOIN Person pe ON iperson.PersonId = pe.PersonId\r\n" + 
					"		WHERE iperson.ItemId = " + id +
					"		GROUP BY iperson.ItemId\r\n" + 
					"        ) person\r\n" + 
					"        ON person.ItemId = i.ItemId" + 
					"				LEFT JOIN\r\n" + 
					"    Story s ON i.StoryId = s.StoryId\r\n";
			String resource = executeQuery(query, "Select");
			*/
            
            String timeText = "Times: ";
            long startTime = System.nanoTime();
			
			String itemQuery = "SELECT \r\n" + 
					"    i.ItemId AS ItemId,\r\n" + 
					"    i.Title AS Title,\r\n" + 
					"    i.CompletionStatusId AS CompletionStatusId,\r\n" + 
					"    coStatus.Name AS CompletionStatusName,\r\n" + 
					"    coStatus.ColorCode AS CompletionStatusColorCode,\r\n" + 
					"    i.TranscriptionStatusId AS TranscriptionStatusId,\r\n" + 
					"    trStatus.Name AS TranscriptionStatusName,\r\n" + 
					"    trStatus.ColorCode AS TranscriptionStatusColorCode,\r\n" + 
					"    i.DescriptionStatusId AS DescriptionStatusId,\r\n" + 
					"    deStatus.Name AS DescriptionStatusName,\r\n" + 
					"    deStatus.ColorCode AS DescriptionStatusColorCode,\r\n" + 
					"    i.LocationStatusId AS LocationStatusId,\r\n" + 
					"    loStatus.Name AS LocationStatusName,\r\n" + 
					"    loStatus.ColorCode AS LocationStatusColorCode,\r\n" + 
					"    i.TaggingStatusId AS TaggingStatusId,\r\n" + 
					"    taStatus.Name AS TaggingStatusName,\r\n" + 
					"    taStatus.ColorCode AS TaggingStatusColorCode,\r\n" + 
					"    i.AutomaticEnrichmentStatusId AS AutomaticEnrichmentStatusId,\r\n" + 
					"    auStatus.Name AS AutomaticEnrichmentStatusName,\r\n" + 
					"    auStatus.ColorCode AS AutomaticEnrichmentStatusColorCode,\r\n" + 
					"    i.OldItemId AS OldItemId,\r\n" + 
					"    i.Description AS Description,\r\n" + 
					"    i.DescriptionLanguage AS DescriptionLanguage,\r\n" + 
					"    i.DateStart AS DateStart,\r\n" + 
					"    i.DateEnd AS DateEnd,\r\n" + 
					"    i.DateStartDisplay AS DateStartDisplay,\r\n" + 
					"    i.DateEndDisplay AS DateEndDisplay,\r\n" + 
					"    i.DatasetId AS DatasetId,\r\n" + 
					"    i.ImageLink AS ImageLink,\r\n" + 
					"    i.OrderIndex AS OrderIndex,\r\n" + 
					"    i.Timestamp AS Timestamp,\r\n" + 
					"    i.LockedTime AS LockedTime,\r\n" + 
					"    i.LockedUser AS LockedUser,\r\n" + 
					"    i.Manifest AS Manifest,\r\n\r\n" + 
					"    s.StoryId AS StoryId,\r\n" + 
					"    s.`dc:title` AS StorydcTitle,\r\n" + 
					"    s.`dc:description` AS StorydcDescription,\r\n" + 
					"    s.`edm:landingPage` AS StoryedmLandingPage,\r\n" + 
					"    s.ExternalRecordId AS StoryExternalRecordId,\r\n" + 
					"    s.PlaceName AS StoryPlaceName,\r\n" + 
					"    s.PlaceLatitude AS StoryPlaceLatitude,\r\n" + 
					"    s.PlaceLongitude AS StoryPlaceLongitude,\r\n" + 
					"    s.PlaceZoom AS StoryPlaceZoom,\r\n" + 
					"    s.PlaceLink AS StoryPlaceLink,\r\n" + 
					"    s.PlaceComment AS StoryPlaceComment,\r\n" + 
					"    s.PlaceUserId AS StoryPlaceUserId,\r\n" + 
					"    s.PlaceUserGenerated AS StoryPlaceUserGenerated,\r\n" + 
					"    s.`dc:creator` AS StorydcCreator,\r\n" + 
					"    s.`dc:source` AS StorydcSource,\r\n" + 
					"    s.`edm:country` AS StoryedmCountry,\r\n" + 
					"    s.`edm:dataProvider` AS StoryedmDataProvider,\r\n" + 
					"    s.`edm:provider` AS StoryedmProvider,\r\n" + 
					"    s.`edm:year` AS StoryedmYear,\r\n" + 
					"    s.`dc:publisher` AS StorydcPublisher,\r\n" + 
					"    s.`dc:coverage` AS StorydcCoverage,\r\n" + 
					"    s.`dc:date` AS StorydcDate,\r\n" + 
					"    s.`dc:type` AS StorydcType,\r\n" + 
					"    s.`dc:relation` AS StorydcRelation,\r\n" + 
					"    s.`dcterms:medium` AS StorydctermsMedium,\r\n" + 
					"    s.`edm:datasetName` AS StoryedmDatasetName,\r\n" + 
					"    s.`dc:contributor` AS StorydcContributor,\r\n" + 
					"    s.`edm:rights` AS StoryedmRights,\r\n" + 
					"    s.`edm:begin` AS StoryedmBegin,\r\n" + 
					"    s.`edm:end` AS StoryedmEnd,\r\n" + 
					"    s.`edm:isShownAt` AS StoryedmIsShownAt,\r\n" + 
					"    s.`dc:Rights` AS StorydcRights,\r\n" + 
					"    s.`dc:language` AS StorydcLanguage,\r\n" + 
					"    s.`edm:language` AS StoryedmLanguage,\r\n" + 
					"    s.ProjectId AS StoryProjectId,\r\n" + 
					"    s.Summary AS StorySummary,\r\n" + 
					"    s.ParentStory AS StoryParentStory,\r\n" + 
					"    s.SearchText AS StorySearchText,\r\n" + 
					"    s.DateStart AS StoryDateStart,\r\n" + 
					"    s.DateEnd AS StoryDateEnd,\r\n" + 
					"    s.OrderIndex AS StoryOrderIndex\r\n" +
					"FROM\r\n" + 
					"    (SELECT \r\n" + 
					"        *\r\n" + 
					"    FROM\r\n" + 
					"        Item\r\n" + 
					"    WHERE\r\n" + 
					"        ItemId = " + id + ") i\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    CompletionStatus coStatus ON i.CompletionStatusId = coStatus.CompletionStatusId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    CompletionStatus trStatus ON i.TranscriptionStatusId = trStatus.CompletionStatusId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    CompletionStatus deStatus ON i.DescriptionStatusId = deStatus.CompletionStatusId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    CompletionStatus loStatus ON i.LocationStatusId = loStatus.CompletionStatusId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    CompletionStatus taStatus ON i.TaggingStatusId = taStatus.CompletionStatusId\r\n" + 
					"        LEFT JOIN\r\n" + 
					"    CompletionStatus auStatus ON i.AutomaticEnrichmentStatusId = auStatus.CompletionStatusId\r\n" + 
					"		 LEFT JOIN\r\n" + 
					"    Story s ON i.StoryId = s.StoryId\r\n";
			Item item = getItemData(itemQuery);

			timeText += "itemData: " + ((System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();

			String transcriptionQuery = "SELECT \r\n" + 
								"			t.ItemId,\r\n" + 
								"			t.TranscriptionId AS TranscriptionId,\r\n" + 
								"			t.Text AS Text,\r\n" + 
								"			t.TextNoTags AS TextNoTags,\r\n" + 
								"			t.UserId AS UserId,\r\n" + 
								"			t.CurrentVersion + 0 AS CurrentVersion,\r\n" + 
								"			t.Timestamp AS Timestamp,\r\n" + 
								"			u.WP_UserId AS WP_UserId,\r\n" + 
								"			IFNULL(t.EuropeanaAnnotationId, 'NULL') AS EuropeanaAnnotationId,\r\n" + 
								"			t.NoText + 0 AS NoText,\r\n" + 
								"    		IFNULL(l.LanguageId, 'NULL') AS LanguageId,\r\n" + 
								"    		IFNULL(l.Name, 'NULL') AS LanguageName,\r\n" + 
								"    		IFNULL(l.NameEnglish, 'NULL') AS LanguageNameEnglish,\r\n" + 
								"    		IFNULL(l.ShortName, 'NULL') AS LanguageShortName,\r\n" + 
								"    		IFNULL(l.Code, 'NULL') AS LanguageCode\r\n" + 
								"		FROM Transcription t " +
								"        LEFT JOIN\r\n" + 
								"    	(SELECT \r\n" + 
								"        	WP_UserId, UserId\r\n" + 
								"    	FROM\r\n" + 
								"        	User) u ON t.UserId = u.UserId\r\n" + 
								"        LEFT JOIN\r\n" + 
								"    	(" +
								"			SELECT \r\n" + 
								"        		tl.TranscriptionId,\r\n" + 
								"            	GROUP_CONCAT(IFNULL(l.LanguageId, 'NULL') SEPARATOR '&~&') AS LanguageId,\r\n" + 
								"            	GROUP_CONCAT(IFNULL(l.Name, 'NULL') SEPARATOR '&~&') AS Name,\r\n" + 
								"            	GROUP_CONCAT(IFNULL(l.NameEnglish, 'NULL') SEPARATOR '&~&') AS NameEnglish,\r\n" + 
								"            	GROUP_CONCAT(IFNULL(l.ShortName, 'NULL') SEPARATOR '&~&') AS ShortName,\r\n" + 
								"            	GROUP_CONCAT(IFNULL(l.Code, 'NULL') SEPARATOR '&~&') AS Code\r\n" + 
								"    		FROM\r\n" + 
								"        		TranscriptionLanguage tl\r\n" + 
								"    		JOIN Language l ON l.LanguageId = tl.LanguageId\r\n" + 
								"    		WHERE tl.TranscriptionId IN (SELECT TranscriptionId FROM Transcription WHERE ItemId = " + id + ")" +
								"    		GROUP BY tl.TranscriptionId" +	
								"		) " +
								"		l ON t.TranscriptionId = l.TranscriptionId " +
								"		WHERE t.ItemId = " + id +
								"		ORDER BY Timestamp DESC";
			String transcriptionData = TranscriptionResponse.executeQuery(transcriptionQuery, "Select");				
			Type transcriptionType = new TypeToken<List<Transcription>>(){}.getType();
			List<Transcription> transcriptions = gson.fromJson(transcriptionData, transcriptionType);

			timeText += ", transcriptionData: " + ((System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();
			
			String propertyQuery = "SELECT \r\n" + 
							"			ip.ItemId as ItemId,\r\n" + 
							"			p.PropertyId AS PropertyId,\r\n" + 
							"			pt.Name AS PropertyType,\r\n" + 
							"			pt.PropertyTypeId AS PropertyTypeId,\r\n" + 
							"			p.Value AS PropertyValue,\r\n" + 
							"			null AS Motivation,\r\n" + 
							"			null AS MotivationId,\r\n" + 
							"			null AS Editable,\r\n" + 
							"			null AS X_Coord,\r\n" + 
							"			null AS Y_Coord,\r\n" + 
							"			null AS Width,\r\n" + 
							"			null AS Height,\r\n" + 
							"			IFNULL(p.Description, 'NULL') AS PropertyDescription,\r\n" + 
							"			pt.Editable + 0 AS PropertyEditable\r\n" + 
							"		FROM\r\n" + 
							"			ItemProperty ip\r\n" + 
							"				LEFT JOIN\r\n" + 
							"			Property p ON ip.PropertyId = p.PropertyId\r\n" + 
							"				LEFT JOIN\r\n" + 
							"			PropertyType pt ON p.PropertyTypeId = pt.PropertyTypeId\r\n" + 
							"		WHERE ip.ItemId = " + id;
			String propertyData = PropertyResponse.executeQuery(propertyQuery, "Select");		
			Type propertyType = new TypeToken<List<Property>>(){}.getType();
			List<Property> properties = gson.fromJson(propertyData, propertyType);
			
			timeText += ", propertyData: " + ((System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();

			String automatedEnrichmentQuery = "SELECT \r\n" + 
							"			ae.ItemId,\r\n" + 
							"			ae.AutomatedEnrichmentId AS AutomatedEnrichmentId,\r\n" + 
							"			ae.Name  AS Name,\r\n" + 
							"			ae.Type AS Type,\r\n" + 
							"			ae.ExternalId AS ExternalId,\r\n" + 
							"			IFNULL(ae.WikiData, 'NULL') AS WikiData\r\n" + 
							"		FROM\r\n" + 
							"			AutomatedEnrichment ae\r\n" + 
							"		WHERE ae.ItemId =  " + id;
			String automatedEnrichmentData = AutomatedEnrichmentResponse.executeQuery(automatedEnrichmentQuery, "Select");		
			Type automatedEnrichmentType = new TypeToken<List<AutomatedEnrichment>>(){}.getType();
			List<AutomatedEnrichment> automatedEnrichments = gson.fromJson(automatedEnrichmentData, automatedEnrichmentType);
			
			timeText += ", automatedEnrichmentData: " + ((System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();
			
			String placeQuery = "SELECT \r\n" + 
						"			pl.ItemId,\r\n" + 
						"			null AS StoryId,\r\n" +
						"			null AS Title,\r\n" + 
						"			pl.PlaceId AS PlaceId,\r\n" + 
						"			pl.Name AS Name,\r\n" + 
						"			pl.Latitude AS Latitude,\r\n" + 
						"			pl.Longitude AS Longitude,\r\n" + 
						"			pl.Link AS Link,\r\n" + 
						"			pl.Zoom AS Zoom,\r\n" + 
						"			pl.Comment AS Comment,\r\n" + 
						"			pl.UserId AS UserId,\r\n" + 
						"			pl.UserGenerated + 0 AS UserGenerated,\r\n" + 
						"			pl.WikidataName AS WikidataName,\r\n" + 
						"			pl.WikidataId AS WikidataId\r\n" + 
						"		FROM\r\n" + 
						"			Place pl\r\n" + 
						"		WHERE pl.ItemId = " + id;
			String placeData = PlaceResponse.executeQuery(placeQuery, "Select");
			Type placeType = new TypeToken<List<Place>>(){}.getType();
			List<Place> places = gson.fromJson(placeData, placeType);
			
			timeText += ", placeData: " + ((System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();
			
			String personQuery =  "	SELECT \r\n" + 
							"			iperson.ItemId,\r\n" + 
							"			pe.PersonId AS PersonId,\r\n" + 
							"			IFNULL(pe.FirstName, 'NULL') AS FirstName,\r\n" + 
							"			IFNULL(pe.LastName, 'NULL') AS LastName,\r\n" + 
							"			IFNULL(pe.BirthPlace, 'NULL') AS BirthPlace,\r\n" + 
							"			IFNULL(pe.BirthDate, 'NULL') AS BirthDate,\r\n" + 
							"			IFNULL(pe.DeathPlace, 'NULL') AS DeathPlace,\r\n" + 
							"			IFNULL(pe.DeathDate, 'NULL') AS DeathDate,\r\n" + 
							"			IFNULL(pe.Link, 'NULL') AS Link,\r\n" + 
							"			IFNULL(pe.Description, 'NULL') AS Description\r\n" + 
							"		FROM\r\n" + 
							"			ItemPerson iperson \r\n" + 
							"		JOIN Person pe ON iperson.PersonId = pe.PersonId\r\n" + 
							"		WHERE iperson.ItemId = " + id;
			String personData = PersonResponse.executeQuery(personQuery, "Select");
			Type personType = new TypeToken<List<Person>>(){}.getType();
			List<Person> persons = gson.fromJson(personData, personType);
			
			timeText += ", personData: " + ((System.nanoTime() - startTime) / 1000000);
			startTime = System.nanoTime();
			



			item.Properties = properties;
			item.AutomatedEnrichments = automatedEnrichments;
			item.Places = places;
			item.Persons = persons;
			item.Transcriptions = transcriptions;

		    Gson gsonBuilder = new GsonBuilder().create();
		    String result = gsonBuilder.toJson(item);

			ResponseBuilder rBuild = Response.ok(result);
			
			timeText += ", Rest: " + ((System.nanoTime() - startTime) / 1000000);
			File file = new File("/home/enrich/log/itemTiming/" + id + ".txt");
			file.getParentFile().mkdirs();
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(timeText);
		    fileWriter.close();
			//ResponseBuilder rBuild = Response.ok(transcriptionData);
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