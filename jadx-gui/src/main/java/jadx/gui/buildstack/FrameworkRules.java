package jadx.gui.buildstack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jadx.gui.buildstack.rules.*;

/**
 * Kelas yang menyediakan semua aturan deteksi framework.
 */
public class FrameworkRules {

	private static final List<String> FRAMEWORK_ORDER = List.of(
			"Native Android",
			"Flutter",
			"React Native",
			"Unity",
			"Cordova",
			"Capacitor",
			"Xamarin",
			"Tauri",
			"Cocos2d",
			"Unreal Engine",
			"Kotlin runtime",
			"AndroidX / Jetpack",
			"Jetpack Compose",
			"Room",
			"Firebase",
			"Retrofit",
			"OkHttp",
			"Dagger / Hilt",
			"Koin",
			"RxJava",
			"Glide",
			"Lottie",
			"WebView / Hybrid",
			"R8 / ProGuard");

	private static final List<FrameworkRule> RULES = new ArrayList<>();

	static {
		RULES.add(new NativeAndroidRule());
		RULES.add(new FlutterRule());
		RULES.add(new ReactNativeRule());
		RULES.add(new UnityRule());
		RULES.add(new CordovaRule());
		RULES.add(new CapacitorRule());
		RULES.add(new XamarinRule());
		RULES.add(new TauriRule());
		RULES.add(new Cocos2dRule());
		RULES.add(new UnrealEngineRule());
		RULES.add(new KotlinRuntimeRule());
		RULES.add(new AndroidXJetpackRule());
		RULES.add(new JetpackComposeRule());
		RULES.add(new RoomRule());
		RULES.add(new FirebaseRule());
		RULES.add(new RetrofitRule());
		RULES.add(new OkHttpRule());
		RULES.add(new DaggerHiltRule());
		RULES.add(new KoinRule());
		RULES.add(new RxJavaRule());
		RULES.add(new GlideRule());
		RULES.add(new LottieRule());
		RULES.add(new WebViewHybridRule());
		RULES.add(new R8ProGuardRule());
	}

	private FrameworkRules() {
	}

	/**
	 * Mendeteksi semua framework dan mengembalikan hasilnya dalam urutan yang ditentukan.
	 */
	public static List<FrameworkDetection> detectAll(Set<String> resourceNames, Set<String> classNames,
			Map<String, String> libraryVersions) {
		RuleContext ctx = new RuleContext(resourceNames, classNames, libraryVersions);
		List<FrameworkDetection> frameworks = new ArrayList<>();
		for (FrameworkRule rule : RULES) {
			boolean detected = rule.detect(ctx);
			frameworks.add(new FrameworkDetection(
					rule.getName(),
					detected ? "DETECTED" : "NOT_DETECTED",
					detected ? rule.getConfidence() : "NONE",
					detected ? rule.getEvidence(ctx) : List.of()));
		}
		return frameworks.stream()
				.sorted(Comparator.comparingInt(framework -> FRAMEWORK_ORDER.indexOf(framework.getName())))
				.collect(Collectors.toList());
	}

	/**
	 * Mengecek apakah library penting.
	 */
	public static boolean isImportantLibrary(String name) {
		return name.startsWith("androidx.core")
				|| name.startsWith("androidx.appcompat")
				|| name.startsWith("androidx.databinding")
				|| name.startsWith("androidx.navigation")
				|| name.startsWith("androidx.room")
				|| name.startsWith("androidx.compose")
				|| name.startsWith("androidx.camera")
				|| name.startsWith("kotlinx_coroutines")
				|| name.startsWith("com.google.android.material")
				|| name.startsWith("com.google.firebase")
				|| name.startsWith("com.squareup.retrofit")
				|| name.startsWith("com.squareup.okhttp")
				|| name.startsWith("com.google.dagger")
				|| name.startsWith("org.insert-koin")
				|| name.startsWith("com.airbnb.android")
				|| name.startsWith("io.reactivex")
				|| name.startsWith("com.github.bumptech.glide");
	}
}
