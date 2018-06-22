package com.aman.olx.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.aman.olx.Activities.Login;
import com.aman.olx.Models.Post;
import com.aman.olx.R;
import com.aman.olx.Utils.PostListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Aman Bansal on 05-06-2018.
 */

public class Account extends Fragment {

    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private Button mSignOutButton;

    private static final String TAG = "AccountFragment";
    private static final int NUM_GRID_COLUMNS = 2;
    private static final int GRID_ITEM_MARGIN = 5;

    //widgets
    private RecyclerView mRecyclerView;
    private FrameLayout mFrameLayout;

    //vars
    private PostListAdapter mAdapter;
    private ArrayList<Post> mPosts;
    private ArrayList<String> mPostsIds;
    private DatabaseReference mReference;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_account,container,false);

        mSignOutButton=(Button)view.findViewById(R.id.signout_button);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.account_recycler);
        mFrameLayout = (FrameLayout) view.findViewById(R.id.account_container);

        init();
        setupFirebaseListener();
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

            }
        });


        return view;
    }


    private void init(){
        Log.d(TAG, "init: initializing.");
        mPosts = new ArrayList<>();
        setupPostsList();

        //reference for listening when items are added or removed from the watch list
        mReference = FirebaseDatabase.getInstance().getReference()
               // .child(getString(R.string.node_watch_list))
                .child(getString(R.string.node_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.node_posts));

        //set the listener to the reference
        mReference.addValueEventListener(mLisenter);

    }

    private void getMyPosts(){
        Log.d(TAG, "getMyPosts: getting users' posts");
        if(mPosts != null){
            mPosts.clear();
        }
        if(mPostsIds != null){
            mPostsIds.clear();
        }

        mPostsIds = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.node_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.node_posts))
                .orderByKey();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){

                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        String id = snapshot.child(getString(R.string.field_post_id)).getValue().toString();
                        Log.d(TAG, "onDataChange: found a post id: " + id);
                        mPostsIds.add(id);
                    }
                    getPosts();
                }else{
                    getPosts();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPosts(){
        if(mPostsIds.size() > 0){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            for(int i  = 0; i < mPostsIds.size(); i++){
                Log.d(TAG, "getPosts: getting post information for: " + mPostsIds.get(i));

                Query query = reference.child(getString(R.string.node_posts))
                        .orderByKey()
                        .equalTo(mPostsIds.get(i));

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                        Post post = singleSnapshot.getValue(Post.class);
                        Log.d(TAG, "onDataChange: found a post: " + post.getTitle());
                        mPosts.add(post);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }else{
            mAdapter.notifyDataSetChanged(); //still need to notify the adapter if the list is empty
        }
    }

    private void setupPostsList(){
//        RecyclerViewMargin itemDecorator = new RecyclerViewMargin(GRID_ITEM_MARGIN, NUM_GRID_COLUMNS);
//        mRecyclerView.addItemDecoration(itemDecorator);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), NUM_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new PostListAdapter(getActivity(), mPosts);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void viewPost(String postId){
        ViewPostFragment fragment = new ViewPostFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        Bundle args = new Bundle();
        args.putString(getString(R.string.arg_post_id), postId);
        fragment.setArguments(args);

        transaction.replace(R.id.account_container, fragment, getString(R.string.fragment_view_post));
        transaction.addToBackStack(getString(R.string.fragment_view_post));
        transaction.commit();

        mFrameLayout.setVisibility(View.VISIBLE);
    }

    private void setupFirebaseListener(){
        mAuthStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser();
                if(user!=null){

                }else{

                    Intent intent= new Intent(getActivity(), Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }
            }
        };
    }

    ValueEventListener mLisenter = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: a change was made to this users watch lits node.");
            getMyPosts();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}

