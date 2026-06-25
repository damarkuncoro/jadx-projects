package dexforge.core.parser.analysis.emulator.library;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * REUSEABLE: Robust handler for java/javax crypto operations.
 * Supports AES, DES, and various padding schemes used in Android apps.
 */
public final class CryptoMethodHandler implements VirtualMethodHandler {
    private static final Set<String> SUPPORTED_CLASSES = new HashSet<>();

    static {
        SUPPORTED_CLASSES.add("Ljavax/crypto/Cipher;");
        SUPPORTED_CLASSES.add("Ljavax/crypto/spec/SecretKeySpec;");
        SUPPORTED_CLASSES.add("Ljavax/crypto/spec/IvParameterSpec;");
    }

    @Override
    public boolean canHandle(String methodSignature) {
        return SUPPORTED_CLASSES.stream().anyMatch(methodSignature::startsWith);
    }

    @Override
    public Object execute(String signature, List<Object> args) throws Exception {
        // Cipher.getInstance(String)
        if (signature.contains("Cipher;->getInstance")) {
            return Cipher.getInstance((String) args.get(0));
        }

        // SecretKeySpec(byte[], String)
        if (signature.contains("SecretKeySpec;-><init>")) {
            String algorithm = (String) args.get(2);
            if (algorithm != null && algorithm.contains("/")) {
                algorithm = algorithm.split("/")[0];
            }
            return new SecretKeySpec((byte[]) args.get(1), algorithm);
        }

        // IvParameterSpec(byte[])
        if (signature.contains("IvParameterSpec;-><init>")) {
            return new IvParameterSpec((byte[]) args.get(1));
        }

        // Cipher.init(int, Key) or Cipher.init(int, Key, AlgorithmParameterSpec)
        if (signature.contains("Cipher;->init")) {
            Cipher cipher = (Cipher) args.get(0);
            int mode = (Integer) args.get(1);
            Key key = (Key) args.get(2);
            if (args.size() > 3) {
                cipher.init(mode, key, (AlgorithmParameterSpec) args.get(3));
            } else {
                cipher.init(mode, key);
            }
            return null;
        }

        // Cipher.doFinal(byte[])
        if (signature.contains("Cipher;->doFinal")) {
            Cipher cipher = (Cipher) args.get(0);
            return cipher.doFinal((byte[]) args.get(1));
        }

        return null;
    }
}
