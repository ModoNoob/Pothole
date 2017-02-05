package com.salty.potholefinder;

import android.graphics.Matrix;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MapActivity extends AppCompatActivity {

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
    }

    private void fabOnClick(View v){
        Toast.makeText(this, "WOW", Toast.LENGTH_LONG).show();
    }
}
