package dk.itu.big_red.part;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.EdgeConnection;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;

/**
 * PartFactories produce {@link EditPart}s from bigraph model objects.
 * @author alec
 *
 */
public class PartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		AbstractGraphicalEditPart part = null;
		
		if (model instanceof Bigraph) {
			part = new BigraphPart();
		} else if (model instanceof Node) {
			part = new NodePart();
        } else if (model instanceof Root) {
        	part = new RootPart();
        } else if (model instanceof Site) {
    		part = new SitePart();
        } else if (model instanceof EdgeConnection) {
        	part = new EdgeConnectionPart();
        } else if (model instanceof Edge) {
        	part = new EdgePart();
        } else if (model instanceof InnerName) {
        	part = new InnerNamePart();
        } else if (model instanceof Port) {
        	part = new PortPart();
        }
	       
		if (part != null)
			part.setModel(model);
		
		return part;
	}
}