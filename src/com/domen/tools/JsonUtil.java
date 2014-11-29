package com.domen.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 工具类
 * 
 * @author hankwing
 * 
 */
public class JsonUtil {

	/**
	 * 解析话题json数据 例如 ［{theme: [ {1},{2},{3}……] },{theme: [{1}, {2}, {3}… ...
	 * ]},{… ...}］
	 * 
	 * @param jsonStr
	 * @return
	 * @throws JarException
	 * @throws JSONException
	 */
	public static ArrayList<HashMap<String, Object>> AnalysisTopics(
			String jsonStr, String type) throws JarException, JSONException {

		if (type.compareTo("-1") == 0) {
			// 返回的为新数据
			// ArrayList<ArrayList<HashMap<String, Object>>> listWrapper = new
			// ArrayList<ArrayList<HashMap<String, Object>>>(); //存放3个话题类型的list
			ArrayList<HashMap<String, Object>> singleThemeList = new ArrayList<HashMap<String, Object>>();
			JSONArray array = new JSONArray(jsonStr); // {1},{2},{3}
			for (int i = 0; i < array.length(); i++) {
				// 对每一个话题
				HashMap<String, Object> tempMap = new HashMap<String, Object>();
				JSONObject singleTopics = array.getJSONObject(i);
				tempMap.put("id", "" + singleTopics.getInt("id"));
				tempMap.put("topicName", singleTopics.getString("topicName"));
				tempMap.put("url", singleTopics.getString("url"));
				tempMap.put("date", singleTopics.getString("date"));
				singleThemeList.add(tempMap);
			}
			return singleThemeList;
		} else {
			// 返回的为旧数据 待处理
			return null;
		}
	}

	/**
	 * 分析已有话题的房间信息 run in a thread
	 * @param jsonStr
	 * @param roomData
	 * @throws JarException
	 * @throws JSONException
	 */
	public static void AnalysisRoomList(
			String jsonStr, ArrayList<Map<String, String>> roomData) throws JarException, JSONException {
		// 返回的为新数据
		// ArrayList<ArrayList<HashMap<String, Object>>> listWrapper = new
		// ArrayList<ArrayList<HashMap<String, Object>>>(); //存放3个话题类型的list
		//ArrayList<HashMap<String, Object>> singleThemeList = new ArrayList<HashMap<String, Object>>();
		JSONArray array = new JSONArray(jsonStr); // {1},{2},{3}
		for (int i = 0; i < array.length(); i++) {
			// 对每一个房间
			HashMap<String, String> tempMap = new HashMap<String, String>();
			JSONObject singleTopics = array.getJSONObject(i);
			String cat = singleTopics.getString("positive1");
			String catNickName = cat.substring(0, cat.indexOf("@"));
			String catBareJID = cat.substring(0, cat.indexOf("/"));
			
			String dog = singleTopics.getString("negative1");
			String dogNickName = dog.substring(0, dog.indexOf("@"));
			String dogBareJID = dog.substring(0, dog.indexOf("/"));
			//room = new MultiUserChat(MXMPPConnection.getInstance(), singleTopics.getString("roomJID"));
			tempMap.put("roomJID", singleTopics.getString("roomJID"));
			tempMap.put("positive1JID", cat);
			tempMap.put("positive1NickName", catNickName);
			tempMap.put("positive1BAREJID", catBareJID);
			tempMap.put("negative1JID", dog);
			tempMap.put("negative1NickName", dogNickName);
			tempMap.put("negative1BAREJID", dogBareJID);
			//tempMap.put("positiveMember", value);
			roomData.add(tempMap);
		}

	}
	

	/**
	 * analysis topic occupants count json into int array
	 * @param jsonStr
	 * @param data
	 * @throws JarException
	 * @throws JSONException
	 */
	public static void AnalysisTopicOccuCount(
			String jsonStr, int data[] ,int pageNumber) throws JarException, JSONException {
		
		JSONArray array = new JSONArray(jsonStr); // [1,2,3]
		int index = array.getInt(0);
		pageNumber = index / 20;
		for (int i = 1 ; i < array.length() ; i++, index++) {
			
			data[index] = array.getInt(i);
		}

	}
	
}
