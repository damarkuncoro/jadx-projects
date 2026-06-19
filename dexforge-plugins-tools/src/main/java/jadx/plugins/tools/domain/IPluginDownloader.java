package jadx.plugins.tools.domain;

import java.nio.file.Path;

public interface IPluginDownloader {
	boolean needDownload(String url);

	void download(String url, Path destination) throws Exception;
}
