package dk.itu.big_red.model.changes.bigraph;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.changes.Change;

public class BigraphChangeLayout extends Change {
	public LayoutableModelObject model;
	public Rectangle newLayout;
	
	public BigraphChangeLayout(LayoutableModelObject model, Rectangle newLayout) {
		this.model = model;
		this.newLayout = newLayout;
	}

	private Rectangle oldLayout;
	@Override
	public void beforeApply() {
		oldLayout = model.getLayout();
	}
	
	@Override
	public Change inverse() {
		return new BigraphChangeLayout(model, oldLayout);
	}
	
	@Override
	public boolean canInvert() {
		return (oldLayout != null);
	}
	
	@Override
	public boolean isReady() {
		return (model != null && newLayout != null);
	}
}
