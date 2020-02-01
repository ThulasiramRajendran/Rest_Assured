package api.tests;

import java.io.File;
import java.io.FileInputStream;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.testng.Reporter;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class Methods extends Utilities {

	public RequestSpecification request;
	public Response response;
	
	public Response getAllFileDetails(String token, String path) throws Exception {
		request = RestAssured.given();
		
		if (path.equals("Inbox")) {
			response = request.get("/files?token=" + token + "&getSharedFiles=" + path);
		} else {
			response = request.get("/files?token=" + token);
		}
		
		System.out.println(response.asString());
		
		if (response.getStatusCode() != 200) {
			throw new Exception("Failed - Status (" + response.getStatusCode() + ") - Method: Get All Files");
		} return response;
	}
	
	public Response getFileDetailsByFileId(String token, String fileId) throws Exception {
		request = RestAssured.given();
		response = request.get("/upload?token=" + token + "&fileId=" + fileId);
		
		System.out.println(response.asString());
		
		if (response.getStatusCode() != 200) {
			throw new Exception("Failed - Status (" + response.getStatusCode() + ") - Method: Get File Details By File ID");
		} return response;
	}
	
	public Response uploadFileDetails(String token, String name, String size) throws Exception {
		request = RestAssured.given();
		response = request.post("/upload?token=" + token + "&name=" + name + "&hash=" + name + "&size=" + size);
		
		System.out.println(response.asString());
		
		if (response.getStatusCode() != 200) {
			throw new Exception("Failed - Status (" + response.getStatusCode() + ") - Method: Upload File Details");
		} return response;
	}
	
	public Response updateFileUploadStatus(String token, String fileId, String size) throws Exception {
		request = RestAssured.given();
		response = request.put("/upload?token=" + token + "&fileId=" + fileId + "&bytesCompleted=" + size);
		
		System.out.println(response.asString());
		
		if (response.getStatusCode() != 200) {
			throw new Exception("Failed - Status (" + response.getStatusCode() + ") - Method: Update File Upload Status");
		} return response;
	}
	
	public Response deleteFileDetails(String token, String fileId) throws Exception {
		request = RestAssured.given();
		response = request.delete("/files?token=" + token + "&fileId=" + fileId);
		
		System.out.println(response.asString());
		
		if (response.getStatusCode() != 200) {
			throw new Exception("Failed - Status (" + response.getStatusCode() + ") - Method: Delete File Details");
		} return response;
	}
	
	public Response shareFileDetails(String token, String fileId, String shareId) throws Exception {
		request = RestAssured.given();
		response = request.post("/files?token=" + token + "&fileId=" + fileId + "&shareTo=" + shareId);
		
		System.out.println(response.asString());
		
		if (response.getStatusCode() != 200) {
			throw new Exception("Failed - Status (" + response.getStatusCode() + ") - Method: Share File to Another User");
		} return response;
	}
	
	public Response rejectFileDetails(String token, String fileId) throws Exception {
		request = RestAssured.given();
		response = request.put("/files?token=" + token + "&fileId=" + fileId + "&isAccepted=false");
		
		System.out.println(response.asString());
		
		if (response.getStatusCode() != 200) {
			throw new Exception("Failed - Status (" + response.getStatusCode() + ") - Method: Reject File");
		} return response;
	}
	
	public Response acceptFileDetails(String token, String fileId) throws Exception {
		request = RestAssured.given();
		response = request.put("/files?token=" + token + "&fileId=" + fileId + "&isAccepted=true");
		
		System.out.println(response.asString());
		
		if (response.getStatusCode() != 200) {
			throw new Exception("Failed - Status (" + response.getStatusCode() + ") - Method: Accept File");
		} return response;
	}
	
	public Response getUserDetails(String token) throws Exception {
		request = RestAssured.given();
		response = request.get("/users?token=" + token);
		
		System.out.println(response.asString());
		
		if (response.getStatusCode() != 200) {
			throw new Exception("Failed - Status (" + response.getStatusCode() + ") - Method: Get All User Details");
		} return response;
	}
	
	public void validateJsonWithSchema(File fileSchema, String json, String jsonType) throws Exception {
		try {
			JSONObject jsonSchema = new JSONObject(new JSONTokener(new FileInputStream(fileSchema)));
			Schema schema = SchemaLoader.load(jsonSchema);
			
			if (jsonType.equals("JSONArray")) {
				JSONArray jsonArray = new JSONArray(new JSONTokener(json));
				
				for (Object obj : jsonArray) {
					schema.validate((JSONObject) obj);
				}
			} else if (jsonType.equals("JSONObject")) {
				JSONObject jsonObject = new JSONObject(new JSONTokener(json));				
				schema.validate(jsonObject);
			} else {
				throw new JSONException("Type for the json is not as expected");
			}

			Reporter.log("Response JSON matched with expected JSON Schema");
		} catch (ValidationException ex) {
			throw new Exception("Response JSON does not match with JSON Schema");
		} catch (JSONException ex) {
			throw new Exception("Type for the json is not as expected");
		}
	}
}