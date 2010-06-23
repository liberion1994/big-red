package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import dk.itu.big_red.figure.AbstractFigure;
import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.ILayoutable;

public abstract class AbstractPart extends AbstractGraphicalEditPart implements PropertyChangeListener {
	@Override
	public Thing getModel() {
		return (Thing)super.getModel();
	}
	
	public void activate() {
		super.activate();
		getModel().addPropertyChangeListener(this);
	}

	public void deactivate() {
		getModel().removePropertyChangeListener(this);
		super.deactivate();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		/* 
		 * TutoGEF suggests putting this in each of the *Part classes, but it
		 * makes much more sense to have it in the base, surely? 
		 */
	    if (evt.getPropertyName().equals(Thing.PROPERTY_LAYOUT) ||
	    	evt.getPropertyName().equals(Thing.PROPERTY_RENAME)) {
	    	refreshVisuals();
	    } else if (evt.getPropertyName().equals(Thing.PROPERTY_CHILD)) {
	    	refreshChildren();
	    }
	}
	
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		
		AbstractFigure figure = (AbstractFigure)getFigure();
		Thing model = getModel();
		
		figure.setToolTip(model.getClass().getSimpleName());
		
		figure.setConstraint(model.getLayout());
		figure.setRootConstraint(model.getRootLayout());
		
		for (Object i : getChildren())
			((EditPart)i).refresh();
	}
	
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

	@Override
	protected List<EdgeConnection> getModelSourceConnections() {
        return new ArrayList<EdgeConnection>();
    }
    
	@Override
	protected List<EdgeConnection> getModelTargetConnections() {
        return new ArrayList<EdgeConnection>();
    }
	
	@Override
	public List<ILayoutable> getModelChildren() {
		return new ArrayList<ILayoutable>();
	}
}
