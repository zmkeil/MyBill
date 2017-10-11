package com.example.mybill.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.util.Log;

import com.example.mybill.util.FileHelper;

public class PPIndexManager {
	public static final String record_property_file = "record_property";
	public static final String record_property_size_file = "record_property_size";
	public static final String index_property_push_file = "index_property_push";
	public static final String index_property_pull_file = "index_property_pull";
	
	private final String TAG = "PPIndexManager";
	FileHelper file_helper;

	// record_file: record_gay_year_month (bill), record_property (property, only for zmkeil)
	// record_size_file: record_gay_size_year_month (bill), record_property_size (property, only for zmkeil)
	// index_push_file: index_gay_push_year_month (bill), index_property_push (property, only for zmkeil)
	// index_pull_file: index_gay_pull (bill), index_property_pull (property), format:
	//		gay \t pull_index \t breakpoint_index
	//		sharer1 \t pull_index
	//		sharer2 \t pull_index
	//		sharer3 \t pull_index
	
	public PPIndexManager(FileHelper file_helper) {
		this.file_helper = file_helper;
	}
	
	public void update_local_index(String record_size_file, String record_file,
			String record_line) {
		// update local record and record_size when local db modify
    	int record_size = 0;
		if (file_helper.fileIsExists(record_size_file)) {
			String record_size_line = file_helper.read(record_size_file);
			if (record_size_line != null) {
				record_size = Integer.parseInt(record_size_line);
			}
		}
    	record_size++;
    	file_helper.save(record_size_file, record_size + "");
    	
    	// record
    	String new_record_line = record_size + "\t" + record_line + "\n";
		Log.i(TAG, record_file + " NEW: " + new_record_line);
    	file_helper.append(record_file, new_record_line);
	}

	public int[] get_record_size_and_push_index(String record_size_file,
			String index_push_file) {
		// record size
		int record_size = 0;
		if (file_helper.fileIsExists(record_size_file)) {
			String record_size_line = file_helper.read(record_size_file);
	    	if (record_size_line != null && record_size_line != "") {
	    		record_size = Integer.parseInt(record_size_line);
	    	}
		}
		
		// index push
    	int index_push = 0;
    	if (file_helper.fileIsExists(index_push_file)) {
	    	String index_push_line = file_helper.read(index_push_file);
	    	if (index_push_line != null && index_push_line != "") {
	    		index_push = Integer.parseInt(index_push_line);
	    	}
    	}
		return new int[]{record_size, index_push};
	}
	
	public class PullIndexInfo {
		public int begin_index;
		public int breakpoint_index;
		public PullIndexInfo(int begin_index, int breakpoint_index) {
			this.begin_index = begin_index;
			this.breakpoint_index = breakpoint_index;
		}
		public void pull_success(int pull_count) {
			this.begin_index += pull_count;
		}
	}
	
	public Map<String, PullIndexInfo> get_pull_index(String gay,
			Set<String> shares, String index_pull_file) {				
		Map<String, PullIndexInfo> pull_info = new HashMap<String, PullIndexInfo>();	
		Map<String, Integer> old_index = new HashMap<String, Integer>();
		if (!file_helper.fileIsExists(index_pull_file)) {
			pull_info.put(gay, new PullIndexInfo(1, 0));
		} else {
			String[] lines = file_helper.read(index_pull_file).split("\n");
			for (String line: lines) {
				Log.i(TAG, "line: " + line);
				String[] arr = line.split("\t");
				if (arr.length != 3) {
					continue;
				}
				if (arr[0].equals(gay)) {
					int pull_index = Integer.parseInt(arr[1]);
					int breakpoint_index = Integer.parseInt(arr[2]);
					Log.i(TAG, "gay " + gay + ", pull_index " + pull_index + ", breakpoint_index " + breakpoint_index);
					pull_info.put(gay, new PullIndexInfo(pull_index, breakpoint_index));
				} else {
					old_index.put(arr[0], Integer.parseInt(arr[1]));
				}
			}			
		}

		// for shares
		for (String sh : shares) {
			int pull_index = old_index.containsKey(sh) ? old_index.get(sh) : 1;
			pull_info.put(sh, new PullIndexInfo(pull_index, 0));
		}
		return pull_info;
	}

	
	public void update_index_files(String gay, Set<String> sharers, 
			String index_push_file, int push_index_thistime, 
			String index_pull_file, Map<String, PullIndexInfo> pull_index_info, int nows_pull_size_for_self) {
		// push_index
        file_helper.save(index_push_file, "" + push_index_thistime);
        
        // pull_index
        String pull_index_str = "";
        // self's
        if (pull_index_info.containsKey(gay)) {
        	PullIndexInfo val = pull_index_info.get(gay);
        	int begin_index = val.begin_index;
			int breakpoint_index = val.breakpoint_index;
			Log.i(TAG, gay + "'s nows_pull_size_for_self: " + nows_pull_size_for_self
					+ ", breakpoint_index: " + breakpoint_index);
			if (breakpoint_index <= 0) {
				breakpoint_index = nows_pull_size_for_self;
				Log.i(TAG, "set self's breakpoint_index: " + breakpoint_index);
			}
			pull_index_str = pull_index_str + gay + "\t" 
					+ begin_index + "\t" + breakpoint_index + "\n";
        }
        // sharers'
        for (String sh : sharers) {
			int begin_index = 0;
			int breakpoint_index = 0;
			if (pull_index_info.containsKey(sh)) {
				begin_index = pull_index_info.get(sh).begin_index;
				breakpoint_index = pull_index_info.get(sh).breakpoint_index;
			}
			pull_index_str = pull_index_str + sh + "\t" 
					+ begin_index + "\t" + breakpoint_index + "\n";
		}
		file_helper.save(index_pull_file, pull_index_str);
	}
}
