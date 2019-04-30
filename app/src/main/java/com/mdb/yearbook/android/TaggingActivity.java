package com.mdb.yearbook.android;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

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

public class TaggingActivity extends AppCompatActivity {

    public static TaggingActivityAdapter adapter;
    public static ArrayList<String> taggedMembers;
    public static Context context;

    private static final int REQUEST_TAKE_PHOTO_PERMISSIONS = 4;
    private static int IMAGE_CAPTURE_REQUEST = 1;

    public static Uri testbro;

    public static boolean isVideo;

    public static Uri imageUri, newPhotoUri; //newPhotoUri is the current photo that the user took

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra("newVideoUri")) {
            setContentView(R.layout.activity_tagging_video);
            isVideo = true;

        } else {
            setContentView(R.layout.activity_tagging);
        }

        context = this;
        taggedMembers = YearbookActivity.taggedMembers;//getIntent().getStringArrayListExtra("taggedMembers");

        adapter = new TaggingActivityAdapter(getApplicationContext(), taggedMembers, this);

        SearchView memberNameSearch = (SearchView) findViewById(R.id.searchView);

        memberNameSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<String> searchResultList = new ArrayList<String>();
                for (int i = 0 ; i < taggedMembers.size(); i++)
                {
                    if (taggedMembers.get(i).toLowerCase().startsWith(newText.toLowerCase()))
                        searchResultList.add(taggedMembers.get(i));
                }
                adapter.updateList(searchResultList);
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        if (getIntent().hasExtra("newVideoUri")) {
            newPhotoUri = Uri.parse(getIntent().getStringExtra("newVideoUri"));
            testbro = newPhotoUri;
            ((VideoView) findViewById(R.id.ForTheBoys2)).setVideoURI(newPhotoUri);
            ((VideoView) findViewById(R.id.ForTheBoys2)).start();
            ((VideoView) findViewById(R.id.ForTheBoys2)).setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });
        } else {
            newPhotoUri = Uri.parse(getIntent().getStringExtra("newPhotoUri"));
            ((ImageView) findViewById(R.id.ForTheBoys)).setImageURI(newPhotoUri);
        }

        //imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.tagRecyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        ((FloatingActionButton) findViewById(R.id.floatingActionButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToFirebase();
            }
        });

        findViewById(R.id.descriptionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TaggingActivity.context, AddDescriptionActivity.class);
                startActivity(intent);
            }
        });
    }


    public void addToFirebase() {

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final String key = ref.child("Photos").push().getKey();

        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        final ArrayList<String> taggedMembers = new ArrayList<>();

        try {
            String x = YearbookActivity.mAuth.getCurrentUser().getUid();
            DatabaseReference d = YearbookActivity.mDatabase;

//        FirebaseUtils.groupIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            YearbookActivity.mDatabase.child("Users").child(YearbookActivity.mAuth.getCurrentUser().getUid()).child("groupIds").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        YearbookActivity.currentGroup = ((ArrayList<String>) dataSnapshot.getValue()).get(0); //get current group

                        ArrayList<String> groupIds = new ArrayList<>();
                        groupIds.add(YearbookActivity.currentGroup);

                        Long timeUnix = System.currentTimeMillis();
                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(getString(R.string.firebase_link));
                        final StorageReference newPhotoRef;
                        if (newPhotoUri.getPath().contains("video")) {
                            newPhotoRef = storageRef.child("photos/" + key + ".mp4");
                        } else {
                            newPhotoRef = storageRef.child("photos/" + key + ".jpg");
                        }

                        Calendar now = Calendar.getInstance();
                        final int year = now.get(Calendar.YEAR);
                        final int month = now.get(Calendar.MONTH) + 1;
                        final int day = now.get(Calendar.DAY_OF_MONTH);

                        final Photo newPhoto = new Photo("", key, YearbookActivity.mAuth.getCurrentUser().getUid(),
                                groupIds, "" + month + "" + day + "" + year, Long.toString(timeUnix), "", adapter.isTagged);

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


                                newPhotoRef.putFile(newPhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "ERROR IN TAGGING ACTIVITY",Toast.LENGTH_LONG);
        }
        finish();
    }

    public static void updateAdapter() {
        try {
            adapter.notifyDataSetChanged();
        } catch (Exception e) {

        }
    }


}
