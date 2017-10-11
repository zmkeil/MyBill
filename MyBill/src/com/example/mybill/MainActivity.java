package com.example.mybill;

import com.example.mybill.NewBillActivity;
import com.example.mybill.BillDetailFragment;
import com.example.mybill.BillRecordFragment;
import com.example.mybill.ContentFragment;
import com.example.mybill.network.NetworkService;
import com.example.mybill.widget.TabView;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {
	
	final static public int ITEM_ID_BASE = 10000;
	final static public int RECORD_ITEM_ID = ITEM_ID_BASE + 1;
	final static public int DETAIL_ITEM_ID = ITEM_ID_BASE + 2;
	final static public int EVALUATE_ITEM_ID = ITEM_ID_BASE + 3;
	final static public int PROPERTY_ITEM_ID = ITEM_ID_BASE + 4;
	
	private TabView tab;
	private BillRecordFragment record_fg;
	private BillDetailFragment detail_fg;
	private EvaluateFragment evaluate_fg;
	private AssetFragment assets_fg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // set activity label
        getActionBar().setTitle("微账");
        // use icon instead of logo
        getActionBar().setDisplayUseLogoEnabled(false);        
        // display Home (logo and icon)
        getActionBar().setDisplayShowHomeEnabled(false);
        
        tab = (TabView)findViewById(R.id.id_main_tabview);        
        FragmentManager fm = getFragmentManager();
        tab.setFragmentManager(fm);
        
        record_fg = new BillRecordFragment(this);
        detail_fg = new BillDetailFragment(this);
        evaluate_fg = new EvaluateFragment(this);
        assets_fg = new AssetFragment(this);

        tab.addItem(R.drawable.bill_record, "随手记", record_fg, RECORD_ITEM_ID);
        tab.addItem(R.drawable.bill_detail, "账单明细", detail_fg, DETAIL_ITEM_ID);
        tab.addItem(R.drawable.bill_evaluation, "月度评估", evaluate_fg, EVALUATE_ITEM_ID);
        tab.addItem(R.drawable.bill_storage, "资产总览", assets_fg, PROPERTY_ITEM_ID);
        tab.set_selected_item_id(DETAIL_ITEM_ID);
        Log.i("Main", "onCreate, before tab selected");
        tab.notifyItemsChanged();
        
        Log.i("Main", "start service");
      	Intent intent = new Intent(this, NetworkService.class);
      	startService(intent);
    }
    
    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) { 
        super.onActivityResult(requestCode, resultCode, intent);  

        Log.i("MAIN", "onActivityResult");
        //requestCode标示请求的标示, resultCode表示有数据  
        if (requestCode == BillRecordFragment.NEW_BILL_REQUEST && resultCode == RESULT_OK) {
        	int year_position = intent.getIntExtra(NewBillActivity.EXTRA_UPDATE_YEAR, 1);
        	int month_position = intent.getIntExtra(NewBillActivity.EXTRA_UPDATE_MONTH, 1);
        	int day_position = intent.getIntExtra(NewBillActivity.EXTRA_UPDATE_DAY, 0);
        	Log.i("MAIN", "onActivityResult before select new");
        	tab.select_new_item(DETAIL_ITEM_ID);
        	detail_fg.select_year(year_position);
        	detail_fg.select_month(month_position);
        	detail_fg.select_day(day_position);
        }  
        // other requestCode
        if (requestCode == BillRecordFragment.NEW_ASSETS_REQUEST && resultCode == RESULT_OK) {
        	int year_position = intent.getIntExtra(NewBillActivity.EXTRA_UPDATE_YEAR, 1);
        	int month_position = intent.getIntExtra(NewBillActivity.EXTRA_UPDATE_MONTH, 1);
        	tab.select_new_item(PROPERTY_ITEM_ID);
        	assets_fg.select_year(year_position);
        	assets_fg.select_month(month_position);
        }
    }  


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	int id = item.getItemId();
        switch (id) {
        case R.id.action_settings:
        	Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        	this.startActivity(intent);
        	break;
        default:
        	return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
