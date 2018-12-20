package com.mdb.yearbook.android;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by MEEEE on 4/4/17.
 */

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolderWrapper> {
    //Class for the Photo Feed activity for all photos taken for this user
    //Scrollable photo view
    //Adapter for the fragment

    public static final int VIEW_TYPE_2_IMAGES = 0;
    public static final int VIEW_TYPE_1_IMAGES = 1;

    public static Bitmap currentImageClickedBitmap;
    public static String currentPhotoLink;

    private Context context;
    ArrayList<Photo> photoList;

    public FeedAdapter(Context context, ArrayList<Photo> photoList) {
        this.context = context;
        this.photoList = photoList;
    }

    public ViewHolderWrapper onCreateViewHolder(ViewGroup parent, int viewType) { //creates pattern of 2 1 3 photos per row

        if (viewType == VIEW_TYPE_1_IMAGES) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_row_layout_1, parent, false);
            return new CustomViewHolder(view);

        } else if (viewType == VIEW_TYPE_2_IMAGES) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_row_layout_2, parent, false);
            return new CustomViewHolder2(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_row_layout_3, parent, false);
            return new CustomViewHolder3(view);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolderWrapper holder, int position) {
        //TODO what the heck is this repetition
        if (getItemViewType(position) == VIEW_TYPE_1_IMAGES) { //if 1 photo, load 1 photo
//            ((CustomViewHolder)holder).photo.setVisibility(View.VISIBLE);
            Photo p;
            try {
                p = photoList.get(getStartingArraylistPosition(position));
                ((CustomViewHolder)holder).photoObj = p;
            } catch (Exception e) {
                p = null;
            }

            String url = p.getImageUrl();
            Log.d("this is the url", url);
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            if (url.contains("mp4")) {
                //need to download file, then play it

                //TODO: Display loading bar here
                final ArrayList<File> files = new ArrayList<>();
                File localFile = null;
                try {
                    localFile = File.createTempFile("images", ".mp4");
                    files.add(localFile);

                } catch (Exception e) {

                }
                storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        ((CustomViewHolder)holder).video.setVideoPath(files.get(0).getAbsolutePath());//uri);
                        ((CustomViewHolder)holder).video.start();
                        ((CustomViewHolder)holder).video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.setLooping(true);
                            }
                        });
                        ((CustomViewHolder) holder).photo.setVisibility(View.INVISIBLE);

                    }
                });

