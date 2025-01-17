package nemosofts.online.live.utils.advertising;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAds;

import nemosofts.online.live.callback.Callback;

public class AdManagerInterUnity {

    public static boolean isAdLoaded = false;

    public AdManagerInterUnity() {
    }

    public void createAd() {
        UnityAds.load(Callback.unityInterstitialAdID, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                isAdLoaded = true;
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                isAdLoaded = false;
            }
        });
    }

    public boolean getAd() {
        return isAdLoaded;
    }

    public static void setAd() {
        isAdLoaded = false;
    }
}