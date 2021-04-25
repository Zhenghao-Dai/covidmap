package com.example.myapplication.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.DataBase.HistoryDBHelper;
import com.example.myapplication.DataBase.TestCenter;
import com.example.myapplication.DataBase.TestCenterDBHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Recorder extends Worker {

    private static final String TAG = "Recorder";
    private FusedLocationProviderClient mFusedLocationClient;
    private Context mContext;
    private Location mLocation;
    private LocationCallback mLocationCallback;
    private Geocoder mGeocoder;
    private String mCity = "Los Angeles County";
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    public Recorder(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: ");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        };

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            mFusedLocationClient
                    .getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                                Log.d(TAG, "Location : " + mLocation);


                                mGeocoder = new Geocoder(mContext);

                                try {
                                    List<Address> addresses = mGeocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
                                    if (addresses.get(0) != null){
                                        if(addresses.get(0).getLocality() != null){
                                            mCity = addresses.get(0).getLocality();
                                            Log.d(TAG, "Geocoder: City Changed to . " + mCity);
                                        }
                                        else{
                                            Log.d(TAG, "Geocoder: Locality Null. " + mCity);
                                        }
                                    } else{
                                        Log.d(TAG, "Geocoder: Address Null. " + mCity);
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, "Geocoder: Exception during Geocoder. " + e);
                                    e.printStackTrace();
                                }
                                Log.d(TAG, "Geocoder: Current City to . " + mCity);


                                // Record
                                HistoryDBHelper inst = HistoryDBHelper.getInstance(mContext);
                                Date date = new Date();
                                System.out.println(new Timestamp(date.getTime()));
                                inst.addHistoryItem(mCity, mLocation.getLatitude(), mLocation.getLongitude(), new Timestamp(date.getTime()));
                                // new Timestamp(today.getTime());
                                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
            return Result.failure();
        }

        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, null);
        } catch (SecurityException unlikely) {
            //Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
            return Result.failure();
        } catch (Exception e) {
            Log.e(TAG, "Exception. " + e);
            return Result.failure();
        }


        return Result.success();
    }


}
