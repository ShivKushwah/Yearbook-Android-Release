package com.mdb.yearbook.android;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;

public class AddDescriptionActivity extends AppCompatActivity {

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_description);
        context = this;
        findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToFirebase();
                startActivity(new Intent(context, YearbookActivity.class));
            }
        });


    }

    public void addToFirebase() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final String key = ref.child("Photos").push().getKey();

        Calendar now = Calendar.getInstance();
        final int year = now.get(Calendar.YEAR);
        final int month = now.get(Calendar.MONTH) + 1;
        final int day = now.get(Calendar.DAY_OF_MONTH);

        final ArrayList<String> taggedMembers = new ArrayList<>();

        FirebaseUtils.groupIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    YearbookActivity.currentGroup = ((ArrayList<String>) dataSnapshot.getValue()).get(0); //get current group

                    ArrayList<String> groupIds = new ArrayList<>();
                    groupIds.add(YearbookActivity.currentGroup);

                    Long timeUnix = System.currentTimeMillis();
                    String caption = ((EditText) findViewById(R.id.captionText)).getText().toString();
                    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(getString(R.string.firebase_link));
                    final StorageReference newPhotoRef = storageRef.child("photos/" + key + ".jpg");
                    final Photo newPhoto = new Photo(caption, key, YearbookActivity.mAuth.getCurrentUser().getUid(),
                            groupIds, "" + month + "" + day + "" + year, Long.toString(timeUnix), "", TaggingActivity.adapter.isTagged);

                    ref.child("Groups").child(YearbookActivity.currentGroup).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Group g = dataSnapshot.getValue(Group.class);
                            if (g != null)  {
                                if (g.getPhotoIds() == null) {
                                    g.setPhotoIds(new ArrayList<String>());
                                }
                                g.getPhotoIds().add(key);
                            }


                            newPhotoRef.putFile(TaggingActivity.newPhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    newPhoto.setImageUrl(taskSnapshot.getDownloadUrl().toString());
                                    ref.child("Photos").child(key).setValue(newPhoto);
                                    //progressBar.setVisibility(ProgressBar.INVISIBLE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Image failed to upload", Toast.LENGTH_SHORT);
                                    //progressBar.setVisibility(ProgressBar.INVISIBLE);
                                }
                            });

                            ref.child("Groups").child(YearbookActivity.currentGroup).setValue(g);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });


                }catch (NullPointerException e)
                {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });

        finish();
    }
}
