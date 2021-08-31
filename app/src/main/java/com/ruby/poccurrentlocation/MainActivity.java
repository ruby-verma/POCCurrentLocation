package com.ruby.poccurrentlocation;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.io.Console;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static com.google.android.gms.location.LocationRequest.PRIORITY_LOW_POWER;

public class MainActivity extends AppCompatActivity {

    TextView tvLocation;
    Button btnGetLocation;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLocation;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

    ActivityResultLauncher<String[]> locationPermissionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                Log.d(getClass().getName(), "Precise location access granted.");
                                getLastLocation();
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                Log.d(getClass().getName(), "Only approximate location access granted.");
                                getLastLocation();
                            } else {
                                Log.d(getClass().getName(), "No location access granted.");
                            }
                        }
                );

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        btnGetLocation = (Button) findViewById(R.id.button);
        btnGetLocation.setOnClickListener( v -> {
            getLocation();
        });
    }

    private void getLocation() {
        System.out.println("Button clicked");

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            getLastLocation();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            Log.i(getClass().getName(), "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            locationPermissionRequest.launch(new String[] {
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            });
                        }
                    });
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void showSnackbar(final String text) {
        View container = findViewById(R.id.main_activity_container);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private boolean checkPermissions() {

        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(getClass().getName(), "onRequestPermissionsResult: " + requestCode);
        //if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(getClass().getName(), "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                Log.i(getClass().getName(), "Permission granted.");
                getLastLocation();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        //}
    }*/

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        Log.d(getClass().getName(), "getLastLocation: ");
        mFusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Log.d(getClass().getName(), "Task is not null");
                        boolean success = task.isSuccessful();
                        Log.d(getClass().getName(), "success: " + success);
                        boolean completed = task.isComplete();
                        Log.d(getClass().getName(), "completed: " + completed);
                        boolean canceled = task.isCanceled();
                        Log.d(getClass().getName(), "canceled: " + canceled);
                        mLocation = task.getResult();
                        if(mLocation != null) {
                            Log.d(getClass().getName(), "result: " + mLocation.toString());
                            @SuppressLint("DefaultLocale") String result = String.format("Location (success): %f: %f", mLocation.getLatitude(), mLocation.getLongitude()) ;
                            tvLocation.setText(result);
                        }
                        else {
                            Log.d(getClass().getName(), "result is null");
                            showSnackbar(getString(R.string.no_location_detected));
                        }
                    }
                });
    }
}