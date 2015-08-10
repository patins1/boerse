/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory1 implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f, editorArea);
		topLeft.addView("com.kiegeland.boerse.views.ControlView");
		IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.50f, "topLeft");
		bottomLeft.addView("com.kiegeland.boerse.views.OneStockGainsView");
		// IFolderLayout bottom = layout.createFolder( "bottom",
		// IPageLayout.BOTTOM,
		// 0.60f,
		// editorArea );
		IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, 0.70f, editorArea);

		topRight.addView("com.kiegeland.boerse.views.AllStockGainsView");
		// topRight.addView(WebView.ID);

	}

}
