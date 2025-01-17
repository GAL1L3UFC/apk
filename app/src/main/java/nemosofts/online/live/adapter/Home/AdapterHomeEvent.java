package nemosofts.online.live.adapter.Home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.nemosofts.view.RoundedImageView;
import androidx.recyclerview.widget.RecyclerView;
;
import com.squareup.picasso.Picasso;

import java.util.List;

import nemosofts.online.live.R;
import nemosofts.online.live.item.ItemEvent;

public class AdapterHomeEvent extends RecyclerView.Adapter<AdapterHomeEvent.MyViewHolder> {

    private final List<ItemEvent> arrayList;

    public AdapterHomeEvent(List<ItemEvent> arrayList) {
        this.arrayList = arrayList;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final RoundedImageView iv_team_one, iv_team_two;
        private final TextView tv_event_title, tv_team_one, tv_team_two;

        MyViewHolder(View view) {
            super(view);

            tv_event_title = view.findViewById(R.id.tv_event_title);

            tv_team_one = view.findViewById(R.id.tv_team_one);
            iv_team_one = view.findViewById(R.id.iv_team_one);

            tv_team_two = view.findViewById(R.id.tv_team_two);
            iv_team_two = view.findViewById(R.id.iv_team_two);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_home_event, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.tv_event_title.setText(arrayList.get(position).getTitle());

        holder.tv_team_one.setText(arrayList.get(position).getTitleOne());
        Picasso.get()
                .load(arrayList.get(position).getThumbOne())
                .centerCrop()
                .resize(300,300)
                .placeholder(R.drawable.material_design_default)
                .into(holder.iv_team_one);

        holder.tv_team_two.setText(arrayList.get(position).getTitleTwo());
        Picasso.get()
                .load(arrayList.get(position).getThumbTwo())
                .centerCrop()
                .resize(300,300)
                .placeholder(R.drawable.material_design_default)
                .into(holder.iv_team_two);
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