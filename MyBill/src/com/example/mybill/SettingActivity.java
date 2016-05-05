package com.example.mybill;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

public class SettingActivity extends Activity {

	final private String TAG = "SettingActivity";
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragement()).commit();
    }
	
    @Override
    protected void onResume() {
    	super.onResume();
    }
	
	public static class PrefsFragement extends PreferenceFragment {  
        @Override  
        public void onCreate(Bundle savedInstanceState) {  
            // TODO Auto-generated method stub  
            super.onCreate(savedInstanceState);  
            addPreferencesFromResource(R.xml.setting);  
        }  
    }
	
}
