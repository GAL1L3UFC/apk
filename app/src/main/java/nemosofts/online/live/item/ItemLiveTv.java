package nemosofts.online.live.item;

public class ItemLiveTv {

    private final String id;
    private final String cat_id;
    private final String live_title;
    private final String live_url;
    private final String image;
    private final String live_type;
    private final String live_description;
    private String averageRating;
    private String totalRate;
    private final String total_views;
    private final String total_share;
    private final boolean isPremium;
    private boolean isFavourite;

    private boolean isUserAgent = false;
    private String userAgentName;
    private String player_type;


    private String userRating="";
    private String userMessage="" ;

    public ItemLiveTv(String id, String cat_id, String live_title, String live_url, String image,
                      String live_type, String live_description, String averageRating, String totalRate, String total_views,
                      String total_share, boolean isPremium, boolean isFavourite, boolean userAgent, String userAgentName, String player_type) {
        this.id = id;
        this.cat_id = cat_id;
        this.live_title = live_title;
        this.live_url = live_url;
        this.image = image;
        this.live_type = live_type;
        this.live_description = live_description;
        this.averageRating = averageRating;
        this.totalRate = totalRate;
        this.total_views = total_views;
        this.total_share = total_share;
        this.isPremium = isPremium;
        this.isFavourite = isFavourite;
        this.isUserAgent = userAgent;
        this.userAgentName = userAgentName;
        this.player_type = player_type;
    }

    public String getId() {
        return id;
    }

    public String getCatId() {
        return cat_id;
    }

    public String getLiveTitle() {
        return live_title;
    }

    public String getLiveURL() {
        return live_url;
    }

    public String getImage() {
        return image;
    }

    public String getLiveType() {
        return live_type;
    }

    public String getLiveDescription() {
        return live_description;
    }

    public String getTotalViews() {
        return total_views;
    }

    public String getTotalShare() {
        return total_share;
    }

    public boolean IsPremium() {
        return isPremium;
    }

    public Boolean IsFav() {
        return isFavourite;
    }
    public void setIsFav(Boolean favourite) {
        isFavourite = favourite;
    }

    public String getUserRating() {
        return userRating;
    }
    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }

    public String getUserMessage() {
        return userMessage;
    }
    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getAverageRating() {
        return averageRating;
    }

    public String getTotalRate() {
        return totalRate;
    }

    public void setAverageRating(String averageRating) {
        this.averageRating = averageRating;
    }

    public void setTotalRate(String totalRate) {
        this.totalRate = totalRate;
    }

    public boolean isUserAgent() {
        return isUserAgent;
    }

    public String getUserAgentName() {
        return userAgentName;
    }

    public String getPlayerType() {
        return player_type;
    }
}