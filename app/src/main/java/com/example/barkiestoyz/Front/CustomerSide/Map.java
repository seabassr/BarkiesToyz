package com.example.barkiestoyz.Front.CustomerSide;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.example.barkiestoyz.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Map store locator, and user's location
public class Map extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentUserLocationMarker;
    private static final int Request_User_Location_Code = 99;
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference locationDB = database.child("Locations");
    private List<String> store = new ArrayList<>();
    private List<Double> latitude = new ArrayList<>();
    private List<Double> longitude = new ArrayList<>();
    private List<String> hours = new ArrayList<>();
    private int count;

    // Setup maps
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps);

        // Check user permission for location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkUserLocationPermission();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Check customer location permission
    public boolean checkUserLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            return false;
        }
        else {
            return true;
        }
    }

    // When maps first starts up
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    // Accessing google services
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    // Go to last location, and pin store location
    @SuppressWarnings("deprecation")
    public void onLocationChanged(Location location) {
        lastLocation = location;

        if (currentUserLocationMarker != null) {
            currentUserLocationMarker.remove();
        }

        // Store location
        LatLng lastLocation = new LatLng(29.354116308805647, -98.439425430251);

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(lastLocation, 15.5f);
        mMap.moveCamera(update);

        // Add marker and snippet
        MarkerOptions options = new MarkerOptions();
        options.position(lastLocation);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        options.title("Barkies Toyz");
        options.snippet("3143 SE Military Dr #115, San Antonio, TX 78223");
        mMap.addMarker(options);

        // Get user's location
        lastLocation = new LatLng(location.getLatitude(), location.getLongitude());

        CameraUpdate update2 = CameraUpdateFactory.newLatLngZoom(lastLocation, 15.5f);
        mMap.moveCamera(update2);

        // Add marker and snippet
        MarkerOptions options2 = new MarkerOptions();
        options2.position(lastLocation);
        options2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        options2.title("YOU");
        mMap.addMarker(options2);

        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    //  @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // When search button is click
    public void searchClick(View v) {
        // Grab text from edit text
        EditText ETLocation = findViewById(R.id.search);
        String location = ETLocation.getText().toString();

        List<Address> addlist;
        addlist = null;

        // Get location of address entered, and mark it
        if (location != null || location.equals("")) {
            Geocoder geo = new Geocoder(this);

            try {
                addlist = geo.getFromLocationName(location, 1);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addlist.get(0);
            LatLng newLocation = new LatLng(address.getLatitude(), address.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(newLocation, 15.5f);
            mMap.moveCamera(update);

            // Add marker and snippet
            MarkerOptions options = new MarkerOptions();
            options.position(newLocation);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            options.title("YOU");
            mMap.addMarker(options);
        }
    }

    // When exit button is clicked, leave map
    public void goBack(View v) {
        this.finish();
    }
}