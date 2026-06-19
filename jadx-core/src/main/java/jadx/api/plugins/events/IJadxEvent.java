package dexforge.api.plugins.events;

public interface IJadxEvent {

	JadxEventType<? extends IJadxEvent> getType();
}
