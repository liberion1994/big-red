package it.uniud.bigredit.command;

import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.model.Reaction;


import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.changes.ChangeGroup;



public class LayoutableCreateCommand extends ChangeCommand {
	ChangeGroup cg = new ChangeGroup();
	
	public LayoutableCreateCommand() {
		setChange(cg);
	}
	
	private Rectangle layout = null;
	private ModelObject container = null;
	private ModelObject node = null;
//	
//	@Override
//	public boolean canExecute(){
//		System.out.println(super.canExecute());
//		return true;
//	}
//	
	
	
	@Override
	public LayoutableCreateCommand prepare() {
		cg.clear();
		if (layout == null || container == null || node == null)
			return this;
		
		if (container instanceof Bigraph){
			setTarget(((Bigraph) container).getBigraph());
		}else if(container instanceof BRS){
			setTarget((BRS)container);
			
		}else if(container instanceof Layoutable){
			setTarget(((Layoutable)container).getBigraph());
		}else if (container instanceof Reaction){
			
		}
		
		if (container instanceof Container) {
			
			for (Layoutable i : ((Container) container).getChildren()) {
				if (i instanceof Edge)
					continue;
				else if (ExtendedDataUtilities.getLayout(i).intersects(layout))
					return this;
			}
		}
		if (container instanceof Bigraph)
			/* enforce boundaries */;
		
		if (container instanceof Bigraph) {
			if (node instanceof Root){
				System.out.println("instance of root");
				String name = ((Bigraph) container).getBigraph().getFirstUnusedName((Layoutable)node);
				cg.add(((Bigraph) container).changeAddChild(((Root)node), name), ExtendedDataUtilities.changeLayout(((Layoutable)node), layout));
			}else{
				String name = ((Bigraph) container).getBigraph().getFirstUnusedName((Layoutable)node);
				cg.add(((Bigraph) container).changeAddChild(((Layoutable)node), name), ExtendedDataUtilities.changeLayout(((Layoutable)node), layout));
			}
			/** TODO add name */
			//String name = ((Bigraph) container).getBigraph().getFirstUnusedName((Layoutable)node);
			//cg.add(((Bigraph) container).changeAddChild(((Layoutable)node), name), ((Layoutable)node).changeLayout(layout));
			//cg.add(((Bigraph) container).changeAddChild(((Layoutable)node), "R0"), ((Layoutable)node).changeLayout(layout));
		}
		if (container instanceof BRS){
			/** TODO get a name for Bigraph */
			System.out.println("Instance of BRS");
			setTarget((BRS)container);
			cg.add(((BRS)container).changeAddChild(node, "B0"),
			((BRS)container).changeLayoutChild(node, layout));
			
		}
		
		if (container instanceof Reaction){
			/** TODO get a name for Bigraph */
			System.out.println("Instance of Reaction");
			setTarget((Reaction)container);
			if(layout.x > ((Reaction)container).SEPARATOR_WIDTH){
				cg.add(((Reaction) container).changeAddReactum((Bigraph) node),
						((Reaction) container).changeLayoutChild(
								(Bigraph) node, layout));
			}else{
				cg.add(((Reaction) container).changeAddRedex((Bigraph) node),
						((Reaction) container).changeLayoutChild(
								(Bigraph) node, layout));
			}
		}
		return this;
	}
	
	public void setObject(Object s) {
		if (s instanceof Layoutable){
			node = (Layoutable)s;
		}
		
		if (s instanceof ModelObject){
			node = (ModelObject)s;
		}
	}
	
	public void setContainer(Object e) {

		if (e instanceof Container){
			container = (Container)e;
		}else if(e instanceof ModelObject){
			System.out.println("instanceof ModelObject");
			container = (ModelObject)e;
		}
	}
	
	public void setLayout(Object r) {
		if (r instanceof Rectangle)
			layout = (Rectangle)r;

	}

}
