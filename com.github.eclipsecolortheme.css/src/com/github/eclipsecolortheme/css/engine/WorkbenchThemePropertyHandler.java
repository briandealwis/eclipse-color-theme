package com.github.eclipsecolortheme.css.engine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.ui.IWorkbench;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import com.github.eclipsecolortheme.ColorThemeApplicator;
import com.github.eclipsecolortheme.ColorThemeManager;
import com.github.eclipsecolortheme.ParsedTheme;

public class WorkbenchThemePropertyHandler implements ICSSPropertyHandler {

	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		// assert engine instanceof CSSWorkbenchEngine;

		IWorkbench workbench;
		if (element instanceof IWorkbench) {
			workbench = (IWorkbench) element;
		} else if (element instanceof WorkbenchElement) {
			workbench = (IWorkbench) ((WorkbenchElement) element)
					.getNativeWidget();
		} else {
			return false;
		}
		if (value instanceof CSSPrimitiveValue
				&& ((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_URI) {
			InputStream stream = engine.getResourcesLocatorManager()
					.getInputStream(
							((CSSPrimitiveValue) value).getStringValue());
			try {
				applyEclipseColorTheme(workbench, stream, engine);
			} finally {
				stream.close();
			}
			return true;
		} else if (value instanceof CSSPrimitiveValue
				&& ((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_STRING) {
			InputStream stream = new ByteArrayInputStream(value.getCssText()
					.getBytes(StandardCharsets.UTF_8));
			applyEclipseColorTheme(workbench, stream, engine);
			return true;
		} else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			StringBuilder contents = new StringBuilder();
			CSSValueList list = (CSSValueList) value;
			for (int i = 0; i < list.getLength(); i++) {
				CSSValue item = list.item(i);
				if (item instanceof CSSPrimitiveValue
						&& ((CSSPrimitiveValue) item).getPrimitiveType() == CSSPrimitiveValue.CSS_STRING) {
					contents.append(item.getCssText()).append('\n');
				} else {
					return false;
				}
			}
			InputStream stream = new ByteArrayInputStream(contents.toString()
					.getBytes(StandardCharsets.UTF_8));
			applyEclipseColorTheme(workbench, stream, engine);
			return true;
		}
		return false;
	}

	private void applyEclipseColorTheme(IWorkbench workbench,
			InputStream stream, CSSEngine engine) {
		// engine.applyEclipseColorTheme(stream);
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

	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
