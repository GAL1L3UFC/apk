package nemosofts.online.live.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.nemosofts.envato.EnvatoProduct;
import androidx.nemosofts.envato.interfaces.EnvatoListener;
import androidx.nemosofts.theme.ColorUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import nemosofts.online.live.BuildConfig;
import nemosofts.online.live.R;
import nemosofts.online.live.asyncTask.LoadAbout;
import nemosofts.online.live.asyncTask.LoadLogin;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.interfaces.AboutListener;
import nemosofts.online.live.interfaces.LoginListener;
import nemosofts.online.live.utils.helper.DBHelper;
import nemosofts.online.live.utils.helper.Helper;
import nemosofts.online.live.utils.SharedPref;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity implements EnvatoListener {

    private Helper helper;
    private SharedPref sharedPref;
    private DBHelper dbHelper;
    private ProgressBar pb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        hideStatusBar();

        helper = new Helper(this);
        sharedPref = new SharedPref(this);
        dbHelper = new DBHelper(this);

        pb = findViewById(R.id.pb_splash);

        findViewById(R.id.rl_splash).setBackgroundColor(ColorUtils.colorBackground(this));

        loadAboutData();
    }

    private void loadAboutData() {
        if (helper.isNetworkAvailable()) {
            LoadAbout loadAbout = new LoadAbout(SplashActivity.this, new AboutListener() {
                @Override
                public void onStart() {
                    pb.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEnd(String success, String verifyStatus, String message){
                    pb.setVisibility(View.GONE);
                    if (success.equals("1")){
                        if (!verifyStatus.equals("-1") && !verifyStatus.equals("-2")){
                            dbHelper.getAbout();
                            setSaveData();
                        } else {
                            errorDialog(getString(R.string.error_unauthorized_access), message);
                        }
                    } else {
                        errorDialog(getString(R.string.error_server), getString(R.string.error_server_not_connected));
                    }
                }
            });
            loadAbout.execute();
        } else {
            if (Boolean.TRUE.equals(sharedPref.getIsFirst())) {
                errorDialog(getString(R.string.error_internet_not_connected), getString(R.string.error_try_internet_connected));
            } else {
                try {
                    dbHelper.getAbout();
                    setSaveData();
                } catch (Exception e) {
                    e.printStackTrace();
                    errorDialog(getString(R.string.error_internet_not_connected), getString(R.string.error_try_internet_connected));
                }
            }
        }
    }

    private void setSaveData() {
        new EnvatoProduct(this, this).execute();
    }

    private void loadSettings() {
        if (Boolean.TRUE.equals(Callback.isAppUpdate) && Callback.app_new_version != BuildConfig.VERSION_CODE){
            openDialogActivity(Callback.DIALOG_TYPE_UPDATE);
        } else if(Boolean.TRUE.equals(Callback.isMaintenance)){
            openDialogActivity(Callback.DIALOG_TYPE_MAINTENANCE);
        } else {
            if (Boolean.TRUE.equals(sharedPref.getIsFirst())) {
                if (Boolean.TRUE.equals(Callback.isLogin)){
                    openSignInActivity();
                } else {
                    sharedPref.setIsFirst(false);
                    openMainActivity();
                }
            } else {
                if (Boolean.FALSE.equals(sharedPref.getIsAutoLogin())) {
                    new Handler().postDelayed(this::openMainActivity, 2000);
                } else {
                    if (sharedPref.getLoginType().equals(Callback.LOGIN_TYPE_GOOGLE)) {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            loadLogin(Callback.LOGIN_TYPE_GOOGLE, sharedPref.getAuthID());
                        } else {
                            sharedPref.setIsAutoLogin(false);
                            openMainActivity();
                        }
                    } else {
                        loadLogin(Callback.LOGIN_TYPE_NORMAL, "");
                    }
                }
            }
        }
    }

    private void loadLogin(final String loginType, final String authID) {
        if (helper.isNetworkAvailable()) {
            LoadLogin loadLogin = new LoadLogin(new LoginListener() {
                @Override
                public void onStart() {
                    pb.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEnd(String success, String loginSuccess, String message, String user_id, String user_name, String user_gender, String user_phone, String profile_img) {
                    pb.setVisibility(View.GONE);
                    if (success.equals("1") && (!loginSuccess.equals("-1"))) {
                            sharedPref.setLoginDetails(user_id, user_name, user_phone, sharedPref.getEmail(), user_gender, profile_img, authID, sharedPref.getIsRemember(), sharedPref.getPassword(), loginType);
                            sharedPref.setIsLogged(true);
                    }
                    openMainActivity();
                }
            }, helper.getAPIRequest(Callback.METHOD_LOGIN, 0,"","","","","","",sharedPref.getEmail(),"","",sharedPref.getPassword(),authID,loginType,null));
            loadLogin.execute();
        } else {
            Toast.makeText(SplashActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
            sharedPref.setIsAutoLogin(false);
            openMainActivity();
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void openSignInActivity() {
        Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", "");
        startActivity(intent);
        finish();
    }

    private void openDialogActivity(String type) {
        Intent intent = new Intent(SplashActivity.this, DialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", type);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStartPairing() {
        pb.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnected() {
        pb.setVisibility(View.GONE);
        loadSettings();
    }

    @Override
    public void onUnauthorized(String message) {
        pb.setVisibility(View.GONE);
        errorDialog(getString(R.string.error_unauthorized_access), message);
    }

    @Override
    public void onReconnect() {
        Toast.makeText(SplashActivity.this, "Please wait a minute", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError() {
        pb.setVisibility(View.GONE);
        errorDialog(getString(R.string.error_server), getString(R.string.error_server_not_connected));
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void errorDialog(String title, String message) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this, R.style.dialogTheme);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        if (title.equals(getString(R.string.error_internet_not_connected)) || title.equals(getString(R.string.error_server_not_connected))) {
            alertDialog.setNegativeButton(getString(R.string.retry), (dialog, which) -> loadAboutData());
        }
        alertDialog.setPositiveButton(getString(R.string.exit), (dialog, which) -> finish());
        alertDialog.show();
    }

    @Override
    public void onDestroy() {
        try {
            dbHelper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}