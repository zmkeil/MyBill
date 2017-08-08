package com.example.mybill;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SettingActivity extends Activity {

	final private String TAG = "SettingActivity";
	private Context mcontext;
	
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragement()).commit();
        mcontext = this;
    }
	
    @Override
    protected void onResume() {
    	super.onResume();
    }
	
	public class PrefsFragement extends PreferenceFragment {  
        @Override  
        public void onCreate(Bundle savedInstanceState) {  
            // TODO Auto-generated method stub  
            super.onCreate(savedInstanceState);  
            addPreferencesFromResource(R.xml.setting);
                        
            EditTextPreference remote_address = (EditTextPreference) findPreference("remote_address");
            EditTextPreference user = (EditTextPreference) findPreference("user_name");
            EditTextPreference sharer1 = (EditTextPreference) findPreference("sharer_name1");
            EditTextPreference sharer2 = (EditTextPreference) findPreference("sharer_name2");
            EditTextPreference sharer3 = (EditTextPreference) findPreference("sharer_name3");
            String remote_address_s = remote_address.getText();
            String user_s = user.getText();
            String sharer1_s = sharer1.getText();
            String sharer2_s = sharer2.getText();
            String sharer3_s = sharer3.getText();
            remote_address.setSummary(remote_address_s); 
            user.setSummary(user_s);
            sharer1.setSummary(sharer1_s);
            sharer2.setSummary(sharer2_s);
            sharer3.setSummary(sharer3_s);

            OnPreferenceChangeListener change_listener = new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference,
						Object newValue) {
					preference.setSummary(((EditTextPreference)preference).getText());
					return true;
				}
            };
            remote_address.setOnPreferenceChangeListener(change_listener);
            user.setOnPreferenceChangeListener(change_listener);
            sharer1.setOnPreferenceChangeListener(change_listener);
            sharer2.setOnPreferenceChangeListener(change_listener);
            sharer3.setOnPreferenceChangeListener(change_listener);
        }  
    }
}
