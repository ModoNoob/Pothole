package com.salty.potholefinder;

import android.content.Context;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.salty.potholefinder.data.FileSystemRepository;
import com.salty.potholefinder.model.Pothole;

public class MainActivity extends AppCompatActivity {

    private FileSystemRepository<Pothole> potHoleRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        potHoleRepo = new FileSystemRepository<>(getApplicationContext());
    }
}
