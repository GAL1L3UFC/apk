package nemosofts.online.live.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.nemosofts.AppCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import nemosofts.online.live.R;
import nemosofts.online.live.adapter.AdapterSimilar;
import nemosofts.online.live.adapter.AdapterSimilarGrid;
import nemosofts.online.live.asyncTask.LoadLiveID;
import nemosofts.online.live.asyncTask.LoadStatus;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.cast.Casty;
import nemosofts.online.live.cast.MediaData;
import nemosofts.online.live.dialog.FeedBackDialog;
import nemosofts.online.live.dialog.ReviewDialog;
import nemosofts.online.live.fragment.player.ChromecastScreenFragment;
import nemosofts.online.live.fragment.player.EmbeddedHLSFragment;
import nemosofts.online.live.fragment.player.EmbeddedImageFragment;
import nemosofts.online.live.fragment.player.EmbeddedYoutubeFragment;
import nemosofts.online.live.fragment.player.ExoPlayerFragment;
import nemosofts.online.live.fragment.player.ExternalImageFragment;
import nemosofts.online.live.fragment.player.PremiumContentFragment;
import nemosofts.online.live.fragment.player.WebsiteImageFragment;
import nemosofts.online.live.interfaces.LiveIDListener;
import nemosofts.online.live.interfaces.SuccessListener;
import nemosofts.online.live.item.ItemData;
import nemosofts.online.live.item.ItemLiveTv;
import nemosofts.online.live.utils.ApplicationUtil;
import nemosofts.online.live.utils.IfSupported;
import nemosofts.online.live.utils.helper.DBHelper;
import nemosofts.online.live.utils.Events;
import nemosofts.online.live.utils.GlobalBus;
import nemosofts.online.live.utils.helper.Helper;
import nemosofts.online.live.utils.SharedPref;

public class VideoDetailsActivity extends AppCompatActivity {

    private String post_id;
    private Toolbar toolbar;
    private Helper helper;
    private SharedPref sharedPref;
    private ItemLiveTv itemLive;
    private String error_msg = "";

    private FragmentManager fragmentManager;
    private int playerHeight;
    private FrameLayout frameLayout;
    boolean isFullScreen = false;
    private NestedScrollView mNestedScrollView;
    private ProgressBar mProgressBar;
    private RelativeLayout lytParent;

    private Casty casty;

    private int mOriginalSystemUiVisibility;

    private TextView tv_title, tv_views, tv_avg_rate;
    private RatingBar ratingBar;
    private ImageView iv_fav;
    private WebView mWebView;

    private RecyclerView rv_similar;
    private ArrayList<ItemData> arrayListPost;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalBus.getBus().register(this);

        IfSupported.IsRTL(this);
        IfSupported.IsScreenshot(this);
        IfSupported.keepScreenOn(this);

        toolbar = findViewById(R.id.toolbar);

