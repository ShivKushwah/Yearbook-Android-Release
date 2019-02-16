package com.mdb.yearbook.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.github.clans.fab.FloatingActionMenu;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class YearbookActivity extends AppCompatActivity {
    //NOTE: To switch to a different firebase, you need to create a new firebase, redownload/overwrite google-services.json, change the string constant for
    //the firebase storage link in the strings.xml under the values folder in resources, and then change the firebase database link in the values.xml file under
    // app/build/ .. //google-service/debug folder

    public static FirebaseAuth mAuth;
    private static FirebaseAuth.AuthStateListener mAuthListener;

    public static DatabaseReference mDatabase;

    public static String currentGroup;

    private static int IMAGE_CAPTURE_REQUEST = 1;
    private static int IMAGE_GALLERY_REQUEST = 2;
    private static int COVER_GALLERY_UPDATE = 3;
    private static final int REQUEST_TAKE_PHOTO_PERMISSIONS = 4;
    private static final int REQUEST_VIDEO_CAPTURE = 5;
    public static boolean alarmServiceOn = false;

    private Uri newPhotoUri, coverPhotoUri;
    public static Activity activity;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private static ProgressBar progressBar;
    private GroupsAdapter groupsAdapter;
    private ArrayList<Group> groupsList;
    private static ImageView background;
    private static TextView getStartedTextView;
    private static Toolbar toolbar;

    final static ArrayList<String> taggedMembers = new ArrayList<>();

    public static Context context;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yearbook);
        activity = YearbookActivity.this;
        context = getApplicationContext();

        alarmServiceOn = false;

        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        final View actionA = findViewById(R.id.action_a);
        final View actionB = findViewById(R.id.action_b);
        final View actionC = findViewById(R.id.action_c);
//        final View actionD = findViewById(R.id.action_d);

        final FloatingActionMenu menuMultipleActions = (FloatingActionMenu) findViewById(R.id.multiple_actions);
        menuMultipleActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuMultipleActions.setClosedOnTouchOutside(true);

            }
        });
        actionA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_TAKE_PHOTO_PERMISSIONS); //Take Photo
                menuMultipleActions.setClosedOnTouchOutside(true);
                menuMultipleActions.close(true);
            }
        });

        actionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchGalleryPictureIntent();
                menuMultipleActions.setClosedOnTouchOutside(true);
                menuMultipleActions.close(true);
            }
        });
        actionC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchCoverGalleryPictureIntent();
                menuMultipleActions.setClosedOnTouchOutside(true);
                menuMultipleActions.close(true);
            }
        });
