package nemosofts.online.live.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.nemosofts.AppCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.nemosofts.view.ToggleView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import java.util.Objects;

import nemosofts.online.live.R;
import nemosofts.online.live.asyncTask.LoadAbout;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.dialog.ExitDialog;
import nemosofts.online.live.fragment.FragmentDashBoard;
import nemosofts.online.live.fragment.Online.FragmentCategories;
import nemosofts.online.live.fragment.Online.FragmentEvent;
import nemosofts.online.live.fragment.Online.FragmentLatest;
import nemosofts.online.live.fragment.Online.FragmentRecent;
import nemosofts.online.live.fragment.Online.FragmentTrending;
import nemosofts.online.live.interfaces.AboutListener;
import nemosofts.online.live.utils.IfSupported;
import nemosofts.online.live.utils.SharedPref;
import nemosofts.online.live.utils.advertising.AdManagerInterAdmob;
import nemosofts.online.live.utils.advertising.AdManagerInterApplovin;
import nemosofts.online.live.utils.advertising.AdManagerInterStartApp;
import nemosofts.online.live.utils.advertising.AdManagerInterUnity;
import nemosofts.online.live.utils.advertising.AdManagerInterWortise;
import nemosofts.online.live.utils.advertising.AdManagerInterYandex;
import nemosofts.online.live.utils.advertising.AppOpenAdManager;
import nemosofts.online.live.utils.advertising.GDPRChecker;
import nemosofts.online.live.utils.helper.DBHelper;
import nemosofts.online.live.utils.helper.Helper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager fm;
    MenuItem menu_login;
    MenuItem menu_profile;
    ReviewManager manager;
    ReviewInfo reviewInfo;

    Helper helper;
    DBHelper dbHelper;
    SharedPref sharedPref;
    NavigationView navigationView;
    ToggleView tv_nav_home, tv_nav_latest, tv_nav_most, tv_nav_category, tv_nav_restore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IfSupported.IsRTL(this);
        IfSupported.IsScreenshot(this);

        fm = getSupportFragmentManager();

        helper = new Helper(this);
        dbHelper = new DBHelper(this);
        sharedPref = new SharedPref(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(R.drawable.ic_menu_white);
        toggle.setToolbarNavigationClickListener(view -> drawer.openDrawer(GravityCompat.START));
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        if(Boolean.TRUE.equals(new ThemeEngine(this).getIsThemeMode())) {
            Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.white), PorterDuff.Mode.SRC_ATOP);
        } else {
            Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.black), PorterDuff.Mode.SRC_ATOP);
        }

        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        menu_login = menu.findItem(R.id.nav_login);
        menu_profile = menu.findItem(R.id.nav_profile);

        new GDPRChecker(MainActivity.this).check();
        changeLoginName();
        loadAboutData();

        manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reviewInfo = task.getResult();
            }
        });

        tv_nav_home = findViewById(R.id.tv_nav_home);
        tv_nav_home.setBadgeText("");
        tv_nav_latest = findViewById(R.id.tv_nav_latest);
        tv_nav_most = findViewById(R.id.tv_nav_most);
        tv_nav_category = findViewById(R.id.tv_nav_category);
        tv_nav_restore = findViewById(R.id.tv_nav_restore);

        tv_nav_home.setOnClickListener(view -> {
            if (!tv_nav_home.isActive()){
                FragmentDashBoard f_home = new FragmentDashBoard();
                loadFrag(f_home, getString(R.string.dashboard), fm);
            }
            bottomNavigationView(0);
        });
        tv_nav_latest.setOnClickListener(view -> {
            if (!tv_nav_latest.isActive()){
                FragmentLatest f_latest = new FragmentLatest();
                loadFrag(f_latest, getString(R.string.latest), fm);
            }
            bottomNavigationView(1);
        });
        tv_nav_most.setOnClickListener(view -> {
            if (!tv_nav_most.isActive()){
                FragmentTrending f_most = new FragmentTrending();
                loadFrag(f_most, getString(R.string.trending), fm);
            }
            bottomNavigationView(2);
        });
        tv_nav_category.setOnClickListener(view -> {
            if (!tv_nav_category.isActive()){
                FragmentCategories f_category = new FragmentCategories();
                loadFrag(f_category, getString(R.string.categories), fm);
            }
            bottomNavigationView(3);
        });
        tv_nav_restore.setOnClickListener(view -> {
            if (!tv_nav_restore.isActive()){
                FragmentRecent f_recent = new FragmentRecent();
                loadFrag(f_recent, getString(R.string.recently), fm);
            }
            bottomNavigationView(4);
        });

        loadDashboardFrag();
    }

    private void loadDashboardFrag() {
        FragmentDashBoard f1 = new FragmentDashBoard();
        loadFrag(f1, getResources().getString(R.string.dashboard), fm);
        navigationView.setCheckedItem(R.id.nav_home);
    }

    public void loadFrag(Fragment f1, String name, FragmentManager fm) {
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStackImmediate();
        }

        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (!name.equals(getString(R.string.dashboard))) {
            ft.hide(fm.getFragments().get(fm.getBackStackEntryCount()));
            ft.add(R.id.fragment, f1, name);
            ft.addToBackStack(name);
        } else {
            ft.replace(R.id.fragment, f1, name);
        }
        ft.commit();

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(name);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void changeLoginName() {
        if (menu_login != null) {
            if (sharedPref.isLogged()) {
                menu_profile.setVisible(true);
                menu_login.setTitle(getResources().getString(R.string.logout));
                menu_login.setIcon(getResources().getDrawable(R.drawable.ic_logout));

            } else {
                menu_profile.setVisible(false);
                menu_login.setTitle(getResources().getString(R.string.login));
                menu_login.setIcon(getResources().getDrawable(R.drawable.ic_login));
            }
        }
    }

    public void loadAboutData() {
        if (helper.isNetworkAvailable()) {
            LoadAbout loadAbout = new LoadAbout(MainActivity.this, new AboutListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onEnd(String success, String verifyStatus, String message) {
                    if (success.equals("1")) {
                        helper.initializeAds();
                        dbHelper.addtoAbout();
                        if (Callback.isInterAd && Callback.isAdsStatus) {
                            switch (Callback.adNetwork) {
                                case Callback.AD_TYPE_ADMOB -> {
                                    AdManagerInterAdmob adManagerInterAdmob = new AdManagerInterAdmob(getApplicationContext());
                                    adManagerInterAdmob.createAd();
                                }
                                case Callback.AD_TYPE_STARTAPP -> {
                                    AdManagerInterStartApp adManagerInterStartApp = new AdManagerInterStartApp(getApplicationContext());
                                    adManagerInterStartApp.createAd();
                                }
                                case Callback.AD_TYPE_APPLOVIN -> {
                                    AdManagerInterApplovin adManagerInterApplovin = new AdManagerInterApplovin(MainActivity.this);
                                    adManagerInterApplovin.createAd();
                                }
                                case Callback.AD_TYPE_YANDEX -> {
                                    AdManagerInterYandex adManagerInterYandex = new AdManagerInterYandex(MainActivity.this);
                                    adManagerInterYandex.createAd();
                                }
                                case Callback.AD_TYPE_WORTISE -> {
                                    AdManagerInterWortise adManagerInterWortise = new AdManagerInterWortise(MainActivity.this);
                                    adManagerInterWortise.createAd();
                                }
                                case Callback.AD_TYPE_UNITY -> {
                                    AdManagerInterUnity adManagerInterUnity = new AdManagerInterUnity();
                                    adManagerInterUnity.createAd();
                                }
                                default -> {
                                }
                            }
                        }
                    }
                }
            });
            loadAbout.execute();
        } else {
            try {
                dbHelper.getAbout();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home){
            if (!tv_nav_home.isActive()){
                FragmentDashBoard f_home = new FragmentDashBoard();
                loadFrag(f_home, getString(R.string.dashboard), fm);
            }
            bottomNavigationView(0);
        } else if (id == R.id.nav_latest){
            if (!tv_nav_latest.isActive()){
                FragmentLatest f_latest = new FragmentLatest();
                loadFrag(f_latest, getString(R.string.latest), fm);
            }
            bottomNavigationView(1);
        } else if (id == R.id.nav_most){
            if (!tv_nav_most.isActive()){
                FragmentTrending f_most = new FragmentTrending();
                loadFrag(f_most, getString(R.string.trending), fm);
            }
            bottomNavigationView(2);
        } else if (id == R.id.nav_category){
            if (!tv_nav_category.isActive()){
                FragmentCategories f_category = new FragmentCategories();
                loadFrag(f_category, getString(R.string.categories), fm);
            }
            bottomNavigationView(3);
        } else if (id == R.id.nav_restore){
            if (!tv_nav_restore.isActive()){
                FragmentRecent f_recent = new FragmentRecent();
                loadFrag(f_recent, getString(R.string.recently), fm);
            }
            bottomNavigationView(4);
        } else if (id == R.id.nav_event){
            FragmentEvent f_event = new FragmentEvent();
            loadFrag(f_event, getString(R.string.live_event), fm);
            bottomNavigationView(5);
        } else if (id == R.id.nav_suggest){
            if (sharedPref.isLogged()){
                startActivity(new Intent(MainActivity.this, SuggestionActivity.class));
            } else {
                helper.clickLogin();
            }
        } else if (id == R.id.nav_fav){
            Intent intent = new Intent(MainActivity.this, PostIDActivity.class);
            intent.putExtra("page_type", getString(R.string.favourite));
            intent.putExtra("id", "");
            intent.putExtra("name", getString(R.string.favourite));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (id == R.id.nav_profile){
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_settings){
            overridePendingTransition(0, 0);
            overridePendingTransition(0, 0);
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
            finish();
        } else if (id == R.id.nav_login){
            helper.clickLogin();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void bottomNavigationView(int pos) {
        if (pos == 0){
            if (!tv_nav_home.isActive()){
                tv_nav_home.activate();
                tv_nav_home.setBadgeText("");
            }
        } else {
            if (tv_nav_home.isActive()){
                tv_nav_home.deactivate();
                tv_nav_home.setBadgeText(null);
            }
        }

        if (pos == 1){
            if (!tv_nav_latest.isActive()){
                tv_nav_latest.activate();
                tv_nav_latest.setBadgeText("");
            }
        } else {
            if (tv_nav_latest.isActive()){
                tv_nav_latest.deactivate();
                tv_nav_latest.setBadgeText(null);
            }
        }

        if (pos == 2){
            if (!tv_nav_most.isActive()){
                tv_nav_most.activate();
                tv_nav_most.setBadgeText("");
            }
        } else {
            if (tv_nav_most.isActive()){
                tv_nav_most.deactivate();
                tv_nav_most.setBadgeText(null);
            }
        }

        if (pos == 3){
            if (!tv_nav_category.isActive()){
                tv_nav_category.activate();
                tv_nav_category.setBadgeText("");
            }
        } else {
            if (tv_nav_category.isActive()){
                tv_nav_category.deactivate();
                tv_nav_category.setBadgeText(null);
            }
        }

        if (pos == 4){
            if (!tv_nav_restore.isActive()){
                tv_nav_restore.activate();
                tv_nav_restore.setBadgeText("");
            }
        } else {
            if (tv_nav_restore.isActive()){
                tv_nav_restore.deactivate();
                tv_nav_restore.setBadgeText(null);
            }
        }
        if (pos == 5){
            if (tv_nav_home.isActive()){
                tv_nav_home.deactivate();
                tv_nav_home.setBadgeText(null);
            }
            if (tv_nav_latest.isActive()){
                tv_nav_latest.deactivate();
                tv_nav_latest.setBadgeText(null);
            }
            if (tv_nav_most.isActive()){
                tv_nav_most.deactivate();
                tv_nav_most.setBadgeText(null);
            }
            if (tv_nav_category.isActive()){
                tv_nav_category.deactivate();
                tv_nav_category.setBadgeText(null);
            }
            if (tv_nav_restore.isActive()){
                tv_nav_restore.deactivate();
                tv_nav_restore.setBadgeText(null);
            }
        }
    }

    @Override
    public void onResume() {
        changeLoginName();
        super.onResume();
    }

    @Override
    public int setLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    public int setAppCompat() {
        return AppCompat.COMPAT();
    }

    @Override
    protected void onDestroy() {;
        try {
            dbHelper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Boolean.TRUE.equals(Callback.isOpenAdStart) && Boolean.TRUE.equals(Callback.isOpenAdStartShow)){
            Callback.isOpenAdStartShow = false;
            new AppOpenAdManager(MainActivity.this);
        } else if (Boolean.TRUE.equals(Callback.isOpenAdResume)){
            new AppOpenAdManager(MainActivity.this);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fm.getBackStackEntryCount() != 0) {
            String title = fm.getFragments().get(fm.getBackStackEntryCount()).getTag();
            if (title != null){
                if (title.equals(getString(R.string.dashboard)) || title.equals(getString(R.string.nav_home))) {
                    navigationView.setCheckedItem(R.id.nav_home);
                    bottomNavigationView(0);
                } else if (title.equals(getString(R.string.latest))){
                    navigationView.setCheckedItem(R.id.nav_latest);
                    bottomNavigationView(1);
                } else if (title.equals(getString(R.string.trending))){
                    navigationView.setCheckedItem(R.id.nav_most);
                    bottomNavigationView(2);
                } else if (title.equals(getString(R.string.categories))){
                    navigationView.setCheckedItem(R.id.nav_category);
                    bottomNavigationView(3);
                } else if (title.equals(getString(R.string.recently))){
                    navigationView.setCheckedItem(R.id.nav_restore);
                    bottomNavigationView(4);
                } else if (title.equals(getString(R.string.search))){
                    navigationView.setCheckedItem(R.id.nav_home);
                    bottomNavigationView(5);
                }
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
            super.onBackPressed();
        } else if (reviewInfo != null){
            Task<Void> flow = manager.launchReviewFlow(MainActivity.this, reviewInfo);
            flow.addOnCompleteListener(task1 -> new ExitDialog(MainActivity.this));
        } else {
            new ExitDialog(MainActivity.this);
        }
    }
}