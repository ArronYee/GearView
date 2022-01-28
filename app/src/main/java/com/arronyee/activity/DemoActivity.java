package com.arronyee.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arronyee.gearview.GearView;

public class DemoActivity extends AppCompatActivity {
    private GearView gearView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        gearView = findViewById(R.id.gearView);
        gearView.setGearViewClickListener(new GearView.GearViewClickListener() {
            @Override
            public void onClick(int tag) {
                Toast.makeText(gearView.getContext(),"tag:"+tag,Toast.LENGTH_SHORT).show();
            }
        });
//        gearView.beginAutoRotate();
    }
}
