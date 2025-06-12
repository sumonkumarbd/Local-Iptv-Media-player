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
    private final Fragment[] fragments = new Fragment[3];

    public IPTVPagerAdapter(IPTVFragment fragment) {
        super(fragment);
        this.parentFragment = fragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = LiveTVFragment.newInstance(parentFragment.getIPTVService());
                break;
            case 1:
                fragment = MoviesFragment.newInstance(parentFragment.getIPTVService());
                break;
            case 2:
                fragment = SeriesFragment.newInstance(parentFragment.getIPTVService());
                break;
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
        fragments[position] = fragment;
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId >= 0 && itemId < getItemCount();
    }

    public void clearData() {
        for (int i = 0; i < getItemCount(); i++) {
            Fragment fragment = fragments[i];
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
