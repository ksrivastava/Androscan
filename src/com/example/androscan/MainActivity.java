package com.example.androscan;

import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends ApplicationActivity {
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		new Handler().postDelayed(new Runnable() { 
			   public void run() { 
			     openOptionsMenu(); 
			   } 
			}, 100); 
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	
}