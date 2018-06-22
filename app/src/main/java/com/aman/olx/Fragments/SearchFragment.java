package com.aman.olx.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aman.olx.Activities.FilterActivity;
import com.aman.olx.Activities.Search;
import com.aman.olx.Models.Post;
import com.aman.olx.R;
import com.aman.olx.Utils.PostListAdapter;
import com.aman.olx.Utils.SquareImageView;
import com.aman.olx.Utils.UniversalImageLoader;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
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

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final int NUM_GRID_COLUMNS = 2;


    ImageView search_imageview;
    RecyclerView mRecyclerView;
    FrameLayout mFrameLayout;


    private PostListAdapter mAdapter;
    private ArrayList<Post> mPosts;

    private DatabaseReference mReference;

    private EditText searchEditText;

    String city, state, country;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        search_imageview = (ImageView) view.findViewById(R.id.ic_search);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        searchEditText=(EditText)view.findViewById(R.id.input_search) ;
        // mPostList=new ArrayList<>();
        //  mAdapter=new PostListAdapter(getActivity(),mPosts);
        //firebaseDatabaseRef=FirebaseDatabase.getInstance().getReference();
        mFrameLayout = (FrameLayout) view.findViewById(R.id.container);
        //context=getActivity();


        init();

        setupPostList();

        getSearchFilters();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getSearchFilters();
    }

    private void getSearchFilters() {

        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
         country= preferences.getString(getString(R.string.preferences_country),"");
         state=preferences.getString(getString(R.string.preferences_state_province),"");
         city=preferences.getString(getString(R.string.preferences_city),"");
    }


    public void viewPost(String postId) {
        ViewPostFragment fragment = new ViewPostFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        Bundle args = new Bundle();
        args.putString(getString(R.string.arg_post_id), postId);
        fragment.setArguments(args);

        transaction.replace(R.id.container, fragment, getString(R.string.fragment_view_post));
        transaction.addToBackStack(getString(R.string.fragment_view_post));
        transaction.commit();

        mFrameLayout.setVisibility(View.VISIBLE);
    }

    void setupPostList() {

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_GRID_COLUMNS, LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mAdapter = new PostListAdapter(getActivity(), mPosts);
        mRecyclerView.setAdapter(mAdapter);


    }


    ValueEventListener mLisenter = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange: a change was made to this users watch lits node.");
            getPosts();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void getPosts() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


        // Log.d(TAG, "getPosts: getting post information for: " + mPostsIds.get(i));

        Query query = reference.child(getString(R.string.node_posts))
                .orderByKey();


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        Post post = snapshot.getValue(Post.class);
                        Log.d(TAG, "onDataChange: found a post: " + post.getTitle());
                        mPosts.add(post);
                        mAdapter.notifyDataSetChanged();

                    }
                } else {
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void init() {
        search_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), FilterActivity.class));
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String text=editable.toString();

                Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();

                ArrayList<Post> temp = new ArrayList();

                for(Post post:mPosts) {

                    if(post.getCity().toLowerCase().contains(city.toLowerCase())
                            && post.getCountry().toLowerCase().contains(country.toLowerCase())
                            && post.getState_province().toLowerCase().contains(state.toLowerCase())
                            && post.getTitle().toLowerCase().contains(text.toLowerCase())) {

                        temp.add(post);


                    }
                    //update recyclerview
                    mAdapter.updateList(temp);

                }
            }
        });

        Log.d(TAG, "init: initializing.");
        mPosts = new ArrayList<>();


        //reference for listening when items are added or removed from the watch list
        mReference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_posts));

        //set the listener to the reference
        mReference.addValueEventListener(mLisenter);


    }



}





