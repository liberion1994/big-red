package dk.itu.big_red.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.model.assistants.ModelPropertySource;
import dk.itu.big_red.model.interfaces.internal.ICommentable;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;

/**
 * All of the objects which can actually appear on a bigraph are instances of
 * {@link LayoutableModelObject}. This extends {@link ModelObject} with
 * implementations of {@link ILayoutable}, {@link ICommentable}, and {@link
 * IAdaptable}.
 * @author alec
 * @see ModelObject
 *
 */
public abstract class LayoutableModelObject extends ModelObject implements IAdaptable, ILayoutable, ICommentable {
	protected Rectangle layout;
	protected Container parent;
	
	public LayoutableModelObject() {
		layout = new Rectangle(10, 10, 100, 100);
	}
	
	@Override
	public Rectangle getLayout() {
		return new Rectangle(layout);
	}

	@Override
	public Rectangle getRootLayout() {
		return getLayout().getCopy().translate(getParent().getRootLayout().getTopLeft());
	}

	@Override
	public void setLayout(Rectangle newLayout) {
		Rectangle oldLayout = layout;
		layout = newLayout;
		firePropertyChange(PROPERTY_LAYOUT, oldLayout, layout);
	}

	@Override
	public Bigraph getBigraph() {
		return getParent().getBigraph();
	}

	@Override
	public Container getParent() {
		return parent;
	}

	/**
	 * Changes the parent of this object.
	 * @param p the new parent {@link Container}
	 */
	protected void setParent(Container parent) {
		this.parent = parent;
	}
	
	private String comment = null;
	
	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(String comment) {
		String oldComment = this.comment;
		this.comment = comment;
		firePropertyChange(PROPERTY_COMMENT, oldComment, comment);
	}
	
	private ModelPropertySource propertySource;
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			if (propertySource == null)
				propertySource = new ModelPropertySource(this);
			return propertySource;
		}
		return null;
	}
	
	@Override
	public abstract LayoutableModelObject clone() throws CloneNotSupportedException;
}
