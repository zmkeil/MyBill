package com.example.mybill;

import java.util.Calendar;

import com.example.mybill.util.DBHelper;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NewBillActivity extends Activity {
		
	private static final String BWORD="BILL";
	
	final public String EXTRA_INSERT_PAY_OR_EARN = "EXTRA_INSERT_PAY_OR_EARN";
	final static public String EXTRA_UPDATE_YEAR = "EXTRA_UPDATE_YEAR";
	final static public String EXTRA_UPDATE_MONTH = "EXTRA_UPDATE_MONTH";
	final static public String EXTRA_UPDATE_DAY = "EXTRA_UPDATE_DAY";
	
	private DBHelper db_helper;
	private SQLiteDatabase db;
	
	private DateSpinnerView date_spinner;
	
	private EditText bill_consumer_EditText;
	private EditText bill_comment_EditText;
	private EditText bill_cost_EditText;
	
	private Context _context;
	private boolean is_pay;
	private String role = "c";
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        _context = this;
        Intent intent =getIntent();
        is_pay = intent.getBooleanExtra(EXTRA_INSERT_PAY_OR_EARN, true);
        role = (is_pay ? "消费者" : "收入者");
        
        getActionBar().setTitle(is_pay ? "花钱啦" : "赚钱啦");
        // hide Home(logo and icon)
        getActionBar().setDisplayShowHomeEnabled(false);
        // up the Home
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // use a new icon
        getActionBar().setHomeAsUpIndicator(R.drawable.back);
                
        db_helper = new DBHelper(this);
        db = db_helper.getWritableDatabase();
        
        // create today's bill by default
        Calendar c = Calendar.getInstance();
        int year_position = c.get(Calendar.YEAR) - 2015;
        int month_position = c.get(Calendar.MONTH);
        int day_position = c.get(Calendar.DAY_OF_MONTH) - 1;
        
        date_spinner = (DateSpinnerView)findViewById(R.id.id_bill_record_dateview);
        date_spinner.setIntialParams(new boolean[]{true, true, true},
				new int[]{year_position, month_position, day_position},
				new boolean[]{true, true, true}, null);
        
        bill_consumer_EditText = (EditText)findViewById(R.id.billConsumer);
        bill_comment_EditText = (EditText)findViewById(R.id.billComment);
        bill_cost_EditText = (EditText)findViewById(R.id.billCost); 
        
        TextView tv_role = (TextView)findViewById(R.id.textConsumer);
        tv_role.setText(role);
    }
    
    public void newBillCreate(View view) {
    	Log.i(BWORD, "create a new bill");
    	String consumer_s = bill_consumer_EditText.getText().toString();
    	String comment_s = bill_comment_EditText.getText().toString();
    	String cost_s = bill_cost_EditText.getText().toString();
    	if (consumer_s.isEmpty() || comment_s.isEmpty() || cost_s.isEmpty()) {
    		VariousDialog.new_alert_dialog(this, "系统提示", "请填写完整信息",
    				"确定", null, null, null).show();
    	} else {
    		String message = "您确认创建新单子： \n" + "日期： "
            		+ date_spinner.get_year_s() + " 年 "
            		+ date_spinner.get_month_s() + " 月 "
            		+ date_spinner.get_day_s() + " 日\n" 
            		+ role + "：" + consumer_s + "\n"
            		+ "明细：" + comment_s + "\n"
            		+ "金额：" + cost_s;
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
    	String consumer_s = bill_consumer_EditText.getText().toString();
    	String comment_s = bill_comment_EditText.getText().toString();
    	String cost_s = bill_cost_EditText.getText().toString();
    	String gay = PreferenceManager.getDefaultSharedPreferences(this)
    			.getString("user_name", "noset").split(":")[0];
    	String bill_table_name = "bills_of_" + gay + "_" 
    			+ date_spinner.get_year_s() + "_" + date_spinner.get_month_s();
        if (!db_helper.insert_new_bill(db, bill_table_name/*date_spinner.get_current_relative_table()*/,
        		date_spinner.get_day(), is_pay ? 0 : 1, consumer_s, comment_s, Integer.valueOf(cost_s))) {
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
			bill_comment_EditText.setText("");
			bill_cost_EditText.setText("");
		}   	
    };

    private PopularCallback _skim = new PopularCallback() {
		@Override
		public void func(String context) {
			//Intent intent = new Intent(_context, HistoryBillActivity.class);
			Intent intent = new Intent();
			intent.putExtra(EXTRA_UPDATE_YEAR, date_spinner.get_year_p());
			intent.putExtra(EXTRA_UPDATE_MONTH, date_spinner.get_month_p());
			intent.putExtra(EXTRA_UPDATE_DAY, is_pay ? 0 : 1);

			// return MainActivity
			setResult(RESULT_OK, intent);  
            finish();  
		}
    };
}
