package nemosofts.online.live.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.nemosofts.AppCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import nemosofts.online.live.R;
import nemosofts.online.live.adapter.AdapterVideo;
import nemosofts.online.live.asyncTask.LoadLive;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.dialog.VerifyDialog;
import nemosofts.online.live.interfaces.LiveListener;
import nemosofts.online.live.item.ItemData;
import nemosofts.online.live.utils.IfSupported;
import nemosofts.online.live.utils.SharedPref;
import nemosofts.online.live.utils.helper.Helper;
import nemosofts.online.live.utils.recycler.EndlessRecyclerViewScrollListener;
import nemosofts.online.live.utils.recycler.RecyclerItemClickListener;
import okhttp3.RequestBody;

public class PostIDActivity extends AppCompatActivity {

    private Helper helper;
    private SharedPref sharedPref;
    private RecyclerView rv;
    private AdapterVideo adapter;
    private ArrayList<ItemData> arrayList;
    private Boolean isOver = false, isScroll = false;
    private int page = 1,  nativeAdPos = 0;
    private GridLayoutManager grid;
    private ProgressBar pb;
    private FloatingActionButton fab;
    private String error_msg;
    private FrameLayout frameLayout;
    private String id = "", name = "", page_type = "latest";

    private AdLoader adLoader;
    private final ArrayList<NativeAd> arrayListNativeAds = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IfSupported.IsRTL(this);
        IfSupported.IsScreenshot(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());
        try {
            setTitle(name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        name = intent.getStringExtra("name");
        page_type = intent.getStringExtra("page_type");

        helper = new Helper(this);
        helper = new Helper(PostIDActivity.this, (position, type) -> {
            if(adapter.getItem(position) != null) {
                Intent intent1 = new Intent(PostIDActivity.this, VideoDetailsActivity.class);
                intent1.putExtra("post_id", arrayList.get(position).getId());
                startActivity(intent1);
            }
        });

        sharedPref = new SharedPref(this);

        arrayList = new ArrayList<>();

        frameLayout = findViewById(R.id.fl_empty);
        fab = findViewById(R.id.fab);
        pb = findViewById(R.id.pb);
        rv = findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        grid = new GridLayoutManager(PostIDActivity.this, 1);
        grid.setSpanCount(3);
        grid.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapter.getItemViewType(position) == -2 || adapter.isHeader(position)) ? grid.getSpanCount() : 1;
            }
        });
        rv.setLayoutManager(grid);

        rv.addOnItemTouchListener(new RecyclerItemClickListener(this, (view, position) -> helper.showInterAd(position, "")));

        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (Boolean.FALSE.equals(isOver)) {
                    new Handler().postDelayed(() -> {
                        isScroll = true;
                        getData();
                    }, 0);
                } else {
                    adapter.hideHeader();
                }
            }
        });
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItem = grid.findFirstVisibleItemPosition();
                if (firstVisibleItem > 6) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }
        });

        fab.setOnClickListener(v -> rv.smoothScrollToPosition(0));

        if(Callback.nativeAdShow%2 != 0) {
            nativeAdPos = Callback.nativeAdShow + 1;
        } else {
            nativeAdPos = Callback.nativeAdShow;
        }

        if (page_type.equals(getString(R.string.favourite))){
            if(sharedPref.isLogged()) {
                getData();
            } else {
                error_msg = getString(R.string.not_log);
                setEmpty();
            }
        } else {
            getData();
        }
    }

    private void getData() {
        if (helper.isNetworkAvailable()) {
            RequestBody requestBody;
            if (page_type.equals(getString(R.string.categories))){
                requestBody = helper.getAPIRequest(Callback.METHOD_CAT_ID, page, "", id, "", "", "", "", "", "", "", "", "", "", null);
            } else if (page_type.equals(getString(R.string.favourite))){
                requestBody = helper.getAPIRequest(Callback.METHOD_POST_BY_FAV, page, "", "", "", "", sharedPref.getUserId(), "", "", "", "", "", "", "", null);
            } else if (page_type.equals("banner")) {
                requestBody = helper.getAPIRequest(Callback.METHOD_POST_BY_BANNER, page, id, "", "", "","", "", "", "", "", "", "", "", null);
            } else {
                requestBody = helper.getAPIRequest(Callback.METHOD_LATEST, page, "", "", "", "", "", "", "", "", "", "", "", "", null);
            }
            LoadLive loadCategory = new LoadLive(new LiveListener() {
                @Override
                public void onStart() {
                    if (arrayList.isEmpty()) {
                        frameLayout.setVisibility(View.GONE);
                        rv.setVisibility(View.GONE);
                        pb.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onEnd(String success, String verifyStatus, String message, ArrayList<ItemData> arrayListData) {
                    if (success.equals("1")) {
                        if (!verifyStatus.equals("-1")) {
                            if (arrayListData.isEmpty()) {
                                isOver = true;
                                try {
                                    adapter.hideHeader();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                error_msg = getString(R.string.error_no_data_found);
                                setEmpty();
                            } else {
                                for (int i = 0; i < arrayListData.size(); i++) {
                                    arrayList.add(arrayListData.get(i));
                                    if (helper.canLoadNativeAds(PostIDActivity.this,Callback.PAGE_NATIVE_POST)) {
                                        int abc = arrayList.lastIndexOf(null);
                                        if (((arrayList.size() - (abc + 1)) % nativeAdPos == 0)) {
                                            arrayList.add(null);
                                        }
                                    }
                                }
                                page = page + 1;
                                setAdapter();
                            }
                        } else {
                            new VerifyDialog(PostIDActivity.this, getString(R.string.error_unauthorized_access), message);
                        }
                    } else {
                        error_msg = getString(R.string.error_server_not_connected);
                        setEmpty();
                    }
                }
            }, requestBody);
            loadCategory.execute();
        } else {
            error_msg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter() {
        if(Boolean.FALSE.equals(isScroll)) {
            adapter = new AdapterVideo(PostIDActivity.this,  arrayList);
            rv.setAdapter(adapter);
            rv.scheduleLayoutAnimation();
            setEmpty();
            loadNativeAds();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadNativeAds() {
        if (helper.canLoadNativeAds(PostIDActivity.this,Callback.PAGE_NATIVE_POST) && Callback.adNetwork.equals(Callback.AD_TYPE_ADMOB) || Callback.adNetwork.equals(Callback.AD_TYPE_META) && arrayList.size() >= 10) {
            AdLoader.Builder builder = new AdLoader.Builder(PostIDActivity.this, Callback.admobNativeAdID);

            Bundle extras = new Bundle();

            AdRequest adRequest;
            if(Callback.adNetwork.equals(Callback.AD_TYPE_ADMOB)) {
                adRequest = new AdRequest.Builder()
                        .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                        .build();
            } else {
                adRequest = new AdRequest.Builder()
                        .addNetworkExtrasBundle(AdMobAdapter.class, new Bundle())
                        .addNetworkExtrasBundle(FacebookMediationAdapter.class, extras)
                        .build();
            }
            adLoader = builder.forNativeAd(nativeAd -> {
                try {
                    if(adLoader.isLoading()) {
                        arrayListNativeAds.add(nativeAd);
                    } else {
                        arrayListNativeAds.add(nativeAd);
                        if (adapter != null) {
                            adapter.addAds(arrayListNativeAds);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).build();
            adLoader.loadAds(adRequest, 5);
        }
    }

    @SuppressLint("InflateParams")
    private void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = inflater.inflate(R.layout.layout_empty, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(error_msg);

            myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> {
                if (page_type.equals(getString(R.string.favourite))){
                    if(sharedPref.isLogged()) {
                        getData();
                    } else {
                        helper.clickLogin();
                    }
                } else {
                    getData();
                }
            });
            frameLayout.addView(myView);
        }
    }

    @Override
    public int setLayoutResourceId() {
        return R.layout.activity_post_id;
    }

    @Override
    public int setAppCompat() {
        return AppCompat.COMPAT();
    }
}