package dexforge.core.parser.analysis.patterns;

import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.sections.DexInstructionDecoder;
import dexforge.core.parser.dex.service.DexFastIndexer;
import java.util.ArrayList;
import java.util.List;

public final class ByteArrayDecoder {
    private final DexFastIndexer indexer;

    public static final class DecodedArray {
        private final String methodSignature;
        private final int offset;
        private final int size;
        private final byte[] data;
        private final String ascii;

        public DecodedArray(String methodSignature, int offset, int size, byte[] data, String ascii) {
            this.methodSignature = methodSignature;
            this.offset = offset;
            this.size = size;
            this.data = data;
            this.ascii = ascii;
        }

        public String getMethodSignature() { return methodSignature; }
        public int getOffset() { return offset; }
        public int getSize() { return size; }
        public byte[] getData() { return data; }
        public String getAscii() { return ascii; }
    }

    public ByteArrayDecoder(DexFastIndexer indexer) {
        this.indexer = indexer;
    }

    public List<DecodedArray> decodeStaticArrays() {
        List<DecodedArray> results = new ArrayList<>();

        for (DexClass clazz : indexer.getClasses()) {
            indexer.fillClassData(clazz);
            if (clazz.getClassData() == null) continue;

            scanMethodList(clazz.getClassData().directMethods, results);
            scanMethodList(clazz.getClassData().virtualMethods, results);
        }

        return results;
    }

    private void scanMethodList(List<DexEncodedMethod> methods, List<DecodedArray> results) {
        for (DexEncodedMethod m : methods) {
            if (m.getCodeOff() == 0) continue;

            var code = indexer.getCodeParser().parse(m.getCodeOff());
            if (code == null) continue;

            short[] raw = code.getInstructions();
            if (raw == null || raw.length == 0) continue;

            List<DexInstruction> insns = DexInstructionDecoder.decode(code);
            String signature = indexer.getMethodPool().getMethodSignature(m.getMethodIndex());

            for (DexInstruction insn : insns) {
                int op = insn.getOpcode() & 0xFF;
                if (op == 0x26) { // fill-array-data
                    int payloadOffset = insn.getOffset() + (int) insn.getLiteral();
                    if (payloadOffset < 0 || payloadOffset + 4 > raw.length) continue;

                    int magic = raw[payloadOffset] & 0xFFFF;
                    if (magic != 0x0300) continue; // payload magic

                    int elementWidth = raw[payloadOffset + 1] & 0xFFFF;
                    long size = (raw[payloadOffset + 2] & 0xFFFFL) | ((raw[payloadOffset + 3] & 0xFFFFL) << 16);

                    // We are looking for potential crypto key elements (element_width == 1, length >= 8)
                    if (elementWidth == 1 && size >= 8 && size <= 256) {
                        byte[] data = new byte[(int) size];
                        boolean isPrintable = true;
                        StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < size; i++) {
                            int wordIdx = payloadOffset + 4 + (i / 2);
                            if (wordIdx >= raw.length) {
                                isPrintable = false;
                                break;
                            }
                            int byteShift = (i % 2) * 8;
                            byte b = (byte) ((raw[wordIdx] >> byteShift) & 0xFF);
                            data[i] = b;

                            if (b >= 0x20 && b <= 0x7E) {
                                sb.append((char) b);
                            } else {
                                isPrintable = false;
                            }
                        }

                        if (isPrintable) {
                            results.add(new DecodedArray(signature, payloadOffset, (int) size, data, sb.toString()));
                        }
                    }
                }
            }
        }
    }
}
