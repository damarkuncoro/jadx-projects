package dexforge.plugins.input.xapk;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import jadx.api.ResourceFile;
import jadx.api.ResourceType;
import jadx.api.ResourcesLoader;
import dexforge.api.plugins.CustomResourcesLoader;
import dexforge.api.plugins.JadxPluginContext;
import jadx.core.utils.files.FileUtils;
import dexforge.plugins.input.xapk.data.XApkData;

public class XApkCustomResourcesLoader implements CustomResourcesLoader {
	private final JadxPluginContext context;
	private final XApkLoader loader;

	public XApkCustomResourcesLoader(JadxPluginContext context, XApkLoader loader) {
		this.context = context;
		this.loader = loader;
	}

	@Override
	public boolean load(ResourcesLoader resLoader, List<ResourceFile> list, File file) {
		XApkData xApkData = loader.checkAndLoad(file.toPath());
		if (xApkData == null) {
			return false;
		}
		for (Path apkPath : xApkData.getApks()) {
			resLoader.defaultLoadFile(list, apkPath.toFile(), apkPath.getFileName() + "/");
		}
		for (Path filePath : xApkData.getFiles()) {
			File innerFile = filePath.toFile();
			String relativePath = xApkData.getTmpDir().relativize(filePath).toString();
			if (FileUtils.isZipFile(innerFile)) {
				// zip will be unpacked by default loader
				resLoader.defaultLoadFile(list, innerFile, relativePath + "/");
			} else {
				// create resource to file in tmp folder, but with name relative to xapk root
				ResourceType type = ResourceType.getFileType(relativePath);
				ResourceFile resFile = ResourceFile.createResourceFile(context.getDecompiler(), innerFile, type);
				resFile.setDeobfName(relativePath);
				list.add(resFile);
			}
		}
		return true;
	}

	@Override
	public void close() throws IOException {
	}
}
