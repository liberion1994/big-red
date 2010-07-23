package dk.itu.big_red.model.interfaces;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Bigraph;

/**
 * Objects implementing ILayoutable are model objects which have a graphical
 * representation:
 * 
 * <ul>
 * <li>they have a <i>layout</i> (a {@link Rectangle}) which defines their
 * bounding box and which can change under some circumstances;
 * <li>they have a <i>parent</i> (another {@link ILayoutable}) and a
 * <i>{@link Bigraph bigraph}</i>, which serve as their containers; and
 * <li>they may also optionally have <i>children</i> (other
 * {@link ILayoutable}s).
 * </ul>
 * 
 * <p>As they also implement {@link IPropertyChangeNotifier}, they can also
 * notify interested parties when these properties change.
 * @author alec
 *
 */
public interface ILayoutable extends IPropertyChangeNotifier {
	/**
	 * The property name fired when the ILayoutable's layout changes (i.e.,
	 * it's resized or moved).
	 */
	public static final String PROPERTY_LAYOUT = "ILayoutableLayout";
	
	/**
	 * The property name fired when a child is added or removed.
	 */
	public static final String PROPERTY_CHILD = "ILayoutableChild";
	
	/**
	 * The property name fired when the ILayoutable's parent changes.
	 */
	public static final String PROPERTY_PARENT = "ILayoutableParent";
	
	/**
	 * Gets a copy of the current layout of this object.
	 * @return the current layout
	 */
	public Rectangle getLayout();
	
	/**
	 * Gets the layout of this object relative to the top-left of the
	 * <i>root</i> rather than the immediate parent. (Like {@link
	 * ILayoutable#getLayout}, the object returned is newly created, and so can
	 * be safely modified.)
	 * @return the current layout relative to the root
	 */
	public Rectangle getRootLayout();
	
	/**
	 * Sets the layout of this object.
	 * 
	 * <p>Implementers are required not to store a reference to the {@link
	 * Rectangle} provided - its values should instead be copied into another
	 * structure.
	 * @param layout the new layout
	 */
	public void setLayout(Rectangle layout);
	
	/**
	 * Returns the {@link Bigraph} that contains this object.
	 * @return a Bigraph
	 */
	public Bigraph getBigraph();
	
	/**
	 * Returns the parent of this object.
	 * @return an {@link ILayoutable}
	 */
	public ILayoutable getParent();

	/**
	 * Changes the parent of this object.
	 * @param p the new parent {@link ILayoutable}
	 */
	public void setParent(ILayoutable p);
	
	/**
	 * Gets this object's children.
	 */
	public List<ILayoutable> getChildren();

	/**
	 * Adds a new child to this object.
	 * @param c an {@link ILayoutable}
	 */
	public void addChild(ILayoutable c);
	
	/**
	 * Removes an existing child from this object.
	 * @param c an {@link ILayoutable}
	 */
	public void removeChild(ILayoutable c);
	
	/**
	 * Indicates whether or not the given object is a child of this one.
	 * @param c an {@link ILayoutable}
	 * @return <code>true</code> if this object contains <code>c</code> as a
	 *         child, or <code>false</code> otherwise
	 */
	public boolean hasChild(ILayoutable c);
	
	/**
	 * Indicates whether or not this object can contain the specified {@link
	 * ILayoutable}.
	 * @param c an {@link ILayoutable}
	 * @return <code>true</code> if this object can contain <code>c</code>, or
	 *         <code>false</code> otherwise
	 */
	public boolean canContain(ILayoutable c);

	public ILayoutable clone() throws CloneNotSupportedException;
}
