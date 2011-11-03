package dk.itu.big_red.editors.rule;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.SaveAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import dk.itu.big_red.editors.bigraph.BigraphEditorContextMenuProvider;
import dk.itu.big_red.editors.bigraph.actions.BigraphCheckpointAction;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCopyAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPasteAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPropertiesAction;
import dk.itu.big_red.editors.bigraph.parts.PartFactory;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.import_export.ReactionRuleXMLImport;
import dk.itu.big_red.util.UI;
import dk.itu.big_red.util.ValidationFailedException;

public class RuleEditor extends EditorPart implements
	CommandStackListener, ISelectionListener {
	private DefaultEditDomain editDomain = new DefaultEditDomain(this);
	
	private static class ActionIDList extends ArrayList {
		@Override
		public boolean add(Object o) {
			if (o instanceof IAction) {
				try {
					IAction action = (IAction) o;
					o = action.getId();
					throw new IllegalArgumentException(
							"Action IDs should be added to lists, not the action: " + action); //$NON-NLS-1$
				} catch (IllegalArgumentException exc) {
					exc.printStackTrace();
				}
			}
			return super.add(o);
		}
	}

	private ActionRegistry actionRegistry = new ActionRegistry();
	private List selectionActions = new ActionIDList();
	private List stackActions = new ActionIDList();
	private List propertyActions = new ActionIDList();
	
	private ScrollingGraphicalViewer redexViewer, reactumViewer;
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		redexViewer = new ScrollingGraphicalViewer();
		reactumViewer = new ScrollingGraphicalViewer();
		
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		return getCommandStack().isDirty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout gl = new GridLayout(3, false);
		gl.marginTop = gl.marginLeft = gl.marginBottom = gl.marginRight = 
			gl.horizontalSpacing = gl.verticalSpacing = 10;
		c.setLayout(gl);
		
		redexViewer.createControl(c);
		redexViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label l = new Label(c, SWT.NONE);
		l.setFont(UI.tweakFont(l.getFont(), 40, SWT.BOLD));
		l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		l.setText("→");
		
		reactumViewer.createControl(c);
		reactumViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		org.eclipse.swt.widgets.List list =
			new org.eclipse.swt.widgets.List(c, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		gd.heightHint = 100;
		list.setLayoutData(gd);
		
		redexViewer.getControl().setBackground(ColorConstants.listBackground);
		reactumViewer.getControl().setBackground(ColorConstants.listBackground);
		
		redexViewer.setEditDomain(editDomain);
		reactumViewer.setEditDomain(editDomain);
		
		redexViewer.setEditPartFactory(new PartFactory());
		reactumViewer.setEditPartFactory(new PartFactory());
		
		redexViewer.setRootEditPart(new ScalableRootEditPart());
		reactumViewer.setRootEditPart(new ScalableRootEditPart());

		createActions();
		
		redexViewer.setContextMenu(
			new BigraphEditorContextMenuProvider(redexViewer, actionRegistry));
		reactumViewer.setContextMenu(
			new BigraphEditorContextMenuProvider(reactumViewer, actionRegistry));
		
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		getSite().setSelectionProvider(redexViewer);
		
		loadInput();
	}

	protected void loadInput() {
		ReactionRule model = null;
		
		IEditorInput input = getEditorInput();
	    if (input instanceof FileEditorInput) {
	    	FileEditorInput fi = (FileEditorInput)input;
	    	try {
	    		model = ReactionRuleXMLImport.importFile(fi.getFile());
	    	} catch (ImportFailedException e) {
	    		e.printStackTrace();
	    		Throwable cause = e.getCause();
	    		if (cause instanceof ValidationFailedException) {
	    			return;
	    		} else {
	    			return;
	    		}
	    	} catch (Exception e) {
	    		return;
	    	}
	    }
	    
	    if (model == null)
	    	model = new ReactionRule();
	    
	    redexViewer.setContents(model.getRedex());
	    setPartName(getEditorInput().getName());
    }
	
	/**
	 * Returns the command stack.
	 * @return the command stack
	 */
	protected CommandStack getCommandStack() {
		return getEditDomain().getCommandStack();
	}
	
	/**
	 * Returns the edit domain.
	 * @return the edit domain
	 */
	protected DefaultEditDomain getEditDomain() {
		return editDomain;
	}

	@Override
    public void commandStackChanged(EventObject event) {
		/*
		 * Why on earth is this necessary?
		 */
        firePropertyChange(IEditorPart.PROP_DIRTY);
        updateActions(stackActions);
    }
	
	/**
	 * Creates actions for this editor. Subclasses should override this method
	 * to create and register actions with the {@link ActionRegistry}.
	 */
	@SuppressWarnings("unchecked")
	protected void createActions() {
		ActionRegistry registry = getActionRegistry();
		IAction action;

		action = new UndoAction(this);
		registry.registerAction(action);
		stackActions.add(action.getId());

		action = new RedoAction(this);
		registry.registerAction(action);
		stackActions.add(action.getId());

		action = new SelectAllAction(this);
		registry.registerAction(action);

		action = new DeleteAction((IWorkbenchPart) this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new SaveAction(this);
		registry.registerAction(action);
		propertyActions.add(action.getId());

    	action = new ContainerPropertiesAction(this);
    	registry.registerAction(action);
    	selectionActions.add(action.getId());
    	
    	action = new ContainerCutAction(this);
    	registry.registerAction(action);
    	selectionActions.add(action.getId());
    	
    	action = new ContainerCopyAction(this);
    	registry.registerAction(action);
    	selectionActions.add(action.getId());
    	
    	action = new ContainerPasteAction(this);
    	registry.registerAction(action);
    	selectionActions.add(action.getId());
    	
    	action = new BigraphRelayoutAction(this);
    	registry.registerAction(action);
    	selectionActions.add(action.getId());
    	
    	action = new BigraphCheckpointAction(this);
    	registry.registerAction(action);
    	selectionActions.add(action.getId());
	}

	/**
	 * Initializes the ActionRegistry. This registry may be used by
	 * {@link ActionBarContributor ActionBarContributors} and/or
	 * {@link ContextMenuProvider ContextMenuProviders}.
	 * <P>
	 * This method may be called on Editor creation, or lazily the first time
	 * {@link #getActionRegistry()} is called.
	 */
	protected void initializeActionRegistry() {
		createActions();
		updateActions(propertyActions);
		updateActions(stackActions);
	}
	
	protected ActionRegistry getActionRegistry() {
		return actionRegistry;
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * A convenience method for updating a set of actions defined by the given
	 * List of action IDs. The actions are found by looking up the ID in the
	 * {@link #getActionRegistry() action registry}. If the corresponding action
	 * is an {@link UpdateAction}, it will have its <code>update()</code> method
	 * called.
	 * 
	 * @param actionIds
	 *            the list of IDs to update
	 */
	@SuppressWarnings("rawtypes")
	protected void updateActions(List actionIds) {
		ActionRegistry registry = getActionRegistry();
		Iterator iter = actionIds.iterator();
		while (iter.hasNext()) {
			IAction action = registry.getAction(iter.next());
			if (action instanceof UpdateAction)
				((UpdateAction) action).update();
		}
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == CommandStack.class) {
			return getCommandStack();
		} else return super.getAdapter(adapter);
	}
}