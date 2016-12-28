/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.chart;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class ChartCanvas extends Canvas {

	protected Chart chart = null;

	Control chartControl;

	public ChartCanvas(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);

		setLayout(new FillLayout());
		boolean USE_BROWSER = false;
		if (USE_BROWSER) {
			chartControl = new Browser(this, SWT.NONE);
		} else {
			chartControl = new Label(this, SWT.NONE);
		}

		addControlListener(new SmartControlAdapter() {

			protected void handleControlResized(ControlEvent event) {
				updateChart();
			}

		});

	}

	public void setChart(Chart chart) {
		this.chart = chart;
	}

	public void updateChart() {
		try {
			if (chart != null)
				drawChart(chart);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void drawChart(Chart chart) throws Exception {
		Point size = getSize();

		IDeviceRenderer render = null;
		PluginSettings ps = PluginSettings.instance();
		render = ps.getDevice("dv.BMP");
		Bounds bounds = BoundsImpl.create(0, 0, size.x, size.y);
		int resolution = render.getDisplayServer().getDpiResolution();
		bounds.scale(72d / resolution);
		GeneratedChartState state;
		Generator gr = Generator.instance();
		state = gr.build(render.getDisplayServer(), chart, bounds, null, null, null);
		File tmpFile = File.createTempFile("birtchart", "");
		render.setProperty(IDeviceRenderer.FILE_IDENTIFIER, tmpFile);
		gr.render(render, state);

		Image img = new Image(getDisplay(), tmpFile.getAbsolutePath());

		if (chartControl instanceof Browser) {
			Browser browser = (Browser) chartControl;
			String url;
			try {
				url = getURL(img);
			} catch (Throwable t) {
				url = "file://" + tmpFile.getCanonicalPath();
				tmpFile = null;
			}
			browser.setText("<style>* {padding:0; margin:0; border:0; }</style><img src='" + url + "' onmousewheel='javascript:syncZoom(event.wheelDelta, event.x);' width='" + size.x + "' height='" + size.y + "'/>");

		} else {
			Label label = (Label) chartControl;
			label.setImage(img);
		}

		if (tmpFile != null) {
			tmpFile.delete();
		}

	}

	public static String getURL(Image img) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		// use reflective methods - as it could compile only with the RAP target
		Class<?> clazz = Class.forName("org.eclipse.swt.internal.graphics.ImageFactory");
		Method m = clazz.getMethod("getImagePath");
		return "../../" + m.invoke(null, img);
		// return "../../" + org.eclipse.swt.internal.graphics.ImageFactory.getImagePath(img);
		// return "../" + img.internalImage.getResourceName();
	}

}
