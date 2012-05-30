package dk.itu.big_red.editors.bigraph.commands;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.changes.ChangeGroup;

public class LayoutableRelayoutCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	public LayoutableRelayoutCommand() {
		setChange(cg);
	}
	
	private Layoutable model;
	private Rectangle layout;
	
	public void setLayout(Object rect) {
		if (rect instanceof Rectangle) {
			layout = (Rectangle)rect;
			if (layout.width < 10)
				layout.width = 10;
			if (layout.height < 10)
				layout.height = 10;
		}
	}

	public void setModel(Object model) {
		if (model instanceof Layoutable && !(model instanceof Bigraph))
			this.model = (Layoutable)model;
	}
	
	@Override
	public LayoutableRelayoutCommand prepare() {
		cg.clear();
		if (model == null || layout == null)
			return this;
		setTarget(model.getBigraph());
		if ((model instanceof Edge || noOverlap()) && boundariesSatisfied())
			cg.add(ExtendedDataUtilities.changeLayout(model, layout));
		return this;
	}
	
	public boolean noOverlap() {
		for (Layoutable i : model.getParent().getChildren()) {
			if (i instanceof Edge || i == model)
				continue;
			else if (ExtendedDataUtilities.getLayout(i).intersects(layout))
				return false;
		}
		return true;
	}
	
	private boolean boundariesSatisfied() {
		if (!(model.getParent() instanceof Bigraph))
			return true;
		return true; /* XXX */
	}
}
