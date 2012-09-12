package com.foursquare.api.oauth2;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.foursquare.api.map.CheckinMap;
import com.foursquare.api.oauth2.store.CredentialStore;
import com.foursquare.api.oauth2.store.SharedPreferencesCredentialStore;
import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest.AuthorizationCodeGrant;
import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
import com.google.api.client.auth.oauth2.draft10.AuthorizationRequestUrl;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

import com.example.androidfoursquareclient.Example;

@SuppressLint("SetJavaScriptEnabled")
public class AccessTokenActivity extends Activity {

	final String TAG = getClass().getName();
	private SharedPreferences sharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Retrienving request token.");
		this.sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		WebView webview = new WebView(this);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setVisibility(View.VISIBLE);
		setContentView(webview);

		webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap bitmap) {
				System.out.println("onPageStarted : " + url);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (url.startsWith(Credentials.REDIRECT_URI)) {
					try {
						if (url.indexOf("code=") != -1) {
							final String code = extractCodeFromUrl(url);

							AuthorizationCodeGrant request = new AuthorizationCodeGrant(
									new NetHttpTransport(),
									new JacksonFactory(),
									Credentials.ACCESS_TOKEN_URL,
									Credentials.CLIENT_ID,
									Credentials.CLIENT_SECRET, code,
									Credentials.REDIRECT_URI);

							AccessTokenResponse accessTokenResponse = request.execute();

							CredentialStore credentialStore = new SharedPreferencesCredentialStore(sharedPreferences);
							credentialStore.write(accessTokenResponse);
							
							view.setVisibility(View.INVISIBLE);
							startActivity(new Intent(AccessTokenActivity.this,CheckinMap.class));
						} else if (url.indexOf("error=") != -1) {
							view.setVisibility(View.INVISIBLE);
							new SharedPreferencesCredentialStore(sharedPreferences).clearCredentials();
							startActivity(new Intent(AccessTokenActivity.this, Example.class));
						}
					} catch (HttpResponseException e) {
						try {
							System.out.println("Error occured " + e.response.parseAsString());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				System.out.println("onPageFinished : " + url);
			}

			private String extractCodeFromUrl(String url) {
				return url.substring(Credentials.REDIRECT_URI.length() + 6,
						url.length());
			}
		});

		final AuthorizationRequestUrl authorizationRequestUrl = new AuthorizationRequestUrl(Credentials.AUTHORIZATION_URL);
		authorizationRequestUrl.clientId 	= Credentials.CLIENT_ID;
		authorizationRequestUrl.redirectUri = Credentials.REDIRECT_URI;
		webview.loadUrl(authorizationRequestUrl.build());
	}
}

