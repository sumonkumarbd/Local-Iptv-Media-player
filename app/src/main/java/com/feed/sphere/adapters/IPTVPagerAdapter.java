package com.feed.sphere.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.feed.sphere.fragments.IPTVFragment;
import com.feed.sphere.fragments.LiveTVFragment;
import com.feed.sphere.fragments.MoviesFragment;
import com.feed.sphere.fragments.SeriesFragment;

public class IPTVPagerAdapter extends FragmentStateAdapter {
    private final IPTVFragment parentFragment;

    public IPTVPagerAdapter(IPTVFragment fragment) {
        super(fragment);
        this.parentFragment = fragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return LiveTVFragment.newInstance(parentFragment.getIPTVService());
            case 1:
                return MoviesFragment.newInstance(parentFragment.getIPTVService());
            case 2:
                return SeriesFragment.newInstance(parentFragment.getIPTVService());
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public void clearData() {
        // Clear data in each fragment
        for (int i = 0; i < getItemCount(); i++) {
            Fragment fragment = parentFragment.getChildFragmentManager().findFragmentByTag("f" + i);
            if (fragment instanceof LiveTVFragment) {
                ((LiveTVFragment) fragment).clearData();
            } else if (fragment instanceof MoviesFragment) {
                ((MoviesFragment) fragment).clearData();
            } else if (fragment instanceof SeriesFragment) {
                ((SeriesFragment) fragment).clearData();
            }
        }
    }
}