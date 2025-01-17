package nemosofts.online.live.adapter.Home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.nemosofts.view.RoundedImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import nemosofts.online.live.R;
import nemosofts.online.live.item.ItemData;

public class AdapterHomeRecent extends RecyclerView.Adapter<AdapterHomeRecent.MyViewHolder> {

    Context context;
    List<ItemData> arrayList;

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv_recent_title;
        private final RoundedImageView iv_recently;

        MyViewHolder(View view) {
            super(view);
            tv_recent_title = view.findViewById(R.id.tv_recent_title);
            iv_recently = view.findViewById(R.id.iv_recently);
        }
    }

    public AdapterHomeRecent(Context context, List<ItemData> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recently, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.tv_recent_title.setText(arrayList.get(position).getTitle());

        Picasso.get()
                .load(arrayList.get(position).getThumb())
                .resize(300,300)
                .placeholder(R.drawable.material_design_default)
                .into(holder.iv_recently);
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