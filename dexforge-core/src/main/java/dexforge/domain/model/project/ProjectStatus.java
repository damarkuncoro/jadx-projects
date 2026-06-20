package dexforge.domain.model.project;

/**
 * Value Object: Project Status
 * Enumerasi state dari project dalam lifecycle-nya.
 */
public enum ProjectStatus {
	/**
	 * Project baru saja di-create, belum di-open.
	 */
	CREATED,

	/**
	 * Project sudah di-open dan siap digunakan.
	 */
	OPENED,

	/**
	 * Project sedang di-decompile.
	 */
	DECOMPILING,

	/**
	 * Project sudah selesai di-decompile.
	 */
	DECOMPILED,

	/**
	 * Project sedang di-analyze.
	 */
	ANALYZING,

	/**
	 * Project closed/tidak aktif.
	 */
	CLOSED;

	public boolean isOpen() {
		return this == OPENED || this == DECOMPILING || this == DECOMPILED || this == ANALYZING;
	}

	public boolean isBusy() {
		return this == DECOMPILING || this == ANALYZING;
	}
}
