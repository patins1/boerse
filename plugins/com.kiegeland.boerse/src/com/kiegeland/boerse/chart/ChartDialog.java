/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.chart;

import static com.kiegeland.boerse.util.Utilities.getLatestStock;
import static com.kiegeland.boerse.util.Utilities.getOldestStock;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.kiegeland.boerse.Manager;
import com.kiegeland.boerse.domain.Stock;
import com.kiegeland.boerse.domain.Stocks;
import com.kiegeland.boerse.util.Utilities;

import lpsolve.LogListener;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import lpsolve.MsgListener;

public class ChartDialog extends Dialog {

	public int range = 32;

	public int offset = 0;

	public static ChartDialog current = null;

	private ChartCanvas canvas;

	public Stocks stocks;

	private Stock stock;

	private Button intradayButton;

	private int oriX;

	private int orioffset;

	public List<Stock> istocks;

	private Button depthButton;

	private Button ordersButton;

	private Button profitButton;

	public Button notifyButton;

	private Button volumnsButton;

	private Button fittingButton;

	private Button macdButton;

	private Button rsiButton;
	private Button fastSTOButton;
	private Button normalizedButton;

	private Button relativeButton;

	private Button lpSolveButton;

	public ChartDialog() {
		super(PlatformUI.getWorkbench().getDisplay().getActiveShell());
		this.setBlockOnOpen(false);
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE | SWT.MAX | SWT.NO_BACKGROUND;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite result = (Composite) super.createDialogArea(parent);
		result.setLayout(new FillLayout());
		canvas = new ChartCanvas(result, SWT.NO_BACKGROUND);

		if (canvas.chartControl instanceof Browser) {
			Browser browser = (Browser) canvas.chartControl;
			new BrowserFunction(browser, "syncZoom") {

				public Object function(Object[] arguments) {
					Integer count = ((Number) arguments[0]).intValue();
					Integer x = ((Number) arguments[1]).intValue();
					processZoom(count, x);
					return true;
				}

			};
		} else {

			result.addListener(SWT.MouseWheel, new Listener() {

				@Override
				public void handleEvent(Event e) {
					processZoom(e.count, e.x);
				}

			});

			canvas.chartControl.addListener(SWT.MouseMove, new Listener() {

				@Override
				public void handleEvent(Event e) {
					if ((e.stateMask & SWT.BUTTON1) != 0) {
						int diff = e.x - oriX;
						offset = Math.max(0, orioffset + range * diff / (canvas.getSize().x - 120));
						repaint();
					} else {
						oriX = e.x;
						orioffset = offset;
					}
				}

			});

		}

		return result;

	}

	private void processZoom(Integer count, Integer x) {
		int rangeOld = range;
		range = Math.max(3, count > 0 ? range * 3 / 4 : range * 4 / 3);
		int rangeDiff = range - rangeOld;
		offset = Math.max(0, offset - (rangeDiff - rangeDiff * x / (canvas.getSize().x - 120)));
		repaint();
	}

