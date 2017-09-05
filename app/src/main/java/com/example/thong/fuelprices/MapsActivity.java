package com.example.thong.fuelprices;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public ArrayList<MarkerOptions> mMarkerArray = new ArrayList<>();
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int REQUEST_CHECK_SETTINGS = 99;
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    public static int CURRENT_POSITION = 0;
    public static int GETTING_LOCATION = 0;
    private int PROXIMITY_RADIUS = 3500;
    public double latitude,longitude;
    Spinner spinner;
    Context context = this;
    DBHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        db = new DBHandler(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        spinner = (Spinner) findViewById(R.id.spinner1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (mMarkerArray.size() != 0) {
                    mMap.clear();
                    LatLng latLng = new LatLng(latitude,longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude,longitude,1);
                        String str = addressList.get(0).getLocality()+",";
                        str += addressList.get(0).getCountryName();
                        mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    insertStations();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("getCurrentPlace", "GETTING ");
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                GETTING_LOCATION = 1;
                display();
                locationManager.removeUpdates(locationListener);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    public void callPlaceAutocompleteActivityIntent(View view) {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    public void display() {
        LatLng latLng = new LatLng(latitude,longitude);
        Geocoder geocoder = new Geocoder(getApplicationContext());
        try {
            mMap.clear();
            List<Address> addressList = geocoder.getFromLocation(latitude,longitude,1);
            if (CURRENT_POSITION == 1) {
                String str = addressList.get(0).getLocality()+",";
                str += addressList.get(0).getCountryName();
                mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                String type = "gas_station";
                String url = getUrl(latitude, longitude, type);
                Object[] DataTransfer = new Object[3];
                DataTransfer[0] = mMap;
                DataTransfer[1] = url;
                DataTransfer[2] = this;
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                getNearbyPlacesData.execute(DataTransfer);
                Toast.makeText(MapsActivity.this,"Nearby Gas Stations", Toast.LENGTH_LONG).show();
            }
            else {
                CURRENT_POSITION = 1;
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.2f));
            Log.i("currentLocation", "DONE");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void currentLocation(View view) {
        getCurrentPlace();
    }

    public void getCurrentPlace() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        displayLocationSettingsRequest();
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    public void onMapSearch(Place place) {
        String location = (String) place.getAddress();
        List<Address> addressList = null;
        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            latitude = address.getLatitude();
            longitude = address.getLongitude();
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title((String) place.getName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.2f));
            String type = "gas_station";
            String url = getUrl(address.getLatitude(), address.getLongitude(), type);
            Object[] DataTransfer = new Object[2];
            DataTransfer[0] = mMap;
            DataTransfer[1] = url;
            DataTransfer[2] = this;
            GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
            getNearbyPlacesData.execute(DataTransfer);
            Toast.makeText(MapsActivity.this,"Nearby Gas Stations", Toast.LENGTH_LONG).show();
        }
    }

    public void insertStations() {
        for (int i = 0; i<mMarkerArray.size(); i++) {
            Log.i("curr", String.valueOf(mMarkerArray.size()));
            LatLng latLng = mMarkerArray.get(i).getPosition();
            final double lat = latLng.latitude;
            final double lng = latLng.longitude;
            String name = mMarkerArray.get(i).getTitle();
            db.insertStation(name, lng, lat);
            String fuelType= spinner.getSelectedItem().toString();
            Log.i("curr", fuelType);
            Cursor res = db.getStation(lng,lat);
            res.moveToFirst();
            IconGenerator iconFactory = new IconGenerator(this);
            if (res.isAfterLast() == false) {
                String value = res.getString(res.getColumnIndex(fuelType));
                if (value.equals("-1"))
                    value = "NONE";
                MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(value)));
                markerOptions.position(latLng);
                mMap.addMarker(markerOptions);
            }
        }
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        Log.i("curr", latitude + "  " + longitude);
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyATuUiZUkEc_UgHuqsBJa1oqaODI-3mLs0");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        currentLocation(new View(this));
        Log.d("MAPREADY", "READY");
    }

    private void displayLocationSettingsRequest() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("MapsActivity", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("MapsActivity", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("MapsActivity", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("MapsActivity", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, intent);
                Log.i("MapsActivity", "Place:" + place.toString());
                onMapSearch(place);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, intent);
                Log.i("MapsActivity", "###"+status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {

            }
        }
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(intent);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        final IconGenerator iconFactory = new IconGenerator(this);
        LatLng latLng = marker.getPosition();
        final double lat = latLng.latitude;
        final double lng = latLng.longitude;
        Cursor res = db.getStation(lng,lat);
        res.moveToFirst();
        if (res.isAfterLast() == false) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.gas_station);
            dialog.setTitle(res.getString(res.getColumnIndex("Name")));
            TextView text = (TextView) dialog.findViewById(R.id.textView);
            text.setText(res.getString(res.getColumnIndex("Name")) + "\n"
                    + "U91: " + res.getString(res.getColumnIndex("U91")) + "\n"
                    + "U95: " + res.getString(res.getColumnIndex("U95")) + "\n"
                    + "U98: " + res.getString(res.getColumnIndex("U98")) + "\n"
                    + "LPG: " + res.getString(res.getColumnIndex("LPG")) + "\n"
                    + "E10: " + res.getString(res.getColumnIndex("E10")) + "\n"
                    + "E85: " + res.getString(res.getColumnIndex("E85")) + "\n"
                    + "Diesel: " + res.getString(res.getColumnIndex("Diesel")));
            Button okButton = (Button) dialog.findViewById(R.id.buttonOk);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("curr", "ok");
                    dialog.dismiss();
                }
            });
            Button deleteButton = (Button) dialog.findViewById(R.id.buttonDelete);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    final Dialog dialogDelete = new Dialog(context);
                    dialogDelete.setContentView(R.layout.delete);
                    dialogDelete.show();
                    Button okButton = (Button) dialogDelete.findViewById(R.id.buttonOk);
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText edit = (EditText) dialogDelete.findViewById(R.id.editTextReason);
                            String result = edit.getText().toString();
                            Log.i("curr", result);
                            db.deleteStation(lat,lng);
                            dialogDelete.dismiss();
                        }
                    });
                    Button cancelButton = (Button) dialogDelete.findViewById(R.id.buttonCancel);
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i("curr", "cancel");
                            dialogDelete.dismiss();
                        }
                    });
                    Log.i("curr", "delete");
                }
            });
            Button updateButton = (Button) dialog.findViewById(R.id.buttonUpdate);
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    final Dialog dialogUpdate = new Dialog(context);
                    dialogUpdate.setContentView(R.layout.update);
                    dialogUpdate.show();
                    Button okButton = (Button) dialogUpdate.findViewById(R.id.buttonOk);
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText edit = (EditText) dialogUpdate.findViewById(R.id.editTextU98);
                            String result = edit.getText().toString();
                            if (result.length() != 0) {
                                double price = Double.parseDouble(result);
                                db.updateU98(lat, lng, price);
                            }
                            edit = (EditText) dialogUpdate.findViewById(R.id.editTextU95);
                            result = edit.getText().toString();
                            if (result.length() != 0) {
                                double price = Double.parseDouble(result);
                                db.updateU95(lat, lng, price);
                            }
                            edit = (EditText) dialogUpdate.findViewById(R.id.editTextU91);
                            result = edit.getText().toString();
                            if (result.length() != 0) {
                                double price = Double.parseDouble(result);
                                db.updateU91(lat, lng, price);
                            }
                            edit = (EditText) dialogUpdate.findViewById(R.id.editTextE10);
                            result = edit.getText().toString();
                            if (result.length() != 0) {
                                double price = Double.parseDouble(result);
                                db.updateE10(lat, lng, price);
                            }
                            edit = (EditText) dialogUpdate.findViewById(R.id.editTextE85);
                            result = edit.getText().toString();
                            if (result.length() != 0) {
                                double price = Double.parseDouble(result);
                                db.updateE85(lat, lng, price);
                            }
                            edit = (EditText) dialogUpdate.findViewById(R.id.editTextLPG);
                            result = edit.getText().toString();
                            if (result.length() != 0) {
                                double price = Double.parseDouble(result);
                                db.updateLPG(lat, lng, price);
                            }
                            edit = (EditText) dialogUpdate.findViewById(R.id.editTextDiesel);
                            result = edit.getText().toString();
                            if (result.length() != 0) {
                                double price = Double.parseDouble(result);
                                db.updateDiesel(lat, lng, price);
                            }
                            String fuelType= spinner.getSelectedItem().toString();
                            Cursor res = db.getStation(lng,lat);
                            res.moveToFirst();
                            String value = res.getString(res.getColumnIndex(fuelType));
                            if (value.equals("-1"))
                                value = "NONE";
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(value)));
                            dialogUpdate.dismiss();
                        }
                    });
                    Button cancelButton = (Button) dialogUpdate.findViewById(R.id.buttonCancel);
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i("curr", "cancel");
                            dialogUpdate.dismiss();
                        }
                    });
                }
            });
            Button navigateButton = (Button) dialog.findViewById(R.id.buttonNavigation);
            navigateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent( Intent.ACTION_VIEW,
                            Uri.parse("http://ditu.google.cn/maps?f=d&source=s_d" +
                                    "&saddr="+ latitude +","+ longitude +"&daddr="+ lat +","+ lng +"&hl=zh&t=m&dirflg=d"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK&Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
                    Log.i("curr", "navigate");
                }
            });
            dialog.show();
        } else {
            Toast.makeText(MapsActivity.this,"Gas Station is removed", Toast.LENGTH_LONG).show();
        }
        return true;
    }
}

