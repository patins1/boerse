/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.chart;

import static com.kiegeland.boerse.util.Utilities.getLatestStock;
import static com.kiegeland.boerse.util.Utilities.getOldestStock;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.birt.chart.extension.datafeed.DifferenceEntry;
import org.eclipse.birt.chart.extension.datafeed.StockEntry;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.LineStyle;
import org.eclipse.birt.chart.model.attribute.NumberFormatSpecifier;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.GradientImpl;
import org.eclipse.birt.chart.model.attribute.impl.JavaDateFormatSpecifierImpl;
import org.eclipse.birt.chart.model.attribute.impl.NumberFormatSpecifierImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.CurveFitting;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.CurveFittingImpl;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.DataSet;
import org.eclipse.birt.chart.model.data.DateTimeDataSet;
import org.eclipse.birt.chart.model.data.DifferenceDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.StockDataSet;
import org.eclipse.birt.chart.model.data.impl.DateTimeDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.DifferenceDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataElementImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.StockDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.layout.TitleBlock;
import org.eclipse.birt.chart.model.type.AreaSeries;
import org.eclipse.birt.chart.model.type.DifferenceSeries;
import org.eclipse.birt.chart.model.type.StockSeries;
import org.eclipse.birt.chart.model.type.impl.AreaSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.DifferenceSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.StockSeriesImpl;
import org.eclipse.birt.chart.util.CDateTime;

import com.kiegeland.boerse.Manager;
import com.kiegeland.boerse.domain.Stock;
import com.kiegeland.boerse.domain.Stocks;

public class BoersenChart {

	private List<Stock> aStocks;
	private Stock baseStock;

	public boolean intraday = false;
	public boolean showDepth = false;
	public boolean showProfit = false;
	public boolean showOrders = false;
	public boolean showVolumn = false;
	public boolean showFitting = false;
	public boolean showMACD = false;
	public boolean showRSI = false;
	public boolean showFastSTO = false;
	public boolean showNormalized = false;

	private List<Stock> allstocks;
	double totalProfit = 0;
	private List<Stock> aStocksMinMax;
	private Stocks original;
	public boolean showLpSolved;

	public BoersenChart(List<Stock> aStocks, Stock baseStock, List<Stock> allstocks, List<Stock> aStocksMinMax, Stocks original) {
		this.aStocks = aStocks;
		this.baseStock = baseStock;
		this.allstocks = allstocks;
		this.aStocksMinMax = aStocksMinMax;
		this.original = original;
	}

