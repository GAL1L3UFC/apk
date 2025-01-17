package nemosofts.online.live.utils.helper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.item.ItemAbout;
import nemosofts.online.live.item.ItemData;
import nemosofts.online.live.utils.EncryptData;

public class DBHelper extends SQLiteOpenHelper {

    EncryptData encryptData;
    static String DB_NAME = "nemosofts.db";

    SQLiteDatabase db;
    final Context context;

    private static final String TAG_ID = "id";

    // Table Name
    private static final String TABLE_ABOUT = "about";
    private static final String TABLE_RECENT = "recent";

    private static final String TAG_LIVE_ID = "live_id";
    private static final String TAG_TITLE = "live_title";
    private static final String TAG_IMAGE = "image";
    private static final String TAG_PREMIUM = "is_premium";

    private static final String
            TAG_ABOUT_EMAIL = "email",
            TAG_ABOUT_AUTHOR = "author",
            TAG_ABOUT_CONTACT = "contact",
            TAG_ABOUT_WEBSITE = "website",
            TAG_ABOUT_DESC = "description",
            TAG_ABOUT_DEVELOPED = "developed",
            TAG_ABOUT_ENVATO_API_KEY = "envato_key",
            TAG_ABOUT_IS_RTL = "is_rtl",
            TAG_ABOUT_IS_SCREEN = "is_screenshot",
            TAG_ABOUT_IS_GOOGLE = "is_google",
            TAG_ABOUT_IS_LOGIN = "is_login",
            TAG_ABOUT_MORE_APP = "more_apps",
            TAG_ADS_STATUS = "ad_status",
            TAG_ADS_NETWORK = "ad_network",
            TAG_ADS_PUBLISHER_ID = "pub_id",
            TAG_ADS_BANNER_ID = "banner_id",
            TAG_ADS_INTER_ID = "inter_id",
            TAG_ADS_NATIVE_ID = "native_id",
            TAG_ADS_OPEN_ID = "app_open_id",
            TAG_ADS_IS_START = "open_start",
            TAG_ADS_IS_RESUME = "open_resume",
            TAG_ADS_IS_BANNER_HOME = "banner_home",
            TAG_ADS_IS_BANNER_DETAILS = "banner_details",
            TAG_ADS_IS_BANNER_CATEGORY = "banner_category",
            TAG_ADS_IS_BANNER_SEARCH = "banner_search",
            TAG_ADS_IS_INTERS = "interstitial",
            TAG_ADS_IS_NATIVE_POST = "native_post",
            TAG_ADS_IS_NATIVE_CAT = "native_category";

    String[] columns_about = new String[]{TAG_ABOUT_EMAIL, TAG_ABOUT_AUTHOR, TAG_ABOUT_CONTACT, TAG_ABOUT_WEBSITE, TAG_ABOUT_DESC, TAG_ABOUT_DEVELOPED,
            TAG_ABOUT_ENVATO_API_KEY, TAG_ABOUT_IS_RTL, TAG_ABOUT_IS_SCREEN, TAG_ABOUT_IS_GOOGLE, TAG_ABOUT_IS_LOGIN, TAG_ABOUT_MORE_APP,
            TAG_ADS_STATUS, TAG_ADS_NETWORK,
            TAG_ADS_PUBLISHER_ID, TAG_ADS_BANNER_ID, TAG_ADS_INTER_ID, TAG_ADS_NATIVE_ID, TAG_ADS_OPEN_ID,
            TAG_ADS_IS_START, TAG_ADS_IS_RESUME, TAG_ADS_IS_BANNER_HOME, TAG_ADS_IS_BANNER_DETAILS, TAG_ADS_IS_BANNER_CATEGORY, TAG_ADS_IS_BANNER_SEARCH, TAG_ADS_IS_INTERS, TAG_ADS_IS_NATIVE_POST, TAG_ADS_IS_NATIVE_CAT};

    String[] columns_live = new String[]{TAG_ID, TAG_LIVE_ID, TAG_TITLE, TAG_IMAGE, TAG_PREMIUM};

