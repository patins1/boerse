/*
 * Created on Sep 7, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.kiegeland.boerse.domain;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import com.kiegeland.boerse.table.ITaggedValues;
import com.kiegeland.boerse.util.Utilities;

public class Stock implements ITaggedValues {

	public float close;
	public Stock succ;
	public int index;
	private Stock[] stocks;
	public Date date;
	float price;
	float open;
	float high;
	float low;
	float adjClose;
	public long volume;
	private final Stocks aStocks;

	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * @param symbol2
	 */
	public Stock(Stocks stocks) {
		this.aStocks = stocks;
	}

	/**
	 * @return
	 */

	/**
	 * @return
	 */
	public float getHigh() {
		return high;
	}

	/**
	 * @param high
	 */
	public void setHigh(float high) {
		this.high = high;
	}

	/**
	 * @return
	 */
	public float getLow() {
		return low;
	}

	/**
	 * @param low
	 */
	public void setLow(float low) {
		this.low = low;
	}

	/**
	 * @return
	 */
	public float getOpen() {
		return open;
	}

	/**
	 * @param open
	 */
	public void setOpen(float open) {
		this.open = open;
	}

	/**
	 * @return
	 */
	public Long getVolume() {
		return volume;
	}

	/**
	 * @param volume
	 */
	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setClose(float close) {
		this.close = close;
		if (this.high < close) {
			// System.out.println("Strange "+stocks.getSymbol()+"
			// close="+close+" high="+high);
			high = close;
		}
		if (this.low > close) {
			// System.out.println("Strange "+stocks.getSymbol()+"
			// close="+close+" low="+low);
			low = close;
		}
	}

	public void setAdjClose(float adjClose) {
		this.adjClose = adjClose;
	}

	public Object getAttribute(String name) {
		if (name.equals("Date"))
			return Stock.dateFormat.format(date);
		if (name.equals("Asc") && pred() != null)
			return Utilities.printPercentage(close / pred().close);
		if (name.equals("Open"))
			return open;
		if (name.equals("High"))
			return high;
		if (name.equals("Low"))
			return low;
		if (name.equals("Close"))
			return close;
		if (name.equals("Volume"))
			return volume;
		if (name.equals("Adj Close"))
			return adjClose;
		return "x";
	}

	@Override
	public RGB getBackgoundColor(int row) {
		return new RGB(255, 255, 255);
	}

	@Override
	public String toString() {
		return aStocks.getStockName() + "   " + Stock.dateFormat.format(date) + "   " + close + "€";
	}

	@Override
	public int getInitialColumnWidth(int column) {
		if (column == 0)
			return 130;
		return 69;
	}

	public Stocks getStocks() {
		return aStocks;
	}

	public void setIndex(int index) {
		this.index = index;
		this.stocks = aStocks.getStocks();
		this.succ = getStock(1);
	}

	public Stock getStock(int delta) {
		int newIndex = index + delta;
		if (newIndex >= 0 && newIndex < stocks.length)
			return stocks[newIndex];
		return null;
	}

	public Stock pred() {
		return getStock(-1);
	}

	public float getZinsen(Stock sell) {
		return (sell.close / this.close - 1) / (sell.index - this.index) + 1;
	}

	public float getZinsenOpt(Stock sell) {
		for (Stock stock = this.succ; stock != sell; stock = stock.succ) {
			if (getZinsen(stock) <= 1) {
				return getZinsen(stock);
			}
		}
		return getZinsen(sell);
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}
}
