package com.mdb.yearbook.android;

import java.util.ArrayList;

/**
 * Created by datarsd1 on 4/4/17.
 */

public class Photo
{

    private String caption;
    private String imageUrl; //NOTE: Use this to obtain the storagebase URL for the image
    private String posterId;
    private ArrayList<String> groupIds;
    private long date; // int
    private String location;
    private ArrayList<String> taggedMembers;
//    private String dateUnix;
    public Photo() {
        this.caption = "";
        this.imageUrl = "";
        this.posterId = "";
        this.groupIds = new ArrayList<>();
        this.date = 0;
        this.location = "";
        this.taggedMembers = new ArrayList<>();
    }

    public Photo(String caption, String imageUrl, String posterId, ArrayList<String> groupIds, long date, String location)
    {
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.posterId = posterId;
        this.groupIds = groupIds;
        this.date = date;
        this.location = location;
//        this.dateUnix = dateUnix;
    }

    public Photo(String caption, String imageUrl, String posterId, ArrayList<String> groupIds, long date, String location, ArrayList<String> taggedMembers)
    {
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.posterId = posterId;
        this.groupIds = groupIds;
        this.date = date;
        this.location = location;
//        this.dateUnix = dateUnix;
        this.taggedMembers  = taggedMembers;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    public ArrayList<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(ArrayList<String> groupIds) {
        this.groupIds = groupIds;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<String> getTaggedMembers() {
        return taggedMembers;
    }

    public void setTaggedMembers(ArrayList<String> taggedMembers) {
        this.taggedMembers = taggedMembers;
    }

//    public String getDateUnix() {
//        return dateUnix;
//    }
//
//    public void setDateUnix(String dateUnix) {
//        this.dateUnix = dateUnix;
//    }
}