//        actionD.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ActivityCompat.requestPermissions(activity,
//                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        REQUEST_VIDEO_CAPTURE); //Take Photo
//                menuMultipleActions.setClosedOnTouchOutside(true);
//                menuMultipleActions.close(true);
//
//            }
//        });

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(YearbookActivity.this, LoginActivity.class));
                }
            }
        };

        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), Scheduler.class);

        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY,
                AlarmManager.INTERVAL_DAY, alarmIntent);

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view


        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "44"); //increment the string to view the onboarding again on next run

        sequence.setOnItemShownListener(new MaterialShowcaseSequence.OnSequenceItemShownListener() {
            @Override
            public void onShow(MaterialShowcaseView itemView, int position) {
                //Toast.makeText(itemView.getContext(), "Item #" + position, Toast.LENGTH_SHORT).show();
            }
        });

        sequence.setConfig(config);

        sequence.addSequenceItem(findViewById(R.id.space), "Press the PLUS button to add photos", "GOT IT");

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(findViewById(R.id.space2))
                        .setDismissText("GOT IT")
                        .setContentText("Swipe right to view your photos")
                        .withRectangleShape(true)
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(findViewById(R.id.toolbar))
                        .setDismissText("GOT IT")
                        .setContentText("Tap on the icons to manage your yearbooks and add members to your current yearbook")
                        .withRectangleShape(true)
                        .build()
        );

        sequence.start();
    }



    //TODO does this really need to be here it's already a 1000 line file
    public static class FeedFragment extends Fragment {
        //Fragment for the Photo Feed view for all photos taken for this user
        //Scrollable photo view

        @Nullable
        private ArrayList<Photo> photos;
        private ArrayList<String> feedPhotoUrls;

        private RecyclerView photosListView;
        private FeedAdapter adapter;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View feedView = inflater.inflate(R.layout.fragment_feed, container, false);
            background = ((ImageView) feedView.findViewById(R.id.emptyImageView));
            getStartedTextView = ((TextView) feedView.findViewById(R.id.getStartedTextView));
            photos = new ArrayList<>();
            feedPhotoUrls = new ArrayList<>();

            photosListView = (RecyclerView) feedView.findViewById(R.id.photosRecyclerID);
            photosListView.setLayoutManager(new LinearLayoutManager(getActivity()));

            adapter = new FeedAdapter(getActivity(), photos);
            photosListView.setAdapter(adapter);

            //load the feed
            mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        currentGroup = ((ArrayList<String>) dataSnapshot.getValue()).get(0);
                        photos = new ArrayList<Photo>();
                        feedPhotoUrls = new ArrayList<String>();
                        mDatabase.child("Groups").child(currentGroup).child("photoIds").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                photos = new ArrayList<Photo>();
                                feedPhotoUrls = new ArrayList<String>();
                                ArrayList<String> photoUrls = (ArrayList<String>) dataSnapshot.getValue();

                                if (photoUrls != null && photoUrls.size() > 0) {
                                    makeBackgroundInvisible();
                                    for (String s : photoUrls) {
                                        mDatabase.child("Photos").child(s).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                HashMap<String, Object> photoMap = (HashMap<String, Object>) dataSnapshot.getValue();

                                                if (photoMap != null) {
                                                    Photo p = new Photo(
                                                            (String) photoMap.get("caption"), (String) photoMap.get("imageUrl"), (String) photoMap.get("posterId"),
                                                            (ArrayList<String>) photoMap.get("groupIds"), (long) photoMap.get("date"),
                                                            (String) photoMap.get("location"), (ArrayList<String>) photoMap.get("taggedMembers")
                                                    );

                                                    if (feedPhotoUrls.size() > 0 && feedPhotoUrls.contains(p.getImageUrl())) {
                                                        Log.w("EXTRA", "EXTRA DETECTED");
                                                    }else
                                                    {
                                                        photos.add(p);
                                                    }

                                                    feedPhotoUrls.add(p.getImageUrl());

                                                }

                                                adapter.updateList(photos);
                                                adapter.notifyDataSetChanged();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                } else
                                {
                                    makeBackgroundVisible();
                                    adapter.updateList(photos);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
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

            return feedView;
        }
        public void makeBackgroundInvisible() {
            background.setVisibility(View.GONE);
            getStartedTextView.setVisibility(View.GONE);
        }
        public void makeBackgroundVisible() {
            background.setVisibility(View.VISIBLE);
            getStartedTextView.setVisibility(View.VISIBLE);
        }

    }


    //TODO put these fragments in separate files please
    public static class CalendarFragment extends Fragment {
        //fragment for calendar view

        @Nullable
        CustomCalendarView calendar;
        GridView grid;
        ImageView profileImage;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
            View calendarView = inflater.inflate(R.layout.fragment_calendar, container, false);

            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTimeInMillis(System.currentTimeMillis());
            calendar2.set(Calendar.HOUR_OF_DAY, 20);
            calendar2.set(Calendar.MINUTE, 28);

            mAuth = FirebaseAuth.getInstance();

            calendar = (CustomCalendarView) calendarView.findViewById(R.id.calendar);

            profileImage = (ImageView) calendarView.findViewById(R.id.groupCoverImageID);
//            profileImage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dispatchCoverGalleryPictureIntent();
//                }
//            });
            //get current group
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try
                    {
                        currentGroup = ((ArrayList<String>) dataSnapshot.getValue()).get(0);
                    } catch (NullPointerException e)
                    {

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // ...
                }
            });

            //Get group photo from firebase and group name and place it in the toolbar
            mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        currentGroup = ((ArrayList<String>) dataSnapshot.getValue()).get(0);
                        mDatabase.child("Groups").child(currentGroup).child("coverPhotoUrl").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String url = (String) dataSnapshot.getValue();
                                if (url != null && !url.equals("")) {
                                    Glide.with(context)
                                            .load(url)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(profileImage);
                                    progressBar.setVisibility(ProgressBar.INVISIBLE);

                                }else
                                {
                                    profileImage.setImageResource(R.drawable.profile_purple);
                                }

                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        mDatabase.child("Groups").child(currentGroup).child("title").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String groupName = (String)dataSnapshot.getValue();
                                ((TextView)YearbookActivity.activity.findViewById(R.id.groupTitle)).setText(groupName);
                                //toolbar.setTitle(groupName);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
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

            return calendarView;
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return new CalendarFragment();
            } else {
                return new FeedFragment();
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
            }
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("ACT", "ACT RESULT");

        //Handle coming back from take photo screen for either uploading a photo or changing cover photo

        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK) {

            newPhotoUri = data.getData();
            if (newPhotoUri != null) {
                Log.d("Photo status", "ADDED");
                addPhoto();
            }else
            {
                Log.d("NULL RETURN", "NULL");
            }
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            newPhotoUri = data.getData();
            addVideo();
            //mVideoView.setVideoURI(videoUri);
        }

        if (requestCode == IMAGE_CAPTURE_REQUEST && resultCode == RESULT_OK)
        {
            newPhotoUri = imageUri;
            if (newPhotoUri != null) {
                Log.d("Photo status", "ADDED");
                addPhoto();
            }else
            {
                Log.d("NULL RETURN", "NULL");
            }
        }

        if (requestCode == COVER_GALLERY_UPDATE && resultCode == RESULT_OK)
        {
            coverPhotoUri = data.getData();
            if (coverPhotoUri != null) {
                StorageReference coverStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(getString(R.string.firebase_link));

                final String groupPhotoKey = mDatabase.child("Groups").child(currentGroup).child("coverPhotoUrl").push().getKey();

                progressBar.setVisibility(ProgressBar.VISIBLE);

                StorageReference newPhotoRef = coverStorageRef.child("photos/" + groupPhotoKey + ".jpg");

                newPhotoRef.putFile(coverPhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String url = taskSnapshot.getDownloadUrl().toString();
                        mDatabase.child("Groups").child(currentGroup).child("coverPhotoUrl").setValue(url);
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Image failed to upload", Toast.LENGTH_SHORT);
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                });
            }
        }
    }

    private void createAlertDialog() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Choose Group Cover Photo",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(YearbookActivity.this);
        builder.setTitle("Select a Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    dispatchTakePictureIntent();
                } else if (items[item].equals("Choose from Library")) {
                    dispatchGalleryPictureIntent();
                } else if (items[item].equals("Choose Group Cover Photo")) {
                    dispatchCoverGalleryPictureIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            imageUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", createImageFile());
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST);
        } catch (IOException e) {}

    }

    private void dispatchGalleryPictureIntent() {
        Intent galleryPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryPictureIntent.setType("image/*");
        startActivityForResult(galleryPictureIntent, IMAGE_GALLERY_REQUEST);
    }

    private void dispatchCoverGalleryPictureIntent() {
        Intent galleryPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryPictureIntent.setType("image/*");
        startActivityForResult(galleryPictureIntent, COVER_GALLERY_UPDATE);
    }

    //upload photo after taking with the default camera app
    private void addVideo()
    {
        //progressBar.setVisibility(ProgressBar.VISIBLE);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final String key = ref.child("Photos").push().getKey();

        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        //TODO: Fill up taggedMembers
        //final ArrayList<String> taggedMembers = new ArrayList<>();
        //TEST

        //load the feed
        mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    currentGroup = ((ArrayList<String>) dataSnapshot.getValue()).get(0);
                    mDatabase.child("Groups").child(currentGroup).child("memberIds").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            ArrayList<String> taggedMembersTemp = (ArrayList<String>) dataSnapshot.getValue();
                            for (String key: taggedMembersTemp) {

                                mDatabase.child("Users").child(key).child("name")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                taggedMembers.add((String) dataSnapshot.getValue());
                                                TaggingActivity.updateAdapter();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                            }

                            taggedMembers.clear();

                            Intent intent = new Intent(YearbookActivity.context, TaggingActivity.class);
                            intent.putExtra("newVideoUri", newPhotoUri.toString());
//        intent.putExtra("imageUri", imageUri.toString());
                            intent.putExtra("taggedMembers", taggedMembers);

                            //TODO: Add new tagging activity here
                            startActivity(intent);

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
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

    }

    //upload photo after taking with the default camera app
    private void addPhoto()
    {
        //progressBar.setVisibility(ProgressBar.VISIBLE);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final String key = ref.child("Photos").push().getKey();

        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        //TODO: Fill up taggedMembers
        //final ArrayList<String> taggedMembers = new ArrayList<>();
        //TEST

        //load the feed
        mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    currentGroup = ((ArrayList<String>) dataSnapshot.getValue()).get(0);
                    mDatabase.child("Groups").child(currentGroup).child("memberIds").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            ArrayList<String> taggedMembersTemp = (ArrayList<String>) dataSnapshot.getValue();
                            for (String key: taggedMembersTemp) {

                                mDatabase.child("Users").child(key).child("name")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                              @Override
                                              public void onDataChange(DataSnapshot dataSnapshot) {
                                                  taggedMembers.add((String) dataSnapshot.getValue());
                                                  TaggingActivity.updateAdapter();
                                              }

                                              @Override
                                              public void onCancelled(DatabaseError databaseError) {

                                              }
                                          });
                            }

                            taggedMembers.clear();

                            Intent intent = new Intent(YearbookActivity.context, TaggingActivity.class);
                            intent.putExtra("newPhotoUri", newPhotoUri.toString());
