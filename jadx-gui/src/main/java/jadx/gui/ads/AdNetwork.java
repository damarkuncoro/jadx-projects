package jadx.gui.ads;

import java.util.Arrays;
import java.util.List;

public enum AdNetwork {
	// Google AdMob
	ADMOB("Google AdMob", Arrays.asList("com.google.ads", "com.google.android.gms.ads")),
	// Facebook Audience Network
	FACEBOOK("Facebook Audience Network", Arrays.asList("com.facebook.ads")),
	// Unity Ads
	UNITY("Unity Ads", Arrays.asList("com.unity3d.ads")),
	// AppLovin
	APPLOVIN("AppLovin", Arrays.asList("com.applovin")),
	// MoPub
	MOPUB("MoPub", Arrays.asList("com.mopub")),
	// StartApp
	STARTAPP("StartApp", Arrays.asList("com.startapp")),
	// IronSource
	IRONSOURCE("IronSource", Arrays.asList("com.ironsource")),
	// Vungle
	VUNGLE("Vungle", Arrays.asList("com.vungle")),
	// AdColony
	ADCOLONY("AdColony", Arrays.asList("com.adcolony")),
	// Chartboost
	CHARTBOOST("Chartboost", Arrays.asList("com.chartboost")),
	// InMobi
	INMOBI("InMobi", Arrays.asList("com.inmobi")),
	// Tapjoy
	TAPJOY("Tapjoy", Arrays.asList("com.tapjoy"));

	private final String name;
	private final List<String> packagePrefixes;

	AdNetwork(String name, List<String> packagePrefixes) {
		this.name = name;
		this.packagePrefixes = packagePrefixes;
	}

	public String getName() {
		return name;
	}

	public List<String> getPackagePrefixes() {
		return packagePrefixes;
	}

	public boolean matchesPackage(String packageName) {
		for (String prefix : packagePrefixes) {
			if (packageName.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}
}
