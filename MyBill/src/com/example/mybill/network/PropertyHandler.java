package com.example.mybill.network;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mybill.network.BillBean.BillService;
import com.example.mybill.network.BillBean.PropertyRecord;
import com.example.mybill.network.BillBean.PropertyRecord.AssetsRecord;
import com.example.mybill.network.BillBean.PropertyRecord.PocketRecord;
import com.example.mybill.network.BillBean.PropertyRequest;
import com.example.mybill.network.BillBean.PropertyResponse;
import com.example.mybill.network.BillBean.Record;
import com.example.mybill.network.PPIndexManager.PullIndexInfo;
import com.example.mybill.util.DBHelper;
import com.example.rpc.Controller;
import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class PropertyHandler {

	private final String TAG = "PropertyHandler";

	private Context mcontext;
	private DBHelper db_helper;
	private SQLiteDatabase db;
	
	public PropertyHandler(Context context, DBHelper db_helper, SQLiteDatabase db) {
		this.mcontext = context;
		this.db_helper = db_helper;
		this.db = db;
	}
	
	public void fill_request_with_push_property(PropertyRequest.Builder request_builder,
			String record_file, int push_index_lasttime, int push_index_thistime) {
		Map<String, Integer> update_pocket = new HashMap<String/*sid*/, Integer/*action*/>();
		Map<String, Integer> update_assets = new HashMap<String/*sid*/, Integer/*action*/>();
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
            	if (brr.length != 4) {
            		Log.i(TAG, "bad property record line: " + line);
            		continue;
            	}
            	String sid = brr[3];
            	int action = Integer.parseInt(brr[2]);
            	if (brr[1].equals("p")) {
	            	if (update_pocket.containsKey(brr[2])) {
	            		// must be NEW-UPDATE, or UPDATE-UPDATE, so don't need put it again
	            		Log.i(TAG, "pocket " + sid + " already exists");
	            	} else {
	            		update_pocket.put(sid, action);
	            	}
            	} else if (brr[1].equals("a")) {
            		if (update_assets.containsKey(brr[2])) {
	            		// must be NEW-UPDATE, or UPDATE-UPDATE, so don't need put it again
	            		Log.i(TAG, "assets " + sid + " already exists");
	            	} else {
	            		update_assets.put(sid, action);
	            	}
            	}
            }
            db_helper.fill_propertyrequest_push_content(db, update_pocket, update_assets, request_builder);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void fill_request_with_pull_info(PropertyRequest.Builder request_builder, String gay,
			Map<String, PullIndexInfo> pull_index_info, int MAX_RECORD_EACH_TIME) {
		Iterator<Entry<String, PullIndexInfo>> iter = pull_index_info.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String gay_name = (String) entry.getKey();
			// only pull zmkeil's property
			if (!gay_name.equals("zmkeil")) {
				continue;
			}
			PullIndexInfo val = (PullIndexInfo) entry.getValue();
    		int begin_index = val.begin_index;
    		int breakpoint_index = val.breakpoint_index;
    		if (gay.equals("zmkeil")) {
    			// zmkeil only pull when begin_index < breakpoint
    			if ((breakpoint_index > 0) && (begin_index <= breakpoint_index)) {
    				request_builder.setBeginIndex(begin_index).setMaxLine(MAX_RECORD_EACH_TIME);
    			}
    		} else {
    			// other just pull it
    			request_builder.setBeginIndex(begin_index).setMaxLine(MAX_RECORD_EACH_TIME);
    		}
		}
	}
	
	public void sync_others_records(List<PropertyRecord> pull_records) {
		int size = pull_records.size();
		for (int i = 0; i < size; i++) {
			String sid = null;
			int year = 0;
			int month = 0;
	        int day = 0;
	        int money = 0;
	        String item_n = "100";
	        String flowtype_n = "0";
	        String dest_item_n = "0";
	        int is_deleted = 0;
	        
			PropertyRecord record = pull_records.get(i);
			PropertyRecord.PropertyType property_type = record.getPropertyType();
			if (property_type == PropertyRecord.PropertyType.POCKET_MONEY) {
				// pocket
				PocketRecord p_record = record.getPocketRecord();
				sid = p_record.getSid();
				year = p_record.getYear();
				month = p_record.getMonth();
				money = p_record.getMoney();
				is_deleted = p_record.getIsDeleted();
			} else if (property_type == PropertyRecord.PropertyType.FIXED_ASSETS) {
				// assets
				AssetsRecord a_record = record.getAssetsRecord();
				sid = a_record.getSid();
				year = a_record.getYear();
				month = a_record.getMonth();
				day = a_record.getDay();
				item_n = a_record.getStoreAddr().getNumber() + "";
				flowtype_n = a_record.getFlowType().getNumber() + "";
				money = a_record.getMoney();
				dest_item_n = a_record.getStoreAddrOp().getNumber() + "";
				is_deleted = a_record.getIsDeleted();
			}

	        if (record.getType() == PropertyRecord.Type.NEW) {
	        	// if exist, ignore it
	        	if (!db_helper.is_property_exist(db, property_type, sid)) {
	        		db_helper.insert_new_property(db, sid, year, month, day, 
	        				item_n, flowtype_n, money, dest_item_n);
	        	}
	        } else {
	        	Map<String,Object> update_data = new HashMap<String,Object>();
	        	boolean is_pocket = (property_type == PropertyRecord.PropertyType.POCKET_MONEY);
	        	if (is_deleted == 1) {
	        		update_data.put("delete", true);
	        	} else {
	        		if (is_pocket) {
	        			update_data.put("money", money);
	        		} else {
			        	update_data.put("day", day);
			        	update_data.put("money", money);
			        	update_data.put("store_addr", Integer.valueOf(item_n));
			        	update_data.put("flow_type", Integer.valueOf(flowtype_n));
			        	update_data.put("store_addr_op", Integer.valueOf(dest_item_n));
	        		}
	        	}
	        	db_helper.update_property(db, is_pocket, -1, sid, update_data);
	        }
		}
	}
	
	public PropertyResponse update_records_rpc_call(BlockingRpcChannel bchannel, PropertyRequest request) {
		Controller cntl = new Controller();
		BillService.BlockingInterface bstub = BillService.newBlockingStub(bchannel);
		PropertyResponse response = null;
        try {
			response = bstub.property((RpcController)cntl, request);
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}        
		return response;
	}
}
