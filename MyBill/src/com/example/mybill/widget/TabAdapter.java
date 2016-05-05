package com.example.mybill.widget;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.example.mybill.R;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TabAdapter extends BaseAdapter {

    private final LayoutInflater mLayoutInflater;
    private final int mContentFragmentId;
    private final int mItemResId;
    
    private final int item_selected_title_color = Color.rgb(136, 205, 170);
    private final int item_noselected_title_color = Color.rgb(0, 0, 0);
        
    private Vector<TabItem> tab_items;
    Map<Integer, View> tab_items_map;

    private int selected_item_id = View.NO_ID;
    //private View selected_item;
    
    private FragmentManager fm;
    

    public TabAdapter(LayoutInflater inflater, int fragmentId, int itemResId) {
        mLayoutInflater = inflater;
        mContentFragmentId = fragmentId;
        mItemResId = itemResId;
        
        tab_items_map = new HashMap<Integer, View>();
    }   

	public void set_data(Vector<TabItem> tab_items) {
		this.tab_items = tab_items;
	}

	public void setFragmentManager(FragmentManager fm) {
		this.fm = fm;
	}

	@Override
	public int getCount() {
		return tab_items.size();
	}

	@Override
	public TabItem getItem(int position) {
		return tab_items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return tab_items.get(position).itemId;
	}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	//Log.i("TAB", "getview " + position);
        final View view;
        final ViewHolder holder;
        if (null == convertView) {
            view = mLayoutInflater.inflate(mItemResId, parent, false);
            if (view == null) {
            	return null;
            }
            // set its ID
            if (parent.getChildCount() == position) {
            	//Log.i("TAB", "child count " + position);
                view.setId(getItem(position).itemId);
            	tab_items_map.remove(view.getId());
            	tab_items_map.put(view.getId(), view);
            }
            // view holder
            holder = new ViewHolder();
            holder.image = (ImageView) view.findViewById(R.id.image);
            holder.title = (TextView) view.findViewById(R.id.title);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        bindView(holder, position);
        view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				select_new_item(v.getId());
			}        	
        });
        
        // if selected_item_id == NO_ID, use first item by default
        // NOTE!!! position ZERO will be drew many times(most in front and most at last) for test
        if ((selected_item_id == View.NO_ID) 
        		&& (position == 0) 
        		&& (parent.getChildCount() == position)) {
        	Log.i("TAB", "get the default selected item_id");
        	selected_item_id = view.getId();
        }
        if (position == (getCount() - 1)) {
        	set_selected_item(selected_item_id, null);
        }
        return view;
    }

	private void bindView(ViewHolder holder, int position) {
    	// draw the item
        TabItem item = getItem(position);
        holder.image.setImageResource(item.ImgResId);
        holder.title.setText(item.label);
        // bind item-material to the holder
        holder.position = position;
    }	


	public void set_selected_item_id(int item_id) {
		selected_item_id = item_id;
	}

    public void set_selected_item(int new_id, Fragment old_fg) {
    	// set new selected item color
    	selected_item_id = new_id;
    	View selected_item = tab_items_map.get(new_id);
    	Log.i("TAB", "new selected id: " + new_id);
    	selected_item.setBackgroundResource(R.drawable.buttom_bar_item_select_bg);
    	ViewHolder holder = (ViewHolder) selected_item.getTag();
		holder.title.setTextColor(item_selected_title_color);
		// set new selected fragment
		FragmentTransaction transaction = fm.beginTransaction();
		if (old_fg != null) {
			transaction.hide(old_fg);
		}
        Fragment fg = getItem(holder.position).fragment;
        if (!fg.isAdded()) {
        	transaction.add(mContentFragmentId, fg);
        } else {
        	transaction.show(fg);
        }
        //transaction.replace(mContentFragmentId, fg);
        transaction.commit();
	}

	public void select_new_item(int new_id) {
		//Log.i("TAB", "old select id: " + selected_item_id);
		// reset old selected item color
		View selected_item = tab_items_map.get(selected_item_id);
		selected_item.setBackgroundResource(0);
		ViewHolder holder = (ViewHolder)selected_item.getTag();
		holder.title.setTextColor(item_noselected_title_color);
		// set new select_view
		Fragment old_fg = getItem(holder.position).fragment;
		set_selected_item(new_id, old_fg);
	}	

    public static class ViewHolder {
        public ImageView image;
        public TextView title;
        public int position;
    }

}
