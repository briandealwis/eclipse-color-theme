package com.github.eclipsecolortheme.css.engine;

import java.io.InputStream;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.impl.engine.CSSEngineImpl;
import org.eclipse.e4.ui.css.core.impl.engine.RegistryCSSElementProvider;
import org.eclipse.e4.ui.css.core.impl.engine.RegistryCSSPropertyHandlerProvider;
import org.eclipse.ui.IWorkbench;
import org.w3c.dom.Element;

import com.github.eclipsecolortheme.ColorThemeApplicator;
import com.github.eclipsecolortheme.ColorThemeManager;
import com.github.eclipsecolortheme.ParsedTheme;

public class CSSWorkbenchEngine extends CSSEngineImpl {
	private IWorkbench workbench;

	public CSSWorkbenchEngine(IWorkbench workbench, IExtensionRegistry registry) {
		this.workbench = workbench;
		setElementProvider(new RegistryCSSElementProvider());
		propertyHandlerProviders.add(new RegistryCSSPropertyHandlerProvider(
				registry));
	}

	@Override
	public Element getElement(Object element) {
		if (element instanceof CSSStylableElement
				&& ((CSSStylableElement) element).getNativeWidget() instanceof IWorkbench) {
			return (CSSStylableElement) element;
		} else if (element instanceof IWorkbench) {
			return super.getElement(element);
		}
		return null;
	}

	public void reapply() {
		applyStyles(workbench, true);
	}

	public void applyEclipseColorTheme(InputStream stream) {
		ColorThemeManager manager = new ColorThemeManager();
		ParsedTheme theme;
		try {
			theme = manager.parseTheme(stream, true);
			new ColorThemeApplicator(workbench, manager).apply(
					theme.getTheme(), true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
