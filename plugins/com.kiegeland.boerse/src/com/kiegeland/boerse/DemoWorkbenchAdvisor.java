package com.kiegeland.boerse;

import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class DemoWorkbenchAdvisor extends WorkbenchAdvisor {

	public void initialize(IWorkbenchConfigurer configurer) {
		getWorkbenchConfigurer().setSaveAndRestore(false);
		super.initialize(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return "com.kiegeland.boerse.perspective1";
	}

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(final IWorkbenchWindowConfigurer windowConfigurer) {
		return new DemoWorkbenchWindowAdvisor(windowConfigurer);
	}
}
