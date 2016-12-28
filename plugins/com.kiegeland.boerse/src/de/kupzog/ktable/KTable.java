package de.kupzog.ktable;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;

public class KTable extends Grid {

	private KTableDefaultModel model;

	public KTable(Composite parent, int style) {
		super(parent, style & ~SWT.FULL_SELECTION & ~SWT.FILL);
		this.setHeaderVisible(true);

		this.addListener(SWT.DefaultSelection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				fireCellDoubleClicked(0, (Integer) event.item.getData(), 0);
			}
		});
		this.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				com.ibm.icu.util.UResourceBundle ui = null;
				fireCellSelection(0, (Integer) event.item.getData(), 0);
			}
		});

		// addMouseListener(new MouseListener() {
		// @Override
		// public void mouseDoubleClick(MouseEvent e) {
		// }
		//
		// @Override
		// public void mouseDown(MouseEvent e) {
		// }
		//
		// @Override
		// public void mouseUp(MouseEvent e) {
		// GridItem item = getItem(new Point(e.x, e.y));
		// if (item != null) {
		// fireCellSelection(0, (Integer) item.getData(), 0);
		// }
		// }
		// });

	}

	protected void fireCellDoubleClicked(int col, int row, int statemask) {
	}

	public void setModel(KTableDefaultModel model) {
		this.model = model;
		for (Item item : this.getItems()) {
			item.dispose();
		}

		for (int col = 0; col < model.getColumnCount(); col++) {
			GridColumn column = new GridColumn(this, SWT.NONE, col);
			column.setText("" + model.doGetContentAt(col, 0));
			column.setWidth(model.getInitialColumnWidth(col));
		}

		for (int row = model.getFixedHeaderRowCount(); row < model.getRowCount(); row++) {
			final GridItem item = new GridItem(this, SWT.NONE);
			item.setData(new Integer(row));
			for (int col = 0; col < model.getColumnCount(); col++) {
				item.setText(col, "" + model.doGetContentAt(col, row));
				Color color = model.doGetCellColor(col, row);
				if (color != null) {
					item.setBackground(col, color);
				}
			}
		}

	}

	public KTableDefaultModel getModel() {
		return this.model;
	}

	protected void fireCellSelection(int col, int row, int statemask) {
	}

	public void setSelection(int col, int row, boolean b) {
		row -= model.getFixedHeaderRowCount();
		if (row >= 0) {
			this.setSelection(row);
		}
	}

}
