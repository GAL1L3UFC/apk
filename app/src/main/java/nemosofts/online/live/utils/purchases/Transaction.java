package nemosofts.online.live.utils.purchases;

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import nemosofts.online.live.R;
import nemosofts.online.live.asyncTask.LoadStatus;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.interfaces.SuccessListener;
import nemosofts.online.live.utils.helper.Helper;
import nemosofts.online.live.utils.SharedPref;

public class Transaction {

    private final ProgressDialog pDialog;
    private final Activity mContext;
    private final Helper helper;

    public Transaction(Activity context) {
        this.mContext = context;
        helper = new Helper(mContext);
        pDialog = new ProgressDialog(mContext, R.style.dialogTheme);
    }

    public void purchasedItem(String planId, String planName, String planPrice, String planDuration, String planCurrencyCode) {
        if (helper.isNetworkAvailable()) {
            LoadStatus transaction = new LoadStatus(new SuccessListener() {
                @Override
                public void onStart() {
                    showProgressDialog();
                }

                @Override
                public void onEnd(String success, String status, String message) {
                    dismissProgressDialog();
                    if (success.equals("1")) {
                        if (status.equals("1")) {
                            ActivityCompat.recreate(mContext);
                        }
                        Toast.makeText(mContext,message,Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext,mContext.getString(R.string.error_server_not_connected),Toast.LENGTH_SHORT).show();
                    }
                }
            }, helper.getAPIRequest(Callback.TRANSACTION_URL, 0, planId, planName, planPrice,planDuration, new SharedPref(mContext).getUserId(), planCurrencyCode, "", "", "", "", "", "", null));
            transaction.execute();
        } else {
            Toast.makeText(mContext,mContext.getString(R.string.error_internet_not_connected),Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressDialog() {
        pDialog.setMessage(mContext.getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void dismissProgressDialog() {
        if (pDialog.isShowing()){
            pDialog.dismiss();
        }
    }

}
