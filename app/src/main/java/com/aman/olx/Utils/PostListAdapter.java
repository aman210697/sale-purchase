package com.aman.olx.Utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aman.olx.Activities.Search;
import com.aman.olx.Fragments.Account;
import com.aman.olx.Fragments.SearchFragment;
import com.aman.olx.Fragments.WatchList;
import com.aman.olx.Models.Post;
import com.aman.olx.R;

import java.util.ArrayList;

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

    private ArrayList<Post> mPost;
    private Context mContext;

    public PostListAdapter(Context nContext ,ArrayList<Post> mPost) {
        this.mPost = mPost;
        this.mContext = nContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.view_post,parent,false);


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        UniversalImageLoader.setImage(mPost.get(position).getImage(), holder.mPostImage);
        holder.mPriceText.setText(mPost.get(position).getPrice());
        holder.mTitleText.setText(mPost.get(position).getTitle());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //TODO

                //view the post in more detail
                Fragment fragment = (Fragment) ((Search) mContext).getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                                ((Search) mContext).mViewPager.getCurrentItem());
                if (fragment != null) {

                    ///check which frament from which it is being called
                    //SearchFragment (AKA #0)
                    if (fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":0")) {
                      //  Log.d(TAG, "onClick: switching to: " + mContext.getString(R.string.fragment_view_post));

                        SearchFragment searchFragment = (SearchFragment) ((Search) mContext).getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                                        ((Search) mContext).mViewPager.getCurrentItem());

                        searchFragment.viewPost(mPost.get(position).getPost_id());
                    }
//                    //WatchList Fragment (AKA #1)
//                    else
                      if (fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":1")) {
                        //Log.d(TAG, "onClick: switching to: " + mContext.getString(R.string.fragment_watch_list));

                        WatchList watchListFragment = (WatchList) ((Search) mContext).getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                                        ((Search) mContext).mViewPager.getCurrentItem());

                        watchListFragment.viewPost(mPost.get(position).getPost_id());
                    }

                    //WatchList Fragment (AKA #3)
                    if (fragment.getTag().equals("android:switcher:" + R.id.viewpager_container + ":3")) {
                        //Log.d(TAG, "onClick: switching to: " + mContext.getString(R.string.fragment_watch_list));

                       Account accountFragment= (Account) ((Search) mContext).getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" +
                                        ((Search) mContext).mViewPager.getCurrentItem());

                        accountFragment.viewPost(mPost.get(position).getPost_id());
                    }



                }


            }
        });

    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView mPostImage;
        TextView mPriceText, mTitleText;
        CardView cardView;


        public ViewHolder(View itemView/*, ImageView postImage, TextView price, TextView title*/) {
            super(itemView);

            mPostImage= (ImageView) itemView.findViewById(R.id.viewPostImage);
            mPriceText=(TextView)itemView.findViewById(R.id.price_text);
            mTitleText=(TextView)itemView.findViewById(R.id.title_text);
            cardView=(CardView)itemView.findViewById(R.id.row_Cardview);

            int gridthWidth= mContext.getResources().getDisplayMetrics().widthPixels;
            int imageWidth= gridthWidth/3;
            mPostImage.setMaxHeight(imageWidth);
            mPostImage.setMaxWidth(imageWidth);
        }
    }

    public void updateList(ArrayList<Post> list){
        mPost=list;
        notifyDataSetChanged();
    }
}
