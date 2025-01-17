package nemosofts.online.live.utils.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.sdk.AppLovinSdk;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.wortise.ads.WortiseSdk;
import com.wortise.ads.banner.BannerAd;
import com.wortise.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;

import nemosofts.online.live.R;
import nemosofts.online.live.activity.SignInActivity;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.interfaces.InterAdListener;
import nemosofts.online.live.utils.ApplicationUtil;
import nemosofts.online.live.utils.SharedPref;
import nemosofts.online.live.utils.advertising.AdManagerInterAdmob;
import nemosofts.online.live.utils.advertising.AdManagerInterApplovin;
import nemosofts.online.live.utils.advertising.AdManagerInterStartApp;
import nemosofts.online.live.utils.advertising.AdManagerInterUnity;
import nemosofts.online.live.utils.advertising.AdManagerInterWortise;
import nemosofts.online.live.utils.advertising.AdManagerInterYandex;
import nemosofts.online.live.utils.advertising.GDPRChecker;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Helper {

    private final Context ctx;
    private InterAdListener interAdListener;

    public Helper(Context ctx) {
        this.ctx = ctx;
    }

    public Helper(Context ctx, InterAdListener interAdListener) {
        this.ctx = ctx;
        this.interAdListener = interAdListener;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public void initializeAds() {
        if (Callback.adNetwork.equals(Callback.AD_TYPE_ADMOB) || Callback.adNetwork.equals(Callback.AD_TYPE_META)) {
            MobileAds.initialize(ctx, initializationStatus -> {
            });
        }
        if (Callback.adNetwork.equals(Callback.AD_TYPE_STARTAPP)) {
            StartAppSDK.init(ctx, Callback.startappAppID, false);
            StartAppAd.disableSplash();
            StartAppSDK.setUserConsent(ctx, "pas", System.currentTimeMillis(), new GDPRChecker(ctx).canLoadAd());
        }
        if (Callback.adNetwork.equals(Callback.AD_TYPE_APPLOVIN) && (!AppLovinSdk.getInstance(ctx).isInitialized())) {
            AppLovinSdk.initializeSdk(ctx);
            AppLovinSdk.getInstance(ctx).setMediationProvider("max");
            AppLovinSdk.getInstance(ctx).getSettings().setTestDeviceAdvertisingIds(Arrays.asList("656822d9-18de-4120-994e-44d4245a4d63", "249d75a2-1ef2-8ff9-8885-c50384843a66"));
        }
        if (Callback.adNetwork.equals(Callback.AD_TYPE_IRONSOURCE)) {
            IronSource.init(ctx, Callback.ironsourceAppKey, () -> {
            });
        }
        if (Callback.adNetwork.equals(Callback.AD_TYPE_UNITY)) {
            UnityAds.initialize(ctx, Callback.unityGameID, true, new IUnityAdsInitializationListener() {
                @Override
                public void onInitializationComplete() {
                }

                @Override
                public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                }
            });
        }
        if (Callback.adNetwork.equals(Callback.AD_TYPE_YANDEX)) {
            com.yandex.mobile.ads.common.MobileAds.initialize(ctx, () -> {
            });
        }
        if (Callback.adNetwork.equals(Callback.AD_TYPE_WORTISE) && !WortiseSdk.isInitialized()) {
            WortiseSdk.initialize(ctx, Callback.wortiseAppID);
        }
    }

    public Object showBannerAd(LinearLayout linearLayout, String page) {
        if (isBannerAd(page)){
            switch (Callback.adNetwork) {
                case Callback.AD_TYPE_ADMOB:
                case Callback.AD_TYPE_META:
                    Bundle extras = new Bundle();
                    AdView adViewAdmob = new AdView(ctx);
                    AdRequest adRequest;
                    if (Callback.adNetwork.equals(Callback.AD_TYPE_ADMOB)) {
                        adRequest = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                .build();
                    } else {
                        adRequest = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                .addNetworkExtrasBundle(FacebookMediationAdapter.class, extras)
                                .build();
                    }
                    adViewAdmob.setAdUnitId(Callback.admobBannerAdID);
                    adViewAdmob.setAdSize(AdSize.BANNER);
                    linearLayout.addView(adViewAdmob);
                    adViewAdmob.loadAd(adRequest);
                case Callback.AD_TYPE_WORTISE:
                    BannerAd mBannerAd = new BannerAd(ctx);
                    mBannerAd.setAdSize(com.wortise.ads.AdSize.HEIGHT_50);
                    mBannerAd.setAdUnitId(Callback.wortiseBannerAdID);
                    linearLayout.addView(mBannerAd);
                    mBannerAd.loadAd();
                    return mBannerAd;
                case Callback.AD_TYPE_STARTAPP:
                    Banner startAppBanner = new Banner(ctx);
                    startAppBanner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    linearLayout.addView(startAppBanner);
                    startAppBanner.loadAd();
                    return startAppBanner;
                case Callback.AD_TYPE_UNITY:
                    BannerView bannerView = new BannerView((Activity) ctx, Callback.unityBannerAdID, new UnityBannerSize(320, 50));
                    linearLayout.addView(bannerView);
                    bannerView.load();
                    return null;
                case Callback.AD_TYPE_APPLOVIN:
                    MaxAdView adView = new MaxAdView(Callback.applovinBannerAdID, ctx);
                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    int heightPx = ctx.getResources().getDimensionPixelSize(R.dimen.banner_height);
                    adView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
                    linearLayout.addView(adView);
                    adView.loadAd();
                    return adView;
                case Callback.AD_TYPE_IRONSOURCE:
                    IronSourceBannerLayout iBannerAd  = IronSource.createBanner((Activity) ctx, ISBannerSize.BANNER);
                    linearLayout.addView(iBannerAd);
                    IronSource.loadBanner(iBannerAd);
                    return iBannerAd;
                case Callback.AD_TYPE_YANDEX:
                    BannerAdView yBannerAd = new BannerAdView(ctx);
                    int width2 = ViewGroup.LayoutParams.MATCH_PARENT;
                    int heightPx2 = ctx.getResources().getDimensionPixelSize(R.dimen.banner_height);
                    yBannerAd.setLayoutParams(new FrameLayout.LayoutParams(width2, heightPx2));
                    yBannerAd.setAdUnitId(Callback.yandexBannerAdID);
                    com.yandex.mobile.ads.common.AdRequest yadRequest = new com.yandex.mobile.ads.common.AdRequest.Builder().build();
                    linearLayout.addView(yBannerAd);
                    yBannerAd.loadAd(yadRequest);
                    return yBannerAd;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public void showInterAd(final int pos, final String type) {
        if (isInterAd()){
            switch (Callback.adNetwork) {
                case Callback.AD_TYPE_ADMOB, Callback.AD_TYPE_META -> {

                    final AdManagerInterAdmob adManagerInterAdmob = new AdManagerInterAdmob(ctx);
                    if (adManagerInterAdmob.getAd() != null) {
                        adManagerInterAdmob.getAd().setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                AdManagerInterAdmob.setAd(null);
                                adManagerInterAdmob.createAd();
                                interAdListener.onClick(pos, type);
                                super.onAdDismissedFullScreenContent();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull @NotNull com.google.android.gms.ads.AdError adError) {
                                AdManagerInterAdmob.setAd(null);
                                adManagerInterAdmob.createAd();
                                interAdListener.onClick(pos, type);
                                super.onAdFailedToShowFullScreenContent(adError);
                            }
                        });
                        adManagerInterAdmob.getAd().show((Activity) ctx);
                    } else {
                        AdManagerInterAdmob.setAd(null);
                        adManagerInterAdmob.createAd();
                        interAdListener.onClick(pos, type);
                    }
                }
                case Callback.AD_TYPE_STARTAPP -> {
                    final AdManagerInterStartApp adManagerInterStartApp = new AdManagerInterStartApp(ctx);
                    if (adManagerInterStartApp.getAd() != null && adManagerInterStartApp.getAd().isReady()) {
                        adManagerInterStartApp.getAd().showAd(new AdDisplayListener() {
                            @Override
                            public void adHidden(Ad ad) {
                                AdManagerInterStartApp.setAd(null);
                                adManagerInterStartApp.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void adDisplayed(Ad ad) {

                            }

                            @Override
                            public void adClicked(Ad ad) {

                            }

                            @Override
                            public void adNotDisplayed(Ad ad) {
                                AdManagerInterStartApp.setAd(null);
                                adManagerInterStartApp.createAd();
                                interAdListener.onClick(pos, type);
                            }
                        });
                    } else {
                        AdManagerInterStartApp.setAd(null);
                        adManagerInterStartApp.createAd();
                        interAdListener.onClick(pos, type);
                    }
                }
                case Callback.AD_TYPE_UNITY -> {
                    final AdManagerInterUnity adManagerInterUnity = new AdManagerInterUnity();
                    if (AdManagerInterUnity.isAdLoaded) {
                        UnityAds.show((Activity) ctx, Callback.unityInterstitialAdID, new IUnityAdsShowListener() {
                            @Override
                            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                                AdManagerInterUnity.setAd();
                                adManagerInterUnity.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onUnityAdsShowStart(String placementId) {

                            }

                            @Override
                            public void onUnityAdsShowClick(String placementId) {

                            }

                            @Override
                            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                                AdManagerInterUnity.setAd();
                                adManagerInterUnity.createAd();
                                interAdListener.onClick(pos, type);
                            }
                        });
                    } else {
                        AdManagerInterUnity.setAd();
                        adManagerInterUnity.createAd();
                        interAdListener.onClick(pos, type);
                    }
                }
                case Callback.AD_TYPE_APPLOVIN -> {
                    final AdManagerInterApplovin adManagerInterApplovin = new AdManagerInterApplovin(ctx);
                    if (adManagerInterApplovin.getAd() != null && adManagerInterApplovin.getAd().isReady()) {
                        adManagerInterApplovin.getAd().setListener(new MaxAdListener() {
                            @Override
                            public void onAdLoaded(@NonNull MaxAd ad) {

                            }

                            @Override
                            public void onAdDisplayed(@NonNull MaxAd ad) {

                            }

                            @Override
                            public void onAdHidden(@NonNull MaxAd ad) {
                                AdManagerInterApplovin.setAd(null);
                                adManagerInterApplovin.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onAdClicked(@NonNull MaxAd ad) {

                            }

                            @Override
                            public void onAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError error) {
                                AdManagerInterApplovin.setAd(null);
                                adManagerInterApplovin.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onAdDisplayFailed(@NonNull MaxAd ad, @NonNull MaxError error) {
                                AdManagerInterApplovin.setAd(null);
                                adManagerInterApplovin.createAd();
                                interAdListener.onClick(pos, type);
                            }
                        });
                        adManagerInterApplovin.getAd().showAd();
                    } else {
                        AdManagerInterStartApp.setAd(null);
                        adManagerInterApplovin.createAd();
                        interAdListener.onClick(pos, type);
                    }
                }
                case Callback.AD_TYPE_IRONSOURCE -> {
                    if (IronSource.isInterstitialReady()) {
                        IronSource.setInterstitialListener(new InterstitialListener() {
                            @Override
                            public void onInterstitialAdReady() {

                            }

                            @Override
                            public void onInterstitialAdLoadFailed(IronSourceError error) {
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onInterstitialAdOpened() {

                            }

                            @Override
                            public void onInterstitialAdClosed() {
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onInterstitialAdShowFailed(IronSourceError error) {
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onInterstitialAdClicked() {
                            }

                            @Override
                            public void onInterstitialAdShowSucceeded() {

                            }
                        });
                        IronSource.showInterstitial();
                    } else {
                        interAdListener.onClick(pos, type);
                    }
                    IronSource.init(ctx, Callback.ironsourceAppKey, IronSource.AD_UNIT.INTERSTITIAL);
                    IronSource.loadInterstitial();
                }
                case Callback.AD_TYPE_YANDEX -> {
                    final AdManagerInterYandex adManagerInterYandex = new AdManagerInterYandex(ctx);
                    if (adManagerInterYandex.getAd() != null) {
                        adManagerInterYandex.getAd().setAdEventListener(new InterstitialAdEventListener() {
                            @Override
                            public void onAdShown() {

                            }

                            @Override
                            public void onAdFailedToShow(@NonNull AdError adError) {
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onAdDismissed() {
                                AdManagerInterYandex.setAd(null);
                                adManagerInterYandex.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onAdClicked() {

                            }

                            @Override
                            public void onAdImpression(@Nullable ImpressionData impressionData) {

                            }
                        });
                        adManagerInterYandex.getAd().show((Activity) ctx);
                    } else {
                        AdManagerInterYandex.setAd(null);
                        adManagerInterYandex.createAd();
                        interAdListener.onClick(pos, type);
                    }
                }
                case Callback.AD_TYPE_WORTISE -> {
                    final AdManagerInterWortise adManagerInterWortise = new AdManagerInterWortise(ctx);
                    if (adManagerInterWortise.getAd() != null && adManagerInterWortise.getAd().isAvailable()) {
                        adManagerInterWortise.getAd().setListener(new InterstitialAd.Listener() {
                            @Override
                            public void onInterstitialShown(@NonNull InterstitialAd interstitialAd) {

                            }

                            @Override
                            public void onInterstitialLoaded(@NonNull InterstitialAd interstitialAd) {

                            }

                            @Override
                            public void onInterstitialImpression(@NonNull InterstitialAd interstitialAd) {

                            }

                            @Override
                            public void onInterstitialFailedToShow(@NonNull InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                                AdManagerInterWortise.setAd(null);
                                adManagerInterWortise.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onInterstitialFailedToLoad(@NonNull InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                                AdManagerInterWortise.setAd(null);
                                adManagerInterWortise.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onInterstitialDismissed(@NonNull InterstitialAd interstitialAd) {
                                AdManagerInterWortise.setAd(null);
                                adManagerInterWortise.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onInterstitialClicked(@NonNull InterstitialAd interstitialAd) {

                            }
                        });
                        adManagerInterWortise.getAd().showAd();
                    } else {
                        AdManagerInterWortise.setAd(null);
                        adManagerInterWortise.createAd();
                        interAdListener.onClick(pos, type);
                    }
                }
                default -> interAdListener.onClick(pos, type);
            }
        } else {
            interAdListener.onClick(pos, type);
        }
    }

    private boolean isInterAd() {
        if (isNetworkAvailable() && Boolean.TRUE.equals(Callback.isInterAd) && Boolean.TRUE.equals(Callback.isAdsStatus) && new GDPRChecker(ctx).canLoadAd()) {
            Callback.adCount = Callback.adCount + 1;
            return Callback.adCount % Callback.interstitialAdShow == 0;
        } else {
            return false;
        }
    }

    private boolean isBannerAd(String page) {
        if (isNetworkAvailable() && Boolean.TRUE.equals(Callback.isAdsStatus) && new GDPRChecker(ctx).canLoadAd()) {
            return switch (page) {
                case Callback.PAGE_HOME -> Callback.isBannerAdHome;
                case Callback.PAGE_POST_DETAILS -> Callback.isBannerAdPostDetails;
                case Callback.PAGE_CAT_DETAILS -> Callback.isBannerAdCatDetails;
                case Callback.PAGE_SEARCH -> Callback.isBannerAdSearch;
                default -> true;
            };
        } else {
            return false;
        }
    }

    public boolean canLoadNativeAds(Context context, String page) {
        if (Boolean.TRUE.equals(Callback.isAdsStatus) && new GDPRChecker(context).canLoadAd()) {
            return switch (page) {
                case Callback.PAGE_NATIVE_POST -> Callback.isNativeAdPost;
                case Callback.PAGE_NATIVE_CAT -> Callback.isNativeAdCat;
                default -> true;
            };
        } else {
            return false;
        }
    }

    public void clickLogin() {
        SharedPref sharePref = new SharedPref(ctx);
        if (sharePref.isLogged()) {
            logout((Activity) ctx, sharePref);
            Toast.makeText(ctx, ctx.getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(ctx, SignInActivity.class);
            intent.putExtra("from", "app");
            ctx.startActivity(intent);
        }
    }

    public void logout(Activity activity, SharedPref sharePref) {
        if (sharePref.getLoginType().equals(Callback.LOGIN_TYPE_GOOGLE)) {
            FirebaseAuth.getInstance().signOut();
        }
        sharePref.setIsAutoLogin(false);
        sharePref.setIsLogged(false);
        sharePref.setLoginDetails("", "", "", "", "", "", "", false, "", Callback.LOGIN_TYPE_NORMAL);
        Intent intent1 = new Intent(ctx, SignInActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.putExtra("from", "");
        ctx.startActivity(intent1);
        activity.finish();
    }

    public RequestBody getAPIRequest(String helper_name, int page, String itemID, String catID, String searchText, String reportMessage, String userID, String name, String email, String mobile, String gender, String password, String authID, String loginType, File file) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd' 'HH:mm:ss").create();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(gson);
        jsObj.addProperty("helper_name", helper_name);
        jsObj.addProperty("application_id", ctx.getPackageName());

        if (Callback.METHOD_APP_DETAILS.equals(helper_name)){
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_LOGIN.equals(helper_name)){
            jsObj.addProperty("user_email", email);
            jsObj.addProperty("user_password", password);
            jsObj.addProperty("auth_id", authID);
            jsObj.addProperty("type", loginType);
        } else if (Callback.METHOD_REGISTER.equals(helper_name)){
            jsObj.addProperty("user_name", name);
            jsObj.addProperty("user_email", email);
            jsObj.addProperty("user_phone", mobile);
            jsObj.addProperty("user_gender", gender);
            jsObj.addProperty("user_password", password);
            jsObj.addProperty("auth_id", authID);
            jsObj.addProperty("type", loginType);
        } else if (Callback.METHOD_PROFILE.equals(helper_name)) {
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_ACCOUNT_DELETE.equals(helper_name)) {
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_EDIT_PROFILE.equals(helper_name)){
            jsObj.addProperty("user_id", userID);
            jsObj.addProperty("user_name", name);
            jsObj.addProperty("user_email", email);
            jsObj.addProperty("user_phone", mobile);
            jsObj.addProperty("user_password", password);
        } else if (Callback.METHOD_USER_IMAGES_UPDATE.equals(helper_name)){
            jsObj.addProperty("user_id", userID);
            jsObj.addProperty("type", loginType);
        } else if (Callback.METHOD_FORGOT_PASSWORD.equals(helper_name)){
            jsObj.addProperty("user_email", email);
        } else if (Callback.METHOD_NOTIFICATION.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_REMOVE_NOTIFICATION.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_REPORT.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("user_id", userID);
            jsObj.addProperty("report_title", searchText);
            jsObj.addProperty("report_msg", reportMessage);
        } else if (Callback.METHOD_GET_RATINGS.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("device_id", userID);
        } else if (Callback.METHOD_RATINGS.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("device_id", userID);
            jsObj.addProperty("rate", authID);
            jsObj.addProperty("message", reportMessage);
        }

        else if (Callback.METHOD_HOME.equals(helper_name)) {
            jsObj.addProperty("user_id", userID);
            if(!itemID.equals("")) {
                jsObj.addProperty("post_ids", itemID);
            }
        } else if (Callback.METHOD_HOME_DETAILS.equals(helper_name)) {
            jsObj.addProperty("id", itemID);
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_LATEST.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Callback.METHOD_MOST_VIEWED.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Callback.METHOD_CAT.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("search_text", searchText);
            jsObj.addProperty("search_type", loginType);
        } else if (Callback.METHOD_LIVE_RECENT.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("post_ids", itemID);
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_LIVE_ID.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_CAT_ID.equals(helper_name)){
            jsObj.addProperty("cat_id", catID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Callback.METHOD_POST_BY_FAV.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_POST_BY_BANNER.equals(helper_name)){
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("post_id", itemID);
        } else if (Callback.METHOD_DO_FAV.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_EVENT.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
        }

        else if (Callback.METHOD_SEARCH_LIVE.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("search_text", searchText);
        } else if (Callback.METHOD_SEARCH.equals(helper_name)) {
            jsObj.addProperty("search_text", searchText);
        }

        else if (Callback.METHOD_SUGGESTION.equals(helper_name)) {
            jsObj.addProperty("user_id", userID);
            jsObj.addProperty("suggest_title", searchText);
            jsObj.addProperty("suggest_message", reportMessage);
        }

        else if (Callback.TRANSACTION_URL.equals(helper_name)){
            jsObj.addProperty("planId", itemID);
            jsObj.addProperty("planName", catID);
            jsObj.addProperty("planPrice", searchText);
            jsObj.addProperty("planDuration", reportMessage);
            jsObj.addProperty("planCurrencyCode", name);
            jsObj.addProperty("user_id", userID);
        }

        switch (helper_name) {
            case Callback.METHOD_REGISTER, Callback.METHOD_SUGGESTION, Callback.METHOD_USER_IMAGES_UPDATE -> {
                final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                if (file != null) {
                    builder.addFormDataPart("image_data", file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file));
                }
                return builder.addFormDataPart("data", ApplicationUtil.toBase64(jsObj.toString())).build();
            }
            default -> {
                return new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("data", ApplicationUtil.toBase64(jsObj.toString()))
                        .build();
            }
        }
    }

    @SuppressLint("Range")
    public String getPathImage(Uri uri) {
        try {
            String filePath = "";
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Images.Media.DATA};

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = ctx.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }

            cursor.close();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            if (uri == null) {
                return null;
            }
            // try to retrieve the image from the media store first
            // this will only work for images selected from gallery
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = ctx.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String returnn = cursor.getString(column_index);
                cursor.close();

                if (returnn == null) {
                    String path = null, image_id = null;
                    Cursor cursor2 = ctx.getContentResolver().query(uri, null, null, null, null);
                    if (cursor2 != null) {
                        cursor2.moveToFirst();
                        image_id = cursor2.getString(0);
                        image_id = image_id.substring(image_id.lastIndexOf(":") + 1);
                        cursor2.close();
                    }

                    Cursor cursor3 = ctx.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);
                    if (cursor3 != null) {
                        cursor3.moveToFirst();
                        path = cursor3.getString(cursor3.getColumnIndex(MediaStore.Images.Media.DATA));
                        cursor3.close();
                    }
                    return path;
                }
                return returnn;
            }
            // this is our fallback here
            return uri.getPath();
        }
    }
}
