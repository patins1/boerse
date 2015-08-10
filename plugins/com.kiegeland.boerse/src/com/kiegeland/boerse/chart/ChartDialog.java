package com.kiegeland.boerse.chart;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.kiegeland.boerse.domain.Stock;
import com.kiegeland.boerse.domain.Stocks;

public class ChartDialog extends Dialog {

	private static ChartDialog current = null;

	private ChartCanvas canvas;

	private Stocks stocks;

	private Stock stock;

	public ChartDialog() {
		super(PlatformUI.getWorkbench().getDisplay().getActiveShell());
		this.setBlockOnOpen(false);
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE | SWT.MAX;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite result = (Composite) super.createDialogArea(parent);
		result.setLayout(new FillLayout());
		canvas = new ChartCanvas(result, SWT.NO_BACKGROUND);
		calcChart();
		return result;
	}

	private void calcChart() {
		if (canvas != null) {
			canvas.setChart(new BoersenChart(stocks, stock).createCFStockChart());
			enabledButtons();
		}
	}

	private void enabledButtons() {
		if (getButton(22) != null)
			getButton(22).setEnabled(BoersenChart.range > 1);
		if (getButton(23) != null)
			getButton(23).setEnabled(stocks.size() > BoersenChart.range);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, 24, "Yahoo", false);
		createButton(parent, 22, "Halve", false);
		createButton(parent, 23, "Double", false);
		enabledButtons();
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 22) {
			BoersenChart.range = BoersenChart.range / 2;
			calcChart();
			current.canvas.redraw();
		} else if (buttonId == 23) {
			BoersenChart.range = BoersenChart.range * 2;
			calcChart();
			current.canvas.redraw();
		} else if (buttonId == 24) {
			try {
				URI uri = new URI("http://de.finance.yahoo.com/q?s=" + stocks.getSymbol());
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(uri);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else
			super.buttonPressed(buttonId);
	}

	@Override
	protected Control createContents(Composite parent) {
		return super.createContents(parent);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		// newShell.setMaximized(true);
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

	@Override
	protected void constrainShellSize() {
		getShell().setSize(600, 600);
		super.constrainShellSize();
	}

	private void setChart(Stocks stocks, Stock stock) {
		this.stocks = stocks;
		this.stock = stock;
		calcChart();
	}

	public static void displayIfOpened(Stocks stocks, Stock stock) {
		if (current != null)
			display(stocks, stock);
	}

	static public void display(Stocks stocks, Stock stock) {
		if (current == null) {
			current = new ChartDialog();
			current.setChart(stocks, stock);
			current.open();
		} else {
			current.setChart(stocks, stock);
			current.canvas.redraw();
		}
	}

}
