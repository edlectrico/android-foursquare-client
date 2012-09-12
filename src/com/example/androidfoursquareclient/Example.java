package com.example.androidfoursquareclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.foursquare.api.oauth2.AccessTokenActivity;

public class Example extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_example);
	    
	    final Button login = (Button) findViewById(R.id.foursquare_oauth_login);
	    login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent().setClass(v.getContext(), AccessTokenActivity.class));
				finish();
			}
		});
	}
}