	public final Chart createCFStockChart() throws ParseException {

		Map<Stock, Integer> indexes = getIndexes();

		if (showNormalized) {
			int firstVisible = indexes.get(getOldestStock(aStocks));
			int lastVisible = indexes.get(getLatestStock(aStocks)) + 1;
			List<Stock> theStocks = normalizeVolumes(aStocks);
			int preHundert = Math.max(0, firstVisible - 100);
			List<Stock> theNewAllStocks = normalizeVolumes(allstocks.subList(0, preHundert));
			theNewAllStocks.addAll(normalizeVolumes(allstocks.subList(preHundert, firstVisible)));
			theNewAllStocks.addAll(theStocks);
			theNewAllStocks.addAll(normalizeVolumes(allstocks.subList(lastVisible, allstocks.size())));
			allstocks = theNewAllStocks;
			aStocks = theStocks;
			indexes = getIndexes();
		}

		List<Crazy> craziest = new ArrayList<Crazy>();
		craziest.add(new Crazy("A2M", 1.7050, 1.7750, "2016-01-27", "2016-02-16"));
		craziest.add(new Crazy("A2M", 2.050, 2.210, "2016-11-09", "2016-12-05"));
		craziest.add(new Crazy("BAL", 12.4000, 10.7900, "2016-02-19", "2016-02-29"));
		craziest.add(new Crazy("BHP", 18.6200, 17.5650, "2016-03-07", "2016-03-11"));
		craziest.add(new Crazy("BKL", 212.3600, 172.6276, "2016-01-20", "2016-02-11"));
		craziest.add(new Crazy("CBA", 75.0192, 74.200000, "2016-02-09", "2016-02-12"));
		craziest.add(new Crazy("CBA", 75.0192, 78.000000, "2016-02-09", "2016-08-10"));
		craziest.add(new Crazy("JBH", 22.1195, 21.1490, "2016-02-17", "2016-02-18"));
		craziest.add(new Crazy("SXY", .1625, .1750, "2016-02-18", "2016-02-23"));
		craziest.add(new Crazy("TWE", 8.7458, 8.9500, "2016-02-17", "2016-02-18"));

		ChartWithAxes cwaStock = ChartWithAxesImpl.create();

		// Title
		cwaStock.getTitle().getLabel().getCaption().getFont().setName("Arial");// $NON-NLS-1$
		cwaStock.getTitle().getLabel().getCaption().getFont().setItalic(true);// $NON-NLS-1$
		cwaStock.getTitle().getLabel().getCaption().setValue(baseStock != null ? baseStock.toString() : original.getSymbol());// $NON-NLS-1$
		TitleBlock tb = cwaStock.getTitle();
		tb.setBackground(GradientImpl.create(ColorDefinitionImpl.create(0, 128, 0), ColorDefinitionImpl.create(128, 0, 0), 0, false));
		tb.getLabel().getCaption().setColor(ColorDefinitionImpl.WHITE());

		// Plot
		cwaStock.getBlock().setBackground(GradientImpl.create(ColorDefinitionImpl.create(240, 240, 240), ColorDefinitionImpl.create(255, 255, 255), 90, false));
		cwaStock.getPlot().getClientArea().getInsets().set(10, 10, 10, 10);

		// Legend
		cwaStock.getLegend().setBackground(ColorDefinitionImpl.ORANGE());
		cwaStock.getLegend().setVisible(false);

		// X-Axis
		Axis xAxisPrimary = ((ChartWithAxesImpl) cwaStock).getPrimaryBaseAxes()[0];
		xAxisPrimary.setFormatSpecifier(JavaDateFormatSpecifierImpl.create("yyyy-MM-dd"));//$NON-NLS-1$
		if (Manager.CourseOfSales)
			xAxisPrimary.setFormatSpecifier(JavaDateFormatSpecifierImpl.create("MM-dd  HH:mm:ss"));//$NON-NLS-1$
		if (intraday)
			xAxisPrimary.setFormatSpecifier(JavaDateFormatSpecifierImpl.create("MM-dd  HH:mm"));//$NON-NLS-1$

		xAxisPrimary.getTitle().getCaption().setValue("X Axis");//$NON-NLS-1$
		xAxisPrimary.getTitle().getCaption().setColor(ColorDefinitionImpl.RED());
		xAxisPrimary.getTitle().getCaption().setValue("Date");//$NON-NLS-1$
		xAxisPrimary.setTitlePosition(Position.ABOVE_LITERAL);

		xAxisPrimary.getLabel().getCaption().setColor(ColorDefinitionImpl.RED());
		xAxisPrimary.getLabel().getCaption().getFont().setName("Arial");
		xAxisPrimary.getLabel().getCaption().getFont().setRotation(65);
		xAxisPrimary.setLabelPosition(Position.ABOVE_LITERAL);

		xAxisPrimary.setType(AxisType.DATE_TIME_LITERAL);
		// xAxisPrimary.setType(AxisType.TEXT_LITERAL);
		xAxisPrimary.getOrigin().setType(IntersectionType.MAX_LITERAL);

		xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.ABOVE_LITERAL);
		xAxisPrimary.getMajorGrid().getLineAttributes().setColor(ColorDefinitionImpl.create(255, 196, 196));
		xAxisPrimary.getMajorGrid().getLineAttributes().setStyle(LineStyle.DOTTED_LITERAL);
		xAxisPrimary.getMajorGrid().getLineAttributes().setVisible(true);

		xAxisPrimary.setCategoryAxis(true);

		// Y-Axis
		Axis yAxisPrimary = ((ChartWithAxesImpl) cwaStock).getPrimaryOrthogonalAxis(xAxisPrimary);

		yAxisPrimary.getLabel().getCaption().setValue("Price Axis");//$NON-NLS-1$
		yAxisPrimary.getLabel().getCaption().setColor(ColorDefinitionImpl.BLUE());
		yAxisPrimary.setLabelPosition(Position.LEFT_LITERAL);

		yAxisPrimary.getTitle().getCaption().setValue("Microsoft ($ Stock Price)");//$NON-NLS-1$
		yAxisPrimary.getTitle().getCaption().setColor(ColorDefinitionImpl.BLUE());
		yAxisPrimary.setTitlePosition(Position.LEFT_LITERAL);

		double minVolumn = Double.MAX_VALUE;
		double maxVolumn = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double minDemand = Double.MAX_VALUE;
		double maxDemand = Double.MIN_VALUE;
		double minDemandPeople = Double.MAX_VALUE;
		double maxDemandPeople = Double.MIN_VALUE;
		double minSellers = Double.MAX_VALUE;
		double maxSellers = Double.MIN_VALUE;
		double minBuyers = Double.MAX_VALUE;
		double maxBuyers = Double.MIN_VALUE;
		for (Stock stock : aStocks) {
			min = Math.min(stock.getLow(), min);
			max = Math.max(stock.getHigh(), max);
			minVolumn = Math.min(stock.volume, minVolumn);
			maxVolumn = Math.max(stock.volume, maxVolumn);
			minDemand = Math.min(stock.getBuyers(), minDemand);
			maxDemand = Math.max(stock.getBuyers(), maxDemand);
			minDemand = Math.min(stock.getSellers(), minDemand);
			maxDemand = Math.max(stock.getSellers(), maxDemand);
			minDemandPeople = Math.min(stock.getBuyersPeople(), minDemandPeople);
			maxDemandPeople = Math.max(stock.getBuyersPeople(), maxDemandPeople);
			minDemandPeople = Math.min(stock.getSellersPeople(), minDemandPeople);
			maxDemandPeople = Math.max(stock.getSellersPeople(), maxDemandPeople);
			minSellers = Math.min(stock.getSellers(), minSellers);
			maxSellers = Math.max(stock.getSellers(), maxSellers);
			minBuyers = Math.min(stock.getBuyers(), minBuyers);
			maxBuyers = Math.max(stock.getBuyers(), maxBuyers);
		}
		List<Double> buys = new ArrayList<Double>();
		List<Double> sells = new ArrayList<Double>();

