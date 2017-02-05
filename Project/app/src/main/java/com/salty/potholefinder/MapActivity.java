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
import java.util.Date;
import java.util.UUID;

public class MapActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    private String mCurrentPhotoPath;

    private FileSystemRepository<Pothole> potHoleRepo;

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
            createPotHoleWithImage();
            //Gets the bitmap and display in a ImageView
            //Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
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
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //Creates a Pothole with the image in the mCurrentPhotoPath
    private void  createPotHoleWithImage(){
        Pothole potHole = new Pothole();
        potHole.potholeID = UUID.randomUUID().toString();
        potHole.latitude = 0.0;
        potHole.longitude = 0.0;
        potHole.picturePath = mCurrentPhotoPath;
        potHole.unixTimeStamp = new Date().getTime();

        potHoleRepo.save(potHole.potholeID, potHole);
    }

    private void fabOnClick(View v){
        Toast.makeText(this, "WOW", Toast.LENGTH_LONG).show();
    }
}