//                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
//                {
//                    @Override
//                    public void onSuccess(Uri uri)
//                    {
//                        Uri bro = Uri.parse(uri.toString());
//                        Glide.with(context)
//                                .load(uri)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .into(((CustomViewHolder)holder).photo);
//                        //Uri bro = TaggingActivity.testbro;
//                        ((CustomViewHolder)holder).video.setVideoURI(uri);//uri);
//                        ((CustomViewHolder)holder).video.start();
//                        //((CustomViewHolder) holder).photo.setVisibility(View.INVISIBLE);
//                    }
//
//                });
            } else {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                {
                    @Override
                    public void onSuccess(Uri uri)
                    {
                        Glide.with(context)
                                .load(uri)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(((CustomViewHolder)holder).photo);
                        ((CustomViewHolder)holder).video.setVisibility(View.INVISIBLE);//uri);

                    }

                });
            }


        } else if (getItemViewType(position) == VIEW_TYPE_2_IMAGES) { //if 2 photos, load 2 photos
            ((CustomViewHolder2)holder).photo.setVisibility(View.VISIBLE);
            ((CustomViewHolder2)holder).photo2.setVisibility(View.VISIBLE);
            Photo p = photoList.get(getStartingArraylistPosition(position));
            ((CustomViewHolder2)holder).photoObj = p;
            String url = p.getImageUrl();


            if (url.contains("mp4")) {
                //need to download file, then play it
                final ArrayList<File> files = new ArrayList<>();
                File localFile = null;
                try {
                    localFile = File.createTempFile("images", ".mp4");
                    files.add(localFile);

                } catch (Exception e) {

                }
                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);

                storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        ((CustomViewHolder2) holder).video.setVideoPath(files.get(0).getAbsolutePath());//uri);
                        ((CustomViewHolder2) holder).video.start();
                        ((CustomViewHolder2) holder).video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.setLooping(true);
                            }
                        });
                        ((CustomViewHolder2) holder).photo.setVisibility(View.INVISIBLE);

                    }
                });
            } else {


                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context)
                                .load(uri)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(((CustomViewHolder2) holder).photo);
                    }

                });
            }

            Photo p2;
            try {

                p2 = photoList.get(getStartingArraylistPosition(position) + 1);
                ((CustomViewHolder2)holder).photoObj2 = p2;
                String url2 = p2.getImageUrl();

                Log.d("this is the url", url);


                StorageReference storageRef2 = FirebaseStorage.getInstance().getReferenceFromUrl(url2);
                if (url2.contains("mp4")) {
                    //need to download file, then play it
                    final ArrayList<File> files = new ArrayList<>();
                    File localFile = null;
                    try {
                        localFile = File.createTempFile("images", ".mp4");
                        files.add(localFile);

                    } catch (Exception e) {

                    }

                    storageRef2.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            ((CustomViewHolder2) holder).video2.setVideoPath(files.get(0).getAbsolutePath());//uri);
                            ((CustomViewHolder2) holder).video2.start();
                            ((CustomViewHolder2) holder).video2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.setLooping(true);
                                }
                            });
                            ((CustomViewHolder2) holder).photo2.setVisibility(View.INVISIBLE);

                        }
                    });
                } else {


                    storageRef2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ((CustomViewHolder2) holder).video2.start();

                            Glide.with(context)
                                    .load(uri)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(((CustomViewHolder2) holder).photo2);
                        }

                    });
                }
            } catch (Exception e) {
                ((CustomViewHolder2)holder).photo2.setVisibility(View.INVISIBLE);
                ((CustomViewHolder2) holder).video2.setVisibility(View.INVISIBLE);

            }
        }
        else { //else load 3
            ((CustomViewHolder3)holder).photo.setVisibility(View.VISIBLE);
            ((CustomViewHolder3)holder).photo2.setVisibility(View.VISIBLE);
            ((CustomViewHolder3)holder).photo3.setVisibility(View.VISIBLE);
            Photo p = photoList.get(getStartingArraylistPosition(position));
            ((CustomViewHolder3)holder).photoObj = p;
            String url = p.getImageUrl();

            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            if (url.contains("mp4")) {
                //need to download file, then play it
                final ArrayList<File> files = new ArrayList<>();
                File localFile = null;
                try {
                    localFile = File.createTempFile("images", ".mp4");
                    files.add(localFile);

                } catch (Exception e) {

                }

                storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        ((CustomViewHolder3) holder).video.setVideoPath(files.get(0).getAbsolutePath());//uri);
                        ((CustomViewHolder3) holder).video.start();
                        ((CustomViewHolder3) holder).video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.setLooping(true);
                            }
                        });
                        ((CustomViewHolder3) holder).photo.setVisibility(View.INVISIBLE);

                    }
                });
            } else {


                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context)
                                .load(uri)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(((CustomViewHolder3) holder).photo);
                    }
                });
            }
            Photo p2;
            try {

                p2 = photoList.get(getStartingArraylistPosition(position) + 1);
                ((CustomViewHolder3)holder).photoObj2 = p2;
                String url2 = p2.getImageUrl();

                Log.d("this is the url", url);

                StorageReference storageRef2 = FirebaseStorage.getInstance().getReferenceFromUrl(url2);
                if (url2.contains("mp4")) {
                    //need to download file, then play it
                    final ArrayList<File> files = new ArrayList<>();
                    File localFile = null;
                    try {
                        localFile = File.createTempFile("images", ".mp4");
                        files.add(localFile);

                    } catch (Exception e) {

                    }

                    storageRef2.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            ((CustomViewHolder3) holder).video2.setVideoPath(files.get(0).getAbsolutePath());//uri);
                            ((CustomViewHolder3) holder).video2.start();
                            ((CustomViewHolder3) holder).video2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.setLooping(true);
                                }
                            });
                            ((CustomViewHolder3) holder).photo2.setVisibility(View.INVISIBLE);

                        }
                    });
                } else {


                    storageRef2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(YearbookActivity.context)
                                    .load(uri)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(((CustomViewHolder3) holder).photo2);
                        }

                    });
                }
            } catch (Exception e) {
                ((CustomViewHolder3)holder).photo2.setVisibility(View.INVISIBLE);
                ((CustomViewHolder3) holder).video2.setVisibility(View.INVISIBLE);

            }

            Photo p3;
            try {

                p3 = photoList.get(getStartingArraylistPosition(position) + 2);
                ((CustomViewHolder3)holder).photoObj3 = p3;
                String url2 = p3.getImageUrl();

                Log.d("this is the url", url);


                StorageReference storageRef3 = FirebaseStorage.getInstance().getReferenceFromUrl(url2);
                if (url2.contains("mp4")) {
                    //need to download file, then play it
                    final ArrayList<File> files = new ArrayList<>();
                    File localFile = null;
                    try {
                        localFile = File.createTempFile("images", ".mp4");
                        files.add(localFile);

                    } catch (Exception e) {

                    }

                    storageRef3.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            ((CustomViewHolder3) holder).video3.setVideoPath(files.get(0).getAbsolutePath());//uri);
                            ((CustomViewHolder3) holder).video3.start();
                            ((CustomViewHolder3) holder).video3.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.setLooping(true);
                                }
                            });
                            ((CustomViewHolder3) holder).photo3.setVisibility(View.INVISIBLE);

                        }
                    });
                } else {


                    storageRef3.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(context)
                                    .load(uri)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(((CustomViewHolder3) holder).photo3);
                        }

                    });
                }
            } catch (Exception e) {
                ((CustomViewHolder3)holder).photo3.setVisibility(View.INVISIBLE);
                ((CustomViewHolder3) holder).video3.setVisibility(View.INVISIBLE);

            }
        }

    }

    public int getStartingArraylistPosition(int row) {
        int photoNum = 0;
        while (row / 3 >= 1) {
            photoNum += 6;
            row = row - 3;
        }

        if (row % 3 == 0) { //1st type of row with 2 items
            return photoNum;
        }
        else if (row % 3 == 1) { //2nd type of row with 1 item
            return photoNum + 2;
        }
        else { //last row with 3 items
            return  photoNum + 3;

        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position % 3;
    }

    @Override
    public int getItemCount() {
        int photoCount = photoList.size();
        int cycles = photoCount / 6;
        int remainder = photoCount % 6;
        if (photoCount % 6 == 0) {
            return cycles * 3;
        } else if (remainder == 1 || remainder == 2) {
            return cycles * 3 + 1;
        } else if (remainder == 3) {
            return cycles * 3 + 2;
        } else {
            return cycles * 3 + 3;
        }
    }



    class ViewHolderWrapper extends RecyclerView.ViewHolder  {

        public ViewHolderWrapper(View view) {
            super(view);
        }
    }

    class CustomViewHolder extends ViewHolderWrapper {
        ImageView photo;
        VideoView video;
        Photo photoObj;

        public CustomViewHolder(View view) {
            super(view);
            this.photo = (ImageView) view.findViewById(R.id.image1);
            video = (VideoView) view.findViewById(R.id.video1);
//            this.photo.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    photo.buildDrawingCache();
//                    Bitmap image= photo.getDrawingCache();
//                    currentImageClickedBitmap = image;
//                    currentPhotoLink = photoObj.getImageUrl();
//                    Intent intent = new Intent(YearbookActivity.context, FullScreenPhotoViewActivity.class);
//                    YearbookActivity.context.startActivity(intent);
//
////                    LayoutInflater groupManager = LayoutInflater.from(YearbookActivity.activity);
////                    View groupsView = groupManager.inflate(R.layout.flagging_view, null);
////                    groupsView.findViewById(R.id.flagButton).setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View view) {
////                            Toast.makeText(YearbookActivity.context, "Photo has been reported!", Toast.LENGTH_LONG).show();
////                        }
////                    });
////                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(YearbookActivity.activity);
////
////                    alertDialogBuilder.setView(groupsView);
////                    alertDialogBuilder.setTitle("Flag as Inappropirate");
////                    alertDialogBuilder.show();
//
//                }
//            });
        }
    }

    class CustomViewHolder2 extends ViewHolderWrapper {
        ImageView photo;
        ImageView photo2;
        VideoView video;
        VideoView video2;

        Photo photoObj;
        Photo photoObj2;

        public CustomViewHolder2(View view) {
            super(view);
            this.photo = (ImageView) view.findViewById(R.id.image1);
            this.photo2 = (ImageView) view.findViewById(R.id.image2);
            video = (VideoView) view.findViewById(R.id.video1);
            video2 = (VideoView) view.findViewById(R.id.video2);


            this.photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    photo.buildDrawingCache();
                    Bitmap image= photo.getDrawingCache();
                    currentImageClickedBitmap = image;
                    currentPhotoLink = photoObj.getImageUrl();
                    Intent intent = new Intent(YearbookActivity.context, FullScreenPhotoViewActivity.class);
                    YearbookActivity.context.startActivity(intent);
                }
            });

            this.photo2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    photo2.buildDrawingCache();
                    Bitmap image= photo2.getDrawingCache();
                    currentImageClickedBitmap = image;
                    currentPhotoLink = photoObj2.getImageUrl();
                    Intent intent = new Intent(YearbookActivity.context, FullScreenPhotoViewActivity.class);
                    YearbookActivity.context.startActivity(intent);
                }
            });

        }
    }

    class CustomViewHolder3 extends ViewHolderWrapper {
        ImageView photo;
        ImageView photo2;
        ImageView photo3;

        VideoView video;
        VideoView video2;
        VideoView video3;

        Photo photoObj;
        Photo photoObj2;
        Photo photoObj3;

        public CustomViewHolder3(View view) {
            super(view);

            this.photo = (ImageView) view.findViewById(R.id.image1);
            this.photo2 = (ImageView) view.findViewById(R.id.image2);
            this.photo3 = (ImageView) view.findViewById(R.id.image3);

            video = (VideoView) view.findViewById(R.id.video1);
            video2 = (VideoView) view.findViewById(R.id.video2);
            video3 = (VideoView) view.findViewById(R.id.video3);


            //TODO again
            this.photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    photo.buildDrawingCache();
                    Bitmap image= photo.getDrawingCache();
                    currentImageClickedBitmap = image;
                    currentPhotoLink = photoObj.getImageUrl();
                    Intent intent = new Intent(YearbookActivity.context, FullScreenPhotoViewActivity.class);
                    YearbookActivity.context.startActivity(intent);
                }
            });

            this.photo2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    photo2.buildDrawingCache();
                    Bitmap image= photo2.getDrawingCache();
                    currentImageClickedBitmap = image;
                    currentPhotoLink = photoObj2.getImageUrl();
                    Intent intent = new Intent(YearbookActivity.context, FullScreenPhotoViewActivity.class);
                    YearbookActivity.context.startActivity(intent);
                }
            });

            this.photo3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    photo3.buildDrawingCache();
                    Bitmap image= photo3.getDrawingCache();
                    currentImageClickedBitmap = image;
                    Intent intent = new Intent(YearbookActivity.context, FullScreenPhotoViewActivity.class);
                    YearbookActivity.context.startActivity(intent);
                }
            });

        }
    }

    public void updateList(ArrayList<Photo> newList)
    {
        this.photoList = newList;
    }

}