package com.foursquare.api.oauth2.store;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.foursquare.api.utils.Constants;
import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;

public class SharedPreferencesCredentialStore implements CredentialStore {

	private SharedPreferences preferences;
	
	public SharedPreferencesCredentialStore(SharedPreferences prefs) {
		this.preferences = prefs;
	}
	
	@Override
	public AccessTokenResponse read() {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
			accessTokenResponse.accessToken 	= preferences.getString(Constants.ACCESS_TOKEN, "");
			accessTokenResponse.expiresIn 		= preferences.getLong(Constants.EXPIRES_IN, 0);
			accessTokenResponse.refreshToken 	= preferences.getString(Constants.REFRESH_TOKEN, "");
			accessTokenResponse.scope 			= preferences.getString(Constants.SCOPE, "");
		
			return accessTokenResponse;
	}

	@Override
	public void write(AccessTokenResponse accessTokenResponse) {
		Editor editor = preferences.edit();
		if (accessTokenResponse.accessToken != null) 
			editor.putString(Constants.ACCESS_TOKEN,accessTokenResponse.accessToken);
		if (accessTokenResponse.expiresIn != null) 
			editor.putLong(Constants.EXPIRES_IN,accessTokenResponse.expiresIn);
		if (accessTokenResponse.refreshToken != null) 
			editor.putString(Constants.REFRESH_TOKEN,accessTokenResponse.refreshToken);
		if (accessTokenResponse.scope != null) 
			editor.putString(Constants.SCOPE,accessTokenResponse.scope);
		
		editor.commit();
	}
	
	@Override
	public void clearCredentials() {
		Editor editor = preferences.edit();
		editor.remove(Constants.ACCESS_TOKEN);
		editor.remove(Constants.EXPIRES_IN);
		editor.remove(Constants.REFRESH_TOKEN);
		editor.remove(Constants.SCOPE);
		editor.commit();
	}
}
