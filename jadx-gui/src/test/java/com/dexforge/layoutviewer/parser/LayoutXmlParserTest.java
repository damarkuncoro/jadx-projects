package com.dexforge.layoutviewer.parser;

import org.junit.jupiter.api.Test;

import com.dexforge.layoutviewer.model.AndroidViewNode;

import static org.assertj.core.api.Assertions.assertThat;

class LayoutXmlParserTest {
	@Test
	void parseAndroidLayoutTree() throws Exception {
		String xml = "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\""
				+ " android:orientation=\"vertical\" android:layout_width=\"match_parent\""
				+ " android:layout_height=\"match_parent\">"
				+ "<TextView android:id=\"@+id/title\" android:text=\"@string/login_title\" />"
				+ "<Button android:text=\"Sign in\" />"
				+ "</LinearLayout>";

		AndroidViewNode root = new LayoutXmlParser().parse(xml);

		assertThat(root.getTag()).isEqualTo("LinearLayout");
		assertThat(root.getAttributes()).containsEntry("android:orientation", "vertical");
		assertThat(root.getChildren()).hasSize(2);
		assertThat(root.getChildren().get(0).getTag()).isEqualTo("TextView");
		assertThat(root.getChildren().get(0).getAttributes()).containsEntry("android:text", "@string/login_title");
	}
}
