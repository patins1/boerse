/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.average;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.kiegeland.boerse.Manager;
import com.kiegeland.boerse.conditions.Condition;
import com.kiegeland.boerse.domain.Markt;
import com.kiegeland.boerse.domain.Stock;
import com.kiegeland.boerse.domain.Stocks;

public class AverageCanvas extends Canvas {

	/**
	 * The image which caches the chart image to improve drawing performance.
	 */
	private Image cachedImage = null;

	private AverageConfig averageConfig;
	float[][] chart = null;
	private boolean fillOutGaps = true;
	private boolean average = false && fillOutGaps;
	private boolean cancelExtremes = true;
	private static final boolean blur = false;

	private static final float MIN_VALUE = -10000;

	/**
	 * Constructs one canvas containing chart.
	 * 
	 * @param parent
	 *            a composite control which will be the parent of the new instance (cannot be null)
	 * @param style
	 *            the style of control to construct
	 */
	public AverageCanvas(Composite parent, int style) {
		super(parent, style);

		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {

				Composite co = (Composite) e.getSource();
				final Rectangle rect = co.getClientArea();

				if (cachedImage == null) {
					buildChart();
					drawToCachedImage(rect);
				}
				e.gc.drawImage(cachedImage, 0, 0, cachedImage.getBounds().width, cachedImage.getBounds().height, 0, 0, rect.width, rect.height);

			}
		});

		addControlListener(new ControlAdapter() {

			public void controlResized(ControlEvent e) {

				// buildChart();
				cachedImage = null;
			}
		});
	}

	/**
	 * Builds the chart state. This method should be call when data is changed.
	 */
	private void buildChart() {

		if (chart != null && chart.length == averageConfig.getLookRange() && chart[0].length == getSize().y) {
			return;
		}

		System.out.println("Calc chart..");

		Point size = new Point(averageConfig.getLookRange(), getSize().y);
		chart = new float[size.x][size.y];
		int[][] chartCount = new int[size.x][size.y];

		float factorY = (size.y - 1) / (averageConfig.above - averageConfig.below);
		int middley = (int) ((1 - averageConfig.below) * factorY);
		int middlex = averageConfig.lookBack;
		int totalGraphis = 0;
		for (Markt markt : Manager.getEnabledMaerkte())
			for (Stocks aStock : markt.getStocks()) {
				for (Stock buy : aStock.getStocks()) {
					Stock sell = buy.getStock(this.averageConfig.sellAfter);
					Stock beginAt = buy.getStock(-this.averageConfig.lookBack);
					Stock stopAt = buy.getStock(this.averageConfig.lookAhead);
					if (sell == null || beginAt == null)
						continue;
					if (Manager.invalidDate(buy))
						continue;
					if (stopAt == null && !Manager.UseDate)
						continue;
					double zinsen = Condition.getGain(buy);
					if (zinsen < 0) {
						zinsen = Condition.getGain(buy);
						continue;
					}
					if (zinsen == 0)
						continue;
					// double zinsen = buy.getZinsen(sell);
					// if (Double.isNaN(zinsen))
					// continue;

					float weight = (float) (Math.pow(zinsen, this.averageConfig.macht) - 1);
					totalGraphis++;
					int stockX = 0;
					if (stopAt != null)
						stopAt = stopAt.succ;
					for (Stock stock = beginAt; stock != stopAt; stock = stock.succ) {
						float absZinsen = stock.close / buy.close;
						if (absZinsen >= averageConfig.below && absZinsen <= averageConfig.above) {
							float fy = ((absZinsen - averageConfig.below) * factorY);
							int y = (int) fy;
							chart[stockX][y] += weight;
							chartCount[stockX][y]++;
						}
						stockX++;
					}
				}
			}

		if (fillOutGaps)
			for (int x = 0; x < size.x; x++) {
				float[] chartx = chart[x];
				int[] chartxCount = chartCount[x];
				float z = 0;
				for (int y = 0; y < size.y; y++) {
					if (chartxCount[y] != 0) {
						z = chartx[y];
						if (average)
							z /= chartxCount[y];
					}
					chartx[y] = z;
				}
			}

		{
			for (int x = 0; x < size.x; x++) {
				chart[x][middley] = 0;
			}
			for (int y = 0; y < size.y; y++) {
				chart[middlex][y] = 0;
			}
			chart[middlex][middley] = 1;
		}

		for (int x = 0; x < size.x; x++) {
			float[] chartx = chart[x];
			float max = MIN_VALUE;
			float min = Float.MAX_VALUE;
			for (int y = 0; y < size.y; y++) {
				float b = chartx[y];
				max = Math.max(max, b);
				min = Math.min(min, b);
			}
			if (max != MIN_VALUE && min != Float.MAX_VALUE) {
				float rel = (max - min);
				if (rel == 0) {
					for (int y = 0; y < size.y; y++) {
						chartx[y] = 0;
					}
				} else {
					for (int y = 0; y < size.y; y++) {
						float b = chartx[y];
						chartx[y] = (b - min) / rel;
					}

					if (cancelExtremes) {
						max = Float.MIN_VALUE;
						for (int y = 0; y < size.y; y++) {
							float b = chartx[y];
							if (b <= 0.9) {
								max = Math.max(max, b);
							}
						}
						if (max <= 0.5) {
							min = 0;
							rel = (max - min);
							for (int y = 0; y < size.y; y++) {
								float b = chartx[y];
								if (b > max)
									chartx[y] = 1;
								else
									chartx[y] = (b - min) / rel;
							}
						}

						min = Float.MAX_VALUE;
						for (int y = 0; y < size.y; y++) {
							float b = chartx[y];
							if (b >= 0.1) {
								min = Math.min(min, b);
							}
						}
						if (min >= 0.5) {
							max = 1;
							rel = (max - min);
							for (int y = 0; y < size.y; y++) {
								float b = chartx[y];
								if (b < min)
									chartx[y] = 0;
								else
									chartx[y] = (b - min) / rel;
							}
						}
					}

					if (blur) {
						for (int y = 0; y < size.y / 2; y++) {
							float b = (chartx[y * 2] + chartx[y * 2 + 1]) / 2;
							chartx[y * 2] = b;
							chartx[y * 2 + 1] = b;
						}
						for (int y = 0; y < size.y / 2 - 1; y++) {
							float b = (chartx[y * 2 + 1] + chartx[y * 2 + 2]) / 2;
							chartx[y * 2 + 1] = b;
							chartx[y * 2 + 2] = b;
						}
					}
				}
			}
		}

		System.out.println("Calc chart finished (total graphs=" + totalGraphis + ")");

		// float max = Float.MIN_VALUE;
		// float min = Float.MAX_VALUE;
		// for (int x = 0; x < size.x; x++) {
		// for (int y = 0; y < size.y; y++) {
		// float b = chart[x][y];
		// max = Math.max(max, b);
		// min = Math.min(min, b);
		// }
		// }
		//
		// float rel = (max - min);
		// for (int x = 0; x < size.x; x++) {
		// for (int y = 0; y < size.y; y++) {
		// float b = chart[x][y];
		// chart[x][y] = (b - min) / rel;
		// }
		// }

		// min = min;

	}

	/**
	 * Draws the chart onto the cached image in the area of the given <code>Rectangle</code>.
	 * 
	 * @param size
	 *            the area to draw
	 */
	public void drawToCachedImage(Rectangle size) {
		GC gc = null;
		try {
			if (cachedImage != null)
				cachedImage.dispose();
			cachedImage = new Image(Display.getCurrent(), size.width, size.height);

			gc = new GC(cachedImage);

			gc.setForeground(new Color(null, 255, 255, 255));
			gc.fillRectangle(size);

			System.out.println("Paint chart..");

			float factorX = averageConfig.getLookRange() / (float) size.width;
			for (int x = 0; x < size.width; x++) {
				int stockX = (int) (x * factorX);
				for (int y = 0; y < size.height; y++) {

					int i = 255 - (int) (chart[stockX][y] * 255);
					// if (stockX <= averageConfig.lookBack)
					// gc.setForeground(new Color(null, i, i, i));
					// else
					gc.setForeground(new Color(null, i, i, i));

					gc.drawPoint(x, size.height - 1 - y);
				}
			}
			System.out.println("Paint chart finished");

		} finally {
			if (gc != null)
				gc.dispose();
		}
	}

	/**
	 * Sets the chart into this canvas. Note: When the chart is set, the cached image will be dopped, but this method doesn't reset the flag <code>cachedImage</code>.
	 * 
	 * @param chart
	 *            the chart to set
	 */
	public void setChart(AverageConfig averageConfig) {
		if (cachedImage != null)
			cachedImage.dispose();

		cachedImage = null;
		this.averageConfig = averageConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose() {
		if (cachedImage != null)
			cachedImage.dispose();
		super.dispose();
	}

}
