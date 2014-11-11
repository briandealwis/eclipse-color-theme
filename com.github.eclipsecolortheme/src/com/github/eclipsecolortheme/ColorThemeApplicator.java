package com.github.eclipsecolortheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.MultiPartInitException;
import org.eclipse.ui.PartInitException;

/**
 * Logic for applying a given color theme to the workbench and its contents.
 */
public class ColorThemeApplicator {
	protected IWorkbench workbench;
	protected ColorThemeManager manager;
	protected IPreferenceStore prefStore = Activator.getDefault()
			.getPreferenceStore();

	/**
	 * Does the workbench support the openEditors() taking mementos that was
	 * only added in 3.8.2/4.2.2?
	 */
	private static boolean pageSupportsNewProtocol;

	static {
		try {
			IWorkbenchPage.class.getDeclaredMethod("getEditorState",
					new Class[] { IEditorReference[].class, boolean.class });
			pageSupportsNewProtocol = true;
		} catch (Exception e) {
			pageSupportsNewProtocol = false;
		}
	}

	public ColorThemeApplicator(IWorkbench workbench, ColorThemeManager manager) {
		this.workbench = workbench;
		this.manager = manager;
	}

	private static IMemento getEditorMemento(IEditorReference editor) {
		if (!pageSupportsNewProtocol) {
			return null;
		}
		return editor.getPage().getEditorState(
				new IEditorReference[] { editor }, false)[0];
	}

	public void apply(String themeName, boolean forceDefaultBG)
			throws MultiPartInitException {
		apply(manager.getTheme(themeName), forceDefaultBG);
	}

	/**
	 * Apply the given theme name.
	 * 
	 * @param themeName
	 * @return true if applied, false if cancelled by user (e.g., didn't want to
	 *         re-open editors)
	 * @throws MultiPartInitException
	 */
	public boolean apply(ColorTheme theme, boolean forceDefaultBG)
			throws MultiPartInitException {
		// FIXME: we should only re-open editors if the theme application
		// actually changes something. Unfortunately we don't (yet) have a way
		// to detect this
		if (theme.getName().equals(prefStore.getString("colorTheme"))) {
			// hmm... what if the value changed?
			return false;
		}

		Map<IWorkbenchPage, Collection<IEditorReference>> editorsToClose = new HashMap<IWorkbenchPage, Collection<IEditorReference>>();
		Map<IWorkbenchPage, Collection<EditorState>> editorsToReopen = new HashMap<IWorkbenchPage, Collection<EditorState>>();

		// used to accumulate exceptions when restoring editors
		List<IWorkbenchPartReference> failedEditors = new ArrayList<IWorkbenchPartReference>();
		List<PartInitException> failedExceptions = new ArrayList<PartInitException>();

		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {			
			Collection<IEditorReference> toClose = new LinkedList<IEditorReference>(); 
			editorsToClose.put(window.getActivePage(), toClose);
			Collection<EditorState> toReopen = new LinkedList<EditorState>();
			editorsToReopen.put(window.getActivePage(), toReopen);
			
			for (IEditorReference editor : window.getActivePage()
					.getEditorReferences()) {
				String id = editor.getId();
				/*
				 * C++ editors are not closed/reopened because it messes their
				 * colors up. TODO: Make this configurable in the mapping file.2
				 */
				if (!id.equals("org.eclipse.cdt.ui.editor.CEditor")) {
					toClose.add(editor);
					try {
						toReopen.add(new EditorState(editor));
					} catch (PartInitException e) {
						failedEditors.add(editor);
						failedExceptions.add(e);
					}
				}
			}
		}

		if (!editorsToClose.isEmpty()) {
			if (!MessageDialog
					.openConfirm(
							workbench.getActiveWorkbenchWindow() == null ? workbench
									.getActiveWorkbenchWindow().getShell()
									: null,
							"Reopen Editors",
							"In order to change the color theme, some editors have to be closed and reopened.")) {
				return false;
			}

			for(Entry<IWorkbenchPage,Collection<IEditorReference>> editors : editorsToClose.entrySet()) {
				editors.getKey().closeEditors(
						editors.getValue()
								.toArray(
										new IEditorReference[editors.getValue()
												.size()]), true);
			}
		}

		if (prefStore != null) {
			prefStore.setValue("colorTheme", theme.getName());
			prefStore.setValue("forceDefaultBG", forceDefaultBG);
		}
		manager.applyTheme(theme);

		// Re-open editors while accumulating PartInitExceptions
		for (Entry<IWorkbenchPage, Collection<EditorState>> editors : editorsToReopen
				.entrySet()) {
			int size = editors.getValue().size();
			if (size == 0) {
				continue;
			}
			String[] editorIDs = new String[size];
			IEditorInput[] inputs = new IEditorInput[size];
			IMemento[] mementos = new IMemento[size];
			int i = 0;
			for (EditorState state : editors.getValue()) {
				inputs[i] = state.input;
				editorIDs[i] = state.editorId;
				mementos[i] = state.memento;
				i++;
			}
			try {
				if (pageSupportsNewProtocol) {
					editors.getKey().openEditors(inputs, editorIDs, mementos,
							IWorkbenchPage.MATCH_INPUT, -1);
				} else {
					editors.getKey().openEditors(inputs, editorIDs,
							IWorkbenchPage.MATCH_INPUT);
				}
			} catch (MultiPartInitException e) {
				Collections.addAll(failedEditors, e.getReferences());
				Collections.addAll(failedExceptions, e.getExceptions());
			}
		}
		if (!failedExceptions.isEmpty()) {
			throw new MultiPartInitException(
					failedEditors.toArray(new IWorkbenchPartReference[failedEditors
							.size()]),
					failedExceptions
							.toArray(new PartInitException[failedExceptions
									.size()]));
		}
		return true;
	}

	private class EditorState {
		public String editorId;
		public IEditorInput input;
		public IMemento memento;

		public EditorState(String editorId, IEditorInput input, IMemento memento) {
			this.editorId = editorId;
			this.input = input;
			this.memento = memento;
		}

		public EditorState(IEditorReference editor) throws PartInitException {
			this(editor.getId(), editor.getEditorInput(),
					getEditorMemento(editor));

		}
	}

}
