package com.github.eclipsecolortheme.css.engine;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.ui.IWorkbench;
import org.w3c.dom.Element;

public class WorkbenchElementProvider implements IElementProvider {

	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof Element) {
			return (Element) element;
		}
		if (element instanceof IWorkbench) {
			return new WorkbenchElement((IWorkbench) element, engine);
		}
		return null;
	}

}
