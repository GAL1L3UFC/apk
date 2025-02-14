package nemosofts.online.live.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.interfaces.HomeListener;
import nemosofts.online.live.item.ItemCat;
import nemosofts.online.live.item.ItemData;
import nemosofts.online.live.item.ItemEvent;
import nemosofts.online.live.item.ItemPost;
import nemosofts.online.live.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadSearch extends AsyncTask<String, String, String> {

    RequestBody requestBody;
    HomeListener homeListener;
    ArrayList<ItemPost> arrayListPost = new ArrayList<>();
    String message = "";

    public LoadSearch(HomeListener homeListener, RequestBody requestBody) {
        this.homeListener = homeListener;
        this.requestBody = requestBody;
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
            JSONObject jsonObjectRoot = mainJson.getJSONObject(Callback.TAG_ROOT);

            ItemPost itemPost;
            String postTitle, postType, postId;

            if (jsonObjectRoot.has("live_list")) {
                ArrayList<ItemData> arrayListLive = new ArrayList<>();

                JSONArray jsonArray_countries = jsonObjectRoot.getJSONArray("live_list");

                if (jsonArray_countries.length() > 0) {
                    postId = "3";
                    postTitle = "Live";
                    postType = "live";
                    itemPost = new ItemPost(postId, postTitle, postType, "live");

                    for (int i = 0; i < jsonArray_countries.length(); i++) {
                        JSONObject objJson = jsonArray_countries.getJSONObject(i);

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
                    arrayListPost.add(itemPost);
                }
            }

            if (jsonObjectRoot.has("category_list")) {
                ArrayList<ItemCat> arrayListCat = new ArrayList<>();

                JSONArray jsonArray_albums = jsonObjectRoot.getJSONArray("category_list");

                if (jsonArray_albums.length() > 0) {
                    postId = "2";
                    postTitle = "Categories";
                    postType = "category";
                    itemPost = new ItemPost(postId, postTitle, postType, "category");

                    for (int i = 0; i < jsonArray_albums.length(); i++) {
                        JSONObject objJson = jsonArray_albums.getJSONObject(i);

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
                    arrayListPost.add(itemPost);
                }
            }

            if (jsonObjectRoot.has("event_list")) {
                ArrayList<ItemEvent> arrayListEvent = new ArrayList<>();

                JSONArray jsonArray_albums = jsonObjectRoot.getJSONArray("event_list");

                if (jsonArray_albums.length() > 0) {
                    postId = "1";
                    postTitle = "Event";
                    postType = "event";
                    itemPost = new ItemPost(postId, postTitle, postType, "event");

                    for (int i = 0; i < jsonArray_albums.length(); i++) {
                        JSONObject objJson = jsonArray_albums.getJSONObject(i);

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
                    arrayListPost.add(itemPost);
                }
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