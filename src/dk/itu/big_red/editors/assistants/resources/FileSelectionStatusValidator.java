package dk.itu.big_red.editors.assistants.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

import dk.itu.big_red.intro.Activator;

/**
 * FileSelectionStatusValidators only validate selections which contain a
 * single {@link IFile}.
 * @author alec
 *
 */
public class FileSelectionStatusValidator implements ISelectionStatusValidator {
	public static final IStatus
		OK_STATUS = new Status(Status.OK, Activator.PLUGIN_ID, ""),
		CANCEL_STATUS = new Status(Status.CANCEL, Activator.PLUGIN_ID, "");
	
	@Override
	public IStatus validate(Object[] selection) {
		IStatus r = OK_STATUS;
		if (selection.length != 1 || !(selection[0] instanceof IFile))
			r = CANCEL_STATUS;
		return r;
	}

}