        setTitle("");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());


        try {
            casty = Casty.create(this).withMiniController();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        post_id = intent.getStringExtra("post_id");

        helper = new Helper(this);
        sharedPref = new SharedPref(this);

        fragmentManager = getSupportFragmentManager();

        arrayListPost = new ArrayList<>();

        mProgressBar = findViewById(R.id.pb);
        lytParent = findViewById(R.id.lytParent);
        mNestedScrollView = findViewById(R.id.nestedScrollView);

        tv_title = findViewById(R.id.tv_details_title);
        ratingBar = findViewById(R.id.rb_video);
        tv_views = findViewById(R.id.tv_views);
        tv_avg_rate = findViewById(R.id.tv_avg_rate);
        iv_fav = findViewById(R.id.iv_fav);
        rv_similar = findViewById(R.id.rv_similar);

        mWebView = findViewById(R.id.webView_det);
        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.getSettings().setJavaScriptEnabled(true);

        frameLayout = findViewById(R.id.playerSection);
        int columnWidth = ApplicationUtil.getScreenWidth(VideoDetailsActivity.this);
        frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth / 2));
        playerHeight = frameLayout.getLayoutParams().height;

        getDetails();

        iv_fav.setOnClickListener(v -> loadFav());
        findViewById(R.id.iv_sta).setOnClickListener(v -> {
            if (sharedPref.isLogged()){
                showRateDialog();
            } else {
                helper.clickLogin();
            }
        });

        ImageView iv_similar = findViewById(R.id.iv_similar);
        iv_similar.setImageResource(Boolean.TRUE.equals(sharedPref.getGridSimilar()) ? R.drawable.ic_round_list : R.drawable.ic_grid_view);
        iv_similar.setOnClickListener(v -> {
            sharedPref.setGridSimilar(!sharedPref.getGridSimilar());
            iv_similar.setImageResource(Boolean.TRUE.equals(sharedPref.getGridSimilar()) ? R.drawable.ic_round_list : R.drawable.ic_grid_view);
            setSimilarAdapter();
        });

        LinearLayout adView = findViewById(R.id.ll_adView);
        helper.showBannerAd(adView,"");

        LinearLayout adView2 = findViewById(R.id.ll_adView_2);
        helper.showBannerAd(adView2,"");
    }

    private void getDetails() {
        if (helper.isNetworkAvailable()) {
            LoadLiveID loadBank = new LoadLiveID(new LiveIDListener() {
                @Override
                public void onStart() {
                    mProgressBar.setVisibility(View.VISIBLE);
                    lytParent.setVisibility(View.GONE);
                }

                @Override
                public void onEnd(String success, ArrayList<ItemLiveTv> arrayListLive, ArrayList<ItemData> arrayListRelated) {
                    if (success.equals("1")) {
                        if (arrayListLive.isEmpty()) {
                            error_msg = getString(R.string.error_no_data_found);
                            setEmpty();

                        } else {
                            itemLive = arrayListLive.get(0);
                            if (!arrayListRelated.isEmpty()){
                                arrayListPost.clear();
                                arrayListPost.addAll(arrayListRelated);
                            }

                            if (itemLive != null){
                                displayData();
                            } else {
                                error_msg = getString(R.string.error_no_data_found);
                                setEmpty();
                            }
                        }
                    } else {
                        error_msg = getString(R.string.error_server_not_connected);
                        setEmpty();
                    }
                }
            }, helper.getAPIRequest(Callback.METHOD_LIVE_ID, 0, post_id, "", "", "", new SharedPref(this).getUserId(), "", "", "","","","","",null));
            loadBank.execute();
        } else {
            error_msg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    private void displayData() {

        tv_title.setText(itemLive.getLiveTitle());
        ratingBar.setRating(Integer.parseInt(itemLive.getAverageRating()));
        tv_avg_rate.setText(itemLive.getAverageRating());
        tv_views.setText(getString(R.string.count, ApplicationUtil.format(Integer.parseInt(itemLive.getTotalViews()))));
        changeFav(itemLive.IsFav());

        try {
            new DBHelper(VideoDetailsActivity.this).addToRecent(new ItemData(itemLive.getId(),itemLive.getLiveTitle(),itemLive.getImage(),itemLive.IsPremium()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        setDescription();
        initPlayer();
        setSimilarAdapter();
        setEmpty();
    }

    private void setSimilarAdapter() {
        if (!arrayListPost.isEmpty()) {
            findViewById(R.id.ll_similar).setVisibility(View.VISIBLE);
            if (Boolean.TRUE.equals(sharedPref.getGridSimilar())){
                GridLayoutManager grid_fresh = new GridLayoutManager(this, 2);
                grid_fresh.setSpanCount(2);
                rv_similar.setLayoutManager(grid_fresh);
                rv_similar.setItemAnimator(new DefaultItemAnimator());
                rv_similar.setHasFixedSize(true);
                AdapterSimilarGrid adapterPostHome = new AdapterSimilarGrid(arrayListPost, (itemData, position) -> {
                    post_id = arrayListPost.get(position).getId();
                    mNestedScrollView.scrollTo(0, 0);
                    getDetails();
                });
                rv_similar.setAdapter(adapterPostHome);
            } else {
                AdapterSimilar adapter = new AdapterSimilar(arrayListPost, (itemPost, position) -> {
                    post_id = arrayListPost.get(position).getId();
                    mNestedScrollView.scrollTo(0, 0);
                    getDetails();
                });
                LinearLayoutManager llm = new LinearLayoutManager(VideoDetailsActivity.this);
                rv_similar.setLayoutManager(llm);
                rv_similar.setItemAnimator(new DefaultItemAnimator());
                rv_similar.setHasFixedSize(true);
                rv_similar.setAdapter(adapter);
            }
        } else {
            findViewById(R.id.ll_similar).setVisibility(View.GONE);
        }
    }

    private void initPlayer() {
        if (itemLive.IsPremium()) {
            if (Boolean.TRUE.equals(Callback.isPurchases)) {
                setPlayer();
            } else {
                PremiumContentFragment premiumContentFragment = PremiumContentFragment.newInstance(itemLive.getId(),"live");
                fragmentManager.beginTransaction().replace(R.id.playerSection, premiumContentFragment).commitAllowingStateLoss();
            }
        } else {
            setPlayer();
        }
    }

    private void setPlayer() {
        if (itemLive.getLiveURL().isEmpty()) {
            EmbeddedImageFragment embeddedImageFragment = EmbeddedImageFragment.newInstance(itemLive.getLiveURL(), itemLive.getImage(), false);
            fragmentManager.beginTransaction().replace(R.id.playerSection, embeddedImageFragment).commitAllowingStateLoss();
        } else {
            switch (itemLive.getLiveType()) {
                case "hls", "rtmp", "dash", "mp4" -> {
                    if (casty != null && casty.isConnected()) {
                        ChromecastScreenFragment chromecastScreenFragment = new ChromecastScreenFragment();
                        fragmentManager.beginTransaction().replace(R.id.playerSection, chromecastScreenFragment).commitAllowingStateLoss();
                    } else {
                        @SuppressLint("UnsafeOptInUsageError") ExoPlayerFragment exoPlayerFragment = ExoPlayerFragment.newInstance(itemLive.getLiveURL(), itemLive.getLiveTitle(), itemLive.getUserAgentName(), itemLive.isUserAgent());
                        fragmentManager.beginTransaction().replace(R.id.playerSection, exoPlayerFragment).commitAllowingStateLoss();
                    }
                }
                case "embedded" -> {
                    EmbeddedImageFragment embeddedImageFragment = EmbeddedImageFragment.newInstance(itemLive.getLiveURL(), itemLive.getImage(), true);
                    fragmentManager.beginTransaction().replace(R.id.playerSection, embeddedImageFragment).commitAllowingStateLoss();
                }
                case "webview" -> {
                    WebsiteImageFragment websiteImageFragment = WebsiteImageFragment.newInstance(itemLive.getLiveURL(), itemLive.getImage(), true);
                    fragmentManager.beginTransaction().replace(R.id.playerSection, websiteImageFragment).commitAllowingStateLoss();
                }
                case "youtube", "youtube_live" -> {
                    String videoId2 = ApplicationUtil.getVideoId(itemLive.getLiveURL());
                    EmbeddedYoutubeFragment embeddedYoutubePlayerActivity = EmbeddedYoutubeFragment.newInstance(videoId2, true);
                    fragmentManager.beginTransaction().replace(R.id.playerSection, embeddedYoutubePlayerActivity).commitAllowingStateLoss();
                }
                case "browser" -> {
                    ExternalImageFragment externalImageFragment = ExternalImageFragment.newInstance(itemLive.getLiveURL(), itemLive.getImage(), true, "browser");
                    fragmentManager.beginTransaction().replace(R.id.playerSection, externalImageFragment).commitAllowingStateLoss();
                }
                case "external" -> {
                    ExternalImageFragment externalFragment = ExternalImageFragment.newInstance(itemLive.getLiveURL(), itemLive.getImage(), true, "player");
                    fragmentManager.beginTransaction().replace(R.id.playerSection, externalFragment).commitAllowingStateLoss();
                }
                case "player" -> {
                    EmbeddedHLSFragment hls_externalFragment = EmbeddedHLSFragment.newInstance(itemLive.getLiveURL(), itemLive.getImage(), itemLive.getPlayerType(), itemLive.getLiveTitle(), itemLive.getUserAgentName(), itemLive.isUserAgent());
                    fragmentManager.beginTransaction().replace(R.id.playerSection, hls_externalFragment).commitAllowingStateLoss();
                }
            }
        }

        if (casty != null){
            casty.setOnConnectChangeListener(new Casty.OnConnectChangeListener() {
                @Override
                public void onConnected() {

                }

                @Override
                public void onDisconnected() {
                    switch (itemLive.getLiveType()) {
                        case "hls", "rtmp", "dash", "mp4" -> {
                            @SuppressLint("UnsafeOptInUsageError") ExoPlayerFragment exoPlayerFragment = ExoPlayerFragment.newInstance(itemLive.getLiveURL(), itemLive.getLiveTitle(), itemLive.getUserAgentName(), itemLive.isUserAgent());
                            fragmentManager.beginTransaction().replace(R.id.playerSection, exoPlayerFragment).commitAllowingStateLoss();
                        }
                        case "embedded" -> {
                            EmbeddedImageFragment embeddedImageFragment = EmbeddedImageFragment.newInstance(itemLive.getLiveURL(), itemLive.getImage(), true);
                            fragmentManager.beginTransaction().replace(R.id.playerSection, embeddedImageFragment).commitAllowingStateLoss();
                        }
                        case "webview" -> {
                            WebsiteImageFragment websiteImageFragment = WebsiteImageFragment.newInstance(itemLive.getLiveURL(), itemLive.getImage(), true);
                            fragmentManager.beginTransaction().replace(R.id.playerSection, websiteImageFragment).commitAllowingStateLoss();
                        }
                        case "youtube", "youtube_live" -> {
                            String videoId2 = ApplicationUtil.getVideoId(itemLive.getLiveURL());
                            EmbeddedYoutubeFragment embeddedYoutubePlayerActivity = EmbeddedYoutubeFragment.newInstance(videoId2, true);
                            fragmentManager.beginTransaction().replace(R.id.playerSection, embeddedYoutubePlayerActivity).commitAllowingStateLoss();
                        }
                        case "browser" -> {
                            ExternalImageFragment externalImageFragment = ExternalImageFragment.newInstance(itemLive.getLiveURL(), itemLive.getImage(), true, "browser");
                            fragmentManager.beginTransaction().replace(R.id.playerSection, externalImageFragment).commitAllowingStateLoss();
                        }
                        case "external" -> {
                            ExternalImageFragment externalFragment = ExternalImageFragment.newInstance(itemLive.getLiveURL(), itemLive.getImage(), true, "player");
                            fragmentManager.beginTransaction().replace(R.id.playerSection, externalFragment).commitAllowingStateLoss();
                        }
                        case "player" -> {
                            EmbeddedHLSFragment hls_externalFragment = EmbeddedHLSFragment.newInstance(itemLive.getLiveURL(), itemLive.getImage(), itemLive.getPlayerType(), itemLive.getLiveTitle(), itemLive.getUserAgentName(), itemLive.isUserAgent());
                            fragmentManager.beginTransaction().replace(R.id.playerSection, hls_externalFragment).commitAllowingStateLoss();
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (casty != null){
            casty.addMediaRouteMenuItem(menu);
        }
        getMenuInflater().inflate(R.menu.menu_details, menu);
        menu.findItem(R.id.menu_cast_play).setVisible(casty != null && casty.isConnected());
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == android.R.id.home){
            finish();
        } else if (id == R.id.menu_cast_play){
            playViaCast();
        } else if (id == R.id.menu_fields){
            if (itemLive != null){
                setTextSize();
            }
        } else if (id == R.id.menu_feedback){
            if (itemLive != null){
                new FeedBackDialog(this).showDialog(itemLive.getId(), itemLive.getLiveTitle());
            }
        } else if (id == R.id.menu_share){
            if (itemLive != null){
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_the_app));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,   itemLive.getLiveTitle() + "\n\nvia " + getResources().getString(R.string.app_name) + " - http://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_the_app)));
            }
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void setTextSize() {
        final String[] listItems = {"Extra Small", "Small", "Medium", "Large", "Extra Large"};
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoDetailsActivity.this, R.style.dialogTheme);
        builder.setTitle("Select Font Size");
        builder.setSingleChoiceItems(listItems, sharedPref.getTextSize(), (dialog, which) -> sharedPref.setTextSize(which));
        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            setDescription();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void playViaCast() {
        if (itemLive.getLiveType().equals("hls") || itemLive.getLiveType().equals("rtmp") || itemLive.getLiveType().equals("dash") || itemLive.getLiveType().equals("mp4")) {
            casty.getPlayer().loadMediaAndPlay(createSampleMediaData(itemLive.getLiveURL(), itemLive.getLiveTitle(), itemLive.getImage()));
            ChromecastScreenFragment chromecastScreenFragment = new ChromecastScreenFragment();
            fragmentManager.beginTransaction().replace(R.id.playerSection, chromecastScreenFragment).commitAllowingStateLoss();
        } else {
            Toast.makeText(VideoDetailsActivity.this, getResources().getString(R.string.cast_youtube), Toast.LENGTH_SHORT).show();
        }
    }

    private MediaData createSampleMediaData(@NonNull String videoUrl, String videoTitle, String videoImage) {
        if (videoUrl.endsWith(".mp4")){
            return new MediaData.Builder(videoUrl)
                    .setStreamType(MediaData.STREAM_TYPE_BUFFERED)
                    .setContentType(getType(videoUrl))
                    .setMediaType(MediaData.MEDIA_TYPE_MOVIE)
                    .setTitle(videoTitle)
                    .setSubtitle(getString(R.string.app_name))
                    .addPhotoUrl(videoImage)
                    .build();
        } else {
            return new MediaData.Builder(videoUrl)
                    .setStreamType(MediaData.STREAM_TYPE_BUFFERED)
                    .setContentType(getType(videoUrl))
                    .setMediaType(MediaData.MEDIA_TYPE_TV_SHOW)
                    .setTitle(videoTitle)
                    .setSubtitle(getString(R.string.app_name))
                    .addPhotoUrl(videoImage)
                    .build();
        }

    }

    @NonNull
    private String getType(@NonNull String videoUrl) {
        if (videoUrl.endsWith(".mp4")) {
            return "videos/mp4";
        } else if (videoUrl.endsWith(".m3u8")) {
            return "application/x-mpegurl";
        } else {
            return "application/x-mpegurl";
        }
    }

    private void setEmpty() {
        if (itemLive != null){
            mProgressBar.setVisibility(View.GONE);
            lytParent.setVisibility(View.VISIBLE);
        } else {
            if (!error_msg.isEmpty()){
                Toast.makeText(VideoDetailsActivity.this, error_msg, Toast.LENGTH_SHORT).show();
            }
            mProgressBar.setVisibility(View.GONE);
            lytParent.setVisibility(View.GONE);
        }
    }

    @Override
    public int setLayoutResourceId() {
        return R.layout.activity_video_details;
    }

    @Override
    public int setAppCompat() {
        return AppCompat.COMPAT();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }

    @Subscribe
    public void getFullScreen(@NonNull Events.FullScreen fullScreen) {
        isFullScreen = fullScreen.isFullScreen();
        if (fullScreen.isFullScreen()) {
            gotoFullScreen();
        } else {
            gotoPortraitScreen();
        }
    }

    private void gotoPortraitScreen() {
        mNestedScrollView.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, playerHeight));
        getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility);
    }

    private void gotoFullScreen() {
        mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
        mNestedScrollView.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
        frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().getDecorView().setSystemUiVisibility(3846);
    }

    @Override
    public void onBackPressed() {
        if (isFullScreen) {
            Events.FullScreen fullScreen = new Events.FullScreen();
            fullScreen.setFullScreen(false);
            GlobalBus.getBus().post(fullScreen);
        } else {
            super.onBackPressed();
        }
    }

    private void loadFav() {
        if (sharedPref.isLogged()) {
            if (helper.isNetworkAvailable()) {
                LoadStatus loadFav = new LoadStatus(new SuccessListener() {
                    @Override
                    public void onStart() {
                        changeFav(!itemLive.IsFav());
                    }

                    @Override
                    public void onEnd(String success, String favSuccess, String message) {
                        if (success.equals("1")) {
                            itemLive.setIsFav(message.equals("Added to Favourite"));
                            changeFav(itemLive.IsFav());
                            Toast.makeText(VideoDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VideoDetailsActivity.this, getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, helper.getAPIRequest(Callback.METHOD_DO_FAV, 0, itemLive.getId(), "", "", "", sharedPref.getUserId(), "", "", "", "", "", "", "", null));
                loadFav.execute();
            } else {
                Toast.makeText(VideoDetailsActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
            }
        } else {
            helper.clickLogin();
        }
    }

    private void showRateDialog() {
        if (helper.isNetworkAvailable()){
            ReviewDialog reviewDialog = new ReviewDialog(this, new ReviewDialog.RatingDialogListener() {
                @Override
                public void onShow() {
                }

                @Override
                public void onGetRating(String rating, String message) {
                    itemLive.setUserRating(String.valueOf(rating));
                    itemLive.setUserMessage(message);
                }

                @Override
                public void onDismiss(String success, String rateSuccess, String message, int rating, String userRating, String userMessage) {
                    if (success.equals("1")) {
                        if (rateSuccess.equals("1")) {
                            try {
                                itemLive.setAverageRating(String.valueOf(rating));
                                itemLive.setTotalRate(String.valueOf(Integer.parseInt(itemLive.getTotalRate() + 1)));
                                itemLive.setUserRating(String.valueOf(userRating));
                                itemLive.setUserMessage(String.valueOf(userMessage));
                                ratingBar.setRating(rating);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(VideoDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(VideoDetailsActivity.this, getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            reviewDialog.showDialog(itemLive.getId(), itemLive.getUserRating(), itemLive.getUserMessage());
        } else {
            Toast.makeText(VideoDetailsActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void changeFav(Boolean isFav) {
        if (Boolean.TRUE.equals(isFav)) {
            iv_fav.setImageDrawable(getResources().getDrawable(R.drawable.ic_turned_in));
        } else {
            iv_fav.setImageDrawable(getResources().getDrawable(R.drawable.ic_turned_in_not));
        }
    }

    private void setDescription() {
        String htmlText = itemLive.getLiveDescription();
        String textSize;
        int textData = sharedPref.getTextSize();
        if (0 == textData){
            textSize = "body{font-size:12px;}";
        } else if (1 == textData){
            textSize = "body{font-size:14px;}";
        } else if (2 == textData){
            textSize = "body{font-size:16px;}";
        } else if (3 == textData){
            textSize = "body{font-size:17px;}";
        } else if (4 == textData){
            textSize = "body{font-size:20px;}";
        } else {
            textSize = "body{font-size:14px;}";
        }

        String myCustomStyleString;
        if (Boolean.TRUE.equals(new ThemeEngine(this).getIsThemeMode())) {
            myCustomStyleString = "<style channelType=\"text/css\">body,* {color:#DBDBDB; font-family: MyFont;text-align: justify;}img{max-width:100%;height:auto; border-radius: 3px;}</style>"
                    + "<style type=\"text/css\">"+ textSize + "</style>";
        } else {
            myCustomStyleString = "<style channelType=\"text/css\">body,* {color:#161616; font-family: MyFont; text-align: justify;}img{max-width:100%;height:auto; border-radius: 3px;}</style>"
                    + "<style type=\"text/css\">"+ textSize + "</style>";
        }

        String htmlString;
        if(Boolean.FALSE.equals(Callback.isRTL)) {
            htmlString = myCustomStyleString + "<div>" + htmlText + "</div>";
        } else {
            htmlString = "<html dir=\"rtl\" lang=\"\"><body>" + myCustomStyleString + "<div>" + htmlText + "</div>" + "</body></html>";
        }
        mWebView.loadDataWithBaseURL("", htmlString, "text/html", "utf-8", null);
    }
}