/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.conditions;

import com.kiegeland.boerse.domain.Stock;

public class MaximizeAscendCondition extends Condition {

	public double success(Stock buy, Stock base) {
		// return buy.close / base.close;
		return 1;
	}

}
