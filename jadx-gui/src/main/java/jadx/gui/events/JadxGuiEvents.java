package jadx.gui.events;

import dexforge.api.plugins.events.JadxEventType;

import jadx.gui.events.types.TreeUpdate;

import static dexforge.api.plugins.events.JadxEventType.create;

public class JadxGuiEvents {

	public static final JadxEventType<TreeUpdate> TREE_UPDATE = create("TREE_UPDATE");
}