//        intent.putExtra("imageUri", imageUri.toString());
                            intent.putExtra("taggedMembers", taggedMembers);

                            //TODO: Add new tagging activity here
                            startActivity(intent);

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
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



        

//        mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                try {
//                    currentGroup = ((ArrayList<String>) dataSnapshot.getValue()).get(0); //get current group
//
//                    ArrayList<String> groupIds = new ArrayList<>();
//                    groupIds.add(currentGroup);
//
//                    Long timeUnix = System.currentTimeMillis();
//                    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(getString(R.string.firebase_link));
//                    final StorageReference newPhotoRef = storageRef.child("photos/" + key + ".jpg");
//                    final Photo newPhoto = new Photo("", key, mAuth.getCurrentUser().getUid(),
//                            groupIds, timeUnix, "", taggedMembers);
//
//                    ref.child("Groups").child(currentGroup).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            Group g = dataSnapshot.getValue(Group.class);
//                            if (g != null)  {
//                                if (g.getPhotoIds() == null) {
//                                    g.setPhotoIds(new ArrayList<String>());
//                                }
//                                g.getPhotoIds().add(key);
//                            }
//
//
//                            newPhotoRef.putFile(newPhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                @Override
//                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                    newPhoto.setImageUrl(taskSnapshot.getDownloadUrl().toString());
//                                    ref.child("Photos").child(key).setValue(newPhoto);
//                                    progressBar.setVisibility(ProgressBar.INVISIBLE);
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(getApplicationContext(), "Image failed to upload", Toast.LENGTH_SHORT);
//                                    progressBar.setVisibility(ProgressBar.INVISIBLE);
//                                }
//                            });
//
//                            ref.child("Groups").child(currentGroup).setValue(g);
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) { }
//                    });
//
//
//                }catch (NullPointerException e)
//                {
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // ...
//            }
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.videoButton:
//                ActivityCompat.requestPermissions(activity,
//                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        REQUEST_VIDEO_CAPTURE); //Take Photo
//
//
//                break;
            case R.id.settingsButtonID:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

