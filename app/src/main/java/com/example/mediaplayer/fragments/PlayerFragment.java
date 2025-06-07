package com.example.mediaplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mediaplayer.R;

public class PlayerFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        TextView infoText = view.findViewById(R.id.playerInfo);
        infoText.setText("Select a media file from Local Files or IPTV tabs to start playing.\n\n" +
                "Supported formats:\n" +
                "• Video: MP4, AVI, MKV, MOV, 3GP\n" +
                "• Audio: MP3, AAC, FLAC, OGG\n" +
                "• Streams: HTTP, HTTPS, RTMP, RTSP\n\n" +
                "The player will open in full-screen mode with playback controls.");

        return view;
    }
}