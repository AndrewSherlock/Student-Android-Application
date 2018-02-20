package com.itbstudentapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.microedition.khronos.opengles.GL;

public class ProfileSettings extends AppCompatActivity implements View.OnClickListener{

    private TextView changeProfilePicture, changePassword, backToHome;
    private static final int request_code = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        changeProfilePicture = findViewById(R.id.profile_change_image);
        changeProfilePicture.setOnClickListener(this);

        changePassword = findViewById(R.id.profile_reset_password);
        changePassword.setOnClickListener(this);

        backToHome = findViewById(R.id.profile_back_home);
        backToHome.setOnClickListener(this);

        loadProfilePicture();
    }

    private void loadProfilePicture()
    {
        final Context context = this;
        final ImageView profile = findViewById(R.id.profile_image);
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + UtilityFunctions.getUserNameFromFirebase());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("imageLink")!= null)
                {
                    StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReference("userImages/" + dataSnapshot.child("imageLink").getValue(String.class));

                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(context).load(uri).into(profile);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed getting user image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == changeProfilePicture.getId())
        {
            changeProfilePicture();
        }
        else if(v.getId() == changePassword.getId())
        {
            changeUserPassword();
        }
        else if(v.getId() == backToHome.getId())
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    private void changeUserPassword()
    {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.profile_reset_password);
        dialog.show();

        final EditText user_new_password = dialog.findViewById(R.id.password_modal_enter);
        final EditText user_reenter_password = dialog.findViewById(R.id.password_modal_reenter);
        final EditText user_old_password = dialog.findViewById(R.id.password_modal_old_password);

        TextView resetPassButton = dialog.findViewById(R.id.profile_reset_password);
        resetPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user_new_password.getText().equals(user_reenter_password.getText())) {
                    handlePasswordReset(user_new_password.getText().toString(), user_old_password.getText().toString());
                } else{
                    Toast.makeText(dialog.getContext(), "Passwords do not match. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handlePasswordReset(final String newPassword, final String oldPassword)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + UtilityFunctions.getUserNameFromFirebase());
        final Context context = this;
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("password").getValue(String.class).equals(oldPassword))
                {
                   //TODO grrrrr firebase
                } else{
                    Toast.makeText(context, "Passwords do not match. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void changeProfilePicture()
    {
        if(!UtilityFunctions.doesUserHaveConnection(this))
        {
            Toast.makeText(this, "No network connection. Please wait to upload image.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
        gallery.setType("image/*");
        startActivityForResult(Intent.createChooser(gallery, "Pick a file to upload"), request_code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        if(requestCode == request_code)
        {
            if(resultCode == RESULT_OK)
            {
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + UtilityFunctions.getUserNameFromFirebase());
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userImageLink;

                        if(dataSnapshot.child("imageLink").exists() && dataSnapshot.child("imageLink").getValue(String.class) != null)
                        {
                            // grab the old link to overwrite
                            userImageLink = dataSnapshot.child("imageLink").getValue(String.class);
                        } else{
                            // user has no image. easy upload
                            userImageLink = UUID.randomUUID().toString();
                            reference.child("imageLink").setValue(userImageLink);
                            Log.e("Key", reference.toString());
                        }

                        handleImageUpload(userImageLink, progressDialog, data.getData());
                    }

                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        }
    }

    private void handleImageUpload(final String userImageLink, final ProgressDialog progressDialog, final Uri data)
    {
        final Context context = this;
        final ImageView userImage = findViewById(R.id.profile_image);

        StorageReference storage = FirebaseStorage.getInstance().getReference("userImages/");
        storage.child(userImageLink).putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(progressDialog.getContext(), "File uploaded", Toast.LENGTH_SHORT).show();
                Glide.with(getApplicationContext()).load(data).into(userImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(progressDialog.getContext(), "Upload failed. Please try again", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setMessage((int) progress + "% complete.");
            }
        });
    }
}