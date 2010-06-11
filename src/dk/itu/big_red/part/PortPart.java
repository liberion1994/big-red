package dk.itu.big_red.part;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.EllipseAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.figure.NodeFigure;
import dk.itu.big_red.figure.PortFigure;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.EdgeConnection;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.interfaces.ILayoutable;

public class PortPart extends AbstractGraphicalEditPart implements NodeEditPart {
	@Override
	public Port getModel() {
		return (Port)super.getModel();
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
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		Port model = getModel();
		PortFigure figure = (PortFigure)getFigure();
		
		figure.setLayout(model.getLayout());
	}
	
	@Override
	protected List<EdgeConnection> getModelSourceConnections() {
        return new ArrayList<EdgeConnection>();
    }
    
	@Override
	protected List<EdgeConnection> getModelTargetConnections() {
        return new ArrayList<EdgeConnection>();
    }
	
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new EllipseAnchor(getFigure());
    }
    
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new EllipseAnchor(getFigure());
    }
	
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new EllipseAnchor(getFigure());
    }
    
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new EllipseAnchor(getFigure());
    }
}
