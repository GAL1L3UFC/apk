package nemosofts.online.live.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.nemosofts.AppCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.theme.ColorUtils;

import java.util.Objects;

import nemosofts.online.live.R;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.dialog.DModeDialog;
import nemosofts.online.live.dialog.MaintenanceDialog;
import nemosofts.online.live.dialog.UpgradeDialog;
import nemosofts.online.live.dialog.VpnDialog;

public class DialogActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.rl_splash).setBackgroundColor(ColorUtils.colorBackground(this));

        String from = getIntent().getStringExtra("from");
        switch (Objects.requireNonNull(from)) {
            case Callback.DIALOG_TYPE_UPDATE ->
                    new UpgradeDialog(this, new UpgradeDialog.UpgradeListener() {
                        @Override
                        public void onCancel() {
                            openMainActivity();
                        }

                        @Override
                        public void onDo() {

                        }
                    });
            case Callback.DIALOG_TYPE_MAINTENANCE -> new MaintenanceDialog(this);
            case Callback.DIALOG_TYPE_DEVELOPER -> new DModeDialog(this);
            case Callback.DIALOG_TYPE_VPN -> new VpnDialog(this);
            default -> openMainActivity();
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(DialogActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public int setLayoutResourceId() {
        return R.layout.activity_splash;
    }

    @Override
    public int setAppCompat() {
        return AppCompat.COMPAT();
    }
}