package com.salty.potholefinder;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.salty.potholefinder.data.FileSystemRepository;
import com.salty.potholefinder.model.Pothole;
import com.salty.potholefinder.model.PotholeBuilder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.List;
import java.util.UUID;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    private String mCurrentPicturePath;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private ClusterManager<Pothole> mClusterManager;
    private GoogleApiClient googleApiClient;
    private String mCurrentPhotoPath;
    private FloatingActionButton fab;
    private boolean fabMenuIsOpen = false;

    private FileSystemRepository<Pothole> potHoleRepo;

    private Location lastLocation;

    Random r = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        fab = (FloatingActionButton) this.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabOnClick(v);
            }
        });

        final FloatingActionButton fabLocation = (FloatingActionButton) this.findViewById(R.id.fab_location);
        fabLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                if (lastLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(16.5f));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
                }

                Toast.makeText(getApplicationContext(), "Current position: " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            }
        });

        final FloatingActionButton fabCamera = (FloatingActionButton) this.findViewById(R.id.fab_camera);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
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
    public void onConnected(@Nullable Bundle bundle) {
        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        if (lastLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.zoomTo(16.5f));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Google Play API", "Could not connect to the Google Play API");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Pothole pothole = new PotholeBuilder()
                    .withPotholeID(UUID.randomUUID().toString())
                    .withLatitude(lastLocation.getLatitude())
                    .withLongitude(lastLocation.getLongitude())
                    .withPicturePath(mCurrentPicturePath)
                    .withUnixTimeStamp(new Date().getTime())
                    .createPothole();
            potHoleRepo.save(pothole.potholeID, pothole);

            addEffects();
            //Gets the bitmap and display in a ImageView
            //Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPicturePath);
            //ImageView mImageView = (ImageView)findViewById(R.id.activity_camera_imageview);
            //mImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                createAndSavePothole(latLng);
            }
        });

        try {
            mMap.setMyLocationEnabled(true);
        } catch(SecurityException e) {
            e.printStackTrace();
        }

        addEffects();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.INTERNET}, 10);
            return;
        }
        else
        {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    public void onBackPressed() {
        if (fabMenuIsOpen){
            fabOnClick(this.findViewById(R.id.fab));
        }
        else
        {
            moveTaskToBack(true);
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

        SimplexNoise simplexNoise = new SimplexNoise(100,0.1,5000);
        simplexNoise.seed = 1234567890;

        List<Pothole> potholes = repo.getAll();
        for (int i = 45, x = 0; i < 46; i+=0.0005, x++){
            for (int j = 0, y = 0; j < 100; j+=0.0005, y++){
                if(simplexNoise.getNoise(x, y) > 0.75)
                    addRandomPothole(potholes, i, j);
            }
        }

        for (Object pothole : potholes) {
            if (pothole != null) {
                try{
                    mClusterManager.addItem((Pothole)pothole);
                    LatLng current = ((Pothole)pothole).getPosition();
                    //mMap.addMarker(new MarkerOptions().position(current));
                    list.add(current);
                }catch(Exception e){
                    Log.e("ERROR", e.toString());
                }
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

    private void addRandomPothole(List<Pothole> potholes, double latitude, double longitude){


        potholes.add(new PotholeBuilder()
                .withPotholeID(UUID.randomUUID().toString())
                .withLatittude(latitude)
                .withLongitude(longitude)
                .withPicturePath("")
                .withUnixTimeStamp(new Date().getTime())
                .createPothole());
    }



    //Use this to open the camera app and take a picturePath
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                break;
        }
    }

    private void fabOnClick(View v){

        FloatingActionButton fabLocation = (FloatingActionButton) this.findViewById(R.id.fab_location);
        FloatingActionButton fabCamera = (FloatingActionButton) this.findViewById(R.id.fab_camera);

        FrameLayout.LayoutParams fabLocationParams = (FrameLayout.LayoutParams) fabLocation.getLayoutParams();
        FrameLayout.LayoutParams fabCameraParams = (FrameLayout.LayoutParams) fabCamera.getLayoutParams();

        if (fabMenuIsOpen){
            fab.setImageDrawable(getResources().getDrawable(R.drawable.pothole_icon_white));

            Animation hideFabLocation = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_location_hide);
            fabLocationParams.bottomMargin -= (int) (fabLocation.getHeight() * 1.3);
            fabLocation.setLayoutParams(fabLocationParams);
            fabLocation.startAnimation(hideFabLocation);

            Animation hideFabCamera = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_camera_hide);
            fabCameraParams.bottomMargin -= (int) (fabCamera.getHeight() * 2.5);
            fabCamera.setLayoutParams(fabCameraParams);
            fabCamera.startAnimation(hideFabCamera);
        }
        else{
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_white_48dp));

            Animation showFabLocation = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_location_show);
            fabLocationParams.bottomMargin += (int) (fabLocation.getHeight() * 1.3);
            fabLocation.setLayoutParams(fabLocationParams);
            fabLocation.startAnimation(showFabLocation);

            Animation showFabCamera = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_camera_show);
            fabCameraParams.bottomMargin += (int) (fabCamera.getHeight() * 2.5);
            fabCamera.setLayoutParams(fabCameraParams);
            fabCamera.startAnimation(showFabCamera);
        }

        fabLocation.setClickable(true);
        fabCamera.setClickable(true);

        fabMenuIsOpen = !fabMenuIsOpen;
        Log.d("shit", v.toString());
    }

    private void createAndSavePothole(LatLng latLng){
        String uuid = UUID.randomUUID().toString();
        potHoleRepo.save(uuid, new PotholeBuilder()
                .withPotholeID(uuid)
                .withLatitude(latLng.latitude)
                .withLongitude(latLng.longitude)
                .withPicturePath("")
                .withUnixTimeStamp(new Date().getTime())
                .createPothole());
        addEffects();
    }

    private void addRandomPothole(List<Pothole> potholes){
        potholes.add(new PotholeBuilder()
                .withPotholeID(UUID.randomUUID().toString())
                .withLatitude(randomLatitude())
                .withLongitude(randomLongitude())
                .withPicturePath("")
                .withUnixTimeStamp(new Date().getTime())
                .createPothole());
    }

    private double randomLongitude(){
        double longitudeMin = -74;
        double longitudeMax = -73;
        return randomDouble(longitudeMin, longitudeMax);
    }

    private double randomLatitude() {
        double latitudeMin = 45;
        double latitudeMax = 46;
        return randomDouble(latitudeMin, latitudeMax);
    }

    private double randomDouble(double min, double max){
        return min + (max - min) * r.nextDouble();
    }

}
