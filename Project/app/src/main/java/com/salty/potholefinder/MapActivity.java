package com.salty.potholefinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import android.util.Log;

import com.salty.potholefinder.data.FileSystemRepository;
import com.salty.potholefinder.model.Pothole;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class MapActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    private String mCurrentPicturePath;

    private FileSystemRepository<Pothole> potHoleRepo;

    Random r = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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