    // Creating table about
    private static final String CREATE_TABLE_ABOUT = "create table " + TABLE_ABOUT + "(" +
            TAG_ABOUT_EMAIL + " TEXT, " +
            TAG_ABOUT_AUTHOR + " TEXT, " +
            TAG_ABOUT_CONTACT + " TEXT, " +
            TAG_ABOUT_WEBSITE + " TEXT, " +
            TAG_ABOUT_DESC + " TEXT, " +
            TAG_ABOUT_DEVELOPED + " TEXT, " +
            TAG_ABOUT_ENVATO_API_KEY + " TEXT, " +
            TAG_ABOUT_IS_RTL + " TEXT, " +
            TAG_ABOUT_IS_SCREEN + " TEXT, " +
            TAG_ABOUT_IS_GOOGLE + " TEXT, " +
            TAG_ABOUT_IS_LOGIN + " TEXT, " +
            TAG_ABOUT_MORE_APP + " TEXT, " +
            TAG_ADS_STATUS + " TEXT, " +
            TAG_ADS_NETWORK + " TEXT, " +
            TAG_ADS_PUBLISHER_ID + " TEXT, " +
            TAG_ADS_BANNER_ID + " TEXT, " +
            TAG_ADS_INTER_ID + " TEXT, " +
            TAG_ADS_NATIVE_ID + " TEXT, " +
            TAG_ADS_OPEN_ID + " TEXT, " +
            TAG_ADS_IS_START + " TEXT, " +
            TAG_ADS_IS_RESUME + " TEXT, " +
            TAG_ADS_IS_BANNER_HOME + " TEXT, " +
            TAG_ADS_IS_BANNER_DETAILS + " TEXT, " +
            TAG_ADS_IS_BANNER_CATEGORY + " TEXT, " +
            TAG_ADS_IS_BANNER_SEARCH + " TEXT, " +
            TAG_ADS_IS_INTERS + " TEXT, " +
            TAG_ADS_IS_NATIVE_POST + " TEXT, " +
            TAG_ADS_IS_NATIVE_CAT + " TEXT);";

