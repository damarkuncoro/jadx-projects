package dexforge.core.infrastructure.jadx.cache.code;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jadx.api.ICodeCache;
import jadx.api.ICodeInfo;
import jadx.api.impl.DelegateCodeCache;

/**
 * Keep code strings for faster search
 */
public class CodeStringCache extends DelegateCodeCache {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CodeStringCache.class);

	private final Map<String, String> codeCache = new ConcurrentHashMap<>();
	private long lastCheckTime = 0;
	private static final long CHECK_INTERVAL_MS = 3000;

	public CodeStringCache(ICodeCache backCache) {
		super(backCache);
	}

	private void checkMemoryWithDebounce() {
		long now = System.currentTimeMillis();
		if (now - lastCheckTime > CHECK_INTERVAL_MS) {
			lastCheckTime = now;
			Runtime runtime = Runtime.getRuntime();
			long maxMemory = runtime.maxMemory();
			long totalFree = runtime.freeMemory() + (maxMemory - runtime.totalMemory());
			long minFree = Math.min((long) (maxMemory * 0.2), 512 * 1024L * 1024L);
			if (totalFree <= minFree) {
				LOG.warn("Free memory is low! Reset code strings cache. Cache size {}", codeCache.size());
				codeCache.clear();
				System.gc();
			}
		}
	}

	@Override
	@Nullable
	public String getCode(String clsFullName) {
		checkMemoryWithDebounce();
		String code = codeCache.get(clsFullName);
		if (code != null) {
			return code;
		}
		String backCode = backCache.getCode(clsFullName);
		if (backCode != null) {
			codeCache.put(clsFullName, backCode);
		}
		return backCode;
	}

	@Override
	public @NotNull ICodeInfo get(String clsFullName) {
		checkMemoryWithDebounce();
		return super.get(clsFullName);
	}

	@Override
	public void add(String clsFullName, ICodeInfo codeInfo) {
		checkMemoryWithDebounce();
		codeCache.put(clsFullName, codeInfo.getCodeStr());
		backCache.add(clsFullName, codeInfo);
	}

	@Override
	public void remove(String clsFullName) {
		codeCache.remove(clsFullName);
		backCache.remove(clsFullName);
	}

	@Override
	public void close() throws IOException {
		try {
			backCache.close();
		} finally {
			codeCache.clear();
		}
	}
}
