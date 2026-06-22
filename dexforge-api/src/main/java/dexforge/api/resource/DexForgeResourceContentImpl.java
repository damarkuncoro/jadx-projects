package dexforge.api.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import dexforge.core.infrastructure.jadx.JadxResourceHelper;

/**
 * Internal implementation of DexForgeResourceContent wrapping a JADX ResContainer.
 */
final class DexForgeResourceContentImpl implements DexForgeResourceContent {
	private final Object delegate;
	private List<DexForgeResourceContent> cachedSubFiles;

	DexForgeResourceContentImpl(Object delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	public String getName() {
		return JadxResourceHelper.getResContainerName(delegate);
	}

	@Override
	public DexForgeResourceContentType getContentType() {
		String type = JadxResourceHelper.getResContainerDataType(delegate);
		switch (type) {
			case "TEXT":
			case "RES_TABLE":
				return DexForgeResourceContentType.TEXT;
			case "DECODED_DATA":
				return DexForgeResourceContentType.BINARY;
			default:
				return DexForgeResourceContentType.UNKNOWN;
		}
	}

	@Override
	public Optional<String> getText() {
		return Optional.ofNullable(JadxResourceHelper.getResContainerText(delegate));
	}

	@Override
	public Optional<byte[]> getData() {
		return Optional.ofNullable(JadxResourceHelper.getResContainerBinary(delegate));
	}

	@Override
	public List<DexForgeResourceContent> getSubContents() {
		if (cachedSubFiles == null) {
			List<?> subFiles = JadxResourceHelper.getResContainerSubFiles(delegate);
			if (subFiles.isEmpty()) {
				cachedSubFiles = Collections.emptyList();
			} else {
				List<DexForgeResourceContent> list = new ArrayList<>(subFiles.size());
				for (Object sub : subFiles) {
					list.add(new DexForgeResourceContentImpl(sub));
				}
				cachedSubFiles = Collections.unmodifiableList(list);
			}
		}
		return cachedSubFiles;
	}
}
