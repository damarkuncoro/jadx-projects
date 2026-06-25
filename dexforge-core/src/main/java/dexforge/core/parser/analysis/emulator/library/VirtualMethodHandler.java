package dexforge.core.parser.analysis.emulator.library;

import java.util.List;

/**
 * REUSEABLE: Interface for simulating system library methods in the emulator.
 */
public interface VirtualMethodHandler {
    /**
     * Determines if this handler can simulate the given method.
     */
    boolean canHandle(String methodSignature);

    /**
     * Simulates the execution of the method.
     * @param signature The full method signature.
     * @param arguments The arguments passed from registers.
     * @return The result of the simulated execution.
     */
    Object execute(String signature, List<Object> arguments) throws Exception;
}
