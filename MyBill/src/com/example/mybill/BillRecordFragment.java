package com.example.mybill;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BillRecordFragment extends Fragment implements OnClickListener {

	final public String EXTRA_INSERT_PAY_OR_EARN = "EXTRA_INSERT_PAY_OR_EARN";
	final static public int NEW_BILL_REQUEST = 1;
	
	private Context mcontext;
	
	public BillRecordFragment(Context context) {
		this.mcontext = context;
	}
	
	@Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState)  
    {  
		RelativeLayout ly =  (RelativeLayout) inflater.inflate(R.layout.bill_record_fragment, container, false); 
        Button btn_pay = (Button) ly.findViewById(R.id.id_bill_record_pay);
        Button btn_earn = (Button) ly.findViewById(R.id.id_bill_record_earn);
        btn_pay.setOnClickListener(this);
        btn_earn.setOnClickListener(this);
        return ly;
    }

	@Override
	public void onClick(View v) {
		boolean is_pay = true;
		switch(v.getId()) {
		case R.id.id_bill_record_pay:
			is_pay = true;
			break;
		case R.id.id_bill_record_earn:
			is_pay = false;
			break;
		default:
			break;
		}
		
		Intent intent = new Intent(mcontext, NewBillActivity.class);
		intent.putExtra(EXTRA_INSERT_PAY_OR_EARN, is_pay);
		getActivity().startActivityForResult(intent, NEW_BILL_REQUEST);
	}
	
}
