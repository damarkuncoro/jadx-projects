package dexforge.api.plugins.events.types;

import dexforge.api.plugins.events.IJadxEvent;
import dexforge.api.plugins.events.JadxEventType;
import dexforge.api.plugins.events.JadxEvents;

public class ReloadProject implements IJadxEvent {

	public static final ReloadProject EVENT = new ReloadProject();

	private ReloadProject() {
		// singleton
	}

	@Override
	public JadxEventType<ReloadProject> getType() {
		return JadxEvents.RELOAD_PROJECT;
	}

	@Override
	public String toString() {
		return "RELOAD_PROJECT";
	}
}
