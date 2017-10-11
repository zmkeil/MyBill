package com.example.mybill;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.mybill.util.AdaptorContent;
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

public class EvaluateFragment extends Fragment {
	
	final private String BWORD = "BILL";
	
	private DBHelper db_helper;
	private SQLiteDatabase db;
	
	private DateSpinnerView date_view;		
	private ListView evaluatelist_view;
	private List<Map<String, Object>> evaluatelist_data;
	private SimpleAdapter evaluatelist_adapter;
	
	private Context mcontext;
	
	public EvaluateFragment(Context context) {
		this.mcontext = context;
		
		Log.i(BWORD, "new DetailFragment");
		
		evaluatelist_data = new ArrayList<Map<String, Object>>();
		evaluatelist_adapter = new SimpleAdapter(context, evaluatelist_data, R.layout.evaluatelist_item,
	        	new String[]{"name","value","comment"},
	        	new int[]{R.id.evaluatelist_item_name,R.id.evaluatelist_item_value,
	        		R.id.evaluatelist_item_comment});

		// NOTE!!! here mcontext must be activity's, getActivity() will error.
		db_helper = new DBHelper(mcontext);
        db = db_helper.getWritableDatabase();
	}
	
	@Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState)  
    {  
		RelativeLayout ly =  (RelativeLayout) inflater.inflate(R.layout.month_evaluate_fragment, container, false); 
		date_view = (DateSpinnerView) ly.findViewById(R.id.id_evaluate_dateview);
		evaluatelist_view = (ListView) ly.findViewById(R.id.id_evaluate_listview);
		Log.i("BWORD", "onCreateView");

		// show current month's bills by default
        Calendar c = Calendar.getInstance();
        int year_position = c.get(Calendar.YEAR) - 2015;
        int month_position = c.get(Calendar.MONTH);
        Log.i("BWORD", "month from date_view: " + date_view.get_month_p() + ", month: " + month_position);
        Log.i("BWORD", "day from date_view: " + date_view.get_day_p() + ", day: " + 0);
        date_view.setIntialParams(new boolean[]{true, true, false},
				new int[]{year_position, month_position, 0},
				new boolean[]{true, true, false}, new PopularCallback() {
					@Override
					public void func(String context) {
						evaluateQuery(false, context);
					}
        		}
		);
        Log.i("BWORD", "month after date_view.setiParams: " + date_view.get_month_p());
		
		// set listView's adapter
		evaluatelist_view.setAdapter(evaluatelist_adapter);
        return ly;
    }
	
	@Override
	public void onResume() {
		super.onResume();
		Log.i("BWORD", "onResume");
		date_view.setDayToBillType();
		evaluateQuery(true, "resume");
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
	
	private void evaluateQuery(boolean from_resume, String from_context) {
    	getEvaluateData();
        Log.i("BWORD", "item num: " + evaluatelist_data.size() + ", from_context: " + from_context);
    	evaluatelist_adapter.notifyDataSetChanged();
    }
	
	
	private boolean getEvaluateData() {
		Map<String, String> bill_table_info = new HashMap<String, String>();
		
		String user_s = PreferenceManager.getDefaultSharedPreferences(this.mcontext).getString("user_name", "jxj:金");
    	String[] user_sa = user_s.split(":");
    	if (user_sa.length == 2) {
    		String table_name = "bills_of_" + user_sa[0] 
    				+ "_" + date_view.get_year_s() + "_" + date_view.get_month_s();
    		String gay_name = user_sa[1];
    		bill_table_info.put(gay_name, table_name);
    	}		
		for (int i = 1; i < 4; i++) {
			String sharer_s = PreferenceManager.getDefaultSharedPreferences(this.mcontext).getString("sharer_name" + i, "");
			String[] sharer_sa = sharer_s.split(":");
			if (sharer_sa.length == 2) {
				String table_name = "bills_of_" + sharer_sa[0] 
	    				+ "_" + date_view.get_year_s() + "_" + date_view.get_month_s();
	    		String gay_name = sharer_sa[1];
	    		bill_table_info.put(gay_name, table_name);
			}
		}
		
    	evaluatelist_data.clear();
    	Map<String, Object> map_title = new HashMap<String, Object>();
		map_title.put("name", "条目");
		map_title.put("value", "金额/占比");
        map_title.put("comment", "备注");
        evaluatelist_data.add(map_title);
    	
    	return db_helper.query_evaluate(db, date_view.get_year(), date_view.get_month(), 
    			bill_table_info, evaluatelist_data);
    }
	
}
