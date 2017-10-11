package com.example.mybill.widget;

import android.app.Fragment;

public class TabItem {

	public int ImgResId;
	public String label;
	public Fragment fragment;
	public int itemId;
	
	TabItem(int ImgResId, String label, Fragment fragment, int itemId) {
		this.ImgResId = ImgResId;
		this.label = label;
		this.fragment = fragment;
		this.itemId = itemId;
	}
}
