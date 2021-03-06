/**
 * Copyright (C) 2015 by Joerg Kiegeland
 */
package com.kiegeland.boerse.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import de.kupzog.ktable.DefaultCellRenderer;
import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableDefaultModel;
import de.kupzog.ktable.TextCellRenderer;

public class KBoersenModel extends KTableDefaultModel {

	public final List<? extends ITaggedValues> stocks;

	TextCellRenderer renderHeader = new TextCellRenderer(SWT.BOLD);

	private final List<String> columns;

	public KBoersenModel(List<? extends ITaggedValues> stocks, List<String> columns) {
		super();
		this.stocks = new ArrayList<ITaggedValues>(stocks);
		Collections.reverse(this.stocks);
		this.columns = columns;
	}

	@Override
	public KTableCellEditor doGetCellEditor(int col, int row) {
		return null;
	}

	public Color doGetCellColor(int col, int row) {
		try {
			ITaggedValues stock = stocks.get(row - 1);
			RGB rgb = stock.getBackgoundColor(col);
			Color color = new Color(null, rgb);
			return color;
		} catch (Throwable e) {
			return null;
		}
	}

	public KTableCellRenderer doGetCellRenderer(int col, int row) {
		if (row == 0)
			return renderHeader;
		try {
			TextCellRenderer renderCell = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS_ROW);
			renderCell.setBackground(doGetCellColor(col, row));
			// renderCell.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT);
			return renderCell;
		} catch (Throwable e) {
			return renderHeader;
		}
	}

	@Override
	public int doGetColumnCount() {
		return columns.size();
	}

	@Override
	public Object doGetContentAt(int col, int row) {
		try {
			String name = columns.get(col);
			if (row == 0) {
				return name;
			}
			ITaggedValues stock = stocks.get(row - 1);
			return "" + stock.getAttribute(name);
		} catch (Throwable e) {
			return "";
		}
	}

	@Override
	public int doGetRowCount() {
		return stocks.size() + 1;
	}

	@Override
	public void doSetContentAt(int col, int row, Object value) {
	}

	@Override
	public int getInitialColumnWidth(int column) {
		if (!stocks.isEmpty())
			return stocks.get(0).getInitialColumnWidth(column);
		return super.getInitialFirstRowHeight();
	}

	@Override
	public int getInitialRowHeight(int row) {
		return 18;
	}

	@Override
	public int getFixedHeaderColumnCount() {
		return 0;
	}

	@Override
	public int getFixedHeaderRowCount() {
		return 1;
	}

	@Override
	public int getFixedSelectableColumnCount() {
		return 0;
	}

	@Override
	public int getFixedSelectableRowCount() {
		return 0;
	}

	@Override
	public int getRowHeightMinimum() {
		return 18;
	}

	@Override
	public boolean isColumnResizable(int col) {
		return true;
	}

	@Override
	public boolean isRowResizable(int row) {
		return true;
	}

	public int getColumnWidth(int col) {
		try {
			return super.getColumnWidth(col);
		} catch (Throwable t) {
			return 0;
		}
	}

	public int getRowHeight(int row) {
		try {
			return super.getRowHeight(row);
		} catch (Throwable t) {
			return 0;
		}
	}

	public int getRowOf(Object selection) {
		int index = stocks.indexOf(selection);
		if (index >= 0)
			return index + 1;
		return -1;
	}

}
