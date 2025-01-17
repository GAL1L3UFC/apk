package nemosofts.online.live.adapter.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.nemosofts.view.enchanted.EnchantedViewPager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import nemosofts.online.live.R;
import nemosofts.online.live.activity.MainActivity;
import nemosofts.online.live.activity.PostIDActivity;
import nemosofts.online.live.activity.VideoDetailsActivity;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.fragment.Online.FragmentCategories;
import nemosofts.online.live.fragment.Online.FragmentEvent;
import nemosofts.online.live.fragment.Online.FragmentLatest;
import nemosofts.online.live.fragment.Online.FragmentRecent;
import nemosofts.online.live.fragment.Online.FragmentSectionLive;
import nemosofts.online.live.fragment.Online.FragmentTrending;
import nemosofts.online.live.interfaces.InterAdListener;
import nemosofts.online.live.item.ItemPost;
import nemosofts.online.live.utils.helper.Helper;
import nemosofts.online.live.utils.recycler.RecyclerItemClickListener;

public class AdapterHome extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    Helper helper;
    List<ItemPost> arrayList;
    int clickPos = 0;

    final int VIEW_PROG = 0;
    final int VIEW_BANNER = 1;
    final int VIEW_CATEGORIES = 2;
    final int VIEW_EVENT = 3;
    final int VIEW_LIVE = 4;
    final int VIEW_RECENT = 5;
    final int VIEW_ADS = 6;

    Boolean ads = true;

    public AdapterHome(Context context, List<ItemPost> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        helper = new Helper(context, interAdListener);
    }

    static class BannerHolder extends RecyclerView.ViewHolder {

        EnchantedViewPager enchantedViewPager;
        HomePagerAdapter homePagerAdapter;

        BannerHolder(View view) {
            super(view);
            enchantedViewPager = view.findViewById(R.id.viewPager_home);
            enchantedViewPager.useAlpha();
            enchantedViewPager.useScale();
            enchantedViewPager.setPageMargin(-5);
        }
    }

    class CategoriesHolder extends RecyclerView.ViewHolder {

        RecyclerView rv_cat;
        AdapterHomeCategories adapterHomeCategories;
        TextView tv_title;
        LinearLayout ll_home_view_all;

        CategoriesHolder(View view) {
            super(view);
            rv_cat = view.findViewById(R.id.rv_home_cat);
            tv_title = view.findViewById(R.id.tv_home_title);
            ll_home_view_all = view.findViewById(R.id.ll_home_view_all);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv_cat.setLayoutManager(linearLayoutManager);
            rv_cat.setItemAnimator(new DefaultItemAnimator());
        }
    }

    class EventHolder extends RecyclerView.ViewHolder {

        RecyclerView rv_event;
        AdapterHomeEvent adapterHomeEvent;
        TextView tv_title;
        LinearLayout ll_home_view_all;

        EventHolder(View view) {
            super(view);
            rv_event = view.findViewById(R.id.rv_home_cat);
            tv_title = view.findViewById(R.id.tv_home_title);
            ll_home_view_all = view.findViewById(R.id.ll_home_view_all);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            rv_event.setLayoutManager(linearLayoutManager);
            rv_event.setItemAnimator(new DefaultItemAnimator());
        }
    }

    class LiveHolder extends RecyclerView.ViewHolder {

        RecyclerView rv_live;
        AdapterHomeLive adapterHomeLive;
        TextView tv_title;
        LinearLayout ll_home_view_all;

        LiveHolder(View view) {
            super(view);
            rv_live = view.findViewById(R.id.rv_home_cat);
            tv_title = view.findViewById(R.id.tv_home_title);
            ll_home_view_all = view.findViewById(R.id.ll_home_view_all);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv_live.setLayoutManager(linearLayoutManager);
            rv_live.setItemAnimator(new DefaultItemAnimator());
        }
    }

    class RecentHolder extends RecyclerView.ViewHolder {

        RecyclerView rv_recent;
        TextView tv_title;
        AdapterHomeRecent adapterHomeRecent;
        LinearLayout ll_home_view_all;

        RecentHolder(View view) {
            super(view);
            rv_recent = view.findViewById(R.id.rv_home_cat);
            tv_title = view.findViewById(R.id.tv_home_title);
            ll_home_view_all = view.findViewById(R.id.ll_home_view_all);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv_recent.setLayoutManager(linearLayoutManager);
            rv_recent.setItemAnimator(new DefaultItemAnimator());
        }
    }

    static class LatestAds extends RecyclerView.ViewHolder {

        LinearLayout ll_adView;

        LatestAds(View view) {
            super(view);
            ll_adView = view.findViewById(R.id.ll_adView);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private static ProgressBar progressBar;

        private ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_BANNER) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_home_ui_banner, parent, false);
            return new BannerHolder(itemView);
        } else if (viewType == VIEW_CATEGORIES) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_home_ui_categories, parent, false);
            return new CategoriesHolder(itemView);
        } else if (viewType == VIEW_EVENT) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_home_ui_categories, parent, false);
            return new EventHolder(itemView);
        } else if (viewType == VIEW_LIVE) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_home_ui_categories, parent, false);
            return new LiveHolder(itemView);
        }  else if (viewType == VIEW_RECENT) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_home_ui_categories, parent, false);
            return new RecentHolder(itemView);
        } else if (viewType == VIEW_ADS) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_baner_ad, parent, false);
            return new LatestAds(itemView);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_progressbar, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof BannerHolder) {
            if (((BannerHolder) holder).homePagerAdapter == null) {
                ((BannerHolder) holder).enchantedViewPager.setFocusable(false);
                ((BannerHolder) holder).homePagerAdapter = new HomePagerAdapter(context, arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListBanner());
                ((BannerHolder) holder).enchantedViewPager.setAdapter(((BannerHolder) holder).homePagerAdapter);
                if (((BannerHolder) holder).homePagerAdapter.getCount() > 2) {
                    ((BannerHolder) holder).enchantedViewPager.setCurrentItem(1);
                }
            }
        }
        else if (holder instanceof RecentHolder) {
            ((RecentHolder) holder).tv_title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());

            ((RecentHolder) holder).adapterHomeRecent = new AdapterHomeRecent(context,arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListLive());
            ((RecentHolder) holder).rv_recent.setAdapter(((RecentHolder) holder).adapterHomeRecent);

            ((RecentHolder) holder).rv_recent.addOnItemTouchListener(new RecyclerItemClickListener(context, (view, position12) -> {
                clickPos = holder.getAbsoluteAdapterPosition();
                helper.showInterAd(position12, context.getString(R.string.live));
            }));

            ((RecentHolder) holder).ll_home_view_all.setOnClickListener(v -> {
                FragmentRecent f_recent = new FragmentRecent();
                FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                        .getFragments()
                        .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, f_recent, context.getString(R.string.recently));
                ft.addToBackStack(context.getString(R.string.recently));
                ft.commit();
                Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(context.getString(R.string.recently));
                ((MainActivity) context).bottomNavigationView(4);
            });
        }
        else if (holder instanceof CategoriesHolder) {

            ((CategoriesHolder) holder).tv_title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());

            ((CategoriesHolder) holder).adapterHomeCategories = new AdapterHomeCategories(arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListCategories());
            ((CategoriesHolder) holder).rv_cat.setAdapter(((CategoriesHolder) holder).adapterHomeCategories);

            ((CategoriesHolder) holder).rv_cat.addOnItemTouchListener(new RecyclerItemClickListener(context, (view, position1) -> {
                clickPos = holder.getAbsoluteAdapterPosition();
                helper.showInterAd(position1, context.getString(R.string.categories));
            }));

            ((CategoriesHolder) holder).ll_home_view_all.setOnClickListener(v -> {
                FragmentCategories f_albums = new FragmentCategories();
                Bundle bundle = new Bundle();
                bundle.putBoolean("ishome", true);
                bundle.putString("id", arrayList.get(holder.getAbsoluteAdapterPosition()).getId());
                f_albums.setArguments(bundle);
                FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                        .getFragments()
                        .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, f_albums, context.getString(R.string.categories));
                ft.addToBackStack(context.getString(R.string.categories));
                ft.commit();
                Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                ((MainActivity) context).bottomNavigationView(5);
            });
        }
        else if (holder instanceof EventHolder) {
            ((EventHolder) holder).tv_title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());

            ((EventHolder) holder).adapterHomeEvent = new AdapterHomeEvent(arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListEvent());
            ((EventHolder) holder).rv_event.setAdapter(((EventHolder) holder).adapterHomeEvent);

            ((EventHolder) holder).rv_event.addOnItemTouchListener(new RecyclerItemClickListener(context, (view, position1) -> {
                clickPos = holder.getAbsoluteAdapterPosition();
                helper.showInterAd(position1, context.getString(R.string.live_event));
            }));

            ((EventHolder) holder).ll_home_view_all.setOnClickListener(v -> {
                FragmentEvent f_event = new FragmentEvent();
                FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                        .getFragments()
                        .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, f_event, context.getString(R.string.live_event));
                ft.addToBackStack(context.getString(R.string.live_event));
                ft.commit();
                Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(context.getString(R.string.live_event));
                ((MainActivity) context).bottomNavigationView(5);
            });
        }
        else if (holder instanceof LiveHolder) {
            ((LiveHolder) holder).tv_title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());

            ((LiveHolder) holder).adapterHomeLive = new AdapterHomeLive(arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListLive());
            ((LiveHolder) holder).rv_live.setAdapter(((LiveHolder) holder).adapterHomeLive);

            ((LiveHolder) holder).rv_live.addOnItemTouchListener(new RecyclerItemClickListener(context, (view, position1) -> {
                clickPos = holder.getAbsoluteAdapterPosition();
                helper.showInterAd(position1, context.getString(R.string.live));
            }));

            ((LiveHolder) holder).ll_home_view_all.setOnClickListener(v -> {
                if (arrayList.get(holder.getAbsoluteAdapterPosition()).getSections().equals("recently")){
                    FragmentRecent f_recently = new FragmentRecent();
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, f_recently, context.getString(R.string.recently));
                    ft.addToBackStack(context.getString(R.string.recently));
                    ft.commit();
                    Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(context.getString(R.string.recently));
                    ((MainActivity) context).bottomNavigationView(4);
                } else if (arrayList.get(holder.getAbsoluteAdapterPosition()).getSections().equals("latest")){
                    FragmentLatest f_latest = new FragmentLatest();
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, f_latest, context.getString(R.string.latest));
                    ft.addToBackStack(context.getString(R.string.latest));
                    ft.commit();
                    Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(context.getString(R.string.latest));
                    ((MainActivity) context).bottomNavigationView(1);
                } else if (arrayList.get(holder.getAbsoluteAdapterPosition()).getSections().equals("trending")){
                    FragmentTrending f_most = new FragmentTrending();
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, f_most, context.getString(R.string.trending));
                    ft.addToBackStack(context.getString(R.string.trending));
                    ft.commit();
                    Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(context.getString(R.string.trending));
                    ((MainActivity) context).bottomNavigationView(2);
                } else {
                    FragmentSectionLive f_Section = new FragmentSectionLive();
                    Bundle bundle = new Bundle();
                    bundle.putString("id", arrayList.get(holder.getAbsoluteAdapterPosition()).getId());
                    f_Section.setArguments(bundle);
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, f_Section, context.getString(R.string.live));
                    ft.addToBackStack(context.getString(R.string.live));
                    ft.commit();
                    Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(((LiveHolder) holder).tv_title.getText().toString());
                    ((MainActivity) context).bottomNavigationView(5);
                }
            });
        }
        else if (holder instanceof LatestAds) {
            if (ads){
                ads = false;
                helper.showBannerAd(((LatestAds) holder).ll_adView, Callback.PAGE_HOME);
            }
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    public boolean isHeader(int position) {
        return arrayList.get(position) == null;
    }

    @Override
    public int getItemViewType(int position) {
        return switch (arrayList.get(position).getType()) {
            case "slider" -> VIEW_BANNER;
            case "live" -> VIEW_LIVE;
            case "recent" -> VIEW_RECENT;
            case "category" -> VIEW_CATEGORIES;
            case "event" -> VIEW_EVENT;
            case "ads" -> VIEW_ADS;
            default -> VIEW_PROG;
        };
    }

    InterAdListener interAdListener = (position, type) -> {
        if (type.equals(context.getString(R.string.live))) {
            Intent intent = new Intent(context, VideoDetailsActivity.class);
            intent.putExtra("post_id", arrayList.get(clickPos).getArrayListLive().get(position).getId());
            context.startActivity(intent);
        } else if (type.equals(context.getString(R.string.categories))){
            Intent intent = new Intent(context, PostIDActivity.class);
            intent.putExtra("page_type", context.getString(R.string.categories));
            intent.putExtra("id", arrayList.get(clickPos).getArrayListCategories().get(position).getId());
            intent.putExtra("name", arrayList.get(clickPos).getArrayListCategories().get(position).getName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else if (type.equals(context.getString(R.string.live_event))) {
            Intent intent = new Intent(context, VideoDetailsActivity.class);
            intent.putExtra("post_id", arrayList.get(clickPos).getArrayListEvent().get(position).getPostId());
            context.startActivity(intent);
        }
    };
}