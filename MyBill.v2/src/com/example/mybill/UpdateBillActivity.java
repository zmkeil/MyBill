package com.example.mybill;

import java.util.HashMap;
import java.util.Map;

import com.example.mybill.util.DBHelper;
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

public class UpdateBillActivity extends Activity {
	
	final private String BWORD = "UpdateBill";
	final private String EXTRA_UPDATE_ID = "EXTRA_UPDATE_ID";
	final private String EXTRA_UPDATE_YEAR = "EXTRA_UPDATE_YEAR";
	final private String EXTRA_UPDATE_MONTH = "EXTRA_UPDATE_MONTH";
	final private String EXTRA_UPDATE_DAY = "EXTRA_UPDATE_DAY";
	final private String EXTRA_UPDATE_TABLE_NAME = "EXTRA_UPDATE_TABLE_NAME";
	
	private DBHelper db_helper;
	private SQLiteDatabase db;
	
	private EditText bill_consumer_EditText;
	private EditText bill_comment_EditText;
	private EditText bill_cost_EditText;
	private DateSpinnerView date_spinner;
		
	private int bill_id;
	private String bill_sid;
	private String bill_table_name;
	private int origin_day_position;
	private Map<String,Object> update_data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_bill);
		
		getActionBar().setTitle("手别抖");
        // hide Home(logo and icon)
        getActionBar().setDisplayShowHomeEnabled(false);
        // up the Home
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // use a new icon
        getActionBar().setHomeAsUpIndicator(R.drawable.back);
		
		bill_consumer_EditText = (EditText)findViewById(R.id.billConsumer_up);
        bill_comment_EditText = (EditText)findViewById(R.id.billComment_up);
        bill_cost_EditText = (EditText)findViewById(R.id.billCost_up);
        bill_consumer_EditText.addTextChangedListener(new Watcher(bill_consumer_EditText,"consumer"));
        bill_comment_EditText.addTextChangedListener(new Watcher(bill_comment_EditText,"comment"));
        bill_cost_EditText.addTextChangedListener(new Watcher(bill_cost_EditText,"cost"));
		
		db_helper = new DBHelper(this);
        db = db_helper.getWritableDatabase();
		
		Intent intent =getIntent();
		bill_id = intent.getIntExtra(EXTRA_UPDATE_ID, -1);
		int year_position = intent.getIntExtra(EXTRA_UPDATE_YEAR, -1);
		int month_position = intent.getIntExtra(EXTRA_UPDATE_MONTH, -1);
		int day_position = intent.getIntExtra(EXTRA_UPDATE_DAY, -1);
		origin_day_position = day_position;
		bill_table_name = intent.getStringExtra(EXTRA_UPDATE_TABLE_NAME);
			
		date_spinner = (DateSpinnerView)findViewById(R.id.id_bill_update_dateview);
        date_spinner.setIntialParams(new boolean[]{true, true, true},
				new int[]{year_position, month_position, day_position},
				new boolean[]{false, false, true}, null);
		
		update_data = new HashMap<String,Object>();
		Map<String,Object> bill_data = db_helper.get_bill_data(db, bill_table_name, bill_id);
		show_origin_bill(bill_data);
		update_data.clear();
	}
	
	private void show_origin_bill(Map<String,Object> bill_data) {
		bill_consumer_EditText.setText(bill_data.get("consumer").toString());
		bill_comment_EditText.setText(bill_data.get("comment").toString());
		bill_cost_EditText.setText(bill_data.get("cost").toString());
		bill_sid = bill_data.get("sid").toString();
	}
    
    private void really_update(String action) {
    	if (db_helper.update_bill(db, bill_table_name, bill_id, bill_sid, update_data)) {
    		update_data.clear();
    		VariousDialog.new_alert_dialog(this, "账单" + action, "成功!",
    				"确定", finish_it, null, null).show();
    	} else {
    		VariousDialog.new_alert_dialog(this, "账单" + action, "失败!\n请稍后重试",
    				"确定", finish_it, null, null).show();
    	}
    }
    
    public void updateBillSure(View view) {
    	int day_p = date_spinner.get_day_p();
    	if ( day_p != origin_day_position) {
    		update_data.put("day", date_spinner.get_day_s());
    		origin_day_position = day_p;
    	}
    	if (update_data.isEmpty()) {
    		VariousDialog.new_alert_dialog(this, "账单更新", "没有变化", "确定", null, null, null).show();
    		return;
    	}    	
    	really_update("更新");
    }
    
    public void disableBillSure(View view) {
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
