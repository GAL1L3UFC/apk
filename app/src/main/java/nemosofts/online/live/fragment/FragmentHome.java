package nemosofts.online.live.fragment;

import android.content.Context;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import nemosofts.online.live.R;
import nemosofts.online.live.adapter.Home.AdapterHome;
import nemosofts.online.live.asyncTask.LoadHome;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.dialog.InvalidUserDialog;
import nemosofts.online.live.fragment.Search.FragmentSearch;
import nemosofts.online.live.interfaces.HomeListener;
import nemosofts.online.live.item.ItemPost;
import nemosofts.online.live.utils.helper.DBHelper;
import nemosofts.online.live.utils.helper.Helper;

public class FragmentHome extends Fragment {

    DBHelper DBHelper;
    Helper helper;
    ProgressBar progressBar;
    FrameLayout frameLayout;
    RecyclerView rv_home;
    AdapterHome adapterHome;
    ArrayList<ItemPost> arrayList;
    private String errr_msg;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        helper = new Helper(getActivity());

        arrayList = new ArrayList<>();
        DBHelper = new DBHelper(getActivity());

        progressBar = rootView.findViewById(R.id.pb_home);
        frameLayout = rootView.findViewById(R.id.fl_empty);

        rv_home = rootView.findViewById(R.id.rv_home);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv_home.setLayoutManager(llm);
        rv_home.setItemAnimator(new DefaultItemAnimator());
        rv_home.setHasFixedSize(true);

        loadHome();

        setHasOptionsMenu(true);
        return rootView;
    }

    private void loadHome() {
        if (helper.isNetworkAvailable()) {
            LoadHome loadHome = new LoadHome(getContext(), new HomeListener() {
                @Override
                public void onStart() {
                    frameLayout.setVisibility(View.GONE);
                    rv_home.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEnd(String success, String message, ArrayList<ItemPost> arrayListPost) {
                    if (getActivity() != null) {
                        if (success.equals("1")) {

                            if (!arrayListPost.isEmpty()){

                                arrayList.addAll(arrayListPost);
                                if (Callback.isAdsStatus){
                                    arrayList.add(new ItemPost("100","ads","ads", "ads"));
                                }
                                adapterHome = new AdapterHome(getActivity(), arrayList);

                                rv_home.setAdapter(adapterHome);
                                setEmpty();
                            } else {
                                errr_msg = getString(R.string.error_no_data_found);
                                setEmpty();
                            }

                        } else if (success.equals("-2")) {
                            new InvalidUserDialog(getActivity(),message);
                        } else {
                            errr_msg = getString(R.string.error_server);
                            setEmpty();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }, helper.getAPIRequest(Callback.METHOD_HOME, 0, DBHelper.getRecentIDs("10"), "", "", "", "", "", "", "", "", "", "", "", null));
            loadHome.execute();
        } else {
            errr_msg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    public void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv_home.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {
            rv_home.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = inflater.inflate(R.layout.layout_empty, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errr_msg);

            myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> loadHome());

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
            FragmentSearch fsearch = new FragmentSearch();
            FragmentManager fm = getParentFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.hide(Objects.requireNonNull(fm.findFragmentByTag(getString(R.string.nav_home))));
            ft.add(R.id.fragment, fsearch, getString(R.string.search));
            ft.addToBackStack(getString(R.string.search));
            ft.commit();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };
}