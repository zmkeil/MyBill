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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AssetFragment extends Fragment {
	
	private DBHelper db_helper;
	private SQLiteDatabase db;
	
	private DateSpinnerView date_view;
	
	private ListView pocketlist_view;
	private List<Map<String, Object>> pocketlist_data;
	private SimpleAdapter pocketlist_adapter;
	
	private ListView assetslist_view;
	private TextView assets_total_view;
	private TextView assets_increment_view;
	private List<Map<String, Object>> assetslist_data;
	private Map<String, Integer> assetslist_sum_data;
	private SimpleAdapter assetslist_adapter;
	
	private Context mcontext;
	
	final private String BWORD = "ASSETS_F";
	final private String EXTRA_UPDATE_ID = "EXTRA_UPDATE_ID";
	final private String EXTRA_UPDATE_IS_POCKET = "EXTRA_UPDATE_IS_POCKET";
	final private String EXTRA_UPDATE_YEAR = "EXTRA_UPDATE_YEAR";
	final private String EXTRA_UPDATE_MONTH = "EXTRA_UPDATE_MONTH";
	final private String EXTRA_UPDATE_DAY = "EXTRA_UPDATE_DAY";
	
	public AssetFragment(Context context) {
		this.mcontext = context;
		
		pocketlist_data = new ArrayList<Map<String, Object>>();
		pocketlist_adapter = new SimpleAdapter(context, pocketlist_data, R.layout.pocketlist_item,
	        	new String[]{"yymm","money","comment"},
	        	new int[]{R.id.pocketlist_item_yymm,R.id.pocketlist_item_money,
	        		R.id.pocketlist_item_comment});
		
		assetslist_data = new ArrayList<Map<String, Object>>();
		assetslist_sum_data = new HashMap<String, Integer>();
		assetslist_adapter = new SimpleAdapter(context, assetslist_data, R.layout.assetslist_item,
	        	new String[]{"yymmdd", "item","flow_type","money", "dest_item"},
	        	new int[]{R.id.assetslist_item_yymmdd, R.id.assetslist_item_src,R.id.assetslist_item_flow,
	        		R.id.assetslist_item_money, R.id.assetslist_item_dest});

		// NOTE!!! here mcontext must be activity's, getActivity() will error.
		db_helper = new DBHelper(mcontext);
        db = db_helper.getWritableDatabase();
	}

    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState)  
    {  
    	RelativeLayout ly =  (RelativeLayout)inflater.inflate(R.layout.assets_detail_fragment, container, false); 
    	date_view = (DateSpinnerView) ly.findViewById(R.id.id_assets_dateview);
		pocketlist_view = (ListView) ly.findViewById(R.id.id_pocket_detail_listview);
		assetslist_view = (ListView) ly.findViewById(R.id.id_assets_detail_listview);
		assets_total_view = (TextView) ly.findViewById(R.id.id_assets_total_textview);
		assets_increment_view = (TextView) ly.findViewById(R.id.id_assets_increment_textview);
		Log.i("BWORD", "onCreateView");

		// show current month's bills by default
        Calendar c = Calendar.getInstance();
        int year_position = c.get(Calendar.YEAR) - 2015;
        int month_position = c.get(Calendar.MONTH);
        Log.i("BWORD", "month from date_view: " + date_view.get_month_p() + ", month: " + month_position);
        Log.i("BWORD", "day from date_view: " + date_view.get_day_p() + ", day: " + 0);
        date_view.setIntialParams(new boolean[]{true, true, true},
				new int[]{year_position, month_position, 0},
				new boolean[]{true, true, true}, new PopularCallback() {
					@Override
					public void func(String context) {
						historyPocketQuery(false, context);
						historyAssetsQuery(false, context);
					}
        		}
		);
        Log.i("BWORD", "month after date_view.setiParams: " + date_view.get_month_p());
		date_view.setDayToAssetsType();
		
		// set listView's adapter
		pocketlist_view.setAdapter(pocketlist_adapter);
		pocketlist_view.setOnItemClickListener(new OnItemClickListener(){        	 
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            	if (pocketlist_data.get(arg2).containsKey("id")) {
            		String user_s = PreferenceManager.getDefaultSharedPreferences(mcontext).getString("user_name", null);
            		if (user_s.equals("zmkeil:赵")) { 
	            		int id = Integer.valueOf(pocketlist_data.get(arg2).get("id").toString());
	                	updateAssets(id, true);
            		} else {
            			new AlertDialog.Builder(getActivity()).setTitle("系统提示")
                        .setMessage("只有管理员能修改资产信息")
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
		
		assetslist_view.setAdapter(assetslist_adapter);
		assetslist_view.setOnItemClickListener(new OnItemClickListener(){        	 
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            	if (assetslist_data.get(arg2).containsKey("id")) {
            		String user_s = PreferenceManager.getDefaultSharedPreferences(mcontext).getString("user_name", null);
            		if (user_s.equals("zmkeil:赵")) { 
	            		int id = Integer.valueOf(assetslist_data.get(arg2).get("id").toString());
	                	updateAssets(id, false);
            		} else {
            			new AlertDialog.Builder(getActivity()).setTitle("系统提示")
                        .setMessage("只有管理员能修改资产信息")
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
		date_view.setDayToAssetsType();
		historyPocketQuery(true, "resume");
		historyAssetsQuery(true, "resume");
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
	
	private void historyPocketQuery(boolean from_resume, String from_context) {
    	getPocketData();
        Log.i(BWORD, "item num: " + pocketlist_data.size() + ", from_context: " + from_context);
    	pocketlist_adapter.notifyDataSetChanged();
    }
	
	private void historyAssetsQuery(boolean from_resume, String from_context) {
    	getAssetsData();
        Log.i(BWORD, "item num: " + assetslist_data.size() + ", from_context: " + from_context);
    	assetslist_adapter.notifyDataSetChanged();
    	assets_total_view.setText(assetslist_sum_data.get("total").toString());
    	assets_increment_view.setText(assetslist_sum_data.get("increment").toString());
    }
	
	private boolean getPocketData() {		
    	pocketlist_data.clear();
    	Map<String, Object> map_title = new HashMap<String, Object>();
		map_title.put("yymm", "年月");
        map_title.put("money", "额度");
        map_title.put("comment", "备注");
        pocketlist_data.add(map_title);
    	return db_helper.query_pockets(db, date_view.get_year_s(), 
    			date_view.get_month_s(), pocketlist_data);
    }
	
	private boolean getAssetsData() {
		assetslist_data.clear();
		assetslist_sum_data.clear();
		Map<String, Object> map_title = new HashMap<String, Object>();
		map_title.put("yymmdd", "日期");
		map_title.put("item", "资产项");
        map_title.put("flow_type", "流动");
        map_title.put("money", "金额");
        map_title.put("dest_item", "流向");
        assetslist_data.add(map_title);
    	return db_helper.query_assets(db, date_view.get_year_s(), date_view.get_month_s(), 
    			Integer.valueOf(date_view.get_assets_s()), assetslist_data, assetslist_sum_data);
	}
	
	
	private void updateAssets(int id, boolean is_pocket) {
    	Intent intent = new Intent(getActivity(), UpdateAssetsActivity.class);
    	intent.putExtra(EXTRA_UPDATE_ID, id);  
    	intent.putExtra(EXTRA_UPDATE_IS_POCKET, is_pocket);   
    	
    	startActivity(intent);
    }

	public void select_year(int year_position) {
		date_view.select_year(year_position);		
	}

	public void select_month(int month_position) {
		date_view.select_month(month_position);		
	}
    
}
