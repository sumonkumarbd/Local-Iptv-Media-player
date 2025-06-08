package com.example.mediaplayer.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mediaplayer.IPTVFragment;
import com.example.mediaplayer.fragments.LiveTVFragment;
import com.example.mediaplayer.fragments.MoviesFragment;
import com.example.mediaplayer.fragments.SeriesFragment;

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
}