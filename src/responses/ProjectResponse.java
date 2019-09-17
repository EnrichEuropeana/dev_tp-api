package responses;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.commons.io.IOUtils;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import objects.ApiKey;
import objects.Dataset;
import objects.Project;
import objects.Story;
import sun.misc.BASE64Encoder;

@Path("/projects")
public class ProjectResponse {


	public String executeQuery(String query, String type) throws SQLException{
		   List<Project> projectList = new ArrayList<Project>();
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
		   if (type != "Select") {
			   int success = stmt.executeUpdate(query);
			   if (success > 0) {
				   return type +" succesful";
			   }
			   else {
				   return type +" could not be executed";
			   }
		   }
		   ResultSet rs = stmt.executeQuery(query);
		   
		   // Extract data from result set
		   while(rs.next()){
		      //Retrieve by column name
			  Project Project = new Project();
			  Project.setProjectId(rs.getInt("ProjectId"));
			  Project.setName(rs.getString("Name"));
			  projectList.add(Project);
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
	    String result = gsonBuilder.toJson(projectList);
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
	
	public String executeDatasetQuery(String query, String type) throws SQLException{
	    List<Dataset> datasetList = new ArrayList<Dataset>();
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
		   if (type != "Select") {
			   int success = stmt.executeUpdate(query);
			   if (success > 0) {
				   return type +" succesful";
			   }
			   else {
				   return type +" could not be executed";
			   }
		   }
		   ResultSet rs = stmt.executeQuery(query);
		   
		   // Extract data from result set
		   while(rs.next()){
		      //Retrieve by column name
			  Dataset Dataset = new Dataset();
			  Dataset.setDatasetId(rs.getInt("DatasetId"));
			  Dataset.setName(rs.getString("Name"));
			  Dataset.setProjectId(rs.getInt("ProjectId"));
			  datasetList.add(Dataset);
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
	    String result = gsonBuilder.toJson(datasetList);
	    return result;
	}
	
	//Get Entries
	@Path("")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response search(@Context UriInfo uriInfo, String body) throws SQLException {
		String query = "SELECT * FROM Project WHERE 1";

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
	
	//Add new entry
	@Path("")
	@POST
	public String add(String body) throws SQLException {	
	    GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
	    Gson gson = gsonBuilder.create();
	    Project project = gson.fromJson(body, Project.class);
	    
	    //Check if all mandatory fields are included
	    if (project.ProjectId != null && project.Name != null) {
			String query = "INSERT INTO Project (ProjectId, Name) "
							+ "VALUES (" + project.ProjectId
							+ ", '" + project.Name + "')";
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
	    JsonObject  changes = gson.fromJson(body, JsonObject.class);
	    
	    //Check if field is allowed to be changed
	    if (changes.get("ProjectId") != null) {
	    	return "Prohibited change attempt";
	    }
	    
	    //Check if NOT NULL field is attempted to be changed to NULL
	    if (changes.get("Name") == null || !changes.get("Name").isJsonNull()){
		    String query = "UPDATE Project SET ";
		    
		    int keyCount = changes.entrySet().size();
		    int i = 1;
			for(Map.Entry<String, JsonElement> entry : changes.entrySet()) {
			    query += entry.getKey() + " = " + entry.getValue();
			    if (i < keyCount) {
			    	query += ", ";
			    }
			    i++;
			}
			query += " WHERE ProjectId = " + id;
			String resource = executeQuery(query, "Update");
			return resource;
	    } else {
	    	return "Prohibited changes to null";
	    }
	}
	

	//Delete entry by id
	@Path("/{id}")
	@DELETE
	public String delete(@PathParam("id") int id) throws SQLException {
		String resource = executeQuery("DELETE FROM Project WHERE ProjectId = " + id, "Delete");
		return resource;
	}


	//Get entry by id
	@Path("/{id}")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getEntry(@PathParam("id") int id, String body) throws SQLException {
		String resource = executeQuery("SELECT * FROM Project WHERE ProjectId = " + id, "Select");
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}

	//Get entry by id
	@Path("/{project_id}/datasets")
	@Produces("application/json;charset=utf-8")
	@GET
	public Response getDatasets(@PathParam("project_id") int projectId, String body) throws SQLException {
		String query = "SELECT * FROM Dataset WHERE ProjectId = " + projectId;
		String resource = executeDatasetQuery(query, "Select");
		if (resource == "Failed") {
			ResponseBuilder rBuild = Response.status(Response.Status.BAD_REQUEST);
	        return rBuild.build();
		}
		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}
	

	public String executeInsertQuery(String query, String type) throws SQLException, ClientProtocolException, IOException{
		try (InputStream input = new FileInputStream("/home/enrich/tomcat/apache-tomcat-9.0.13/webapps/tp-api/WEB-INF/config.properties")) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            final String DB_URL = prop.getProperty("DB_URL");
            final String USER = prop.getProperty("USER");
            final String PASS = prop.getProperty("PASS");
            
    		HttpClient httpclient = HttpClients.createDefault();
    		
            HttpPost httppost = new HttpPost("https://keycloak-server-test.eanadev.org/auth/realms/DataExchangeInfrastructure/protocol/openid-connect/token");
    	
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
    	                JsonObject data = new JsonParser().parse(writer.toString()).getAsJsonObject();

    	    	        //String authHeader = data.get("access_token").toString();
    	    	        //httppost2.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
    	            }
    	        }
        
		
	   // Register JDBC driver
	   try {
			Class.forName("com.mysql.jdbc.Driver");
		
		   // Open a connection
		   Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		   // Execute SQL query
		   Statement stmt = conn.createStatement();
		   if (type != "Select") {
			   int success = stmt.executeUpdate(query);
			   /*
				if (1==1) {
					return query;
				}
				*/
			   if (success > 0) {
				   conn.close();
				   stmt.close();
				   return type +" succesful";
			   }
			   else {
				   conn.close();
				   stmt.close();
				   return "Failed";
			   }
		   }
		   else {
			   conn.close();
			   stmt.close();
			   return "test2";
		   }
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
	   //return "query couldn't be executed";
		return query;
	}
	
	//Get entry by id
	@Path("/{project_id}/stories")
	@POST
	public Response insertStory(@PathParam("project_id") int projectId, @Context UriInfo uriInfo, String body, @Context HttpHeaders headers) throws Exception {

	    FileWriter fileWriter = new FileWriter("request2.txt");
	    fileWriter.write("test");
	    fileWriter.close();
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
		fields.add("edm:landingPage");
		fields.add("dc:creator");
		fields.add("dc:source");
		fields.add("edm:country");
		fields.add("edm:dataProvider");
		fields.add("edm:provider");
		fields.add("edm:rights");
		fields.add("edm:begin");
		fields.add("edm:end");
		fields.add("dc:contributor");
		fields.add("edm:year");
		fields.add("dc:publisher");
		fields.add("dc:coverage");
		fields.add("dc:date");
		fields.add("dc:type");
		fields.add("dc:relation");
		fields.add("dcterms:medium");
		fields.add("edm:datasetName");
		fields.add("edm:isShownAt");
		fields.add("dc:rights");
		fields.add("dc:language");
		fields.add("edm:language");
		boolean placeAdded = false;
	    int keyCount = dataArray.size();

		List<String> keys = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		
		String manifestUrl = "";
		String storyTitle = "";
		String recordId = "";
		String imageLink = "";
		
		if (data.getAsJsonObject().has("iiif_url")) {
			manifestUrl = data.getAsJsonObject().get("iiif_url").getAsString();
		}

		for (int i = 0; i < keyCount; i++) {
			for(Map.Entry<String, JsonElement> entry : dataArray.get(i).getAsJsonObject().entrySet()) {
				if (fields.contains(entry.getKey())) {
					if (entry.getValue().isJsonObject()) {
						if (entry.getValue().getAsJsonObject().has("@value")) {
							if (!keys.contains(entry.getKey())) {
								keys.add(entry.getKey());
								values.add(entry.getValue().getAsJsonObject().get("@value").toString());
								if (entry.getKey().equals("dc:title")) {
									storyTitle = entry.getValue().getAsJsonObject().get("@value").toString();
								}
							}
						}
						else if (entry.getValue().getAsJsonObject().has("@id")) {
							if (!keys.contains(entry.getKey())) {
								keys.add(entry.getKey());
								values.add(entry.getValue().getAsJsonObject().get("@id").toString());
								if (entry.getKey().equals("dc:title")) {
									storyTitle = entry.getValue().getAsJsonObject().get("@id").toString();
								}
							}
						}
					}
					else if (entry.getValue().isJsonArray()){	
						if (!keys.contains(entry.getKey())) {
							String key = "";
							String value = "";
							for (int j = 0; j < entry.getValue().getAsJsonArray().size(); j++) {
								if (entry.getValue().getAsJsonArray().get(j).isJsonObject()) {
									JsonObject element = entry.getValue().getAsJsonArray().get(j).getAsJsonObject();
									if (key == "") {
										if (element.has("@value")) {
											key = entry.getKey();
											value = element.get("@value").toString();
										}
										if (element.has("@id")) {
											key = entry.getKey();
											value = element.get("@id").toString();
										}
									}
									else {
										if (element.has("@language") && element.get("@language").toString().contains("en")) {
											key = entry.getKey();
											value = element.get("@value").toString();
										}
									}
								}
								else {
									if (key == "") {
										key = entry.getKey();
										value = entry.getValue().getAsJsonArray().get(j).toString();
									}
								}
							}
							if (key != "" && value != "") {
								keys.add(key);
								values.add(value);	
								if (entry.getKey().equals("dc:title")) {
									storyTitle = "\"" + value.replace(",", " | ").replaceAll("[\"{}\\[\\]]", "") + "\"";
								}						
							}
						}
					}
					else {
						if (!keys.contains(entry.getKey())) {
							keys.add(entry.getKey());
							values.add("\"" + entry.getValue().toString().replace(",", " | ").replaceAll("[\"{}\\[\\]]", "") + "\"");
							if (entry.getKey().equals("dc:title")) {
								storyTitle = "\"" + entry.getValue().toString().replace(",", " | ").replaceAll("[\"{}\\[\\]]", "") + "\"";
							}
						}
					}
				}
				else {
					if (entry.getKey().equals("iiif_url")) {
						manifestUrl = entry.getValue().getAsString();
					}
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
					else {
						if (entry.getKey().equals("@type") && entry.getValue().getAsString().equals("edm:WebResource")) {
							if (dataArray.get(i).getAsJsonObject().keySet().contains("dcterms:isReferencedBy")){
								if (dataArray.get(i).getAsJsonObject().get("dcterms:isReferencedBy").isJsonObject()
										&& dataArray.get(i).getAsJsonObject().get("dcterms:isReferencedBy").getAsJsonObject().get("@id").getAsString().endsWith("manifest.json")) {
									if (manifestUrl == "") {
										manifestUrl = dataArray.get(i).getAsJsonObject().get("dcterms:isReferencedBy").getAsJsonObject().get("@id").getAsString();
									}
									//manifestUrl = dataArray.get(i).getAsJsonObject().get("dcterms:isReferencedBy").getAsJsonObject().get("@id").getAsString();
								}
							}
							else {
								imageLink = dataArray.get(i).getAsJsonObject().get("@id").toString();
							}
						}
						else if (entry.getKey().equals("@type") && entry.getValue().getAsString().equals("edm:ProvidedCHO")) {
							if (dataArray.get(i).getAsJsonObject().keySet().contains("@id")){
								if (dataArray.get(i).getAsJsonObject().get("@id").getAsString().contains("europeana.eu")) {
									String[] arr = dataArray.get(i).getAsJsonObject().get("@id").getAsString().split("/");
									recordId = "/" + arr[arr.length - 2] + "/" + arr[arr.length - 1];
								}
								recordId = dataArray.get(i).getAsJsonObject().get("@id").getAsString();
							}
						}
					}
				}
			}
		}

		keys.add("PlaceUserGenerated");
		values.add("1");
		keys.add("ProjectId");
		values.add("" + projectId);
		
		String query = "";
		query += "INSERT INTO Story (";

	    query += "ExternalRecordId, ";
		Iterator<String> keysIterator = keys.iterator();
	    while (keysIterator.hasNext()) {
			query += "`" + keysIterator.next() + "`";
	        if (keysIterator.hasNext()) {
	        	query += ", ";
	        }
		}
	    query += ") VALUES (";
	    query += "\"" + recordId + "\", ";
		Iterator<String> valuesIterator = values.iterator();
	    while (valuesIterator.hasNext()) {
			query += valuesIterator.next();
	        if (valuesIterator.hasNext()) {
	        	query += ", ";
	        }
		}
	    query += ")";
		String resource = executeInsertQuery(query, "Import");
		if (resource == "Failed") {
			ResponseBuilder rBuild = Response.status(Response.Status.BAD_REQUEST);
	        return rBuild.build();
		}

		String itemQuery = "";
		if (manifestUrl == "") {
			itemQuery = "";
			itemQuery += "INSERT INTO Item ("
					+ "Title, "
					+ "StoryId, "
					+ "ImageLink, "
					+ "OrderIndex, "
					+ "Manifest"
					+ ") "
					+ "VALUES ("
					+ "\"" + storyTitle.replace("\"", "") + " Item "  + "1" + "\"" +  ", "
					+ "(SELECT StoryId FROM Story ORDER BY StoryId DESC LIMIT 1), "
					+ "\"" + imageLink.replace("\"", "") + "\"" + ", "
					+ "1" + ", "
					+ "\"" + manifestUrl + "\"" + ")";
			String itemResponse = executeInsertQuery(itemQuery, "Import");
			if (itemResponse == "Failed") {
				ResponseBuilder rBuild = Response.status(Response.Status.BAD_REQUEST);
		        return rBuild.build();
			}
		}
		else {
			try (InputStream input = new FileInputStream("/home/enrich/tomcat/apache-tomcat-9.0.13/webapps/tp-api/WEB-INF/config.properties")) {

	            Properties prop = new Properties();

	            // load a properties file
	            prop.load(input);

	            // get the property value and print it out
	            final String DB_URL = prop.getProperty("DB_URL");
	            final String USER = prop.getProperty("USER");
	            final String PASS = prop.getProperty("PASS");
	            
	    		HttpClient httpclient = HttpClients.createDefault();
	    		
	            HttpPost httppost = new HttpPost("https://keycloak-server-test.eanadev.org/auth/realms/DataExchangeInfrastructure/protocol/openid-connect/token");
    	
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
    	    	        //httppost2.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

        	            URL url = new URL(manifestUrl);
        				HttpURLConnection con = (HttpURLConnection) url.openConnection();
						
						con.setRequestMethod("GET");
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
						
    					JsonObject manifest = new JsonParser().parse(content.toString()).getAsJsonObject();
    					
    					JsonArray imageArray = manifest.get("sequences").getAsJsonArray().get(0).getAsJsonObject().get("canvases").getAsJsonArray();
    					int imageCount = imageArray.size();
    	
    					itemQuery = "INSERT INTO Item ("
    							+ "Title, "
    							+ "StoryId, "
    							+ "ImageLink, "
    							+ "OrderIndex, "
    							+ "Manifest"
    							+ ") VALUES ";
    					for (int i = 0; i < imageCount; i++) {
    						imageLink = imageArray.get(i).getAsJsonObject().get("images").getAsJsonArray().get(0).getAsJsonObject().get("resource").getAsJsonObject().toString();

    						if (i == 0) {
    							itemQuery += "("
    							+ "\"" + storyTitle.replace("\"", "") + " Item "  + (i + 1) + "\"" +  ", "
    							+ "(SELECT StoryId FROM Story ORDER BY StoryId DESC LIMIT 1), "
    							+ "\"" + imageLink.replace("\"", "\\\"") + "\"" + ", "
    							+ (i + 1) + ", "
    							+ "\"" + manifestUrl + "\"" + ")";
    						}
    						else {
    							itemQuery += ", ("
    									+ "\"" + storyTitle.replace("\"", "") + " Item "  + (i + 1) + "\"" +  ", "
    									+ "(SELECT StoryId FROM Story ORDER BY StoryId DESC LIMIT 1), "
    									+ "\"" + imageLink.replace("\"", "\\\"") + "\"" + ", "
    									+ (i + 1) + ", "
    									+ "\"" + manifestUrl + "\"" + ")";
    						}
    					}
    					String itemResponse = executeInsertQuery(itemQuery, "Import");
    					
    					
    					if (itemResponse == "Failed") {
    						ResponseBuilder rBuild = Response.status(Response.Status.BAD_REQUEST);
    				        return rBuild.build();
    					}
    	            }
    	        }
			}
		}

		ResponseBuilder rBuild = Response.ok(resource);
        return rBuild.build();
	}
}


