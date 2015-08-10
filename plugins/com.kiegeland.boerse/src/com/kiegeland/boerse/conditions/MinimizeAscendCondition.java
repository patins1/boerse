package com.kiegeland.boerse.conditions;

import com.kiegeland.boerse.domain.Stock;

public class MinimizeAscendCondition extends Condition {

	public float success(Stock buy, Stock base) {
		return base.close / buy.close;
	}

	@Override
	public boolean fullfilled(Stock buy, Stock base) {
		return success(buy, base) >= 1.0;
	}

}
