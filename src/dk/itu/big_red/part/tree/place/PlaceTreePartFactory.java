package dk.itu.big_red.part.tree.place;



import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import dk.itu.big_red.model.*;



public class PlaceTreePartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null; 
	    
		if (model instanceof Bigraph) {
			part = new BigraphPlaceTreePart();
		} else if (model instanceof Node) {
            part = new NodePlaceTreePart();
        } else if (model instanceof Root) {
        	part = new RootPlaceTreePart();
        } else if (model instanceof Site) {
    		part = new SitePlaceTreePart();
        } else if (model instanceof InnerName) {
        	part = new NamePlaceTreePart();
        }
	    
		if (part != null)
			part.setModel(model);
		
		return part;
	}

}
