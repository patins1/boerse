/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.table;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.kiegeland.boerse.Manager;
import com.kiegeland.boerse.domain.Gain;
import com.kiegeland.boerse.domain.Stocks;

public class StockTableDialog extends Dialog {

	private final Stocks aStocks;

	public StockTableDialog(Shell shell, Stocks aStocks) {
		super(shell);
		this.aStocks = aStocks;
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE | SWT.MAX;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite result = (Composite) super.createDialogArea(parent);

		List<Gain> gains = Manager.calcGains(aStocks);
		KBoersenModel model = new KBoersenModel(gains, Manager.getColumns(gains, aStocks.getSymbol()));
		final Point fullSize = new Point(0, 0);
		for (int col = 0; col < model.getColumnCount(); col++) {
			fullSize.x += model.getColumnWidth(col);
		}
		for (int row = 0; row < model.getRowCount(); row++) {
			fullSize.y += model.getRowHeight(row);
		}
		KTable subTable = new KTable(result, SWT.FULL_SELECTION | SWTX.AUTO_SCROLL | SWT.FILL) {
			@Override
			public Point computeSize(int hint, int hint2, boolean changed) {
				return new Point(fullSize.x, fullSize.y + 17);
			}

			@Override
			protected void fireCellDoubleClicked(int col, int row, int statemask) {
				ITaggedValues tv = ((KBoersenModel) this.getModel()).stocks.get(row - 1);
				if (tv instanceof Gain) {
					final Gain gain = (Gain) tv;
					// int index = aStocks.getStocks().indexOf(gain.getUserObject());
					// Stocks aStocks2 = new Stocks(aStocks.getSymbol(), aStocks.getStocks().subList(index, index + 20));
					// final Chart chart = new BoersenChart(aStocks2, aStocks.getStocks().get(index)).createCFStockChart();
					// StockChartDialog dialog = new StockChartDialog((Shell) null, chart);
					// dialog.open();
				}
			}

		};
		subTable.setModel(model);
		return result;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		String symbol = aStocks.getSymbol();
		newShell.setText(symbol);
	}

}
