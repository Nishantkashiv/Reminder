package com.example.abhilash.reminder;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.text.DateFormat;
import java.util.Date;

public class AddLocation extends AppCompatActivity implements
        LocationListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
    private static final String TAG = "LocationActivity";
    GoogleApiClient googleApiClient;
    GoogleMap gmap = null;
    boolean flag = true;
    LocationRequest mLocationRequest;
    Location mCurrentLocation = null;
    String mLastUpdateTime;
    public static double lat, lng;
    public static String address;
    boolean gotLoc = false;
    private PlaceAutocompleteAdapter mAdapter;
    private boolean mapReady = false;
    private AutoCompleteTextView mAutocompleteView;
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                //Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            LatLng srchRes = place.getLatLng();
            lat = srchRes.latitude;
            lng = srchRes.longitude;

            address = (String) place.getAddress();
            Log.e("Address",address+"yaha select kiya");
            gotLoc = true;
            CameraPosition target = CameraPosition.builder().target(srchRes).zoom(17).build();
            gmap.animateCamera(CameraUpdateFactory.newCameraPosition(target), 3000, null);
            // Format details of the place for display and show it in a TextView.
            /*mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
                    place.getId(), place.getAddress(), place.getPhoneNumber(),
                    place.getWebsiteUri()));
*/
            // Display the third party attributions if set.
            final CharSequence thirdPartyAttribution = places.getAttributions();
            if (thirdPartyAttribution == null) {
                //mPlaceDetailsAttribution.setVisibility(View.GONE);
            } else {
                //mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
                //mPlaceDetailsAttribution.setText(Html.fromHtml(thirdPartyAttribution.toString()));
            }

            //Log.i(TAG, "Place details received: " + place.getName());

            places.release();
        }
    };
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            //Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(googleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            //Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        createLocationRequest();
        setContentView(R.layout.activity_add_location);
        mAutocompleteView = (AutoCompleteTextView)
                findViewById(R.id.autoCompleteTextView);
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);
        mAdapter = new PlaceAutocompleteAdapter(this, googleApiClient, BOUNDS_GREATER_SYDNEY,
                null);
        mAutocompleteView.setAdapter(mAdapter);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
        mapFragment.getMapAsync(this);
        Button select = (Button) findViewById(R.id.selectBut);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gotLoc) {
                    /*Bundle basket = new Bundle();
                    basket.putDouble("lat", lat);
                    basket.putDouble("lng", lng);
                    basket.putString("add", address);
                    Intent goBack = new Intent(AddLocation.this,MainActivity.class);
                    goBack.putExtras(basket);
                    startActivity(goBack);*/
                    startActivity(new Intent(AddLocation.this,Database.class));
                }
                else{
                    Toast.makeText(AddLocation.this,"Select toh kar",Toast.LENGTH_SHORT);
                }
            }
        });


    }

    /*private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(TAG, res.getString(Integer.parseInt("0"), name, id, address, phoneNumber,
        websiteUri));
        return Html.fromHtml(res.getString(Integer.parseInt("0"), name, id, address, phoneNumber,
                websiteUri));

    }*/

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Firing onMapReady..............................................");
        mapReady = true;
        gmap = googleMap;
        //LatLng newYork = new LatLng(40.7484, -73.9857);
        FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;

    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + googleApiClient.isConnected());
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;

        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        TextView loca = (TextView) findViewById(R.id.LocationTV);

        double lat = mCurrentLocation.getLatitude();
        double lon = mCurrentLocation.getLongitude();
        loca.setText("lat" + lat + "lon" + lon);
        if (mapReady && flag) {
            flag = false;
            LatLng current = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            CameraPosition target = CameraPosition.builder().target(current).zoom(17).build();
            gmap.animateCamera(CameraUpdateFactory.newCameraPosition(target), 3000, null);
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart fired ..............");
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop fired ..............");
        googleApiClient.disconnect();
        Log.e(TAG, "isConnected ...............: " + googleApiClient.isConnected());
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, mLocationRequest, this);
        Log.e(TAG, "Location update started ..............: ");
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
        Log.e(TAG, "Location update stopped .......................");
    }

}
