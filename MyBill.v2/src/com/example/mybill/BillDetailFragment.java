package com.example.mybill;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.mybill.util.DBHelper;
import com.example.mybill.util.PopularCallback;
import com.example.mybill.widget.DateSpinnerView;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.preference.PreferenceManager;

public class BillDetailFragment extends Fragment {
	
	final private String BWORD = "BILL";
	
	private DBHelper db_helper;
	private SQLiteDatabase db;
	
	private DateSpinnerView date_view;		
	private ListView billlist_view;
	private List<Map<String, Object>> billlist_data;
	private SimpleAdapter billlist_adapter;
	
	final private String EXTRA_UPDATE_ID = "EXTRA_UPDATE_ID";
	final private String EXTRA_UPDATE_YEAR = "EXTRA_UPDATE_YEAR";
	final private String EXTRA_UPDATE_MONTH = "EXTRA_UPDATE_MONTH";
	final private String EXTRA_UPDATE_DAY = "EXTRA_UPDATE_DAY";
	final private String EXTRA_UPDATE_TABLE_NAME = "EXTRA_UPDATE_TABLE_NAME";
	
	private Context mcontext;
	
	public BillDetailFragment(Context context) {
		this.mcontext = context;
		
		Log.i(BWORD, "new DetailFragment");
		
		billlist_data = new ArrayList<Map<String, Object>>();
		billlist_adapter = new SimpleAdapter(context, billlist_data, R.layout.billlist_item,
	        	new String[]{"day","consumer","comment","cost"},
	        	new int[]{R.id.billlist_item_day,R.id.billlist_item_consumer,
	        		R.id.billlist_item_comment,R.id.billlist_item_cost});

		// NOTE!!! here mcontext must be activity's, getActivity() will error.
		db_helper = new DBHelper(mcontext);
        db = db_helper.getWritableDatabase();
        //db.execSQL("DROP TABLE IF EXISTS bills_2015_12"); 
	}
	
	@Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState)  
    {  
		RelativeLayout ly =  (RelativeLayout) inflater.inflate(R.layout.bill_detail_fragment, container, false); 
		date_view = (DateSpinnerView) ly.findViewById(R.id.id_bill_detail_dateview);
		billlist_view = (ListView) ly.findViewById(R.id.id_bill_detail_listview);
		Log.i("BWORD", "onCreateView");

		// show current month's bills by default
        Calendar c = Calendar.getInstance();
        int year_position = c.get(Calendar.YEAR) - 2015;
        int month_position = c.get(Calendar.MONTH);
        Log.i("BWORD", "month from date_view: " + date_view.get_month_p() + ", month: " + month_position);
        Log.i("BWORD", "day from date_view: " + date_view.get_day_p() + ", day: " + 0);
        date_view.setIntialParams(new boolean[]{true, true, false},
				new int[]{year_position, month_position, 0},
				new boolean[]{true, true, true}, new PopularCallback() {
					@Override
					public void func(String context) {
						historyBillQuery(false, context);
					}
        		}
		);
        Log.i("BWORD", "month after date_view.setiParams: " + date_view.get_month_p());
		date_view.setDayToBillType();
		
		// set listView's adapter
		billlist_view.setAdapter(billlist_adapter);
		billlist_view.setOnItemClickListener(new OnItemClickListener(){        	 
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            	if (billlist_data.get(arg2).containsKey("id")) {
            		if (date_view.get_day_p() <= 1) { 
	            		int id = Integer.valueOf(billlist_data.get(arg2).get("id").toString());
	                	int day_position = Integer.valueOf(billlist_data.get(arg2).get("day").toString()) - 1;
	                	updateBill(id, day_position);
            		} else {
            			new AlertDialog.Builder(getActivity()).setTitle("系统提示")
                        .setMessage("您不能修改他人的记录")
                        .setPositiveButton("确定",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        }).show();
            		}            		
            	}
            }
        });
        return ly;
    }
	
	@Override
	public void onResume() {
		super.onResume();
		Log.i("BWORD", "onResume");
		date_view.setDayToBillType();
		historyBillQuery(true, "resume");
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.i("FG", "--MyFragment->>onDestroyView");
		date_view.clearAnimation();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("FG", "--MyFragment->>onDestroy");
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		Log.i("FG", "--MyFragment->>onSaveInstanceState");
		//String content = etCon.getText().toString();
		//utState.putString("inputCon", content);
	}
	
	@Override
	public void onViewStateRestored(Bundle savedInstanceState){
		super.onViewStateRestored(savedInstanceState);
		Log.i("FG", "--MyFragment->>onViewStateRestored");
		if(savedInstanceState != null){
			Log.i("FG", "--MyFragment->>onViewStateRestored not null");
			//etCon.setText(savedInstanceState.getString("inputCon", "");
		}
	}
	
	private void historyBillQuery(boolean from_resume, String from_context) {
    	if ((!getData()) && (!from_resume)) {
    		new AlertDialog.Builder(getActivity()).setTitle("系统提示")
            .setMessage("您选择的 "+ date_view.get_year_s() + " 年 "
            		+ date_view.get_month_s() + " 月 " + "没有"
            		+ ((date_view.get_day_p() == 0) ? "账单" : "收入") + "记录")
            .setPositiveButton("确定",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            }).show();
    	}
        Log.i("BWORD", "item num: " + billlist_data.size() + ", from_context: " + from_context);
    	billlist_adapter.notifyDataSetChanged();
    }
	
	private void updateBill(int id, int day_position) {
    	Intent intent = new Intent(getActivity(), UpdateBillActivity.class);
    	intent.putExtra(EXTRA_UPDATE_ID, id);    	
    	intent.putExtra(EXTRA_UPDATE_YEAR, date_view.get_year_p());
    	intent.putExtra(EXTRA_UPDATE_MONTH, date_view.get_month_p());
    	intent.putExtra(EXTRA_UPDATE_DAY, day_position);
    	
    	//int type = date_view.get_day_p();
    	String gay = PreferenceManager.getDefaultSharedPreferences(this.mcontext)
    			.getString("user_name", "noset").split(":")[0];
    	String table_prefix = "bills_of_" + gay + '_';
    	String table_name = table_prefix + date_view.get_year_s()
    			+ "_" + date_view.get_month_s();
    	intent.putExtra(EXTRA_UPDATE_TABLE_NAME, table_name);
    	startActivity(intent);
    }
	
	private boolean getData() {
		int gay_p = date_view.get_day_p();
		String gay_name = date_view.get_gay_s();
		String table_prefix = "bills_of_" + gay_name + '_';
		String role = "消费者";
		if (gay_p % 2 == 1) {
			role = "收入者";
		}
		
    	billlist_data.clear();
    	Map<String, Object> map_title = new HashMap<String, Object>();
		map_title.put("day", "日期");
		map_title.put("consumer", role);
        map_title.put("comment", "明细");
        map_title.put("cost", "金额");
        billlist_data.add(map_title);
    	
    	String bill_table_name = table_prefix + date_view.get_year_s() +
    			"_" + date_view.get_month_s();
    	Log.i(BWORD, "table name " + bill_table_name);
    	return db_helper.query_bills(db, bill_table_name, gay_p % 2, billlist_data);
    }

	public void select_year(int year_position) {
		date_view.select_year(year_position);
	}

	public void select_month(int month_position) {
		date_view.select_month(month_position);
	}

	public void select_day(int day_position) {
		date_view.select_day(day_position);
	}
	
}
