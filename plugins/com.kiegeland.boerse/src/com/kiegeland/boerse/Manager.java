/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.kiegeland.boerse.average.AverageDialog;
import com.kiegeland.boerse.conditions.Condition;
import com.kiegeland.boerse.conditions.MaximizeAscendCondition;
import com.kiegeland.boerse.domain.Gain;
import com.kiegeland.boerse.domain.Markt;
import com.kiegeland.boerse.domain.Stock;
import com.kiegeland.boerse.domain.Stocks;
import com.kiegeland.boerse.util.Utilities;
import com.kiegeland.boerse.views.AllStockGainsView;

public class Manager {

	public static Date buyDate = null;

	public static int BaseDays = 7;

	public static float Ascendent = (float) 1.02;

	public static boolean UseDate = true;

	public static List<Markt> maerkte = new ArrayList<Markt>();

	public static String[] marktNames = new String[] { "GDAXI", "TECDAX", "MDAXI"
	/* , "SDAXI" , "DJI", "IXIC", "NDX", "STOXX50E" */};

	public static List<Condition> conditions;

	static {
		conditions = new ArrayList<Condition>();
		// conditions.add(new DetermineBaseCondition());
		conditions.add(new MaximizeAscendCondition());
		// conditions.add(new MinimizeAbsoluteCondition());
		// conditions.add(new UpUpDownCondition());
	}

	static {

		downloadStocks();

		for (String marktName : marktNames) {
			Markt markt = new Markt(marktName);
			List<Stocks> stockz = new ArrayList<Stocks>();
			String marktDir = markt.getDir();
			Collection<Stocks> symbols;
			try {
				symbols = extractSymbols(new FileInputStream(markt.getMetaFile()));
			} catch (FileNotFoundException e) {
				// throw new RuntimeException(e);
				System.err.println("cannot load markt " + marktName);
				continue;
			}
			for (Stocks aktie : symbols) {
				String fileName = marktDir + "/" + aktie.getFileName();
				if (aktie.getSymbol().equals("HRX.DE"))
					continue;
				if (readStocks(aktie, new File(fileName))) {
					stockz.add(aktie);
				}
			}
			markt.setStocks(stockz);
			maerkte.add(markt);
		}

		buyDate = findEqualOr(new Date(), false);
	}

	public static List<Gain> calcGains(Stocks aStock) {
		Stock[] stocks = aStock.getStocks();
		Date startDate = null;
		Date endDate = null;
		try {
			// startDate = Stock.dateFormat.parse("2001-12-01");
			// endDate = Stock.dateFormat.parse("2008-12-12");
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (startDate != null && endDate != null) {
			List<Stock> filteredStocks = new ArrayList<Stock>();
			for (Stock stock : stocks) {
				if (!stock.date.before(startDate) && !stock.date.after(endDate))
					filteredStocks.add(stock);
			}
			stocks = filteredStocks.toArray(new Stock[] {});
		}

		List<Gain> gains = new ArrayList<Gain>();
		for (Stock buy : stocks) {
			if (invalidDate(buy)) {
				continue;
			}
			float success = Condition.getGain(buy);
			if (success == 0)
				continue;
			Gain gain = new Gain(buy.date, aStock, buy);
			gain.success = success;
			for (Stock sell = buy.succ; sell != null && sell.index - buy.index <= 50; sell = sell.succ) {
				gain.add(buy.getZinsen(sell));
			}
			gains.add(gain);

		}
		return gains;
	}

	public static List<String> getColumns(List<Gain> summedGains, String caption) {
		int actualNumberDays = 0;
		for (Gain gain : summedGains) {
			actualNumberDays = Math.max(gain.getNumber(), actualNumberDays);
		}
		List<String> gainColumns = new ArrayList<String>();
		gainColumns.add("");
		for (int hold = 1; hold <= actualNumberDays; hold++) {
			gainColumns.add("" + hold);
		}
		return gainColumns;
	}

	public static boolean invalidDate(Stock buy) {
		return UseDate && buyDate != null && !buy.date.equals(buyDate);
	}

	public static List<Markt> getEnabledMaerkte() {
		List<Markt> result = new ArrayList<Markt>();
		for (Markt markt : maerkte)
			if (markt.isEnabled())
				result.add(markt);
		return result;
	}

	public static Date findEqualOr(Date newDate, boolean after) {
		Date result = null;
		for (Markt markt : getEnabledMaerkte())
			for (Stocks aStocks : markt.getStocks()) {
				for (Stock stock : aStocks.getStocks()) {
					Date stockDate = stock.date;
					if (newDate.equals(stockDate)) {
						return newDate;
					}
					if ((after ? stockDate.after(newDate) : stockDate.before(newDate))) {
						if (result == null || (after ? result.after(stockDate) : result.before(stockDate))) {
							result = stockDate;
						}
					}
				}
			}
		return result;
	}

	static private boolean readStocks(Stocks result, File file) {
		List<Stock> stocks = new ArrayList<Stock>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String stockData;

			while ((stockData = br.readLine()) != null) {
				StringTokenizer stockTok = new StringTokenizer(stockData, ",\"");
				String date = stockTok.nextToken();
				if ("Date".equals(date))
					continue;
				Date _date = Stock.dateFormat.parse(date);
				Stock stock = new Stock(result);
				stock.setDate(_date);
				stock.setOpen(new Float(stockTok.nextToken()));
				stock.setHigh(new Float(stockTok.nextToken()));
				stock.setLow(new Float(stockTok.nextToken()));
				stock.setClose(new Float(stockTok.nextToken()));
				stock.setVolume(new Long(stockTok.nextToken()));
				stock.setAdjClose(new Float(stockTok.nextToken()));
				if (stock.close <= 0) {
					System.out.println("" + stock + " is disregard because close=" + stock.close);
					return false;
				}
				stocks.add(stock);
			}
		} catch (Exception e1) {
			System.err.println("cannot load symbol " + result.getSymbol());
			// throw new RuntimeException(e1);
		}
		if (stocks.isEmpty())
			return false;
		Collections.reverse(stocks);
		result.setStocks(stocks);
		int index = 0;
		for (Stock stock : result.getStocks()) {
			stock.setIndex(index);
			index++;
		}
		return true;
	}

