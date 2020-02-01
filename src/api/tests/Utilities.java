package api.tests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Utilities {

	public String getDateTime() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH-mm-ss");
		Date date = Calendar.getInstance().getTime();
		return dateFormat.format(date);
	}
	
	public Object deserializeJsonMessage(String jsonMsg) throws Exception {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(jsonMsg);
		
		if (obj instanceof JSONArray)
			return (JSONArray) obj;
		else if (obj instanceof JSONObject)
			return (JSONObject) obj;
		else
			return "Invalid JSON";
	}
}