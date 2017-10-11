package com.example.mybill;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.example.mybill.util.AdaptorContent;
import com.example.mybill.util.AssetsUtil;
import com.example.mybill.util.DBHelper;
import com.example.mybill.util.MyAdapter;
import com.example.mybill.util.PopularCallback;
import com.example.mybill.util.VariousDialog;
import com.example.mybill.widget.DateSpinnerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class NewAssetsActivity extends Activity {
		
	private static final String BWORD="NEWASSETS";
	
	final public String EXTRA_INSERT_PAY_OR_EARN = "EXTRA_INSERT_PAY_OR_EARN";
	final static public String EXTRA_UPDATE_YEAR = "EXTRA_UPDATE_YEAR";
	final static public String EXTRA_UPDATE_MONTH = "EXTRA_UPDATE_MONTH";
	final static public String EXTRA_UPDATE_DAY = "EXTRA_UPDATE_DAY";
	
	private DBHelper db_helper;
	private SQLiteDatabase db;
	
	private DateSpinnerView date_spinner;
	
	private Spinner assets_item_Spinner;
	private Spinner assets_flowtype_Spinner;
	private EditText assets_cost_EditText;
	private Spinner assets_dest_item_Spinner;
	
	private MyAdapter assets_item_adaptor;
	private MyAdapter assets_flowtype_adaptor;
	private MyAdapter assets_dest_item_adaptor;

	private int item_position = 0;
	private int flowtype_position = 0;
	private int dest_item_position = 0;
	
	private Context mcontext;
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_assets);
        mcontext = this;        
                
        db_helper = new DBHelper(this);
        db = db_helper.getWritableDatabase();
        
        // create today's bill by default
        Calendar c = Calendar.getInstance();
        int year_position = c.get(Calendar.YEAR) - 2015;
        int month_position = c.get(Calendar.MONTH);
        int day_position = c.get(Calendar.DAY_OF_MONTH) - 1;
        
        date_spinner = (DateSpinnerView)findViewById(R.id.id_assets_record_dateview);
        date_spinner.setIntialParams(new boolean[]{true, true, true},
				new int[]{year_position, month_position, day_position},
				new boolean[]{true, true, true}, null);
        
        assets_item_Spinner = (Spinner)findViewById(R.id.assetsItem);
        assets_flowtype_Spinner = (Spinner)findViewById(R.id.assetsFlowType);
        assets_cost_EditText = (EditText)findViewById(R.id.assetsCost); 
        assets_dest_item_Spinner = (Spinner)findViewById(R.id.assetsDestItem); 
        
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
	         @Override
	         public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3)
	         {
	        	 switch(arg0.getId()) {
	        	 case R.id.assetsItem:
	        		  item_position = position;
	        		  break;
	        	 case R.id.assetsFlowType:
	        		  flowtype_position = position;
	        		  break;
	        	 case R.id.assetsDestItem:
	        		  dest_item_position = position;
	        		  break;
	        	 default:
	        		 break;
	        	 }
	        	 stype_change();
	         }

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
			
			private void stype_change() {
				if (item_position == 8) {
					assets_flowtype_Spinner.setEnabled(false);
					assets_dest_item_Spinner.setEnabled(false);
				} else {
					assets_flowtype_Spinner.setEnabled(true);
					if (flowtype_position == 4) {
						assets_dest_item_Spinner.setEnabled(true);
					} else {
						assets_dest_item_Spinner.setEnabled(false);
					}
				}
			}
        };
    	assets_item_adaptor = new MyAdapter(mcontext, AssetsUtil.getInstance().assets_item_withp_list); 
		assets_flowtype_adaptor = new MyAdapter(mcontext, AssetsUtil.getInstance().assets_action_list);		      
		assets_dest_item_adaptor = new MyAdapter(mcontext, AssetsUtil.getInstance().assets_item_list);		
		
		assets_item_Spinner.setAdapter(assets_item_adaptor);
		assets_flowtype_Spinner.setAdapter(assets_flowtype_adaptor);
		assets_dest_item_Spinner.setAdapter(assets_dest_item_adaptor);
				
		assets_item_Spinner.setOnItemSelectedListener(listener);
		assets_flowtype_Spinner.setOnItemSelectedListener(listener);
		assets_dest_item_Spinner.setOnItemSelectedListener(listener);
    }
    
    public void newAssetsCreate(View view) {
    	Log.i(BWORD, "create a new assets");
    	String item_s = ((AdaptorContent)assets_item_adaptor.getItem(item_position)).display_text;
    	String flowtype_s = ((AdaptorContent)assets_flowtype_adaptor.getItem(flowtype_position)).display_text;
    	String cost_s = assets_cost_EditText.getText().toString();
    	String dest_item_s = ((AdaptorContent)assets_dest_item_adaptor.getItem(dest_item_position)).display_text;

    	String user_s = PreferenceManager.getDefaultSharedPreferences(mcontext).getString("user_name", null);
		if (!user_s.equals("zmkeil:赵")) { 
			VariousDialog.new_alert_dialog(this, "系统提示", "只有管理员能添加资产项",
    				"确定", null, null, null).show();
		} else if (cost_s.isEmpty()) {
    		VariousDialog.new_alert_dialog(this, "系统提示", "请填写金额信息",
    				"确定", null, null, null).show();
    	} else if (flowtype_position == 4 && item_position == dest_item_position) {
    		VariousDialog.new_alert_dialog(this, "系统提示", "资金不能项目内转移",
    				"确定", null, null, null).show();
    	} else {
    		String message = "您确认创建新单子： \n" + "日期： "
            		+ date_spinner.get_year_s() + " 年 "
            		+ date_spinner.get_month_s() + " 月 "
            		+ date_spinner.get_day_s() + " 日\n";
    		if (item_position == 8) {
    			message += "流动资金 - 结算\n";
    		} else {
    			if (flowtype_position != 4) {
    				message += (item_s + " - " + flowtype_s + "\n");
    			} else {
    				message += (item_s + " 转移到 " + dest_item_s + "\n");
    			}
    		}
            message += ("金额： " + cost_s);
        	VariousDialog.new_alert_dialog(this, "系统提示", message,
        		"确定", new PopularCallback() {
    				@Override
    				public void func(String context) {
    					really_insert();
    				}    		
    	    	}, "取消", null).show();
    	}
    }
    
    private void really_insert() {
    	Log.i(BWORD, "really insert");
    	String item_n = ((AdaptorContent)assets_item_adaptor.getItem(item_position)).ext_text;
    	String flowtype_n = ((AdaptorContent)assets_flowtype_adaptor.getItem(flowtype_position)).ext_text;
    	String cost_s = assets_cost_EditText.getText().toString();
    	String dest_item_n = ((AdaptorContent)assets_dest_item_adaptor.getItem(dest_item_position)).ext_text;
    	
        if (!db_helper.insert_new_assets(db, null, date_spinner.get_year(), date_spinner.get_month(),
        		date_spinner.get_day(), item_n, flowtype_n, Integer.valueOf(cost_s), dest_item_n)) {
        	VariousDialog.new_alert_dialog(this, "系统提示", "创建单子失败\n请稍后重试或联系管理员",
        			"确定", null, null, null).show();
        } else {
        	VariousDialog.new_alert_dialog(this, "系统提示", "创建单子成功\n要继续建单吗？",
        			"是", _continue, "否", _skim).show();
        }
    }
    
    private PopularCallback _continue = new PopularCallback() {
		@Override
		public void func(String context) {
			assets_cost_EditText.setText("");
		}   	
    };

    private PopularCallback _skim = new PopularCallback() {
		@Override
		public void func(String context) {
			//Intent intent = new Intent(_context, HistoryBillActivity.class);
			Intent intent = new Intent();
			intent.putExtra(EXTRA_UPDATE_YEAR, date_spinner.get_year_p());
			intent.putExtra(EXTRA_UPDATE_MONTH, date_spinner.get_month_p());
//			intent.putExtra(EXTRA_UPDATE_DAY, is_pay ? 0 : 1);

			// return MainActivity
			setResult(RESULT_OK, intent);  
            finish();  
		}
    };
}
