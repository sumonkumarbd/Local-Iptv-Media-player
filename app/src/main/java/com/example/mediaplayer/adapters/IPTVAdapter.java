package com.example.mediaplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mediaplayer.R;
import com.example.mediaplayer.models.IPTVStream;
import java.util.List;

public class IPTVAdapter extends ArrayAdapter<IPTVStream> {
    private Context context;
    private List<IPTVStream> streams;

    public IPTVAdapter(Context context, List<IPTVStream> streams) {
        super(context, R.layout.item_iptv_stream, streams);
        this.context = context;
        this.streams = streams;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_iptv_stream, parent, false);
        }

        IPTVStream stream = streams.get(position);

        TextView nameText = convertView.findViewById(R.id.streamName);
        TextView typeText = convertView.findViewById(R.id.streamType);
        ImageView thumbnailImage = convertView.findViewById(R.id.streamThumbnail);

        nameText.setText(stream.getName());
        typeText.setText(stream.getStreamType().toUpperCase());

        // TODO: Load thumbnail if available
        // You can use an image loading library like Glide or Picasso here
        // if (stream.getThumbnailUrl() != null && !stream.getThumbnailUrl().isEmpty())
        // {
        // Glide.with(context).load(stream.getThumbnailUrl()).into(thumbnailImage);
        // }

        return convertView;
    }
}
