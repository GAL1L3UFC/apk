package nemosofts.online.live.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.online.live.callback.Callback;
import nemosofts.online.live.interfaces.EventListener;
import nemosofts.online.live.item.ItemEvent;
import nemosofts.online.live.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadEvent extends AsyncTask<String, String, String> {

    private final EventListener listener;
    private final ArrayList<ItemEvent> arrayList = new ArrayList<>();
    private final RequestBody requestBody;
    private String verifyStatus = "0", message = "";

    public LoadEvent(EventListener listener, RequestBody requestBody) {
        this.listener = listener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
        super.onPreExecute();
    }

    @Override
    protected  String doInBackground(String... strings)  {
        String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
        try {
            JSONObject jOb = new JSONObject(json);
            JSONArray jsonArray = jOb.getJSONArray(Callback.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objJson = jsonArray.getJSONObject(i);

                if (!objJson.has(Callback.TAG_SUCCESS)) {

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

                    ItemEvent objItem = new ItemEvent(id, post_id, title, time, date, title_one,thumb_one, title_two,thumb_two);
                    arrayList.add(objItem);

                } else {
                    verifyStatus = objJson.getString(Callback.TAG_SUCCESS);
                    message = objJson.getString(Callback.TAG_MSG);
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
        listener.onEnd(s, verifyStatus, message, arrayList);
        super.onPostExecute(s);
    }

}

