package nemosofts.online.live.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.nemosofts.AppCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.theme.ColorUtils;
import androidx.nemosofts.theme.ThemeEngine;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Objects;

import nemosofts.online.live.BuildConfig;
import nemosofts.online.live.R;
import nemosofts.online.live.utils.ApplicationUtil;
import nemosofts.online.live.utils.IfSupported;

public class SettingActivity extends AppCompatActivity {

    private ThemeEngine themeEngine;
    private ProgressDialog progressDialog;
    private TextView tv_cache_size;
    private TextView tv_classic, tv_dark_grey, tv_dark, tv_dark_blue;
    private ImageView iv_dark_mode;

    OnBackPressedCallback callback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            overridePendingTransition(0, 0);
            overridePendingTransition(0, 0);
            startActivity(new Intent(SettingActivity.this, MainActivity.class));
            finish();
        }
    };

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
        toolbar.setNavigationOnClickListener(view -> callback.handleOnBackPressed());

        themeEngine = new ThemeEngine(this);

        progressDialog = new ProgressDialog(SettingActivity.this, R.style.dialogTheme);
        progressDialog.setMessage(getString(R.string.clearing_cache));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        tv_classic = findViewById(R.id.tv_classic);
        tv_dark_grey = findViewById(R.id.tv_dark_grey);
        tv_dark = findViewById(R.id.tv_dark);
        tv_dark_blue = findViewById(R.id.tv_dark_blue);
        iv_dark_mode = findViewById(R.id.iv_dark_mode);
        tv_cache_size = findViewById(R.id.tv_cachesize);

        try {
            ObjectAnimator fadeAltAnim = ObjectAnimator.ofFloat(iv_dark_mode, View.ALPHA, 0, 1);
            fadeAltAnim.setDuration(1500);
            fadeAltAnim.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeCache();
        getThemeData();

        findViewById(R.id.ll_notifications).setOnClickListener(v -> notification());
        findViewById(R.id.ll_about).setOnClickListener(v -> startActivity(new Intent(SettingActivity.this, AboutUsActivity.class)));
        findViewById(R.id.ll_privacy).setOnClickListener(v ->  {
            Intent intent = new Intent(SettingActivity.this, WebActivity.class);
            intent.putExtra("web_url", BuildConfig.BASE_URL+"privacy_policy.php");
            intent.putExtra("page_title", getResources().getString(R.string.privacy_policy));
            ActivityCompat.startActivity(SettingActivity.this, intent, null);
        });
        findViewById(R.id.ll_terms).setOnClickListener(v ->  {
            Intent intent = new Intent(SettingActivity.this, WebActivity.class);
            intent.putExtra("web_url", BuildConfig.BASE_URL+"terms.php");
            intent.putExtra("page_title", getResources().getString(R.string.terms_and_conditions));
            ActivityCompat.startActivity(SettingActivity.this, intent, null);
        });
        findViewById(R.id.ll_privacy_data).setOnClickListener(v ->  {
            Intent intent = new Intent(SettingActivity.this, WebActivity.class);
            intent.putExtra("web_url", BuildConfig.BASE_URL+"policy/account_delete_request.php");
            intent.putExtra("page_title", getResources().getString(R.string.deletion_policy));
            ActivityCompat.startActivity(SettingActivity.this, intent, null);
        });
        findViewById(R.id.ll_cache).setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"StaticFieldLeak", "SetTextI18n"})
            @Override
            public void onClick(View v) {
                new AsyncTask<String, String, String>() {
                    @Override
                    protected void onPreExecute() {
                        progressDialog.show();
                        super.onPreExecute();
                    }

                    @Override
                    protected String doInBackground(String... strings) {
                        try {
                            FileUtils.deleteQuietly(getCacheDir());
                            FileUtils.deleteQuietly(getExternalCacheDir());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        progressDialog.dismiss();
                        Toast.makeText(SettingActivity.this, getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show();
                        tv_cache_size.setText("0 MB");
                        super.onPostExecute(s);
                    }
                }.execute();
            }
        });

        tv_classic.setOnClickListener(view -> {
            if (themeEngine.getThemePage() != 0){
                setThemeMode(false, 0);
            }
        });
        tv_dark_grey.setOnClickListener(view -> {
            if (themeEngine.getThemePage() != 2){
                setThemeMode(true, 2);
            }
        });
        tv_dark_blue.setOnClickListener(view -> {
            if (themeEngine.getThemePage() != 3){
                setThemeMode(true, 3);
            }
        });
        tv_dark.setOnClickListener(view -> {
            if (themeEngine.getThemePage() != 1){
                setThemeMode(true, 1);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, callback);
        callback.setEnabled(true);
    }

    private void setThemeMode(Boolean isChecked, int isTheme) {
        themeEngine.setThemeMode(isChecked);
        themeEngine.setThemePage(isTheme);
        recreate();
    }

    private void initializeCache() {
        long size = 0;
        size += getDirSize(this.getCacheDir());
        size += getDirSize(this.getExternalCacheDir());
        tv_cache_size.setText(ApplicationUtil.readableFileSize(size));
    }

    private long getDirSize(File dir) {
        long size = 0;
        try {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (file != null && file.isDirectory()) {
                    size += getDirSize(file);
                } else if (file != null && file.isFile()) {
                    size += file.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    private void getThemeData() {
        int theme = themeEngine.getThemePage();
        if (theme == 0){
            tv_classic.setBackgroundResource(R.drawable.btn_accent);
            tv_dark_grey.setBackgroundResource(R.drawable.btn_border_bg);
            tv_dark_blue.setBackgroundResource(R.drawable.btn_border_bg);
            tv_dark.setBackgroundResource(R.drawable.btn_border_bg);

            tv_classic.setTextColor(ColorUtils.colorWhite(this));
            tv_dark_grey.setTextColor(ColorUtils.colorTitle(this));
            tv_dark_blue.setTextColor(ColorUtils.colorTitle(this));
            tv_dark.setTextColor(ColorUtils.colorTitle(this));

            iv_dark_mode.setImageResource(R.drawable.classic);

        } else if (theme == 1){
            tv_classic.setBackgroundResource(R.drawable.btn_border_bg);
            tv_dark_grey.setBackgroundResource(R.drawable.btn_border_bg);
            tv_dark_blue.setBackgroundResource(R.drawable.btn_border_bg);
            tv_dark.setBackgroundResource(R.drawable.btn_accent);

            tv_classic.setTextColor(ColorUtils.colorTitle(this));
            tv_dark_grey.setTextColor(ColorUtils.colorTitle(this));
            tv_dark_blue.setTextColor(ColorUtils.colorTitle(this));
            tv_dark.setTextColor(ColorUtils.colorWhite(this));

            iv_dark_mode.setImageResource(R.drawable.dark);

        } else if (theme == 2){
            tv_classic.setBackgroundResource(R.drawable.btn_border_bg);
            tv_dark_grey.setBackgroundResource(R.drawable.btn_accent);
            tv_dark_blue.setBackgroundResource(R.drawable.btn_border_bg);
            tv_dark.setBackgroundResource(R.drawable.btn_border_bg);

            tv_classic.setTextColor(ColorUtils.colorTitle(this));
            tv_dark_grey.setTextColor(ColorUtils.colorWhite(this));
            tv_dark_blue.setTextColor(ColorUtils.colorTitle(this));
            tv_dark.setTextColor(ColorUtils.colorTitle(this));

            iv_dark_mode.setImageResource(R.drawable.dark_grey);
        } else if (theme == 3){
            tv_classic.setBackgroundResource(R.drawable.btn_border_bg);
            tv_dark_grey.setBackgroundResource(R.drawable.btn_border_bg);
            tv_dark_blue.setBackgroundResource(R.drawable.btn_accent);
            tv_dark.setBackgroundResource(R.drawable.btn_border_bg);

            tv_classic.setTextColor(ColorUtils.colorTitle(this));
            tv_dark_grey.setTextColor(ColorUtils.colorTitle(this));
            tv_dark_blue.setTextColor(ColorUtils.colorWhite(this));
            tv_dark.setTextColor(ColorUtils.colorTitle(this));

            iv_dark_mode.setImageResource(R.drawable.dark_blue);
        }
    }

    private void notification() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
        } else {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
        }
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            callback.handleOnBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    @Override
    public int setLayoutResourceId() {
        return R.layout.activity_setting;
    }

    @Override
    public int setAppCompat() {
        return AppCompat.COMPAT();
    }
}