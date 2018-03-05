package com.itbstudentapp.ChatSystem;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itbstudentapp.R;
import com.itbstudentapp.UtilityFunctions;
import com.itbstudentapp.utils.LetterImageView;

import java.util.ArrayList;

public class ContactList extends Dialog implements View.OnClickListener, TextWatcher {

    private ChatSystemController csr;

    private TextView studentButton, staffButton, lecturerButton;
    private EditText enteredText;

    private String currentChoice = "student";
    private String lastChoice = "";

    public ContactList(@NonNull Context context, ArrayList<ContactCard> cc, ChatSystemController csr)
    {
        super(context, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        setContentView(R.layout.contact_list);

        this.csr = csr;
        studentButton = findViewById(R.id.user_student);
        staffButton = findViewById(R.id.user_staff);
        lecturerButton = findViewById(R.id.user_lecturer);

        enteredText = findViewById(R.id.user_search_box);
        enteredText.addTextChangedListener(this);

        studentButton.setOnClickListener(this);
        staffButton.setOnClickListener(this);
        lecturerButton.setOnClickListener(this);

        populateList(csr.filterByType(currentChoice));


        show();
    }

    private void populateList(ArrayList<ContactCard> cc)
    {
        LinearLayout layout = this.findViewById(R.id.contact_holder);
        layout.removeAllViews();

        for(final ContactCard card : cc)
        {
            View v = LayoutInflater.from(this.getContext()).inflate(R.layout.contact_card, null);
            TextView userName = v.findViewById(R.id.contact_name);
            TextView userType = v.findViewById(R.id.contact_account);
            final TextView user_id = v.findViewById(R.id.contact_id);

            String[] userNameArray = card.getUser_name().split(" ");
            String name = UtilityFunctions.formatTitles(userNameArray[0]) + " " + UtilityFunctions.formatTitles(userNameArray[1]);
            userName.setText(name);

            String userAccountType = card.getUserAccountType().toString();
            userAccountType = userAccountType.substring(0,1).toUpperCase() + userAccountType.substring(1, userAccountType.length()).toLowerCase();
            userType.setText(userAccountType);

            user_id.setText(card.getUser_id().toUpperCase().replace("_","."));

            LetterImageView letterImageView = v.findViewById(R.id.letterCircle);
            letterImageView.setOval(true);
            letterImageView.setLetter(name.charAt(0));

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    csr.setCurrentContact(card);
                    dismiss();
                }
            });

            layout.addView(v);
        }
    }

    @Override
    public void onClick(View v)
    {
        String lastChoice = currentChoice;

        if(v.getId() == studentButton.getId())
        {
         currentChoice = "STUDENT";
        } else if(v.getId() == staffButton.getId())
        {
            currentChoice = "ITB_STAFF";
        } else if(v.getId() == lecturerButton.getId())
        {
            currentChoice = "LECTURER";
        }

        if(!lastChoice.equalsIgnoreCase(currentChoice))
        {
            ArrayList<ContactCard> filtered = csr.filterByType(currentChoice);
            populateList(filtered);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        if(s.toString().length() > lastChoice.length())
        {
            lastChoice = s.toString();
            populateList(csr.filterByName(lastChoice, false, currentChoice));
        } else{
            lastChoice = s.toString();
            populateList(csr.filterByName(lastChoice, true, currentChoice));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}

