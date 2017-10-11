package com.example.mybill.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetsUtil {

	private static AssetsUtil instance;
	
	public Map<String, String> assets_item_map = new HashMap<String, String>();
	public Map<String, String> assets_action_map = new HashMap<String, String>();
	
	public List<AdaptorContent> assets_item_list;
	public List<AdaptorContent> assets_item_withp_list;
	public List<AdaptorContent> assets_item_withall_list;

	public List<AdaptorContent> assets_action_list;	
    
	private AssetsUtil () {
		assets_item_map.put("0", "ZM余额宝");
		assets_item_map.put("1", "ZM基金");
		assets_item_map.put("2", "ZM铜板街");
		assets_item_map.put("3", "ZM微贷");
		assets_item_map.put("4", "ZM苏州银行");
		assets_item_map.put("5", "JXJ铜板街");
		assets_item_map.put("6", "JXJ陆金所");
		assets_item_map.put("7", "JXJ苏州银行");
		List<Map.Entry<String, String>> item_map_entry = 
				new ArrayList<Map.Entry<String, String>>(assets_item_map.entrySet());
		Collections.sort(item_map_entry, new Comparator<Map.Entry<String, String>>() {   
		    public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {      
		        //return (o2.getValue() - o1.getValue()); 
		        return (o1.getKey()).toString().compareTo(o2.getKey());
		    }
		}); 
		
		assets_item_list = new ArrayList<AdaptorContent>();
		for (Map.Entry<String, String> entry : item_map_entry) {
			assets_item_list.add(new AdaptorContent(entry.getValue(), entry.getKey()));
		}
		
		assets_item_withp_list = new ArrayList<AdaptorContent>();
		for (Map.Entry<String, String> entry : item_map_entry) {
			assets_item_withp_list.add(new AdaptorContent(entry.getValue(), entry.getKey()));
		}
		assets_item_withp_list.add(new AdaptorContent("零钱", "100"));

		assets_item_withall_list = new ArrayList<AdaptorContent>();
		assets_item_withall_list.add(new AdaptorContent("ALL", "100"));
		for (Map.Entry<String, String> entry : item_map_entry) {
			assets_item_withall_list.add(new AdaptorContent(entry.getValue(), entry.getKey()));
		}

		
		assets_action_map.put("0", "结入");
		assets_action_map.put("1", "利息");
		assets_action_map.put("2", "支取");
		assets_action_map.put("3", "偿债");
		assets_action_map.put("4", "转移");
		List<Map.Entry<String, String>> action_map_entry = 
				new ArrayList<Map.Entry<String, String>>(assets_action_map.entrySet());
		Collections.sort(action_map_entry, new Comparator<Map.Entry<String, String>>() {   
		    public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {      
		        //return (o2.getValue() - o1.getValue()); 
		        return (o1.getKey()).toString().compareTo(o2.getKey());
		    }
		}); 
		
		assets_action_list = new ArrayList<AdaptorContent>();
		for (Map.Entry<String, String> entry : action_map_entry) {
			assets_action_list.add(new AdaptorContent(entry.getValue(), entry.getKey()));
		}
	}
	
	public static AssetsUtil getInstance() {
		if (instance == null) {
			instance = new AssetsUtil(); 
		}
		return instance;
	}
}
