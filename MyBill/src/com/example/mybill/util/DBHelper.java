package com.example.mybill.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.example.mybill.network.BillBean.PropertyRecord.AssetsRecord;
import com.example.mybill.network.BillBean.PropertyRecord.PocketRecord;
import com.example.mybill.network.BillBean.PropertyRecord.PropertyType;
import com.example.mybill.network.BillBean.PropertyRequest;
import com.example.mybill.network.PPIndexManager;
import com.example.mybill.network.BillBean.Record;
import com.example.mybill.network.BillBean.BillRequest;
import com.example.mybill.network.BillBean.PropertyRecord;
import com.example.mybill.network.BillBean.PropertyRequest;


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
	
	private final String POCKET_TABLE_SCHEMA = "(" +
			"id INTEGER PRIMARY KEY AUTOINCREMENT," +
			"sid VARCHAR(100) NOT NULL," +
			"year INT NOT NULL," +
			"month INT NOT NULL," +
			"money INT NOT NULL," +
			"is_deleted INT NOT NULL DEFAULT 0" +
	");";

	private final String ASSETS_TABLE_SCHEMA = "(" +
			"id INTEGER PRIMARY KEY AUTOINCREMENT," +
			"sid VARCHAR(100) NOT NULL," +
			"year INT NOT NULL," +
			"month INT NOT NULL," +
			"day INT NOT NULL," +
			"store_addr INT NOT NULL," +
			"flow_type INT NOT NULL," +
			"money INT NOT NULL," +
			"store_addr_op INT NOT NULL," +
			"is_deleted INT NOT NULL DEFAULT 0" +
	");";	
    
    private final static String table_name_pocket = "pocket_money";
    private final static String table_name_assets = "assets";
	
	private Context mcontext;
	private FileHelper file_helper;
	private PPIndexManager pp_index_manager;
			
	// this is needed
	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		this.mcontext = context;
		file_helper = new FileHelper(context);
        pp_index_manager = new PPIndexManager(file_helper);
	}
	public DBHelper(Context context, String name, int version){
		super(context, name, null, version);
		this.mcontext = context;
		file_helper = new FileHelper(context);
        pp_index_manager = new PPIndexManager(file_helper);
	}
	public DBHelper(Context context, String name){
		super(context, name, null, VERSION);
		this.mcontext = context;
		file_helper = new FileHelper(context);
        pp_index_manager = new PPIndexManager(file_helper);
	}
	public DBHelper(Context context) {
		super(context, db_name, null, VERSION);
		this.mcontext = context;
		file_helper = new FileHelper(context);
        pp_index_manager = new PPIndexManager(file_helper);
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

	
	/*---------------------------------------------------------*/
	/* for public */
	/*---------------------------------------------------------*/
	
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
	            	String data_prefix = get_bill_prefix(table_name);
	            	strid = data_prefix + "_" + id;
	            	Map<String,Object> update_strid = new HashMap<String,Object>();
	            	update_strid.put("sid", strid);
	            	if (!update_bill(db, table_name, id, null, update_strid)) {
	            		Log.i(BWORD, "set sid " + strid + " for " + id + "failed");
	            	} else {
	            		Log.i(BWORD, "set sid " + strid + " for " + id + "success");
	            	}
	            	
	            	// update record/index file
	            	update_local_index_of_bill(strid, data_prefix, true);
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
				String data_prefix = get_bill_prefix(table_name);
				update_local_index_of_bill(sid, data_prefix, false);
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
		
	public void fill_billrequest_push_content(SQLiteDatabase db, String table_name,
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
			
			String sql = "SELECT * FROM " + table_name + " where sid in " + ids;
			Log.i(BWORD, sql);
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
	    		request_builder.addPushRecords(Record.newBuilder().setType(type).setId(sid)
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

	private String get_bill_prefix(String table_name/*bills_of_gay_year_month*/) {
		// bill_prefix: gay_year_month
		String[] arr = table_name.split("_");
		return arr[2] + "_" + arr[3] + "_" + arr[4];
	}
	
	private void update_local_index_of_bill(String sid, String data_prefix, boolean new_or_update) {
		// record size
		String record_size_file = "record_size_" + data_prefix;
    	String record_file = "record_" + data_prefix;
    	String record_line = (new_or_update ? "0\t" : "1\t") + sid;
    	pp_index_manager.update_local_index(record_size_file, record_file, record_line);
	}
	
	
	/*---------------------------------------------------------*/
	/* for assets */
	/*---------------------------------------------------------*/
	
	
	public boolean insert_new_property(SQLiteDatabase db, String sid, int year, int month, int day, 
			String item_n, String flowtype_n, int cost, String dest_item_n) {  
        // for pocket monty
        if (item_n == "100") {
        	if (!create_pocket_table(db, table_name_pocket)) {
            	return false;
            }
        	try {
        		ContentValues values = new ContentValues();
                values.put("year", year);
                values.put("month", month);
                values.put("money", cost);
                values.put("is_deleted", 0);
                values.put("sid", (sid == null) ? "org" : sid);
                
                String table_name = table_name_pocket;
                if (db.insert(table_name, null, values) == -1) {
                	Log.i(BWORD, "insert pocket action failed, maybe exist");
                	return false;
                }
                // sid != null, 表明是sync others' records
                if (sid == null) {
    	            Cursor cursor = db.rawQuery("select last_insert_rowid() from " + table_name, null);       
    	            int id; 
    	            String strid;
    	            if(cursor.moveToFirst()) {
    	            	id = cursor.getInt(0);
    	            	strid = "" + id;
    	            	Map<String,Object> update_strid = new HashMap<String,Object>();
    	            	update_strid.put("sid", strid);
    	            	if (!update_bill(db, table_name, id, null, update_strid)) {
    	            		Log.i(BWORD, "set sid " + strid + " for " + id + "failed");
    	            	} else {
    	            		Log.i(BWORD, "set sid " + strid + " for " + id + "success");
    	            	}
    	            	
    	            	// update record/index file
    	            	update_local_index_of_property(strid, true, true);
    	            }
                }
        	} catch(Exception e) {
            	Log.i(BWORD, "insert pocket failed");
            	System.out.print(e);
            	return false;
            }
        } 
        
        // for assets
        else {
        	if (!create_assets_table(db, table_name_assets)) {
            	return false;
            }
        	try {
            	ContentValues values = new ContentValues();
                values.put("year", year);
                values.put("month", month);
                values.put("day", day);
                values.put("store_addr", Integer.valueOf(item_n));
                values.put("flow_type", Integer.valueOf(flowtype_n));
                values.put("money", cost);
                values.put("store_addr_op", Integer.valueOf(dest_item_n));
                values.put("is_deleted", 0);
                values.put("sid", (sid == null) ? "org" : sid);
                
                String table_name = table_name_assets;
                db.insert(table_name, null, values);
                // sid != null, 表明是sync others' records
                if (sid == null) {
    	            Cursor cursor = db.rawQuery("select last_insert_rowid() from " + table_name, null);       
    	            int id;
    	            String strid;
    	            if(cursor.moveToFirst()) {
    	            	id = cursor.getInt(0);
    	            	strid = "" + id;
    	            	Map<String,Object> update_strid = new HashMap<String,Object>();
    	            	update_strid.put("sid", strid);
    	            	if (!update_bill(db, table_name, id, null, update_strid)) {
    	            		Log.i(BWORD, "set sid " + strid + " for " + id + "failed");
    	            	} else {
    	            		Log.i(BWORD, "set sid " + strid + " for " + id + "success");
    	            	}
    	            	
    	            	// update record/index file
    	            	update_local_index_of_property(strid, false, true);
    	            }
                }
            } catch(Exception e) {
            	Log.i(BWORD, "insert bill failed");
            	System.out.print(e);
            	return false;
            }
        }       
    	
        return true;
	}
	
	public boolean query_pockets(SQLiteDatabase db, 
			String year_s, String month_s,
			List<Map<String, Object>> pocketlist_data) {
		boolean has_bills = true;		
		Cursor c = null;		
		try {
			c = db.rawQuery("SELECT * FROM " + table_name_pocket + " ORDER BY year,month", null);
			while (c.moveToNext()) {
				int is_delete = c.getInt(c.getColumnIndex("is_deleted"));
				if (is_delete == 1) {
					continue;
				}
	    		int year = c.getInt(c.getColumnIndex("year"));
	    		int month = c.getInt(c.getColumnIndex("month"));
	    		int money = c.getInt(c.getColumnIndex("money"));
	    		int id = c.getInt(c.getColumnIndex("id"));
	    		
	    		// 所选年月，3个月内的零钱记录
	    		int year_s_i = Integer.valueOf(year_s);
	    		int month_s_i = Integer.valueOf(month_s);
	    		int x = (year_s_i - year) * 12 + month_s_i - month;
	    		if (x < 0 || x >= 3) {
	    			continue;
	    		}
	    		
		    	Map<String, Object> map = new HashMap<String, Object>();
		        map.put("yymm", year + (month > 9 ? "/" : "/0") + month);
		        map.put("money", money);
		        map.put("comment", "ALL");
		        map.put("id", id);		        
		        pocketlist_data.add(map);
	    	}
	        c.close();
		} catch(Exception e) {
			Log.i("QUERY", "NO DATA" + e);
			has_bills = false;
		}
		return has_bills;
	}

	private int calc_delta_one_month(int select_item, int flow_type,
			int store_addr, int store_addr_op, int money) {
		int delta = 0;
		switch(flow_type) {
		case 0:
		case 1:
			delta += money;
			break;
		case 2:
		case 3:
			delta -= money;
			break;
		case 4:
			if (select_item == 100) {
				break;
			} else if (select_item == store_addr) {
				delta -= money;
			} else if (select_item == store_addr_op) {
				delta += money;
			} else {
				break;
			}
			break;	    				
		}
		return delta;
	}
	
	public boolean query_assets(SQLiteDatabase db, String year_s, String month_s, int select_item, 
			List<Map<String, Object>> assetslist_data, Map<String, Integer> assetslist_sum_data) {
		boolean has_bills = true;	
		int year_s_i = Integer.valueOf(year_s);
		int month_s_i = Integer.valueOf(month_s);
		int x_s = year_s_i * 12 + month_s_i;
		int assets_total = 0;
		int assets_increment = 0;
		Cursor c = null;		
		try {
			if (select_item == 100) {
				c = db.rawQuery("SELECT * FROM " + table_name_assets + " ORDER BY year,month,day", null);
			} else {
				c = db.rawQuery("SELECT * FROM " + table_name_assets 
						+ " where store_addr = " + select_item 
						+ " or (flow_type = 4 and store_addr_op = " + select_item + ")"
						+ " ORDER BY year,month,day", null);
			}
			while (c.moveToNext()) {
				int is_delete = c.getInt(c.getColumnIndex("is_deleted"));
				if (is_delete == 1) {
					continue;
				}
	    		int year = c.getInt(c.getColumnIndex("year"));
	    		int month = c.getInt(c.getColumnIndex("month"));
	    		int day = c.getInt(c.getColumnIndex("day"));
	    		int store_addr = c.getInt(c.getColumnIndex("store_addr"));
	    		int flow_type = c.getInt(c.getColumnIndex("flow_type"));
	    		int money = c.getInt(c.getColumnIndex("money"));
	    		int store_addr_op = c.getInt(c.getColumnIndex("store_addr_op"));
	    		int id = c.getInt(c.getColumnIndex("id"));
	    		
	    		// < (year_s，month_s)的所有增量累加为total
	    		// == (year_s，month_s)的所有增量累加为本月increment
	    		int x = year * 12 + month;
	    		if (x > x_s) {
	    			// skip
	    			continue;
	    		} else {
	    			// x <= x_s, total
	    			// among it, x == x_s, increment
	    			int delta = calc_delta_one_month(select_item, flow_type, store_addr, store_addr_op, money); 	    			
	    			assets_total += delta;
	    			
	    			if (x == x_s) {
	    				// increment this month
	    				assets_increment += delta;
	    				// clauses this month
				    	Map<String, Object> map = new HashMap<String, Object>();
				        map.put("yymmdd", year + (month > 9 ? "" : "0") + month 
				        		+ (day > 9 ? "" : "0") + day);
				        map.put("item", AssetsUtil.getInstance().assets_item_map.get("" + store_addr));
				        map.put("flow_type", AssetsUtil.getInstance().assets_action_map.get("" + flow_type));
				        map.put("money", money);
				        if (flow_type == 4) {
				        	map.put("dest_item", AssetsUtil.getInstance().assets_item_map.get("" + store_addr_op));
				        } else {
				        	map.put("dest_item", "- -");
				        }
				        map.put("id", id);		        
				        assetslist_data.add(map);
	    			}
	    		}
	    	}
	        c.close();
		} catch(Exception e) {
			Log.i("QUERY", "NO DATA " + e);
			has_bills = false;
		}
		assetslist_sum_data.put("total", assets_total);
        assetslist_sum_data.put("increment", assets_increment);
		return has_bills;
	}

	public Map<String, Object> get_property_data(SQLiteDatabase db,
			boolean is_pocket, int property_id) {
		Map<String,Object> map = new HashMap<String, Object>();		
		String sql = "SELECT * FROM " + (is_pocket ? table_name_pocket : table_name_assets) + " where id=?";
		Cursor c = db.rawQuery(sql, new String[]{String.valueOf(property_id)});
		if(c.moveToFirst()) {
			map.put("year", c.getInt(c.getColumnIndex("year")));
			map.put("month", c.getInt(c.getColumnIndex("month")));
		    map.put("money", c.getInt(c.getColumnIndex("money")));
		    map.put("sid", c.getString(c.getColumnIndex("sid")));
			if (!is_pocket) {
				map.put("day", c.getInt(c.getColumnIndex("day")));
			    map.put("store_addr", c.getInt(c.getColumnIndex("store_addr")));
			    map.put("flow_type", c.getInt(c.getColumnIndex("flow_type")));
			    map.put("store_addr_op", c.getInt(c.getColumnIndex("store_addr_op")));
			}
		}
		c.close();
		return map;
	}
	
	public boolean update_property(SQLiteDatabase db, boolean is_pocket,
			int property_id, String property_sid, Map<String, Object> update_data) {
		ContentValues cv = new ContentValues();
		if (update_data.containsKey("delete")) {
			cv.put("is_deleted", 1);
		} else {
			Set set = update_data.keySet();			  
			for(Iterator iter = set.iterator(); iter.hasNext();)
			{
				String key = (String)iter.next();
				if (key == "day" || key == "money") {
					cv.put(key, Integer.valueOf(update_data.get(key).toString()));
				} else {
					cv.put(key, update_data.get(key).toString());
				}
				Log.i(BWORD, "update [" + key + "]: " + update_data.get(key).toString());
			}
		}
		
		// self update
		if (property_id >= 0) {
			String whereClause = "id=?";
			String[] whereArgs = {String.valueOf(property_id)};
			try {
				db.update(is_pocket ? table_name_pocket : table_name_assets, cv, whereClause, whereArgs);
			} catch(Exception e) {
	        	Log.i(BWORD, "update self property failed");
	        	return false;
	        }			
			// update record/index file
			if (property_sid != null) {
				update_local_index_of_property(property_sid, is_pocket, false);
			}
		}
		// other update
		else {
			String whereClause = "sid=?";
			String[] whereArgs = {property_sid};
			try {
				db.update(is_pocket ? table_name_pocket : table_name_assets, cv, whereClause, whereArgs);
			} catch(Exception e) {
	        	Log.i(BWORD, "update other property failed");
	        	return false;
	        }
		}
    	   	
		return true;
	}
		
	public boolean query_evaluate(SQLiteDatabase db, int year,
			int month, Map<String, String> bill_table_info,
			List<Map<String, Object>> evaluatelist_data) {
		// month earn and pay
		int all_earn = 0;
		int all_pay = 0;
		Map<String, Integer> earn_list = new HashMap<String, Integer>();
		Map<String, Integer> pay_list = new HashMap<String, Integer>();
		Iterator iter = bill_table_info.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String gay_name = entry.getKey().toString();
			String table_name = entry.getValue().toString();
			Map<String,Integer> earn_pay = query_total_bill(db, table_name);
			
			earn_list.put(gay_name, earn_pay.get("earn"));
			pay_list.put(gay_name, earn_pay.get("pay"));
			all_earn += earn_pay.get("earn");
			all_pay += earn_pay.get("pay");
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("name", gay_name + "收入");
			map.put("value", earn_pay.get("earn") + "");
			map.put("comment", "");
	        evaluatelist_data.add(map);
		}		
		
		// assets delta
		Map<String, Integer> assets_list = query_assets_delta(db, year, month);
		int assets_use = assets_list.get("use");
		int assets_store = assets_list.get("store");
		Map<String, Object> map_assets_use = new HashMap<String, Object>();
		map_assets_use.put("name", "理财支取");
		map_assets_use.put("value", assets_use + "");
		map_assets_use.put("comment", "");
        evaluatelist_data.add(map_assets_use);
        Map<String, Object> map_assets_store = new HashMap<String, Object>();
        map_assets_store.put("name", "理财结入");
        map_assets_store.put("value", assets_store + "");
        map_assets_store.put("comment", "");
        evaluatelist_data.add(map_assets_store);
		int assets_store_real = assets_store - assets_use;
		
		// assets store rate
		Map<String, Object> map_assets_store_rate = new HashMap<String, Object>();
		map_assets_store_rate.put("name", "结入占比");
		map_assets_store_rate.put("value", (all_earn != 0) ? 
				String.format("%.2f", assets_store_real * 100.0 / all_earn) + "%" : "- -");
		map_assets_store_rate.put("comment", "");
        evaluatelist_data.add(map_assets_store_rate);
		
		// pocket use
		int pocket_use = 0;
		Map<String, Object> map_pocket_use = new HashMap<String, Object>();
		Map<String, Integer> pocket_list = query_pocket_delta(db, year, month);
		if (pocket_list.containsKey("this_month") && pocket_list.containsKey("next_month")) {
			pocket_use = pocket_list.get("this_month") - pocket_list.get("next_month");
		} else {
			map_pocket_use.put("comment", "miss");
		}
		map_pocket_use.put("name", "流动资金支出");
		map_pocket_use.put("value", pocket_use);
		evaluatelist_data.add(map_pocket_use);
		
		// pay info
		int all_pay_real = all_earn + pocket_use - assets_store_real;
		Map<String, Object> map_real_pay = new HashMap<String, Object>();
		map_real_pay.put("name", "实际支出");
		map_real_pay.put("value", all_pay_real + "");
		map_real_pay.put("comment", "");
        evaluatelist_data.add(map_real_pay);
        
        for (Map.Entry<String, Integer> entry : pay_list.entrySet()) {
        	Map<String, Object> map_record_pay = new HashMap<String, Object>();
        	map_record_pay.put("name", entry.getKey() + "支出");
        	map_record_pay.put("value", entry.getValue() + "");
        	map_record_pay.put("comment", "");
            evaluatelist_data.add(map_record_pay);
        }
		
        Map<String, Object> map_record_pay_error = new HashMap<String, Object>();
        map_record_pay_error.put("name", "记账误差");
        map_record_pay_error.put("value", (all_pay_real != 0) ? 
        		String.format("%.2f", (all_pay - all_pay_real) * 100.0 / all_pay_real) + "%" : "- -");
        map_record_pay_error.put("comment", "");
        evaluatelist_data.add(map_record_pay_error);
			
		return true;
	}
	
	private Map<String, Integer> query_assets_delta(SQLiteDatabase db,
			int year, int month) {
		Map<String, Integer> assets_list = new HashMap<String, Integer>();
		int store = 0;
		int use = 0;
		Cursor c = null;		
		try {
			c = db.rawQuery("SELECT * FROM " + table_name_assets + " WHERE year = " + year + " AND month = " + month, null);
			while (c.moveToNext()) {
				int is_delete = c.getInt(c.getColumnIndex("is_deleted"));
				if (is_delete == 1) {
					continue;
				}
				int flow_type = c.getInt(c.getColumnIndex("flow_type"));
				int money = c.getInt(c.getColumnIndex("money"));
				if (flow_type == 0) {
					store += money;
				} else if (flow_type == 2) {
					use += money;
				}
			} 
		} catch(Exception e) {
			Log.i("QUERY", "QUERY MONTH ASSETS FAILED");
		}		
		assets_list.put("store", store);
		assets_list.put("use", use);
		return assets_list;
	}
	
	private Map<String, Integer> query_pocket_delta(SQLiteDatabase db,
			int year, int month) {
		Map<String, Integer> pocket_list = new HashMap<String, Integer>();
		int m_time = year * 12 + month;
		Cursor c = null;		
		try {
			c = db.rawQuery("SELECT * FROM " + table_name_pocket, null);
			while (c.moveToNext()) {
				int is_delete = c.getInt(c.getColumnIndex("is_deleted"));
				if (is_delete == 1) {
					continue;
				}
				int year_t = c.getInt(c.getColumnIndex("year"));
				int month_t = c.getInt(c.getColumnIndex("month"));
	    		int money = c.getInt(c.getColumnIndex("money"));
				if ((year_t * 12 + month_t) == m_time) {
					pocket_list.put("this_month", money);
				} else if ((year_t * 12 + month_t - 1) == m_time) {
					pocket_list.put("next_month", money);
				}
			}
		} catch(Exception e) {
			Log.i("QUERY", "QUERY MONTH POCKET FAILED");
		}
		return pocket_list;
	}
			
	private Map<String,Integer> query_total_bill(SQLiteDatabase db, String table_name) {
		int all_cost = 0;
		int all_earn = 0;
		Map<String,Integer> map = new HashMap<String, Integer>();
		
		Cursor c = null;		
		try {
			c = db.rawQuery("SELECT * FROM " + table_name + " ORDER BY DAY", null);
			while (c.moveToNext()) {
				int is_delete = c.getInt(c.getColumnIndex("is_deleted"));
				if (is_delete == 1) {
					continue;
				}
	    		int cost = c.getInt(c.getColumnIndex("cost"));
	    		int type = c.getInt(c.getColumnIndex("pay_earn"));
	    		if (type == 0) {
	    			all_cost += cost;
	    		} else {
	    			all_earn += cost;
	    		}
	    	}
			c.close();
		} catch(Exception e) {
			Log.i("QUERY", "QUERY MONTH BILLSUM FAILED");
		}
		map.put("pay", all_cost);
		map.put("earn", all_earn);
		return map;
	}
	
	private String get_property_sids(Map<String, Integer> update_property) {
		int id_size = update_property.size();
		String ids = "(";
		int i = 0;
		Iterator<Entry<String, Integer>> it = update_property.entrySet().iterator();
		while(it.hasNext()) {
			i++;
			String sid = it.next().getKey().toString();
			ids += "'";
			ids += sid;
			ids += "'";
			ids += ((i != id_size) ? "," : ")");
		}
		return ids;
	}
	
	private void fill_propertyrequest_push_pocket(SQLiteDatabase db, PropertyRequest.Builder request_builder,
			Map<String, Integer> update_pocket, String ids) {
		String sql = "SELECT * FROM " + table_name_pocket + " where sid in " + ids;
		Log.i(BWORD, sql);
		Cursor c = db.rawQuery(sql, null);
		while (c.moveToNext()) {
			int is_delete = c.getInt(c.getColumnIndex("is_deleted"));
    		int year = c.getInt(c.getColumnIndex("year"));
    		int month = c.getInt(c.getColumnIndex("month"));
    		int money = c.getInt(c.getColumnIndex("money"));
    		String sid = c.getString(c.getColumnIndex("sid"));	    		
    		PropertyRecord.Type type = (Integer.parseInt(update_pocket.get(sid).toString()) == 0) ?
    				PropertyRecord.Type.NEW : PropertyRecord.Type.UPDATE;
    		Log.i("QUERY", "pocket " + sid + " " + type.name() + " " + year + " " + month + " " + money);
    		request_builder.addPushPropertyRecords(PropertyRecord.newBuilder().setType(type)
    				.setPropertyType(PropertyRecord.PropertyType.POCKET_MONEY)
    				.setPocketRecord(PocketRecord.newBuilder().setSid(sid)
        				.setYear(year).setMonth(month).setMoney(money)
        				.setIsDeleted(is_delete).build()).build());
    	}
	}
	
	private void fill_propertyrequest_push_assets(SQLiteDatabase db, PropertyRequest.Builder request_builder,
			Map<String, Integer> update_assets, String ids) {
		String sql = "SELECT * FROM " + table_name_assets + " where sid in " + ids;
		Log.i(BWORD, sql);
		Cursor c = db.rawQuery(sql, null);
		while (c.moveToNext()) {
			int is_delete = c.getInt(c.getColumnIndex("is_deleted"));
    		int year = c.getInt(c.getColumnIndex("year"));
    		int month = c.getInt(c.getColumnIndex("month"));
    		int day = c.getInt(c.getColumnIndex("day"));
    		int store_addr = c.getInt(c.getColumnIndex("store_addr"));
    		int flow_type = c.getInt(c.getColumnIndex("flow_type"));
    		int money = c.getInt(c.getColumnIndex("money"));
    		int store_addr_op = c.getInt(c.getColumnIndex("store_addr_op"));
    		String sid = c.getString(c.getColumnIndex("sid"));	    		
    		PropertyRecord.Type type = (Integer.parseInt(update_assets.get(sid).toString()) == 0) ?
    				PropertyRecord.Type.NEW : PropertyRecord.Type.UPDATE;
    		Log.i("QUERY", "assets " + sid + " " + type.name() + " " + year + " " + month + " " + day
    				+ " " + store_addr + " " + flow_type + " " + money + " " + store_addr_op);
    		request_builder.addPushPropertyRecords(PropertyRecord.newBuilder().setType(type)
    				.setPropertyType(PropertyRecord.PropertyType.FIXED_ASSETS)
    				.setAssetsRecord(AssetsRecord.newBuilder().setSid(sid)
        				.setYear(year).setMonth(month).setDay(day)
        				.setStoreAddr(AssetsRecord.StoreAddr.valueOf(store_addr))
        				.setFlowType(AssetsRecord.FlowType.valueOf(flow_type))
        				.setStoreAddrOp(AssetsRecord.StoreAddr.valueOf(store_addr_op))
        				.setMoney(money).setIsDeleted(is_delete).build()).build());
    	}
	}
	
	public void fill_propertyrequest_push_content(SQLiteDatabase db, Map<String, Integer> update_pocket,
			Map<String, Integer> update_assets, PropertyRequest.Builder request_builder) {
		try {
			if (update_pocket.size() > 0) {
				String pocket_ids = get_property_sids(update_pocket);
				fill_propertyrequest_push_pocket(db, request_builder, update_pocket, pocket_ids);
			}
			if (update_assets.size() > 0) {
				String assets_ids = get_property_sids(update_assets);
				fill_propertyrequest_push_assets(db, request_builder, update_assets, assets_ids);
			}			
		} catch(Exception e) {
			e.printStackTrace();
			Log.i("QUERY", table_name_pocket + " NO DATA");
		}			
	}
	
	public boolean is_property_exist(SQLiteDatabase db,
			PropertyType property_type, String sid) {
		if (property_type == PropertyRecord.PropertyType.POCKET_MONEY ? 
				create_pocket_table(db, table_name_pocket) :
				create_assets_table(db, table_name_assets)) {
			// 建表失败，认为是不存在
        	return false;
        }
		boolean ret = false;
		String table_name = (property_type == PropertyRecord.PropertyType.POCKET_MONEY ?
				table_name_pocket : table_name_assets);
		String sql = "SELECT * FROM " + table_name + " where sid=?";
		Cursor c = db.rawQuery(sql, new String[]{sid});
		if(c.moveToFirst()) {
		    ret = true;
		}
		c.close();
		return ret;
	}
	
	public boolean dump_property_records_over_again(SQLiteDatabase db) {
		// first delete old record and record_size file
		String record_file = PPIndexManager.record_property_file;
		String record_size_file = PPIndexManager.record_property_size_file;
		file_helper.deleteFile(record_file);
		file_helper.deleteFile(record_size_file);

		// then select sid from pocket and assets, update record and record_size
		Cursor c = null;		
		try {
			c = db.rawQuery("SELECT sid FROM " + table_name_pocket + " where is_deleted=0 ORDER BY YEAR,MONTH", null);
			while (c.moveToNext()) {
				String sid = c.getString(c.getColumnIndex("sid"));
				update_local_index_of_property(sid, true, true);
			}
			c.close();
			
			c = db.rawQuery("SELECT sid FROM " + table_name_assets + " where is_deleted=0 ORDER BY YEAR,MONTH,DAY", null);
			while (c.moveToNext()) {
				String sid = c.getString(c.getColumnIndex("sid"));
				update_local_index_of_property(sid, false, true);
			}
			c.close();
		} catch(Exception e) {
			Log.i("QUERY", "DUMP PROPERTY FAILED");
			return false;
		}
		return true;
	}
	
	private void update_local_index_of_property(String sid, boolean pocket_or_assets, boolean new_or_update) {		
		String record_line = (pocket_or_assets ? "p\t" : "a\t") 
				+ (new_or_update ? "0\t" : "1\t") + sid;
		pp_index_manager.update_local_index(PPIndexManager.record_property_size_file,
				PPIndexManager.record_property_file, record_line);
	}
	
	private boolean create_assets_table(SQLiteDatabase db,
			String table_name) {
        //drop_table(db, table_name);
		try {
			String create_sql = "create table if not exists " + table_name + ASSETS_TABLE_SCHEMA;
			db.execSQL(create_sql);
		} catch(Exception e) {
			Log.i(BWORD,"create a assets_table [" + table_name + "] failed");
			return false;
		}		
		Log.i(BWORD,"create a assets_table [" + table_name + "] done");
		return true;
	}
	
	private boolean create_pocket_table(SQLiteDatabase db,
			String table_name) {
		//drop_table(db, table_name);
		try {
			String create_sql = "create table if not exists " + table_name + POCKET_TABLE_SCHEMA;
			db.execSQL(create_sql);
		} catch(Exception e) {
			Log.i(BWORD,"create a pocket_table [" + table_name + "] failed");
			return false;
		}		
		Log.i(BWORD,"create a pocket_table [" + table_name + "] done");
		return true;
	}

	
	/*---------------------------------------------------------*/
	/* for public */
	/*---------------------------------------------------------*/
	
	public boolean delete_table(SQLiteDatabase db, String table_name) {
		String sql = "delete from " + table_name;
		db.execSQL(sql);
		return true;
	}
	
	public boolean drop_table(SQLiteDatabase db, String table_name) {
		String sql = "drop table " + table_name;
		db.execSQL(sql);
		return true;
	}
}
