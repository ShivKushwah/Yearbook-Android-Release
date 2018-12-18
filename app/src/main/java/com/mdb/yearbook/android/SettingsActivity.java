package com.mdb.yearbook.android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = this;

        ((Button) findViewById(R.id.addGroupsButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

                final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                final GroupsAdapter groupsAdapter;
                final ArrayList<Group> groupsList;

                LayoutInflater inviter = LayoutInflater.from(SettingsActivity.this);
                View inviteView = inviter.inflate(R.layout.invite_to_group, null);

                android.app.AlertDialog.Builder inviteDialogBuilder = new android.app.AlertDialog.Builder(SettingsActivity.this);

                final EditText inviteEmailText = (EditText)inviteView.findViewById(R.id.addMemberToGroup);

                inviteDialogBuilder.setView(inviteView);

                inviteDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Add to Group",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {

                                        final String email = inviteEmailText.getText().toString().trim();

                                        mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                try {
                                                    YearbookActivity.currentGroup = ((ArrayList<String>) dataSnapshot.getValue()).get(0);


                                                    mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (email!=null && !email.equals("")) {
                                                                boolean found = false;
                                                                for (final DataSnapshot user : dataSnapshot.getChildren()) {
                                                                    HashMap<String, Object> userMap = (HashMap<String, Object>) user.getValue();
                                                                    if (((String) userMap.get("email")).equalsIgnoreCase(email))
                                                                    {
                                                                        ArrayList<String> userGroups = (ArrayList<String>) userMap.get("groupIds");
                                                                        userGroups.add(YearbookActivity.currentGroup);

                                                                        final User newUser = new User((String)userMap.get("name"), (String)userMap.get("email"), "", userGroups);
                                                                        mDatabase.child("Users").child(user.getKey()).setValue(newUser);

                                                                        mDatabase.child("Groups").child(YearbookActivity.currentGroup).child("memberIds").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                ArrayList<String> ids = (ArrayList<String>) dataSnapshot.getValue();
                                                                                ids.add(user.getKey());
                                                                                mDatabase.child("Groups").child(YearbookActivity.currentGroup).child("memberIds").setValue(ids);
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                        Toast.makeText(SettingsActivity.this, (String)userMap.get("name") + " has been Added!", Toast.LENGTH_SHORT).show();
                                                                        found = true;
                                                                        break;
                                                                    }
                                                                }
                                                                if (!found)
                                                                {
                                                                    Toast.makeText(SettingsActivity.this, "User could not be Found!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }else
                                                            {
                                                                Toast.makeText(SettingsActivity.this, "Please enter a valid e-mail!", Toast.LENGTH_SHORT).show();
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

                                    }})
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }});

                android.app.AlertDialog inviteDialog = inviteDialogBuilder.create();
                inviteDialog.show();
                Button nButton = inviteDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                nButton.setTextColor(getResources().getColor(R.color.purple_main));
                Button pButton = inviteDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                pButton.setTextColor(getResources().getColor(R.color.purple_main));

            }
        });

        ((Button) findViewById(R.id.changeGroupButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

                final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                final GroupsAdapter groupsAdapter;
                final ArrayList<Group> groupsList;
                LayoutInflater groupManager = LayoutInflater.from(SettingsActivity.this);
                View groupsView = groupManager.inflate(R.layout.group_management, null);
                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(SettingsActivity.this);

                RecyclerView groupsRecyclerView = (RecyclerView)groupsView.findViewById(R.id.groupsRecyclerView);
                groupsRecyclerView.setLayoutManager(new LinearLayoutManager(SettingsActivity.this));
                groupsList = new ArrayList<>();

                alertDialogBuilder.setView(groupsView);

                alertDialogBuilder
                        .setCancelable(true);

                final android.app.AlertDialog alertDialog = alertDialogBuilder.create();

                groupsAdapter = new GroupsAdapter(SettingsActivity.this, groupsList, mDatabase, mAuth, alertDialog);
                groupsRecyclerView.setAdapter(groupsAdapter);

                FloatingActionButton addGroupButton = (FloatingActionButton)groupsView.findViewById(R.id.addGroupButton);

                mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        groupsList.clear();
                        for (String s: (ArrayList<String>) dataSnapshot.getValue())
                        {
                            mDatabase.child("Groups").child(s).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Group g = dataSnapshot.getValue(Group.class);
                                    if (g!= null) {
                                        if (g.getPhotoIds() == null) {
                                            g.setPhotoIds(new ArrayList<String>());
                                        }
                                        groupsList.add(g);
                                    }

                                    groupsAdapter.updateList(groupsList);
                                    groupsAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                addGroupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LayoutInflater groupManager = LayoutInflater.from(SettingsActivity.this);
                        View newGroupView = groupManager.inflate(R.layout.group_add_layout, null);
                        android.app.AlertDialog.Builder newGroupAlertDialogBuilder = new android.app.AlertDialog.Builder(SettingsActivity.this);

                        final EditText newGroupName = (EditText)newGroupView.findViewById(R.id.newGroupNameInput);

                        newGroupAlertDialogBuilder.setView(newGroupView);

                        newGroupAlertDialogBuilder
                                .setCancelable(false)
                                .setPositiveButton("Confirm",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,int id) {


                                                final String groupKey = mDatabase.child("Groups").push().getKey();

                                                String name = newGroupName.getText().toString().trim();

                                                if (name != null && name.length()>0) {

                                                    try
                                                    {
                                                        final int MISSES = 3;

                                                        ArrayList<String> newGroupMembers = new ArrayList<String>();
                                                        newGroupMembers.add(mAuth.getCurrentUser().getUid());
                                                        Long firstDate = System.currentTimeMillis();

                                                        ArrayList<String> adminIDs = new ArrayList<>();
                                                        adminIDs.add(mAuth.getCurrentUser().getUid());

                                                        Group newGroup = new Group(firstDate, name, mAuth.getCurrentUser().getUid(), adminIDs, 0, newGroupMembers,
                                                                new ArrayList<String>(), MISSES, null, "Personal Album", new HashMap<String, Long>(), -1, -1);

                                                        mDatabase.child("Groups").child(groupKey).setValue(newGroup);

                                                        mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                ArrayList<String> currentGroupIds = (ArrayList<String>) dataSnapshot.getValue();
                                                                ArrayList<String> newGroupIds = new ArrayList<String>();

                                                                newGroupIds.add(groupKey);

                                                                for (String groupId : currentGroupIds) {
                                                                    newGroupIds.add(groupId);
                                                                }

                                                                mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds").setValue(newGroupIds);
                                                                Toast.makeText(SettingsActivity.this, "Group Created!", Toast.LENGTH_SHORT).show();

                                                                alertDialog.cancel();
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });



                                                    }catch(Exception e)
                                                    {
                                                        Toast.makeText(SettingsActivity.this, "Enter a valid name and Tolerance!", Toast.LENGTH_SHORT).show();
                                                    }

                                                }else
                                                {
                                                    Toast.makeText(SettingsActivity.this, "Enter a valid group name!", Toast.LENGTH_SHORT).show();
                                                }

                                            }})
                                .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,int id) {
                                                dialog.cancel();
                                            }});

                        android.app.AlertDialog newGroupAlertDialog = newGroupAlertDialogBuilder.create();
                        newGroupAlertDialog.show();

                        Button nButton = newGroupAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        nButton.setTextColor(getResources().getColor(R.color.purple_main));
                        Button pButton = newGroupAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        pButton.setTextColor(getResources().getColor(R.color.purple_main));

                    }
                });

                alertDialog.show();
            }
        });

        ((Button) findViewById(R.id.logoutButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingsActivity.context.startActivity(new Intent(SettingsActivity.context, LoginActivity.class));
                finish();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();

            }
        });
    }


}
