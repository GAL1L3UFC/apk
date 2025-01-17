package nemosofts.online.live.item;


public class ItemEvent {

    private final String id;
    private final String post_id;
    private final String title;
    private final String time;
    private final String date;

    private final String title_one;
    private final String thumb_one;

    private final String title_two;
    private final String thumb_two;

    public ItemEvent(String id, String post_id, String title, String time, String date, String title_one, String thumb_one, String title_two, String thumb_two) {
        this.id = id;
        this.post_id = post_id;
        this.title = title;
        this.time = time;
        this.date = date;
        this.title_one = title_one;
        this.thumb_one = thumb_one;
        this.title_two = title_two;
        this.thumb_two = thumb_two;
    }

    public String getId() {
        return id;
    }

    public String getPostId() {
        return post_id;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getTitleOne() {
        return title_one;
    }

    public String getThumbOne() {
        return thumb_one;
    }

    public String getTitleTwo() {
        return title_two;
    }

    public String getThumbTwo() {
        return thumb_two;
    }
}