/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.views;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.kiegeland.boerse.chart.ChartDialog;
import com.kiegeland.boerse.domain.Stock;
import com.kiegeland.boerse.domain.Stocks;
import com.kiegeland.boerse.table.ITaggedValues;
import com.kiegeland.boerse.table.KBoersenModel;
import com.kiegeland.boerse.util.Utilities;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.SWTX;

public class OneStockDaysView extends ViewPart {

	private Stocks aStocks;

	private KTable historyTable;

	public OneStockDaysView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		historyTable = new KTable(parent, SWT.FULL_SELECTION | SWTX.AUTO_SCROLL | SWTX.FILL_WITH_LASTCOL | SWTX.FILL_WITH_LASTCOL) {

			@Override
			protected void fireCellDoubleClicked(int col, int row, int statemask) {
				ITaggedValues tv = ((KBoersenModel) this.getModel()).stocks.get(row - 1);
				if (tv instanceof Stock) {
					Stock stock = (Stock) tv;
					if (stock.getStocks() != null) {
						final Stocks aStocks = stock.getStocks();
						ChartDialog.display(aStocks, stock);
					}
				}
			}

			@Override
			protected void fireCellSelection(int col, int row, int statemask) {
				ITaggedValues tv = ((KBoersenModel) this.getModel()).stocks.get(row - 1);
				if (tv instanceof Stock) {
					Stock stock = (Stock) tv;
					if (stock.getStocks() != null) {
						final Stocks aStocks = stock.getStocks();
						ChartDialog.displayIfOpened(aStocks, stock);
					}
				}
			}

		};
		setStocks(new Stocks("dummy"), null);
	}

	@Override
	public void setFocus() {
		// nothing to do
	}

	public void setStocks(Stocks stocks, Stock selection) {
		this.aStocks = stocks;
		getShorts(stocks);
		KBoersenModel model = new KBoersenModel(aStocks.asList(), Arrays.asList("Date", "Asc", "Open", "High", "Low", "Close", "Volume", "Adj Close"));
		historyTable.setModel(model);
		int row = model.getRowOf(selection);
		if (row >= 0) {
			historyTable.setSelection(0, row, true);
		}
	}

	static public int shortages = 0;
	static public int shortagesGreen = 0;

	public static double getShorts(Stocks stocks) {
		int green = 0;
		int red = 0;
		double gainSum = 0;
		for (Stock stock : stocks.asList()) {
			if (1900 + stock.date.getYear() != 2015) {
				RGB col = stock.getBackgoundColor(0);
				if (col.red != col.green) {
					shortages++;
					if (col.red == 0) {
						green++;
					} else if (col.green == 0) {
						red++;
					}
					double gain = stock.succ.getDelta();
					if (stock.succ.getDelta() > 0) {
						// gain += stock.succ.succ.getDelta();
						// if (stock.succ.succ.getDelta()>0) {
						// gainSum += stock.succ.succ.succ.getDelta();
						//// if (stock.succ.succ.succ.getDelta()>0) {
						//// gainSum += stock.succ.succ.succ.succ.getDelta();
						//// }
						// }
					}
					if (gain > 0)
						shortagesGreen++;
					gainSum += gain;
				}
			}
		}
		if (red != 0 || green != 0) {
			double oneShort = gainSum / (red + green);
			System.out.println(stocks.getSymbol() + " has " + red + "\t reds and " + green + "\tgreens; success probability " + Utilities.printPercentage(1.0 * green / red - 1) + "\t gain sum " + Utilities.printPercentage(gainSum) + "\t avg. gain " + Utilities.printPercentage(oneShort));
			return oneShort;
		}
		return 0;
	}

}
