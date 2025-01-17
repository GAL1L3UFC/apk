package nemosofts.online.live.adapter.Home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.nemosofts.view.RoundedImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import nemosofts.online.live.R;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.item.ItemData;

public class AdapterHomeLive extends RecyclerView.Adapter<AdapterHomeLive.MyViewHolder> {

    private final List<ItemData> arrayList;

    public AdapterHomeLive(List<ItemData> arrayList) {
        this.arrayList = arrayList;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout rl_live_tv;
        TextView tv_live, tv_live_premium;
        RoundedImageView iv_live;

        MyViewHolder(View view) {
            super(view);
            rl_live_tv = view.findViewById(R.id.rl_live_tv);
            tv_live = view.findViewById(R.id.tv_live);
            iv_live = view.findViewById(R.id.iv_live);
            tv_live_premium = view.findViewById(R.id.tv_live_premium);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_home_live, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.tv_live.setText(arrayList.get(position).getTitle());
        Picasso.get()
                .load(arrayList.get(position).getThumb())
                .centerCrop()
                .resize(300,300)
                .placeholder(R.drawable.material_design_default)
                .into(holder.iv_live);

        if (Boolean.TRUE.equals(Callback.isPurchases)){
            holder.tv_live_premium.setVisibility(View.GONE);
        } else {
            holder.tv_live_premium.setVisibility(arrayList.get(position).getIsPremium() ? View.VISIBLE : View.GONE);
        }

        holder.rl_live_tv.setOnClickListener(v -> {

        });
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}