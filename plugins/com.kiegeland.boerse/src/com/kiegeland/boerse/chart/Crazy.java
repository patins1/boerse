package com.kiegeland.boerse.chart;

import java.text.ParseException;
import java.util.Date;

import com.kiegeland.boerse.domain.Stock;

public class Crazy {

	final private String symbol;
	final private double buy;
	final private Date from;
	final private Date to;
	final private Double sell;

	public Crazy(String symbol, double buy, Double sell, String from, String to) throws ParseException {
		this.symbol = symbol;
		this.buy = buy;
		this.sell = sell;
		this.from = Stock.dateFormat.parse(from);
		this.to = to != null ? Stock.dateFormat.parse(to) : new Date();
	}

	public String getSymbol() {
		return symbol;
	}

	public Date getFrom() {
		return from;
	}

	public Date getTo() {
		return to;
	}

	public double getBuy() {
		return buy;
	}

	public Double getSell() {
		return sell;
	}

	public boolean within(Stock stock) {
		return !stock.date.before(getFrom()) && !stock.date.after(getTo());
	}

}
