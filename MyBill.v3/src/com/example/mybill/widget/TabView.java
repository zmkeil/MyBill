package com.example.mybill.widget;

import java.util.Vector;

import com.example.mybill.R;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TabView extends RelativeLayout {

	private FragmentManager fm;
	private TabAdapter tab_adapter;
	private Vector<TabItem> tab_items;
	
	private GridView view_bottom_bar;
	private TextView view_content;
	
	public TabView(Context context, AttributeSet attrs) {  
        super(context, attrs);       
        
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.tab_view, this);          
        view_bottom_bar = (GridView)findViewById(R.id.id_tab_bottom_bar);
        //view_content = (TextView)findViewById(R.id.id_tab_content);
        
        tab_adapter = new TabAdapter(inflater, R.id.id_tab_content, R.layout.tab_item);
        tab_items = new Vector<TabItem>(10);
        tab_adapter.set_data(tab_items);
        view_bottom_bar.setAdapter(tab_adapter);
	}
	
	public boolean addItem(int ImgResId, String label, Fragment fragment, int itemId) {
		tab_items.addElement(new TabItem(ImgResId, label, fragment, itemId));
		return true;
	}
	
	public void notifyItemsChanged() {
        view_bottom_bar.setNumColumns(tab_adapter.getCount());
        tab_adapter.notifyDataSetChanged();
	}

	public void setFragmentManager(FragmentManager fm) {
		this.fm = fm;
		tab_adapter.setFragmentManager(fm);
	}

	public void set_selected_item_id(int item_id) {
		tab_adapter.set_selected_item_id(item_id);
	}
	
	public void select_new_item(int new_item_id) {
		tab_adapter.select_new_item(new_item_id);
	}
	
}
