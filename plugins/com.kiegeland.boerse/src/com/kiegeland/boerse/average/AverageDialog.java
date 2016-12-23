/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.average;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class AverageDialog extends Dialog {

	private static AverageDialog current = null;

	private AverageConfig averageConfig;

	private AverageCanvas averageCanvas;

	public AverageDialog(AverageConfig averageConfig) {
		super(PlatformUI.getWorkbench().getDisplay().getActiveShell());
		this.averageConfig = averageConfig;
		this.setBlockOnOpen(false);
		current = this;
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE | SWT.MAX;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite result = (Composite) super.createDialogArea(parent);
		result.setLayout(new FillLayout());
		averageCanvas = new AverageCanvas(result, SWT.NO_BACKGROUND);
		averageCanvas.setChart(averageConfig);
		return result;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		// newShell.setMaximized(true);
	}

	@Override
	protected void constrainShellSize() {
		super.constrainShellSize();
		getShell().setSize(300, 200);
	}

	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(newShellStyle & ~(SWT.APPLICATION_MODAL));
	}

	@Override
	public boolean close() {
		try {
			return super.close();
		} finally {
			if (current == this)
				current = null;
		}
	}

	public static void update() {
		if (current != null) {
			current.averageCanvas.setChart(current.averageConfig);
			current.averageCanvas.chart = null;
			current.averageCanvas.redraw();
		}
	}

}
