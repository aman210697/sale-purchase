package com.aman.olx.Activities;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.aman.olx.Fragments.Account;
import com.aman.olx.Fragments.PostFragment;
import com.aman.olx.Fragments.WatchList;
import com.aman.olx.Fragments.SearchFragment;
import com.aman.olx.R;
import com.aman.olx.Utils.SectionPagerAdapter;

public class Search extends AppCompatActivity {


    private TabLayout mTablayout;
    public ViewPager mViewPager;

    public SectionPagerAdapter mPageAdapter;
    private final static int REQUEST_CODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTablayout=(TabLayout)findViewById(R.id.tabs);
        mViewPager=(ViewPager)findViewById((R.id.viewpager_container));



        setupViewPager();

        mTablayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPager(){
        mPageAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mPageAdapter.addFragment(new SearchFragment(), getString(R.string.fragment_search));
        mPageAdapter.addFragment(new WatchList(),getString(R.string.fragment_watch_list));
        mPageAdapter.addFragment(new PostFragment(),getString(R.string.fragment_post));
        mPageAdapter.addFragment(new Account(),getString(R.string.fragment_account));

        mViewPager.setAdapter(mPageAdapter);



    }

    private void verifyPermission(){
        /*
        * ask for permission */

        String[] permission= {android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE};


        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permission[0]) == PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(this.getApplicationContext(), permission[1]) == PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(this.getApplicationContext(), permission[2]) == PackageManager.PERMISSION_GRANTED) {
            setupViewPager();

        }else{
            ActivityCompat.requestPermissions(this,permission,REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        verifyPermission();


    }
}
