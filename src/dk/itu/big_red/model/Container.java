package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.w3c.dom.Node;

import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.utilities.geometry.Rectangle;

/**
 * The <code>Container</code> is the superclass of anything which can contain
 * {@link Layoutable}s: {@link Bigraph}s, {@link Root}s, and {@link Node}s.
 * With the notable exception of {@link Bigraph}s, they can all be moved around
 * and resized.
 * @author alec
 *
 */
public abstract class Container extends Layoutable {
	protected abstract class ContainerChange
	extends dk.itu.big_red.model.Layoutable.LayoutableChange {
		@Override
		public Container getCreator() {
			return (Container)super.getCreator();
		}
	}
	
	public class ChangeAddChild extends ContainerChange {
		public Layoutable child;
		public String name;
		
		public ChangeAddChild(Layoutable child, String name) {
			this.child = child;
			this.name = name;
		}
		
		@Override
		public ContainerChange inverse() {
			return getCreator().changeRemoveChild(child);
		}
		
		@Override
		public boolean isReady() {
			return (child != null && name != null);
		}
		
		@Override
		public String toString() {
			return "Change(add child " + child + " to parent " + getCreator() + " with name \"" + name + "\")";
		}
	}
	
	public class ChangeRemoveChild extends ContainerChange {
		public Layoutable child;
		
		public ChangeRemoveChild(Layoutable child) {
			this.child = child;
		}
		
		private String oldName = null;
		
		@Override
		public void beforeApply() {
			oldName = child.getName();
		}
		
		@Override
		public boolean canInvert() {
			return (oldName != null);
		}
		
		@Override
		public ContainerChange inverse() {
			return getCreator().changeAddChild(child, oldName);
		}
		
		@Override
		public boolean isReady() {
			return (child != null);
		}
		
		@Override
		public String toString() {
			return "Change(remove child " + child + " from " + getCreator() + ")";
		}
	}
	
	protected ArrayList<Layoutable> children = new ArrayList<Layoutable>();

	/**
	 * The property name fired when a child is added or removed. The property
	 * values are {@link Layoutable}s.
	 */
	public static final String PROPERTY_CHILD = "ContainerChild";
	
	public boolean canContain(Layoutable child) {
		return false;
	}
	
	protected void addChild(Layoutable child) {
		if (children.contains(child))
			throw new RuntimeException("BUG: " + this + " already contains " + child);
		children.add(child);
		child.setParent(this);
		firePropertyChange(PROPERTY_CHILD, null, child);
	}
	
	protected void removeChild(Layoutable child) {
		boolean removed = children.remove(child);
		if (removed) {
			child.setParent(null);
			firePropertyChange(PROPERTY_CHILD, child, null);
		}
	}
	
	public List<Layoutable> getChildren() {
		return children;
	}

	public boolean hasChild(Layoutable child) {
		return children.contains(child);
	}
	
	@Override
	public Container clone(Map<ModelObject, ModelObject> m) {
		Container c = (Container)super.clone(m);
		
		for (Layoutable child : getChildren())
			c.addChild(child.clone(m));
		
		return c;
	}
	
	/**
	 * Creates {@link ContainerChange}s which will resize this object to a sensible
	 * default size and resize and reposition all of its children.
	 * @param cg a {@link ChangeGroup} to which changes should be appended
	 * @return the proposed new size of this object
	 */
	@Override
	protected Dimension relayout(ChangeGroup cg) {
		int maxHeight = 0;
		
		HashMap<Layoutable, Dimension> sizes =
				new HashMap<Layoutable, Dimension>();
		
		for (Layoutable i : getChildren()) {
			Dimension childSize = i.relayout(cg);
			sizes.put(i, childSize);
			if (childSize.height > maxHeight)
				maxHeight = childSize.height;
		}
		
		Rectangle nl = new Rectangle();
		
		int width = PADDING;
		
		for (Layoutable i : getChildren()) {
			Rectangle cl =
				new Rectangle().setSize(sizes.get(i));
			cl.setLocation(width,
					PADDING + ((maxHeight - cl.getHeight()) / 2));
			cg.add(i.changeLayout(cl));
			width += cl.getWidth() + PADDING;
		}
		
		if (width < 50)
			width = 50;
		
		Dimension r =
			new Dimension(width, maxHeight + (PADDING * 2));
		cg.add(changeLayout(nl.setSize(r)));
		return r;
	}
	
	public ContainerChange changeAddChild(Layoutable child, String name) {
		return new ChangeAddChild(child, name);
	}
	
	public ContainerChange changeRemoveChild(Layoutable child) {
		return new ChangeRemoveChild(child);
	}
	
	/**
	 * {@inheritDoc}
	 * <p><strong>Special notes for {@link Container}:</strong>
	 * <ul>
	 * <li>Passing {@link #PROPERTY_CHILD} will return a {@link List}&lt;{@link
	 * Layoutable}&gt;, <strong>not</strong> a {@link Layoutable}.
	 * </ul>
	 */
	@Override
	public Object getProperty(String name) {
		if (name.equals(PROPERTY_CHILD)) {
			return getChildren();
		} else return super.getProperty(name);
	}
}
