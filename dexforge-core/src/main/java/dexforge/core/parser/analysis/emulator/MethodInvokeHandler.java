package dexforge.core.parser.analysis.emulator;

import dexforge.core.parser.analysis.emulator.library.CryptoMethodHandler;
import dexforge.core.parser.analysis.emulator.library.JdkReflectionMethodHandler;
import dexforge.core.parser.analysis.emulator.library.VirtualMethodHandler;
import dexforge.core.parser.dex.model.DexInstruction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages method invocation logic and virtual library handlers.
 */
public final class MethodInvokeHandler {
    private final List<VirtualMethodHandler> libraryHandlers = new ArrayList<>();
    private final Map<Integer, Object> registers;
    private SmaliEmulator emulator;

    public MethodInvokeHandler(Map<Integer, Object> registers) {
        this.registers = registers;
        registerDefaultHandlers();
    }

    public void setEmulator(SmaliEmulator emulator) {
        this.emulator = emulator;
    }

    public void registerHandler(VirtualMethodHandler handler) {
        libraryHandlers.add(0, handler);
    }

    private void registerDefaultHandlers() {
        libraryHandlers.add(new CryptoMethodHandler());
        libraryHandlers.add(new JdkReflectionMethodHandler());
    }

    public Object handleInvoke(String signature, List<Object> args) {
        Object result = null;
        boolean handled = false;

        for (VirtualMethodHandler handler : libraryHandlers) {
            if (handler.canHandle(signature)) {
                try {
                    result = handler.execute(signature, args);
                    handled = true;
                } catch (Exception e) {
                    System.err.println("  Emulator Error: " + signature + " : " + e.getMessage());
                }
                break;
            }
        }

        // If not a library call, try to emulate internal method call
        if (!handled && emulator != null && !signature.startsWith("Landroid/") && !signature.startsWith("Ljava/")) {
            Map<Integer, Object> internalRegs = new HashMap<>();
            // Map args to p0, p1, etc (simplified mapping for now)
            for (int i = 0; i < args.size(); i++) {
                internalRegs.put(i, args.get(i));
            }
            result = emulator.executeMethod(signature, internalRegs);
            handled = true;
        }

        if (!handled) {
            System.out.println("  Emulator [MISSING]: " + signature + " args: " + args);
        }

        return result;
    }

    public List<Object> getInvokeArgs(DexInstruction insn) {
        List<Object> args = new ArrayList<>();
        short[] units = insn.getUnits();
        if (units == null || units.length < 3) return args;

        int regCount = (units[0] >> 12) & 0x0F;
        int g = (units[0] >> 8) & 0x0F;
        int c = units[2] & 0x0F;
        int d = (units[2] >> 4) & 0x0F;
        int e = (units[2] >> 8) & 0x0F;
        int f = (units[2] >> 12) & 0x0F;

        int[] regs = {c, d, e, f, g};
        for (int i = 0; i < Math.min(regCount, 5); i++) {
            args.add(registers.get(regs[i]));
        }
        return args;
    }
}
