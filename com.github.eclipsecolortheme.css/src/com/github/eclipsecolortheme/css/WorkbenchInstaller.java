package com.github.eclipsecolortheme.css;

import java.util.Hashtable;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import com.github.eclipsecolortheme.css.engine.CSSWorkbenchEngine;

public class WorkbenchInstaller implements IStartup {
	private IWorkbench workbench;
	private IThemeEngine themeEngine;
	private CSSWorkbenchEngine cssEngine;
	private IExtensionRegistry extensions;
	private ServiceRegistration<?> registration;

	private EventHandler themeChangedHandler = new EventHandler() {
		public void handleEvent(Event event) {
			// prepare for the possibility of multiple workbenches
			if (themeEngine == event
					.getProperty(IThemeEngine.Events.THEME_ENGINE)) {
				themeEngine.applyStyles(workbench, true);
			}
		}
	};

	public void earlyStartup() {
		workbench = PlatformUI.getWorkbench();

		themeEngine = (IThemeEngine) workbench.getService(IThemeEngine.class);
		if (themeEngine == null) {
			return;
		}

		Bundle bundle = FrameworkUtil.getBundle(getClass());
		BundleContext context;
		if (bundle != null && (context = bundle.getBundleContext()) != null) {
			// not sure what to do if the bundle is null
			String[] topics = new String[] { IThemeEngine.Events.THEME_CHANGED };
			Hashtable ht = new Hashtable();
			ht.put(EventConstants.EVENT_TOPIC, topics);
			registration = context.registerService(
					EventHandler.class.getName(), themeChangedHandler, ht);
		}
		// FIXME: should add a workbench shutdown listener on the workbench to
		// remove the service registration

		// FIXME: IThemeEngine does a reset-theme and the SWT CSS engines
		// assume the reset-theme happens on the SWT thread.
		// IExtensionRegistry extensions = (IExtensionRegistry)
		// workbench.getService(IExtensionRegistry.class);
		// cssEngine = new CSSWorkbenchEngine(workbench, extensions);
		// workbench.getDisplay().asyncExec(new Runnable() {
		// public void run() {
		// themeEngine.addCSSEngine(cssEngine);
		// // cssEngine.reapply();
		// }
		// });
	}
}
