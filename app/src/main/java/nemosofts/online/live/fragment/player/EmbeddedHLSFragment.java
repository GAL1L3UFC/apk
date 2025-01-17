package nemosofts.online.live.fragment.player;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import nemosofts.online.live.R;
import nemosofts.online.live.utils.SharedPref;

public class EmbeddedHLSFragment extends Fragment {

    @NonNull
    public static EmbeddedHLSFragment newInstance(String streamUrl, String imageCover, String player_type, String streamName, String userAgentName, boolean isUserAgent) {
        EmbeddedHLSFragment f = new EmbeddedHLSFragment();
        Bundle args = new Bundle();
        args.putString("streamUrl", streamUrl);
        args.putString("imageCover", imageCover);
        args.putString("player_type", player_type);
        args.putString("streamName", streamName);
        args.putString("userAgent", userAgentName);
        args.putBoolean("isUserAgent", isUserAgent);
        f.setArguments(args);
        return f;
    }

    private String streamUrl, imageUrl, streamName, userAgent, player_type;
    private boolean isUserAgent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_embedded_image, container, false);

        SharedPref sharedPref = new SharedPref(getActivity());

        if (getArguments() != null) {
            streamUrl = getArguments().getString("streamUrl");
            imageUrl = getArguments().getString("imageCover");

            streamName = getArguments().getString("streamName");
            userAgent = getArguments().getString("userAgent");
            player_type = getArguments().getString("player_type");
            isUserAgent = getArguments().getBoolean("isUserAgent");
        }

        ImageView imageCover = rootView.findViewById(R.id.imageCover);
        ImageView imagePlay = rootView.findViewById(R.id.imagePlay);

        Picasso.get().load(imageUrl).into(imageCover);

        imagePlay.setOnClickListener(v -> {
            if (!streamUrl.isEmpty()) {
                try {
                    Intent sendIntent = new Intent(Intent.ACTION_SEARCH);
                    sendIntent.setPackage(sharedPref.getHLSVideoPlayer());
                    sendIntent.putExtra("video_title", streamName);
                    sendIntent.putExtra("video_url", streamUrl);
                    if (isUserAgent){
                        sendIntent.putExtra("video_agent", userAgent);
                    } else {
                        sendIntent.putExtra("video_agent", "");
                    }
                    // videoType = normal or youtube or webview
                    sendIntent.putExtra("video_type", player_type);
                    sendIntent.setType("text/plain");
                    sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(sendIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + sharedPref.getHLSVideoPlayer())));
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.stream_not_found), Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}
