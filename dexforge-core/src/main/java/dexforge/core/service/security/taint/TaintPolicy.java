package dexforge.core.service.security.taint;

import dexforge.core.service.security.taint.model.TaintSink;
import dexforge.core.service.security.taint.model.TaintSource;
import java.util.HashMap;
import java.util.Map;

/**
 * REUSEABLE: Central registry for Taint Sources and Sinks.
 * Follows SOLID (Open/Closed) - easy to add new categories without changing analysis engine.
 */
public final class TaintPolicy {
    private final Map<String, TaintSource> sources = new HashMap<>();
    private final Map<String, TaintSink> sinks = new HashMap<>();

    public TaintPolicy() {
        initDefaultSources();
        initDefaultSinks();
    }

    private void initDefaultSources() {
        // Device Identity
        addSource("Landroid/telephony/TelephonyManager;->getDeviceId", "DEVICE_ID");
        addSource("Landroid/telephony/TelephonyManager;->getImei", "DEVICE_ID");
        addSource("Landroid/provider/Settings$Secure;->getString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;", "ANDROID_ID");

        // Personal Data
        addSource("Landroid/location/Location;->getLatitude", "LOCATION");
        addSource("Landroid/location/Location;->getLongitude", "LOCATION");
        addSource("Landroid/content/ContentResolver;->query(Landroid/net/Uri;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;", "DATABASE_CONTENT");

        // Communication
        addSource("Landroid/telephony/SmsMessage;->getMessageBody", "SMS_CONTENT");
    }

    private void initDefaultSinks() {
        // Network
        addSink("Ljava/net/URL;->openConnection", "NETWORK");
        addSink("Lokhttp3/OkHttpClient;->newCall", "NETWORK");

        // Storage & Logs
        addSink("Landroid/util/Log;->d", "LOG");
        addSink("Landroid/util/Log;->e", "LOG");
        addSink("Ljava/io/FileOutputStream;->write", "FILE_SYSTEM");
    }

    private void addSource(String sig, String label) {
        sources.put(sig, new TaintSource(sig, label));
    }

    private void addSink(String sig, String label) {
        sinks.put(sig, new TaintSink(sig, label));
    }

    public TaintSource getSource(String sig) { return sources.get(sig); }
    public TaintSink getSink(String sig) { return sinks.get(sig); }
}
