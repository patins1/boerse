package com.kiegeland.boerse.conditions;

import com.kiegeland.boerse.domain.Stock;

public class DetermineBaseCondition extends Condition {

	@Override
	public float success(Stock buy, Stock base) {
		return 0;
	}

	@Override
	public Stock determineBase(Stock buy, int variable) {
		Stock base = buy.getStock(-variable);
		if (base != null)
			return base;
		return buy.getStocks().getOldestStock();
	}

	@Override
	public boolean fullfilled(Stock buy, Stock base) {
		return false;
	}
}
