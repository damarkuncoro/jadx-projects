package dexforge.core.parser.kotlin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import dexforge.core.parser.dex.model.DexAnnotation;
import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.kotlin.model.KotlinFunctionInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class KotlinMetadataEnhancerTest {

    @Test
    void testExtractMetadata() {
        DexFastIndexer indexer = mock(DexFastIndexer.class);
        DexClass clazz = mock(DexClass.class);

        DexAnnotation annotation = mock(DexAnnotation.class);
        when(annotation.getType()).thenReturn("Lkotlin/Metadata;");

        Map<String, Object> elements = new HashMap<>();
        elements.put("d1", new String[]{"some-protobuf-data"});
        when(annotation.getElements()).thenReturn(elements);

        List<DexAnnotation> annotations = new ArrayList<>();
        annotations.add(annotation);
        when(indexer.getClassAnnotations(clazz)).thenReturn(annotations);

        KotlinMetadataEnhancer enhancer = new KotlinMetadataEnhancer(indexer);
        List<KotlinFunctionInfo> results = enhancer.extractMetadata(clazz);

        assertThat(results).hasSize(2);

        KotlinFunctionInfo f1 = results.get(0);
        assertThat(f1.getName()).isEqualTo("suspendFunc");
        assertThat(f1.isSuspend()).isTrue();
        assertThat(f1.isInline()).isFalse();

        KotlinFunctionInfo f2 = results.get(1);
        assertThat(f2.getName()).isEqualTo("inlineFunc");
        assertThat(f2.isSuspend()).isFalse();
        assertThat(f2.isInline()).isTrue();
    }
}
