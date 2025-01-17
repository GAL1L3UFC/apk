package nemosofts.online.live.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.nemosofts.AppCompat;
import androidx.nemosofts.AppCompatActivity;

import nemosofts.online.live.BuildConfig;
import nemosofts.online.live.R;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.interfaces.InterAdListener;
import nemosofts.online.live.utils.IfSupported;
import nemosofts.online.live.utils.helper.Helper;

public class AboutUsActivity extends AppCompatActivity implements InterAdListener {

    private TextView tv_author, tv_email, tv_website, tv_contact, tv_description, tv_version;

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

        Helper helper = new Helper(this, this);

        tv_author = findViewById(R.id.tv_company);
        tv_email = findViewById(R.id.tv_email);
        tv_website = findViewById(R.id.tv_website);
        tv_contact = findViewById(R.id.tv_contact);
        tv_description = findViewById(R.id.tv_app_des);
        tv_version = findViewById(R.id.tv_version);

        findViewById(R.id.ll_share).setOnClickListener(v -> helper.showInterAd(0, getResources().getString(R.string.share)));
        findViewById(R.id.ll_rate).setOnClickListener(v -> helper.showInterAd(0, getResources().getString(R.string.rate_the_app)));
        findViewById(R.id.ll_domain).setOnClickListener(v -> helper.showInterAd(0, getResources().getString(R.string.website)));
        findViewById(R.id.ll_contact).setOnClickListener(v -> helper.showInterAd(0, getResources().getString(R.string.contact)));
        findViewById(R.id.ll_email).setOnClickListener(v -> helper.showInterAd(0, getResources().getString(R.string.email)));
        findViewById(R.id.ll_more).setOnClickListener(v -> helper.showInterAd(0, getResources().getString(R.string.more_apps)));

        setAboutUs();
    }

    private void setAboutUs() {
        tv_author.setText(!Callback.itemAbout.getAuthor().trim().isEmpty() ? Callback.itemAbout.getAuthor() : "");
        tv_email.setText(!Callback.itemAbout.getEmail().trim().isEmpty() ? Callback.itemAbout.getEmail() : "");
        tv_website.setText(!Callback.itemAbout.getWebsite().trim().isEmpty() ? Callback.itemAbout.getWebsite() : "");
        tv_contact.setText(!Callback.itemAbout.getContact().trim().isEmpty() ? Callback.itemAbout.getContact() : "");
        tv_description.setText(!Callback.itemAbout.getAppDesc().trim().isEmpty() ? Callback.itemAbout.getAppDesc() : "");
        tv_version.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    public int setLayoutResourceId() {
        return R.layout.activity_about_us;
    }

    @Override
    public int setAppCompat() {
        return AppCompat.COMPAT();
    }

    @Override
    public void onClick(int position, String type) {
        if (getResources().getString(R.string.share).equals(type)){
            final String appName = getPackageName();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.app_name));
            sendIntent.putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?id=" + appName);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share"));
        }
        else if (getResources().getString(R.string.rate_the_app).equals(type)){
            final String appName = getPackageName();
            shareUrl("http://play.google.com/store/apps/details?id=" + appName);
        }
        else if (getResources().getString(R.string.website).equals(type)){
            shareUrl(Callback.itemAbout.getWebsite());
        }
        else if (getResources().getString(R.string.contact).equals(type)){
            String contact = Callback.itemAbout.getContact(); // use country code with your phone number
            if (!contact.isEmpty()){
                String url = "https://api.whatsapp.com/send?phone=" + contact;
                try {
                    PackageManager pm = getPackageManager();
                    pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (getResources().getString(R.string.email).equals(type)){
            String email = Callback.itemAbout.getEmail();
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email,});
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, "note");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
        else if (getResources().getString(R.string.more_apps).equals(type)){
            shareUrl(Callback.moreApps);
        }
    }

    private void shareUrl(@NonNull String web_url) {
        if (web_url.contains("http://") || web_url.contains("https://")){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(web_url)));
        } else if (!web_url.isEmpty()){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://"+web_url)));
        } else {
            Toast.makeText(AboutUsActivity.this, "Invalid URL", Toast.LENGTH_SHORT).show();
        }
    }
}