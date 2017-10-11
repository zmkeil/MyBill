package com.example.mybill.util;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter implements SpinnerAdapter {

		private Context context ;
		private List<AdaptorContent> list;
		
		public MyAdapter(Context context, List<AdaptorContent> list) {
			this.context = context;
			this.list = list;
		}
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, null); 
			TextView tvgetView = (TextView) view.findViewById(android.R.id.text1);
			tvgetView.setText(((AdaptorContent)getItem(position)).display_text); 
			return view;
		}
		
		@Override  
	    public View getDropDownView(int position, View convertView, ViewGroup parent) {  
	        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_activated_1, null);  
	        TextView tvdropdowview=(TextView) view.findViewById(android.R.id.text1);  
	        tvdropdowview.setText(((AdaptorContent)getItem(position)).display_text);  
	        return view;  
	    }
	
}
