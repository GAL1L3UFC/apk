package nemosofts.online.live.fragment.Search;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import nemosofts.online.live.R;
import nemosofts.online.live.activity.MainActivity;
import nemosofts.online.live.adapter.AdapterSearch;
import nemosofts.online.live.asyncTask.LoadSearch;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.dialog.InvalidUserDialog;
import nemosofts.online.live.interfaces.HomeListener;
import nemosofts.online.live.item.ItemPost;
import nemosofts.online.live.utils.helper.Helper;

public class FragmentSearch extends Fragment {

    Helper helper;
    ProgressBar progressBar;
    FrameLayout frameLayout;
    RecyclerView rv_home;
    AdapterSearch adapterSearch;
    ArrayList<ItemPost> arrayList_posts;
    private String errr_msg;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

        helper = new Helper(getActivity());

        arrayList_posts = new ArrayList<>();

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.search));
        ((MainActivity) getActivity()).bottomNavigationView(5);

        progressBar = rootView.findViewById(R.id.pb_home);
        frameLayout = rootView.findViewById(R.id.fl_empty);

        rv_home = rootView.findViewById(R.id.rv_home);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv_home.setLayoutManager(llm);
        rv_home.setItemAnimator(new DefaultItemAnimator());
        rv_home.setHasFixedSize(true);

        loadHome();

        return rootView;
    }

    private void loadHome() {
        if (helper.isNetworkAvailable()) {
            LoadSearch loadSearch = new LoadSearch(new HomeListener() {
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

                                arrayList_posts.addAll(arrayListPost);
                                if (Callback.isAdsStatus){
                                    arrayList_posts.add(new ItemPost("100","ads","ads", "ads"));
                                }
                                adapterSearch = new AdapterSearch(getActivity(), arrayList_posts);
                                rv_home.setAdapter(adapterSearch);
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
            }, helper.getAPIRequest(Callback.METHOD_SEARCH, 0, "", "", Callback.search_item, "", "", "", "", "", "", "", "", "", null));
            loadSearch.execute();
        } else {
            errr_msg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    public void setEmpty() {
        if (!arrayList_posts.isEmpty()) {
            rv_home.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {
            rv_home.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);

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
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public boolean onQueryTextSubmit(String s) {
            if (helper.isNetworkAvailable()) {
                Callback.search_item = s.replace(" ", "%20");
                arrayList_posts.clear();
                if (adapterSearch != null){
                    adapterSearch.notifyDataSetChanged();
                }
                loadHome();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

}