package jadx.gui.device.debugger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jadx.gui.device.debugger.BreakpointManager.FileBreakpoint;

public class BreakpointStore {
	private static SmaliDebugger.RuntimeBreakpoint delayBP = null;

	private Map<FileBreakpoint, SmaliDebugger.RuntimeBreakpoint> bpm = Collections.emptyMap();
	private final SmaliDebugger debugger;

	public BreakpointStore(SmaliDebugger debugger) {
		this.debugger = debugger;
		if (delayBP == null) {
			delayBP = debugger.makeBreakpoint(-1, -1, -1);
		}
	}

	public void reset() {
		bpm.clear();
	}

	public boolean hasSetDelayed(String cls) {
		for (Map.Entry<FileBreakpoint, SmaliDebugger.RuntimeBreakpoint> entry : bpm.entrySet()) {
			if (entry.getValue() == delayBP && entry.getKey().cls.equals(cls)) {
				return true;
			}
		}
		return false;
	}

	public List<FileBreakpoint> get(String cls) {
		List<FileBreakpoint> fbps = new ArrayList<>();
		var iterator = bpm.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<FileBreakpoint, SmaliDebugger.RuntimeBreakpoint> entry = iterator.next();
			if (entry.getValue() == delayBP && entry.getKey().cls.equals(cls)) {
				fbps.add(entry.getKey());
				iterator.remove();
			}
		}
		return fbps;
	}

	public void add(FileBreakpoint fbp, SmaliDebugger.RuntimeBreakpoint rbp) {
		if (bpm == Collections.EMPTY_MAP) {
			bpm = new ConcurrentHashMap<>();
		}
		bpm.put(fbp, rbp == null ? delayBP : rbp);
	}

	public SmaliDebugger.RuntimeBreakpoint removeBreakpoint(FileBreakpoint fbp) {
		return bpm.remove(fbp);
	}
}
