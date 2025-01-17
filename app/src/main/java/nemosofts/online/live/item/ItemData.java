package nemosofts.online.live.item;


public class ItemData {

    private final String id;
    private final String title;
    private final String thumb;
    private final boolean is_premium;

    public ItemData(String id, String title, String thumb, boolean is_premium) {
        this.id = id;
        this.title = title;
        this.thumb = thumb;
        this.is_premium = is_premium;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getThumb() {
        return thumb;
    }

    public boolean getIsPremium() {
        return is_premium;
    }
}