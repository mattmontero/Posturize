package edu.sjsu.posturize.posturize;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;

import edu.sjsu.posturize.posturize.fragments.AnalysisFragment;
import edu.sjsu.posturize.posturize.fragments.GraphFragment;
import edu.sjsu.posturize.posturize.sidenav.SideNavDrawer;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;

/**
 * FragmentActivity for Analysis and Graph
 */

public class MainContentActivity extends FragmentActivity {
    //Number of pages to show
    private static final int NUM_PAGES = 2;
    //Pager widget
    private ViewPager mPager;
    //Pager Adapter
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SideNavDrawer.create(this); //Add SideNavDrawer to activity

        mPager = (ViewPager)findViewById(R.id.viewPager);
        mPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onBackPressed(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else if(mPager.getCurrentItem() == 0){
            GoogleAccountInfo.getInstance().signOut();
            finish();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class SlidePagerAdapter extends FragmentStatePagerAdapter {
        public SlidePagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position){
            switch (position){
                case 0:
                    return AnalysisFragment.newInstance();
                case 1:
                    return GraphFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getCount(){
            return NUM_PAGES;
        }
    }
}
