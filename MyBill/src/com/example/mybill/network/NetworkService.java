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
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.example.mybill.util.DBHelper;
import com.example.mybill.util.FileHelper;
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
	
	@Override
    public void onCreate() {
        super.onCreate();
        file_helper = new FileHelper(this);
        db_helper = new DBHelper(this);
        db = db_helper.getReadableDatabase();
        
    	String ip = PreferenceManager.getDefaultSharedPreferences(this).getString("remote_address", "127.0.0.1");
    	String port = PreferenceManager.getDefaultSharedPreferences(this).getString("remote_port", "8844");
        
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
			Log.i(TAG, "timer task running");
			
			/* first running, init the record.txt and index.txt */
			//file_helper.deleteFile(get_current_record_file());
			//file_helper.deleteFile(get_current_index_file());
			//file_helper.deleteFile(get_current_index_update_file());
			
			//Log.i(TAG, file_helper.read(get_current_record_file()));
			//Log.i(TAG, "current_index: " + file_helper.read(get_current_index_file()));
			//Log.i(TAG, "updated_index: " + file_helper.read(get_current_index_update_file()));
			//Log.i(TAG, file_helper.read_ex("/data/data/com.example.hello/files/2016_03_bills_records_content.txt"));
			
			/*file_helper.deleteFile("2016_03_records.txt");
			file_helper.deleteFile("2016_03_index.txt");
			file_helper.deleteFile("2016_03_index_update.txt");
			db_helper.drop_bills(db, "bills_2016_03");
			file_helper.deleteFile("2016_02_records.txt");
			file_helper.deleteFile("2016_02_index.txt");
			file_helper.deleteFile("2016_02_index_update.txt");
			db_helper.drop_bills(db, "bills_2016_02");
			file_helper.deleteFile("2016_04_records.txt");
			file_helper.deleteFile("2016_04_index.txt");
			file_helper.deleteFile("2016_04_index_update.txt");
			db_helper.drop_bills(db, "bills_2016_04");
			copy_bills_from_hello("2016_04_bills_records_content.txt");
			copy_bills_from_hello("2016_04_earns_records_content.txt");
			copy_bills_from_hello("2016_03_bills_records_content.txt");
			copy_bills_from_hello("2016_03_earns_records_content.txt");
			copy_bills_from_hello("2016_02_bills_records_content.txt");
			copy_bills_from_hello("2016_02_earns_records_content.txt");*/
			
			/* update records to server */
			//network_test();
			//file_helper.deleteFile("2016_02_records.txt");
			//file_helper.deleteFile("2016_03_records.txt");
			//file_helper.deleteFile("2016_02_index_update.txt");
			//file_helper.deleteFile("2016_03_index_update.txt");
			//file_helper.deleteFile(get_current_index_update_file());
			
			//db_helper.drop_bills(db, "bills_of_others_2016_02");
			//db_helper.drop_bills(db, "bills_of_others_2016_03");
			//db_helper.drop_bills(db, "bills_of_others_2016_04");
			//file_helper.deleteFile("sync_index.txt");
			
			//update_records("2016_02_records.txt", "2016_02_index.txt", "2016_02_index_update.txt");
			//update_records("2016_03_records.txt", "2016_03_index.txt", "2016_03_index_update.txt");
			update_records();
		}			
	}
	
	private void update_records(String record_file, String index_file, String index_update_file) {
		// init the records file and index file from DB if not exist
		init_record_and_index_file(record_file, index_file);

		String[] arr = record_file.split("_");
		int year = Integer.parseInt(arr[0]);
		int month = Integer.parseInt(arr[1]);
		String table_name = "bills_" + arr[0] + "_" + arr[1];
    	String gay = PreferenceManager.getDefaultSharedPreferences(this).getString("user_name", "noset");

    	BillRequest.Builder request_builder = BillRequest.newBuilder();
		request_builder.setGay(gay);
		
		// for sync other's records
		int sync_index = 1;
		String sync_index_line = file_helper.read("sync_index.txt");
		if (sync_index_line != null) {
			sync_index = Integer.parseInt(sync_index_line);
		} else {
			// begin from 1
			sync_index = 1;
		}
		request_builder.setBeginIndex(sync_index);
		request_builder.setMaxLine(2);
		
		// for upload self's records
		int record_size = 0;
    	int updated_index = 0;
    	int last_index = 0;
    	String index_line = file_helper.read(index_file);
    	if (index_line != null) {
    		record_size = Integer.parseInt(index_line.split("\t")[0]);
        	String index_update_line = file_helper.read(index_update_file);
        	if (index_update_line != null) {
        		updated_index = Integer.parseInt(index_update_line);
        	} else {
        		updated_index = 0;
        	}
        	last_index = updated_index;
        	
        	if (record_size != updated_index) {
        		last_index = ((updated_index + MAX_RECORD_EACH_TIME) < record_size) ?
        				(updated_index + MAX_RECORD_EACH_TIME) : record_size;       		
        		fill_bill_request(request_builder, record_file, table_name, updated_index, last_index, year, month);        		
        	}        	
    	}
    	
    	// rpc call
    	BillRequest request = request_builder.build();
		Log.i(TAG, "has begin index: " + request.hasBeginIndex() + ", index: " + request.getBeginIndex());
		Log.i(TAG, "head_index:" + record_size + ", updated_index:" + updated_index 
					+ ", last_index:" + last_index + "(" + request.getRecordsCount() + ")");
		for (int i = 0; i < request.getRecordsCount(); i++) {
			Log.i(TAG, "request ids:" + request.getRecords(i).getId() + " " + request.getRecords(i).getComments());
		}
		BillResponse response = update_records_rpc_call(request);
		if (response != null) {
			// check upload status
			if (response.getStatus()) {
		        Log.i(TAG, "upload record OK");
		        file_helper.save(index_update_file, "" + last_index);	        
	        } else {
	        	Log.i(TAG, "upload record Failed");
	        }
			
			// check sync records
			if (response.getRecordsCount() > 0) {
				sync_others_records(response);
				sync_index += response.getRecordsCount();
				file_helper.save("sync_index.txt", sync_index + "");
				Log.i(TAG, "sync " + response.getRecordsCount() + " records OK, next index: " + sync_index);
			} else {
				Log.i(TAG, "no records to sync, next index: " + sync_index);
			}
			
		} else {
			Log.i(TAG, "rpc call failed");
		}
	}	

	private void update_records() {
		update_records(get_current_record_file(), get_current_index_file(),
				get_current_index_update_file());
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
	
	private void sync_others_records(BillResponse response) {
		int size = response.getRecordsCount();
		for (int i = 0; i < size; i++) {
			Record record = response.getRecords(i);
			int year = record.getYear();
			int month = record.getMonth();	        
	        String db_table_name = "bills_of_others_" + year_present[year - 2015] + "_" + month_present[month - 1];
	        
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
	        		db_helper.insert_new_bill(db, db_table_name, sid, day, pay_earn, consumer, comment, cost, is_deleted);
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
	
	private void network_test() {
		Controller cntl = new Controller();
        EchoRequest request = EchoRequest.newBuilder().setMessage("mybill").build();          
        EchoService.BlockingInterface bstub = EchoService.newBlockingStub((BlockingRpcChannel)bchannel);
        EchoResponse response = null;
        try {
			response = bstub.echo((RpcController)cntl, request);
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
        if (response != null) {
	        Log.i(TAG, response.getMessage());
        }
	}
	
	private void init_record_and_index_file(String record_file, String index_file) {
		if (file_helper.fileIsExists(record_file)) {
			Log.i(TAG, "records file " + record_file + " already exist");
		} else {
			// file content: INDEX	ACTION	SID
			List<Map<String, Object>> all_bills = new ArrayList<Map<String, Object>>();
			String[] arr = record_file.split("_");
			int year = Integer.parseInt(arr[0]);
			int month = Integer.parseInt(arr[1]);
			String db_table_name = "bills_" + arr[0] + "_" + arr[1];
			if (db_helper.dump_bills(db, db_table_name, all_bills)) {
				String records = "";
				int record_size = all_bills.size();
				for (int i = 0; i < record_size; ++i) {
					String sid = all_bills.get(i).get("sid").toString();
					String record = String.valueOf(i + 1) + "\t" + "0\t"/*NEW*/ + sid + "\n";
					records += record;
				}
				file_helper.save(record_file, records);				
				String start_index = record_size + "\t0";
				Log.i(TAG, "records: " + start_index);
				file_helper.save(index_file, start_index);
			}
		}
	}	
	
	private void copy_bills_from_hello(String file) {
		String filename = "/data/data/com.example.hello/files/" + file;
		String[] arr = file.split("_");
		String table_name = "bills_" + arr[0] + "_" + arr[1];
		String pe = arr[2];
		int pay_earn = ((pe.equalsIgnoreCase("bills")) ? 0 : 1);
		Log.i(TAG, "pay_earn: " + pay_earn + ", arr[2]:" + pe);
		File f = new File(filename);
		try {
			InputStream instream = new FileInputStream(f);
			InputStreamReader inputreader = new InputStreamReader(instream);
            BufferedReader buffreader = new BufferedReader(inputreader);
            String line;
            while (( line = buffreader.readLine()) != null) {
            	String[] brr = line.split("\t");
            	int day = Integer.parseInt(brr[0]);
            	int cost = Integer.parseInt(brr[3]);
            	db_helper.insert_new_bill(db, table_name, day, pay_earn, brr[1], brr[2], cost);
            }
            instream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	static private String get_data_present() {
		Calendar c = Calendar.getInstance();
        int year_position = c.get(Calendar.YEAR) - 2015;
        int month_position = c.get(Calendar.MONTH);
        
        return year_present[year_position] + "_" + month_present[month_position];
	}
	
	static public String get_current_record_file() {
		return get_data_present() + "_records.txt";
	}
	
	static public String get_current_index_file() {
		return get_data_present() + "_index.txt";
	}
	
	static public String get_current_index_update_file() {
		return get_data_present() + "_index_update.txt";
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
