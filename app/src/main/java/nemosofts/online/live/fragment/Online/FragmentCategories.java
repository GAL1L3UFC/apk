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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;

import java.util.ArrayList;

import nemosofts.online.live.R;
import nemosofts.online.live.activity.PostIDActivity;
import nemosofts.online.live.adapter.AdapterCategories;
import nemosofts.online.live.asyncTask.LoadCat;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.dialog.VerifyDialog;
import nemosofts.online.live.fragment.Search.FragmentSearchCategories;
import nemosofts.online.live.interfaces.CategoryListener;
import nemosofts.online.live.item.ItemCat;
import nemosofts.online.live.utils.helper.Helper;
import nemosofts.online.live.utils.recycler.EndlessRecyclerViewScrollListener;

public class FragmentCategories extends Fragment {

    private Helper helper;
    private RecyclerView rv;
    private AdapterCategories adapterCategories;
    private ArrayList<ItemCat> arrayList;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private GridLayoutManager glm_banner;
    private Boolean isLoading = false;
    private String errr_msg = "", homeSecId = "";
    private int page = 1,  nativeAdPos = 0;
    private Boolean isOver = false, isScroll = false, isFromHome = false;

    private AdLoader adLoader;
    private final ArrayList<NativeAd> arrayListNativeAds = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_latest, container, false);

        helper = new Helper(getActivity(), (position, type) -> {
            Intent intent = new Intent(getActivity(), PostIDActivity.class);
            intent.putExtra("page_type", getString(R.string.categories));
            intent.putExtra("id", arrayList.get(position).getId());
            intent.putExtra("name", arrayList.get(position).getName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        try {
            homeSecId = getArguments().getString("id");
            isFromHome = true;
        } catch (Exception e) {
            homeSecId = "";
            isFromHome = false;
            e.printStackTrace();
        }

        arrayList = new ArrayList<>();

        progressBar = rootView.findViewById(R.id.pb);
        frameLayout = rootView.findViewById(R.id.fl_empty);
        rv = rootView.findViewById(R.id.rv);
        glm_banner = new GridLayoutManager(getActivity(), 3);
        glm_banner.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapterCategories.getItemViewType(position) == -2 || adapterCategories.isHeader(position)) ? glm_banner.getSpanCount() : 1;
            }
        });

        rv.setLayoutManager(glm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(glm_banner) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (getActivity() != null){
                    if (Boolean.FALSE.equals(isFromHome)) {
                        if (Boolean.FALSE.equals(isOver)) {
                            if (Boolean.FALSE.equals(isLoading)) {
                                isLoading = true;
                                new Handler().postDelayed(() -> {
                                    isScroll = true;
                                    loadCategories();
                                }, 0);
                            }
                        } else {
                            adapterCategories.hideHeader();
                        }
                    } else {
                        adapterCategories.hideHeader();
                    }
                }
            }
        });

        if(Callback.nativeAdShow%2 != 0) {
            nativeAdPos = Callback.nativeAdShow + 1;
        } else {
            nativeAdPos = Callback.nativeAdShow;
        }

        loadCategories();

        setHasOptionsMenu(true);
        return rootView;
    }

    private void loadCategories() {
        if (helper.isNetworkAvailable()) {
            String helper_name = Boolean.TRUE.equals(isFromHome) ? Callback.METHOD_HOME_DETAILS : Callback.METHOD_CAT;
            LoadCat loadCat = new LoadCat(new CategoryListener() {
                @Override
                public void onStart() {
                    if (arrayList.isEmpty()) {
                        frameLayout.setVisibility(View.GONE);
                        rv.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onEnd(String success, String verifyStatus, String message, ArrayList<ItemCat> arrayListCat) {
                    if (getActivity() != null) {
                        if (success.equals("1")) {
                            if (!verifyStatus.equals("-1")) {
                                if (arrayListCat.isEmpty()) {
                                    isOver = true;
                                    errr_msg = getString(R.string.error_no_cat_found);
                                    if (Boolean.FALSE.equals(isFromHome)) {
                                        try {
                                            adapterCategories.hideHeader();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    setEmpty();
                                } else {
                                    for (int i = 0; i < arrayListCat.size(); i++) {
                                        arrayList.add(arrayListCat.get(i));
                                        if (helper.canLoadNativeAds(requireContext(),Callback.PAGE_NATIVE_CAT)) {
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
                            isOver = true;
                            try {
                                adapterCategories.hideHeader();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            errr_msg = getString(R.string.error_server);
                            setEmpty();
                        }
                        progressBar.setVisibility(View.GONE);
                        isLoading = false;
                    }
                }
            }, helper.getAPIRequest(helper_name, page, homeSecId, "", "", "", "", "", "", "","","","","", null));
            loadCat.execute();
        } else {
            errr_msg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setAdapter() {
        if (Boolean.FALSE.equals(isScroll)) {
            adapterCategories = new AdapterCategories(getActivity(), arrayList, (itemCat, position) -> helper.showInterAd(position, ""));
            rv.setAdapter(adapterCategories);
            setEmpty();
            loadNativeAds();
        } else {
            adapterCategories.notifyDataSetChanged();
        }
    }

    private void loadNativeAds() {
        if (helper.canLoadNativeAds(requireContext(),Callback.PAGE_NATIVE_CAT) && Callback.adNetwork.equals(Callback.AD_TYPE_ADMOB) || Callback.adNetwork.equals(Callback.AD_TYPE_META) && arrayList.size() >= 10) {
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
                        if (adapterCategories != null) {
                            adapterCategories.addAds(arrayListNativeAds);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).build();
            adLoader.loadAds(adRequest, 5);
        }
    }

    public void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errr_msg);

            myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> loadCategories());

            frameLayout.addView(myView);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        if (searchView != null){
            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            Callback.search_item = s.replace(" ", "%20");
            FragmentSearchCategories fsearch = new FragmentSearchCategories();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
            ft.add(R.id.fragment, fsearch, getString(R.string.search_categories));
            ft.addToBackStack(getString(R.string.search_categories));
            ft.commit();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

}