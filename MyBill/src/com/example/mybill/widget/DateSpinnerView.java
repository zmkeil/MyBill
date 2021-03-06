package com.example.mybill.widget;

import com.example.mybill.util.PopularCallback;
import com.example.mybill.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
	private int day_position;
	final private String[] year_present = new String[]{"2015", "2016", "2017", "2018"};
	final private String[] month_present = new String[]{"01", "02", "03", "04",
			"05","06","07","08","09","10","11","12"};
	final private String[] day_present = new String[]{"01", "02", "03", "04",
			"05","06","07","08","09","10","11","12","13","14","15","16","17","18","19",
			"20","21","22","23","24","25","26","27","28","29","30","31"};
	final private String[] day_present_for_detail = new String[]{"支出", "收入", "他人支出", "他人收入"};
	private ArrayAdapter<String> year_spinner_adapter;
	private ArrayAdapter<String> month_spinner_adapter;
	private ArrayAdapter<String> day_spinner_adapter;
		
	private Context mcontext;
	
	public DateSpinnerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mcontext = context;
		
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
		 
		year_spinner_adapter = new ArrayAdapter<String>(context,
	        		android.R.layout.simple_spinner_item, year_present);
        year_spinner_adapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        year_p.setAdapter(year_spinner_adapter);
        month_spinner_adapter = new ArrayAdapter<String>(context,
        		android.R.layout.simple_spinner_item, month_present);
        month_spinner_adapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        month_p.setAdapter(month_spinner_adapter);
        day_spinner_adapter = new ArrayAdapter<String>(context,
	     		android.R.layout.simple_spinner_item, day_present);
	    day_spinner_adapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
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
	
	public void setDayToBillType(boolean is_pay) {
		AdapterView.OnItemSelectedListener listener = day_p.getOnItemSelectedListener();
		day_p.setOnItemSelectedListener(null);
		
		day_spinner_adapter = new ArrayAdapter<String>(mcontext,
	     		android.R.layout.simple_spinner_item, day_present_for_detail);
	    day_spinner_adapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
	    day_p.setAdapter(day_spinner_adapter);
	    day_position = is_pay ? 0 : 1;
	    day_p.setSelection(day_position, true);
	    
	    day_t.setText("");
	    
	    day_p.setVisibility(View.VISIBLE);
	    day_t.setVisibility(View.VISIBLE);
	    day_p.setOnItemSelectedListener(listener);
	}	

	public void setDayToBillType() {
		setDayToBillType(true);
	}
	
	public String get_year_s() {
		return year_spinner_adapter.getItem(year_position);
	}
	public String get_month_s() {
		return month_spinner_adapter.getItem(month_position);
	}
	public String get_day_s() {
		return day_spinner_adapter.getItem(day_position);
	}
	
	public int get_day() {
		return Integer.valueOf(get_day_s());
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

}
