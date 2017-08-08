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
	final static private String[] year_present = new String[]{"2015", "2016", "2017", "2018"};
	final static private String[] month_present = new String[]{"01", "02", "03", "04",
			"05","06","07","08","09","10","11","12"};
	final static private String[] day_present = new String[]{"01", "02", "03", "04",
			"05","06","07","08","09","10","11","12","13","14","15","16","17","18","19",
			"20","21","22","23","24","25","26","27","28","29","30","31"};
	
	private final int MAX_RECORD_EACH_TIME = 2;
    
	private FileHelper file_helper;
	private DBHelper db_helper;
	private SQLiteDatabase db;
	
	private BlockChannel bchannel = null;
	private Context mcontext;
	
	@Override
    public void onCreate() {
        super.onCreate();
        mcontext = this;
        file_helper = new FileHelper(this);
        db_helper = new DBHelper(this);
        db = db_helper.getReadableDatabase();
        
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
		// record_file: record_gay_year_month
		// record_size_file: record_gay_size_year_month
		// index_push_file: index_gay_push_year_month
		// index_pull_file: index_gay_pull, format:
		//		gay \t pull_index \t breakpoint_index
		//		sharer1 \t pull_index
		//		sharer2 \t pull_index
		//		sharer3 \t pull_index
		
		String[] arr = record_file.split("_");
		String gay = arr[1];
		int year = Integer.parseInt(arr[2]);
		int month = Integer.parseInt(arr[3]);
		String table_name = "bills_of_" + gay + "_" + year_present[year - 2015] 
				+ "_" + month_present[month - 1];

		// build request
    	BillRequest.Builder request_builder = BillRequest.newBuilder();
		request_builder.setGay(gay);
		
		// for upload self's records
		int[] ri = get_record_size_and_push_index(record_size_file, index_push_file);
		int record_size = ri[0];
		int index_push = ri[1];		
    	int next_index_push = index_push;        	
        if (record_size != index_push) {
        	next_index_push = ((index_push + MAX_RECORD_EACH_TIME) < record_size) ?
        				(index_push + MAX_RECORD_EACH_TIME) : record_size;       		
        	fill_bill_request(request_builder, record_file, table_name, 
        			index_push, next_index_push, year, month);        		
    	}
		Log.i(TAG, "record_size:" + record_size + ", index_push:" + index_push 
				+ ", next_index_push:" + next_index_push 
				+ "(" + request_builder.getPushRecordsCount() + ")");
		for (int i = 0; i < request_builder.getPushRecordsCount(); i++) {
			Log.i(TAG, "push records: " + request_builder.getPushRecords(i).getId() 
					+ " " + request_builder.getPushRecords(i).getComments());
		}
    	
		// for sync other's records		
		Map<String, PullIndexInfo> pull_index_info = get_pull_index(index_pull_file);
		Iterator<Entry<String, PullIndexInfo>> iter = pull_index_info.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String gay_name = (String) entry.getKey();
			PullIndexInfo val = (PullIndexInfo) entry.getValue();
    		int begin_index = val.begin_index;
    		int breakpoint_index = val.breakpoint_index;
    		if ((gay_name.equals(gay)) && (breakpoint_index <= 0)) {
    			// if self's breakpoint <= 0, don't pull self's records.
    			// wait for last_push_index returned by the first push
    			Log.i(TAG, gay + " self maybe the first time to push, wait for the last_push_index");
    		} else {
	    		Log.i(TAG, "pull " + gay_name + "'s record from begin_index " + begin_index
	    				+ ", breakpoint_index " + breakpoint_index);
	    		request_builder.addPullInfos(PullInfo.newBuilder()
	    				.setGay(gay_name).setBeginIndex(begin_index)
	    				.setMaxLine(MAX_RECORD_EACH_TIME).build());
    		}
		}
    	
    	// rpc call
    	BillRequest request = request_builder.build();	
		BillResponse response = update_records_rpc_call(request);
		if (response != null) {
			int push_last_index = 0;
			// check upload status
			if (response.getStatus()) {
		        Log.i(TAG, "upload record OK");
		        push_last_index = response.getLastIndex();
	        } else {
	        	Log.i(TAG, "upload record Failed");
	        }
			
			// check sync records
			for (int i = 0; i < response.getPullRecordsCount(); i++) {				
				PullRecords pull_record = response.getPullRecords(i);
				sync_others_records(pull_record);

				String gay_name = pull_record.getGay();
				pull_index_info.get(gay_name).pull_success(pull_record.getRecordsCount());
				Log.i(TAG, "pull " + gay_name + "'s " + pull_record.getRecordsCount() 
						+ " records OK, next index: " + pull_index_info.get(gay_name).begin_index);
			}
			
			// update index_push/pull_file
			update_index_files(index_push_file, next_index_push,
					index_pull_file, pull_index_info, push_last_index);
		} else {
			Log.i(TAG, "rpc call failed");
		}
	}	
	
	private void update_records() {
		update_records(get_current_record_file(), get_current_record_size_file(),
				get_current_index_push_file(), get_current_index_pull_file());
	}

	private BillResponse update_records_rpc_call(BillRequest request) {
		Controller cntl = new Controller();
		BillService.BlockingInterface bstub = BillService.newBlockingStub((BlockingRpcChannel)bchannel);
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
	
	private void sync_others_records(PullRecords pull_record) {
		String gay = pull_record.getGay();
		int size = pull_record.getRecordsCount();
		for (int i = 0; i < size; i++) {
			Record record = pull_record.getRecords(i);
			int year = record.getYear();
			int month = record.getMonth();	        
	        String db_table_name = "bills_of_" + gay + "_"
	        		+ year_present[year - 2015] + "_" + month_present[month - 1];
	        
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

	private void fill_bill_request(BillRequest.Builder request_builder, String record_file,
			String table_name, int updated_index, int last_index, int year, int month) {
		Map<String, Integer> update_records = new HashMap<String, Integer>();
		try {
			FileInputStream recordStream = openFileInput(record_file);
			InputStreamReader inputreader = new InputStreamReader(recordStream);
            BufferedReader buffreader = new BufferedReader(inputreader);
    		String line = null;
    		int i = 0;
            while (( line = buffreader.readLine()) != null) {
            	i++;
            	if (i <= updated_index) {
            		continue;
            	} else if (i > last_index) {
            		break;
            	}
            	Log.i(TAG, "push_index " + updated_index 
            			+ ", last_index " + last_index + ", line " + line);
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
		
	private class PullIndexInfo {
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

	private Map<String, PullIndexInfo> get_pull_index(String index_pull_file) {
		String gay = index_pull_file.split("_")[2];
		Set<String> shares = get_sharers();
				
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
					if (pull_index < breakpoint_index) {
						pull_info.put(gay, new PullIndexInfo(pull_index, breakpoint_index));
					}
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

	private int[] get_record_size_and_push_index(String record_size_file,
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
	
	private void update_index_files(String index_push_file,
			int next_index_push, String index_pull_file,
			Map<String, PullIndexInfo> pull_index_info, int push_last_index) {
		String gay = index_push_file.split("_")[2];
		// push_index
        file_helper.save(index_push_file, "" + next_index_push);
        
        // pull_index
        String pull_index_str = "";
        // self's
        if (pull_index_info.containsKey(gay)) {
        	PullIndexInfo val = pull_index_info.get(gay);
        	int begin_index = val.begin_index;
			int breakpoint_index = val.breakpoint_index;
			Log.i(TAG, gay + "'s last push index: " + push_last_index
					+ ", breakpoint_index: " + breakpoint_index);
			if (breakpoint_index == 0) {
				breakpoint_index = push_last_index;
				Log.i(TAG, "set self's breakpoint_index: " + breakpoint_index);
			}
			pull_index_str = pull_index_str + gay + "\t" 
					+ begin_index + "\t" + breakpoint_index + "\n";
        }
        // sharers'
        Set<String> sharers = get_sharers();
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

	static private String get_data_present() {
		Calendar c = Calendar.getInstance();
        int year_position = c.get(Calendar.YEAR) - 2015;
        int month_position = c.get(Calendar.MONTH);
        
        return year_present[year_position] + "_" + month_present[month_position];
	}
	
	private String get_current_record_file() {
		String user_s = PreferenceManager.getDefaultSharedPreferences(this.mcontext).getString("user_name", "noset");
		String gay = user_s.split(":")[0];
		return "record_" + gay + "_" + get_data_present();
	}
	
	private String get_current_record_size_file() {
		String user_s = PreferenceManager.getDefaultSharedPreferences(this.mcontext).getString("user_name", "noset");
		String gay = user_s.split(":")[0];
		return "record_size_" + gay + "_" + get_data_present();
	}
	
	private String get_current_index_push_file() {
		String user_s = PreferenceManager.getDefaultSharedPreferences(this.mcontext).getString("user_name", "noset");
		String gay = user_s.split(":")[0];
		return "index_push_" + gay + "_" + get_data_present();
	}
	
	private String get_current_index_pull_file() {
		String user_s = PreferenceManager.getDefaultSharedPreferences(this.mcontext).getString("user_name", "noset");
		String gay = user_s.split(":")[0];
		return "index_pull_" + gay;
	}
	
	public class UpdateRecord {
		public int action;
		public String sid;
		public UpdateRecord(int action, String sid) {
			this.action = action;
			this.sid = sid;
		}
	}
}
