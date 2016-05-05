package com.example.mybill.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.example.mybill.network.BillBean.Record;
import com.example.mybill.network.BillBean.BillRequest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper{
	
	final private static String db_name = "NewBill.DB";

	private static final int VERSION = 1;
	private static final String BWORD="BILL";
	final private String[] day_present = new String[]{"01", "02", "03", "04",
			"05","06","07","08","09","10","11","12","13","14","15","16","17","18","19",
			"20","21","22","23","24","25","26","27","28","29","30","31"};
	
	private final String BILL_TABLE_SCHEMA = "(" +
			"id INTEGER PRIMARY KEY AUTOINCREMENT," +
			"sid VARCHAR(100) NOT NULL," +
			"day INT NOT NULL," +
			"pay_earn INT default 0," +
			"consumer VARCHAR(50) NOT NULL," +
			"consume_address VARCHAR(100)," +
			"comment VARCHAR(200) NOT NULL," +
			"cost INT NOT NULL," +
			"submitter VARCHAR(50)," +
			"submit_time INT," +
			"submit_address VARCHAR(100)," +
			"last_modify_time INT," +
			"last_modify_address VARCHAR(100)," +
			"is_deleted INT NOT NULL DEFAULT 0" +
	");";
	
	private Context mcontext;
	private FileHelper file_helper;
			
	// this is needed
	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		this.mcontext = context;
		file_helper = new FileHelper(context);
	}
	public DBHelper(Context context, String name, int version){
		super(context, name, null, version);
		this.mcontext = context;
		file_helper = new FileHelper(context);
	}
	public DBHelper(Context context, String name){
		super(context, name, null, VERSION);
		this.mcontext = context;
		file_helper = new FileHelper(context);
	}
	public DBHelper(Context context) {
		super(context, db_name, null, VERSION);
		this.mcontext = context;
		file_helper = new FileHelper(context);
	}

	//创建数据库
	public void onCreate(SQLiteDatabase db) {	
		// nothing
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//创建成功，日志输出提示
		Log.i(BWORD,"update a Database");
	}
	
	public boolean create_bill_table(SQLiteDatabase db, String table_name) {
		try {
			String create_sql = "create table if not exists " + table_name + BILL_TABLE_SCHEMA;
			db.execSQL(create_sql);
		} catch(Exception e) {
			Log.i(BWORD,"create a bill_table [" + table_name + "] failed");
			return false;
		}		
		Log.i(BWORD,"create a bill_table [" + table_name + "] done");
		return true;
	}
		
	public boolean query_bills(SQLiteDatabase db, String table_name, int type, List<Map<String, Object>> billlist_data) {
		int all_cost = 0;
		boolean has_bills = true;
		
		Cursor c = null;		
		try {
			c = db.rawQuery("SELECT * FROM " + table_name + " where pay_earn=" + type + " ORDER BY DAY", null);
			while (c.moveToNext()) {
				int is_delete = c.getInt(c.getColumnIndex("is_deleted"));
				if (is_delete == 1) {
					continue;
				}
	    		int day = c.getInt(c.getColumnIndex("day"));
	    		String consumer = c.getString(c.getColumnIndex("consumer"));
	    		String comment = c.getString(c.getColumnIndex("comment"));
	    		int cost = c.getInt(c.getColumnIndex("cost"));
	    		int id = c.getInt(c.getColumnIndex("id"));
	    		all_cost += cost;
		    	Map<String, Object> map = new HashMap<String, Object>();
		        map.put("day", day_present[day - 1]);
		        map.put("consumer", consumer);
		        map.put("comment", comment);
		        map.put("cost", String.valueOf(cost));
		        map.put("id", id);
		        billlist_data.add(map);
	    	}
		} catch(Exception e) {
			Log.i("QUERY", "NO DATA");
			has_bills = false;
		}

		Map<String, Object> map = new HashMap<String, Object>();
		if (has_bills) {			
	        map.put("day", "-");
	        map.put("consumer", "-");
	        map.put("comment", "总计");
	        map.put("cost", all_cost);
	        billlist_data.add(map);
	        c.close();
		}
		return has_bills;
	}
	
	public boolean dump_bills(SQLiteDatabase db, String table_name, List<Map<String, Object>> all_bills) {		
		Cursor c = null;		
		try {
			c = db.rawQuery("SELECT sid FROM " + table_name + " where is_deleted=0 ORDER BY DAY", null);
			while (c.moveToNext()) {
				String sid = c.getString(c.getColumnIndex("sid"));Map<String, Object> map = new HashMap<String, Object>();
		        map.put("sid", sid);
		        all_bills.add(map);
			}
			c.close();
		} catch(Exception e) {
			Log.i("QUERY", "NO DATA");
			return false;
		}
		return true;
	}
				
	
	public boolean insert_new_bill(SQLiteDatabase db, String table_name, String sid, int day, int pay_earn,
			String consumer, String comment, int cost, int is_deleted) {
        ContentValues values = new ContentValues();
        values.put("day", day);
        values.put("pay_earn", pay_earn);
        values.put("consumer", consumer);
        values.put("comment", comment);
        values.put("cost", cost);
        values.put("is_deleted", is_deleted);
        values.put("sid", (sid == null) ? "org" : sid);
        
        if (!create_bill_table(db, table_name)) {
        	return false;
        }
        try {
            db.insert(table_name, null, values);
            // sid != null, 表明是sync others' records
            if (sid == null) {
	            Cursor cursor = db.rawQuery("select last_insert_rowid() from " + table_name, null);       
	            int id; 
	            String strid;
	            if(cursor.moveToFirst()) {
	            	id = cursor.getInt(0);
	            	String user_name = PreferenceManager.getDefaultSharedPreferences(mcontext).getString("user_name", "noset");
	            	strid = user_name + "_" + table_name + "_" + id;
	            	Map<String,Object> update_strid = new HashMap<String,Object>();
	            	update_strid.put("sid", strid);
	            	if (!update_bill(db, table_name, id, null, update_strid)) {
	            		Log.i(BWORD, "set sid " + strid + " for " + id + "failed");
	            	} else {
	            		Log.i(BWORD, "set sid " + strid + " for " + id + "success");
	            	}
	            	
	            	// update record/index file
	            	String data_prefix = get_data_prefix(table_name);
	            	add_record(strid, data_prefix, true);
	            }
            }
        } catch(Exception e) {
        	Log.i(BWORD, "insert bill failed");
        	System.out.print(e);
        	return false;
        }
      
        return true;
	}
	
	public boolean insert_new_bill(SQLiteDatabase db, String table_name, int day, int pay_earn,
			String consumer, String comment, int cost) {
		return insert_new_bill(db, table_name, null, day, pay_earn, consumer, comment, cost, 0);
	}
	
	public Map<String, Object> get_bill_data(SQLiteDatabase db, String table_name, int id) {
		Map<String,Object> map = new HashMap<String, Object>();
		
		String sql = "SELECT * FROM " + table_name + " where id=?";
		Cursor c = db.rawQuery(sql, new String[]{String.valueOf(id)});
		if(c.moveToFirst()) {
		    map.put("consumer", c.getString(c.getColumnIndex("consumer")));
		    map.put("comment", c.getString(c.getColumnIndex("comment")));
		    map.put("cost", c.getInt(c.getColumnIndex("cost")));
		    map.put("sid", c.getString(c.getColumnIndex("sid")));
		}
		c.close();
		return map;
	}
	
	public boolean update_bill(SQLiteDatabase db, String table_name, int id,
			String sid/*just for update record_file*/, Map<String, Object> update_data) {
		ContentValues cv = new ContentValues();
		if (update_data.containsKey("delete")) {
			cv.put("is_deleted", 1);
		} else {
			Set set = update_data.keySet();			  
			for(Iterator iter = set.iterator(); iter.hasNext();)
			{
				String key = (String)iter.next();
				if (key == "day" || key == "cost") {
					cv.put(key, Integer.valueOf(update_data.get(key).toString()));
				} else {
					cv.put(key, update_data.get(key).toString());
				}
				Log.i(BWORD, "update [" + key + "]: " + update_data.get(key).toString());
			}
		}
		
		// self update
		if (id >= 0) {
			String whereClause = "id=?";
			String[] whereArgs = {String.valueOf(id)};
			try {
				db.update(table_name, cv, whereClause, whereArgs);
			} catch(Exception e) {
	        	Log.i(BWORD, "update self bill failed");
	        	return false;
	        }			
			// update record/index file
			if (sid != null) {
				String data_prefix = get_data_prefix(table_name);
				add_record(sid, data_prefix, false);
			}
		}
		// other update
		else {
			String whereClause = "sid=?";
			String[] whereArgs = {sid};
			try {
				db.update(table_name, cv, whereClause, whereArgs);
			} catch(Exception e) {
	        	Log.i(BWORD, "update other bill failed");
	        	return false;
	        }
		}
    	   	
		return true;
	}
	
	public boolean drop_bills(SQLiteDatabase db, String table_name) {
		String sql = "delete from " + table_name;
		db.execSQL(sql);
		return true;
	}
	
	private String get_data_prefix(String table_name) {
		String[] arr = table_name.split("_");
		return arr[1] + "_" + arr[2];
	}
	
	private void add_record(String sid, String data_prefix, boolean new_update) {
		String index_file = data_prefix + "_index.txt";
    	String index_line = file_helper.read(index_file);
    	int record_size;
    	// 这里的current_index弃用，放在xxxx_xx_index_update.txt中，以使本地操作和网络更新 不会使用
    	// 相同的文件，避免使用mutex
    	String current_index;
    	if (index_line != null) {
    		String[] indexarr = index_line.split("\t");    	
    		record_size = Integer.parseInt(indexarr[0]);
    		current_index = indexarr[1];
    	} else {
    		record_size = 0;
    		current_index = "0";
    	}
    	
    	record_size++;
    	String new_index_line = record_size + "\t" + current_index;
    	file_helper.save(index_file, new_index_line);
    	
    	String record_file = data_prefix + "_records.txt";
    	String new_record_line = record_size + (new_update ? "\t0\t" : "\t1\t") + sid + "\n";
    	file_helper.append(record_file, new_record_line);
	}
		
	public void get_record_content(SQLiteDatabase db, String table_name,
			int year, int month, Map<String, Integer> update_records, BillRequest.Builder request_builder) {
		try {
			int id_size = update_records.size();
			String ids = "(";
			int i = 0;
			Iterator<Entry<String, Integer>> it = update_records.entrySet().iterator();
			while(it.hasNext()) {
				i++;
				String sid = it.next().getKey().toString();
				ids += "'";
				ids += sid;
				ids += "'";
				ids += ((i != id_size) ? "," : ")");
			}
			Log.i(BWORD, ids);
			
			String sql = "SELECT * FROM " + table_name + " where sid in " + ids;
			Cursor c = db.rawQuery(sql, null);
			while (c.moveToNext()) {
				int is_delete = c.getInt(c.getColumnIndex("is_deleted"));
	    		int day = c.getInt(c.getColumnIndex("day"));
	    		String consumer = c.getString(c.getColumnIndex("consumer"));
	    		String comment = c.getString(c.getColumnIndex("comment"));
	    		int cost = c.getInt(c.getColumnIndex("cost"));
	    		int pay_earn = c.getInt(c.getColumnIndex("pay_earn"));
	    		String sid = c.getString(c.getColumnIndex("sid"));	    		
	    		Record.Type type = (Integer.parseInt(update_records.get(sid).toString()) == 0) ?
	    				Record.Type.NEW : Record.Type.UPDATE;
	    		Log.i("QUERY", sid + " " + type.name() + " " + day + " " + consumer + " " + comment + " " + cost);
	    		request_builder.addRecords(Record.newBuilder().setType(type).setId(sid)
        				.setYear(year).setMonth(month).setDay(day).setPayEarn(pay_earn)
        				.setGay(consumer).setComments(comment).setCost(cost)
        				.setIsDeleted(is_delete).build());
	    	}
		} catch(Exception e) {
			e.printStackTrace();
			Log.i("QUERY", table_name + " NO DATA");
		}
			
	}
	public boolean is_bill_exist(SQLiteDatabase db, String db_table_name,
			String sid) {
		if (!create_bill_table(db, db_table_name)) {
			// 建表失败，认为是不存在
        	return false;
        }
		boolean ret = false;
		String sql = "SELECT * FROM " + db_table_name + " where sid=?";
		Cursor c = db.rawQuery(sql, new String[]{sid});
		if(c.moveToFirst()) {
		    ret = true;
		}
		c.close();
		return ret;
	}


}
