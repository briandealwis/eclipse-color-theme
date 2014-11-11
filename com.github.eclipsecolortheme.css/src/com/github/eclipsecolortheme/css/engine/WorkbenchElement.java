package com.github.eclipsecolortheme.css.engine;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.css.core.dom.ElementAdapter;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.utils.ClassUtils;
import org.eclipse.ui.IWorkbench;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WorkbenchElement extends ElementAdapter {
	private String namespaceURI;
	private String localName;

	public WorkbenchElement(IWorkbench workbench, CSSEngine engine) {
		super(workbench, engine);
	}

	public Node getParentNode() {
		return null;
	}

	public NodeList getChildNodes() {
		return null;
	}

	public String getNamespaceURI() {
		if (namespaceURI == null) {
			namespaceURI = ClassUtils.getPackageName(IWorkbench.class);
		}
		return namespaceURI;
	}

	@Override
	public String getLocalName() {
		if (localName == null) {
			localName = ClassUtils.getSimpleName(IWorkbench.class);
		}
		return localName;
	}

	public String getCSSId() {
		return Platform.getProduct() != null ? Platform.getProduct().getId()
				: null;
	}

	public String getCSSClass() {
		return null;
	}

	public String getCSSStyle() {
		return null;
	}

	@Override
	public String getAttribute(String attr) {
		return null;
	}

}
