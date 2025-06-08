package com.example.mediaplayer;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class LocalFilesFragment extends Fragment {
    private ListView listView;
    private List<MediaFile> mediaFiles;
    private MediaFileAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_files, container, false);

        listView = view.findViewById(R.id.listView);
        mediaFiles = new ArrayList<>();

        loadMediaFiles();

        adapter = new MediaFileAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, v, position, id) -> {
            MediaFile file = mediaFiles.get(position);
            playMedia(file);
        });

        return view;
    }

    private void loadMediaFiles() {
        mediaFiles.clear();

        // Load video files
        String[] videoProjection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION
        };

        Cursor videoCursor = getActivity().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                MediaStore.Video.Media.DISPLAY_NAME + " ASC"
        );

        if (videoCursor != null) {
            while (videoCursor.moveToNext()) {
                String name = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                String path = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                long duration = videoCursor.getLong(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));

                mediaFiles.add(new MediaFile(name, path, "Video", duration));
            }
            videoCursor.close();
        }

        // Load audio files
        String[] audioProjection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        Cursor audioCursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                audioProjection,
                null,
                null,
                MediaStore.Audio.Media.DISPLAY_NAME + " ASC"
        );

        if (audioCursor != null) {
            while (audioCursor.moveToNext()) {
                String name = audioCursor.getString(audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String path = audioCursor.getString(audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                long duration = audioCursor.getLong(audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                mediaFiles.add(new MediaFile(name, path, "Audio", duration));
            }
            audioCursor.close();
        }
    }

    private void playMedia(MediaFile file) {
        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra("media_path", file.getPath());
        intent.putExtra("media_title", file.getName());
        startActivity(intent);
    }

    public void refreshFiles() {
        loadMediaFiles();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private class MediaFileAdapter extends ArrayAdapter<MediaFile> {
        public MediaFileAdapter() {
            super(getActivity(), R.layout.item_media_file, mediaFiles);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_media_file, parent, false);
            }

            MediaFile file = mediaFiles.get(position);

            TextView nameText = convertView.findViewById(R.id.fileName);
            TextView typeText = convertView.findViewById(R.id.fileType);
            TextView durationText = convertView.findViewById(R.id.fileDuration);

            nameText.setText(file.getName());
            typeText.setText(file.getType());
            durationText.setText(formatDuration(file.getDuration()));

            return convertView;
        }
    }

    private String formatDuration(long duration) {
        if (duration <= 0) return "Unknown";

        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            return String.format("%02d:%02d", minutes, seconds % 60);
        }
    }
}