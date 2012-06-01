package dk.itu.big_red.wizards.creation.assistants;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import dk.itu.big_red.model.Signature;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog.Mode;
import dk.itu.big_red.utilities.ui.ResourceSelector;
import dk.itu.big_red.utilities.ui.ResourceSelector.ResourceListener;
import dk.itu.big_red.utilities.ui.UI;

public class WizardNewAgentCreationPage extends WizardPage {
	private IStructuredSelection selection = null;
	
	private IFile signature;
	private IContainer folder;
	
	private Text nameText = null;
	
	public WizardNewAgentCreationPage(String pageName, IStructuredSelection selection) {
		super(pageName);
		this.selection = selection;
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new GridLayout(2, false));
		
		ModifyListener sharedModifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		};
		
		UI.chain(new Label(root, 0)).text("&Parent folder:").done();
		
		ResourceSelector folderSelector =
				new ResourceSelector(root, null, Mode.CONTAINER);
		folderSelector.addListener(new ResourceListener() {
			@Override
			public void resourceChanged(IResource oldValue, IResource newValue) {
				if (newValue instanceof IContainer) {
					setFolder((IContainer)newValue);
				} else setFolder(null);
			}
		});
		folderSelector.getButton().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, false));
		
		UI.chain(new Label(root, SWT.NONE)).text("&Signature:").done();
		
		ResourceSelector signatureSelector =
				new ResourceSelector(root, null, Mode.FILE,
						Signature.CONTENT_TYPE);
		signatureSelector.addListener(new ResourceListener() {
			@Override
			public void resourceChanged(IResource oldValue, IResource newValue) {
				if (newValue instanceof IFile) {
					setSignature((IFile)newValue);
				} else setSignature(null);
			}
		});
		signatureSelector.getButton().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, false));
		
		new Label(root, SWT.HORIZONTAL | SWT.SEPARATOR).setLayoutData(
				new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
		
		UI.chain(new Label(root, SWT.NONE)).text("&Name:").done();
		
		nameText = new Text(root, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		nameText.addModifyListener(sharedModifyListener);
		
		setControl(root);
	}
	
	private void setFolder(IContainer c) {
		folder = c;
		validate();
	}
	
	public IContainer getFolder() {
		return folder;
	}
	
	private void setSignature(IFile f) {
		signature = f;
		validate();
	}
	
	public IFile getSignature() {
		return signature;
	}
	
	public String getFileName() {
		return nameText.getText().concat(".bigraph-agent");
	}
	
	private boolean validate() {
		setPageComplete(false);
		
		if (getFolder() == null) {
			setErrorMessage("Parent folder is empty.");
			return false;
		} else if (folder instanceof IWorkspaceRoot) {
			setErrorMessage("Parent is not a folder.");
			return false;
		}

		if (getSignature() == null) {
			setErrorMessage("Signature is empty.");
			return false;
		} else {
			IContentType t;
			try {
				t = getSignature().getContentDescription().getContentType();
			} catch (CoreException e) {
				t = null;
			}
			if (t == null || !t.getId().equals(Signature.CONTENT_TYPE)) {
				setErrorMessage("Signature has the wrong content type.");
				return false;
			}
		}
		
		String nT = nameText.getText().trim();
		
		if (nT.length() == 0) {
			setErrorMessage("Name is empty.");
			return false;
		}
		
		String proposedFileName = nT + ".bigraph-agent";
		
		IPath p = folder.getFullPath().makeRelative();
		if (!p.isValidSegment(proposedFileName)) {
			setErrorMessage("Name contains invalid characters.");
			return false;
		} else {
			p.append(proposedFileName);
			if (Project.findFileByPath(null, p) != null) {
				setErrorMessage("Name already exists.");
				return false;
			}
		}
		
		setPageComplete(true);
		setErrorMessage(null);
		return true;
	}
}
