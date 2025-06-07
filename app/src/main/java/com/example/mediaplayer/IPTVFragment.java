package com.example.mediaplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class IPTVFragment extends Fragment {
    private EditText urlInput;
    private EditText nameInput;
    private EditText passwordInput;
    private Button addButton;
    private ListView streamsList;
    private List<IPTVStream> streams;
    private IPTVAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_i_p_t_v, container, false);

        urlInput = view.findViewById(R.id.urlInput);
        nameInput = view.findViewById(R.id.nameInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        addButton = view.findViewById(R.id.addButton);
        streamsList = view.findViewById(R.id.streamsList);

        streams = new ArrayList<>();
        adapter = new IPTVAdapter(getActivity(), streams);
        streamsList.setAdapter(adapter);

        addButton.setOnClickListener(v -> addStream());

        streamsList.setOnItemClickListener((parent, v, position, id) -> {
            IPTVStream stream = streams.get(position);
//            playStream(stream);
        });

        // Add some example streams (commented out - user should add their own)
        // addExampleStreams();

        return view;
    }

    private void addStream() {
        String url = urlInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (url.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a stream URL", Toast.LENGTH_SHORT).show();
            return;
        }


        if (name.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a stream name", Toast.LENGTH_SHORT).show();
        }

        if (password.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a password", Toast.LENGTH_SHORT).show();
        }

        if (!isValidUrl(url)) {
            Toast.makeText(getActivity(), "Please enter a valid URL", Toast.LENGTH_SHORT).show();
            return;
        }

        IPTVStream stream = new IPTVStream(name, password , url);
        streams.add(stream);
        adapter.notifyDataSetChanged();

        urlInput.setText("");
        nameInput.setText("");
        passwordInput.setText("");

        Toast.makeText(getActivity(), "Stream added successfully", Toast.LENGTH_SHORT).show();
    }

    private boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://") ||
                url.startsWith("rtmp://") || url.startsWith("rtsp://");
    }

    private void playStream(IPTVStream stream) {
        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra("media_path", stream.getUrl());
        intent.putExtra("media_title", stream.getName());
        intent.putExtra("password", stream.getPassword());
        intent.putExtra("is_stream", true);
        startActivity(intent);
    }
}