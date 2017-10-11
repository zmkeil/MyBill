package com.example.mybill;

import java.util.HashMap;
import java.util.Map;

import com.example.mybill.util.AssetsUtil;
import com.example.mybill.util.DBHelper;
import com.example.mybill.util.MyAdapter;
import com.example.mybill.util.PopularCallback;
import com.example.mybill.util.VariousDialog;
import com.example.mybill.widget.DateSpinnerView;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class UpdateAssetsActivity extends Activity {
	
	final private String BWORD = "UpdateAssets";
	final private String EXTRA_UPDATE_ID = "EXTRA_UPDATE_ID";
	final private String EXTRA_UPDATE_IS_POCKET = "EXTRA_UPDATE_IS_POCKET";
	final private String EXTRA_UPDATE_YEAR = "EXTRA_UPDATE_YEAR";
	final private String EXTRA_UPDATE_MONTH = "EXTRA_UPDATE_MONTH";
	final private String EXTRA_UPDATE_DAY = "EXTRA_UPDATE_DAY";
	
	private DBHelper db_helper;
	private SQLiteDatabase db;
	
	private DateSpinnerView date_spinner;
	private TextView assets_item_TextView;
	private TextView assets_flowtype_TextView;
	private EditText assets_cost_EditText;
	private TextView assets_dest_item_TextView;
		
	private int assets_id;
	private String assets_sid;
	private boolean is_pocket;
	private int origin_day_position;
	private Map<String,Object> update_data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_assets);
		
		getActionBar().setTitle("手别抖");
        // hide Home(logo and icon)
        getActionBar().setDisplayShowHomeEnabled(false);
        // up the Home
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // use a new icon
        getActionBar().setHomeAsUpIndicator(R.drawable.back);
        
		date_spinner = (DateSpinnerView)findViewById(R.id.id_assets_record_dateview);
        assets_item_TextView = (TextView)findViewById(R.id.assetsItem);
        assets_flowtype_TextView = (TextView)findViewById(R.id.assetsFlowType);
        assets_cost_EditText = (EditText)findViewById(R.id.assetsCost); 
        assets_dest_item_TextView = (TextView)findViewById(R.id.assetsDestItem); 
		
		db_helper = new DBHelper(this);
        db = db_helper.getWritableDatabase();
        
        update_data = new HashMap<String,Object>();
		update_data.clear();
		
		Intent intent =getIntent();
		assets_id = intent.getIntExtra(EXTRA_UPDATE_ID, -1);
		is_pocket = intent.getBooleanExtra(EXTRA_UPDATE_IS_POCKET, true);
		Map<String,Object> org_data = db_helper.get_property_data(db, is_pocket, assets_id);
		assets_sid = org_data.get("sid").toString();
		show_origin_assets(org_data);
	}
	
	private void show_origin_assets(Map<String,Object> org_data) {
		assets_cost_EditText.setText(org_data.get("money").toString());
		int year = Integer.valueOf(org_data.get("year").toString());
		int month = Integer.valueOf(org_data.get("month").toString());
		
		if (is_pocket) {
			assets_item_TextView.setText("零钱");
			assets_flowtype_TextView.setText("- -");
			assets_dest_item_TextView.setText("- -");
			date_spinner.setIntialParams(new boolean[]{true, true, false},
					new int[]{year - 2015, month - 1, 0},
					new boolean[]{false, false, false}, null);
		} else {
			int day = Integer.valueOf(org_data.get("day").toString());
			date_spinner.setIntialParams(new boolean[]{true, true, true},
					new int[]{year - 2015, month - 1, day - 1},
					new boolean[]{false, false, true}, null);
			int store_addr = Integer.valueOf(org_data.get("store_addr").toString());
			int flow_type = Integer.valueOf(org_data.get("flow_type").toString());
			assets_item_TextView.setText(AssetsUtil.getInstance().assets_item_map.get("" + store_addr));
			assets_flowtype_TextView.setText(AssetsUtil.getInstance().assets_action_map.get("" + flow_type));
			if (flow_type == 4) {
				int store_addr_op = Integer.valueOf(org_data.get("store_addr_op").toString());
				assets_dest_item_TextView.setText(AssetsUtil.getInstance().assets_item_map.get("" + store_addr_op));
			} else {
				assets_dest_item_TextView.setText("- -");
			}
		}
	}
    
    private void really_update(String action) {
    	if (db_helper.update_property(db, is_pocket, assets_id, assets_sid, update_data)) {
    		update_data.clear();
    		VariousDialog.new_alert_dialog(this, "账单" + action, "成功!",
    				"确定", finish_it, null, null).show();
    	} else {
    		VariousDialog.new_alert_dialog(this, "账单" + action, "失败!\n请稍后重试",
    				"确定", finish_it, null, null).show();
    	}
    }
    
    public void updateAssetsSure(View view) {
		update_data.put("money", Integer.valueOf(assets_cost_EditText.getText().toString()));
    	if (!is_pocket) {
    		update_data.put("day", date_spinner.get_day_s());
    	} 	
    	really_update("更新");
    }
    
    public void disableAssetsSure(View view) {
    	update_data.clear();
    	update_data.put("delete", true);
    	really_update("删除");
    }
    
    private PopularCallback finish_it = new PopularCallback() {
		@Override
		public void func(String context) {
			finish();
		}	
	};
    
    private class Watcher implements TextWatcher {
    	Watcher(EditText view, String key) {
    		this._view = view;
    		this._key = key;
    	}   	
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {			
		}

		@Override
		public void afterTextChanged(Editable s) {
			update_data.put(_key, _view.getText().toString());
		}
		
		private String _key;
		private EditText _view;
    	
    };

}
