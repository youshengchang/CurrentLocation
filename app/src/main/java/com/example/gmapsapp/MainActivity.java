package com.example.gmapsapp;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

	private static final int GPS_ERRORDIALOG_REQUEST = 9001;
	GoogleMap mMap;

    private SharedPreferences store;
    private String mSearchHistory;
    private final String SEARCH_KEY = "key";

    private AutoCompleteTextView actv;

    private String[] places;
    Set<String> set;


	@SuppressWarnings("unused")
	private static final double SEATTLE_LAT = 47.60621,
	SEATTLE_LNG =-122.33207, 
	SYDNEY_LAT = -33.867487,
	SYDNEY_LNG = 151.20699, 
	NEWYORK_LAT = 40.714353, 
	NEWYORK_LNG = -74.005973;
	private static final float DEFAULTZOOM = 15;
	@SuppressWarnings("unused")
	private static final String LOGTAG = "Maps";
    //LocationClient mLocationClient;
    GoogleApiClient mGoogleApiClient;
	
	//Location mLocationClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (servicesOK()) {
			setContentView(R.layout.activity_map);

			if (initMap()) {
				Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
                buildGoogleApiClient();

			}
			else {
				Toast.makeText(this, "Map not available!", Toast.LENGTH_SHORT).show();
			}
		}
		else {
			setContentView(R.layout.activity_main);
		}
        store = getPreferences(MODE_PRIVATE);
        actv = (AutoCompleteTextView) findViewById(R.id.editText1);
        setupAutoList();

	}

    private void setupAutoList() {
        SharedPreferences.Editor edit = store.edit();
        set = store.getStringSet(SEARCH_KEY, null);
        if(set != null){
            Object[] objs = set.toArray();
            places = new String[objs.length];
            for(int i = 0; i < places.length;i++){
                places[i] = (String)objs[i];
            }
        }else{
            places = new String[]{""};
            set = new HashSet<String>();

        }

    }
    private void updateView() {
        ArrayAdapter adapter = new ArrayAdapter
                (this,android.R.layout.simple_list_item_1,places);
        actv.setAdapter(adapter);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean servicesOK() {
		int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if (isAvailable == ConnectionResult.SUCCESS) {
			return true;
		}
		else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
			dialog.show();
		}
		else {
			Toast.makeText(this, "Can't connect to Google Play services", Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	private boolean initMap() {
		if (mMap == null) {
			SupportMapFragment mapFrag =
					(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
			mMap = mapFrag.getMap();
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}
		return (mMap != null);
	}

	@SuppressWarnings("unused")
	private void gotoLocation(double lat, double lng) {
		LatLng ll = new LatLng(lat, lng);
		CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
		mMap.moveCamera(update);
	}

	private void gotoLocation(double lat, double lng,
			float zoom) {
		LatLng ll = new LatLng(lat, lng);
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
		mMap.moveCamera(update);
	}

    private void gotoCurrentLocation(){


        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);


        if(currentLocation == null){
            Toast.makeText(this, "The current location is not available", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "The current location is available: " + currentLocation.getLatitude() + " " + currentLocation.getLongitude() , Toast.LENGTH_SHORT).show();
            LatLng ll = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, DEFAULTZOOM);
            mMap.animateCamera(update);
        }

    }

	public void geoLocate(View v) throws IOException {
		hideSoftKeyboard(v);

//		EditText et = (EditText) findViewById(R.id.editText1);
//		String location = et.getText().toString();
//
//        mSearchHistory = location;
//        et.setText("");
//
//        SharedPreferences.Editor editor = store.edit();
//        editor.putString(SEARCH_KEY, mSearchHistory);
//        editor.commit();

        boolean duplicated = false;
        AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.editText1);
        String location = actv.getText().toString();
        Log.i("AUTOCOMPLETE:", "selected text = " + location);
        Toast.makeText(this, "Text is " + location, Toast.LENGTH_LONG).show();
        for(String place: set){
            if(location.trim().equalsIgnoreCase(place))
                duplicated = true;
        }
        if(!duplicated){

            set.add(location);
            Object[] obj = set.toArray();
            Iterator<String> it = set.iterator();
            Log.i("AUTO", "obj array length = " + obj.length);
            places = new String[obj.length];
            for(int i = 0; i< obj.length; i++){
                places[i] = (String)obj[i];
            }


            SharedPreferences.Editor edit = store.edit();
            edit.putStringSet(SEARCH_KEY, set);
            edit.commit();
            updateView();
        }

		Geocoder gc = new Geocoder(this);
		List<Address> list = gc.getFromLocationName(location, 1);
		Address add = list.get(0);
		String locality = add.getLocality();
		Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

		double lat = add.getLatitude();
		double lng = add.getLongitude();

		gotoLocation(lat, lng, DEFAULTZOOM);

	}

	private void hideSoftKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.mapTypeNone:
			mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
			break;
		case R.id.mapTypeNormal:
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
		case R.id.mapTypeSatellite:
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
		case R.id.mapTypeTerrain:
			mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
		case R.id.mapTypeHybrid:
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			break;
        case R.id.gotoCurrentLocation:
            gotoCurrentLocation();
            break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		MapStateManager mgr = new MapStateManager(this);
		mgr.saveMapState(mMap);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MapStateManager mgr = new MapStateManager(this);
		CameraPosition position = mgr.getSavedCameraPosition();
		if (position != null) {
			CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
			mMap.moveCamera(update);
//			This is part of the answer to the code challenge
			mMap.setMapType(mgr.getSavedMapType());
		}
        //mSearchHistory = store.getString(SEARCH_KEY, "no value");
        setupAutoList();
        updateView();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		Toast.makeText(this, "The connection service is available", Toast.LENGTH_SHORT).show();
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(60000);
        request.setFastestInterval(1000);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);

        gotoCurrentLocation();
	}

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onLocationChanged(Location location) {
        String msg = "Location: " + location.getLatitude() + ", " + location.getLongitude();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }



    @Override
    protected void onRestart() {
        super.onRestart();
        //mSearchHistory = store.getString(SEARCH_KEY,"no value");
        setupAutoList();
        updateView();
    }

    public void restoreHistory(View v){

        EditText et = (EditText) v.findViewById(R.id.editText1);
        if(!mSearchHistory.equalsIgnoreCase("no value"))
            et.setText(mSearchHistory);

    }


}
