package com.itbstudentapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Transport extends AppCompatActivity implements View.OnClickListener{

    private LinearLayout dub_bus, bus_eir, irish_rail, itb_shuttle;
    private ProgressDialog progressDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport);

        dub_bus = (LinearLayout) findViewById(R.id.dub_bus);
        bus_eir = (LinearLayout) findViewById(R.id.bus_eir);
        irish_rail = (LinearLayout) findViewById(R.id.iar_eir);
        itb_shuttle = (LinearLayout) findViewById(R.id.itb_shuttle);

        dub_bus.setOnClickListener(this);
        bus_eir.setOnClickListener(this);
        irish_rail.setOnClickListener(this);
        itb_shuttle.setOnClickListener(this);

       // TrainHandler th = new TrainHandler();
        //try {
         //   th.getXmlStringOfDetails();

        //} catch (IOException e){}


    }

    @Override
    public void onClick(View v) {

        if(!UtilityFunctions.doesUserHaveConnection(this))
        {
            Toast.makeText(this, "No network connection. Please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        switch (v.getId())
        {
            case R.id.dub_bus:
                CallBusScreen("dublin_bus");
                break;
            case R.id.bus_eir:
                break;
            case R.id.iar_eir:
                break;
            case R.id.itb_shuttle:
                break;
            default:
                Log.e("Error", "onClick: was unknown button.");
                break;
        }
    }

    private void CallBusScreen(String methodOfTravel)
    {
        Intent intent = new Intent(this, RouteChoice.class);
        startActivity(intent);
        finish();
    }
}
