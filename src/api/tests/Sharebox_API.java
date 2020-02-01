package api.tests;

import java.io.File;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Reporter;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import io.restassured.RestAssured;

public class Sharebox_API extends Methods {
	
	private String tokenPri, tokenSec;
	
	@BeforeTest
	public void beforeTest() throws Exception {
		RestAssured.baseURI = "https://ec2-13-232-224-131.ap-south-1.compute.amazonaws.com/sharebox/api";
		RestAssured.useRelaxedHTTPSValidation();
		tokenPri = "8fb26496-e39d-471a-9c56-e4a6fb793a2f"; //Token from Account 1
		tokenSec = "93cbd0c5-a4ba-4554-87c8-90109b925a84"; //Token from Account 2
	}
	
	@Test
	public void verify_files_list() throws Exception {
		File filesSchema = new File(System.getProperty("user.dir") + File.separator + "resources" + File.separator + "Files.json");
		
		getAllFileDetails(tokenPri, "Dashboard");
		Reporter.log("Get All Files from Dashboard Request is Successful");
		validateJsonWithSchema(filesSchema, response.asString(), "JSONArray");
		
		getAllFileDetails(tokenPri, "Inbox");
		Reporter.log("Get All Files from Inbox Request is Successful");
		validateJsonWithSchema(filesSchema, response.asString(), "JSONArray");
	}
	
	@Test
	public void verify_upload_file() throws Exception {
		File filesSchema = new File(System.getProperty("user.dir") + File.separator + "resources" + File.separator + "Files.json");
		File uploadSchema = new File(System.getProperty("user.dir") + File.separator + "resources" + File.separator + "UploadFile.json");
		
		uploadFileDetails(tokenPri, "Auto_" + getDateTime(), "50");
		Reporter.log("Post Request to Upload File is successful");
		validateJsonWithSchema(uploadSchema, response.asString(), "JSONObject");
		
		JSONObject jObj = (JSONObject) deserializeJsonMessage(response.asString());
		String fileId = (String) jObj.get("fileId");
		
		Reporter.log("FileId: " + fileId);
		
		getFileDetailsByFileId(tokenPri, fileId);
		validateJsonWithSchema(filesSchema, response.asString(), "JSONObject");
		Reporter.log("Request for Get File Details by FileId is successful");
				
		String fileDetails = response.asString();
		
		getAllFileDetails(tokenPri, "Dashboard");
		validateJsonWithSchema(filesSchema, response.asString(), "JSONArray");
		
		if (!response.asString().contains(fileDetails)) {
			throw new Exception("Uploaded File is not listed as expected");
		}
		Reporter.log("Uploaded File details is verified in All Files List");
	}
	
	@Test
	@Parameters({"FileId"})
	public void verify_delete_file(String fileId) throws Exception {
		File deleteSchema = new File(System.getProperty("user.dir") + File.separator + "resources" + File.separator + "DeleteFile.json");
		
		if (fileId.equals(null)) {
			throw new Exception("File ID is invalid");
		}
		
		deleteFileDetails(tokenPri, fileId);
		validateJsonWithSchema(deleteSchema, response.asString(), "JSONObject");

		JSONObject jObj = (JSONObject) deserializeJsonMessage(response.asString());
		String message = (String) jObj.get("message");
		
		if (message.equals("File deleted successfully")) {
			Reporter.log("File delete request is successful");
		} else {
			throw new Exception("File delete request is not successful");
		}
		
		getFileDetailsByFileId(tokenPri, fileId);
		
		if (response.getStatusCode() == 200) {
			throw new Exception("Deleted file is not removed from the backend");
		}
		
		getAllFileDetails(tokenPri, "Dashboard");
		
		JSONArray jArray = (JSONArray) deserializeJsonMessage(response.asString());
		
		for (Object obj : jArray) {
			JSONObject jObject = (JSONObject) obj;
			
			if (jObject.get("fileId").equals(fileId)) {
				throw new Exception("Deleted file is not removed from the list");
			}
		}
	}
	
	@Test
	@Parameters({"ShareTo", "FileId"})
	public void verify_share_file_to_another_account(String shareTo, String fileId) throws Exception {
		File fileSchema = new File(System.getProperty("user.dir") + File.separator + "resources" + File.separator + "ShareFile.json");
		String shareId = "NA";
		
		getUserDetails(tokenPri);
		
		JSONArray jsonArray = (JSONArray) deserializeJsonMessage(response.asString());
		
		for (Object obj : jsonArray) {
			JSONObject jObj = (JSONObject) obj;
			
			if (jObj.get("email").equals(shareTo)) {
				shareId = String.valueOf(jObj.get("id"));
				break;
			}
		}
		
		if (shareId.equals("NA")) {
			throw new Exception("Unable to find the user detail");
		}
		
		shareFileDetails(tokenPri, fileId, shareId);
		validateJsonWithSchema(fileSchema, response.asString(), "JSONObject");

		JSONObject jObj = (JSONObject) deserializeJsonMessage(response.asString());
		String message = (String) jObj.get("message");
		
		if (message.equals("Successfully Shared")) {
			Reporter.log("File share request is successful");
		} else {
			throw new Exception("File share request is not successful");
		}
	}
	
	@Test
	@Parameters({"FileId", "Status"})
	public void verify_accept_reject_shared_file(String fileId, String status) throws Exception {
		boolean flag = false;
		File fileSchema = new File(System.getProperty("user.dir") + File.separator + "resources" + File.separator + "AcceptRejectFile.json");
		
		getAllFileDetails(tokenSec, "Inbox");
		
		JSONArray jsonArray = (JSONArray) deserializeJsonMessage(response.asString());
		
		for (Object obj : jsonArray) {
			JSONObject jObj = (JSONObject) obj;
			
			if (jObj.get("fileId").equals(fileId)) {
				Reporter.log("File received in Inbox");
				flag = true;
				break;
			}
		}
		
		if (!flag) {
			throw new Exception("File shared is not listed");
		}
		
		if (status.equals("Accept")) {
			acceptFileDetails(tokenSec, fileId);
		} else {
			rejectFileDetails(tokenSec, fileId);
		}
		
		validateJsonWithSchema(fileSchema, response.asString(), "JSONObject");
		Reporter.log("File Shared is " + status + "ed");
		
		if (status.equals("Accept")) {
			getAllFileDetails(tokenSec, "Dashboard");
			
			JSONArray jArray = (JSONArray) deserializeJsonMessage(response.asString());
			for (Object obj : jArray) {
				JSONObject jObj = (JSONObject) obj;
				
				if (jObj.get("fileId").equals(fileId)) {
					Reporter.log("File accepted is listed in Dashboard");
					break;
				}
			}
			
		} else {
			getAllFileDetails(tokenSec, "Inbox");
			
			JSONArray jArray = (JSONArray) deserializeJsonMessage(response.asString());
			for (Object obj : jArray) {
				JSONObject jObj = (JSONObject) obj;
				
				if (jObj.get("fileId").equals(fileId)) {
					throw new Exception("Rejected file is still listed in Inbox");
				}
			}
		}
	}
}