	static Collection<Stocks> extractSymbols(InputStream content) {
		Collection<Stocks> result = new ArrayList<Stocks>();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(content));
			String stockData;

			while ((stockData = br.readLine()) != null) {
				StringTokenizer stockTok = new StringTokenizer(stockData, ";\"");
				Stocks stock = new Stocks(stockTok.nextToken());
				stockTok.nextToken();
				stockTok.nextToken();
				String date = stockTok.nextToken();
				date = date.substring(6, 6 + 4) + "-" + date.substring(3, 3 + 2) + "-" + date.substring(0, 0 + 2);
				Date _date = Stock.dateFormat.parse(date);
				stock.setDate(_date);
				stockTok.nextToken();
				stockTok.nextToken();
				stockTok.nextToken();
				stockTok.nextToken();
				stock.setVolume(new Long(stockTok.nextToken().trim()));
				result.add(stock);
			}
		} catch (Exception e1) {
			// throw new RuntimeException(e1);
			System.out.println(e1.getMessage());
		}

		// String symbol;
		// SearchStructure sea = new SearchStructure(content);
		// while ((symbol=sea.findNext("<a href=\"/q?s=", "\"")) != null) {
		// if (symbol.endsWith(".DE")) {
		// String name=sea.findNext("<small>", "</small>");
		// if (name==null)
		// continue;
		// if (name.endsWith(" N"))
		// name=name.substring(0,name.length()-" N".length());
		// String vol=sea.findNext("<td class=\"yfnc_tabledata1\"
		// align=\"right\">", "</td>");
		// if (vol==null)
		// continue;
		// for (int punkt=vol.indexOf(".");punkt!=-1;punkt=vol.indexOf(".")) {
		// vol=vol.substring(0,punkt)+vol.substring(punkt+1);
		// }
		//
		// result.add(new MetaData(symbol,Integer.parseInt(vol)));
		// }
		// }
		return result;
	}

	static private void downloadStocks() {
		try {
			for (String marktName : marktNames) {
				Markt markt = new Markt(marktName);
				String marktDir = markt.getDir();
				new File(marktDir).mkdirs();
				File metadataFile = markt.getMetaFile();
				InputStream contentMetadata;
				if (metadataFile.exists()) {
					continue;
					// contentMetadata = new FileInputStream(new File(marktDir
					// + "/" + METADATA));
				} else {
					String kurseString = Utilities.downloadURL("http://de.old.finance.yahoo.com/d/quotes.csv?s=@%5E" + markt.getName() + "&f=sl1d1t1c1ohgv&e=.csv");
					Utilities.toFile(metadataFile, kurseString);
					contentMetadata = new StringBufferInputStream(kurseString);
				}
				Collection<Stocks> symbols = extractSymbols(contentMetadata);
				for (Stocks symbol : symbols) {
					File kursFile = new File(marktDir + "/" + symbol.getFileName());
					if (!kursFile.exists()) {
						// "http://ichart.yahoo.com/table.csv?s=IFX.DE&d=11&e=31&f=2008&g=d&a=2&b=14&c=2000&ignore=.csv
						String kursString = Utilities.downloadURL("http://ichart.yahoo.com/table.csv?s=" + symbol.getSymbol() + "&d=11&e=14&f=2009&g=d&a=0&b=0&c=1900&ignore=.csv");
						Utilities.toFile(kursFile, kursString);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static public void ReCalc() {
		try {
			AllStockGainsView viewPart1 = (AllStockGainsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("com.kiegeland.boerse.views.AllStockGainsView");
			viewPart1.calcMainTable();
			AverageDialog.update();
		} catch (PartInitException e2) {
			throw new RuntimeException(e2);
		}
	}
}