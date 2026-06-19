package jadx.plugins.tools.infrastructure;

import java.nio.file.Path;

import jadx.plugins.tools.domain.IPluginDownloader;
import jadx.plugins.tools.utils.PluginUtils;

public class HttpPluginDownloader implements IPluginDownloader {
	@Override
	public boolean needDownload(String url) {
		return url.startsWith("https://") || url.startsWith("http://");
	}

	@Override
	public void download(String url, Path destination) throws Exception {
		PluginUtils.downloadFile(url, destination);
	}
}
