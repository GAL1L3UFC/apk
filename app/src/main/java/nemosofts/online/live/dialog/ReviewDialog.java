package nemosofts.online.live.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Objects;

import nemosofts.online.live.R;
import nemosofts.online.live.asyncTask.GetRating;
import nemosofts.online.live.asyncTask.LoadRating;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.interfaces.RatingListener;
import nemosofts.online.live.utils.helper.Helper;
import nemosofts.online.live.utils.SharedPref;

public class ReviewDialog {

    private final Helper helper;
    private final SharedPref sharedPref;
    private Dialog dialog;
    private final Activity ctx;
    private final ProgressDialog progressDialog;
    private final RatingDialogListener listener;

    public ReviewDialog(Activity ctx, RatingDialogListener listener) {
        this.ctx = ctx;
        this.listener = listener;
        helper = new Helper(ctx);
        sharedPref = new SharedPref(ctx);
        progressDialog = new ProgressDialog(ctx, R.style.dialogTheme);
        progressDialog.setMessage(ctx.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    public void showDialog(String id, String userRating, String userMessage) {
        dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_review);

        final TextView tv_rate = dialog.findViewById(R.id.tv_rate);
        final RatingBar ratingBar = dialog.findViewById(R.id.rb_add);
        final EditText et_messages = dialog.findViewById(R.id.et_messages);
        final ProgressBar pb_rate = dialog.findViewById(R.id.pb_rate);

        ratingBar.setStepSize(Float.parseFloat("1"));

        if(sharedPref.isLogged()) {
            if (userRating.equals("") || userRating.equals("0")) {
                new GetRating(new RatingListener() {
                    @Override
                    public void onStart() {
                        pb_rate.setVisibility(View.VISIBLE);
                        ratingBar.setEnabled(false);
                        et_messages.setEnabled(false);
                    }

                    @Override
                    public void onEnd(String success, String rateSuccess, String message, int rating) {
                        ratingBar.setEnabled(true);
                        et_messages.setEnabled(true);
                        pb_rate.setVisibility(View.GONE);
                        if (rating > 0) {
                            ratingBar.setRating(rating);
                            et_messages.setText(message);
                            tv_rate.setText(ctx.getString(R.string.thanks_for_rating));
                            listener.onGetRating(String.valueOf(rating),message);
                        } else {
                            ratingBar.setRating(1);
                        }
                    }
                }, helper.getAPIRequest(Callback.METHOD_GET_RATINGS, 0, id,"","","",sharedPref.getUserId(),"","","","","","","",null)).execute();
            } else {
                if (Integer.parseInt(userRating) != 0 && !userRating.isEmpty()) {
                    tv_rate.setText(ctx.getString(R.string.thanks_for_rating));
                    et_messages.setText(userMessage);
                    ratingBar.setRating(Integer.parseInt(userRating));
                } else {
                    ratingBar.setRating(1);
                }
            }
        } else {
            ratingBar.setRating(1);
        }
        dialog.findViewById(R.id.tv_cancel).setOnClickListener(view -> dismissDialog());
        dialog.findViewById(R.id.iv_close).setOnClickListener(view -> dismissDialog());
        dialog.findViewById(R.id.tv_submit).setOnClickListener(view -> {
            if (ratingBar.getRating() != 0) {
                if(et_messages.getText().toString().trim().isEmpty()) {
                    et_messages.setError(ctx.getString(R.string.report_message));
                    et_messages.requestFocus();
                } else {
                    if(sharedPref.isLogged()) {
                        loadRatingApi(String.valueOf((int) ratingBar.getRating()), et_messages.getText().toString(), id);
                    } else {
                        helper.clickLogin();
                    }
                }
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.select_rating), Toast.LENGTH_SHORT).show();
            }
        });
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void loadRatingApi(final String rate, final String report, String id) {
        if (helper.isNetworkAvailable()) {
            LoadRating loadRating = new LoadRating(new RatingListener() {

                @Override
                public void onStart() {
                    progressDialog.show();
                    listener.onShow();
                }

                @Override
                public void onEnd(String success, String rateSuccess, String message, int rating) {
                    listener.onDismiss(success, rateSuccess, message, rating, rate, report);
                    dismissDialog();
                }
            }, helper.getAPIRequest(Callback.METHOD_RATINGS, 0, id,"","",report, sharedPref.getUserId(),"","","","","",rate,"",null));
            loadRating.execute();
        }else {
            Toast.makeText(ctx, ctx.getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }
    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public interface RatingDialogListener {
        void onShow();
        void onGetRating(String rating, String message);
        void onDismiss(String success, String rateSuccess, String message, int rating, String userRating , String userMessage);
    }
}
