package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.figure.PortFigure;
import dk.itu.big_red.figure.adornments.CentreAnchor;
import dk.itu.big_red.model.EdgeConnection;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.interfaces.ICommentable;
import dk.itu.big_red.model.interfaces.IConnectable;
import dk.itu.big_red.model.interfaces.ILayoutable;

/**
 * PortParts represent {@link Port}s, sites on {@link Node}s which can be
 * connected to {@link Edge}s.
 * @see Port
 * @author alec
 *
 */
public class PortPart extends AbstractPart implements NodeEditPart, PropertyChangeListener {
	@Override
	public Port getModel() {
		return (Port)super.getModel();
	}
	
	@Override
	public void activate() {
		super.activate();
		getModel().getParent().addPropertyChangeListener(this);
	}

	@Override
	public void deactivate() {
		getModel().getParent().removePropertyChangeListener(this);
		super.deactivate();
	}
	
	@Override
	protected IFigure createFigure() {
		return new PortFigure();
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		Object source = evt.getSource();
		if (source == getModel()) {
			if (prop.equals(IConnectable.PROPERTY_SOURCE_EDGE)) {
				refreshSourceConnections();
				refreshVisuals();
		    } else if (prop.equals(ICommentable.PROPERTY_COMMENT)) {
		    	refreshVisuals();
		    }
		} else if (source == getModel().getParent()) {
			if (prop.equals(ILayoutable.PROPERTY_LAYOUT))
				refreshVisuals();
		}
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		setResizable(false);
		
		Port model = getModel();
		PortFigure figure = (PortFigure)getFigure();
		
		Rectangle r = model.getLayout();
		figure.setConstraint(r);
		
		String toolTip = model.getName();
		List<EdgeConnection> l = model.getConnections();
		if (l.size() != 0)
			toolTip += "\n(connected to " + l.get(0).getParent() + ")";
		if (model.getComment() != null)
			toolTip += "\n\n" + model.getComment();
		figure.setToolTip(toolTip);
	}
	
	@Override
	protected List<EdgeConnection> getModelSourceConnections() {
        return getModel().getConnections();
    }
	
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new CentreAnchor(getFigure());
    }
    
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new CentreAnchor(getFigure());
    }
	
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new CentreAnchor(getFigure());
    }
    
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new CentreAnchor(getFigure());
    }
}
