package com.kiegeland.boerse.chart;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.CurveFittingImpl;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.DateTimeDataSet;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.StockDataSet;
import org.eclipse.birt.chart.model.data.impl.DateTimeDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.StockDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.layout.TitleBlock;
import org.eclipse.birt.chart.model.type.AreaSeries;
import org.eclipse.birt.chart.model.type.StockSeries;
import org.eclipse.birt.chart.model.type.impl.AreaSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.StockSeriesImpl;
import org.eclipse.birt.chart.util.CDateTime;

import com.kiegeland.boerse.domain.Stock;
import com.kiegeland.boerse.domain.Stocks;

public class BoersenChart {

	private Stocks aStocks;
	private Stock baseStock;
	private Stocks original;

	public static int range = 32;

	public BoersenChart(Stocks aStocks, Stock baseStock) {
		original = aStocks;
		this.baseStock = baseStock;
		calcCutout();
	}

	public void calcCutout() {
		aStocks = original;
		int index = original.indexOf(this.baseStock);
		int toIndex = Math.min(index + range / 2, original.size());
		int fromIndex = Math.max(toIndex - range, 0);
		List<Stock> subList = original.subList(fromIndex, toIndex);
		while (subList.size() >= 128)
			subList = halve(subList);
		this.aStocks = new Stocks(original, subList);
	}

	private List<Stock> halve(List<Stock> subList) {
		List<Stock> result = new ArrayList<Stock>();
		int index = 0;
		for (Stock stock : subList) {
			if ((subList.size() - 1 + index) / 2 * 2 == subList.size() - 1 + index)
				result.add(stock);
			index++;
		}
		return result;
	}

	public final Chart createCFStockChart() {

		ChartWithAxes cwaStock = ChartWithAxesImpl.create();

		// Title
		cwaStock.getTitle().getLabel().getCaption().setValue(baseStock != null ? baseStock.toString() : aStocks.getSymbol());//$NON-NLS-1$
		TitleBlock tb = cwaStock.getTitle();
		tb.setBackground(GradientImpl.create(ColorDefinitionImpl.create(0, 128, 0), ColorDefinitionImpl.create(128, 0, 0), 0, false));
		tb.getLabel().getCaption().setColor(ColorDefinitionImpl.WHITE());

		// Plot
		cwaStock.getBlock().setBackground(GradientImpl.create(ColorDefinitionImpl.create(196, 196, 196), ColorDefinitionImpl.WHITE(), 90, false));
		cwaStock.getPlot().getClientArea().getInsets().set(10, 10, 10, 10);

		// Legend
		cwaStock.getLegend().setBackground(ColorDefinitionImpl.ORANGE());
		cwaStock.getLegend().setVisible(false);

		// X-Axis
		Axis xAxisPrimary = ((ChartWithAxesImpl) cwaStock).getPrimaryBaseAxes()[0];
		xAxisPrimary.setFormatSpecifier(JavaDateFormatSpecifierImpl.create("yyyy-MM-dd"));//$NON-NLS-1$		

		xAxisPrimary.getTitle().getCaption().setValue("X Axis");//$NON-NLS-1$
		xAxisPrimary.getTitle().getCaption().setColor(ColorDefinitionImpl.RED());
		xAxisPrimary.getTitle().getCaption().setValue("Date");//$NON-NLS-1$
		xAxisPrimary.setTitlePosition(Position.ABOVE_LITERAL);

		xAxisPrimary.getLabel().getCaption().setColor(ColorDefinitionImpl.RED());
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
		for (Stock stock : aStocks.getStocks()) {
			min = Math.min(stock.getLow(), min);
			max = Math.max(stock.getHigh(), max);
			minVolumn = Math.min(stock.volume, minVolumn);
			maxVolumn = Math.max(stock.volume, maxVolumn);
		}

		// yAxisPrimary.getScale().setMin(NumberDataElementImpl.create(min));
		// yAxisPrimary.getScale().setMax(NumberDataElementImpl.create(max));
		// yAxisPrimary.getScale().setStep((max - min) / 20);
		// yAxisPrimary.getScale().setTickBetweenCategories(true);

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
			StockEntry entry;
			// if (stock.equals(baseStock)) {
			// entry = new StockEntry( stock.getOpen(), stock.getLow(),
			// stock.getHigh(), stock.getClose()) {
			//
			// @Override
			// public String toString() {
			// return "(x)"+super.toString();
			// }
			//
			// };
			// } else
			entry = new StockEntry(stock.getOpen(), stock.getLow(), stock.getHigh(), stock.close);
			stockEntries.add(entry);
			// if (stock.equals(baseStock)) {
			// dates.add(new CDateTime(stock.getDate())
			// {
			//
			// @Override
			// public String toString() {
			// return "(x)" + super.toString();
			// }
			//
			// });
			// } else
			dates.add(new CDateTime(stock.date));
			volumns.add((stock.volume) / (maxVolumn) * (max - min) + min);
			// volumns.add((stock.volume-minVolumn)/(maxVolumn-minVolumn)*(max-min)+min);
		}

		// Data Set
		DateTimeDataSet dsDateValues = DateTimeDataSetImpl.create(dates.toArray(new CDateTime[] {}));
		StockDataSet dsStockValues = StockDataSetImpl.create(stockEntries.toArray(new StockEntry[] {}));
		NumberDataSet orthoValuesDataSet2 = NumberDataSetImpl.create(volumns.toArray(new Double[] {}));

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
		as2.getLineAttributes().setColor(ColorDefinitionImpl.PINK());

		// Y-Series
		StockSeries ss = (StockSeries) StockSeriesImpl.create();
		ss.setSeriesIdentifier("Stock Price");//$NON-NLS-1$
		ss.getLineAttributes().setColor(ColorDefinitionImpl.BLUE());
		ss.setDataSet(dsStockValues);
		ss.setCurveFitting(CurveFittingImpl.create());

		NumberFormatSpecifier fs = NumberFormatSpecifierImpl.create();
		yAxisPrimary.setFormatSpecifier(fs);

		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		sdY.getSeriesPalette().update(ColorDefinitionImpl.CYAN());
		yAxisPrimary.getSeriesDefinitions().add(sdY);
		sdY.getSeries().add(as2);
		sdY.getSeries().add(ss);

		// xAxisPrimary.getScale().setMin(DateTimeDataElementImpl.create(aStocks.getOldestStock().date.getTime()));
		// xAxisPrimary.getScale().setMax(NumberDataElementImpl.create(aStocks.getLatestStock().date.getTime()));
		//
		// MarkerLine ml = MarkerLineImpl.create(xAxisPrimary, DateTimeDataElementImpl
		// .create(baseStock.date.getTime()));
		// ml.setLineAttributes( LineAttributesImpl.create( ColorDefinitionImpl
		// .create( 17, 37, 223 ), LineStyle.SOLID_LITERAL, 1 ) );

		return cwaStock;
	}

}
