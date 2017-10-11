package com.example.mybill.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.example.mybill.util.AdaptorContent;
import com.example.mybill.util.AssetsUtil;
import com.example.mybill.util.MyAdapter;
import com.example.mybill.util.PopularCallback;
import com.example.mybill.R;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class DateSpinnerView extends LinearLayout {
	
	final private String TAG = "DateSpinner";
	
	TextView year_t;
	TextView month_t;
	TextView day_t;
	Spinner year_p;
	Spinner month_p;
	Spinner day_p;
	
	private int year_position = 100;
	private int month_position = 100;
	private int day_position = 0;
	

	

	private MyAdapter year_spinner_adapter;
	private MyAdapter month_spinner_adapter;
	private MyAdapter day_spinner_adapter;
	private MyAdapter gay_spinner_adapter;
	private MyAdapter assets_spinner_adapter;
		
	private Context mcontext;
	
	public DateSpinnerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mcontext = context;
		
		List<AdaptorContent> year_content_list = new ArrayList<AdaptorContent>();
		year_content_list.add(new AdaptorContent("2015", ""));
		year_content_list.add(new AdaptorContent("2016", ""));
		year_content_list.add(new AdaptorContent("2017", ""));
		year_content_list.add(new AdaptorContent("2018", ""));
		year_spinner_adapter = new MyAdapter(context, year_content_list);

		List<AdaptorContent> month_content_list = new ArrayList<AdaptorContent>();
		month_content_list.add(new AdaptorContent("01", ""));
		month_content_list.add(new AdaptorContent("02", ""));
		month_content_list.add(new AdaptorContent("03", ""));
		month_content_list.add(new AdaptorContent("04", ""));
		month_content_list.add(new AdaptorContent("05", ""));
		month_content_list.add(new AdaptorContent("06", ""));
		month_content_list.add(new AdaptorContent("07", ""));
		month_content_list.add(new AdaptorContent("08", ""));
		month_content_list.add(new AdaptorContent("09", ""));
		month_content_list.add(new AdaptorContent("10", ""));
		month_content_list.add(new AdaptorContent("11", ""));
		month_content_list.add(new AdaptorContent("12", ""));
		month_spinner_adapter = new MyAdapter(context, month_content_list);
		
		List<AdaptorContent> day_content_list = new ArrayList<AdaptorContent>();
		day_content_list.add(new AdaptorContent("01", ""));
		day_content_list.add(new AdaptorContent("02", ""));
		day_content_list.add(new AdaptorContent("03", ""));
		day_content_list.add(new AdaptorContent("04", ""));
		day_content_list.add(new AdaptorContent("05", ""));
		day_content_list.add(new AdaptorContent("06", ""));
		day_content_list.add(new AdaptorContent("07", ""));
		day_content_list.add(new AdaptorContent("08", ""));
		day_content_list.add(new AdaptorContent("09", ""));
		day_content_list.add(new AdaptorContent("10", ""));
		day_content_list.add(new AdaptorContent("11", ""));
		day_content_list.add(new AdaptorContent("12", ""));
		day_content_list.add(new AdaptorContent("13", ""));
		day_content_list.add(new AdaptorContent("14", ""));
		day_content_list.add(new AdaptorContent("15", ""));
		day_content_list.add(new AdaptorContent("16", ""));
		day_content_list.add(new AdaptorContent("17", ""));
		day_content_list.add(new AdaptorContent("18", ""));
		day_content_list.add(new AdaptorContent("19", ""));
		day_content_list.add(new AdaptorContent("20", ""));
		day_content_list.add(new AdaptorContent("21", ""));
		day_content_list.add(new AdaptorContent("22", ""));
		day_content_list.add(new AdaptorContent("23", ""));
		day_content_list.add(new AdaptorContent("24", ""));
		day_content_list.add(new AdaptorContent("25", ""));
		day_content_list.add(new AdaptorContent("26", ""));
		day_content_list.add(new AdaptorContent("27", ""));
		day_content_list.add(new AdaptorContent("28", ""));
		day_content_list.add(new AdaptorContent("29", ""));
		day_content_list.add(new AdaptorContent("30", ""));
		day_content_list.add(new AdaptorContent("31", ""));
		day_spinner_adapter = new MyAdapter(context, day_content_list);
		
		
		LayoutInflater inflater = LayoutInflater.from(context);
		//LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.date_spinner_view, this);
        
		 year_t = (TextView) findViewById(R.id.id_year_text);
		 month_t = (TextView) findViewById(R.id.id_month_text);
		 day_t = (TextView) findViewById(R.id.id_day_text);
		 
		 year_t.setText("年");
		 month_t.setText("月");
		 day_t.setText("日");
		 
		 //year_p = new Spinner(context);
		 year_p = (Spinner)findViewById(R.id.id_year_spinner);
		 month_p = (Spinner)findViewById(R.id.id_month_spinner);
		 day_p = (Spinner)findViewById(R.id.id_day_spinner);
		 
        year_p.setAdapter(year_spinner_adapter);
        month_p.setAdapter(month_spinner_adapter);
	    day_p.setAdapter(day_spinner_adapter);	    
	}
	
	public void setIntialParams(boolean[] visible, int[] positions, boolean[] selectable,
			final PopularCallback select_callback/*every selection causes this*/) {
		
		AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
	         @Override
	         public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3)
	         {
	        	  String from_context = "c";
	        	  switch(arg0.getId()) {
	        	  case R.id.id_year_spinner:
	        		  year_position = position;
	        		  from_context = "year-select";
	        		  break;
	        	  case R.id.id_month_spinner:
	        		  month_position = position;
	        		  from_context = "month-select";
	        		  break;
	        	  case R.id.id_day_spinner:
		         	  day_position = position;
		         	 from_context = "day-select";
		         	  break;
		          default:
		        	  break;
	        	  }
	         	  if (select_callback != null) {
	         		  select_callback.func(from_context);
	         	  }
	         }           
	         @Override
	         public void onNothingSelected(AdapterView<?> arg0)
	         {
	         }
	    };
	    
		year_position = positions[0];
        month_position = positions[1];
        day_position = positions[2];
        
        year_p.setSelection(year_position, true);
        month_p.setSelection(month_position, true);
        day_p.setSelection(day_position, true);

        year_p.setEnabled(selectable[0]);
        month_p.setEnabled(selectable[1]);
   	 	day_p.setEnabled(selectable[2]);

        year_p.setOnItemSelectedListener(listener);
        month_p.setOnItemSelectedListener(listener);        
	    day_p.setOnItemSelectedListener(listener);
	    
    	if (!visible[2]) {
        	day_t.setVisibility(View.GONE);
        	day_p.setVisibility(View.GONE);
        }
        
        //LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
		//		LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		//param1.setMargins(0, 0, 40, 0);
		//addView(year_p);
		//addView(year_t, param1);		
	}
	
	public void setDayToBillType() {
		int bill_num = 0;
		AdapterView.OnItemSelectedListener listener = day_p.getOnItemSelectedListener();
		day_p.setOnItemSelectedListener(null);
		
		List<AdaptorContent> gay_content_list = new ArrayList<AdaptorContent>();
    	String user_s = PreferenceManager.getDefaultSharedPreferences(this.mcontext).getString("user_name", "jxj:金");
    	String[] user_sa = user_s.split(":");
    	if (user_sa.length == 2) {
    		gay_content_list.add(new AdaptorContent(user_sa[1] + "支出", user_sa[0]));
    		gay_content_list.add(new AdaptorContent(user_sa[1] + "收入", user_sa[0]));
    		bill_num += 2;
    	}		
		for (int i = 1; i < 4; i++) {
			String sharer_s = PreferenceManager.getDefaultSharedPreferences(this.mcontext).getString("sharer_name" + i, "");
			String[] sharer_sa = sharer_s.split(":");
			if (sharer_sa.length == 2) {
				gay_content_list.add(new AdaptorContent(sharer_sa[1] + "支出", sharer_sa[0]));
				gay_content_list.add(new AdaptorContent(sharer_sa[1] + "收入", sharer_sa[0]));
				bill_num += 2;
			}
		}
		gay_spinner_adapter = new MyAdapter(mcontext, gay_content_list);		
	    day_p.setAdapter(gay_spinner_adapter);
	    
	    day_position = (day_position > (bill_num - 1)) ? (bill_num - 1) : day_position;
	    day_p.setSelection(day_position, true);
	    
	    day_t.setText("");	    
	    day_p.setVisibility(View.VISIBLE);
	    day_t.setVisibility(View.VISIBLE);
	    day_p.setOnItemSelectedListener(listener);
	}	
	
	public String get_year_s() {
		return ((AdaptorContent)year_spinner_adapter.getItem(year_position)).display_text;
	}
	public String get_month_s() {
		return ((AdaptorContent)month_spinner_adapter.getItem(month_position)).display_text;
	}
	public String get_day_s() {
		return ((AdaptorContent)day_spinner_adapter.getItem(day_position)).display_text;
	}	
	
	public String get_gay_s() {
		return ((AdaptorContent)gay_spinner_adapter.getItem(day_position)).ext_text;
	}
		
	public int get_day() {
		return Integer.valueOf(get_day_s());
	}
	
	public int get_month() {
		return Integer.valueOf(get_month_s());
	}
	
	public int get_year() {
		return Integer.valueOf(get_year_s());
	}

	public int get_year_p() {
		return year_position;
	}
	
	public int get_month_p() {
		return month_position;
	}
	
	public int get_day_p() {
		return day_position;
	}

	public void select_year(int year_position) {
		this.year_position = year_position;
		year_p.setSelection(year_position, true);
	}

	public void select_month(int month_position) {
		this.month_position = month_position;
		month_p.setSelection(month_position, true);
	}

	public void select_day(int day_position) {
		this.day_position = day_position;
		day_p.setSelection(day_position, true);
	}

	
	public void setDayToAssetsType() {
		int bill_num = AssetsUtil.getInstance().assets_item_withall_list.size();
		AdapterView.OnItemSelectedListener listener = day_p.getOnItemSelectedListener();
		day_p.setOnItemSelectedListener(null);
		
		assets_spinner_adapter = new MyAdapter(mcontext, AssetsUtil.getInstance().assets_item_withall_list);		
	    day_p.setAdapter(assets_spinner_adapter);
	    
	    day_position = (day_position > (bill_num - 1)) ? (bill_num - 1) : day_position;
	    day_p.setSelection(day_position, true);
	    
	    day_t.setText("");	    
	    day_p.setVisibility(View.VISIBLE);
	    day_t.setVisibility(View.VISIBLE);
	    day_p.setOnItemSelectedListener(listener);
	}
	
	public String get_assets_s() {
		return ((AdaptorContent)assets_spinner_adapter.getItem(day_position)).ext_text;
	}

}
