package com.foursquare.api.map;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidfoursquareclient.R;
import com.foursquare.api.oauth2.Credentials;
import com.foursquare.api.oauth2.store.CredentialStore;
import com.foursquare.api.oauth2.store.SharedPreferencesCredentialStore;
import com.foursquare.api.utils.Constants;
import com.google.api.client.auth.oauth2.draft10.AccessProtectedResource;
import com.google.api.client.auth.oauth2.draft10.AccessProtectedResource.Method;
import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.Checkin;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;
import fi.foyt.foursquare.api.io.DefaultIOHandler;

public class VenueList extends ListActivity {

	private FoursquareApi foursquareApi;
	private List<CompactVenue> venuesMap;
	private SharedPreferences sharedPreferences;
	private CredentialStore credentialStore;

	private double latitude;
	private double longitude;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.places_list);

		this.venuesMap 			= new ArrayList<CompactVenue>();
		this.sharedPreferences 	= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		this.credentialStore 	= new SharedPreferencesCredentialStore(sharedPreferences);

		if (getIntent().getExtras() != null) {
			latitude 	= getIntent().getExtras().getDouble(Constants.PLACE_LAT_FIELD);
			longitude 	= getIntent().getExtras().getDouble(Constants.PLACE_LNG_FIELD);
		}

		getListView().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						final CompactVenue venue = venuesMap.get((int) id);
						new CheckinTask(venue).execute();
					}
				});

		new PlacesListRefresher().execute();
	}

	public FoursquareApi getFoursquareApi() {
		if (this.foursquareApi == null) {
			AccessTokenResponse accessTokenResponse = credentialStore.read();
			this.foursquareApi = new FoursquareApi(Credentials.CLIENT_ID,
					Credentials.CLIENT_SECRET, Credentials.REDIRECT_URI,
					accessTokenResponse.accessToken, new DefaultIOHandler());
		}
		return this.foursquareApi;
	}

	private class PlacesListRefresher extends AsyncTask<Uri, Void, Void> {
		@Override
		protected Void doInBackground(Uri... params) {
			try {
				Log.i(Constants.TAG, "Retrieving places at " + latitude + "," + longitude);
				final Result<VenuesSearchResult> venues = getFoursquareApi().venuesSearch(
						latitude + "," + longitude, null, null, null, null, null, null, null, null, null, null);
				final CompactVenue[] compactVenues = venues.getResult().getVenues();

				Log.i(Constants.TAG, "found " + compactVenues.length + " places");

				for (CompactVenue compactVenue : compactVenues) {
					venuesMap.add(compactVenue);
				}
			} catch (Exception ex) {
				Log.e(Constants.TAG, "Error retrieving venues", ex);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			setListAdapter(new FoursquareTableAdapter(venuesMap));
		}
	}

	protected HttpRequestFactory createApiRequestFactory(
			HttpTransport transport, String accessToken) {
		
		return transport.createRequestFactory(new AccessProtectedResource(
				accessToken, Method.AUTHORIZATION_HEADER) {
			
			protected void onAccessToken(String accessToken) { };
		});
	}

	public void performFoursquareApiCallUsingGoogleApiJavaClient()
			throws Exception {
		final AccessTokenResponse accessTokenResponse 	= credentialStore.read();
		final HttpTransport transport 					= new NetHttpTransport();
		GenericUrl genericUrl 							= new GenericUrl(Constants.FOURSQUARE_API_ENDPOINT);

		genericUrl.put("ll", latitude + "," + longitude);

		HttpRequest httpRequest = createApiRequestFactory(transport,
				accessTokenResponse.accessToken).buildGetRequest(genericUrl);

		HttpResponse httpResponse = httpRequest.execute();

		final JSONObject object 			= new JSONObject(httpResponse.parseAsString());
		final JSONObject fourSquareResponse = (JSONObject) object.get("response");
		final JSONArray groups 				= (JSONArray) fourSquareResponse.get("groups");
		final JSONObject group 				= (JSONObject) groups.get(0);
		final JSONArray items 				= (JSONArray) group.get("items");

		Log.i(Constants.TAG, "Found venues " + items);

		httpRequest = createApiRequestFactory(transport,
				accessTokenResponse.accessToken).buildGetRequest(genericUrl);

		final JsonHttpParser parser = new JsonHttpParser();
		parser.jsonFactory = new JacksonFactory();
		httpRequest.addParser(parser);

		httpResponse = httpRequest.execute();
	}

	private CompactVenue getVenueMapFromAdapter(int position) {
		return (((FoursquareTableAdapter) getListAdapter()).getItem(position));
	}

	class FoursquareTableAdapter extends ArrayAdapter<CompactVenue> {
		FoursquareTableAdapter(List<CompactVenue> list) {
			super(VenueList.this, R.layout.places_list_row, list);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView 			= getLayoutInflater().inflate(R.layout.places_list_row, parent, false);
				holder 					= new ViewHolder();
				holder.txtPlaceName 	= (TextView) convertView.findViewById(R.id.row_placename);
				holder.txtPlaceAddress	= (TextView) convertView.findViewById(R.id.row_placeaddress);
				holder.layout 			= (RelativeLayout) convertView.findViewById(R.id.row_layout);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final CompactVenue venue = getVenueMapFromAdapter(position);

			try {
				holder.txtPlaceName.setText(venue.getName());
				if (venue.getLocation().getAddress() != null
						&& venue.getLocation().getAddress().length() > 0) {
					holder.txtPlaceAddress.setText(venue.getLocation().getAddress());
				} else {
					holder.txtPlaceAddress.setText(R.string.no_address_info_found);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return (convertView);
		}
	}

	static class ViewHolder {
		TextView txtPlaceName;
		TextView txtPlaceAddress;
		RadioButton radio;
		RelativeLayout layout;
	}

	private class CheckinTask extends AsyncTask<Uri, Void, Void> {

		private String apiStatusMsg;
		private CompactVenue venue;

		public CheckinTask(CompactVenue venue) {
			this.venue = venue;
		}

		@Override
		protected Void doInBackground(Uri...params) {
			try {
				final Result<Checkin> result = getFoursquareApi().checkinsAdd(venue.getId(), null, 
						null, null, null, null, null, null);

				if (result.getMeta().getCode()==200) {
					apiStatusMsg = "Checked in to " + venue.getName();
				} else {
					apiStatusMsg = result.getMeta().getErrorDetail();
				}
			} catch (FoursquareApiException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPreExecute() { }

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(VenueList.this, apiStatusMsg, Toast.LENGTH_LONG).show();
			finish();
		}
	}
}
