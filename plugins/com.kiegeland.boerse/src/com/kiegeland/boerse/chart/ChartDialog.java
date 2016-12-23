/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.chart;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.kiegeland.boerse.domain.Stock;
import com.kiegeland.boerse.domain.Stocks;
import com.kiegeland.boerse.util.Utilities;

public class ChartDialog extends Dialog {

	public static ChartDialog current = null;

	private ChartCanvas canvas;

	public Stocks stocks;

	private Stock stock;

	private Button introdayButton;

	private int oriX;

	private int orioffset;

	public Stocks istocks;

	private Button depthButton;

	private Button ordersButton;

	private Button profitButton;

	public Button notifyButton;

	private Button volumnsButton;

	private Button fittingButton;

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

		result.addMouseWheelListener(new MouseWheelListener() {
			public void mouseScrolled(MouseEvent e) {
				int count = e.count;
				int rangeOld = BoersenChart.range;
				BoersenChart.range = Math.max(3, count > 0 ? BoersenChart.range * 3 / 4 : BoersenChart.range * 4 / 3);
				int rangeDiff = BoersenChart.range - rangeOld;
				BoersenChart.offset = Math.max(0, BoersenChart.offset - (rangeDiff - rangeDiff * e.x / (canvas.getSize().x - 120)));
				repaint();
			}
		});