		Stock last = null;
		for (Stock stock : aStocks) {
			if (last != null) {
				// int buy = (stock.getBuyers() - last.getBuyers()) * (stock.getBuyersPeople() - last.getBuyersPeople());
				// int sell = (stock.getSellers() - last.getSellers()) * (stock.getSellersPeople() - last.getSellersPeople());
				// int buy = stock.getBuyers() - last.getBuyers();
				// int sell = stock.getSellers() - last.getSellers();
				int buy = stock.getBuyersPeople() - last.getBuyersPeople();
				int sell = stock.getSellersPeople() - last.getSellersPeople();
				buys.add(1.0 * buy);
				sells.add(1.0 * sell);
			} else {
				buys.add(0.0);
				sells.add(0.0);
			}
			last = stock;
		}

		buys.add(0, buys.get(1));

		minDemandPeople = Stream.concat(sells.stream(), buys.stream()).min(Double::compare).orElse(0.0);
		maxDemandPeople = Stream.concat(sells.stream(), buys.stream()).max(Double::compare).orElse(0.0);

		if (min == max) {
			max += 0.001;
		}

		yAxisPrimary.getMajorGrid().getLineAttributes().setColor(ColorDefinitionImpl.create(196, 196, 255));
		yAxisPrimary.getMajorGrid().getLineAttributes().setStyle(LineStyle.DOTTED_LITERAL);
		yAxisPrimary.getMajorGrid().getLineAttributes().setVisible(true);
		yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);

		yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
		yAxisPrimary.getOrigin().setType(IntersectionType.MIN_LITERAL);

		List<Double> volumns = new ArrayList<Double>();
		List<CDateTime> dates = new ArrayList<CDateTime>();
		List<StockEntry> stockEntries = new ArrayList<StockEntry>();
		for (Stock stock : aStocks) {
			StockEntry entry = new StockEntry(stock.getOpen(), stock.getLow(), stock.getHigh(), stock.getClose());
			stockEntries.add(entry);
			dates.add(new CDateTime(stock.date));
			volumns.add((stock.volume - minVolumn) / (maxVolumn - minVolumn) * (max - min) + min);
		}

		// Data Set
		DateTimeDataSet dsDateValues = DateTimeDataSetImpl.create(dates.toArray(new CDateTime[] {}));
		StockDataSet dsStockValues = StockDataSetImpl.create(stockEntries.toArray(new StockEntry[] {}));
		DataSet orthoValuesDataSet2 = NumberDataSetImpl.create(volumns.toArray(new Double[] {}));
		List<DifferenceDataSet> differenceDataSets = new ArrayList<DifferenceDataSet>();

		int totalStockCount = 720 * 1000000;
		if (original.getSymbol().contentEquals("BHP"))
			totalStockCount = 5323 * 1000000;
		if (original.getSymbol().contentEquals("BAL"))
			totalStockCount = 96 * 1000000;
		if (original.getSymbol().contentEquals("BKL"))
			totalStockCount = 17 * 1000000;

		boolean showAvgPurchaseCost = !true;
		if (showAvgPurchaseCost) {
			final List<DifferenceEntry> sea = new ArrayList<DifferenceEntry>();
			double weightedValue = getOldestStock(aStocks).getOpen();
			double summedValue = 0;
			double summedVolumne = 0;
			for (Stock stock : aStocks) {
				weightedValue = stock.close * stock.getVolume() / totalStockCount + weightedValue * (totalStockCount - stock.getVolume()) / totalStockCount;

				summedValue += stock.close * stock.getVolume();
				summedVolumne += stock.getVolume();
				double weighted = (summedValue / summedVolumne) * summedVolumne / totalStockCount + getOldestStock(aStocks).getOpen() * (totalStockCount - summedVolumne) / totalStockCount;

				sea.add(new DifferenceEntry(weighted, weightedValue));
			}
			differenceDataSets.add(DifferenceDataSetImpl.create(sea.toArray(new DifferenceEntry[] {})));
		}

		boolean showOvernightAndDuringdayInceases = !true;
		if (showOvernightAndDuringdayInceases) {

			double aa = getOldestStock(aStocks).close;
			List<Double> amplified = new ArrayList<Double>();
			Stock pred = null;
			for (Stock stock : aStocks) {
				if (pred == null) {
					amplified.add(aa);
					continue;
				}
				// double a = (stock.close - stock.open) * stock.volume;
				// double a = (stock.close - stock.open);
				long vol = stock.getVolume();
				// if (stock.date.getDay() == pred.date.getDay()) {
				// vol = stock.getVolume() - pred.getVolume();
				// }
				// double a = (stock.close - stock.open);
				// double a = (stock.open - pred.close) - (stock.close - stock.open);
				double a = (stock.open - pred.close);
				// double a = (stock.close - pred.close) * vol;
				aa += a;
				// aa = stock.close * vol;
				amplified.add(aa);
				pred = stock;

			}

			aa = getOldestStock(aStocks).close;
			List<Double> amplified2 = new ArrayList<Double>();
			pred = null;
			for (Stock stock : aStocks) {
				if (pred == null) {
					amplified2.add(aa);
					continue;
				}
				// double a = (stock.close - stock.open) * stock.volume;
				// double a = (stock.close - stock.open);
				long vol = stock.getVolume();
				// if (stock.date.getDay() == pred.date.getDay()) {
				// vol = stock.getVolume() - pred.getVolume();
				// }
				double a = (stock.close - stock.open);
				// double a = (stock.open - pred.close) - (stock.close - stock.open);
				// double a = (stock.open - pred.close);
				// double a = (stock.close - pred.close) * vol;
				aa += a;
				// aa = stock.close * vol;
				amplified2.add(aa);
				pred = stock;

			}

			// amplified = ema(amplified, 5);
			// amplified2 = ema(amplified2, 5);

			double minAmplified = Collections.min(amplified);
			double maxAmplified = Collections.max(amplified);

			final List<DifferenceEntry> sea = new ArrayList<DifferenceEntry>();
			int ind = 0;
			for (double f : amplified) {
				double f2 = amplified2.get(ind);
				sea.add(new DifferenceEntry(f2, f));
				// sea.add(new DifferenceEntry((f - minAmplified) / (maxAmplified - minAmplified) * (max - min) + min, 0 * (0 - minAmplified) / (maxAmplified - minAmplified) * (max - min) + min));
				ind++;
			}
			differenceDataSets.add(DifferenceDataSetImpl.create(sea.toArray(new DifferenceEntry[] {})));
		}

		boolean showNoFriday = !true;
		if (showNoFriday) {

			double aa = getOldestStock(aStocks).close;
			List<Double> amplified = new ArrayList<Double>();
			Stock pred = null;
			for (Stock stock : aStocks) {
				if (pred == null) {
					amplified.add(aa);
					continue;
				}
				// double a = (stock.close - stock.open) * stock.volume;
				// double a = (stock.close - stock.open);
				long vol = stock.getVolume();
				// if (stock.date.getDay() == pred.date.getDay()) {
				// vol = stock.getVolume() - pred.getVolume();
				// }
				// double a = (stock.close - stock.open);
				// double a = (stock.open - pred.close) * stock.volume;
				double a = (stock.close - pred.close);
				if (stock.date.getDay() == 5)
					a = 0;
				aa += a;
				// aa = stock.close * vol;
				amplified.add((double) aa);
				pred = stock;
			}

			double minAmplified = Collections.min(amplified);
			double maxAmplified = Collections.max(amplified);

			final List<DifferenceEntry> sea = new ArrayList<DifferenceEntry>();
			for (double f : amplified) {
				sea.add(new DifferenceEntry((f - minAmplified) / (maxAmplified - minAmplified) * (max - min) + min, 0 * (0 - minAmplified) / (maxAmplified - minAmplified) * (max - min) + min));
			}
			differenceDataSets.add(DifferenceDataSetImpl.create(sea.toArray(new DifferenceEntry[] {})));
		}

		if (showOrders && maxSellers != Double.MIN_VALUE) {
			final List<DifferenceEntry> sea = new ArrayList<DifferenceEntry>();
			final List<DifferenceEntry> seaPeople = new ArrayList<DifferenceEntry>();
			int i = 0;
			for (Stock stock : aStocks) {
				double max2 = Math.max(maxSellers, maxBuyers);
				double adj = 1;
				adj = max2 / Math.max(stock.getSellers(), stock.getBuyers());
				sea.add(new DifferenceEntry((stock.getSellers() * adj) / max2 * (max - min) + min, (stock.getBuyers() * adj) / max2 * (max - min) + min));
				// sea.add(new DifferenceEntry((stock.getSellers() - minDemand) / (maxDemand - minDemand) * (max - min) + min, (stock.getBuyers() - minDemand) / (maxDemand - minDemand) * (max - min) + min));
				// seaPeople.add(new DifferenceEntry((maxDemandPeople - stock.getSellersPeople()) / (maxDemandPeople - minDemandPeople) * (max - min) + min, (stock.getBuyersPeople() - minDemandPeople) / (maxDemandPeople - minDemandPeople) * (max - min) + min));

				// seaPeople.add(new DifferenceEntry((sells.get(i) - minDemandPeople) / (maxDemandPeople - minDemandPeople) * (max - min) + min, (buys.get(i) - minDemandPeople) / (maxDemandPeople - minDemandPeople) * (max - min) + min));
				i++;
			}
			differenceDataSets.add(DifferenceDataSetImpl.create(sea.toArray(new DifferenceEntry[] {})));
			// differenceDataSets.add(DifferenceDataSetImpl.create(seaPeople.toArray(new DifferenceEntry[] {})));
		}

		totalProfit = 0;
		List<Double> closes = allstocks.stream().map(x -> x.close).collect(Collectors.toList());

		String addMessage = "";
		if (showMACD) {
			final List<DifferenceEntry> sea = new ArrayList<DifferenceEntry>();

			int ema12 = 12;
			int ema26 = 26;
			List<Double> period12 = ema(closes, ema12);
			List<Double> period26 = ema(closes, ema26);
			List<Double> macdList = subtract(period12, period26);
			List<Double> signalList = ema(macdList, 9);
			double minMACD = Double.MAX_VALUE;
			double maxMACD = Double.MIN_VALUE;
			double maxHistogram = Double.MIN_VALUE;
			// for (double macd : macdList) {
			for (Stock stock : aStocks) {
				int ind = indexes.get(stock);
				double macd = macdList.get(ind);
				double signal = signalList.get(ind);
				minMACD = Math.min(macd, minMACD);
				maxMACD = Math.max(macd, maxMACD);
				maxHistogram = Math.max(Math.abs(macd - signal), maxHistogram);
			}
			double minHistogram = -maxHistogram;
			Double boughtAt = null;
			int count = 0;
			List<DifferenceEntry> profit = new ArrayList<DifferenceEntry>();
			boolean showHistogram = false;
			for (Stock stock : aStocks) {
				int ind = indexes.get(stock);
				double macd = macdList.get(ind);
				double signal = signalList.get(ind);
				// boolean useDetailedNormalizedVolumns = showNormalized;
				// if (useDetailedNormalizedVolumns) {
				// List<Stock> detailed_stocks = allstocks.subList(Math.max(0, ind - 250 - ema26), ind + 1);
				// detailed_stocks = normalizeVolumes(detailed_stocks);
				// List<Double> detailed_closes = detailed_stocks.stream().map(x -> (double) x.close).collect(Collectors.toList());
				// List<Double> detailed_period12 = ema(detailed_closes, ema12);
				// List<Double> detailed_period26 = ema(detailed_closes, ema26);
				// List<Double> detailed_macdList = subtract(detailed_period12, detailed_period26);
				// List<Double> detailed_signalList = ema(detailed_macdList, 9);
				// macd = detailed_macdList.get(detailed_macdList.size() - 1);
				// signal = detailed_signalList.get(detailed_signalList.size() - 1);
				// }

				double histogram = macd - signal;

				// a buy signal occurs when the MACD rises above its signal line
				boolean indicates = macd > signal;

				// It is also popular to buy/sell when the MACD goes above/below zero
				if (!showHistogram)
					indicates = macd > 0;

				boolean exitNow = !indicates;

				if (showHistogram) {
					sea.add(new DifferenceEntry((histogram - minHistogram) / (maxHistogram - minHistogram) * (max - min) + min, (0 - minHistogram) / (maxHistogram - minHistogram) * (max - min) + min));
					// sea.add(new DifferenceEntry((signal - minMACD) / (maxMACD - minMACD) * (max - min) + min, (macd - minMACD) / (maxMACD - minMACD) * (max - min) + min));
				} else {
					// sea.add(new DifferenceEntry((signal - minMACD) / (maxMACD - minMACD) * (max - min) + min, (macd - minMACD) / (maxMACD - minMACD) * (max - min) + min));
					sea.add(new DifferenceEntry(period12.get(ind), period26.get(ind)));
				}

				boughtAt = virtualTrade(differenceDataSets, boughtAt, count, profit, stock, indicates, exitNow);
				count++;
			}

			// double bestLocalTotalProfit = -100000000;
			// int best_i12 = 0;
			// int best_i26 = 0;
			// int best_emaadd = 0;
			// for (int emaadd = 0; emaadd < 2; emaadd += 1) {
			// for (int i12 = 1; i12 < 3000; i12 += 50) {
			// period12 = ema(closes, i12, emaadd);
			// for (int i26 = i12 + 1; i26 < 4000; i26 += 50) {
			// period26 = ema(closes, i26, emaadd);
			// macdList = subtract(period12, period26);
			// signalList = ema(macdList, 9, emaadd);
			// double localTotalProfit = 0;
			// boughtAt = null;
			// for (Stock stock : aStocks) {
			// int ind = indexes.get(stock);
			// double macd = macdList.get(ind);
			// double signal = signalList.get(ind);
			//
			// double histogram = macd - signal;
			//
			// // a buy signal occurs when the MACD rises above its signal line
			// boolean indicates = macd > signal;
			//
			// // It is also popular to buy/sell when the MACD goes above/below zero
			// if (!showHistogram)
			// indicates = macd > 0;
			//
			// boolean exitNow = !indicates;
			//
			// if (boughtAt == null && indicates) {
			// boughtAt = stock.close;
			// }
			// if (boughtAt != null && (exitNow || stock == aStocks.getLatestStock())) {
			// localTotalProfit += (stock.close - boughtAt) * (10000 / boughtAt);
			// boughtAt = null;
			// }
			// }
			// if (localTotalProfit > bestLocalTotalProfit) {
			// bestLocalTotalProfit = localTotalProfit;
			// best_i12 = i12;
			// best_i26 = i26;
			// best_emaadd = emaadd;
			// }
			// }
			// }
			// }
			// if (bestLocalTotalProfit != Double.MIN_VALUE) {
			// addMessage = " ema:" + best_emaadd + " i12:" + best_i12 + " i26:" + best_i26 + " $" + Math.round(bestLocalTotalProfit);
			// }

			differenceDataSets.add(0, DifferenceDataSetImpl.create(sea.toArray(new DifferenceEntry[] {})));
		}

		if (showRSI) {
			final List<DifferenceEntry> sea = new ArrayList<DifferenceEntry>();
			List<Double> gains = avgGainOrLoss(closes, 14, true);
			List<Double> losses = avgGainOrLoss(closes, 14, false);
			List<Double> rsiList = new ArrayList<Double>();

			int i = 0;
			for (double gain : gains) {
				double loss = losses.get(i);
				double rsi = 100;
				if (loss != 0) {
					double rs = gain / loss;
					rsi = 100 - 100 / (1 + rs);
				}
				rsiList.add(rsi);
				i++;
			}
			double minRSI = 0;
			double maxRSI = 100;

			Double boughtAt = null;
			List<DifferenceEntry> profit = new ArrayList<DifferenceEntry>();
			int count = 0;
			for (Stock stock : aStocks) {
				int ind = indexes.get(stock);
				double rsi = rsiList.get(ind);

				sea.add(new DifferenceEntry((rsi - minRSI) / (maxRSI - minRSI) * (max - min) + min, (50 - minRSI) / (maxRSI - minRSI) * (max - min) + min));

				boolean indicates = rsi < 30;
				boolean exitNow = rsi > 70;
				// boughtAt = virtualTrade(differenceDataSets, boughtAt, count, profit, stock, indicates, exitNow);
				count++;
			}
			differenceDataSets.add(0, DifferenceDataSetImpl.create(sea.toArray(new DifferenceEntry[] {})));
		}
		if (showFastSTO) {
			final List<DifferenceEntry> sea = new ArrayList<DifferenceEntry>();
			List<Double> kList = collectHighsOrLows(allstocks, 14);
			List<Double> dList = sma(kList, 3);

			double minK = 0;
			double maxK = 1;

			for (Stock stock : aStocks) {
				int ind = indexes.get(stock);
				double k = kList.get(ind);
				double d = dList.get(ind);
				sea.add(new DifferenceEntry((k - minK) / (maxK - minK) / 6 * (max - min) + min, (d - minK) / (maxK - minK) / 6 * (max - min) + min));
			}
			differenceDataSets.add(0, DifferenceDataSetImpl.create(sea.toArray(new DifferenceEntry[] {})));
		}

		if (showDepth) {

			// for (double tight : new double[] { 0.05, 0.1, 0.2, 0.3, 0.5, 0.6, 1 }) {
			for (double tight : new double[] { 0.05, 0.4, 1.0 }) {
				final List<DifferenceEntry> sea = new ArrayList<DifferenceEntry>();
				int i = 0;
				for (Stock stock : aStocks) {
					int minVolumne = (int) Math.round(Math.min(stock.getSelVolumnes(), stock.getBuyVolumnes()) * tight);
					if (minVolumne == 0) {
						break;
					}
					// double minPrice = stock.getBuyPriceForVolumne(minVolumne);
					// double maxPrice = stock.getSellPriceForVolumne(minVolumne);
					double minPrice = stock.getBuyPriceAtVolumne(minVolumne);
					double maxPrice = stock.getSellPriceAtVolumne(minVolumne);
					sea.add(new DifferenceEntry(minPrice, maxPrice));
					// sea.add(new DifferenceEntry((stock.getSellers() - minDemand) / (maxDemand - minDemand) * (max - min) + min, (stock.getBuyers() - minDemand) / (maxDemand - minDemand) * (max - min) + min));
					// seaPeople.add(new DifferenceEntry((maxDemandPeople - stock.getSellersPeople()) / (maxDemandPeople - minDemandPeople) * (max - min) + min, (stock.getBuyersPeople() - minDemandPeople) / (maxDemandPeople - minDemandPeople) * (max - min) + min));

					i++;
				}
				// differenceDataSets.add(DifferenceDataSetImpl.create(sea.toArray(new DifferenceEntry[] {})));
				if (!sea.isEmpty())
					differenceDataSets.add(DifferenceDataSetImpl.create(sea.toArray(new DifferenceEntry[] {})));
			}
		}

		if (showProfit) {
			for (Crazy crazy : craziest) {
				if (crazy.getSymbol().equals(original.getSymbol())) {
					Stock addedDiff = null;
					final List<DifferenceEntry> profit = new ArrayList<DifferenceEntry>();
					for (Stock stock : aStocks) {

						if (crazy.within(stock)) {
							addedDiff = stock;
							if (stock.date.equals(crazy.getTo()) && crazy.getSell() != null)
								profit.add(new DifferenceEntry(crazy.getSell(), crazy.getBuy()));
							else
								profit.add(new DifferenceEntry(stock.close, crazy.getBuy()));
						} else {
							profit.add(new DifferenceEntry(Double.NaN, Double.NaN));
						}

					}
					if (addedDiff != null) {
						double aProfit = (addedDiff.close - crazy.getBuy()) * (10000 / crazy.getBuy());
						totalProfit += aProfit - 50;
						differenceDataSets.add(DifferenceDataSetImpl.create(profit.toArray(new DifferenceEntry[] {})));
					}
				}
			}
		}

		// X-Series
		Series seBase = SeriesImpl.create();
		seBase.setDataSet(dsDateValues);

		SeriesDefinition sdX = SeriesDefinitionImpl.create();
		sdX.getSeriesPalette().shift(-1);
		xAxisPrimary.getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seBase);

		List<DifferenceSeries> as3s = new ArrayList<DifferenceSeries>();
		for (DifferenceDataSet ds : differenceDataSets) {
			DifferenceSeries as3 = (DifferenceSeries) DifferenceSeriesImpl.create();
			// as3.setSeriesIdentifier("Series x" + differenceDataSets.indexOf(ds));
			as3.setDataSet(ds);
			as3.setTranslucent(true);
			as3.setPaletteLineColor(false);
			as3.getLineAttributes().setColor(ColorDefinitionImpl.RED());
			// as3.getLineAttributes().setStyle(LineStyle.DASHED_LITERAL);
			as3.getLineAttributes().setThickness(1);

			as3.getNegativeLineAttributes().setColor(ColorDefinitionImpl.BLACK());
			// as3.getNegativeLineAttributes().setStyle(LineStyle.DASHED_LITERAL);
			// if (differenceDataSets.get(differenceDataSets.size() - 1) == ds) {
			// as3.getNegativeLineAttributes().setStyle(LineStyle.DOTTED_LITERAL);
			// as3.getLineAttributes().setStyle(LineStyle.DOTTED_LITERAL);
			// }
			as3.getNegativeLineAttributes().setThickness(1);
			as3.getNegativeLineAttributes().setVisible(true);
			as3.setCurve(!true);
			as3s.add(as3);
		}

		// Y-Series
		StockSeries ss = (StockSeries) StockSeriesImpl.create();
		ss.setSeriesIdentifier("Stock Price");//$NON-NLS-1$
		ss.getLineAttributes().setColor(ColorDefinitionImpl.BLUE());
		ss.setDataSet(dsStockValues);

		if (showFitting) {
			CurveFitting fitting = CurveFittingImpl.create();
			ss.setCurveFitting(fitting);
		}

		NumberFormatSpecifier fs = NumberFormatSpecifierImpl.create();
		yAxisPrimary.setFormatSpecifier(fs);

		if (showVolumn) {
			AreaSeries as2 = (AreaSeries) AreaSeriesImpl.create();
			as2.setSeriesIdentifier("Series 2");
			as2.setDataSet(orthoValuesDataSet2);
			as2.setTranslucent(true);

			SeriesDefinition sdY = SeriesDefinitionImpl.create();
			yAxisPrimary.getSeriesDefinitions().add(sdY);
			sdY.getSeries().add(as2);
		}

		for (DifferenceSeries as3 : as3s) {
			SeriesDefinition sdY2 = SeriesDefinitionImpl.create();
			yAxisPrimary.getSeriesDefinitions().add(sdY2);
			sdY2.getSeries().add(as3);
		}

		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		sdY.getSeriesPalette().update(ColorDefinitionImpl.CYAN());
		yAxisPrimary.getSeriesDefinitions().add(sdY);
		sdY.getSeries().add(ss);

		if (totalProfit != 0) {
			cwaStock.getTitle().getLabel().getCaption().setValue(cwaStock.getTitle().getLabel().getCaption().getValue() + " Profit: " + Math.round(totalProfit) + "$" + addMessage);
		}

		yAxisPrimary.getScale().setMax(NumberDataElementImpl.create(aStocksMinMax.stream().map(xx -> xx.high).max(Double::compare).orElse(0.0)));
		yAxisPrimary.getScale().setMin(NumberDataElementImpl.create(aStocksMinMax.stream().map(xx -> xx.low).min(Double::compare).orElse(0.0)));

		return cwaStock;
	}

	private List<Double> subtract(List<Double> period12, List<Double> period26) {
		List<Double> macdList = new ArrayList<Double>();

		int i = 0;
		for (double v12 : period12) {
			double v26 = period26.get(i);
			double macd = (v12 - v26);
			boolean usePPO = true;
			if (usePPO) {
				macd /= v26;
			}
			macdList.add(macd);
			i++;
		}
		return macdList;
	}

	private Map<Stock, Integer> getIndexes() {
		Map<Stock, Integer> indexes = new HashMap<Stock, Integer>();
		int i2 = 0;
		for (Stock stock : allstocks) {
			indexes.put(stock, i2);
			i2++;
		}
		return indexes;
	}

	static private List<Stock> normalizeVolumes(List<Stock> allstocks) {
		List<Stock> result = new ArrayList<Stock>();
		if (allstocks.isEmpty()) {
			return result;
		}

		double totalVolumn = allstocks.stream().map(x -> x.volume).mapToLong(Long::longValue).sum();
		double averageVolumn = totalVolumn / allstocks.size();
		int ind = 0;
		double residualDayVolumn = averageVolumn;
		Stock stock = allstocks.get(ind);
		double residualStockVolumn = stock.volume;
		Stock close = new Stock(null);
		result.add(close);
		while (totalVolumn > 0.01) {
			if (residualDayVolumn >= residualStockVolumn) {
				close.add(stock, residualStockVolumn / averageVolumn);
				residualDayVolumn -= residualStockVolumn;
				totalVolumn -= residualStockVolumn;
				if (totalVolumn < 0.01) {
					break;
				}

				ind++;
				// if (ind == allstocks.size()) {
				// if (result.size() != allstocks.size()) {
				// throw new RuntimeException("Volumn normalization error: Need " + (allstocks.size() - result.size()) + " more closes!");
				// }
				// return result;
				// }
				stock = allstocks.get(ind);
				residualStockVolumn = stock.volume;
			} else {
				close.add(stock, residualDayVolumn / averageVolumn);
				totalVolumn -= residualDayVolumn;
				if (totalVolumn < 0.01) {
					break;
				}
				close = new Stock(null);
				result.add(close);
				residualStockVolumn -= residualDayVolumn;
				residualDayVolumn = averageVolumn;
			}
		}

		if (result.size() != allstocks.size()) {
			throw new RuntimeException("Volumn normalization error: Need " + (allstocks.size() - result.size()) + " more closes!");
		}
		return result;
	}

	private Double virtualTrade(List<DifferenceDataSet> differenceDataSets, Double boughtAt, int count, List<DifferenceEntry> profit, Stock stock, boolean indicates, boolean exitNow) {
		if (boughtAt == null && indicates) {
			// boughtAt = (1 * stock.open + 9 * stock.close) / 10;
			boughtAt = stock.close;
			// boughtAt = (stock.open + stock.close) / 2;
			profit.clear();
			for (int index = 1; index <= count; index++) {
				profit.add(new DifferenceEntry(Double.NaN, Double.NaN));
			}
		}
		if (boughtAt != null) {
			profit.add(new DifferenceEntry(stock.close, boughtAt));
		}
		if (boughtAt != null && (exitNow || stock == getLatestStock(aStocks))) {
			for (int index = 1; index <= aStocks.size() - (count + 1); index++) {
				profit.add(new DifferenceEntry(Double.NaN, Double.NaN));
			}
			if (stock.close < boughtAt || true) {
				differenceDataSets.add(DifferenceDataSetImpl.create(profit.toArray(new DifferenceEntry[] {})));
				totalProfit += (stock.close - boughtAt) * (10000 / boughtAt) - 50;
			}
			boughtAt = null;
		}
		return boughtAt;
	}

	public static List<Double> ema(List<Double> closes, int timePeriod) {
		return ema(closes, timePeriod, 1.0);
	}

	private static List<Double> ema(List<Double> closes, int timePeriod, double f) {
		double multiplier = (2.0 / (timePeriod + 1));
		List<Double> results = new ArrayList<Double>();
		double result = closes.get(0);
		for (double current : closes) {

			// result = (current - result) * multiplier + result;

			// equivalent after resolving "timePeriod" (note that "current" is weighted twice):
			// result = (current * 2.0 + result * (timePeriod - 1)) / (timePeriod + 1);

			result = (current * (1 + f) + result * (timePeriod - f)) / (timePeriod + 1);

			results.add(result);
		}
		return results;
	}

	private List<Double> sma(List<Double> closes, int timePeriod) {
		List<Double> results = new ArrayList<Double>();
		Queue<Double> qLow = new ArrayBlockingQueue<Double>(timePeriod);
		for (double close : closes) {
			if (qLow.size() >= timePeriod) {
				qLow.poll();
			}
			qLow.add(close);
			double result = qLow.stream().mapToDouble(Double::doubleValue).sum() / qLow.size();
			results.add(result);
		}
		return results;
	}

	private List<Double> avgGainOrLoss(List<Double> closes, int timePeriod, boolean processGains) {
		List<Double> results = new ArrayList<Double>();
		double result = 0;
		double lastClose = closes.get(0);
		for (double close : closes) {
			double current = close - lastClose;
			if (processGains ? current < 0 : current > 0) {
				current = 0;
			}
			current = Math.abs(current);
			result = (current * 1.0 + result * (timePeriod - 1)) / timePeriod;
			results.add(result);
			lastClose = close;
		}
		return results;
	}

	private List<Double> collectHighsOrLows(List<Stock> stocks, int timePeriod) {
		List<Double> results = new ArrayList<Double>();
		Queue<Double> qLow = new ArrayBlockingQueue<Double>(timePeriod);
		Queue<Double> qHigh = new ArrayBlockingQueue<Double>(timePeriod);
		for (Stock stock : stocks) {
			if (qLow.size() >= timePeriod) {
				qLow.poll();
			}
			qLow.add((double) stock.low);
			if (qHigh.size() >= timePeriod) {
				qHigh.poll();
			}
			qHigh.add((double) stock.high);
			double low = Collections.min(qLow);
			double high = Collections.max(qHigh);
			double result = high - low == 0 ? 0 : (stock.close - low) / (high - low);
			results.add(result);
		}
		return results;
	}

}
