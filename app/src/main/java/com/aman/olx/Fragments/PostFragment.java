package com.aman.olx.Fragments;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aman.olx.Models.Post;
import com.aman.olx.R;
import com.aman.olx.Utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Aman Bansal on 05-06-2018.
 */

public class PostFragment extends Fragment implements SelectPhotoDialog.OnPhotoSelectedListener{

    //widgets
    private ImageView mPostImage;
    private EditText mTitle, mDescription, mPrice, mCountry, mStateProvince, mCity, mContactEmail;
    private Button mPost;
    private ProgressBar mProgressBar;

    //vars
    private Bitmap mSelectedBitmap;
    private Uri mSelectedUri;
    private byte[] mUploadBytes;
    private double mProgress = 0;

    private Post post;





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_post,container,false);
        mPostImage = view.findViewById(R.id.viewPostImage);
        mTitle = view.findViewById(R.id.input_title);
        mDescription = view.findViewById(R.id.input_description);
        mPrice = view.findViewById(R.id.input_price);
        mCountry = view.findViewById(R.id.input_country);
        mStateProvince = view.findViewById(R.id.input_state_province);
        mCity = view.findViewById(R.id.input_city);
        mContactEmail = view.findViewById(R.id.input_email);
        mPost = view.findViewById(R.id.btn_post);
        mProgressBar =view.findViewById(R.id.progressBar);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        init();




        return view;
    }

    private void init() {

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectPhotoDialog dialog= new SelectPhotoDialog();
                dialog.show( getFragmentManager() ,getString(R.string.dialog_select_photo));
                dialog.setTargetFragment(PostFragment.this,1);

            }
        });

        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!TextUtils.isEmpty(mTitle.getText().toString()) &&
                        !TextUtils.isEmpty(mDescription.getText().toString() )&&
                        !TextUtils.isEmpty(mPrice.getText().toString() )&&
                        !TextUtils.isEmpty(mCountry.getText().toString() )&&
                        !TextUtils.isEmpty(mStateProvince.getText().toString() )&&
                        !TextUtils.isEmpty(mCity.getText().toString() )&&
                        !TextUtils.isEmpty(mContactEmail.getText().toString())
                        ){



                    if(mSelectedBitmap!=null && mSelectedUri==null){

                        uploadNewPhoto(mSelectedBitmap);
                    }

                    else if(mSelectedUri!=null & mSelectedBitmap==null){
                        uploadNewPhoto(mSelectedUri);
                    }

                }
                else{
                    Toast.makeText(getActivity(), "Fill all fields", Toast.LENGTH_SHORT).show();
                }

            }
        });



    }





    private void uploadNewPhoto(Bitmap mSelectedBitmap) {

        ImageCompression compression= new ImageCompression(mSelectedBitmap);
        Uri uri= null;
        compression.execute(uri);

    }





    private void uploadNewPhoto(Uri mSelectedUri) {
        ImageCompression compression= new ImageCompression(null);

        compression.execute(mSelectedUri);
    }





    public class ImageCompression extends AsyncTask<Uri,Integer,byte[]>{
        private Bitmap bitmap;

        public ImageCompression(Bitmap bitmap) {
            if(bitmap!=null) {
                this.bitmap = bitmap;
            }
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getActivity(),"Compressing Image",Toast.LENGTH_SHORT);
            showProgressBar();


        }






        @Override
        protected byte[] doInBackground(Uri... uris) {
            if(bitmap==null){
                try{
                    bitmap=MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),uris[0]);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            byte[] bytearray=null;
            bytearray= getByteArrayfromBitmap(bitmap,100);
            return bytearray;


        }





        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);

            mUploadBytes=bytes;
            hideProgressBar();
            //execute upload task
            executeUploadTask();
        }





        private void executeUploadTask(){
            Toast.makeText(getActivity(),"Uploading Image", Toast.LENGTH_SHORT);

            final String postId= FirebaseDatabase.getInstance().getReference().push().getKey();
            final StorageReference storageReference= FirebaseStorage.getInstance().getReference()
                    .child("post/users"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+
                    "/"+postId+"post_image");

            UploadTask uploadTask =storageReference.putBytes(mUploadBytes);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(getActivity(), "Successfull", Toast.LENGTH_SHORT).show();
                        //insert download url to database
                        Uri firbaseuri= taskSnapshot.getDownloadUrl();
                        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();

                        post = new Post();
                        post.setPost_id(postId);
                        post.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        post.setImage(firbaseuri.toString());
                        post.setPrice(mPrice.getText().toString());
                        post.setTitle(mTitle.getText().toString());
                        post.setDescription(mDescription.getText().toString());
                        post.setCountry(mCountry.getText().toString());
                        post.setState_province(mStateProvince.getText().toString());
                        post.setCity(mCity.getText().toString());
                        post.setContact_email(mContactEmail.getText().toString());

                        databaseReference.child(getString(R.string.node_posts))
                                .child(postId)
                                .setValue(post);

                        databaseReference.child(getString(R.string.node_users))
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(getString(R.string.node_posts))
                                .child(postId)
                                .child(getString(R.string.field_post_id))
                                .setValue(postId);


                       resetFields();




                    }


                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getActivity(),"failed uploading image", Toast.LENGTH_SHORT);
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        double currentPogress = (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());

                        if(currentPogress>15+mProgress) {
                            mProgress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());


                            Toast.makeText(getActivity(), "progress " + mProgress + "%", Toast.LENGTH_SHORT);

                        }
                    }
                });

        }

    }


    public static byte[] getByteArrayfromBitmap(Bitmap bitmap,int quality){
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,quality,stream);
        return stream.toByteArray();
    }



    @Override
    public void getImagePath(Uri imagePath) {

        UniversalImageLoader.setImage(imagePath.toString(),mPostImage);
        // mPostImage.setImageURI(imagePath);
        mSelectedBitmap=null;
        mSelectedUri=imagePath;


    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        mPostImage.setImageBitmap(bitmap);
        mSelectedUri=null;
        mSelectedBitmap=bitmap;

    }


    private void resetFields(){
        UniversalImageLoader.setImage("", mPostImage);
        mTitle.setText("");
        mDescription.setText("");
        mPrice.setText("");
        mCountry.setText("");
        mStateProvince.setText("");
        mCity.setText("");
        mContactEmail.setText("");
    }

    private void showProgressBar(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        mProgressBar.setVisibility(View.GONE);
    }



}
