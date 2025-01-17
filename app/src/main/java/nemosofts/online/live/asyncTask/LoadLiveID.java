package nemosofts.online.live.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.interfaces.LiveIDListener;
import nemosofts.online.live.item.ItemData;
import nemosofts.online.live.item.ItemLiveTv;
import nemosofts.online.live.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadLiveID extends AsyncTask<String, String, String> {

    private final RequestBody requestBody;
    private final LiveIDListener listener;
    private final ArrayList<ItemLiveTv> arrayListLive = new ArrayList<>();
    private final ArrayList<ItemData> arrayListRelated = new ArrayList<>();

    public LoadLiveID(LiveIDListener listener, RequestBody requestBody) {
        this.listener = listener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
            JSONObject mainJson = new JSONObject(json);
            JSONObject jsonObject = mainJson.getJSONObject(Callback.TAG_ROOT);

            JSONArray jsonArrayMovie = jsonObject.getJSONArray("live_data");
            for (int i = 0; i < jsonArrayMovie.length(); i++) {
                JSONObject objJson = jsonArrayMovie.getJSONObject(i);

                String id = objJson.getString("id");
                String cat_id = objJson.getString("cat_id");
                String title = objJson.getString("live_title");
                String live_url = objJson.getString("live_url");
                String image = objJson.getString("image").replace(" ", "%20");
                if (image.equals("")) {
                    image = "null";
                }
                String live_type = objJson.getString("live_type");
                String live_description = objJson.getString("live_description");
                String rate_avg = objJson.getString("rate_avg");
                String total_rate = objJson.getString("total_rate");
                String total_views = objJson.getString("total_views");
                String total_share = objJson.getString("total_share");
                boolean isPremium = objJson.getBoolean("is_premium");
                boolean isFav = objJson.getBoolean("is_favorite");

                boolean isUserAgent = objJson.getBoolean("user_agent");
                String userAgentName = objJson.getString("user_agent_name");
                String player_type = objJson.getString("player_type");

                ItemLiveTv item = new ItemLiveTv(id, cat_id, title, live_url, image,
                        live_type, live_description, rate_avg, total_rate, total_views, total_share,
                        isPremium, isFav, isUserAgent, userAgentName,player_type);

                arrayListLive.add(item);
            }

            JSONArray jsonArray = jsonObject.getJSONArray("related");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objJson = jsonArray.getJSONObject(i);

                String id = objJson.getString("id");
                String title = objJson.getString("live_title");
                String image = objJson.getString("image").replace(" ", "%20");
                if (image.equals("")) {
                    image = "null";
                }
                boolean isPremium = objJson.getBoolean("is_premium");

                ItemData objItem = new ItemData(id,title,image,isPremium);
                arrayListRelated.add(objItem);
            }

            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onEnd(s, arrayListLive, arrayListRelated);
        super.onPostExecute(s);
    }
}