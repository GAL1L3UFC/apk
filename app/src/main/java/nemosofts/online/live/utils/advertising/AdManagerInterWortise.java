package nemosofts.online.live.utils.advertising;

import android.content.Context;

import com.wortise.ads.interstitial.InterstitialAd;

import nemosofts.online.live.callback.Callback;

public class AdManagerInterWortise {

    static InterstitialAd interAd;
    private final Context ctx;

    public AdManagerInterWortise(Context ctx) {
        this.ctx = ctx;
    }

    public void createAd() {
        interAd = new InterstitialAd((ctx), Callback.wortiseInterstitialAdID);
        interAd.loadAd();
    }

    public InterstitialAd getAd() {
        return interAd;
    }

    public static void setAd(InterstitialAd interstitialAd) {
        interAd = interstitialAd;
    }
}