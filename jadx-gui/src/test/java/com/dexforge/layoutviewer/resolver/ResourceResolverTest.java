package com.dexforge.layoutviewer.resolver;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.dexforge.layoutviewer.model.AndroidViewNode;
import com.dexforge.layoutviewer.model.RenderStyle;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceResolverTest {
	@Test
	void resolveReferencesAndStyleAttributes() {
		ResourceResolver resolver = new ResourceResolver();
		resolver.putValue("string", "login_title", "Login");
		resolver.putValue("color", "primary", "#045E9B");
		resolver.putStyle("BaseTitleStyle", Map.of("android:textSize", "20sp"));
		resolver.putStyle("TitleStyle", "BaseTitleStyle", Map.of(
				"android:textColor", "@color/primary"));

		AndroidViewNode node = new AndroidViewNode("TextView");
		node.putAttribute("style", "@style/TitleStyle");
		node.putAttribute("android:text", "@string/login_title");

		resolver.resolveTree(node);

		assertThat(node.getResolvedAttributes()).containsEntry("android:text", "Login");
		assertThat(node.getResolvedAttributes()).containsEntry("android:textColor", "#045E9B");
		assertThat(node.getResolvedAttributes()).containsEntry("android:textSize", "20sp");
	}

	@Test
	void attachDrawableRenderAttributes() {
		ResourceResolver resolver = new ResourceResolver();
		resolver.putValue("color", "card_bg", "#FFFFFF");
		RenderStyle style = new RenderStyle();
		style.setBackgroundColor("@color/card_bg");
		style.setStrokeColor("#22000000");
		style.setStrokeWidth("1dp");
		style.setCornerRadius("10dp");
		resolver.putDrawable("res/drawable/card_background.xml", style);

		AndroidViewNode node = new AndroidViewNode("LinearLayout");
		node.putAttribute("android:background", "@drawable/card_background");

		resolver.resolveTree(node);

		assertThat(node.getResolvedAttributes()).containsEntry(ResourceResolver.ATTR_DRAWABLE_BACKGROUND, "#FFFFFF");
		assertThat(node.getResolvedAttributes()).containsEntry(ResourceResolver.ATTR_DRAWABLE_STROKE_COLOR, "#22000000");
		assertThat(node.getResolvedAttributes()).containsEntry(ResourceResolver.ATTR_DRAWABLE_CORNER_RADIUS, "10dp");
	}

	@Test
	void resolveTextAppearanceFromStyle() {
		ResourceResolver resolver = new ResourceResolver();
		resolver.putStyle("TextAppearance.AppCompat.Widget.ActionBar.Title", Map.of(
				"android:textColor", "#212121",
				"android:textSize", "18sp"));
		resolver.putStyle("RtlOverlay.Widget.AppCompat.ActionBar.TitleItem", Map.of(
				"android:textAppearance", "@style/TextAppearance.AppCompat.Widget.ActionBar.Title"));

		AndroidViewNode node = new AndroidViewNode("TextView");
		node.putAttribute("style", "@style/RtlOverlay.Widget.AppCompat.ActionBar.TitleItem");

		resolver.resolveTree(node);

		assertThat(node.getResolvedAttributes()).containsEntry("android:textColor", "#212121");
		assertThat(node.getResolvedAttributes()).containsEntry("android:textSize", "18sp");
	}
}
