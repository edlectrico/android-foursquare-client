package com.foursquare.api.map;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.example.androidfoursquareclient.R;
import com.foursquare.api.oauth2.Credentials;
import com.foursquare.api.oauth2.store.CredentialStore;
import com.foursquare.api.oauth2.store.SharedPreferencesCredentialStore;
import com.foursquare.api.utils.Constants;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.Setting;
import fi.foyt.foursquare.api.io.DefaultIOHandler;

public class CheckinMap extends MapActivity {

	private ArrayList<OverlayItem> overlays;
	private FoursquareApi foursquareApi;

	private SharedPreferences sharedPreferences;

	private MapView mapView;
	private MapController mapController;

	private Drawable drawable;

	private String venueName 	= null;
	private String venueAddress = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkin_map);
		
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		this.drawable = this.getResources().getDrawable(R.drawable.pin);

		this.overlays = new ArrayList<OverlayItem>();
		this.mapView = (MapView) findViewById(R.id.mapview);

		this.mapView.setBuiltInZoomControls(true);
		this.mapView.setVisibility(View.GONE);
		this.mapController = mapView.getController();

		HelloItemizedOverlay itemizedoverlay = new HelloItemizedOverlay(
				drawable);
		mapView.getOverlays().add(itemizedoverlay);

		if (getIntent().getExtras() != null) {
			final Double latitude 	= getIntent().getExtras().getDouble(Constants.PLACE_LAT_FIELD);
			final Double longitude 	= getIntent().getExtras().getDouble(Constants.PLACE_LNG_FIELD);
			venueAddress 			= getIntent().getExtras().getString(Constants.PLACE_ADDRESS_FIELD);
			venueName 				= getIntent().getExtras().getString(Constants.PLACE_NAME_FIELD);
			final GeoPoint geoPoint = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
			
			HelloItemizedOverlay venueOverlay = (HelloItemizedOverlay) mapView.getOverlays().get(0);
			
			final OverlayItem overlayitem = new OverlayItem(geoPoint, venueName, venueAddress);
			
			venueOverlay.addOverlay(overlayitem);
			mapController.animateTo(geoPoint);
			mapController.setCenter(geoPoint);
			mapController.setZoom(16);
		}
		new PerformApiCallTask().execute();
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	private class HelloItemizedOverlay extends ItemizedOverlay<OverlayItem> {
		public HelloItemizedOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
			populate();
		}

		public void addOverlay(OverlayItem overlay) {
			overlays.add(overlay);
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return overlays.get(i);
		}

		@Override
		public int size() {
			return overlays.size();
		}

		@Override
		public boolean onTap(GeoPoint p, MapView mapView) {
			HelloItemizedOverlay itemizedoverlay = (HelloItemizedOverlay) mapView
					.getOverlays().get(0);
			OverlayItem overlayitem = new OverlayItem(p, "Location at "
					+ "title", "snippet");
			itemizedoverlay.addOverlay(overlayitem);
			mapView.invalidate();
			showVenueList(p.getLatitudeE6() / 1E6,
					p.getLongitudeE6() / 1E6);
			return true;
		}
	}
	
	private class PerformApiCallTask extends AsyncTask<Uri, Void, Void> {

		private boolean apiCallSuccess = false;
		@Override
		protected Void doInBackground(Uri...params) {
			try {
				final Result<Setting> result = getFoursquareApi().settingsAll();
				
			    if (result.getMeta().getCode() == 200) {
			    	apiCallSuccess=true;
			    } else {
			    	apiCallSuccess=false;
			    	StringBuffer sb = new StringBuffer();
					sb.append("Error occured: ");
					sb.append("  code: " + result.getMeta().getCode());
					sb.append("  type: " + result.getMeta().getErrorType());
					sb.append("  detail: " + result.getMeta().getErrorDetail());
			    }

			} catch (Exception ex) {
				ex.printStackTrace();
			}
            return null;
		}

		@Override
		protected void onPreExecute() { }
		
		@Override
		protected void onPostExecute(Void result) {
			if (apiCallSuccess) {
				mapView.setVisibility(View.VISIBLE);
			} else {
				mapView.setVisibility(View.GONE);
			}
		}
	}
	
    private void showVenueList(double latitude,double longitude) {
		Intent intent = new Intent(this, VenueList.class);
		intent.putExtra(Constants.PLACE_LAT_FIELD, latitude);
		intent.putExtra(Constants.PLACE_LNG_FIELD, longitude);
		startActivityForResult(intent, Constants.NOTIFICATION_STATUS_OK);	
    }
    
	public static void writeToLogFile(File file, String text) {
		try {
			BufferedWriter buf = new BufferedWriter(new FileWriter(file, false));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FoursquareApi getFoursquareApi() {
		if (this.foursquareApi==null) {
			this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			CredentialStore credentialStore = new SharedPreferencesCredentialStore(sharedPreferences);
			
			AccessTokenResponse accessTokenResponse = credentialStore.read();
			this.foursquareApi = new FoursquareApi(Credentials.CLIENT_ID,
					Credentials.CLIENT_SECRET,
					Credentials.REDIRECT_URI,
					accessTokenResponse.accessToken, new DefaultIOHandler());
		}
		return this.foursquareApi;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
}