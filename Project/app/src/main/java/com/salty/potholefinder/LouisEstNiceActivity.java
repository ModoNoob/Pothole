package com.salty.potholefinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.salty.potholefinder.data.FileSystemRepository;
import com.salty.potholefinder.model.Pothole;

import java.util.Date;
import java.util.UUID;

public class LouisEstNiceActivity extends AppCompatActivity {

    private FileSystemRepository<Pothole> repository = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_louis_est_nice);

        repository = new FileSystemRepository<>(getApplicationContext());

        String uuid = UUID.randomUUID().toString();

        //repository.deleteAll();

        Pothole p = new Pothole();
        p.longtitude = 45.5037537d;
        p.latitude = -73.6150756d;
        p.picture = "charles pls";
        p.potholeID = uuid;
        p.unixTimeStamp = new Date().getTime();

        repository.save(uuid, p);
        Log.d("FileSystemRepository", "Saving file...");

        Pothole pothole = repository.get(uuid);
        Log.d("FileSystemRepository", "Reading file... " + pothole.potholeID);

        //repository.delete(uuid);
        Log.d("FileSystemRepository", "Deleting file...");

        for(Pothole pot : repository.getAll())
            if (pot != null)
                Log.d("FileSystemRepository", pot.potholeID);
    }
}
