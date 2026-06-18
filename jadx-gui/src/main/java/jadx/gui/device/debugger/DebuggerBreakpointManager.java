package jadx.gui.device.debugger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.gui.ui.panel.JDebuggerPanel;

public class DebuggerBreakpointManager {
	private static final Logger LOG = LoggerFactory.getLogger(DebuggerBreakpointManager.class);

	private final SmaliDebugger debugger;
	private final JDebuggerPanel debuggerPanel;
	private final BreakpointStore bpStore;
	private final ExecutorService updateQueue = Executors.newSingleThreadExecutor();

	public DebuggerBreakpointManager(SmaliDebugger debugger, JDebuggerPanel debuggerPanel) {
		this.debugger = debugger;
		this.debuggerPanel = debuggerPanel;
		this.bpStore = new BreakpointStore(debugger);
	}

	public void reset() {
		bpStore.reset();
	}

	public void initBreakpoints(java.util.List<BreakpointManager.FileBreakpoint> fbps) {
		if (fbps.isEmpty()) {
			return;
		}
		boolean fetch = true;
		for (BreakpointManager.FileBreakpoint fbp : fbps) {
			try {
				long id = debugger.getClassID(fbp.cls, fetch);
				fetch = false;
				if (id > -1) {
					setBreakpoint(id, fbp);
				} else {
					setDelayBreakpoint(fbp);
				}
			} catch (SmaliDebuggerException e) {
				logErr(e);
				failBreakpoint(fbp, e.getMessage());
			}
		}
	}

	public boolean setBreakpoint(BreakpointManager.FileBreakpoint bp) {
		try {
			long cid = debugger.getClassID(bp.cls, true);
			if (cid > -1) {
				setBreakpoint(cid, bp);
			} else {
				setDelayBreakpoint(bp);
			}
		} catch (SmaliDebuggerException e) {
			logErr(e);
			BreakpointManager.failBreakpoint(bp);
			return false;
		}
		return true;
	}

	private void setDelayBreakpoint(BreakpointManager.FileBreakpoint fbp) {
		boolean hasSet = bpStore.hasSetDelayed(fbp.cls);
		bpStore.add(fbp, null);
		if (!hasSet) {
			updateQueue.execute(() -> {
				try {
					debugger.regClassPrepareEventForBreakpoint(fbp.cls, id -> {
						java.util.List<BreakpointManager.FileBreakpoint> list = bpStore.get(fbp.cls);
						for (BreakpointManager.FileBreakpoint bp : list) {
							setBreakpoint(id, bp);
						}
					});
				} catch (SmaliDebuggerException e) {
					logErr(e);
					failBreakpoint(fbp, "");
				}
			});
		}
	}

	protected void setBreakpoint(long cid, BreakpointManager.FileBreakpoint fbp) {
		try {
			long mid = debugger.getMethodID(cid, fbp.mth);
			if (mid > -1) {
				SmaliDebugger.RuntimeBreakpoint rbp = debugger.makeBreakpoint(cid, mid, fbp.codeOffset);
				debugger.setBreakpoint(rbp);
				bpStore.add(fbp, rbp);
				return;
			}
		} catch (SmaliDebuggerException e) {
			logErr(e);
		}
		failBreakpoint(fbp, "Failed to get method for breakpoint, " + fbp.mth + ":" + fbp.codeOffset);
	}

	private void failBreakpoint(BreakpointManager.FileBreakpoint fbp, String msg) {
		if (!msg.isEmpty()) {
			debuggerPanel.log(msg);
		}
		bpStore.removeBreakpoint(fbp);
		BreakpointManager.failBreakpoint(fbp);
	}

	public boolean removeBreakpoint(BreakpointManager.FileBreakpoint fbp) {
		SmaliDebugger.RuntimeBreakpoint rbp = bpStore.removeBreakpoint(fbp);
		if (rbp != null) {
			try {
				debugger.removeBreakpoint(rbp);
			} catch (SmaliDebuggerException e) {
				logErr(e);
				return false;
			}
		}
		return true;
	}

	private void logErr(Exception e) {
		debuggerPanel.log(e.getMessage());
		LOG.error("Debug error", e);
	}
}
