package com.example.mybill.network;

import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.mybill.network.BillBean.BillService;
import com.example.mybill.network.BillBean.PropertyRecord;
import com.example.mybill.network.BillBean.PropertyRequest;
import com.example.mybill.network.BillBean.PropertyResponse;
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
	
	public void fill_request_with_push_bills(PropertyRequest.Builder request_builder, String record_file,
			String table_name, int push_index_lasttime, int push_index_thistime, int year, int month) {
		
	}
	
	public void fill_request_with_pull_info(PropertyRequest.Builder request_builder, String gay,
			Map<String, PullIndexInfo> pull_index_info, int MAX_RECORD_EACH_TIME) {
		
	}
	
	public void sync_others_records(PropertyRecord pull_record) {
		
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
