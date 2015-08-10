package com.kiegeland.boerse.conditions;

import com.kiegeland.boerse.Manager;
import com.kiegeland.boerse.domain.Stock;

public class UpUpDownCondition extends Condition {

	public float success(Stock buy, Stock base) {
		return buy.close / base.close;
	}

	@Override
	public boolean fullfilled(Stock buy, Stock base) {
		return success(buy, base) >= Manager.Ascendent && buy.getStock(-10) != null && buy.getStock(-1).close / buy.getStock(0).close < 1 && buy.getStock(-2).close / buy.getStock(-1).close > 1 && buy.getStock(-3).close / buy.getStock(-2).close < 1 && buy.getStock(-4).close / buy.getStock(-3).close < 1
				&& buy.getStock(-5).close / buy.getStock(-4).close > 1;
	}

}
