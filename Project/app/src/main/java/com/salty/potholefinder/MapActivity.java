package com.salty.potholefinder;

import android.*;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import android.util.Log;
import android.widget.ImageView;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.salty.potholefinder.data.FileSystemRepository;
import com.salty.potholefinder.model.Pothole;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.List;
import java.util.UUID;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    private String mCurrentPicturePath;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ClusterManager<Pothole> mClusterManager;

    private String mCurrentPhotoPath;

    private FileSystemRepository<Pothole> potHoleRepo;

    Random r = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        final FloatingActionButton fabBtn = (FloatingActionButton) this.findViewById(R.id.fab);

        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabOnClick(v);
            }
        });

        potHoleRepo = new FileSystemRepository<>(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Pothole pothole = new PotholeBuilder()
                    .withPotholeID(UUID.randomUUID().toString())
                    .withLatittude(0.0).withLongitude(0.0)
                    .withPicturePath(mCurrentPicturePath)
                    .withUnixTimeStamp(new Date().getTime())
                    .createPothole();
            potHoleRepo.save(pothole.potholeID, pothole);

            //Gets the bitmap and display in a ImageView
            //Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPicturePath);
            //ImageView mImageView = (ImageView)findViewById(R.id.activity_camera_imageview);
            //mImageView.setImageBitmap(bitmap);
        }
    }

    //Use this to open the camera app and take a picturePath
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 10:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    configureGPS();
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        addEffects();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.d("Location", "Ici dans le onMapReady");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.d("Location", "Ici dans le onLocationChanged");
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(current).title("Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.INTERNET}, 10);
            return;
        }
        else
        {
            configureGPS();
        }
    }

    private void configureGPS() {
        try {
            Log.d("Location", "It changed");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0.1f, locationListener);
        } catch(SecurityException e) {
            e.printStackTrace();
        }

    }

    private void addEffects() {

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<Pothole>(this, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        FileSystemRepository<Pothole> repo = new FileSystemRepository<>(getApplicationContext());

        List<LatLng> list = new ArrayList<>();

        for (Pothole pothole : repo.getAll()) {
            if (pothole != null) {
                mClusterManager.addItem(pothole);
                LatLng current = pothole.getPosition();
                mMap.addMarker(new MarkerOptions().position(current));
                list.add(current);
            }
        }

        if (list.size() == 0)
            return;

        // Create a heat map tile provider, passing it the latlngs of the police stations.
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    //Use this to open the camera app and take a picture
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("ERROR", ex.getMessage());
            }

            // Continue only if the File was successfully created
            if (imageFile != null) {
                Uri imageURI = FileProvider.getUriForFile(this, "com.salty.potholefinder.fileprovider", imageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    //creates a file with a valid path that will then be used if
    //the camera returns an image
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPicturePath = image.getAbsolutePath();
        return image;
    }

    private void fabOnClick(View v){
        Toast.makeText(this, "WOW", Toast.LENGTH_LONG).show();
    }

    private void AddRandomPothole(ArrayList<Pothole> potholes){
        potholes.add(new PotholeBuilder()
                .withPotholeID(UUID.randomUUID().toString())
                .withLatittude(randomLatitude())
                .withLongitude(randomLongitude())
                .withPicturePath("")
                .withUnixTimeStamp(new Date().getTime())
                .createPothole());
    }

    private double randomLatitude(){
        double latitudeMin = 45.4402;
        double latitudeMax = 45.5248;
        return randomDouble(latitudeMin, latitudeMax);
    }

    private double randomLongitude(){
        double longitudeMin = -73.715;
        double longitudeMax = -73.564;
        return randomDouble(longitudeMin, longitudeMax);
    }

    private double randomDouble(double min, double max){
        return min + (max - min) * r.nextDouble();
    }
}
