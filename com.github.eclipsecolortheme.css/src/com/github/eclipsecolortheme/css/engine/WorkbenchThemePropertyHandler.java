package com.github.eclipsecolortheme.css.engine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class WorkbenchThemePropertyHandler implements ICSSPropertyHandler {

	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		assert engine instanceof CSSWorkbenchEngine;
		if (value instanceof CSSPrimitiveValue
				&& ((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_URI) {
			InputStream stream = engine.getResourcesLocatorManager()
					.getInputStream(
							((CSSPrimitiveValue) value).getStringValue());
			try {
				applyEclipseColorTheme(stream, (CSSWorkbenchEngine) engine);
			} finally {
				stream.close();
			}
			return true;
		} else if (value instanceof CSSPrimitiveValue
				&& ((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_STRING) {
			InputStream stream = new ByteArrayInputStream(value.getCssText()
					.getBytes(StandardCharsets.UTF_8));
			applyEclipseColorTheme(stream, (CSSWorkbenchEngine) engine);
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
			applyEclipseColorTheme(stream, (CSSWorkbenchEngine) engine);
			return true;
		}
		return false;
	}

	private void applyEclipseColorTheme(InputStream stream, CSSWorkbenchEngine engine) {
		engine.applyEclipseColorTheme(stream);
	}

	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
