/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.kiegeland.boerse.BorderLayout;
import com.kiegeland.boerse.Manager;
import com.kiegeland.boerse.chart.ChartDialog;
import com.kiegeland.boerse.domain.Gain;
import com.kiegeland.boerse.domain.Markt;
import com.kiegeland.boerse.domain.Stock;
import com.kiegeland.boerse.domain.Stocks;
import com.kiegeland.boerse.table.ITaggedValues;
import com.kiegeland.boerse.table.KBoersenModel;
import com.kiegeland.boerse.util.Utilities;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.SWTX;

public class AllStockGainsView extends ViewPart {

	private KTable gainsTable;

	public AllStockGainsView() {
	}

	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new BorderLayout());

		gainsTable = new KTable(parent, SWT.FULL_SELECTION | SWTX.AUTO_SCROLL | SWT.FILL) {

			@Override
			protected void fireCellSelection(int col, int row, int statemask) {
				super.fireCellSelection(col, row, statemask);
				ITaggedValues tv = ((KBoersenModel) this.getModel()).stocks.get(row - 1);
				if (tv instanceof Gain) {
					Gain gain = (Gain) tv;
					if (gain.getStocks() != null) {
						Stocks aStocks = gain.getStocks();
						// System.out.println("" + aStocks.getSymbol() + " vol=" + aStocks.volume + " vol-lastest=" + aStocks.getLatestStock().volume);
						OneStockGainsView viewPart2 = (OneStockGainsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("com.kiegeland.boerse.views.OneStockGainsView");
						if (viewPart2 != null)
							viewPart2.setStocks(aStocks);
						OneStockDaysView viewPart3 = (OneStockDaysView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("com.kiegeland.boerse.views.OneStockDaysView");
						if (viewPart3 != null)
							viewPart3.setStocks(aStocks, aStocks.getLatestStock());
						ChartDialog.displayIfOpened(aStocks, aStocks.getLatestStock());
					} else {
						double allShorts = 0;
						int shortsCount = 0;
						OneStockDaysView.shortages = 0;
						OneStockDaysView.shortagesGreen = 0;
						for (Markt markt : Manager.getEnabledMaerkte()) {
							for (Stocks aStocks : markt.getStocks()) {
								double shorts = OneStockDaysView.getShorts(aStocks);
								if (!Double.isNaN(shorts)) {
									allShorts += shorts;
									shortsCount++;
								}
							}
						}
						System.out.println("AllShorts= " + Utilities.printPercentage(allShorts / shortsCount) + " #" + OneStockDaysView.shortages + " GreenPropability= " + Utilities.printPercentage(1.0 * OneStockDaysView.shortagesGreen / OneStockDaysView.shortages));
					}
				}
			}

			@Override
			protected void fireCellDoubleClicked(int col, int row, int statemask) {
				ITaggedValues tv = ((KBoersenModel) this.getModel()).stocks.get(row - 1);
				if (tv instanceof Gain) {
					Gain gain = (Gain) tv;
					if (gain.getStocks() != null) {
						Stocks aStocks = gain.getStocks();
						ChartDialog.display(aStocks, aStocks.getLatestStock());
					}
				}
			}

		};
		gainsTable.setLayoutData(new BorderLayout.BorderData(BorderLayout.CENTER));

		calcMainTable();
	}

	public void calcMainTable() {
		List<Gain> summedGains = calcAll();
		Collections.sort(summedGains, new Comparator<Gain>() {

			@Override
			public int compare(Gain o1, Gain o2) {
				if (o1.stocks == null)
					return 1;
				if (o2.stocks == null)
					return -1;
				// return (int) (o2.stocks.getLatestStock().volume
				// / (float) o2.stocks.volume - o1.stocks.getLatestStock().volume
				// / (float) o1.stocks.volume);

				// int result = o1.summarizeSize - o2.summarizeSize;
				// if (result != 0)
				// return result;
				double success = o1.success - o2.success;
				if (success != 0)
					return success < 0 ? -1 : 1;
				return o2.stocks.getStockName().compareTo(o1.stocks.getStockName());
				// return (int) (o1.stocks.volume-o2.stocks.volume);
			}
		});

		gainsTable.setModel(new KBoersenModel(summedGains, Manager.getColumns(summedGains, "Symbol")));
	}

	private List<Gain> calcAll() {
		List<Gain> summedGains = new ArrayList<Gain>();
		int totalGains = 0;
		for (Markt markt : Manager.getEnabledMaerkte())
			for (Stocks aStocks : markt.getStocks()) {
				List<Gain> gains = Manager.calcGains(aStocks);
				Gain summedGain = calcAverageGain(aStocks, gains);
				String gainDesc = aStocks.getStockName();
				summedGain.summarizeSize = gains.size();
				if (summedGain.summarizeSize >= 1) {
					if (summedGain.summarizeSize > 1)
						gainDesc += ":" + summedGain.summarizeSize;
					float success = 0;
					for (Gain gain : gains)
						success += gain.success;
					summedGain.success = success / summedGain.summarizeSize;
					Stock buy = aStocks.getLatestStock();
					Stock base = buy.getStock(-130);
					if (base != null)
						summedGain.success = buy.close / base.close;
					else
						summedGain.success = 0;
					gainDesc += " " + (Utilities.printPercentage(summedGain.success));
				}
				summedGain.setSymbol(gainDesc + " " + aStocks.getSymbol());
				summedGains.add(summedGain);
				totalGains += gains.size();
			}

		Gain totalSum = calcAverageGain(null, summedGains);
		totalSum.setSymbol("SUM:" + totalGains);
		summedGains.add(0, totalSum);
		return summedGains;
	}

	private Gain calcAverageGain(Stocks aStocks, List<Gain> gains) {
		Gain summedGain = new Gain(aStocks != null ? aStocks.getSymbol() : "", aStocks, null);
		if (!gains.isEmpty())
			for (int day = 0; true; day++) {
				double s = 0;
				int number = 0;
				for (Gain gain : gains) {
					Float percentage = gain.getPercentage(day);
					if (percentage == null)
						continue;
					s += percentage;
					number++;
				}
				if (number == 0)
					break;
				summedGain.add(s / number);
			}
		return summedGain;
	}

	@Override
	public void setFocus() {
	}
}