	private void calcChart() {
		if (canvas != null) {
			try {
				BoersenChart boersenChart = null;
				if (intradayButton != null && intradayButton.getSelection()) {
					if (istocks == null) {
						int samesame = 0;
						List<Stock> sss = new ArrayList<Stock>();
						File dir = new File("C:\\kurse\\snapshots");
						Stock lastistock = null;
						for (String file : dir.list()) {
							if (file.startsWith(stocks.getSymbol() + " ")) {
								Date date = null;
								if (file.contains("PM."))
									date = Stock.dateFormat4.parse(file.substring(file.indexOf(" ") + 1));
								else
									date = Stock.dateFormat3.parse(file.substring(file.indexOf(" ") + 1));
								if (date.getHours() >= 15) {
									continue;
								}
								if (date.getHours() < 9) {
									continue;
								}
								String text = Utilities.fromFile(new File(dir, file));
								Stock istock = parseStock(text, date);
								if (istock == null) {
									continue;
								}
								istock.setDate(date);
								if (lastistock != null && lastistock.same(istock)) {
									samesame++;
									continue;
								}
								if (lastistock != null && lastistock.getVolume() == istock.getVolume() || istock.getVolume() == 0) {
									samesame++;
									continue;
								}
								// if (lastistock != null && istock.getClose() == lastistock.getClose()) {
								// sss.set(sss.size() - 1, istock);
								// continue;
								// }
								sss.add(istock);
								lastistock = istock;
							}
						}
						istocks = sss;
						System.out.println("samesame:" + samesame);
					}

					canvas.setVisible(istocks.size() != 0);
					if (canvas.isVisible()) {
						boersenChart = new BoersenChart(calcCutout(istocks), getLatestStock(istocks), istocks, calcCutout(istocks), stocks);
					}
				} else {
					canvas.setVisible(true);
					List<Stock> astocks = stocks.asList();
					if (relativeButton.getSelection()) {
						if (stocks.getGroup() != null) {
							for (Stocks groupstocks : Manager.maerkte.get(0).getStocks()) {
								if ("Banks".equals(stocks.getGroup()) && groupstocks.getName().contains("Bank")) {
									// if (groupstocks.getName().contains(stocks.getGroup())) {
									astocks = relativate(groupstocks, getOldestStock(calcCutout(astocks)));
								}
							}
						}
					}
					boersenChart = new BoersenChart(calcCutout(astocks), stock, astocks, calcCutout(stocks.asList()), stocks);
				}

				if (boersenChart != null) {
					boersenChart.intraday = intradayButton.getSelection();
					boersenChart.showDepth = depthButton.getSelection();
					boersenChart.showOrders = ordersButton.getSelection();
					boersenChart.showVolumn = volumnsButton.getSelection();
					boersenChart.showFitting = fittingButton.getSelection();
					boersenChart.showProfit = profitButton.getSelection();
					boersenChart.showMACD = macdButton.getSelection();
					boersenChart.showRSI = rsiButton.getSelection();
					boersenChart.showFastSTO = fastSTOButton.getSelection();
					boersenChart.showNormalized = normalizedButton.getSelection();
					boersenChart.showLpSolved = lpSolveButton.getSelection();
					canvas.setChart(boersenChart.createCFStockChart());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			enabledButtons();
		}
	}

	private List<Stock> relativate(Stocks groupstocks, Stock equalizer) {
		Stocks astocks;
		List<Stock> ss = new ArrayList<Stock>();
		Stock prevstock = null;
		Stock prevgroupstock = null;
		Stock prevrelstock = null;

		Map<Date, Stock> stockByDate = new HashMap<Date, Stock>();
		for (Stock stock : groupstocks.getStocks()) {
			stockByDate.put(stock.date, stock);
		}

		double measuredDifference = 0;
		for (Stock stock : stocks.getStocks()) {
			boolean measureDifference = stock == equalizer;
			Stock groupstock = stockByDate.get(stock.date);
			stock = stock.log();
			if (groupstock != null) {
				groupstock = groupstock.log();
			}
			if (prevstock != null && prevgroupstock != null) {
				if (prevrelstock == null) {
					prevrelstock = prevstock;
				}
				if (groupstock == null) {
					groupstock = prevgroupstock;
				}
				Stock relStock = new Stock(null);
				relStock.close = incr(prevrelstock.close, prevstock.close, stock.close, prevgroupstock.close, groupstock.close);
				relStock.high = incr(prevrelstock.high, prevstock.high, stock.high, prevgroupstock.high, groupstock.high);
				relStock.low = incr(prevrelstock.low, prevstock.low, stock.low, prevgroupstock.low, groupstock.low);
				relStock.open = incr(prevrelstock.open, prevstock.open, stock.open, prevgroupstock.open, groupstock.open);
				relStock.adjClose = incr(prevrelstock.adjClose, prevstock.adjClose, stock.adjClose, prevgroupstock.adjClose, groupstock.adjClose);
				relStock.volume = (long) incr(stock.volume, prevstock.volume, stock.volume, prevgroupstock.volume, groupstock.volume);
				relStock.date = stock.date;
				ss.add(relStock);

				prevgroupstock = groupstock;
				prevstock = stock;
				prevrelstock = relStock;
				if (measureDifference) {
					measuredDifference = stock.close - relStock.close;
				}
			} else {
				ss.add(stock);
				prevstock = stock;
				prevgroupstock = groupstock;
			}
		}
		List<Stock> sss = new ArrayList<Stock>();
		for (Stock relStock : ss) {
			relStock.close += measuredDifference;
			relStock.high += measuredDifference;
			relStock.low += measuredDifference;
			relStock.open += measuredDifference;
			relStock.adjClose += measuredDifference;
			sss.add(relStock.exp());
		}

		return sss;
	}

	private double incr(double close, double percent) {
		return close * (1 + percent);
	}

	private double incr(double close, double closeOld1, double closeNew1, double closeOld2, double closeNew2) {
		return close + (closeNew1 - closeOld1) - (closeNew2 - closeOld2);
		// return Math.exp(Math.log(close) + Math.log(closeNew1) - Math.log(closeOld1) - (Math.log(closeNew2) - Math.log(closeOld2)));

		// return close * (1 + perc(closeOld1, closeNew1) - perc(closeOld2, closeNew2));
		// return close * (1 + perc(closeOld1, closeNew1) - perc(closeOld2, closeNew2));
	}

	private double perc(double closeOld, double closeNew) {
		return (closeNew - closeOld) / closeOld;
	}

	public List<Stock> calcCutout(List<Stock> istocks2) {

		int toIndex = Math.min(istocks2.size() - offset, istocks2.size());
		if (toIndex - range < 0) {
			offset = Math.max(0, offset + (toIndex - range));
			toIndex = Math.min(istocks2.size() - offset, istocks2.size());
		}
		int fromIndex = toIndex - range;
		if (fromIndex < 0) {
			range = Math.max(1, range + fromIndex);
			fromIndex = Math.max(0, toIndex - range);
		}
		List<Stock> subList = istocks2.subList(fromIndex, toIndex);
		// while (subList.size() >= 128)
		// subList = halve(subList);
		return subList;
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

	public static Stock parseStock(String text, Date date) {
		Stock istock = new Stock(null);
		istock.setDate(date);
		double last = findCol("Last_field\" autocomplete=\"off\">", "<", text);

		istock.setHigh(findCol("Offer", text));
		istock.setLow(findCol("Bid", text));
		istock.setOpen(last);
		istock.setClose(last);

		istock.setVolume(Math.round(findCol("lblVolume_field\" autocomplete=\"off\">", "<", text)));
		istock.setBuyers((int) findCol("buyers for ", " units", text));
		istock.setSellers((int) findCol("sellers for ", " units", text));
		istock.setBuyersPeople((int) findCol("TotalBuyersForUnits\":\"", " buyers for", text));
		istock.setSellersPeople((int) findCol("TotalSellersForUnits\":\"", " sellers for", text));
		if (last == 0) {
			return null;
		}
		int pos = 0;
		for (String atext : text.split(Pattern.quote("},{"))) {
			double bn = findCol("Bn\":\"", "\"", atext);
			if (bn == 0)
				continue;
			istock.addBuyer((int) findCol("Bq\":\"", "\"", atext), findCol("Bp\":\"", "\"", atext), (int) findCol("Sq\":\"", "\"", atext), findCol("Sp\":\"", "\"", atext), pos);
			pos = pos + 1;
		}

		return istock;
	}

	private static double findCol(String str, String text) {
		return findCol(str + "_Quote_field\" autocomplete=\"off\">", "<", text);
	}

	private static double findCol(String str, String end, String text) {
		int index = text.indexOf(str);
		if (index != -1) {
			index += str.length();
			String s = text.substring(index, text.indexOf(end, index));
			s = s.replace(",", "");
			return Double.parseDouble(s);
		}
		return 0;
	}

	private void enabledButtons() {
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		notifyButton = createCheck(parent, 30, "Notify", false);
		intradayButton = createCheck(parent, 25, "Intraday", false);
		depthButton = createCheck(parent, 27, "Depth", false);
		ordersButton = createCheck(parent, 28, "Orders", false);
		profitButton = createCheck(parent, 29, "Profit", false);
		volumnsButton = createCheck(parent, 31, "Volumn", false);
		fittingButton = createCheck(parent, 32, "Fitting", false);
		macdButton = createCheck(parent, 33, "MACD", false);
		rsiButton = createCheck(parent, 34, "RSI", false);
		fastSTOButton = createCheck(parent, 35, "Fast STO", false);
		normalizedButton = createCheck(parent, 36, "Norm. Vol.", false);
		relativeButton = createCheck(parent, 37, "Relative", false);
		lpSolveButton = createCheck(parent, 38, "LpSolve", false);
		createButton(parent, 26, "Update", false);
		createButton(parent, 24, "Yahoo", false);
		enabledButtons();
	}

	protected Button createCheck(Composite parent, int id, String label, boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.CHECK);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(Integer.valueOf(id));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(((Integer) event.widget.getData()).intValue());
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		// buttons.put(Integer.valueOf(id), button);
		setButtonLayoutData(button);
		return button;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 26) {
			istocks = null;
		} else if (buttonId == 25) {
			offset = 0;
			range = 32;
		} else if (buttonId == 38) {
			if (lpSolveButton.getSelection()) {
				List<Stock> sts = calcCutout(stocks.asList());
				lpSolve(getOldestStock(sts).date, getLatestStock(sts).date, stocks);
			}
		} else if (buttonId == 24) {
			try {
				String symb = URLEncoder.encode(stocks.getS());
				URI uri = new URI("https://au.finance.yahoo.com/echarts?s=" + symb + "#symbol=" + symb + ";range=5d");
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(uri);
				return;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			super.buttonPressed(buttonId);
		}
		repaint();
	}

	public void repaint() {
		calcChart();
		canvas.updateChart();
		canvas.chartControl.update();
	}

	@Override
	protected Control createContents(Composite parent) {
		Control result = super.createContents(parent);
		calcChart();
		return result;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}

	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(newShellStyle & ~(SWT.APPLICATION_MODAL));
	}

	@Override
	public boolean close() {
		try {
			return super.close();
		} finally {
			if (current == this)
				current = null;
		}
	}

	@Override
	protected void constrainShellSize() {
		getShell().setSize(1300, 600);
		super.constrainShellSize();
	}

	private void setChart(Stocks stocks, Stock stock) {
		if (this.stocks != stocks) {
			this.istocks = null;
		}
		this.stocks = stocks;
		this.stock = stock;
		offset = stocks.size() - 1 - stocks.indexOf(stock);
	}

	public static void displayIfOpened(Stocks stocks, Stock stock) {
		if (current != null)
			display(stocks, stock);
	}

	static public void display(Stocks stocks, Stock stock) {
		if (current == null) {
			current = new ChartDialog();
			current.setChart(stocks, stock);
			current.calcChart();
			current.open();
		} else {
			current.setChart(stocks, stock);
			current.repaint();
		}
	}

	private static final int FIRST_COL = 1;

	private void lpSolve(Date oldest, Date latest, Stocks only) {

		boolean solveAll = false;

		try {
			// {
			//
			// // Create a problem with 4 variables and 0 constraints
			// LpSolve solver = LpSolve.makeLp(0, 4);
			//
			// // add constraints
			// solver.strAddConstraint("3 2 2 1", LpSolve.LE, 4);
			// solver.strAddConstraint("0 4 3 1", LpSolve.GE, 3);
			// List<Stocks> stocks = Manager.maerkte.get(0).getStocks();
			// }

			List<Stocks> stocks = Manager.maerkte.get(0).getStocks();

			// Create a problem with 4 variables and 0 constraints
			LpSolve solver = LpSolve.makeLp(0, stocks.size() * (solveAll ? stocks.size() : 1));

			LogListener logfunc = new LogListener() {

				@Override
				public void logfunc(LpSolve arg0, Object arg1, String arg2) throws LpSolveException {
					System.out.print("LogListener:" + arg2);
				}
			};
			solver.putLogfunc(logfunc, new Integer(234));

			MsgListener msgfunc = new MsgListener() {

				@Override
				public void msgfunc(LpSolve problem, Object userhandle, int msg) throws LpSolveException {
					System.out.print("MsgListener:" + msg);
				}

			};
			solver.putMsgfunc(msgfunc, new Integer(345), 1 | 8 | 16 | 32 | 128 | 512);

			{
				int col = FIRST_COL;
				for (Stocks stock : stocks) {
					if (only != null && stock != only) {
						continue;
					}
					for (Stocks relstock : stocks) {
						solver.setColName(col, stock.getSymbol() + "_" + relstock.getSymbol());
						col++;
					}
				}
			}

			{
				// set objective function
				List<Double> coefficents = new ArrayList<Double>();
				for (Stocks stock : stocks) {
					if (only != null && stock != only) {
						continue;
					}
					for (Stocks relstock : stocks) {
						if (stock == relstock) {
							coefficents.add(0.0);
						} else {
							coefficents.add(1.0);
						}
					}
				}
				solver.setMaxim();
				solver.strSetObjFn(StringUtils.join(coefficents, " "));

				// addConstraint(solver, coefficents, LpSolve.GE, 0.0);
			}

			// calculate closes
			final Set<Date> allDates = new HashSet<Date>();
			for (Stocks stc : stocks) {
				List<Date> dates = stc.asList().stream().map(x -> x.date).filter(date -> date.getDay() == 5).collect(Collectors.toList());
				if (allDates.isEmpty()) {
					allDates.addAll(dates);
				} else {
					allDates.retainAll(dates);
					if (allDates.isEmpty()) {
						throw new RuntimeException("No dates!");
					}
				}
			}
			Map<Stocks, List<Double>> closesMap = new HashMap<Stocks, List<Double>>();
			for (Stocks stc : stocks) {
				List<Double> closes = stc.asList().stream().filter(x -> x.within(oldest, latest) && allDates.contains(x.date)).map(x -> x.close).collect(Collectors.toList());
				List<Double> closesAll = stc.asList().stream().filter(x -> x.within(null, latest) && allDates.contains(x.date)).map(x -> x.close).collect(Collectors.toList());
				closesAll = BoersenChart.ema(closesAll, 3);
				closes = closesAll.subList(closesAll.size() - closes.size(), closesAll.size());
				closesMap.put(stc, closes);
			}

			// add constraints
			for (Stocks stock : stocks) {
				if (only != null && stock != only) {
					continue;
				}

				int index = 0;
				Stock prevStock = null;
				for (Stock st : stock.getStocks()) {
					if (!allDates.contains(st.date) || !st.within(oldest, latest)) {
						continue;
					}
					if (index == 0) {
						prevStock = st;
						index++;
						continue;
					}

					List<Double> coefficents = new ArrayList<Double>();
					for (Stocks relstock : stocks) {
						if (stock == relstock) {
							coefficents.add(0.0);
						} else {
							List<Double> closes = closesMap.get(relstock);
							double reldelta = Math.log(closes.get(index))/* - Math.log(closes.get(index - 1)) */;
							coefficents.add(reldelta/* * stock.volume * (st.close - prevStock.close) */);
						}
					}
					List<Double> closes = closesMap.get(stock);
					double rh = Math.log(closes.get(index)) /*- Math.log(closes.get(index - 1))*/;
					{
						addConstraint(solver, coefficents, rh > 0 ? LpSolve.LE : LpSolve.GE, rh);
					}

					prevStock = st;
					index++;
				}

				ArrayList<Double> coefficents = new ArrayList<Double>();
				append(coefficents, 1.0, stocks.size());
				addConstraint(solver, coefficents, LpSolve.EQ, 1.0);

			}

			for (int row = 0; row < solver.getNrows(); row++) {
				printLastRow(solver, row + 1);
			}

			// solve the problem
			solver.solve();

			// print solution
			System.out.println("Value of objective function: " + solver.getObjective());
			double[] var = solver.getPtrVariables();
			for (int i = 0; i < var.length; i++) {
				System.out.println("Value of var[" + solver.getColName(i + FIRST_COL) + "] = " + var[i]);
			}

			// delete the problem and free memory
			solver.deleteLp();
		} catch (LpSolveException e) {
			e.printStackTrace();
		}

	}

	private void addConstraint(LpSolve solver, List<Double> coefficents, int i, double rh) throws LpSolveException {
		solver.strAddConstraint(StringUtils.join(coefficents, " "), i, rh);
		// solver.addConstraint(coefficents.stream().mapToDouble(Double::doubleValue).toArray(), i, rh);
	}

	private void printLastRow(LpSolve solver, int row) throws LpSolveException {
		// double[] coefficents2 = solver.getPtrRow(row);

		String result = "0";
		int colnr = 0;
		for (int col = 0; col < solver.getNcolumns(); col++) {
			double coeff = solver.getMat(row, col + FIRST_COL);
			String varName = solver.getColName(colnr + FIRST_COL);
			result += "+" + coeff + "*" + varName;
			colnr++;
		}
		if (result.startsWith("0+")) {
			result = result.substring("0+".length());
		}

		short type = solver.getConstrType(row);
		System.out.println(solver.getRowName(row) + ": " + result + (type == LpSolve.LE ? "<=" : type == LpSolve.GE ? ">=" : type == LpSolve.EQ ? "=" : "??") + solver.getRh(row));
	}

	private void append(List<Double> coefficents, double d, int size) {
		while (size > 0) {
			coefficents.add(d);
			size--;
		}
	}

	private String getTextualCoefficients(double[] coefficents, LpSolve solver) throws LpSolveException {
		String result = "0";
		int colnr = FIRST_COL;
		for (double coeff : coefficents) {
			String varName = solver.getColName(colnr);
			result += "+" + coeff + "*" + varName;
			colnr++;
		}
		if (result.startsWith("0+")) {
			result = result.substring("0+".length());
		}
		return result;
	}

}
