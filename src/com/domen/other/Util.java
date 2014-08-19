package com.domen.other;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.JarException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 工具类
 * @author hankwing
 *
 */
public class Util {
	
	/**
	 * 解析话题json数据 例如
	 * ［{theme: [ {1},{2},{3}……] },{theme: [{1}, {2}, {3}… ... ]},{… ...}］

	 * @param jsonStr
	 * @return
	 * @throws JarException
	 * @throws JSONException
	 */
	public static ArrayList<ArrayList<HashMap<String, Object>>> AnalysisNotes(
			String jsonStr) throws JarException, JSONException {

		ArrayList<ArrayList<HashMap<String, Object>>> listWrapper = new ArrayList<ArrayList<HashMap<String, Object>>>();				//存放3个话题类型的list
		ArrayList<HashMap<String, Object>> societyList = new ArrayList<HashMap<String, Object>>();
		//ArrayList<HashMap<String, Object>> scienceList = new ArrayList<HashMap<String, Object>>();
		//ArrayList<HashMap<String, Object>> envirList = new ArrayList<HashMap<String, Object>>();
		listWrapper.add(societyList);
		//listWrapper.add(scienceList);
		//listWrapper.add(envirList);
		
		JSONArray array = new JSONArray(jsonStr);
		for (int i = 0; i < array.length(); i++)
		{
			JSONObject jsonObject = array.getJSONObject(i);							//{theme:[{1},... ...}]
			JSONArray subArray = jsonObject.getJSONArray("theme");				//[{1},{2},... ...]
			HashMap<String, Object> tempMap = new HashMap<String, Object>();
			for	(int j = 0; j < subArray.length(); j++ )
			{
				JSONObject singleTopics = subArray.getJSONObject(j);
				tempMap.put("id", "" + singleTopics.getInt("id"));
				tempMap.put("topics", singleTopics.getString("topics"));
				tempMap.put("type", singleTopics.getString("type"));
				tempMap.put("positive", singleTopics.getString("positive"));
				tempMap.put("negative", singleTopics.getString("negative"));
				tempMap.put("url", singleTopics.getString("url"));
				listWrapper.get(i).add(tempMap);
			}
		}
		return listWrapper;
	}
}
