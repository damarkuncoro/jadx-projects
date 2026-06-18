package com.dexforge.layoutviewer.model;

public class RenderStyle {
	private String backgroundColor;
	private String strokeColor;
	private String strokeWidth;
	private String cornerRadius;
	private String paddingLeft;
	private String paddingTop;
	private String paddingRight;
	private String paddingBottom;

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getStrokeColor() {
		return strokeColor;
	}

	public void setStrokeColor(String strokeColor) {
		this.strokeColor = strokeColor;
	}

	public String getStrokeWidth() {
		return strokeWidth;
	}

	public void setStrokeWidth(String strokeWidth) {
		this.strokeWidth = strokeWidth;
	}

	public String getCornerRadius() {
		return cornerRadius;
	}

	public void setCornerRadius(String cornerRadius) {
		this.cornerRadius = cornerRadius;
	}

	public String getPaddingLeft() {
		return paddingLeft;
	}

	public void setPaddingLeft(String paddingLeft) {
		this.paddingLeft = paddingLeft;
	}

	public String getPaddingTop() {
		return paddingTop;
	}

	public void setPaddingTop(String paddingTop) {
		this.paddingTop = paddingTop;
	}

	public String getPaddingRight() {
		return paddingRight;
	}

	public void setPaddingRight(String paddingRight) {
		this.paddingRight = paddingRight;
	}

	public String getPaddingBottom() {
		return paddingBottom;
	}

	public void setPaddingBottom(String paddingBottom) {
		this.paddingBottom = paddingBottom;
	}

	public boolean hasAnyValue() {
		return backgroundColor != null
				|| strokeColor != null
				|| strokeWidth != null
				|| cornerRadius != null
				|| paddingLeft != null
				|| paddingTop != null
				|| paddingRight != null
				|| paddingBottom != null;
	}
}