		canvas.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				if ((e.stateMask & SWT.BUTTON1) != 0) {
					int diff = e.x - oriX;
					BoersenChart.offset = Math.max(0, orioffset + BoersenChart.range * diff / (canvas.getSize().x - 120));
					repaint();
					System.out.println("BoersenChart.offset:" + BoersenChart.offset);
				} else {
					oriX = e.x;
					orioffset = BoersenChart.offset;
				}
			}

		});

		return result;
	}

	private void calcChart() {
		if (canvas != null) {
			try {
				if (BoersenChart.intraday) {
					if (istocks == null) {
						int samesame = 0;
						istocks = new Stocks(stocks.getSymbol());
						istocks.setName(stocks.getName());
						istocks.setGroup(stocks.getGroup());
						List<Stock> sss = new ArrayList<Stock>();
						File dir = new File("C:\\kurse\\snapshots");
						Stock lastistock = null;
						for (String file : dir.list()) {
							if (file.startsWith(istocks.getSymbol() + " ")) {
								Date date = null;
								if (file.contains("PM."))
									date = Stock.dateFormat4.parse(file.substring(file.indexOf(" ") + 1));
								else
									date = Stock.dateFormat3.parse(file.substring(file.indexOf(" ") + 1));
								if (date.getHours() >= 15) {
									continue;
								}
								if (date.getHours() < 9) {
									continue;
								}
								String text = Utilities.fromFile(new File(dir, file));
								Stock istock = parseStock(istocks, text, date);
								if (istock == null) {
									continue;
								}
								istock.setDate(date);
								if (lastistock != null && lastistock.same(istock)) {
									samesame++;
									continue;
								}
								if (lastistock != null && lastistock.getVolume() == istock.getVolume() || istock.getVolume() == 0) {
									samesame++;
									continue;
								}
								// if (lastistock != null && istock.getClose() == lastistock.getClose()) {
								// sss.set(sss.size() - 1, istock);
								// continue;
								// }
								sss.add(istock);
								lastistock = istock;
							}
						}
						istocks.setStocks(sss);
						System.out.println("samesame:" + samesame);
					}

					canvas.setVisible(istocks.size() != 0);
					if (canvas.isVisible())
						canvas.setChart(new BoersenChart(istocks, istocks.getLatestStock()).createCFStockChart());

				} else {
					canvas.setVisible(true);
					canvas.setChart(new BoersenChart(stocks, stock).createCFStockChart());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			enabledButtons();
		}
	}

	public static Stock parseStock(Stocks istocks, String text, Date date) {
		Stock istock = new Stock(istocks);
		istock.setDate(date);
		float last = findCol("Last_field\" autocomplete=\"off\">", "<", text);

		istock.setHigh(findCol("Offer", text));
		istock.setLow(findCol("Bid", text));
		istock.setOpen(last);
		istock.setClose(last);

		istock.setVolume(Math.round(findCol("lblVolume_field\" autocomplete=\"off\">", "<", text)));
		istock.setBuyers(Math.round(findCol("buyers for ", " units", text)));
		istock.setSellers(Math.round(findCol("sellers for ", " units", text)));
		istock.setBuyersPeople(Math.round(findCol("TotalBuyersForUnits\":\"", " buyers for", text)));
		istock.setSellersPeople(Math.round(findCol("TotalSellersForUnits\":\"", " sellers for", text)));
		if (last == 0) {
			return null;
		}
		int pos = 0;
		for (String atext : text.split(Pattern.quote("},{"))) {
			float bn = findCol("Bn\":\"", "\"", atext);
			if (bn == 0)
				continue;
			istock.addBuyer(Math.round(findCol("Bq\":\"", "\"", atext)), findCol("Bp\":\"", "\"", atext), Math.round(findCol("Sq\":\"", "\"", atext)), findCol("Sp\":\"", "\"", atext), pos);
			pos = pos + 1;
		}

		return istock;
	}

	private static float findCol(String str, String text) {
		return findCol(str + "_Quote_field\" autocomplete=\"off\">", "<", text);
	}

	private static float findCol(String str, String end, String text) {
		int index = text.indexOf(str);
		if (index != -1) {
			index += str.length();
			String s = text.substring(index, text.indexOf(end, index));
			s = s.replace(",", "");
			return Float.parseFloat(s);
		}
		return 0;
	}

	private void enabledButtons() {
		if (getButton(22) != null)
			getButton(22).setEnabled(BoersenChart.range > 1);
		if (getButton(23) != null)
			getButton(23).setEnabled(stocks.size() > BoersenChart.range);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		notifyButton = createCheck(parent, 30, "Notify", false);
		introdayButton = createCheck(parent, 25, "Intraday", false);
		depthButton = createCheck(parent, 27, "Depth", false);
		ordersButton = createCheck(parent, 28, "Orders", false);
		profitButton = createCheck(parent, 29, "Profit", false);
		volumnsButton = createCheck(parent, 31, "Volumn", false);
		fittingButton = createCheck(parent, 32, "Fitting", false);
		createButton(parent, 26, "Update", false);
		createButton(parent, 24, "Yahoo", false);
		enabledButtons();
	}

	protected Button createCheck(Composite parent, int id, String label, boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.CHECK);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(Integer.valueOf(id));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(((Integer) event.widget.getData()).intValue());
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		// buttons.put(Integer.valueOf(id), button);
		setButtonLayoutData(button);
		return button;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 26) {
			istocks = null;
			repaint();
		} else if (buttonId == 25) {
			BoersenChart.intraday = introdayButton.getSelection();
			BoersenChart.offset = 0;
			BoersenChart.range = 32;
			repaint();
		} else if (buttonId == 27) {
			BoersenChart.showDepth = depthButton.getSelection();
			repaint();
		} else if (buttonId == 28) {
			BoersenChart.showOrders = ordersButton.getSelection();
			repaint();
		} else if (buttonId == 31) {
			BoersenChart.showVolumn = volumnsButton.getSelection();
			repaint();
		} else if (buttonId == 32) {
			BoersenChart.showFitting = fittingButton.getSelection();
			repaint();
		} else if (buttonId == 29) {
			BoersenChart.showProfit = profitButton.getSelection();
			repaint();
		} else if (buttonId == 24) {
			try {
				URI uri = new URI("https://au.finance.yahoo.com/echarts?s=" + stocks.getSymbol() + ".AX#symbol=" + stocks.getSymbol() + ".AX;range=5d");
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(uri);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else
			super.buttonPressed(buttonId);
	}

	public void repaint() {
		calcChart();
		current.canvas.redraw();
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
		getShell().setSize(1000, 600);
		super.constrainShellSize();
	}

	private void setChart(Stocks stocks, Stock stock) {
		if (this.stocks != stocks) {
			this.istocks = null;
		}
		this.stocks = stocks;
		this.stock = stock;
		BoersenChart.offset = stocks.size() - 1 - stocks.indexOf(stock);
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
