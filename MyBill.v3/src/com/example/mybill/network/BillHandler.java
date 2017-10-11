package com.example.mybill.network;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mybill.network.BillBean.BillRequest;
import com.example.mybill.network.BillBean.BillResponse;
import com.example.mybill.network.BillBean.BillService;
import com.example.mybill.network.BillBean.Record;
import com.example.mybill.network.BillBean.BillRequest.PullInfo;
import com.example.mybill.network.BillBean.BillResponse.PullRecords;
import com.example.mybill.network.PPIndexManager.PullIndexInfo;
import com.example.mybill.util.DBHelper;
import com.example.rpc.Controller;
import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class BillHandler {
	private final String TAG = "BillHandler";

	private Context mcontext;
	private DBHelper db_helper;
	private SQLiteDatabase db;
	
	public BillHandler(Context context, DBHelper db_helper, SQLiteDatabase db) {
		this.mcontext = context;
		this.db_helper = db_helper;
		this.db = db;
	}
	
	public void fill_request_with_push_bills(BillRequest.Builder request_builder, String record_file,
			String table_name, int push_index_lasttime, int push_index_thistime, int year, int month) {
		Map<String, Integer> update_records = new HashMap<String, Integer>();
		try {
			FileInputStream recordStream = mcontext.openFileInput(record_file);
			InputStreamReader inputreader = new InputStreamReader(recordStream);
            BufferedReader buffreader = new BufferedReader(inputreader);
    		String line = null;
    		int i = 0;
            while (( line = buffreader.readLine()) != null) {
            	i++;
            	if (i <= push_index_lasttime) {
            		continue;
            	} else if (i > push_index_thistime) {
            		break;
            	}
            	Log.i(TAG, "push_index_lasttime " + push_index_lasttime 
            			+ ", push_index_thistime " + push_index_thistime + ", line " + line);
            	String[] brr = line.split("\t");
            	if (update_records.containsKey(brr[2])) {
            		// must be NEW-UPDATE, or UPDATE-UPDATE, so don't need put it again
            		Log.i(TAG, brr[2] + " already exists");
            	} else {
            		update_records.put(brr[2], Integer.parseInt(brr[1]));
            	}
            }
            db_helper.get_record_content(db, table_name, year, month, update_records, request_builder);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void fill_request_with_pull_info(BillRequest.Builder request_builder, String gay,
			Map<String, PullIndexInfo> pull_index_info, int MAX_RECORD_EACH_TIME) {
		Iterator<Entry<String, PullIndexInfo>> iter = pull_index_info.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String gay_name = (String) entry.getKey();
			PullIndexInfo val = (PullIndexInfo) entry.getValue();
    		int begin_index = val.begin_index;
    		int breakpoint_index = val.breakpoint_index;
    		if ((gay_name.equals(gay)) && ((breakpoint_index <= 0) || (begin_index >= breakpoint_index))) {
    			// if self's breakpoint <= 0, don't pull self's records.
    			// wait for last_push_index returned by the first push
    			Log.i(TAG, gay + " self maybe the first time to push, wait for the nows_pull_size_for_self; or already pull self's all records");
    		} else {
	    		Log.i(TAG, "pull " + gay_name + "'s record from begin_index " + begin_index
	    				+ ", breakpoint_index " + breakpoint_index);
	    		request_builder.addPullInfos(PullInfo.newBuilder()
	    				.setGay(gay_name).setBeginIndex(begin_index)
	    				.setMaxLine(MAX_RECORD_EACH_TIME).build());
    		}
		}
	}
		
	public void sync_others_records(PullRecords pull_record) {
		String gay = pull_record.getGay();
		int size = pull_record.getRecordsCount();
		for (int i = 0; i < size; i++) {
			Record record = pull_record.getRecords(i);
			int year = record.getYear();
			int month = record.getMonth();	        
	        String db_table_name = "bills_of_" + gay + "_"
	        		+ NetworkService.year_present[year - 2015] + "_" + NetworkService.month_present[month - 1];
	        
	        int day = record.getDay();
	        int pay_earn = record.getPayEarn();
	        String consumer = record.getGay();
	        String comment = record.getComments();
	        int cost = record.getCost();
	        int is_deleted = record.getIsDeleted();
	        String sid = record.getId();
	        
	        if (record.getType() == Record.Type.NEW) {
	        	// if exist, ignore it
	        	if (!db_helper.is_bill_exist(db, db_table_name, sid)) {
	        		db_helper.insert_new_bill(db, db_table_name, sid, day,
	        				pay_earn, consumer, comment, cost, is_deleted);
	        	}
	        } else {
	        	Map<String,Object> update_data = new HashMap<String,Object>();
	        	if (is_deleted == 1) {
	        		update_data.put("delete", true);
	        	} else {
		        	update_data.put("day", day);
		        	update_data.put("consumer", consumer);
		        	update_data.put("comment", comment);
		        	update_data.put("cost", cost);
	        	}
	        	db_helper.update_bill(db, db_table_name, -1, sid, update_data);
	        }
		}
	}
	

	public BillResponse update_records_rpc_call(BlockingRpcChannel bchannel, BillRequest request) {
		Controller cntl = new Controller();
		BillService.BlockingInterface bstub = BillService.newBlockingStub(bchannel);
        BillResponse response = null;
        try {
			response = bstub.update((RpcController)cntl, request);
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}        
		return response;
	}

}
