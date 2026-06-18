package com.dexforge.layoutviewer.parser;

import org.junit.jupiter.api.Test;

import com.dexforge.layoutviewer.model.RenderStyle;

import static org.assertj.core.api.Assertions.assertThat;

class DrawableXmlParserTest {
	@Test
	void parseShapeDrawableStyle() {
		String xml = "<shape xmlns:android=\"http://schemas.android.com/apk/res/android\">"
				+ "<solid android:color=\"@color/card_bg\" />"
				+ "<stroke android:width=\"2dp\" android:color=\"#33000000\" />"
				+ "<corners android:radius=\"12dp\" />"
				+ "<padding android:left=\"8dp\" android:top=\"6dp\" android:right=\"8dp\" android:bottom=\"6dp\" />"
				+ "</shape>";

		RenderStyle style = new DrawableXmlParser().parse(xml);

		assertThat(style).isNotNull();
		assertThat(style.getBackgroundColor()).isEqualTo("@color/card_bg");
		assertThat(style.getStrokeColor()).isEqualTo("#33000000");
		assertThat(style.getStrokeWidth()).isEqualTo("2dp");
		assertThat(style.getCornerRadius()).isEqualTo("12dp");
		assertThat(style.getPaddingLeft()).isEqualTo("8dp");
	}
}
