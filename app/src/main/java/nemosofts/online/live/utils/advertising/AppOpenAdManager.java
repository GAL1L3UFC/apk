package nemosofts.online.live.utils.advertising;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAppOpenAd;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener;
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader;
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;

import nemosofts.online.live.callback.Callback;

public class AppOpenAdManager {

    public AppOpenAdManager(Activity activity) {
        switch (Callback.adNetwork) {
            case Callback.AD_TYPE_ADMOB -> {
                AdRequest request = new AdRequest.Builder().build();
                AppOpenAd.load(activity, Callback.admobOpenAdID, request, new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        ad.show(activity);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    }
                });
            }
            case Callback.AD_TYPE_YANDEX -> {
                AppOpenAdLoader appOpenAdLoader = new AppOpenAdLoader(activity);
                AdRequestConfiguration adRequestConfiguration = new AdRequestConfiguration.Builder(Callback.yandexOpenAdID).build();
                AppOpenAdLoadListener appOpenAdLoadListener = new AppOpenAdLoadListener() {
                    @Override
                    public void onAdLoaded(@NonNull com.yandex.mobile.ads.appopenad.AppOpenAd appOpenAd) {
                        appOpenAd.show(activity);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull final AdRequestError adRequestError) {
                    }
                };
                appOpenAdLoader.setAdLoadListener(appOpenAdLoadListener);
                appOpenAdLoader.loadAd(adRequestConfiguration);
            }
            case Callback.AD_TYPE_WORTISE -> {
                com.wortise.ads.appopen.AppOpenAd mAppOpenAdWortise = new com.wortise.ads.appopen.AppOpenAd(activity, Callback.wortiseOpenAdID);
                mAppOpenAdWortise.setListener(new com.wortise.ads.appopen.AppOpenAd.Listener() {
                    @Override
                    public void onAppOpenClicked(@NonNull com.wortise.ads.appopen.AppOpenAd appOpenAd) {

                    }

                    @Override
                    public void onAppOpenDismissed(@NonNull com.wortise.ads.appopen.AppOpenAd appOpenAd) {

                    }

                    @Override
                    public void onAppOpenFailedToLoad(@NonNull com.wortise.ads.appopen.AppOpenAd appOpenAd, @NonNull com.wortise.ads.AdError adError) {

                    }

                    @Override
                    public void onAppOpenFailedToShow(@NonNull com.wortise.ads.appopen.AppOpenAd appOpenAd, @NonNull com.wortise.ads.AdError adError) {

                    }

                    @Override
                    public void onAppOpenImpression(@NonNull com.wortise.ads.appopen.AppOpenAd appOpenAd) {

                    }

                    @Override
                    public void onAppOpenLoaded(@NonNull com.wortise.ads.appopen.AppOpenAd appOpenAd) {
                        appOpenAd.showAd(activity);
                    }

                    @Override
                    public void onAppOpenShown(@NonNull com.wortise.ads.appopen.AppOpenAd appOpenAd) {

                    }
                });
                mAppOpenAdWortise.loadAd();
            }
            case Callback.AD_TYPE_APPLOVIN -> {
                MaxAppOpenAd maxAppOpenAd = new MaxAppOpenAd(Callback.applovinOpenAdID, activity);
                maxAppOpenAd.setListener(new MaxAdListener() {
                    @Override
                    public void onAdLoaded(@NonNull MaxAd maxAd) {
                        maxAppOpenAd.showAd();
                    }

                    @Override
                    public void onAdDisplayed(@NonNull MaxAd maxAd) {

                    }

                    @Override
                    public void onAdHidden(@NonNull MaxAd maxAd) {

                    }

                    @Override
                    public void onAdClicked(@NonNull MaxAd maxAd) {

                    }

                    @Override
                    public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
                    }

                    @Override
                    public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {

                    }
                });
                maxAppOpenAd.loadAd();
            }
            case Callback.AD_TYPE_STARTAPP -> {
                StartAppAd startAppAd = new StartAppAd(activity);
                startAppAd.loadAd(new AdEventListener() {
                    @Override
                    public void onReceiveAd(@NonNull Ad ad) {
                        startAppAd.showAd();
                    }

                    @Override
                    public void onFailedToReceiveAd(@Nullable Ad ad) {

                    }
                });
            }
        }
    }
}