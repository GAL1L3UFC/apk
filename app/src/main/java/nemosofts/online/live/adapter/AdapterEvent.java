package nemosofts.online.live.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.nemosofts.view.RoundedImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.wortise.ads.AdError;
import com.wortise.ads.natives.GoogleNativeAd;

import java.util.ArrayList;
import java.util.List;

import nemosofts.online.live.R;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.item.ItemEvent;
import nemosofts.online.live.utils.ApplicationUtil;

public class AdapterEvent extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private List<ItemEvent> arrayList;
    private final List<ItemEvent> filteredArrayList;
    private final RecyclerItemClickListener listener;
    private NameFilter filter;
    private final int VIEW_PROG = -1;

    Boolean isAdLoaded = false;
    List<NativeAd> mNativeAdsAdmob = new ArrayList<>();

    public AdapterEvent(Context context, List<ItemEvent> arrayList, RecyclerItemClickListener clickListener) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        this.context = context;
        this.listener = clickListener;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final RoundedImageView iv_team_one, iv_team_two;
        private final TextView tv_event_title, tv_team_one, tv_team_two, tv_event_time, tv_event_date;
        private final LinearLayout ll_event_list;
        private final RelativeLayout rl_native_ad;

        MyViewHolder(View view) {
            super(view);
            tv_event_title = view.findViewById(R.id.tv_event_title);
            tv_event_date = view.findViewById(R.id.tv_event_date);
            tv_event_time = view.findViewById(R.id.tv_event_time);

            tv_team_one = view.findViewById(R.id.tv_team_one);
            iv_team_one = view.findViewById(R.id.iv_team_one);

            tv_team_two = view.findViewById(R.id.tv_team_two);
            iv_team_two = view.findViewById(R.id.iv_team_two);
            ll_event_list= view.findViewById(R.id.ll_event_list);


            rl_native_ad = view.findViewById(R.id.rl_native_ad);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        @SuppressLint("StaticFieldLeak")
        private static ProgressBar progressBar;
        private ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_PROG) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_progressbar, parent, false);
            return new ProgressViewHolder(v);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_event, parent, false);
            return new MyViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof MyViewHolder) {

            ((MyViewHolder) holder).tv_event_title.setText(arrayList.get(position).getTitle());
            ((MyViewHolder) holder).tv_event_date.setText(arrayList.get(position).getDate());
            ((MyViewHolder) holder).tv_event_time.setText(arrayList.get(position).getTime());

            ((MyViewHolder) holder).tv_team_one.setText(arrayList.get(position).getTitleOne());
            Picasso.get()
                    .load(arrayList.get(position).getThumbOne())
                    .centerCrop()
                    .resize(300,300)
                    .placeholder(R.drawable.material_design_default)
                    .into(((MyViewHolder) holder).iv_team_one);

            ((MyViewHolder) holder).tv_team_two.setText(arrayList.get(position).getTitleTwo());
            Picasso.get()
                    .load(arrayList.get(position).getThumbTwo())
                    .centerCrop()
                    .resize(300,300)
                    .placeholder(R.drawable.material_design_default)
                    .into(((MyViewHolder) holder).iv_team_two);

            ((MyViewHolder) holder).ll_event_list.setOnClickListener(view -> listener.onClick(getPosition(arrayList.get(holder.getAdapterPosition()).getId())));

            if (Boolean.TRUE.equals(Callback.isAdsStatus && Callback.isNativeAdPost && (position != arrayList.size() - 1) && (position + 1) % Callback.nativeAdShow == 0) && !Callback.isPurchases) {
                try {
                    if (((MyViewHolder) holder).rl_native_ad.getChildCount() == 0) {
                        switch (Callback.adNetwork) {
                            case Callback.AD_TYPE_ADMOB, Callback.AD_TYPE_META -> {
                                if (Boolean.TRUE.equals(isAdLoaded) && (mNativeAdsAdmob.size() >= 5)) {

                                    int i = ApplicationUtil.getRandom(mNativeAdsAdmob.size() - 1);

                                    @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                                    populateUnifiedNativeAdView(mNativeAdsAdmob.get(i), adView);
                                    ((MyViewHolder) holder).rl_native_ad.removeAllViews();
                                    ((MyViewHolder) holder).rl_native_ad.addView(adView);

                                    ((MyViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                }
                            }
                            case Callback.AD_TYPE_STARTAPP -> {
                                StartAppNativeAd nativeAd = new StartAppNativeAd(context);
                                nativeAd.loadAd(new NativeAdPreferences().setAdsNumber(1).setAutoBitmapDownload(true).setPrimaryImageSize(2), new AdEventListener() {
                                    @Override
                                    public void onReceiveAd(@NonNull Ad ad) {
                                        try {
                                            if (!nativeAd.getNativeAds().isEmpty()) {
                                                @SuppressLint("InflateParams") RelativeLayout nativeAdView = (RelativeLayout) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_startapp, null);

                                                ImageView icon = nativeAdView.findViewById(R.id.icon);
                                                TextView title = nativeAdView.findViewById(R.id.title);
                                                TextView description = nativeAdView.findViewById(R.id.description);
                                                Button button = nativeAdView.findViewById(R.id.button);

                                                Picasso.get()
                                                        .load(nativeAd.getNativeAds().get(0).getImageUrl())
                                                        .into(icon);
                                                title.setText(nativeAd.getNativeAds().get(0).getTitle());
                                                description.setText(nativeAd.getNativeAds().get(0).getDescription());
                                                button.setText(nativeAd.getNativeAds().get(0).isApp() ? "Install" : "Open");

                                                ((MyViewHolder) holder).rl_native_ad.removeAllViews();
                                                ((MyViewHolder) holder).rl_native_ad.addView(nativeAdView);
                                                ((MyViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailedToReceiveAd(Ad ad) {
                                    }
                                });
                            }
                            case Callback.AD_TYPE_APPLOVIN -> {
                                MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(Callback.applovinNativeAdID, context);
                                nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                                    @Override
                                    public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, @NonNull final MaxAd ad) {
                                        ((MyViewHolder) holder).rl_native_ad.removeAllViews();
                                        ((MyViewHolder) holder).rl_native_ad.addView(nativeAdView);
                                        ((MyViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onNativeAdLoadFailed(@NonNull final String adUnitId, @NonNull final MaxError error) {
                                    }

                                    @Override
                                    public void onNativeAdClicked(@NonNull final MaxAd ad) {
                                    }
                                });

                                nativeAdLoader.loadAd();
                            }
                            case Callback.AD_TYPE_WORTISE -> {
                                GoogleNativeAd googleNativeAd = new GoogleNativeAd(context, Callback.wortiseNativeAdID, new GoogleNativeAd.Listener() {

                                    @Override
                                    public void onNativeFailedToLoad(@NonNull GoogleNativeAd googleNativeAd, @NonNull AdError adError) {
                                    }

                                    @Override
                                    public void onNativeClicked(@NonNull GoogleNativeAd googleNativeAd) {
                                    }

                                    @Override
                                    public void onNativeImpression(@NonNull GoogleNativeAd googleNativeAd) {
                                    }

                                    @Override
                                    public void onNativeLoaded(@NonNull GoogleNativeAd googleNativeAd, @NonNull NativeAd nativeAd) {
                                        @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                                        populateUnifiedNativeAdView(nativeAd, adView);
                                        ((MyViewHolder) holder).rl_native_ad.removeAllViews();
                                        ((MyViewHolder) holder).rl_native_ad.addView(adView);

                                        ((MyViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                    }
                                });
                                googleNativeAd.load();
                            }
                            default -> {
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    public void hideHeader() {
        try {
            ProgressViewHolder.progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isHeader(int position) {
        return position == arrayList.size();
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (arrayList.get(position) != null) {
            return position;
        } else {
            return VIEW_PROG;
        }
    }

    private int getPosition(String id) {
        int count = 0;
        for (int i = 0; i < filteredArrayList.size(); i++) {
            if (id.equals(filteredArrayList.get(i).getId())) {
                count = i;
                break;
            }
        }
        return count;
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new NameFilter();
        }
        return filter;
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<ItemEvent> filteredItems = new ArrayList<>();

                for (int i = 0, l = filteredArrayList.size(); i < l; i++) {
                    String nameList = filteredArrayList.get(i).getTitle();
                    if (nameList.toLowerCase().contains(constraint))
                        filteredItems.add(filteredArrayList.get(i));
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.values = filteredArrayList;
                    result.count = filteredArrayList.size();
                }
            }
            return result;
        }

        @SuppressLint("NotifyDataSetChanged")
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            arrayList = (ArrayList<ItemEvent>) results.values;
            notifyDataSetChanged();
        }
    }

    public void addAds(List<NativeAd> arrayListNativeAds) {
        isAdLoaded = true;
        mNativeAdsAdmob.addAll(arrayListNativeAds);
        for (int i = 0; i < arrayList.size(); i++) {
            if(arrayList.get(i) == null) {
                notifyItemChanged(i);
            }
        }
    }

    private void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);
    }

    public interface RecyclerItemClickListener{
        void onClick(int position);
    }

}