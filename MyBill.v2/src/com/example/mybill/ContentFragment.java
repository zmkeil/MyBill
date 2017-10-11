package com.example.mybill;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContentFragment extends Fragment {
	
	private final String msg;
	
	public ContentFragment(String msg) {
		this.msg = msg;
	}

    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState)  
    {  
    	LinearLayout ly =  (LinearLayout) inflater.inflate(R.layout.fragment_content, container, false); 
        TextView tv = (TextView) ly.findViewById(R.id.msg);
        tv.setText(msg);
        return ly;
    } 
    
}
