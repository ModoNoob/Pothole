package com.salty.potholefinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.salty.potholefinder.data.FileSystemRepository;

import java.util.UUID;

public class LouisEstNiceActivity extends AppCompatActivity {

    private FileSystemRepository<String> repository = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_louis_est_nice);

        repository = new FileSystemRepository<>(getApplicationContext());

        String uuid = UUID.randomUUID().toString();

        repository.deleteAll();

        repository.save(uuid, "Hello!");
        Log.d("FileSystemRepository", "Saving file...");

        String savedStuff = repository.get(uuid);
        Log.d("FileSystemRepository", "Reading file... " + savedStuff);

        repository.delete(uuid);
        Log.d("FileSystemRepository", "Deleting file...");

        for(String s : repository.getAll())
            if (s != null)
                Log.d("FileSystemRepository", s);
    }
}
