package com.github.eclipsecolortheme.css;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.github.eclipsecolortheme.css.engine.CSSWorkbenchEngine;

public class WorkbenchInstaller implements IStartup {
	private IWorkbench workbench;
	private CSSWorkbenchEngine cssEngine;
	private IExtensionRegistry extensions;

	public void earlyStartup() {
		workbench = PlatformUI.getWorkbench();
		extensions = (IExtensionRegistry) workbench
				.getService(IExtensionRegistry.class);

		final IThemeEngine themeEngine = (IThemeEngine) workbench
				.getService(IThemeEngine.class);
		if (themeEngine == null) {
			return;
		}

		cssEngine = new CSSWorkbenchEngine(workbench, extensions);
		// FIXME: IThemeEngine does a reset-theme and the SWT CSS engines
		// assume the reset-theme happens on the SWT thread.
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				themeEngine.addCSSEngine(cssEngine);
				// cssEngine.reapply();
			}
		});
	}
}
