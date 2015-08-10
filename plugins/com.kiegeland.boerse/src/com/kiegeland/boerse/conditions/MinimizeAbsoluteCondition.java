/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.conditions;

import com.kiegeland.boerse.domain.Stock;

public class MinimizeAbsoluteCondition extends Condition {

	public float success(Stock buy, Stock base) {
		return 1 / buy.close;
	}

	@Override
	public boolean fullfilled(Stock buy, Stock base) {
		return success(buy, base) >= 1.0 / 10.0;
	}

}
