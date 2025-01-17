package nemosofts.online.live.callback;

import java.io.Serializable;

import nemosofts.online.live.BuildConfig;
import nemosofts.online.live.item.ItemAbout;

public class Callback implements Serializable {

    private static final long serialVersionUID = 1L;

    // API URL
    public static String API_URL = BuildConfig.BASE_URL+"api.php";

    // TAG_API
    public static final String TAG_ROOT = BuildConfig.API_NAME;
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_MSG = "MSG";

    // Method
    public static final String METHOD_APP_DETAILS = "app_details";
    public static final String METHOD_LOGIN = "user_login";
    public static final String METHOD_REGISTER = "user_register";
    public static final String METHOD_PROFILE = "user_profile";
    public static final String METHOD_ACCOUNT_DELETE = "account_delete";
    public static final String METHOD_EDIT_PROFILE = "edit_profile";
    public static final String METHOD_USER_IMAGES_UPDATE = "user_images_update";
    public static final String METHOD_FORGOT_PASSWORD = "forgot_pass";
    public static final String METHOD_NOTIFICATION = "get_notification";
    public static final String METHOD_REMOVE_NOTIFICATION = "remove_notification";
    public static final String METHOD_DO_FAV = "favourite_post";
    public static final String METHOD_SEARCH_LIVE = "get_search_live";
    public static final String METHOD_SEARCH = "get_search";

    public static final String METHOD_REPORT = "post_report";
    public static final String METHOD_GET_RATINGS = "get_rating";
    public static final String METHOD_RATINGS = "post_rating";
    public static final String METHOD_SUGGESTION = "post_suggest";
    public static final String METHOD_LIVE_ID = "get_live_id";
    public static final String METHOD_CAT_ID = "get_cat_by";
    public static final String METHOD_POST_BY_FAV = "get_favourite";
    public static final String METHOD_POST_BY_BANNER = "get_banner_by";
    public static final String METHOD_EVENT = "get_event";

    public static final String METHOD_PLAN = "subscription_list";
    public static final String TRANSACTION_URL = "transaction";

    public static final String METHOD_HOME = "get_home";
    public static final String METHOD_HOME_DETAILS = "home_collections";
    public static final String METHOD_LATEST = "get_latest";
    public static final String METHOD_MOST_VIEWED = "get_trending";
    public static final String METHOD_CAT = "cat_list";
    public static final String METHOD_LIVE_RECENT = "get_recent";

    public static Boolean isProfileUpdate = false;

    public static final String LOGIN_TYPE_NORMAL = "Normal";
    public static final String LOGIN_TYPE_GOOGLE = "Google";

    // About Details
    public static ItemAbout itemAbout = new ItemAbout("","","","","","");

    public static int recentLimit = 10;

    public static Boolean isRTL = false, isVPN = false, isAPK = false, isMaintenance = false,
            isScreenshot = false, isLogin = false, isGoogleLogin = false, isPurchases = false;

    public static Boolean isAppUpdate = false;
    public static int app_new_version = 1;
    public static String app_update_desc = "", app_redirect_url = "";

    public static final String DIALOG_TYPE_UPDATE = "upgrade", DIALOG_TYPE_MAINTENANCE = "maintenance",
            DIALOG_TYPE_DEVELOPER = "developer", DIALOG_TYPE_VPN = "vpn";

    public static String search_item = "";

    public static final String
            AD_TYPE_ADMOB = "admob",
            AD_TYPE_STARTAPP = "startapp",
            AD_TYPE_UNITY = "unity",
            AD_TYPE_APPLOVIN = "applovin",
            AD_TYPE_IRONSOURCE = "ironsource",
            AD_TYPE_META = "meta",
            AD_TYPE_YANDEX = "yandex",
            AD_TYPE_WORTISE = "wortise";

    public static String adNetwork = "admob";
    public static Boolean isAdsStatus = false;

    public static int adCount = 0;
    public static int interstitialAdShow = 5, nativeAdShow = 6;

    public static String
            admobPublisherID = "",
            admobBannerAdID = "",
            admobInterstitialAdID = "",
            admobNativeAdID = "",
            admobOpenAdID = "";
    public static String startappAppID = "";
    public static String
            unityGameID = "",
            unityBannerAdID = "",
            unityInterstitialAdID = "";
    public static String
            applovinBannerAdID = "",
            applovinInterstitialAdID = "",
            applovinNativeAdID = "",
            applovinOpenAdID = "";
    public static String ironsourceAppKey = "";
    public static String
            mataBannerAdID = "",
            mataInterstitialAdID = "",
            mataNativeAdID = "";
    public static String
            yandexBannerAdID = "",
            yandexInterstitialAdID = "",
            yandexNativeAdID = "",
            yandexOpenAdID = "";
    public static String
            wortiseAppID = "",
            wortiseBannerAdID = "",
            wortiseInterstitialAdID = "",
            wortiseNativeAdID = "",
            wortiseOpenAdID = "";

    public static Boolean
            isOpenAdStartShow = true,
            isOpenAdStart = false,
            isOpenAdResume = false,
            isBannerAdHome = false,
            isBannerAdPostDetails = false,
            isBannerAdCatDetails = false,
            isBannerAdSearch = false,
            isInterAd = false,
            isNativeAdPost = false,
            isNativeAdCat = false;

    public static final String
            PAGE_HOME = "banner_home",
            PAGE_POST_DETAILS = "post_details",
            PAGE_CAT_DETAILS = "category_details",
            PAGE_SEARCH = "search_page",
            PAGE_NATIVE_POST = "post_native",
            PAGE_NATIVE_CAT = "category_native";

    public static String moreApps = "";
}