    // Creating table query
    private static final String CREATE_TABLE_RECENT = "create table " + TABLE_RECENT + "(" +
            TAG_ID + " integer PRIMARY KEY AUTOINCREMENT," +
            TAG_LIVE_ID + " TEXT," +
            TAG_TITLE + " TEXT," +
            TAG_IMAGE + " TEXT," +
            TAG_PREMIUM + " TEXT);";


    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
        encryptData = new EncryptData(context);
        this.context = context;
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_ABOUT);
            db.execSQL(CREATE_TABLE_RECENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    public void addToRecent(ItemData itemData) {
        Cursor cursor_delete = db.query(TABLE_RECENT, columns_live, null, null, null, null, null);
        if (cursor_delete != null && cursor_delete.getCount() > 20) {
            cursor_delete.moveToFirst();
            db.delete(TABLE_RECENT, TAG_LIVE_ID + "=" + cursor_delete.getString(cursor_delete.getColumnIndex(TAG_LIVE_ID)), null);
        }
        if (cursor_delete != null){
            cursor_delete.close();
        }

        if (checkRecent(itemData.getId())) {
            db.delete(TABLE_RECENT, TAG_LIVE_ID + "=" + itemData.getId(), null);
        }

        String name = itemData.getTitle().replace("'", "%27");
        String imageBig = encryptData.encrypt(itemData.getThumb().replace(" ", "%20"));

        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_LIVE_ID, itemData.getId());
        contentValues.put(TAG_TITLE, name);
        contentValues.put(TAG_IMAGE, imageBig);
        contentValues.put(TAG_PREMIUM, itemData.getIsPremium());

        db.insert(TABLE_RECENT, null, contentValues);
    }

    private Boolean checkRecent(String id) {
        Cursor cursor = db.query(TABLE_RECENT, columns_live, TAG_LIVE_ID + "=" + id, null, null, null, null);
        Boolean isFav = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return isFav;
    }

    @SuppressLint("Range")
    public String getRecentIDs(String limit) {
        String liveIDs = "";
        Cursor cursor = db.query(TABLE_RECENT, new String[]{TAG_LIVE_ID}, null, null, null, null, TAG_ID + " DESC", limit);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            liveIDs = cursor.getString(cursor.getColumnIndex(TAG_LIVE_ID));
            cursor.moveToNext();
            for (int i = 1; i < cursor.getCount(); i++) {
                liveIDs = liveIDs + "," + cursor.getString(cursor.getColumnIndex(TAG_LIVE_ID));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return liveIDs;
    }

    // About ---------------------------------------------------------------------------------------
    public void addtoAbout() {
        try {
            db.delete(TABLE_ABOUT, null, null);

            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_ABOUT_EMAIL, Callback.itemAbout.getEmail());
            contentValues.put(TAG_ABOUT_AUTHOR, Callback.itemAbout.getAuthor());
            contentValues.put(TAG_ABOUT_CONTACT, Callback.itemAbout.getContact());
            contentValues.put(TAG_ABOUT_WEBSITE, Callback.itemAbout.getWebsite());
            contentValues.put(TAG_ABOUT_DESC, Callback.itemAbout.getAppDesc());
            contentValues.put(TAG_ABOUT_DEVELOPED, Callback.itemAbout.getDevelopedBY());

            contentValues.put(TAG_ABOUT_ENVATO_API_KEY, "");

            contentValues.put(TAG_ABOUT_IS_RTL, String.valueOf(Callback.isRTL));
            contentValues.put(TAG_ABOUT_IS_SCREEN, String.valueOf(Callback.isScreenshot));
            contentValues.put(TAG_ABOUT_IS_GOOGLE, String.valueOf(Callback.isGoogleLogin));
            contentValues.put(TAG_ABOUT_IS_LOGIN, String.valueOf(Callback.isLogin));
            contentValues.put(TAG_ABOUT_MORE_APP, Callback.moreApps);

            contentValues.put(TAG_ADS_STATUS, String.valueOf(Callback.isAdsStatus));
            contentValues.put(TAG_ADS_NETWORK, Callback.adNetwork);

            switch (Callback.adNetwork) {
                case Callback.AD_TYPE_ADMOB -> {
                    contentValues.put(TAG_ADS_PUBLISHER_ID, Callback.admobPublisherID);
                    contentValues.put(TAG_ADS_BANNER_ID, Callback.admobBannerAdID);
                    contentValues.put(TAG_ADS_INTER_ID, Callback.admobInterstitialAdID);
                    contentValues.put(TAG_ADS_NATIVE_ID, Callback.admobNativeAdID);
                    contentValues.put(TAG_ADS_OPEN_ID, Callback.admobOpenAdID);
                }
                case Callback.AD_TYPE_STARTAPP -> {
                    contentValues.put(TAG_ADS_PUBLISHER_ID, Callback.startappAppID);
                    contentValues.put(TAG_ADS_BANNER_ID, "");
                    contentValues.put(TAG_ADS_INTER_ID, "");
                    contentValues.put(TAG_ADS_NATIVE_ID, "");
                    contentValues.put(TAG_ADS_OPEN_ID, "");
                }
                case Callback.AD_TYPE_UNITY -> {
                    contentValues.put(TAG_ADS_PUBLISHER_ID, Callback.unityGameID);
                    contentValues.put(TAG_ADS_BANNER_ID, Callback.unityBannerAdID);
                    contentValues.put(TAG_ADS_INTER_ID, Callback.unityInterstitialAdID);
                    contentValues.put(TAG_ADS_NATIVE_ID, "");
                    contentValues.put(TAG_ADS_OPEN_ID, "");
                }
                case Callback.AD_TYPE_APPLOVIN -> {
                    contentValues.put(TAG_ADS_PUBLISHER_ID, "");
                    contentValues.put(TAG_ADS_BANNER_ID, Callback.applovinBannerAdID);
                    contentValues.put(TAG_ADS_INTER_ID, Callback.applovinInterstitialAdID);
                    contentValues.put(TAG_ADS_NATIVE_ID, Callback.applovinNativeAdID);
                    contentValues.put(TAG_ADS_OPEN_ID, Callback.applovinOpenAdID);
                }
                case Callback.AD_TYPE_IRONSOURCE -> {
                    contentValues.put(TAG_ADS_PUBLISHER_ID, Callback.ironsourceAppKey);
                    contentValues.put(TAG_ADS_BANNER_ID, "");
                    contentValues.put(TAG_ADS_INTER_ID, "");
                    contentValues.put(TAG_ADS_NATIVE_ID, "");
                    contentValues.put(TAG_ADS_OPEN_ID, "");
                }
                case Callback.AD_TYPE_META -> {
                    contentValues.put(TAG_ADS_PUBLISHER_ID, "");
                    contentValues.put(TAG_ADS_BANNER_ID, Callback.mataBannerAdID);
                    contentValues.put(TAG_ADS_INTER_ID, Callback.mataInterstitialAdID);
                    contentValues.put(TAG_ADS_NATIVE_ID, Callback.mataNativeAdID);
                    contentValues.put(TAG_ADS_OPEN_ID, "");
                }
                case Callback.AD_TYPE_YANDEX -> {
                    contentValues.put(TAG_ADS_PUBLISHER_ID, "");
                    contentValues.put(TAG_ADS_BANNER_ID, Callback.yandexBannerAdID);
                    contentValues.put(TAG_ADS_INTER_ID, Callback.yandexInterstitialAdID);
                    contentValues.put(TAG_ADS_NATIVE_ID, Callback.yandexNativeAdID);
                    contentValues.put(TAG_ADS_OPEN_ID, Callback.yandexOpenAdID);
                }
                case Callback.AD_TYPE_WORTISE -> {
                    contentValues.put(TAG_ADS_PUBLISHER_ID, Callback.wortiseAppID);
                    contentValues.put(TAG_ADS_BANNER_ID, Callback.wortiseBannerAdID);
                    contentValues.put(TAG_ADS_INTER_ID, Callback.wortiseInterstitialAdID);
                    contentValues.put(TAG_ADS_NATIVE_ID, Callback.wortiseNativeAdID);
                    contentValues.put(TAG_ADS_OPEN_ID, Callback.wortiseOpenAdID);
                }
                default -> {
                }
            }

            contentValues.put(TAG_ADS_IS_START, String.valueOf(Callback.isOpenAdStart));
            contentValues.put(TAG_ADS_IS_RESUME, String.valueOf(Callback.isOpenAdResume));
            contentValues.put(TAG_ADS_IS_BANNER_HOME, String.valueOf(Callback.isBannerAdHome));
            contentValues.put(TAG_ADS_IS_BANNER_DETAILS, String.valueOf(Callback.isBannerAdPostDetails));
            contentValues.put(TAG_ADS_IS_BANNER_CATEGORY, String.valueOf(Callback.isBannerAdCatDetails));
            contentValues.put(TAG_ADS_IS_BANNER_SEARCH, String.valueOf(Callback.isBannerAdSearch));
            contentValues.put(TAG_ADS_IS_INTERS, String.valueOf(Callback.isInterAd));
            contentValues.put(TAG_ADS_IS_NATIVE_POST, String.valueOf(Callback.isNativeAdPost));
            contentValues.put(TAG_ADS_IS_NATIVE_CAT, String.valueOf(Callback.isNativeAdCat));

            db.insert(TABLE_ABOUT, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    public Boolean getAbout() {
        Cursor c = db.query(TABLE_ABOUT, columns_about, null, null, null, null, null);

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {

                String email = c.getString(c.getColumnIndex(TAG_ABOUT_EMAIL));
                String author = c.getString(c.getColumnIndex(TAG_ABOUT_AUTHOR));
                String contact = c.getString(c.getColumnIndex(TAG_ABOUT_CONTACT));
                String website = c.getString(c.getColumnIndex(TAG_ABOUT_WEBSITE));
                String desc = c.getString(c.getColumnIndex(TAG_ABOUT_DESC));
                String developed = c.getString(c.getColumnIndex(TAG_ABOUT_DEVELOPED));
                Callback.itemAbout = new ItemAbout(email, author, contact, website, desc, developed);

                Callback.isRTL = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ABOUT_IS_RTL)));
                Callback.isScreenshot = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ABOUT_IS_SCREEN)));
                Callback.isGoogleLogin = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ABOUT_IS_GOOGLE)));
                Callback.isLogin = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ABOUT_IS_LOGIN)));
                Callback.moreApps =  c.getString(c.getColumnIndex(TAG_ABOUT_MORE_APP));

                Callback.isAdsStatus = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ADS_STATUS)));
                Callback.adNetwork = c.getString(c.getColumnIndex(TAG_ADS_NETWORK));

                String adNetwork = c.getString(c.getColumnIndex(TAG_ADS_NETWORK));
                switch (adNetwork) {
                    case Callback.AD_TYPE_ADMOB -> {
                        Callback.admobPublisherID = c.getString(c.getColumnIndex(TAG_ADS_PUBLISHER_ID));
                        Callback.admobBannerAdID = c.getString(c.getColumnIndex(TAG_ADS_BANNER_ID));
                        Callback.admobInterstitialAdID = c.getString(c.getColumnIndex(TAG_ADS_INTER_ID));
                        Callback.admobNativeAdID = c.getString(c.getColumnIndex(TAG_ADS_NATIVE_ID));
                        Callback.admobOpenAdID = c.getString(c.getColumnIndex(TAG_ADS_OPEN_ID));
                    }
                    case Callback.AD_TYPE_STARTAPP ->
                            Callback.startappAppID = c.getString(c.getColumnIndex(TAG_ADS_PUBLISHER_ID));
                    case Callback.AD_TYPE_UNITY -> {
                        Callback.unityGameID = c.getString(c.getColumnIndex(TAG_ADS_PUBLISHER_ID));
                        Callback.unityBannerAdID = c.getString(c.getColumnIndex(TAG_ADS_BANNER_ID));
                        Callback.unityInterstitialAdID = c.getString(c.getColumnIndex(TAG_ADS_INTER_ID));
                    }
                    case Callback.AD_TYPE_APPLOVIN -> {
                        Callback.applovinBannerAdID = c.getString(c.getColumnIndex(TAG_ADS_BANNER_ID));
                        Callback.applovinInterstitialAdID = c.getString(c.getColumnIndex(TAG_ADS_INTER_ID));
                        Callback.applovinNativeAdID = c.getString(c.getColumnIndex(TAG_ADS_NATIVE_ID));
                        Callback.applovinOpenAdID = c.getString(c.getColumnIndex(TAG_ADS_OPEN_ID));
                    }
                    case Callback.AD_TYPE_IRONSOURCE ->
                            Callback.ironsourceAppKey = c.getString(c.getColumnIndex(TAG_ADS_PUBLISHER_ID));
                    case Callback.AD_TYPE_META -> {
                        Callback.mataBannerAdID = c.getString(c.getColumnIndex(TAG_ADS_BANNER_ID));
                        Callback.mataInterstitialAdID = c.getString(c.getColumnIndex(TAG_ADS_INTER_ID));
                        Callback.mataNativeAdID = c.getString(c.getColumnIndex(TAG_ADS_NATIVE_ID));
                    }
                    case Callback.AD_TYPE_YANDEX -> {
                        Callback.yandexBannerAdID = c.getString(c.getColumnIndex(TAG_ADS_BANNER_ID));
                        Callback.yandexInterstitialAdID = c.getString(c.getColumnIndex(TAG_ADS_INTER_ID));
                        Callback.yandexNativeAdID = c.getString(c.getColumnIndex(TAG_ADS_NATIVE_ID));
                        Callback.yandexOpenAdID = c.getString(c.getColumnIndex(TAG_ADS_OPEN_ID));
                    }
                    case Callback.AD_TYPE_WORTISE -> {
                        Callback.wortiseAppID = c.getString(c.getColumnIndex(TAG_ADS_PUBLISHER_ID));
                        Callback.wortiseBannerAdID = c.getString(c.getColumnIndex(TAG_ADS_BANNER_ID));
                        Callback.wortiseInterstitialAdID = c.getString(c.getColumnIndex(TAG_ADS_INTER_ID));
                        Callback.wortiseNativeAdID = c.getString(c.getColumnIndex(TAG_ADS_NATIVE_ID));
                        Callback.wortiseOpenAdID = c.getString(c.getColumnIndex(TAG_ADS_OPEN_ID));
                    }
                    default -> {
                    }
                }

                Callback.isOpenAdStart = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ADS_IS_START)));
                Callback.isOpenAdResume = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ADS_IS_RESUME)));
                Callback.isBannerAdHome = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ADS_IS_BANNER_HOME)));
                Callback.isBannerAdPostDetails = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ADS_IS_BANNER_DETAILS)));
                Callback.isBannerAdCatDetails = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ADS_IS_BANNER_CATEGORY)));
                Callback.isBannerAdSearch = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ADS_IS_BANNER_SEARCH)));
                Callback.isInterAd = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ADS_IS_INTERS)));
                Callback.isNativeAdPost = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ADS_IS_NATIVE_POST)));
                Callback.isNativeAdCat = Boolean.parseBoolean(c.getString(c.getColumnIndex(TAG_ADS_IS_NATIVE_CAT)));
            }
            c.close();
            return true;
        } else {
            if (c != null) {
                c.close();
            }
            return false;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public synchronized void close () {
        if (db != null) {
            db.close();
            super.close();
        }
    }
}