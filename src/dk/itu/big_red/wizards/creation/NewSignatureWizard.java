package dk.itu.big_red.wizards.creation;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.import_export.SignatureXMLExport;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.ui.UI;

public class NewSignatureWizard extends Wizard implements INewWizard {
	private WizardNewFileCreationPage page = null;
	
	@Override
	public boolean performFinish() {
		IContainer c =
			Project.findContainerByPath(null, page.getContainerFullPath());
		if (c != null) {
			try {
				IFile sigFile = Project.getFile(c, page.getFileName());
				NewSignatureWizard.createSignature(sigFile);
				UI.openInEditor(sigFile);
				return true;
			} catch (CoreException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (ExportFailedException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			}
		}
		return false;
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page = new WizardNewFileCreationPage("newSignatureWizardPage", selection);
		
		page.setTitle("Signature");
		page.setDescription("Create a new signature in an existing bigraphical reactive system.");
		page.setFileExtension("bigraph-signature");
		
		addPage(page);
	}

	public static void createSignature(IFile sigFile) throws ExportFailedException, CoreException {
		IOAdapter io = new IOAdapter();
		
		new SignatureXMLExport().setModel(new Signature()).setOutputStream(io.getOutputStream()).exportObject();
		sigFile.setContents(io.getInputStream(), 0, null);
	}
}
