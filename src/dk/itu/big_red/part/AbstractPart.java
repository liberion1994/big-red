package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.model.interfaces.IPropertyChangeNotifier;

/**
 * The AbstractPart is the base class for most of the objects in the bigraph
 * model. It provides sensible default implementations of the abstract methods
 * from {@link AbstractGraphicalEditPart}, and also some generally-useful
 * functionality, like receiving property notifications from model objects.
 * @author alec
 *
 */
public abstract class AbstractPart extends AbstractGraphicalEditPart implements PropertyChangeListener {
	/**
	 * Gets the model object, cast to an {@link IPropertyChangeNotifier}.
	 */
	@Override
	public IPropertyChangeNotifier getModel() {
		return (IPropertyChangeNotifier)super.getModel();
	}
	
	/**
	 * Extends {@link AbstractGraphicalEditPart#activate()} to also register to
	 * receive property change notifications from the model object.
	 */
	@Override
	public void activate() {
		super.activate();
		getModel().addPropertyChangeListener(this);
	}

	/**
	 * Extends {@link AbstractGraphicalEditPart#activate()} to also unregister
	 * from the model object's property change notifications.
	 */
	@Override
	public void deactivate() {
		getModel().removePropertyChangeListener(this);
		super.deactivate();
	}
	
	/**
	 * Checks to see if this {@link EditPart}'s <code>PRIMARY_DRAG_ROLE</code>
	 * {@link EditPolicy} is a {@link ResizableEditPolicy}, and - if it is -
	 * reconfigures it to allow or forbid resizing.
	 * @param resizable whether or not this Part should be resizable
	 */
	protected void setResizable(boolean resizable) {
		EditPolicy pol = getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
		if (pol instanceof ResizableEditPolicy) {
			((ResizableEditPolicy)pol).setResizeDirections(
				(resizable ? PositionConstants.NSEW : 0));
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		refresh();
	}

	/**
	 * Returns an empty list of {@link EdgeConnection}s. Subclasses which
	 * implement {@link IConnectable} should probably override this method!
	 */
	@Override
	protected List<EdgeConnection> getModelSourceConnections() {
        return new ArrayList<EdgeConnection>();
    }

	/**
	 * Returns an empty list of {@link EdgeConnection}s. Subclasses which
	 * implement {@link IConnectable} should probably override this method!
	 */
	@Override
	protected List<EdgeConnection> getModelTargetConnections() {
        return new ArrayList<EdgeConnection>();
    }

	/**
	 * Returns an empty list of {@link ILayoutable}s. Model objects with
	 * children should probably override this method!
	 */
	@Override
	public List<ILayoutable> getModelChildren() {
		return new ArrayList<ILayoutable>();
	}
	
	/**
	 * Handles {@link RequestConstants#REQ_OPEN} requests by opening the
	 * property sheet.
	 */
	@Override
	public void performRequest(Request req) {
		if (req.getType().equals(RequestConstants.REQ_OPEN)) {
			try {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				page.showView(IPageLayout.ID_PROP_SHEET);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}
}
