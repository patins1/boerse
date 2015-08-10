package com.kiegeland.boerse.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class WebView extends ViewPart {

	public Runnable onComplete = null;

	private Composite panelBase;
	public Browser browser;

	public WebView() {
	}

	@Override
	public void createPartControl(Composite parent) {

		// parent.setLayout(new RowLayout(SWT.VERTICAL));

		{
			// panelBase = new Composite(parent, 0);
			// panelBase.setLayout(new FillLayout(SWT.HORIZONTAL));
			browser = new Browser(parent, SWT.FILL /* | SWT.MOZILLA */);
			browser.addProgressListener(new ProgressListener() {

				@Override
				public void changed(ProgressEvent event) {
					System.out.println("changed: " + browser.getUrl());
				}

				@Override
				public void completed(ProgressEvent event) {
					System.out.println("completed: " + browser.getUrl());
					if (onComplete != null) {
						Runnable _onComplete = onComplete;
						onComplete = null;
						_onComplete.run();
					}
				}

			});
			// webBrowser.setUrl("http://www.onvista.de/");
		}

	}

	@Override
	public void setFocus() {
		// nothing to do
	}

}
