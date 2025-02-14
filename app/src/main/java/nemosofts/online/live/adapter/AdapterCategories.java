package nemosofts.online.live.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.Objects;
import java.util.Random;

import nemosofts.online.live.R;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.item.ItemCat;
import nemosofts.online.live.utils.ApplicationUtil;
import nemosofts.online.live.utils.helper.Helper;

public class AdapterCategories extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Helper helper;
    Context context;
    List<ItemCat> arrayList;
    int columnWidth;
    private final RecyclerItemClickListener listener;

    final int VIEW_PROG = -1;
    final int VIEW_ADS = -2;
    Boolean isAdLoaded = false;
    List<NativeAd> mNativeAdsAdmob = new ArrayList<>();

    public AdapterCategories(Context context, List<ItemCat> arrayList, RecyclerItemClickListener listener) {
        this.context = context;
        this.arrayList = arrayList;
        this.listener = listener;
        helper = new Helper(context);
        columnWidth = ApplicationUtil.getColumnWidth(3, 0, context);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout rl_live_tv;
        private final RoundedImageView iv_live_tv;
        private final TextView title;

        MyViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.tv_title);
            rl_live_tv = view.findViewById(R.id.rl_live_tv);
            iv_live_tv = view.findViewById(R.id.iv_live_tv);
        }
    }

    private static class ADViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rl_native_ad;
        boolean isAdRequested = false;

        private ADViewHolder(View view) {
            super(view);
            rl_native_ad = view.findViewById(R.id.rl_native_ad);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
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
        } else if (viewType == VIEW_ADS) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_native_ad, parent, false);
            return new ADViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_cat, parent, false);
            return new MyViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {

            ((MyViewHolder) holder).title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getName());
            ((MyViewHolder) holder).iv_live_tv.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth));

            Picasso.get()
                    .load(arrayList.get(holder.getAbsoluteAdapterPosition()).getImage())
                    .centerCrop()
                    .resize(300,300)
                    .placeholder(R.drawable.material_design_default)
                    .into(((MyViewHolder) holder).iv_live_tv);

            ((MyViewHolder) holder).rl_live_tv.setOnClickListener(v -> listener.onClickListener(arrayList.get(holder.getAbsoluteAdapterPosition()),holder.getAbsoluteAdapterPosition()));

        } else if (holder instanceof ADViewHolder) {
            if (((ADViewHolder) holder).rl_native_ad.getChildCount() == 0) {
                switch (Callback.adNetwork) {
                    case Callback.AD_TYPE_ADMOB, Callback.AD_TYPE_META -> {
                        if (isAdLoaded) {
                            if (mNativeAdsAdmob.size() >= 5) {

                                int i = new Random().nextInt(mNativeAdsAdmob.size() - 1);

                                @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                                populateUnifiedNativeAdView(mNativeAdsAdmob.get(i), adView);
                                ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                ((ADViewHolder) holder).rl_native_ad.addView(adView);

                                ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    case Callback.AD_TYPE_STARTAPP -> {
                        if (!((ADViewHolder) holder).isAdRequested) {
                            StartAppNativeAd nativeAd = new StartAppNativeAd(context);

                            nativeAd.loadAd(new NativeAdPreferences()
                                    .setAdsNumber(1)
                                    .setAutoBitmapDownload(true)
                                    .setPrimaryImageSize(2), new AdEventListener() {
                                @Override
                                public void onReceiveAd(@NonNull Ad ad) {
                                    try {
                                        if (nativeAd.getNativeAds().size() > 0) {
                                            RelativeLayout nativeAdView = (RelativeLayout) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_startapp, null);

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

                                            ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                            ((ADViewHolder) holder).rl_native_ad.addView(nativeAdView);
                                            ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailedToReceiveAd(Ad ad) {
                                    ((ADViewHolder) holder).isAdRequested = false;
                                }
                            });
                            ((ADViewHolder) holder).isAdRequested = true;
                        }
                    }
                    case Callback.AD_TYPE_APPLOVIN -> {
                        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(Callback.applovinNativeAdID, context);
                        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                            @Override
                            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, @NonNull final MaxAd ad) {
                                ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                ((ADViewHolder) holder).rl_native_ad.addView(nativeAdView);
                                ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
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
                        if (!((ADViewHolder) holder).isAdRequested) {
                            GoogleNativeAd googleNativeAd = new GoogleNativeAd(
                                    context, Callback.wortiseNativeAdID, new GoogleNativeAd.Listener() {
                                @Override
                                public void onNativeClicked(@NonNull GoogleNativeAd googleNativeAd) {

                                }

                                @Override
                                public void onNativeFailedToLoad(@NonNull GoogleNativeAd googleNativeAd, @NonNull AdError adError) {

                                }

                                @Override
                                public void onNativeImpression(@NonNull GoogleNativeAd googleNativeAd) {

                                }

                                @Override
                                public void onNativeLoaded(@NonNull GoogleNativeAd googleNativeAd, @NonNull NativeAd nativeAd) {
                                    NativeAdView adView = (NativeAdView) ((Activity) context).getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                                    populateUnifiedNativeAdView(nativeAd, adView);
                                    ((ADViewHolder) holder).rl_native_ad.removeAllViews();
                                    ((ADViewHolder) holder).rl_native_ad.addView(adView);

                                    ((ADViewHolder) holder).rl_native_ad.setVisibility(View.VISIBLE);
                                }
                            });
                            googleNativeAd.load();
                            ((ADViewHolder) holder).isAdRequested = true;
                        }
                    }
                }
            }
        } else {
            if (getItemCount() <= 10) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
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
        return arrayList.size() + 1;
    }


    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return VIEW_PROG;
        } else if (arrayList.get(position) == null) {
            return VIEW_ADS;
        } else {
            return position;
        }
    }

    public ItemCat getItem(int pos) {
        return arrayList.get(pos);
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
        ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
        } else {
            ((ImageView) Objects.requireNonNull(adView.getIconView())).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            Objects.requireNonNull(adView.getStarRatingView()).setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) Objects.requireNonNull(adView.getStarRatingView()))
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            Objects.requireNonNull(adView.getAdvertiserView()).setVisibility(View.INVISIBLE);
        } else {
            ((TextView) Objects.requireNonNull(adView.getAdvertiserView())).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);
    }

    public interface RecyclerItemClickListener{
        void onClickListener(ItemCat itemCat, int position);
    }
}