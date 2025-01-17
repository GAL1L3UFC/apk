package nemosofts.online.live.adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import nemosofts.online.live.R;
import nemosofts.online.live.asyncTask.LoadStatus;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.interfaces.SuccessListener;
import nemosofts.online.live.item.ItemNotify;
import nemosofts.online.live.utils.SharedPref;
import nemosofts.online.live.utils.helper.Helper;


public class AdapterNotify extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ItemNotify> arrayList;
    public final Context ctx;
    private final Helper helper;
    private final SharedPref sharedPref;
    private final int VIEW_ITEM = 1;

    public AdapterNotify(Context ctx, List<ItemNotify> arrayList) {
        this.arrayList = arrayList;
        this.ctx = ctx;
        helper = new Helper(ctx);
        sharedPref = new SharedPref(ctx);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView_name;
        private final TextView textView_comment;
        private final TextView textView_date;
        private final RelativeLayout rl_not_close;

        MyViewHolder(View view) {
            super(view);
            textView_name = view.findViewById(R.id.tv_not_title);
            textView_comment = view.findViewById(R.id.tv_not_note);
            textView_date = view.findViewById(R.id.tv_not_date);
            rl_not_close = view.findViewById(R.id.rl_not_close);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {

        @SuppressLint("StaticFieldLeak")
        private static ProgressBar progressBar;

        private ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_notification, parent, false);
            return new MyViewHolder(itemView);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_progressbar, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {

            ((MyViewHolder) holder).textView_name.setText(arrayList.get(position).getName());
            ((MyViewHolder) holder).textView_comment.setText(arrayList.get(position).getNot());
            ((MyViewHolder) holder).textView_date.setText(arrayList.get(position).getDate());
            ((MyViewHolder) holder).textView_date.setText(arrayList.get(position).getDate());

            ((MyViewHolder) holder).rl_not_close.setOnClickListener(view -> {
                if (position <= arrayList.size()) {
                    loadRemove(holder.getAbsoluteAdapterPosition());
                }
            });

        } else {
            if (getItemCount() == 1) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void loadRemove(int pos) {
        if (helper.isNetworkAvailable()) {
            ProgressDialog progressDialog = new ProgressDialog(ctx, R.style.dialogTheme);
            progressDialog.setMessage(ctx.getString(R.string.loading));
            LoadStatus loadStatus = new LoadStatus(new SuccessListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String status, String message) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        if (status.equals("1")) {
                            arrayList.remove(pos);
                            notifyItemRemoved(pos);
                        }
                        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ctx, ctx.getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }
            }, helper.getAPIRequest(Callback.METHOD_REMOVE_NOTIFICATION, 0,arrayList.get(pos).getId(),"","","", sharedPref.getUserId(),"","","","","","","",null));
            loadStatus.execute();
        } else {
            Toast.makeText(ctx, ctx.getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return isProgressPos(position) ? 0 : VIEW_ITEM;
    }

    private boolean isProgressPos(int position) {
        return position == arrayList.size();
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }
}