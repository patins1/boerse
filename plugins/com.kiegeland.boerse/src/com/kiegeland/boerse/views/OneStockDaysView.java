package com.kiegeland.boerse.views;

import java.util.Arrays;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

import com.kiegeland.boerse.chart.ChartDialog;
import com.kiegeland.boerse.chart.BoersenChart;
import com.kiegeland.boerse.domain.Stock;
import com.kiegeland.boerse.domain.Stocks;
import com.kiegeland.boerse.table.ITaggedValues;
import com.kiegeland.boerse.table.KBoersenModel;

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
		KBoersenModel model = new KBoersenModel(aStocks.asList(), Arrays.asList("Date", "Asc", "Open", "High", "Low", "Close", "Volume", "Adj Close"));
		historyTable.setModel(model);
		int row = model.getRowOf(selection);
		if (row >= 0) {
			historyTable.setSelection(0, row, true);
		}
	}

}
