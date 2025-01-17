package com.example.bewith.util.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import com.example.bewith.view.main.activity.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


public class LocationProviderManager {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    public static final int DEFAULT_LOCATION_REQUEST_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    public static final long DEFAULT_LOCATION_REQUEST_INTERVAL = 2000L;
    public static final long DEFAULT_LOCATION_REQUEST_FAST_INTERVAL = 2000L;
    private LocationCallback locationCallback;


    private Context context;
    public LocationProviderManager(Context context){
        this.context = context;
    }
    public void getMyLocation() {//내위치 갱신하기
        if (locationRequest == null) {
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(DEFAULT_LOCATION_REQUEST_PRIORITY);
            locationRequest.setInterval(DEFAULT_LOCATION_REQUEST_INTERVAL);
            locationRequest.setFastestInterval(DEFAULT_LOCATION_REQUEST_FAST_INTERVAL);
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //권한이 없으면 리턴
            return;
        }locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                MainActivity.myLatitude = locationResult.getLastLocation().getLatitude();
                MainActivity.myLongitude = locationResult.getLastLocation().getLongitude();
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);


            }
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        };
        //위치정보 요청
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

    }
}
