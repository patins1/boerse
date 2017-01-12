/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import com.kiegeland.boerse.table.ITaggedValues;

public class Gain implements ITaggedValues {

	Object desc;

	List<Float> percentages = new ArrayList<Float>();

	float highestPercentage = 0;

	float lowestPercentage = 0;

	public final Stocks stocks;

	private final Stock buyStock;

	public int summarizeSize;

	public double success;

	public Gain(Object desc, Stocks stocks, Stock buyStock) {
		this.desc = desc;
		this.stocks = stocks;
		this.buyStock = buyStock;
	}

	@Override
	public Object getAttribute(String name) {
		if (name.equals("")) {
			if (desc instanceof Date) {
				return Stock.dateFormat.format(((Date) desc));
			}
			return "" + desc;
		}
		Float percentage = getPercentage(Integer.parseInt(name) - 1);
		if (percentage == null)
			return "";
		float eurosPerThousand = 10000 * percentage - 10000;
		// return ""+Math.round(percentage*100);
		return "" + Math.round(eurosPerThousand);
	}

	public void add(double d) {
		if (highestPercentage < d)
			highestPercentage = (float) d;
		if (lowestPercentage > d)
			lowestPercentage = (float) d;
		percentages.add((float) d);
	}

	@Override
	public RGB getBackgoundColor(int column) {
		if (column == 0)
			return new RGB(255, 255, 255);
		Float percentage = getPercentage(column - 1);
		if (percentage == null)
			return new RGB(255, 255, 255);
		if (percentage >= 1) {
			int i = Math.min((int) Math.max((int) Math.round(255 * (((percentage - 1) / (highestPercentage - 1)))), 0), 255);
			return new RGB(255, 255, 255 - i);
		}
		int i = Math.min((int) Math.max((int) Math.round(255 * percentage), 0), 255);
		return new RGB(255, i, i);
	}

	public Float getPercentage(int day) {
		if (day >= percentages.size())
			return null;
		return percentages.get(day);
	}

	public int getNumber() {
		return percentages.size();
	}

	@Override
	public int getInitialColumnWidth(int column) {
		if (column == 0)
			return 160;
		return 30;
	}

	public Stocks getStocks() {
		return stocks;
	}

	public Stock getBuyStock() {
		return buyStock;
	}

	public void setSummarizeSize(int summarizeSize) {
		this.summarizeSize = summarizeSize;
	}

	public void setSymbol(String string) {
		desc = string;
	}

}
