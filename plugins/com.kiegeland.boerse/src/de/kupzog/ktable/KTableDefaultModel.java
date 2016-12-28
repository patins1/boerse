package de.kupzog.ktable;

import org.eclipse.swt.graphics.Color;

public class KTableDefaultModel {

	public KTableDefaultModel() {
		super();
	}

	public KTableCellEditor doGetCellEditor(int col, int row) {
		return null;
	}

	public Color doGetCellColor(int col, int row) {
		return null;
	}

	public int doGetColumnCount() {
		return 0;
	}

	public Object doGetContentAt(int col, int row) {
		return "";
	}

	public int doGetRowCount() {
		return 0;
	}

	public void doSetContentAt(int col, int row, Object value) {
	}

	public int getInitialColumnWidth(int column) {
		return 50;
	}

	public int getInitialRowHeight(int row) {
		return 18;
	}

	public int getFixedHeaderColumnCount() {
		return 0;
	}

	public int getFixedHeaderRowCount() {
		return 1;
	}

	public int getFixedSelectableColumnCount() {
		return 0;
	}

	public int getFixedSelectableRowCount() {
		return 0;
	}

	public int getRowHeightMinimum() {
		return 18;
	}

	public boolean isColumnResizable(int col) {
		return true;
	}

	public boolean isRowResizable(int row) {
		return true;
	}

	public int getColumnWidth(int col) {
		return 100;
	}

	public int getRowHeight(int row) {
		return 0;
	}

	public int getRowOf(Object selection) {
		return -1;
	}

	public int getInitialFirstRowHeight() {
		return 18;
	}

	public int getColumnCount() {
		return doGetColumnCount();
	}

	public int getRowCount() {
		return doGetRowCount();
	}
}
