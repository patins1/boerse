package com.kiegeland.boerse.views;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.kiegeland.boerse.Manager;
import com.kiegeland.boerse.average.AverageConfig;
import com.kiegeland.boerse.average.AverageDialog;
import com.kiegeland.boerse.conditions.Condition;
import com.kiegeland.boerse.conditions.DetermineBaseCondition;
import com.kiegeland.boerse.conditions.MinimizeAscendCondition;
import com.kiegeland.boerse.domain.Markt;
import com.kiegeland.boerse.domain.Stocks;

import de.kupzog.ktable.KTable;

public class ControlView extends ViewPart {

	public static String knr = "x"; // SBroker knr
	public static String onvistaUser = "x";// onvista user name
	public static String onvistaPsw = "x";// onvista password

	private Stocks aStocks;

	private KTable historyTable;

	private Button optParams;

	private Label ascendentLabel;

	private Composite panelAscendent;

	private Composite panelBase;

	private Label baseLabel;

	private Slider ascSlider;

	public ControlView() {
	}

	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new RowLayout(SWT.VERTICAL));
		{
			Composite panelMarkt = new Composite(parent, 0);
			panelMarkt.setLayout(new RowLayout(SWT.HORIZONTAL));

			for (final Markt markt : Manager.maerkte) {

				final Button checkboxMarkt = new Button(panelMarkt, SWT.CHECK);
				checkboxMarkt.setText(markt.getName());
				checkboxMarkt.setSelection(markt.getEnabled());
				checkboxMarkt.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// nothing to do
					}

					@Override
					public void widgetSelected(SelectionEvent e) {
						markt.setEnabled(checkboxMarkt.getSelection());
						Manager.ReCalc();
					}

				});

			}
		}

		{
			Composite panelDate = new Composite(parent, 0);
			panelDate.setLayout(new RowLayout(SWT.HORIZONTAL));
			final DateTime dateTime = new DateTime(panelDate, 0);
			assignBuyDate(dateTime);
			dateTime.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
					DateTime dateWidget = (DateTime) e.getSource();
					Calendar calendar = Calendar.getInstance();
					calendar.set(dateWidget.getYear(), dateWidget.getMonth(), dateWidget.getDay(), 0, 0, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					Date newDate = calendar.getTime();
					if (Manager.buyDate != null) {
						boolean after = newDate.after(Manager.buyDate);
						Date newDate2 = Manager.findEqualOr(newDate, after);
						if (newDate2 == null) {
							newDate2 = Manager.findEqualOr(newDate, !after);
						}
						if (newDate2 == null) {
							assignBuyDate(dateTime);
							return;
						}
						if (newDate2.equals(newDate)) {
							Manager.buyDate = newDate2;
						} else {
							Manager.buyDate = newDate2;
							assignBuyDate(dateTime);
						}
					}
					Manager.ReCalc();
				}

				public void widgetSelected(SelectionEvent e) {
					this.widgetDefaultSelected(e);
				}
			});

			final Button checkboxDate = new Button(panelDate, SWT.CHECK);
			checkboxDate.setSelection(Manager.UseDate);
			checkboxDate.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// nothing to do
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					Manager.UseDate = checkboxDate.getSelection();
					Manager.ReCalc();
				}

			});
		}

		{
			panelBase = new Composite(parent, 0);
			panelBase.setLayout(new FillLayout(SWT.VERTICAL));
			final Slider slider = new Slider(panelBase, 0);
			baseLabel = new Label(panelBase, 0);
			slider.setMinimum(1);
			slider.setMaximum(250);
			slider.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					Manager.BaseDays = slider.getSelection();
					baseLabel.setText("BaseDays=" + slider.getSelection());
					Manager.ReCalc();
				}

			});
			slider.setSelection(Manager.BaseDays);
			baseLabel.setText("BaseDays=" + slider.getSelection());
		}

		{
			panelAscendent = new Composite(parent, 0);
			panelAscendent.setLayout(new FillLayout(SWT.VERTICAL));
			ascSlider = new Slider(panelAscendent, 0);
			ascendentLabel = new Label(panelAscendent, 0);
			setAscendText();
			final int ascScaling = 1000;
			ascSlider.setMaximum((int) (1.1 * ascScaling));
			ascSlider.setMinimum((int) (0.9 * ascScaling));
			// sliderLabel.setSize(400, sliderLabel.getSize().y);
			ascSlider.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					Manager.Ascendent = (float) (ascSlider.getSelection() / (float) ascScaling);
					setAscendText();
					Manager.ReCalc();
				}

			});
			ascSlider.setSelection((int) (Manager.Ascendent * ascScaling));
		}

		{
			Button b = new Button(parent, 0);
			b.setText("ONVISTA");
			b.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						final WebView webView = (WebView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("com.kiegeland.boerse.views.WebView");
						final Browser web = webView.browser;
						webView.onComplete = new Runnable() {

							@Override
							public void run() {
								System.out.println("" + web.execute("document.getElementById('USERNAME').value='" + onvistaUser + "'"));
								System.out.println("" + web.execute("document.getElementById('PASSWORD').value='" + onvistaPsw + "'"));
								System.out.println("" + web.execute("document.getElementById('formular').submit()"));
								webView.onComplete = new Runnable() {
									@Override
									public void run() {
										web.setUrl("http://www.onvista.de/realpushliste.html?ID_SHORT=DAX");
									}
								};
							}
						};

						// if (!web.getUrl().contains("onvista")) {
						web.setUrl("http://www.onvista.de/");

						// webView.webBrowser.setUrl("http://www.google.de/");
					} catch (PartInitException e2) {
						throw new RuntimeException(e2);
					}
				}

			});
		}

		{
			Button b = new Button(parent, 0);
			b.setText("SBROKER");
			b.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						final SBrokerView sBrokerView = (SBrokerView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("com.kiegeland.boerse.views.SBrokerView");
						final Browser web = sBrokerView.browser;
						sBrokerView.onComplete = new Runnable() {

							@Override
							public void run() {
								System.out.println("" + web.execute("document.getElementById('knr').value='" + knr + "'"));
								System.out.println("" + web.execute("for(var i=0; i<document.all.length; i++) if (document.all[i].getAttribute(\"type\")==\"password\") document.all[i].value='8253453';"));
								System.out.println("" + web.execute("document.getElementById('legitimation').submit()"));
								sBrokerView.onComplete = new Runnable() {
									@Override
									public void run() {
										// web.setUrl("http://www.onvista.de/realpushliste.html?ID_SHORT=DAX");
									}
								};
							}
						};

						// if (!web.getUrl().contains("onvista")) {
						web.setUrl("https://meindepot.sbroker.de/0_start/0_4_login.jsp");

						// webView.webBrowser.setUrl("http://www.google.de/");
					} catch (PartInitException e2) {
						throw new RuntimeException(e2);
					}
				}

			});
		}

		{
			Button b = new Button(parent, 0);
			b.setText("Recalculate");
			b.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					Manager.ReCalc();
				}

			});
		}

		optParams = new Button(parent, 0);
		optParams.setText("Calc Opt Params");
		optParams.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				List<Condition> conditions = new ArrayList<Condition>();
				DetermineBaseCondition determineCondition = new DetermineBaseCondition();
				MinimizeAscendCondition ascendCondition = new MinimizeAscendCondition();
				conditions.add(determineCondition);
				conditions.add(ascendCondition);
				int highestDetermineFactor = -1;
				int highestAscendFactor = -1;
				double highestZinsen = Float.MIN_VALUE;
				for (int determineFactor = 0; determineFactor < determineCondition.getVariableRange(); determineFactor++) {
					for (int ascendFactor = 0; ascendFactor < ascendCondition.getVariableRange(); ascendFactor++) {
						double zinsen = 0;
						int count = 0;
						// for (Markt markt : ViewPart1.getEnabledMaerkte())
						// for (Stocks aStock : markt.getStocks()) {
						// for (Stock buy : aStock.getStocks()) {
						// Stock sell = buy.getStock(2);
						// if (sell == null)
						// continue;
						// Stock base = determineCondition.determineBase(buy,
						// determineFactor);
						// // price+=determineCondition.contribution(buy,
						// // base);
						// double add = ascendCondition.contribution(buy, base)
						// * ascendFactor;
						// if (Double.isNaN(add))
						// continue;
						// zinsen += buy.getZinsen(sell);
						// count++;
						// }
						// }
						zinsen = zinsen / count;
						if (zinsen > highestZinsen) {
							highestAscendFactor = ascendFactor;
							highestDetermineFactor = determineFactor;
							// System.out.println(ascendCondition.toString(highestAscendFactor)
							// + " " +
							// determineCondition.toString(highestDetermineFactor));
							// optParams.setText(ascendCondition.toString(highestAscendFactor)
							// + " " +
							// determineCondition.toString(highestDetermineFactor));
						}
					}

					System.out.println("determineFactor=" + determineFactor);
				}
				System.out.println("==================================");
				// System.out.println(ascendCondition.toString(highestAscendFactor)
				// + " " + determineCondition.toString(highestDetermineFactor));
				// optParams.setText(ascendCondition.toString(highestAscendFactor)
				// + " " + determineCondition.toString(highestDetermineFactor));

			}

		});

		Button average = new Button(parent, 0);
		average.setText("Average");
		average.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				AverageConfig chart = new AverageConfig();
				AverageDialog dialog = new AverageDialog(chart);
				dialog.open();
			}

		});

		// b.setLayoutData(new BorderLayout.BorderData(BorderLayout.NORTH));
	}

	private void setAscendText() {
		String result = "" + Manager.Ascendent;
		while (result.length() < "1.020".length())
			result += "0";
		result = "Ascendent=" + result;
		ascendentLabel.setText(result);
		ascendentLabel.pack();
		panelAscendent.pack();
	}

	private void assignBuyDate(DateTime dateTime) {
		if (Manager.buyDate != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(Manager.buyDate);
			dateTime.setYear(calendar.get(Calendar.YEAR));
			dateTime.setMonth(calendar.get(Calendar.MONTH));
			dateTime.setDay(calendar.get(Calendar.DAY_OF_MONTH));
		}
	}

	@Override
	public void setFocus() {
		// nothing to do
	}

}
