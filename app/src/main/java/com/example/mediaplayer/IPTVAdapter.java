package com.example.mediaplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class IPTVAdapter extends ArrayAdapter<IPTVStream> {
    private List<IPTVStream> streams;

    public IPTVAdapter(Context context, List<IPTVStream> streams) {
        super(context, R.layout.item_iptv_stream, streams);
        this.streams = streams;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_iptv_stream, parent, false);
        }

        IPTVStream stream = streams.get(position);

        TextView nameText = convertView.findViewById(R.id.streamName);
        TextView urlText = convertView.findViewById(R.id.streamUrl);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);

        nameText.setText(stream.getName());
        urlText.setText(stream.getUrl());

        deleteButton.setOnClickListener(v -> {
            streams.remove(position);
            notifyDataSetChanged();
        });

        return convertView;
    }
}
