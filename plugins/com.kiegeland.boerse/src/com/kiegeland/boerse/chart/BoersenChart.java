/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.chart;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
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

	private Stocks aStocks;
	private Stock baseStock;

	public boolean intraday = false;
	public boolean showDepth = false;
	public boolean showProfit = false;
	public boolean showOrders = false;
	public boolean showVolumn = false;
	public boolean showFitting = false;

	public BoersenChart(Stocks aStocks, Stock baseStock) {
		this.aStocks = aStocks;
		this.baseStock = baseStock;
	}

	public final Chart createCFStockChart() throws ParseException {

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
		cwaStock.getTitle().getLabel().getCaption().setValue(baseStock != null ? baseStock.toString() : aStocks.getSymbol());// $NON-NLS-1$
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
		for (Stock stock : aStocks.getStocks()) {
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
		for (Stock stock : aStocks.getStocks()) {
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
		for (Stock stock : aStocks.getStocks()) {
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
		if (aStocks.getSymbol().contentEquals("BHP"))
			totalStockCount = 5323 * 1000000;
		if (aStocks.getSymbol().contentEquals("BAL"))
			totalStockCount = 96 * 1000000;
		if (aStocks.getSymbol().contentEquals("BKL"))
			totalStockCount = 17 * 1000000;

		boolean showAvgPurchaseCost = !true;
		if (showAvgPurchaseCost) {
			final List<DifferenceEntry> sea = new ArrayList<DifferenceEntry>();
			int i = 0;
			float weightedValue = aStocks.getOldestStock().getOpen();
			float summedValue = 0;
			float summedVolumne = 0;
			for (Stock stock : aStocks.getStocks()) {
				weightedValue = stock.close * stock.getVolume() / totalStockCount + weightedValue * (totalStockCount - stock.getVolume()) / totalStockCount;

				summedValue += stock.close * stock.getVolume();
				summedVolumne += stock.getVolume();
				float weighted = (summedValue / summedVolumne) * summedVolumne / totalStockCount + aStocks.getOldestStock().getOpen() * (totalStockCount - summedVolumne) / totalStockCount;

				sea.add(new DifferenceEntry(weighted, weightedValue));
			}
			differenceDataSets.add(DifferenceDataSetImpl.create(sea.toArray(new DifferenceEntry[] {})));
		}

		boolean showAmplifiedDifference = !true;
		if (showAmplifiedDifference) {

			double aa = aStocks.getOldestStock().close;
			List<Float> amplified = new ArrayList<Float>();
			for (Stock stock : aStocks.getStocks()) {
				if (stock.pred() == null) {
					amplified.add((float) aa);
					continue;
				}
				// double a = (stock.close - stock.open) * stock.volume;
				// double a = (stock.close - stock.open);
				long vol = stock.getVolume();
				// if (stock.date.getDay() == stock.pred().date.getDay()) {
				// vol = stock.getVolume() - stock.pred().getVolume();
				// }
				double a = (stock.close - stock.pred().close) * vol;
				aa += a;
				// aa = stock.close * vol;
				amplified.add((float) aa);
			}

			double minAmplified = Double.MAX_VALUE;
			double maxAmplified = Double.MIN_VALUE;
			for (float f : amplified) {
				minAmplified = Math.min(minAmplified, f);
				maxAmplified = Math.max(maxAmplified, f);
			}

			final List<DifferenceEntry> sea = new ArrayList<DifferenceEntry>();
			for (float f : amplified) {
				// sea.add(new DifferenceEntry(f, aStocks.getOldestStock().open));
				sea.add(new DifferenceEntry((f - minAmplified) / (maxAmplified - minAmplified) * (max - min) + min, 0 * (0 - minAmplified) / (maxAmplified - minAmplified) * (max - min) + min));
			}
			differenceDataSets.add(DifferenceDataSetImpl.create(sea.toArray(new DifferenceEntry[] {})));
		}

		if (showOrders && maxSellers != Double.MIN_VALUE) {
			final List<DifferenceEntry> sea = new ArrayList<DifferenceEntry>();
			final List<DifferenceEntry> seaPeople = new ArrayList<DifferenceEntry>();
			int i = 0;
			for (Stock stock : aStocks.getStocks()) {
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

		if (showDepth) {

			// for (double tight : new double[] { 0.05, 0.1, 0.2, 0.3, 0.5, 0.6, 1 }) {
			for (double tight : new double[] { 0.05, 0.4, 1.0 }) {
				final List<DifferenceEntry> sea = new ArrayList<DifferenceEntry>();
				int i = 0;
				for (Stock stock : aStocks.getStocks()) {
					int minVolumne = (int) Math.round(Math.min(stock.getSelVolumnes(), stock.getBuyVolumnes()) * tight);
					if (minVolumne == 0) {
						break;
					}
					// float minPrice = stock.getBuyPriceForVolumne(minVolumne);
					// float maxPrice = stock.getSellPriceForVolumne(minVolumne);
					float minPrice = stock.getBuyPriceAtVolumne(minVolumne);
					float maxPrice = stock.getSellPriceAtVolumne(minVolumne);
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
				if (crazy.getSymbol().equals(aStocks.getSymbol())) {
					boolean addedDiff = false;
					final List<DifferenceEntry> sea = new ArrayList<DifferenceEntry>();
					for (Stock stock : aStocks.getStocks()) {

						if (crazy.within(stock)) {
							addedDiff = true;
							if (stock.date.equals(crazy.getTo()) && crazy.getSell() != null)
								sea.add(new DifferenceEntry(crazy.getSell(), crazy.getBuy()));
							else
								sea.add(new DifferenceEntry(stock.close, crazy.getBuy()));
						} else {
							sea.add(new DifferenceEntry(Float.NaN, Float.NaN));
						}

					}
					if (addedDiff) {
						differenceDataSets.add(DifferenceDataSetImpl.create(sea.toArray(new DifferenceEntry[] {})));
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

		AreaSeries as2 = (AreaSeries) AreaSeriesImpl.create();
		as2.setSeriesIdentifier("Series 2");
		as2.setDataSet(orthoValuesDataSet2);
		as2.setTranslucent(true);

		List<DifferenceSeries> as3s = new ArrayList<DifferenceSeries>();
		for (DifferenceDataSet ds : differenceDataSets) {
			DifferenceSeries as3 = (DifferenceSeries) DifferenceSeriesImpl.create();
			as3.setSeriesIdentifier("Series x" + differenceDataSets.indexOf(ds));
			as3.setDataSet(ds);
			as3.setTranslucent(true);
			as3.setPaletteLineColor(false);
			as3.getLineAttributes().setColor(ColorDefinitionImpl.RED());
			as3.getLineAttributes().setStyle(LineStyle.DASHED_LITERAL);
			as3.getLineAttributes().setThickness(2);

			as3.getNegativeLineAttributes().setColor(ColorDefinitionImpl.BLACK());
			as3.getNegativeLineAttributes().setStyle(LineStyle.DASHED_LITERAL);
			if (differenceDataSets.get(differenceDataSets.size() - 1) == ds) {
				as3.getNegativeLineAttributes().setStyle(LineStyle.DOTTED_LITERAL);
				as3.getLineAttributes().setStyle(LineStyle.DOTTED_LITERAL);
			}
			as3.getNegativeLineAttributes().setThickness(2);
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

		return cwaStock;
	}

}
