package nemosofts.online.live.fragment.player;

import android.content.Intent;
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
import nemosofts.online.live.activity.EmbeddedYoutubePlayerActivity;

public class EmbeddedYoutubeFragment extends Fragment {

    @NonNull
    public static EmbeddedYoutubeFragment newInstance(String streamUrl, boolean isPlayVisible) {
        EmbeddedYoutubeFragment f = new EmbeddedYoutubeFragment();
        Bundle args = new Bundle();
        args.putString("streamUrl", streamUrl);
        args.putBoolean("isPlayVisible", isPlayVisible);
        f.setArguments(args);
        return f;
    }

    private String streamUrl;
    private boolean isPlayVisible;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_embedded_image, container, false);

        if (getArguments() != null) {
            streamUrl = getArguments().getString("streamUrl");
            isPlayVisible = getArguments().getBoolean("isPlayVisible");
        }

        ImageView imageCover = rootView.findViewById(R.id.imageCover);
        ImageView imagePlay = rootView.findViewById(R.id.imagePlay);

        imagePlay.setVisibility(isPlayVisible ? View.VISIBLE : View.GONE);

        String YOUTUBE_IMAGE_FRONT="https://img.youtube.com/vi/";
        String YOUTUBE_SMALL_IMAGE_BACK="/hqdefault.jpg";
        String image = YOUTUBE_IMAGE_FRONT + streamUrl + YOUTUBE_SMALL_IMAGE_BACK;

        Picasso.get().load(image).into(imageCover);

        imagePlay.setOnClickListener(v -> {
            if (!streamUrl.isEmpty()) {
                Intent intent = new Intent(getActivity(), EmbeddedYoutubePlayerActivity.class);
                intent.putExtra("streamUrl", streamUrl);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), getString(R.string.stream_not_found), Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}
