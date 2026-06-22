package dexforge.engine;

@FunctionalInterface
public interface DexForgeProjectSessionFactory {
	DexForgeProjectSession open(DexForgeOpenProjectRequest request);
}
