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
import java.util.stream.Collectors;

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

	public static double Ascendent = 1.02;

	public static boolean UseDate = true;

	public static List<Markt> maerkte = new ArrayList<Markt>();

	public static String[] marktNames = new String[] { "ASX"
			/* , "SDAXI" , "DJI", "IXIC", "NDX", "STOXX50E" */ };

	public static List<Condition> conditions;

	public static boolean CourseOfSales;

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
			stockz = symbols.stream().filter(aktie -> readStocks(aktie, new File(marktDir + "/" + aktie.getFileName()))).collect(Collectors.toList());

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
			double success = Condition.getGain(buy);
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
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {

			String stockData;

			while ((stockData = br.readLine()) != null) {
				StringTokenizer stockTok = new StringTokenizer(stockData, ",\"");
				String date = stockTok.nextToken();
				if ("Course Of Sales".equals(date)) {
					CourseOfSales = true;
					// Stock.dateFormat = Stock.dateFormat2;
				}
				if ("Date".equals(date) || "Course Of Sales".equals(date) || "Time".equals(date))
					continue;
				Stock stock = new Stock(result);
				if (CourseOfSales) {
					stock.setDate(Stock.dateFormat2.parse(date));
					stock.setOpen(new Float(stockTok.nextToken()));
					stock.setVolume(new Long(stockTok.nextToken()));
					float value = new Float(stockTok.nextToken());
					String cond = stockTok.hasMoreTokens() ? stockTok.nextToken() : "";
					if (stock.volume > 20000) {
						System.out.println(cond + ":" + stock.volume);
					}
					if ("S3 XT".equals(cond)) {
						continue;
					}

					stock.setOpen(value / stock.getVolume());
					stock.setAdjClose(stock.getOpen());
					stock.setHigh(stock.getOpen());
					stock.setLow(stock.getOpen());
					stock.setClose(stock.getOpen());

				} else {
					stock.setDate(Stock.dateFormat.parse(date));
					stock.setOpen(new Float(stockTok.nextToken()));
					stock.setHigh(new Float(stockTok.nextToken()));
					stock.setLow(new Float(stockTok.nextToken()));
					stock.setClose(new Float(stockTok.nextToken()));
					stock.setVolume(new Long(stockTok.nextToken()));
					stock.setAdjClose(new Float(stockTok.nextToken()));
				}
				// if (CourseOfSales && (stock.date.getHours() < 10 || stock.date.getHours() >= 16)) {
				// continue;
				// }
				if (stock.close <= 0 || CourseOfSales && stock.getVolume() <= 0) {
					System.out.println("" + stock + " is disregard because close=" + stock.close);
					if (!CourseOfSales)
						return false;
					else
						continue;
				}
				// if (stocks.isEmpty() && stock.close * stock.volume <10000000) {
				// System.out.println("" + stock + " is disregard because too less volumn: " + stock.close +"*"+ stock.volume);
				//// br.close();
				//// file.delete();
				// return false;
				// }
				stocks.add(stock);
			}
		} catch (Exception e1) {
			System.err.println("cannot load symbol " + result.getSymbol() + ": " + e1.getMessage());
			// throw new RuntimeException(e1);
		}
		if (CourseOfSales && false) {
			List<Stock> stocks2 = new ArrayList<Stock>();
			for (int i = 0; i < 6 * 60; i++) {
				stocks2.add(null);
			}
			for (Stock stock : stocks) {
				int index = (stock.date.getHours() - 10) * 60 + stock.date.getMinutes();
				Stock stock2 = stocks2.get(index);
				if (stock2 == null) {
					stocks2.set(index, stock);
					continue;
				}
				stock2.setLow(Math.min(stock2.getLow(), stock.getLow()));
				stock2.setHigh(Math.max(stock2.getHigh(), stock.getHigh()));
				stock2.setOpen((stock2.getOpen() * stock2.getVolume() + stock.getOpen() * stock.getVolume()) / (stock.getVolume() + stock2.getVolume()));
				stock2.setVolume(stock2.getVolume() + stock.getVolume());
				stock2.setClose(stock2.getOpen());
			}
			while (stocks2.remove(null)) {
				stocks2.remove(null);
			}
			stocks = stocks2;
			Collections.reverse(stocks);
		}
		if (stocks.isEmpty())
			return false;
		Collections.reverse(stocks);
		result.setStocks(stocks);
		return true;
	}

	static Collection<Stocks> extractSymbols(InputStream content) {
		Collection<Stocks> result = new ArrayList<Stocks>();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(content));
			String stockData;

			while ((stockData = br.readLine()) != null) {
				StringTokenizer stockTok = new StringTokenizer(stockData, ",\"");
				try {
					String name = stockTok.nextToken();
					String symbol = stockTok.nextToken();
					String group = stockTok.nextToken();
					if (symbol.equals("ASX code"))
						continue;
					if (group.equals("Class Pend"))
						continue;
					if (group.equals("Not Applic"))
						continue;
					Stocks stock = new Stocks(symbol);
					stock.setGroup(group);
					stock.setName(name);
					result.add(stock);
				}

				catch (Exception e) {
					// throw new RuntimeException(e1);
					System.out.println(e.getMessage());
				}
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

		result.add(new Stocks("^AXGD", "S&P/ASX All Ordinaries Gold Index"));
		result.add(new Stocks("^AXPJ", "S&P/ASX 200 A-REIT Index"));
		result.add(new Stocks("^AXDJ", "S&P/ASX 200 Consumer Discretionary Index"));
		result.add(new Stocks("^AXSJ", "S&P/ASX 200 Consumer Staples Index"));
		result.add(new Stocks("^AXEJ", "S&P/ASX 200 Energy Index"));
		result.add(new Stocks("^AXFJ", "S&P/ASX 200 Financial Index"));
		result.add(new Stocks("^AXXJ", "S&P/ASX 200 Financials excluding A-REITs Index"));
		result.add(new Stocks("^AXHJ", "S&P/ASX 200 Health Care Index"));
		result.add(new Stocks("^AXNJ", "S&P/ASX 200 Industrials Index"));
		result.add(new Stocks("^AXIJ", "S&P/ASX 200 Information Technology Index"));
		result.add(new Stocks("^AXMJ", "S&P/ASX 200 Materials Index"));
		result.add(new Stocks("^AXMM", "S&P/ASX 300 Metals and Mining Index"));
		result.add(new Stocks("^AXJR", "S&P/ASX 200 Resources"));
		result.add(new Stocks("^AXTJ", "S&P/ASX 200 Telecommunications Services Index"));
		result.add(new Stocks("^AXUJ", "S&P/ASX 200 Utilities Index"));
		result.add(new Stocks("^AXBAJ", "S&P/ASX 200 Bank Index"));

		result.add(new Stocks("^ATLI", "ASX20"));
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
					// continue;
					contentMetadata = new FileInputStream(metadataFile);
				} else {
					String kurseString = Utilities.downloadURL("http://www.asx.com.au/asx/research/ASXListedCompanies.csv");
					Utilities.toFile(metadataFile, kurseString);
					contentMetadata = new StringBufferInputStream(kurseString);
				}
				Collection<Stocks> symbols = extractSymbols(contentMetadata);
				// for (File kursFile : new File(marktDir).listFiles()) {
				// if (kursFile.getName().indexOf(".") == -1) {
				// continue;
				// }
				// String symbol = kursFile.getName().substring(0, kursFile.getName().indexOf("."));
				// if (kursFile.getName().startsWith("_")) {
				// symbol = symbol.substring(1) + ".AX";
				// } else if (kursFile.getName().startsWith("^")) {
				// symbol = symbol;
				// } else {
				// continue;
				// }
				for (Stocks symbol : symbols) {
					File kursFile = new File(marktDir + "/" + symbol.getFileName());
					if (kursFile.exists()) {
						// "http://ichart.yahoo.com/table.csv?s=IFX.DE&d=11&e=31&f=2008&g=d&a=2&b=14&c=2000&ignore=.csv
						try {
							// String kursString = Utilities.downloadURL("http://ichart.yahoo.com/table.csv?s=" + symbol.getS() + "&d=12&e=09&f=2016&g=d&a=0&b=01&c=1980&ignore=.csv");
							// Utilities.toFile(kursFile, kursString);
						} catch (Exception e) {
							// System.err.println(symbol.getGroup() + " " + e.getMessage());
							// throw new RuntimeException(e);
						}
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
