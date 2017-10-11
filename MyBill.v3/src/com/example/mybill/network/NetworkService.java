package com.example.mybill.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.example.mybill.util.DBHelper;
import com.example.mybill.util.FileHelper;
import com.example.mybill.network.BillBean.BillRequest.PullInfo;
import com.example.mybill.network.BillBean.BillResponse.PullRecords;
import com.example.mybill.network.BillBean.Record;
import com.example.mybill.network.EchoServiceBeann.EchoRequest;
import com.example.mybill.network.EchoServiceBeann.EchoResponse;
import com.example.mybill.network.EchoServiceBeann.EchoService;
import com.example.mybill.network.BillBean.BillRequest;
import com.example.mybill.network.BillBean.BillResponse;
import com.example.mybill.network.BillBean.BillService;
import com.example.mybill.network.PPIndexManager.PullIndexInfo;
import com.example.rpc.BlockChannel;
import com.example.rpc.ChannelOption;
import com.example.rpc.Controller;
import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class NetworkService extends Service {
	
	private final String TAG = "NetworkService";
	final static public String[] year_present = new String[]{"2015", "2016", "2017", "2018"};
	final static public String[] month_present = new String[]{"01", "02", "03", "04",
			"05","06","07","08","09","10","11","12"};
	final static public String[] day_present = new String[]{"01", "02", "03", "04",
			"05","06","07","08","09","10","11","12","13","14","15","16","17","18","19",
			"20","21","22","23","24","25","26","27","28","29","30","31"};
	
	private final int MAX_RECORD_EACH_TIME = 2;
    
	private FileHelper file_helper;
	private PPIndexManager pp_index_manager;
	
	private DBHelper db_helper;
	private SQLiteDatabase db;
	private BillHandler bill_handler;
	
	private BlockChannel bchannel = null;
	private Context mcontext;
	
	@Override
    public void onCreate() {
        super.onCreate();
        mcontext = this;
        file_helper = new FileHelper(this);
        pp_index_manager = new PPIndexManager(file_helper);
        
        db_helper = new DBHelper(this);
        db = db_helper.getReadableDatabase();
        bill_handler = new BillHandler(this, db_helper, db);
        
    	String remote_address = PreferenceManager.getDefaultSharedPreferences(this).getString("remote_address", "127.0.0.1:8888");
		Log.i(TAG, "remote address: " + remote_address);
		String[] addr = remote_address.split(":");
		String ip = addr[0];
		String port = addr[1];

        ChannelOption option = new ChannelOption(ip, Integer.valueOf(port));               
        bchannel = new BlockChannel(option);
    }
	
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {

    	Timer timer = new Timer();
    	timer.schedule(new MyTimerTask(), 1000/*ms*/, 30000);
    	
        return super.onStartCommand(intent, flags, startId);        
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        //mp.stop();
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			
			// first running, init the record.txt and index.txt
			/*file_helper.deleteFile(get_current_record_file());
			file_helper.deleteFile(get_current_index_push_file());
			file_helper.deleteFile(get_current_index_pull_file());
			file_helper.save(get_current_record_size_file(), "3");*/
			
			/*db_helper.drop_bills(db, "bills_of_jxj_2016_04");
			db_helper.drop_bills(db, "bills_of_zmkeil_2016_04");
			db_helper.drop_bills(db, "bills_of_zmkeil_2017_08");*/
			
			boolean use_network = PreferenceManager.getDefaultSharedPreferences(mcontext).getBoolean("use_network", false);
			if (use_network) {
				Log.i(TAG, "network task running");
				update_records();
			} else {
				Log.i(TAG, "network task not enable");
			}
		}			
	}
	
	private void update_records(String record_file, String record_size_file,
			String index_push_file, String index_pull_file) {		
		String[] arr = record_file.split("_");
		String gay = arr[1];
		Set<String> sharers = get_sharers();
		int year = Integer.parseInt(arr[2]);
		int month = Integer.parseInt(arr[3]);
		String table_name = "bills_of_" + gay + "_" + year_present[year - 2015] 
				+ "_" + month_present[month - 1];

		// build request
    	BillRequest.Builder request_builder = BillRequest.newBuilder();
		request_builder.setGay(gay);
		
		// for upload self's records
		int[] ri = pp_index_manager.get_record_size_and_push_index(record_size_file, index_push_file);
		int record_size = ri[0];
		int push_index_lasttime = ri[1];		
    	int push_index_thistime = push_index_lasttime;        	
        if (record_size != push_index_lasttime) {
        	push_index_thistime = ((push_index_lasttime + MAX_RECORD_EACH_TIME) < record_size) ?
        				(push_index_lasttime + MAX_RECORD_EACH_TIME) : record_size;       		
        	bill_handler.fill_request_with_push_bills(request_builder, record_file, table_name, 
        			push_index_lasttime, push_index_thistime, year, month);        		
    	}
		Log.i(TAG, "record_size:" + record_size + ", push_index_lasttime:" + push_index_lasttime 
				+ ", push_index_thistime:" + push_index_thistime 
				+ "(" + request_builder.getPushRecordsCount() + ")");
		for (int i = 0; i < request_builder.getPushRecordsCount(); i++) {
			Log.i(TAG, "push records: " + request_builder.getPushRecords(i).getId() 
					+ " " + request_builder.getPushRecords(i).getComments());
		}
    	
		// for sync other's records		
		Map<String, PullIndexInfo> pull_index_info = pp_index_manager.get_pull_index(gay, sharers, index_pull_file);
		bill_handler.fill_request_with_pull_info(request_builder, gay, pull_index_info, MAX_RECORD_EACH_TIME);
    	
    	// rpc call
    	BillRequest request = request_builder.build();	
		BillResponse response = bill_handler.update_records_rpc_call(bchannel, request);
		if (response != null) {
			int nows_pull_size_for_self = 0;
			// check upload status
			if (response.getStatus()) {
		        Log.i(TAG, "upload record OK");
		        nows_pull_size_for_self = response.getLastIndex();
	        } else {
	        	Log.i(TAG, "upload record Failed");
	        }
			
			// check sync records
			for (int i = 0; i < response.getPullRecordsCount(); i++) {				
				PullRecords pull_record = response.getPullRecords(i);
				bill_handler.sync_others_records(pull_record);

				String gay_name = pull_record.getGay();
				pull_index_info.get(gay_name).pull_success(pull_record.getRecordsCount());
				Log.i(TAG, "pull " + gay_name + "'s " + pull_record.getRecordsCount() 
						+ " records OK, next index: " + pull_index_info.get(gay_name).begin_index);
			}
			
			// update index_push/pull_file
			pp_index_manager.update_index_files(gay, sharers, 
					index_push_file, push_index_thistime,
					index_pull_file, pull_index_info, nows_pull_size_for_self);
		} else {
			Log.i(TAG, "rpc call failed");
		}
	}	
	
	private void update_records() {
		update_records(get_current_record_file(), get_current_record_size_file(),
				get_current_index_push_file(), get_current_index_pull_file());
	}

	private void update_property(String record_file, String record_size_file,
			String index_push_file, String index_pull_file) {
		return;
	}
	
	private void update_property() {
		update_property("record_property", "record_property_size",
				"property_index_push", "property_index_pull");
	}
	
		
	private Set<String> get_sharers() {
		String sharer1 = PreferenceManager.getDefaultSharedPreferences(this.mcontext)
				.getString("sharer_name1", "").split(":")[0];
		String sharer2 = PreferenceManager.getDefaultSharedPreferences(this.mcontext)
				.getString("sharer_name2", "").split(":")[0];
		String sharer3 = PreferenceManager.getDefaultSharedPreferences(this.mcontext)
				.getString("sharer_name3", "").split(":")[0];
		Set<String> shares = new HashSet<String>();
		if (sharer1 != "") {
			shares.add(sharer1);
		}
		if (sharer2 != "") {
			shares.add(sharer2);
		}
		if (sharer3 != "") {
			shares.add(sharer3);
		}
		return shares;
	}
	
	
	static private String get_date_present() {
		Calendar c = Calendar.getInstance();
        int year_position = c.get(Calendar.YEAR) - 2015;
        int month_position = c.get(Calendar.MONTH);
        
        return year_present[year_position] + "_" + month_present[month_position];
	}
	
	private String get_current_record_file() {
		String user_s = PreferenceManager.getDefaultSharedPreferences(this.mcontext).getString("user_name", "noset");
		String gay = user_s.split(":")[0];
		return "record_" + gay + "_" + get_date_present();
	}
	
	private String get_current_record_size_file() {
		String user_s = PreferenceManager.getDefaultSharedPreferences(this.mcontext).getString("user_name", "noset");
		String gay = user_s.split(":")[0];
		return "record_size_" + gay + "_" + get_date_present();
	}
	
	private String get_current_index_push_file() {
		String user_s = PreferenceManager.getDefaultSharedPreferences(this.mcontext).getString("user_name", "noset");
		String gay = user_s.split(":")[0];
		return "index_push_" + gay + "_" + get_date_present();
	}
	
	private String get_current_index_pull_file() {
		String user_s = PreferenceManager.getDefaultSharedPreferences(this.mcontext).getString("user_name", "noset");
		String gay = user_s.split(":")[0];
		return "index_pull_" + gay;
	}

}
