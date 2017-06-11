/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.domain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.kiegeland.boerse.SearchStructure;
import com.kiegeland.boerse.util.Utilities;

public class Stocks {

	private String symbol;
	private String name;
	private Date date;
	public long volume;

	private Stock[] stocks = new Stock[] {};
	private String group;

	public Stocks(String symbol) {
		this.symbol = symbol;
	}

	public Stocks(String symbol, String name) {
		this.symbol = symbol;
		this.name = name;
	}

	public String getSymbol() {
		return symbol;
	}

	public Stock[] getStocks() {
		return stocks;
	}

	public void setStocks(List<Stock> stocks) {
		this.stocks = stocks.toArray(new Stock[] {});
		int index = 0;
		for (Stock stock : getStocks()) {
			stock.aStocks = this;
			stock.setIndex(index);
			index++;
		}
		// if (size() == 0)
		// throw new RuntimeException("No stocks found!");
	}

	public Stock getLatestStock() {
		return stocks[stocks.length - 1];
	}

	public Stock getOldestStock() {
		return stocks[0];
	}

	public int size() {
		return stocks.length;
	}

	public int indexOf(Stock baseStock) {
		return asList().indexOf(baseStock);
	}

	public List<Stock> subList(int fromIndex, int toIndex) {
		return asList().subList(fromIndex, toIndex);
	}

	public List<Stock> asList() {
		List<Stock> result = new ArrayList<Stock>(stocks.length);
		for (Stock stock : stocks) {
			result.add(stock);
		}
		return result;
	}

	public String getFileName() {
		if (getSymbol().startsWith("^")) {
			return getSymbol() + ".csv";
		}
		return "_" + getSymbol() + ".csv";
	}

	public String getS() {
		if (getSymbol().startsWith("^")) {
			return getSymbol();
		}
		return getSymbol() + ".AX";
	}

	public File getNameMappingFile() {
		return new File("C:\\kurse\\_" + getSymbol() + ".txt");
	}

	public String getStockName() {
		if (name != null)
			return name;
		try {
			name = Utilities.fromFile(getNameMappingFile());
		} catch (IOException e) {
			try {
				String content = Utilities.downloadURL("http://au.finance.yahoo.com/q?s=" + getSymbol() + ".AX");
				SearchStructure sea = new SearchStructure(content);
				name = sea.findNext("r ", "- Yahoo! Finanzen").trim();
				if (name.endsWith(" N"))
					name = name.substring(0, name.length() - " N".length());
				name = name.replace("&amp;", "&");
				Utilities.toFile(getNameMappingFile(), name);
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}
		if (name.endsWith(" N"))
			name = name.substring(0, name.length() - " N".length());
		name = name.replace("&amp;", "&");
		return name;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setVolume(Long long1) {
		volume = long1;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getGroup() {
		return group;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void add(Stock stock) {
		List<Stock> stockss = new ArrayList<Stock>(Arrays.asList(stocks));
		stockss.add(stock);
		setStocks(stockss);
	}

}
