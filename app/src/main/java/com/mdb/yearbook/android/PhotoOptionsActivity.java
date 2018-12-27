package com.mdb.yearbook.android;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class PhotoOptionsActivity extends AppCompatActivity {
        private String key;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_photo_options);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

                if (FeedAdapter.currentPhotoLink != null) {
                        key = FeedAdapter.currentPhotoLink.substring(FeedAdapter.currentPhotoLink.indexOf("%") +3, FeedAdapter.currentPhotoLink.indexOf(".jpg"));
                } else {
                        key = FeedAdapter.videoURLClickedFirebase.substring(FeedAdapter.videoURLClickedFirebase.indexOf("%") +3, FeedAdapter.videoURLClickedFirebase.indexOf(".mp4"));
                }
                ref.child("Photos").child(key).child("caption").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                                String s = dataSnapshot.getValue(String.class);
                                EditText e = (EditText) findViewById(R.id.editDescriptionText);
                                e.setText(s);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                });

                findViewById(R.id.deletePhoto).setOnClickListener(new View.OnClickListener() {
                        //            get photoid - iterate over group ids, iterate through each group's photo ids and remove the photo id
                        // remove from storage as well
                        public void onClick(View view) {
                                YearbookActivity.mDatabase.child("Groups").child(YearbookActivity.currentGroup).child("photoIds").child(key).removeValue();

                                StorageReference photoRef;
                                if (FeedAdapter.currentPhotoLink != null)
                                        photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(FeedAdapter.currentPhotoLink);
                                else {
                                        photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(FeedAdapter.videoURLClickedFirebase);
                                }

                                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                        }
                                }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                        }
                                });
                                YearbookActivity.mDatabase.child("Photos").child(key).removeValue();
                                Intent i = new Intent(PhotoOptionsActivity.this, YearbookActivity.class);
                                startActivity(i);
                                finish();

                        }
                });
                findViewById(R.id.editTags).setOnClickListener(new View.OnClickListener() {

                        public void onClick(View view) {
                        }
                });
                findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {

                        public void onClick(View view) {
                                EditText edit = (EditText) findViewById(R.id.editDescriptionText);
                                final String newDescription = edit.getText().toString();
                                YearbookActivity.mDatabase.child("Photos").child(key).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                Photo p = dataSnapshot.getValue(Photo.class);
                                                p.setCaption(newDescription);
                                                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                                ref.child("Photos").child(key).setValue(p);

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                });
                                Intent i = new Intent(PhotoOptionsActivity.this, YearbookActivity.class);
                                startActivity(i);
                                finish();
                        }
                });
        }
}