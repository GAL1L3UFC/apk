package nemosofts.online.live.asyncTask;

import android.content.Context;
import android.os.AsyncTask;


import androidx.nemosofts.Envato;

import org.json.JSONArray;
import org.json.JSONObject;

import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.interfaces.AboutListener;
import nemosofts.online.live.item.ItemAbout;
import nemosofts.online.live.utils.ApplicationUtil;
import nemosofts.online.live.utils.helper.Helper;
import nemosofts.online.live.utils.SharedPref;

public class LoadAbout extends AsyncTask<String, String, String> {

    private final Envato envato;
    private final Helper helper;
    private final SharedPref sharedPref;
    private final AboutListener aboutListener;
    private String verifyStatus = "0", message = "";

    public LoadAbout(Context context, AboutListener aboutListener) {
        this.aboutListener = aboutListener;
        helper = new Helper(context);
        sharedPref = new SharedPref(context);
        envato = new Envato(context);
    }

    @Override
    protected void onPreExecute() {
        aboutListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, helper.getAPIRequest(Callback.METHOD_APP_DETAILS, 0, "", "", "", "", sharedPref.getUserId(), "", "", "", "","","","", null));
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray(Callback.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);

                if (!c.has(Callback.TAG_SUCCESS)) {
                    // App Details
                    String app_email = c.getString("app_email");
                    String app_author = c.getString("app_author");
                    String app_contact = c.getString("app_contact");
                    String app_website = c.getString("app_website");
                    String app_description = c.getString("app_description");
                    String app_developed_by = c.getString("app_developed_by");
                    Callback.itemAbout = new ItemAbout(app_email,app_author,app_contact,app_website,app_description,app_developed_by);

                    // Envato
                    String apikey = c.getString("envato_api_key");
                    if (!apikey.isEmpty()){
                        envato.setEnvatoKEY(apikey);
                    }

                    // API Latest Limit
                    if(!c.getString("api_latest_limit").equals("")) {
                        Callback.recentLimit = Integer.parseInt(c.getString("api_latest_limit"));
                    }

                    // is
                    Callback.isRTL = Boolean.parseBoolean(c.getString("isRTL"));
                    Callback.isVPN = Boolean.parseBoolean(c.getString("isVPN"));
                    Callback.isAPK = Boolean.parseBoolean(c.getString("isAPK"));
                    Callback.isMaintenance = Boolean.parseBoolean(c.getString("isMaintenance"));
                    Callback.isScreenshot = Boolean.parseBoolean(c.getString("isScreenshot"));
                    Callback.isLogin = Boolean.parseBoolean(c.getString("isLogin"));
                    Callback.isGoogleLogin = Boolean.parseBoolean(c.getString("isGoogleLogin"));

                    // AppUpdate
                    Callback.isAppUpdate = Boolean.parseBoolean(c.getString("app_update_status"));
                    if(!c.getString("app_new_version").equals("")) {
                        Callback.app_new_version = Integer.parseInt(c.getString("app_new_version"));
                    }
                    Callback.app_update_desc = c.getString("app_update_desc");
                    Callback.app_redirect_url = c.getString("app_redirect_url");

                    Callback.moreApps = c.getString("more_apps_url");

                    // Ads Network ---------------------------------------------------------
                    Callback.isAdsStatus = Boolean.parseBoolean(c.getString("ad_status"));
                    // PRIMARY ADS
                    String adNetwork = c.getString("ad_network");
                    Callback.adNetwork = adNetwork;
                    switch (adNetwork) {
                        case Callback.AD_TYPE_ADMOB -> {
                            Callback.admobPublisherID = c.getString("admob_publisher_id");
                            Callback.admobBannerAdID = c.getString("admob_banner_unit_id");
                            Callback.admobInterstitialAdID = c.getString("admob_interstitial_unit_id");
                            Callback.admobNativeAdID = c.getString("admob_native_unit_id");
                            Callback.admobOpenAdID = c.getString("admob_app_open_ad_unit_id");
                        }
                        case Callback.AD_TYPE_STARTAPP ->
                                Callback.startappAppID = c.getString("startapp_app_id");
                        case Callback.AD_TYPE_UNITY -> {
                            Callback.unityGameID = c.getString("unity_game_id");
                            Callback.unityBannerAdID = c.getString("unity_banner_placement_id");
                            Callback.unityInterstitialAdID = c.getString("unity_interstitial_placement_id");
                        }
                        case Callback.AD_TYPE_APPLOVIN -> {
                            Callback.applovinBannerAdID = c.getString("applovin_banner_ad_unit_id");
                            Callback.applovinInterstitialAdID = c.getString("applovin_interstitial_ad_unit_id");
                            Callback.applovinNativeAdID = c.getString("applovin_native_ad_manual_unit_id");
                            Callback.applovinOpenAdID = c.getString("applovin_app_open_ad_unit_id");
                        }
                        case Callback.AD_TYPE_IRONSOURCE ->
                                Callback.ironsourceAppKey = c.getString("ironsource_app_key");
                        case Callback.AD_TYPE_META -> {
                            Callback.mataBannerAdID = c.getString("mata_banner_ad_unit_id");
                            Callback.mataInterstitialAdID = c.getString("mata_interstitial_ad_unit_id");
                            Callback.mataNativeAdID = c.getString("mata_native_ad_manual_unit_id");
                        }
                        case Callback.AD_TYPE_YANDEX -> {
                            Callback.yandexBannerAdID = c.getString("yandex_banner_ad_unit_id");
                            Callback.yandexInterstitialAdID = c.getString("yandex_interstitial_ad_unit_id");
                            Callback.yandexNativeAdID = c.getString("yandex_native_ad_manual_unit_id");
                            Callback.yandexOpenAdID = c.getString("yandex_app_open_ad_unit_id");
                        }
                        case Callback.AD_TYPE_WORTISE -> {
                            Callback.wortiseAppID = c.getString("wortise_app_id");
                            Callback.wortiseBannerAdID = c.getString("wortise_banner_unit_id");
                            Callback.wortiseInterstitialAdID = c.getString("wortise_interstitial_unit_id");
                            Callback.wortiseNativeAdID = c.getString("wortise_native_unit_id");
                            Callback.wortiseOpenAdID = c.getString("wortise_app_open_unit_id");
                        }
                        default -> {
                        }
                    }

                    // ADS PLACEMENT
                    Callback.isOpenAdStart = Boolean.parseBoolean(c.getString("app_open_ad_on_start"));
                    Callback.isOpenAdResume = Boolean.parseBoolean(c.getString("app_open_ad_on_Resume"));
                    Callback.isBannerAdHome = Boolean.parseBoolean(c.getString("banner_home"));
                    Callback.isBannerAdPostDetails = Boolean.parseBoolean(c.getString("banner_post_details"));
                    Callback.isBannerAdCatDetails = Boolean.parseBoolean(c.getString("banner_category_details"));
                    Callback.isBannerAdSearch = Boolean.parseBoolean(c.getString("banner_search"));
                    Callback.isInterAd = Boolean.parseBoolean(c.getString("interstitial_post_list"));
                    Callback.isNativeAdPost = Boolean.parseBoolean(c.getString("native_ad_post_list"));
                    Callback.isNativeAdCat = Boolean.parseBoolean(c.getString("native_ad_category_list"));

                    // GLOBAL CONFIGURATION
                    if(!c.getString("interstital_ad_click").equals("")) {
                        Callback.interstitialAdShow = Integer.parseInt(c.getString("interstital_ad_click"));
                    }
                    if(!c.getString("native_position").equals("")) {
                        Callback.nativeAdShow = Integer.parseInt(c.getString("native_position"));
                    }

                    if(!c.getString("player_package_name").equals("")) {
                        String applicationID = c.getString("player_package_name");
                        sharedPref.setHLSVideoPlayer(applicationID);
                    }

                    Callback.isPurchases = Boolean.parseBoolean(c.getString("isPurchases"));

                } else {
                    verifyStatus = c.getString(Callback.TAG_SUCCESS);
                    message = c.getString(Callback.TAG_MSG);
                }
            }
            return "1";
        } catch (Exception ee) {
            ee.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        aboutListener.onEnd(s, verifyStatus, message);
        super.onPostExecute(s);
    }
}