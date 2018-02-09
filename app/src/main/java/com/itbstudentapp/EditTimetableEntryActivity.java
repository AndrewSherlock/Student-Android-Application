package com.itbstudentapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditTimetableEntryActivity extends AppCompatActivity {

    private static final String TAG = "EditDataActivity";

    private Button btnSave, btnDelete;
    private EditText class_event, day, time, room;

    DatabaseHelper databaseHelper;

    private String selectedClass;
    private int selectedID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_timetable_entry_layout);
        btnSave = findViewById(R.id.btnSaveTimetableEntry);
        btnDelete = (Button) findViewById(R.id.btnDeleteTimetableEntry);
        class_event = findViewById(R.id.classOrEvent);
        day = findViewById(R.id.timetableDay);
        time = findViewById(R.id.timetableTime);
        room = findViewById(R.id.timetableRoom);
        databaseHelper = new DatabaseHelper(this);

        //get the intent extra from the ListDataActivity
        Intent receivedIntent = getIntent();

        //now get the itemID we passed as an extra
        selectedID = receivedIntent.getIntExtra("id",-1); //NOTE: -1 is just the default value

        //now get the class we passed as an extra
        selectedClass = receivedIntent.getStringExtra("class/event");

        //set the text to show the current selected class
        class_event.setText(selectedClass);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String class_event = EditTimetableEntryActivity.this.class_event.getText().toString();
                String day = EditTimetableEntryActivity.this.day.getText().toString();
                String time = EditTimetableEntryActivity.this.time.getText().toString();
                String room = EditTimetableEntryActivity.this.room.getText().toString();
                if(!class_event.equals("")){
                    databaseHelper.updateTimetableEntry(selectedID, time,class_event,day,room);
                    Intent intent = new Intent(EditTimetableEntryActivity.this, DayView.class);
                    startActivity(intent);
                }else{
                    toastMessage("You must enter a class or event");
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseHelper.deleteTimetableEntry(selectedID);
                toastMessage("removed from database");
                Intent intent = new Intent(EditTimetableEntryActivity.this, DayView.class);
                startActivity(intent);
            }
        });

    }

    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}























