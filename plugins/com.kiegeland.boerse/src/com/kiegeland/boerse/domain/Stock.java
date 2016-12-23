/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.graphics.RGB;

import com.kiegeland.boerse.table.ITaggedValues;
import com.kiegeland.boerse.util.Utilities;

public class Stock implements ITaggedValues {

	public float close;
	public Stock succ;
	public int index;
	public Stock[] stocks;
	public Date date;
	public float open;
	public float high;
	public float low;
	public float adjClose;
	public long volume;
	Stocks aStocks;

	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public static DateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");

	public static DateFormat dateFormat3 = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

	public static DateFormat dateFormat4 = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss a");
	private int buyers;
	private int sellers;
	private int buyersPeople;
	private int sellersPeople;

	float[] depths = new float[10 * 4];
	private int buyVolumnes;
	private int selVolumnes;

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

	public float getClose() {
		return close;
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
			return Utilities.printPercentage(getDelta());
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
		this.succ = getStock(1);
		if (pred() != null && pred().pred() != null && pred().pred().pred() != null && pred().pred().pred().pred() != null && pred().pred().pred().pred().pred() != null && succ != null && succ.succ != null && succ.succ.succ != null) {
			float delta = getDelta();
			float deltaPred = pred().getDelta();
			float deltaPredPred = pred().pred().getDelta();
			float deltaPredPredPred = pred().pred().pred().getDelta();
			float deltaPredPredPredPred = pred().pred().pred().pred().getDelta();
			float deltaNext = succ.getDelta();
			if (!true) {
				delta = getDeltaReverse();
				deltaPred = succ.getDeltaReverse();
				deltaPredPred = succ.succ.getDeltaReverse();
				deltaNext = pred().getDeltaReverse();
			}
			if (!true) {
				delta = getDeltaNegated();
				deltaPred = pred().getDeltaNegated();
				deltaPredPred = pred().pred().getDeltaNegated();
				deltaNext = succ.getDeltaNegated();
			}
			// if (delta > 0.0/100 && delta < 2.0/100 && deltaPredPred<0 && deltaPredPredPred<deltaPred && this.close<this.open) { //AllShorts= +0.62% #5056

			if (delta > 0.0 / 100 && delta < 1.0 / 100 && deltaPred < -1.0 / 100 && this.close < this.open) { // AllShorts= +0.62% #5056
				if (deltaNext > 0.1 / 100)
					return new RGB(0, 255, 0);
				else
					return new RGB(255, 0, 0);

			}
		}
		return new RGB(255, 255, 255);
	}

	public float getDelta() {
		return this.close / pred().close - 1;
	}

	public float getDeltaReverse() {
		return this.close / succ.close - 1;
	}

	public float getDeltaNegated() {
		return this.close / succ.close - 1;
	}

	@Override
	public String toString() {
		return aStocks.getStockName() + " [" + aStocks.getGroup() + "]   " + Stock.dateFormat.format(date) + "   " + close + "$";
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

	public void setBuyers(int buyers) {
		this.buyers = buyers;
	}

	public void setSellers(int sellers) {
		this.sellers = sellers;
	}

	public int getBuyers() {
		return buyers;
	}

	public int getSellers() {
		return sellers;
	}

	public void setBuyersPeople(int buyersPeople) {
		this.buyersPeople = buyersPeople;
	}

	public void setSellersPeople(int sellersPeople) {
		this.sellersPeople = sellersPeople;
	}

	public int getBuyersPeople() {
		return this.buyersPeople;
	}

	public int getSellersPeople() {
		return this.sellersPeople;
	}

	public boolean same(Stock istock) {
		return volume == istock.volume && open == istock.open && close == istock.close && low == istock.low && high == istock.high && buyers == istock.buyers && sellers == istock.sellers && buyersPeople == istock.buyersPeople && sellersPeople == istock.sellersPeople;
	}

	public void addBuyer(int buyVolumne, float buyPrice, int sellVolumne, float sellPrice, int pos) {
		depths[pos * 4 + 0] = buyVolumne;
		depths[pos * 4 + 1] = buyPrice;
		depths[pos * 4 + 2] = sellVolumne;
		depths[pos * 4 + 3] = sellPrice;
		buyVolumnes += buyVolumne;
		selVolumnes += sellVolumne;
	}

	public int getBuyVolumne(int pos) {
		return (int) depths[pos * 4 + 0];
	}

	public float getBuyPrice(int pos) {
		return depths[pos * 4 + 1];
	}

	public int getSellVolumne(int pos) {
		return (int) depths[pos * 4 + 2];
	}

	public float getSellPrice(int pos) {
		return depths[pos * 4 + 3];
	}

	public int getBuyVolumnes() {
		return buyVolumnes;
	}

	public int getSelVolumnes() {
		return selVolumnes;
	}

	public float getBuyPriceAtVolumne(int maxVolumne) {
		float result = 0;
		int volumne = 0;
		for (int pos = 0; pos < getDepth(); pos++) {
			int vol = Math.min(getBuyVolumne(pos), maxVolumne);
			result = getBuyPrice(pos) * vol;
			volumne = vol;
			maxVolumne -= vol;
			if (maxVolumne == 0) {
				return result / volumne;
			}
		}
		return result / volumne;
	}

	public float getSellPriceAtVolumne(int maxVolumne) {
		float result = 0;
		int volumne = 0;
		for (int pos = 0; pos < getDepth(); pos++) {
			int vol = Math.min(getSellVolumne(pos), maxVolumne);
			result = getSellPrice(pos) * vol;
			volumne = vol;
			maxVolumne -= vol;
			if (maxVolumne == 0) {
				return result / volumne;
			}
		}
		return result / volumne;
	}

	public float getBuyPriceForVolumne(int maxVolumne) {
		float result = 0;
		int volumne = 0;
		for (int pos = 0; pos < getDepth(); pos++) {
			int vol = Math.min(getBuyVolumne(pos), maxVolumne);
			result += getBuyPrice(pos) * vol;
			volumne += vol;
			maxVolumne -= vol;
		}
		return result / volumne;
	}

	public float getSellPriceForVolumne(int maxVolumne) {
		float result = 0;
		int volumne = 0;
		for (int pos = 0; pos < getDepth(); pos++) {
			int vol = Math.min(getSellVolumne(pos), maxVolumne);
			result += getSellPrice(pos) * vol;
			volumne += vol;
			maxVolumne -= vol;
		}
		return result / volumne;
	}

	private int getDepth() {
		return depths.length / 4;
	}

}