//            case R.id.logoutMenuButtonID:
//                startActivity(new Intent(this, LoginActivity.class));
//                finish();
//                mAuth.signOut();
//                break;
//
//            case R.id.groupsMenuButtonID: //change group button
//                LayoutInflater groupManager = LayoutInflater.from(YearbookActivity.this);
//                View groupsView = groupManager.inflate(R.layout.group_management, null);
//                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(YearbookActivity.this);
//
//                RecyclerView groupsRecyclerView = (RecyclerView)groupsView.findViewById(R.id.groupsRecyclerView);
//                groupsRecyclerView.setLayoutManager(new LinearLayoutManager(YearbookActivity.this));
//                groupsList = new ArrayList<>();
//
//                alertDialogBuilder.setView(groupsView);
//
//                alertDialogBuilder
//                        .setCancelable(true);
//
//                final android.app.AlertDialog alertDialog = alertDialogBuilder.create();
//
//                groupsAdapter = new GroupsAdapter(YearbookActivity.this, groupsList, mDatabase, mAuth, alertDialog);
//                groupsRecyclerView.setAdapter(groupsAdapter);
//
//                FloatingActionButton addGroupButton = (FloatingActionButton)groupsView.findViewById(R.id.addGroupButton);
//
//                mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        groupsList = new ArrayList<Group>();
//                        for (String s: (ArrayList<String>) dataSnapshot.getValue())
//                        {
//                            mDatabase.child("Groups").child(s).addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    Group g = dataSnapshot.getValue(Group.class);
//                                    if (g!= null) {
//                                        if (g.getPhotoIds() == null) {
//                                            g.setPhotoIds(new ArrayList<String>());
//                                        }
//                                        groupsList.add(g);
//                                    }
//
//                                    groupsAdapter.updateList(groupsList);
//                                    groupsAdapter.notifyDataSetChanged();
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//                            });
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//
//                addGroupButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        LayoutInflater groupManager = LayoutInflater.from(YearbookActivity.this);
//                        View newGroupView = groupManager.inflate(R.layout.group_add_layout, null);
//                        android.app.AlertDialog.Builder newGroupAlertDialogBuilder = new android.app.AlertDialog.Builder(YearbookActivity.this);
//
//                        final EditText newGroupName = (EditText)newGroupView.findViewById(R.id.newGroupNameInput);
//
//                        newGroupAlertDialogBuilder.setView(newGroupView);
//
//                        newGroupAlertDialogBuilder
//                                .setCancelable(false)
//                                .setPositiveButton("Confirm",
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog,int id) {
//
//                                                final String groupKey = mDatabase.child("Groups").push().getKey();
//
//                                                String name = newGroupName.getText().toString().trim();
//
//                                                if (name != null && name.length()>0) {
//
//                                                    try
//                                                    {
//                                                        final int MISSES = 3;
//
//                                                        ArrayList<String> newGroupMembers = new ArrayList<String>();
//                                                        newGroupMembers.add(mAuth.getCurrentUser().getUid());
//                                                        Long firstDate = System.currentTimeMillis();
//
//                                                        ArrayList<String> adminIDs = new ArrayList<>();
//                                                        adminIDs.add(mAuth.getCurrentUser().getUid());
//
//                                                        Group newGroup = new Group(firstDate, name, mAuth.getCurrentUser().getUid(), adminIDs, 0, newGroupMembers,
//                                                                new ArrayList<String>(), MISSES, null, "Personal Album", new HashMap<String, Long>(), -1, -1);
//
//                                                        mDatabase.child("Groups").child(groupKey).setValue(newGroup);
//
//                                                        mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").addListenerForSingleValueEvent(new ValueEventListener() {
//                                                            @Override
//                                                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                                                ArrayList<String> currentGroupIds = (ArrayList<String>) dataSnapshot.getValue();
//                                                                ArrayList<String> newGroupIds = new ArrayList<String>();
//
//                                                                newGroupIds.add(groupKey);
//
//                                                                for (String groupId : currentGroupIds) {
//                                                                    newGroupIds.add(groupId);
//                                                                }
//
//                                                                mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").setValue(newGroupIds);
//                                                                Toast.makeText(YearbookActivity.this, "Group Created!", Toast.LENGTH_SHORT).show();
//
//                                                                alertDialog.cancel();
//                                                            }
//
//                                                            @Override
//                                                            public void onCancelled(DatabaseError databaseError) {
//
//                                                            }
//                                                        });
//
//
//
//                                                    }catch(Exception e)
//                                                    {
//                                                        Toast.makeText(YearbookActivity.this, "Enter a valid name and Tolerance!", Toast.LENGTH_SHORT).show();
//                                                    }
//
//                                                }else
//                                                {
//                                                    Toast.makeText(YearbookActivity.this, "Enter a valid group name!", Toast.LENGTH_SHORT).show();
//                                                }
//
//                                            }})
//                                .setNegativeButton("Cancel",
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog,int id) {
//                                                dialog.cancel();
//                                            }});
//
//                        android.app.AlertDialog newGroupAlertDialog = newGroupAlertDialogBuilder.create();
//                        newGroupAlertDialog.show();
//
//                        Button nButton = newGroupAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
//                        nButton.setTextColor(getResources().getColor(R.color.purple_main));
//                        Button pButton = newGroupAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
//                        pButton.setTextColor(getResources().getColor(R.color.purple_main));
//
//                    }
//                });
//
//                alertDialog.show();
//                break;
//
//                //FIXME i really want this to be a todo but i am feeling merciful. this should be more modular or in a different method or something
//            case R.id.shareMenuButtonID: //add user to group button
//                LayoutInflater inviter = LayoutInflater.from(YearbookActivity.this);
//                View inviteView = inviter.inflate(R.layout.invite_to_group, null);
//
//                android.app.AlertDialog.Builder inviteDialogBuilder = new android.app.AlertDialog.Builder(YearbookActivity.this);
//
//                final EditText inviteEmailText = (EditText)inviteView.findViewById(R.id.addMemberToGroup);
//
//                inviteDialogBuilder.setView(inviteView);
//
//                inviteDialogBuilder
//                        .setCancelable(false)
//                        .setPositiveButton("Add to Group",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog,int id) {
//
//                                        final String email = inviteEmailText.getText().toString().trim();
//
//                                        mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").addListenerForSingleValueEvent(new ValueEventListener() {
//                                            @Override
//                                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                                try {
//                                                    currentGroup = ((ArrayList<String>) dataSnapshot.getValue()).get(0);
//
//
//                                                    mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
//                                                        @Override
//                                                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                                            if (email!=null && !email.equals("")) {
//                                                                boolean found = false;
//                                                                for (final DataSnapshot user : dataSnapshot.getChildren()) {
//                                                                    HashMap<String, Object> userMap = (HashMap<String, Object>) user.getValue();
//                                                                    if (((String) userMap.get("email")).equalsIgnoreCase(email))
//                                                                    {
//                                                                        ArrayList<String> userGroups = (ArrayList<String>) userMap.get("groupIds");
//                                                                        userGroups.add(currentGroup);
//
//                                                                        final User newUser = new User((String)userMap.get("name"), (String)userMap.get("email"), "", userGroups);
//                                                                        mDatabase.child("Users").child(user.getKey()).setValue(newUser);
//
//                                                                        mDatabase.child("Groups").child(currentGroup).child("memberIds").addListenerForSingleValueEvent(new ValueEventListener() {
//                                                                            @Override
//                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                                                                ArrayList<String> ids = (ArrayList<String>) dataSnapshot.getValue();
//                                                                                ids.add(user.getKey());
//                                                                                mDatabase.child("Groups").child(currentGroup).child("memberIds").setValue(ids);
//                                                                            }
//
//                                                                            @Override
//                                                                            public void onCancelled(DatabaseError databaseError) {
//
//                                                                            }
//                                                                        });
//
//                                                                        Toast.makeText(YearbookActivity.this, (String)userMap.get("name") + " has been Added!", Toast.LENGTH_SHORT).show();
//                                                                        found = true;
//                                                                        break;
//                                                                    }
//                                                                }
//                                                                if (!found)
//                                                                {
//                                                                    Toast.makeText(YearbookActivity.this, "User could not be Found!", Toast.LENGTH_SHORT).show();
//                                                                }
//                                                            }else
//                                                            {
//                                                                Toast.makeText(YearbookActivity.this, "Please enter a valid e-mail!", Toast.LENGTH_SHORT).show();
//                                                            }
//                                                        }
//
//                                                        @Override
//                                                        public void onCancelled(DatabaseError databaseError) {
//
//                                                        }
//                                                    });
//
//                                                }catch (NullPointerException e)
//                                                {
//
//                                                }
//                                            }
//
//                                            @Override
//                                            public void onCancelled(DatabaseError databaseError) {
//                                                // ...
//                                            }
//                                        });
//
//                                    }})
//                        .setNegativeButton("Cancel",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog,int id) {
//                                        dialog.cancel();
//                                    }});
//
//                android.app.AlertDialog inviteDialog = inviteDialogBuilder.create();
//                inviteDialog.show();
//                Button nButton = inviteDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
//                nButton.setTextColor(getResources().getColor(R.color.purple_main));
//                Button pButton = inviteDialog.getButton(DialogInterface.BUTTON_POSITIVE);
//                pButton.setTextColor(getResources().getColor(R.color.purple_main));
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Uri imageUri;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    dispatchTakePictureIntent(); //Take photo


                break;
            case REQUEST_VIDEO_CAPTURE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    dispatchTakeVideoIntent(); //take video


                break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
