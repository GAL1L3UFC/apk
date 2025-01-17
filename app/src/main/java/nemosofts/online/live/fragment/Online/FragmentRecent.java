package nemosofts.online.live.fragment.Online;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
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
import nemosofts.online.live.activity.VideoDetailsActivity;
import nemosofts.online.live.adapter.AdapterVideo;
import nemosofts.online.live.asyncTask.LoadLive;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.dialog.VerifyDialog;
import nemosofts.online.live.interfaces.LiveListener;
import nemosofts.online.live.item.ItemData;
import nemosofts.online.live.utils.helper.DBHelper;
import nemosofts.online.live.utils.helper.Helper;
import nemosofts.online.live.utils.recycler.EndlessRecyclerViewScrollListener;
import nemosofts.online.live.utils.recycler.RecyclerItemClickListener;

public class FragmentRecent extends Fragment {

    private Helper helper;
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
    private String recentSongIDs = "";
    private SearchView searchView;

    private AdLoader adLoader;
    private final ArrayList<NativeAd> arrayListNativeAds = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_latest, container, false);

        helper = new Helper(getActivity(), (position, type) -> {
            if(adapter.getItem(position) != null) {
                Intent intent1 = new Intent(getActivity(), VideoDetailsActivity.class);
                intent1.putExtra("post_id", arrayList.get(position).getId());
                startActivity(intent1);
            }
        });

        arrayList = new ArrayList<>();

        frameLayout = rootView.findViewById(R.id.fl_empty);
        fab = rootView.findViewById(R.id.fab);
        pb = rootView.findViewById(R.id.pb);
        rv = rootView.findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        grid = new GridLayoutManager(getActivity(), 1);
        grid.setSpanCount(3);
        grid.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapter.getItemViewType(position) == -2 || adapter.isHeader(position)) ? grid.getSpanCount() : 1;
            }
        });
        rv.setLayoutManager(grid);

        rv.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), (view, position) -> helper.showInterAd(position, "")));

        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (getActivity() != null) {
                    if (Boolean.FALSE.equals(isOver)) {
                        new Handler().postDelayed(() -> {
                            isScroll = true;
                            getData();
                        }, 0);
                    } else {
                        adapter.hideHeader();
                    }
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

        if(Callback.nativeAdShow%3 != 0) {
            nativeAdPos = Callback.nativeAdShow + 1;
        } else {
            nativeAdPos = Callback.nativeAdShow;
        }

        DBHelper dbHelper = new DBHelper(getActivity());
        recentSongIDs = dbHelper.getRecentIDs("100");
        if(!recentSongIDs.equals("")) {
            getData();
        } else {
            error_msg = getString(R.string.error_no_data_found);
            setEmpty();
        }

        setHasOptionsMenu(true);
        return rootView;
    }

    private void getData() {
        if (helper.isNetworkAvailable()) {
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
                    if (getActivity() != null) {
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
                                        if (helper.canLoadNativeAds(requireContext(),Callback.PAGE_NATIVE_POST)) {
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
                                new VerifyDialog(getActivity(), getString(R.string.error_unauthorized_access), message);
                            }
                        } else {
                            error_msg = getString(R.string.error_server_not_connected);
                            setEmpty();
                        }
                    }
                }
            }, helper.getAPIRequest(Callback.METHOD_LIVE_RECENT, page, recentSongIDs, "", "", "", "", "", "", "", "", "", "", "", null));
            loadCategory.execute();
        } else {
            error_msg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter() {
        if(Boolean.FALSE.equals(isScroll)) {
            adapter = new AdapterVideo(getActivity(),  arrayList);
            rv.setAdapter(adapter);
            rv.scheduleLayoutAnimation();
            setEmpty();
            loadNativeAds();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadNativeAds() {
        if (helper.canLoadNativeAds(requireContext(),Callback.PAGE_NATIVE_POST) && Callback.adNetwork.equals(Callback.AD_TYPE_ADMOB) || Callback.adNetwork.equals(Callback.AD_TYPE_META) && arrayList.size() >= 10) {
            AdLoader.Builder builder = new AdLoader.Builder(requireContext(), Callback.admobNativeAdID);

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
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(error_msg);

            myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> getData());

            frameLayout.addView(myView);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem search_item = menu.findItem(R.id.menu_search);
        MenuItemCompat.setShowAsAction(search_item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        if (searchView != null){
            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public boolean onQueryTextChange(String s) {
            if (adapter != null && (!searchView.isIconified())) {
                adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }
            return true;
        }
    };
}