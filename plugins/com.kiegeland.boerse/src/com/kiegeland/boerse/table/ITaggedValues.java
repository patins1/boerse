package com.kiegeland.boerse.table;

import org.eclipse.swt.graphics.RGB;

public interface ITaggedValues {

	Object getAttribute(String name);

	RGB getBackgoundColor(int row);

	int getInitialColumnWidth(int column);

}
