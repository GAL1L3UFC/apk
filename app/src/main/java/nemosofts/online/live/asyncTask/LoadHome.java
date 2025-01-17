package nemosofts.online.live.asyncTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.online.live.R;
import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.interfaces.HomeListener;
import nemosofts.online.live.item.ItemCat;
import nemosofts.online.live.item.ItemData;
import nemosofts.online.live.item.ItemEvent;
import nemosofts.online.live.item.ItemHomeSlider;
import nemosofts.online.live.item.ItemPost;
import nemosofts.online.live.utils.ApplicationUtil;
import nemosofts.online.live.utils.SharedPref;
import okhttp3.RequestBody;

public class LoadHome extends AsyncTask<String, String, String> {

    @SuppressLint("StaticFieldLeak")
    Context ctx;
    RequestBody requestBody;
    HomeListener homeListener;
    ArrayList<ItemPost> arrayListPost = new ArrayList<>();
    String message = "", successAPI = "1";
    SharedPref sharedPref;

    public LoadHome(Context ctx, HomeListener homeListener, RequestBody requestBody) {
        this.ctx = ctx;
        this.homeListener = homeListener;
        this.requestBody = requestBody;
        sharedPref = new SharedPref(ctx);
    }

    @Override
    protected void onPreExecute() {
        homeListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
            JSONObject mainJson = new JSONObject(json);

            try {

                JSONObject jsonObject = mainJson.getJSONObject(Callback.TAG_ROOT);

                ItemPost itemPost;
                String postTitle, postType, postId;

                if (jsonObject.has("slider")) {

                    JSONArray jsonArrayBanner = jsonObject.getJSONArray("slider");

                    postTitle = "Home Banners";
                    postType = "slider";
                    itemPost = new ItemPost("", postTitle, postType, "banners");

                    ArrayList<ItemHomeSlider> arrayListBanner = new ArrayList<>();
                    for (int i = 0; i < jsonArrayBanner.length(); i++) {
                        JSONObject objJsonBanner = jsonArrayBanner.getJSONObject(i);

                        String banner_id = objJsonBanner.getString("bid");
                        String banner_title = objJsonBanner.getString("banner_title");
                        String banner_desc = objJsonBanner.getString("banner_info");
                        String banner_image = objJsonBanner.getString("banner_image").replace(" ", "%20");
                        if (banner_image.equals("")) {
                            banner_image = "null";
                        }

                        arrayListBanner.add(new ItemHomeSlider(banner_id,banner_title,banner_desc,banner_image));
                    }
                    itemPost.setArrayListBanner(arrayListBanner);
                    arrayListPost.add(itemPost);
                }

                if (jsonObject.has("recently")) {

                    JSONArray jsonArrayRecent = jsonObject.getJSONArray("recently");

                    if (jsonArrayRecent.length() > 0) {

                        postTitle = ctx.getString(R.string.recently);
                        postType = "recent";
                        itemPost = new ItemPost("", postTitle, postType, "recently");

                        ArrayList<ItemData> arrayListRecent = new ArrayList<>();
                        for (int i = 0; i < jsonArrayRecent.length(); i++) {
                            JSONObject objJson = jsonArrayRecent.getJSONObject(i);

                            String id = objJson.getString("id");
                            String title = objJson.getString("live_title");
                            String image = objJson.getString("image").replace(" ", "%20");
                            if (image.equals("")) {
                                image = "null";
                            }
                            boolean isPremium = objJson.getBoolean("is_premium");

                            ItemData objItem = new ItemData(id,title,image,isPremium);
                            arrayListRecent.add(objItem);
                        }
                        itemPost.setArrayListLive(arrayListRecent);
                        arrayListPost.add(itemPost);
                    }
                }

                if (jsonObject.has("latest")) {

                    JSONArray jsonArrayLatest = jsonObject.getJSONArray("latest");

                    if (jsonArrayLatest.length() > 0) {

                        postTitle = ctx.getString(R.string.latest);
                        postType = "live";
                        itemPost = new ItemPost("", postTitle, postType, "latest");

                        ArrayList<ItemData> arrayListLatest = new ArrayList<>();
                        for (int i = 0; i < jsonArrayLatest.length(); i++) {
                            JSONObject objJson = jsonArrayLatest.getJSONObject(i);

                            String id = objJson.getString("id");
                            String title = objJson.getString("live_title");
                            String image = objJson.getString("image").replace(" ", "%20");
                            if (image.equals("")) {
                                image = "null";
                            }
                            boolean isPremium = objJson.getBoolean("is_premium");

                            ItemData objItem = new ItemData(id,title,image,isPremium);
                            arrayListLatest.add(objItem);
                        }
                        itemPost.setArrayListLive(arrayListLatest);
                        arrayListPost.add(itemPost);
                    }
                }

                if (jsonObject.has("trending")) {

                    JSONArray jsonArrayTrending = jsonObject.getJSONArray("trending");

                    if (jsonArrayTrending.length() > 0) {

                        postTitle = ctx.getString(R.string.trending);
                        postType = "live";
                        itemPost = new ItemPost("", postTitle, postType, "trending");

                        ArrayList<ItemData> arrayListTrending = new ArrayList<>();
                        for (int i = 0; i < jsonArrayTrending.length(); i++) {
                            JSONObject objJson = jsonArrayTrending.getJSONObject(i);

                            String id = objJson.getString("id");
                            String title = objJson.getString("live_title");
                            String image = objJson.getString("image").replace(" ", "%20");
                            if (image.equals("")) {
                                image = "null";
                            }
                            boolean isPremium = objJson.getBoolean("is_premium");

                            ItemData objItem = new ItemData(id,title,image,isPremium);
                            arrayListTrending.add(objItem);
                        }
                        itemPost.setArrayListLive(arrayListTrending);
                        arrayListPost.add(itemPost);
                    }
                }

                if (jsonObject.has("home_sections")) {

                    JSONArray jsonArraySection = jsonObject.getJSONArray("home_sections");

                    for (int j = 0; j < jsonArraySection.length(); j++) {

                        JSONObject jObjHome = jsonArraySection.getJSONObject(j);

                        postId = jObjHome.getString("home_id");
                        postTitle = jObjHome.getString("home_title");
                        postType = jObjHome.getString("home_type");
                        itemPost = new ItemPost(postId, postTitle, postType , "sections");

                        JSONArray jsonArrayHomeContent = jObjHome.getJSONArray("home_content");

                            switch (postType) {
                                case "category":
                                    ArrayList<ItemCat> arrayListCat = new ArrayList<>();
                                    for (int i = 0; i < jsonArrayHomeContent.length(); i++) {
                                        JSONObject objJson = jsonArrayHomeContent.getJSONObject(i);

                                        String id = objJson.getString("post_id");
                                        String name = objJson.getString("post_title");
                                        String image = objJson.getString("post_image").replace(" ", "%20");
                                        if (image.equals("")) {
                                            image = "null";
                                        }
                                        ItemCat itemCat = new ItemCat(id, name, image);
                                        arrayListCat.add(itemCat);
                                    }
                                    itemPost.setArrayListCategories(arrayListCat);
                                    break;

                                case "event":
                                    ArrayList<ItemEvent> arrayListEvent = new ArrayList<>();
                                    for (int i = 0; i < jsonArrayHomeContent.length(); i++) {
                                        JSONObject objJson = jsonArrayHomeContent.getJSONObject(i);

                                        String id = objJson.getString("id");
                                        String post_id = objJson.getString("post_id");
                                        String title = objJson.getString("event_title");
                                        String time = objJson.getString("event_time");
                                        String date = objJson.getString("event_date");

                                        String title_one = objJson.getString("team_title_one");
                                        String thumb_one = objJson.getString("team_one_thumbnail").replace(" ", "%20");
                                        if (thumb_one.equals("")) {
                                            thumb_one = "null";
                                        }
                                        String title_two = objJson.getString("team_title_two");
                                        String thumb_two = objJson.getString("team_two_thumbnail").replace(" ", "%20");
                                        if (thumb_two.equals("")) {
                                            thumb_two = "null";
                                        }

                                        ItemEvent itemCountries = new ItemEvent(id, post_id, title, time, date, title_one,thumb_one, title_two,thumb_two);
                                        arrayListEvent.add(itemCountries);
                                    }
                                    itemPost.setArrayListEvent(arrayListEvent);
                                    break;

                                case "live":
                                    ArrayList<ItemData> arrayListLive = new ArrayList<>();
                                    for (int i = 0; i < jsonArrayHomeContent.length(); i++) {
                                        JSONObject objJson = jsonArrayHomeContent.getJSONObject(i);

                                        String id = objJson.getString("id");
                                        String title = objJson.getString("live_title");
                                        String image = objJson.getString("image").replace(" ", "%20");
                                        if (image.equals("")) {
                                            image = "null";
                                        }
                                        boolean isPremium = objJson.getBoolean("is_premium");

                                        ItemData objItem = new ItemData(id,title,image,isPremium);
                                        arrayListLive.add(objItem);
                                    }
                                    itemPost.setArrayListLive(arrayListLive);
                                    break;
                            }
                            arrayListPost.add(itemPost);
                        }
                    }
            } catch (Exception e) {
                JSONArray jsonArray = mainJson.getJSONArray(Callback.TAG_ROOT);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                successAPI = jsonObject.getString(Callback.TAG_SUCCESS);
                message = jsonObject.getString(Callback.TAG_MSG);

                e.printStackTrace();
            }
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        homeListener.onEnd(s, message, arrayListPost);
        super.onPostExecute(s);
    }

}