/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.kiegeland.boerse.Manager;
import com.kiegeland.boerse.chart.ChartDialog;
import com.kiegeland.boerse.conditions.Condition;
import com.kiegeland.boerse.domain.Gain;
import com.kiegeland.boerse.domain.Stock;
import com.kiegeland.boerse.domain.Stocks;
import com.kiegeland.boerse.table.ITaggedValues;
import com.kiegeland.boerse.table.KBoersenModel;

public class OneStockGainsView extends ViewPart {

	private Stocks aStocks;

	private KTable subTable;

	public OneStockGainsView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		subTable = new KTable(parent, SWT.FULL_SELECTION | SWTX.AUTO_SCROLL | SWT.FILL) {

			@Override
			protected void fireCellSelection(int col, int row, int statemask) {
				super.fireCellSelection(col, row, statemask);
				ITaggedValues tv = ((KBoersenModel) this.getModel()).stocks.get(row - 1);
				if (tv instanceof Gain) {
					Gain gain = (Gain) tv;
					if (gain.getStocks() != null) {
						Stocks aStocks = gain.getStocks();
						Stock buy = gain.getBuyStock();
						System.out.println("success=" + Condition.getGain(buy));
						OneStockDaysView viewPart3 = (OneStockDaysView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("com.kiegeland.boerse.views.OneStockDaysView");
						if (viewPart3 != null)
							viewPart3.setStocks(aStocks, buy);
						ChartDialog.displayIfOpened(aStocks, buy);
					}
				}
			}

			@Override
			protected void fireCellDoubleClicked(int col, int row, int statemask) {
				ITaggedValues tv = ((KBoersenModel) this.getModel()).stocks.get(row - 1);
				if (tv instanceof Gain) {
					Gain gain = (Gain) tv;
					// Stocks aStocks2 = new
					// Stocks(aStocks.getSymbol(),aStocks.getStocks().subList(2,5));
					Stock buyStock = gain.getBuyStock();
					ChartDialog.display(aStocks, buyStock);
				}
			}

		};
		setStocks(new Stocks("dummy"));
	}

	@Override
	public void setFocus() {
		// nothing to do
	}

	public void setStocks(Stocks stocks) {
		this.aStocks = stocks;
		List<Gain> gains = Manager.calcGains(aStocks);
		KBoersenModel model = new KBoersenModel(gains, Manager.getColumns(gains, aStocks.getSymbol()));
		subTable.setModel(model);
	}

}
