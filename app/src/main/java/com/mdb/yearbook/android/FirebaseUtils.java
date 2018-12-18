package com.mdb.yearbook.android;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Radhika on 11/4/17.
 */

public class FirebaseUtils {
    public static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static DatabaseReference groupIdRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("groupIds");
}
