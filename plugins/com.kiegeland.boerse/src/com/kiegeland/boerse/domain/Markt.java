/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.domain;

import java.io.File;
import java.util.List;

public class Markt {

	private static final String METADATA = "metadata.cvs";

	String marktName;

	List<Stocks> stockz = null;

	private boolean enabled = true;

	public Markt(String marktName) {
		this.marktName = marktName;
		this.enabled = "GDAXI".equals(marktName);
	}

	public String getName() {
		return marktName;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean selection) {
		enabled = selection;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public List<Stocks> getStocks() {
		return stockz;
	}

	public void setStocks(List<Stocks> stockz2) {
		stockz = stockz2;
	}

	public String getDir() {
		return "c:/kurse/" + marktName;
	}

	public File getMetaFile() {
		return new File(getDir() + "/" + METADATA);
	}

}
