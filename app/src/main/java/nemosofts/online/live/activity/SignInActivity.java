package nemosofts.online.live.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.nemosofts.AppCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.view.SmoothCheckBox;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.jetbrains.annotations.Contract;

import nemosofts.online.live.R;
import nemosofts.online.live.asyncTask.LoadLogin;
import nemosofts.online.live.asyncTask.LoadRegister;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.dialog.VerifyDialog;
import nemosofts.online.live.interfaces.LoginListener;
import nemosofts.online.live.interfaces.SocialLoginListener;
import nemosofts.online.live.utils.IfSupported;
import nemosofts.online.live.utils.helper.Helper;
import nemosofts.online.live.utils.SharedPref;

public class SignInActivity extends AppCompatActivity {

    private String from = "";
    private Helper helper;
    private SharedPref sharedPref;
    private EditText et_login_email;
    private EditText et_login_password;
    private SmoothCheckBox remember_me;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private Boolean isVisibility = false;
    private RelativeLayout coordinatorLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IfSupported.IsRTL(this);
        IfSupported.IsScreenshot(this);
        IfSupported.keepScreenOn(this);

        mAuth = FirebaseAuth.getInstance();
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception e) {
            e.printStackTrace();
        }

        from = getIntent().getStringExtra("from");

        helper = new Helper(this);
        sharedPref = new SharedPref(this);

        progressDialog = new ProgressDialog(SignInActivity.this, R.style.dialogTheme);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        coordinatorLayout = findViewById(R.id.rl);
        remember_me = findViewById(R.id.cb_remember_me);
        et_login_email = findViewById(R.id.et_login_email);
        et_login_password = findViewById(R.id.et_login_password);

        if(Boolean.TRUE.equals(sharedPref.getIsRemember())) {
            et_login_email.setText(sharedPref.getEmail());
            et_login_password.setText(sharedPref.getPassword());
        }

        findViewById(R.id.ll_checkbox).setOnClickListener(v -> remember_me.setChecked(!remember_me.isChecked()));
        findViewById(R.id.tv_login_btn).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.tv_login_signup).setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        findViewById(R.id.tv_skip_btn).setOnClickListener(view -> {
            sharedPref.setIsFirst(false);
            openMainActivity();
        });
        findViewById(R.id.tv_forgot_pass).setOnClickListener(view -> startActivity(new Intent(SignInActivity.this, ForgotPasswordActivity.class)));

        LinearLayout ll_google = findViewById(R.id.ll_login_google);
        ll_google.setVisibility(Boolean.TRUE.equals(Callback.isGoogleLogin) ? View.VISIBLE : View.GONE);
        ll_google.setOnClickListener(view -> {
            if (helper.isNetworkAvailable()) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(SignInActivity.this, gso);

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 112);
            } else {
                showSnackBar(false, getString(R.string.error_internet_not_connected));
            }
        });

        ImageView iv_visibility = findViewById(R.id.iv_visibility);
        iv_visibility.setImageResource(Boolean.TRUE.equals(isVisibility) ? R.drawable.ic_login_visibility : R.drawable.ic_login_visibility_off);
        iv_visibility.setOnClickListener(v -> {
            isVisibility = !isVisibility;
            iv_visibility.setImageResource(Boolean.TRUE.equals(isVisibility) ? R.drawable.ic_login_visibility : R.drawable.ic_login_visibility_off);
            et_login_password.setTransformationMethod(Boolean.TRUE.equals(isVisibility) ? HideReturnsTransformationMethod.getInstance()  : PasswordTransformationMethod.getInstance());
        });
    }

    private void attemptLogin() {
        et_login_email.setError(null);
        et_login_password.setError(null);

        // Store values at the time of the login attempt.
        String email = et_login_email.getText().toString();
        String password = et_login_password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            et_login_password.setError(getString(R.string.error_password_sort));
            focusView = et_login_password;
            cancel = true;
        }
        if (et_login_password.getText().toString().endsWith(" ")) {
            et_login_password.setError(getString(R.string.error_pass_end_space));
            focusView = et_login_password;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            et_login_email.setError(getString(R.string.error_cannot_empty));
            focusView = et_login_email;
            cancel = true;
        } else if (!isEmailValid(email)) {
            et_login_email.setError(getString(R.string.error_invalid_email));
            focusView = et_login_email;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            loadLogin();
        }
    }

    private boolean isEmailValid(@NonNull String email) {
        return email.contains("@") && !email.contains(" ");
    }

    @Contract(pure = true)
    private boolean isPasswordValid(@NonNull String password) {
        return password.length() > 0;
    }

    private void loadLogin() {
        if (helper.isNetworkAvailable()) {
            LoadLogin loadLogin = new LoadLogin(new LoginListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String loginSuccess, String message, String user_id, String user_name, String user_gender, String user_phone,String profile_img) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        if (loginSuccess.equals("1")) {
                            sharedPref.setLoginDetails(user_id, user_name, user_phone, et_login_email.getText().toString(), user_gender, profile_img, "", remember_me.isChecked(), et_login_password.getText().toString(), Callback.LOGIN_TYPE_NORMAL);
                            sharedPref.setIsFirst(false);
                            sharedPref.setIsLogged(true);
                            sharedPref.setIsAutoLogin(true);

                            Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (from.equals("app")) {
                                finish();
                            } else {
                                openMainActivity();
                            }
                        } else {
                            Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showSnackBar(false, getString(R.string.error_server_not_connected));
                    }
                }
            }, helper.getAPIRequest(Callback.METHOD_LOGIN, 0,"","","","","","",et_login_email.getText().toString(),"","",et_login_password.getText().toString(),"", Callback.LOGIN_TYPE_NORMAL,null));
            loadLogin.execute();
        } else {
            showSnackBar(false, getString(R.string.error_internet_not_connected));
        }
    }

    private void loadLoginSocial(final String name, String email, final String authId) {
        if (helper.isNetworkAvailable()) {
            LoadRegister loadRegister = new LoadRegister(new SocialLoginListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String registerSuccess, String message, String user_id, String user_name, String email, String auth_id) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        if (registerSuccess.equals("1")) {
                            sharedPref.setLoginDetails(user_id, user_name, "", email, "", "", authId, remember_me.isChecked(), "", Callback.LOGIN_TYPE_GOOGLE);
                            sharedPref.setIsFirst(false);
                            sharedPref.setIsLogged(true);
                            sharedPref.setIsAutoLogin(true);

                            Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (from.equals("app")) {
                                finish();
                            } else {
                                openMainActivity();
                            }
                        } else  if (registerSuccess.equals("-1")) {
                            new VerifyDialog(SignInActivity.this, getString(R.string.error_unauthorized_access), message);
                        } else {
                            if (message.contains("already") || message.contains("Invalid email format")) {
                                et_login_email.setError(message);
                                et_login_email.requestFocus();
                            } else {
                                Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
                            }

                            try {
                                FirebaseAuth.getInstance().signOut();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        showSnackBar(false, getString(R.string.error_server_not_connected));
                    }
                }
            }, helper.getAPIRequest(Callback.METHOD_REGISTER, 0, "", "", "", "", "", name, email, "", "", "", authId, Callback.LOGIN_TYPE_GOOGLE, null));
            loadRegister.execute();
        } else {
            showSnackBar(false, getString(R.string.error_internet_not_connected));
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null){
                    loadLoginSocial(user.getDisplayName(), user.getEmail(), user.getUid());
                } else {
                    Toast.makeText(SignInActivity.this, "Failed to Sign IN", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SignInActivity.this, "Failed to Sign IN", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 112) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            try {
                if (resultCode != 0) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    firebaseAuthWithGoogle(task.getResult().getIdToken());
                } else {
                    Toast.makeText(SignInActivity.this, getString(R.string.error_login_google), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(SignInActivity.this, getString(R.string.error_login_google), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showSnackBar(boolean success, String message) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundResource(success ? R.drawable.snack_bar_success : R.drawable.snack_bar_error);
        snackbar.show();
    }

    @Override
    public int setLayoutResourceId() {
        return R.layout.activity_sign_in;
    }

    @Override
    public int setAppCompat() {
        return AppCompat.COMPAT_N();
    }
}