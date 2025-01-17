package nemosofts.online.live.fragment.player;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import nemosofts.online.live.R;
import nemosofts.online.live.activity.BillingSubscribeActivity;

public class PremiumContentFragment extends Fragment {

    @NonNull
    public static PremiumContentFragment newInstance(String postId, String postType) {
        PremiumContentFragment f = new PremiumContentFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        args.putString("postType", postType);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_premium_content, container, false);

        rootView.findViewById(R.id.btn_subscribe_now).setOnClickListener(view -> {
            Intent intentPlan = new Intent(getActivity(), BillingSubscribeActivity.class);
            intentPlan.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentPlan);
        });

        return rootView;
    }
}
