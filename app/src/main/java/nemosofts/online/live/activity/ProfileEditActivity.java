package nemosofts.online.live.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.nemosofts.AppCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.view.RoundedImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import nemosofts.online.live.R;
import nemosofts.online.live.asyncTask.LoadStatus;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.interfaces.SuccessListener;
import nemosofts.online.live.utils.IfSupported;
import nemosofts.online.live.utils.helper.Helper;
import nemosofts.online.live.utils.SharedPref;

public class ProfileEditActivity extends AppCompatActivity {

    private Helper helper;
    private SharedPref sharedPref;
    private EditText editText_name;
    private EditText editText_email;
    private EditText editText_phone;
    private EditText editText_pass;
    private EditText editText_cPass;
    private ProgressDialog progressDialog;
    private RoundedImageView iv_profile;
    private String imagePath = "";
    private Bitmap bitmap_upload;
    private final int PICK_IMAGE_REQUEST = 1;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IfSupported.IsRTL(this);
        IfSupported.IsScreenshot(this);
        IfSupported.keepScreenOn(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());

        progressDialog = new ProgressDialog(ProfileEditActivity.this, R.style.dialogTheme);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        helper = new Helper(this);
        sharedPref = new SharedPref(this);

        editText_name = findViewById(R.id.editText_profedit_name);
        editText_email = findViewById(R.id.editText_profedit_email);
        editText_phone = findViewById(R.id.editText_profedit_phone);
        editText_pass = findViewById(R.id.editText_profedit_password);
        editText_cPass = findViewById(R.id.editText_profedit_cpassword);

        if(sharedPref.getLoginType().equals(Callback.LOGIN_TYPE_NORMAL)) {
            editText_cPass.setEnabled(true);
            editText_pass.setEnabled(true);
        } else {
            editText_cPass.setEnabled(false);
            editText_pass.setEnabled(false);
        }

        iv_profile = findViewById(R.id.iv_profile_edit);
        try {
            Picasso.get()
                    .load(sharedPref.getProfileImages())
                    .placeholder(R.drawable.user_photo)
                    .into(iv_profile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        findViewById(R.id.rl_profile_edit).setOnClickListener(v -> {
            if (checkPer()) {
                pickImage();
            }
        });
        findViewById(R.id.ll_update_btn).setOnClickListener(view -> {
            if (validate()) {
                loadUpdateProfile();
            }
        });
        setProfileVar();
    }

    public void setProfileVar() {
        editText_name.setText(sharedPref.getUserName());
        editText_phone.setText(sharedPref.getUserMobile());
        editText_email.setText(sharedPref.getEmail());
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image)), PICK_IMAGE_REQUEST);
    }

    @NonNull
    private Boolean validate() {
        editText_name.setError(null);
        editText_email.setError(null);
        editText_cPass.setError(null);
        if (editText_name.getText().toString().trim().isEmpty()) {
            editText_name.setError(getString(R.string.error_cannot_empty));
            editText_name.requestFocus();
            return false;
        } else if (editText_email.getText().toString().trim().isEmpty()) {
            editText_email.setError(getString(R.string.error_password));
            editText_email.requestFocus();
            return false;
        } else if (editText_pass.getText().toString().endsWith(" ")) {
            editText_pass.setError(getString(R.string.error_pass_end_space));
            editText_pass.requestFocus();
            return false;
        } else if (!editText_pass.getText().toString().trim().equals(editText_cPass.getText().toString().trim())) {
            editText_cPass.setError(getString(R.string.error_password));
            editText_cPass.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void loadUpdateProfile() {
        if (helper.isNetworkAvailable()) {
            LoadStatus loadProfileEdit = new LoadStatus(new SuccessListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String registerSuccess, String message) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        if (registerSuccess.equals("1")) {
                            updateArray();
                            Callback.isProfileUpdate = true;
                            finish();
                            Toast.makeText(ProfileEditActivity.this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            if (message.contains("Email address already used")) {
                                editText_email.setError(message);
                                editText_email.requestFocus();
                            }
                        }
                    } else {
                        Toast.makeText(ProfileEditActivity.this, getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }
            }, helper.getAPIRequest(Callback.METHOD_EDIT_PROFILE, 0, "", "", "", "", sharedPref.getUserId(), editText_name.getText().toString(), editText_email.getText().toString(), editText_phone.getText().toString(), "", editText_pass.getText().toString(), "", "", null));
            loadProfileEdit.execute();
        } else {
            Toast.makeText(ProfileEditActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateArray() {
        sharedPref.setUserName(editText_name.getText().toString());
        sharedPref.setEmail(editText_email.getText().toString());
        sharedPref.setUserMobile(editText_phone.getText().toString());
        if (!editText_pass.getText().toString().equals("")) {
            sharedPref.setRemember(false);
        }
    }

    public void uploadImage() {
        if (helper.isNetworkAvailable()) {
            LoadStatus loadStatus = new LoadStatus(new SuccessListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String status, String message) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        Callback.isProfileUpdate = true;
                        imagePath = "";
                        bitmap_upload = null;
                        Toast.makeText(ProfileEditActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileEditActivity.this, getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }
            }, helper.getAPIRequest(Callback.METHOD_USER_IMAGES_UPDATE, 0, "", "", "", "", sharedPref.getUserId(), "", "", "", "", "", "", "", new File(imagePath)));
            loadStatus.execute();
        } else {
            Toast.makeText(this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    private Boolean checkPer() {
        if (Build.VERSION.SDK_INT >= 33){
            if ((ContextCompat.checkSelfPermission(ProfileEditActivity.this, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{READ_MEDIA_IMAGES}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return false;
            } else {
                return true;
            }
        } else if (Build.VERSION.SDK_INT >= 29) {
            if ((ContextCompat.checkSelfPermission(ProfileEditActivity.this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return false;
            } else {
                return true;
            }
        } else {
            if ((ContextCompat.checkSelfPermission(ProfileEditActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return false;
            }
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean canUseExternalStorage = false;
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                canUseExternalStorage = true;
            }
            if (!canUseExternalStorage) {
                Toast.makeText(ProfileEditActivity.this, getResources().getString(R.string.error_cannot_use_features), Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            imagePath = helper.getPathImage(uri);
            try {
                bitmap_upload = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                iv_profile.setImageBitmap(bitmap_upload);
                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int setLayoutResourceId() {
        return R.layout.activity_profile_edit;
    }

    @Override
    public int setAppCompat() {
        return AppCompat.COMPAT();
    }
}