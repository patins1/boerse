/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.conditions;

import com.kiegeland.boerse.Manager;
import com.kiegeland.boerse.domain.Stock;

abstract public class Condition {

	abstract public float success(Stock buy, Stock base);

	public int getVariableRange() {
		return 100; // 1=0.001
	}

	// abstract public String toString(int variable);

	public Stock determineBase(Stock buy, int variable) {
		return buy;
	}

	public boolean fullfilled(Stock buy, Stock base) {
		return success(buy, base) >= 1;
	}

	static public float getGain(Stock buy) {
		// condition: open/close conditions
		// if (true) {
		// if (buy.close < buy.getOpen())
		// continue;
		// }

		// condition: buy monday
		// if (!true) {
		// if (buy.getDate().getDay() != 1)
		// continue;
		// }

		// condition: absolute upper bound
		// {
		// if (!(buy.close<=2))
		// continue;
		// }

		// condition: increase lower bound
		// {
		// if (!(buy.close / base.close >= Ascendent ))
		// continue;
		// }

		// condition: increase upper bound
		// {
		// if (!(buy.close / base.close <= 0.5))
		// continue;
		// }

		// condition: no intermediate day to be higher
		// {
		// boolean cancel = false;
		// for (Stock intermediate = base.succ(); intermediate != buy;
		// intermediate = intermediate.succ()) {
		// if (intermediate.close < buy.close) {
		// cancel = true;
		// break;
		// }
		// }
		// if (cancel)
		// continue;
		// }

		// condition: all days before "base" were higher
		// if (true) {
		// boolean cancel = false;
		// for (Stock intermediate = base.getStock(-25); intermediate !=
		// null && intermediate != base; intermediate = intermediate.succ())
		// {
		// if (intermediate.close < base.close) {
		// cancel = true;
		// break;
		// }
		// }
		// if (cancel)
		// continue;
		// }

		// condition: price was below before base price some longer time ago
		// {
		// Stock back = base.getStock(-30);
		// if (back == null)
		// continue;
		// if (back.close / base.close >= 1.0)
		// continue;
		// }
		Stock base = buy.getStock(-Manager.BaseDays);
		if (base != null) {
			for (Condition condition : Manager.conditions) {
				if (!condition.fullfilled(buy, base)) {
					return 0;
				}
			}
			for (Condition condition : Manager.conditions)
				return condition.success(buy, base);
		}
		return 0;
	}

}
