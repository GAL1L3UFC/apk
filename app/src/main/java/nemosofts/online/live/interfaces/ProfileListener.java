package nemosofts.online.live.interfaces;

public interface ProfileListener {
    void onStart();
    void onEnd(String success, String isApiSuccess, String message,
               String user_id, String user_name, String email, String mobile, String gender , String profile);
}