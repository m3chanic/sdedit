package net.sf.sdedit.editor.plugin;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.sdedit.editor.Editor;
import net.sf.sdedit.editor.TabAction;
import net.sf.sdedit.log._LOG_;
import net.sf.sdedit.ui.Tab;
import net.sf.sdedit.ui.UserInterface;
import net.sf.sdedit.ui.components.buttons.Activator;
import net.sf.sdedit.ui.components.buttons.ManagedAction;

@SuppressWarnings("serial")
public class FileActionProvider {

	private Map<FileHandler, Map<String, Action>> actionMap;

	public FileActionProvider() {
		actionMap = new HashMap<FileHandler, Map<String, Action>>();
	}

	public final Activator getOpenActivator =

	new Activator() {

		public boolean isEnabled() {
			Tab tab = Editor.getEditor().getUI().currentTab();
			if (tab == null) {
				return false;
			}
			return tab.getFileHandler() != null && tab.getFileHandler().canLoad();
		}
	};

	public final Activator getSaveActivator =

	new Activator() {

		public boolean isEnabled() {
			Tab tab = Editor.getEditor().getUI().currentTab();
			if (tab == null) {
				return false;
			}
			return tab.getFileHandler() != null && tab.getFileHandler().canSave();
		}

	};

	public Action getOpenAction(FileHandler handler, UserInterface ui) {
		Action action = getAction(handler, "OPEN");
		if (action == null) {
			action = makeOpenAction(handler, ui);
			setAction(handler, "OPEN", action);
		}
		return action;
	}

	public Action getSaveAction(FileHandler handler, UserInterface ui) {
		Action action = getAction(handler, "SAVE");
		if (action == null) {
			action = makeSaveAction(handler, ui);
			setAction(handler, "SAVE", action);
		}
		return action;
	}

	public Action getSaveAsAction(FileHandler handler, UserInterface ui) {
		Action action = getAction(handler, "SAVE_AS");
		if (action == null) {
			action = makeSaveAsAction(handler, ui);
			setAction(handler, "SAVE_AS", action);
		}
		return action;
	}

	private Action getAction(FileHandler fileHandler, String type) {
		Map<String, Action> types = actionMap.get(fileHandler);
		return types == null ? null : types.get(type);
	}

	private void setAction(FileHandler fileHandler, String type, Action action) {
		Map<String, Action> types = actionMap.get(fileHandler);
		if (types == null) {
			types = new HashMap<String, Action>();
			actionMap.put(fileHandler, types);
		}
		types.put(type, action);
	}

	private Action makeOpenAction(final FileHandler handler, final UserInterface ui) {

		return new AbstractAction() {
			{
				putValue(ManagedAction.ICON_NAME, "open");
				putValue(ManagedAction.ID, handler.getOpenID());
				putValue(ManagedAction.NEW_TEXT, handler.getOpenActionName());
				putValue(Action.SHORT_DESCRIPTION, handler.getOpenDescription());
				String actionName = handler.getOpenActionName();
				_LOG_.log("handler is " + handler.getClass().getSimpleName());
				if (handler.getOpenShortCut() != null) {
					actionName = handler.getOpenShortCut() + actionName;
				}
				_LOG_.log("name is " + actionName);
				putValue(Action.NAME, actionName);
			}

			public void actionPerformed(ActionEvent e) {

				File[] files = handler.selectFilesToOpen();
				if (files != null) {
					for (File file : files) {

						try {
							handler.loadFile(file.toURI().toURL(), ui);
						} catch (IOException ex) {
							ex.printStackTrace();
							ui.errorMessage(ex, null, null);
						}
					}
				}

			}

		};
	}

	private Action makeSaveAction(final FileHandler handler,
			final UserInterface ui) {
		return new TabAction<Tab>(Tab.class, ui) {
			{
				putValue(ManagedAction.ICON_NAME, "save");
				putValue(ManagedAction.ID, "SAVE");
				putValue(ManagedAction.NEW_TEXT, handler.getSaveActionName());
				putValue(Action.SHORT_DESCRIPTION, handler.getSaveDescription());
				String actionName = handler.getSaveActionName();
				if (handler.getSaveShortCut() != null) {
					actionName = handler.getSaveShortCut() + actionName;
				}
				putValue(Action.NAME, actionName);
			}

			protected void _actionPerformed(Tab tab, ActionEvent e) {
				try {
					handler.save(tab, false);
				} catch (IOException ex) {
					ex.printStackTrace();
					ui.errorMessage(ex, null, null);
				}
			}
		};
	}

	private Action makeSaveAsAction(final FileHandler handler,
			final UserInterface ui) {

		return new TabAction<Tab>(Tab.class, ui) {
			{
				putValue(ManagedAction.ICON_NAME, "saveas");
				putValue(ManagedAction.ID, "SAVE_AS");
				putValue(ManagedAction.NEW_TEXT, handler.getSaveAsActionName());
				putValue(Action.SHORT_DESCRIPTION, handler
						.getSaveAsDescription());
				String actionName = handler.getSaveAsActionName();
				if (handler.getSaveAsShortCut() != null) {
					actionName = handler.getSaveAsShortCut() + actionName;
				}
				putValue(Action.NAME, actionName);
			}

			protected void _actionPerformed(Tab tab, ActionEvent e) {
				try {
					handler.save(tab, true);
				} catch (IOException ex) {
					ex.printStackTrace();
					ui.errorMessage(ex, null, null);
				}
			}
		};
	}
}