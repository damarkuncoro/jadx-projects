package dexforge.api.model;

/**
 * Represents decompiled code and its associated metadata.
 * This is the DexForge equivalent of JADX's ICodeInfo.
 */
public interface ICodeInfo {

	ICodeInfo EMPTY = new ICodeInfo() {
		@Override
		public String getCodeStr() {
			return "";
		}

		@Override
		public DexForgeCodeMetadata getCodeMetadata() {
			return DexForgeCodeMetadata.EMPTY;
		}

		@Override
		public boolean hasMetadata() {
			return false;
		}

		@Override
		public Object unwrap() {
			return null;
		}
	};

	String getCodeStr();

	DexForgeCodeMetadata getCodeMetadata();

	boolean hasMetadata();

	/**
	 * Internal use only: get the underlying decompiler-specific object.
	 */
	@Deprecated(forRemoval = false)
	Object unwrap();
}
