/*
  Ad & Tracker Blocker Script for TurboVPN
*/

Java.perform(function () {
    console.log("[*] Dynamic Ad & Tracker Blocker script loaded");

    // Bypass Google AdMob Initialization
    try {
        var MobileAds = Java.use("com.google.android.gms.ads.MobileAds");
        MobileAds.initialize.overloads.forEach(function (o) {
            o.implementation = function () {
                console.log("[+] Blocked Google AdMob initialization");
            };
        });
    } catch (e) {
        console.log("[-] AdMob hook failed: " + e.message);
    }

    // Bypass Unity Ads Initialization
    try {
        var UnityAds = Java.use("com.unity3d.ads.UnityAds");
        UnityAds.initialize.overloads.forEach(function (o) {
            o.implementation = function () {
                console.log("[+] Blocked Unity Ads initialization");
                var listener = arguments[arguments.length - 1];
                if (listener !== null && typeof listener === 'object' && listener.onInitializationComplete) {
                    listener.onInitializationComplete();
                }
            };
        });
    } catch (e) {
        console.log("[-] Unity Ads hook failed: " + e.message);
    }

    // Bypass IronSource SDK Initialization
    try {
        var IronSource = Java.use("com.ironsource.mediationsdk.IronSource");
        IronSource.init.overloads.forEach(function (o) {
            o.implementation = function () {
                console.log("[+] Blocked IronSource SDK initialization");
            };
        });
    } catch (e) {
        console.log("[-] IronSource SDK hook failed: " + e.message);
    }

    // Disable Firebase/Google Analytics Tracking
    try {
        var FirebaseAnalytics = Java.use("com.google.firebase.analytics.FirebaseAnalytics");
        FirebaseAnalytics.logEvent.overloads.forEach(function (o) {
            o.implementation = function (name, bundle) {
                console.log("[+] Blocked Firebase Analytics event: " + name);
            };
        });
    } catch (e) {
        console.log("[-] Firebase Analytics hook failed: " + e.message);
    }

    // Bypass Adjust SDK Initialization
    try {
        var Adjust = Java.use("com.adjust.sdk.Adjust");
        Adjust.onCreate.overloads.forEach(function (o) {
            o.implementation = function () {
                console.log("[+] Blocked Adjust SDK onCreate initialization");
            };
        });
    } catch (e) {
        console.log("[-] Adjust SDK hook failed: " + e.message);
    }

});
