package com.example.mybill.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class VariousDialog {

	public static AlertDialog new_alert_dialog(Context context, String title, String message,
			String positive_btn_text, final PopularCallback positive_callback,
			String negative_btn_text, final PopularCallback negative_callback) {
		AlertDialog.Builder dialog_builder = new AlertDialog.Builder(context).setTitle(title)
				.setMessage(message);
		if (positive_btn_text != null) {
			dialog_builder.setPositiveButton(positive_btn_text, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	if (positive_callback != null) {
	            		positive_callback.func(null);
	            	}
	                return;    
	            }     
	        });
		}
		if (negative_btn_text != null) {
			dialog_builder.setNegativeButton(negative_btn_text, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	if (negative_callback != null) {
	            		negative_callback.func(null);
	            	}
	                return;    
	            }     
	        });
		}
		return dialog_builder.create();
	}

}
