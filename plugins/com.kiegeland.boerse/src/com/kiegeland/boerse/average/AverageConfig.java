package com.kiegeland.boerse.average;

public class AverageConfig {

	public AverageConfig() {
	}

	public int sellAfter = 2;

	public int lookBack = 30;

	public int lookAhead = 30;

	public float above = (float) 1.2;

	public float below = (float) 1 - (above - 1);

	public float macht = 1;

	public int getLookRange() {
		return lookBack + lookAhead + 1;
	}

}
