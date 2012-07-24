package org.bigraph.model;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.names.Namespace;

/**
 * All of the objects which can actually appear as part of a bigraph are
 * instances of <strong>Layoutable</strong>.
 * @author alec
 * @see ModelObject
 *
 */
public abstract class Layoutable extends ModelObject {
	/**
	 * The property name fired when the name changes.
	 */
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PROPERTY_NAME = "LayoutableName";
	
	/**
	 * The property name fired when the parent changes.
	 */
	@RedProperty(fired = Container.class, retrieved = Container.class)
	public static final String PROPERTY_PARENT = "LayoutableParent";
	
	abstract class LayoutableChange extends ModelObjectChange {
		@Override
		public Layoutable getCreator() {
			return Layoutable.this;
		}
	}

	public class ChangeName extends LayoutableChange {
		public String newName;
		
		protected ChangeName(String newName) {
			this.newName = newName;
		}

		private String oldName;
		@Override
		public void beforeApply() {
			oldName = getCreator().getName();
		}
		
		@Override
		public ChangeName inverse() {
			return new ChangeName(oldName);
		}
		
		@Override
		public boolean canInvert() {
			return (oldName != null);
		}
		
		@Override
		public boolean isReady() {
			return (newName != null);
		}
		
		@Override
		public String toString() {
			return "Change(set name of " + getCreator() + " to " + newName + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			Namespace<Layoutable> ns =
				getBigraph(context).getNamespace(Bigraph.getNSI(getCreator()));
			
			ns.remove(context, getName(context));
			context.setProperty(
					getCreator(), Layoutable.PROPERTY_NAME, newName);
			ns.put(context, newName, getCreator());
		}
	}
	
	public class ChangeRemove extends LayoutableChange {
		private String oldName;
		private Container oldParent;
		@Override
		public void beforeApply() {
			oldName = getCreator().getName();
			oldParent = getCreator().getParent();
		}
		
		@Override
		public boolean canInvert() {
			return (oldName != null && oldParent != null);
		}
		
		@Override
		public Change inverse() {
			return oldParent.new ChangeAddChild(getCreator(), oldName);
		}
		
		@Override
		public String toString() {
			return "Change(remove child " + getCreator() + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			Layoutable l = getCreator();
			Container c = l.getParent(context);
			
			context.<Layoutable>getModifiableList(
					c, Container.PROPERTY_CHILD, c.getChildren()).
				remove(l);
			context.setProperty(l, Layoutable.PROPERTY_PARENT, null);
			
			c.getBigraph(context).getNamespace(Bigraph.getNSI(l)).
				remove(context, l.getName(context));
			context.setProperty(l, Layoutable.PROPERTY_NAME, null);
		}
	}
	
	private Container parent = null;
	
	/**
	 * Returns the {@link Bigraph} that ultimately contains this object.
	 * @return a Bigraph
	 */
	public Bigraph getBigraph() {
		return getBigraph(null);
	}

	public Bigraph getBigraph(PropertyScratchpad context) {
		if (getParent(context) == null) {
			return null;
		} else return getParent(context).getBigraph(context);
	}
	
	/**
	 * Returns the parent of this object.
	 * @return an {@link Container}
	 */
	public Container getParent() {
		return parent;
	}

	public Container getParent(PropertyScratchpad context) {
		return (Container)getProperty(context, PROPERTY_PARENT);
	}
	
	/**
	 * Changes the parent of this object.
	 * @param p the new parent {@link Container}
	 */
	void setParent(Container parent) {
		Container oldParent = this.parent;
		this.parent = parent;
		firePropertyChange(PROPERTY_PARENT, oldParent, parent);
	}
	
	protected Layoutable clone(Bigraph m) {
		Layoutable l = (Layoutable)super.clone();
		l.setName(getName());
		m.getNamespace(Bigraph.getNSI(l)).put(getName(), l);
		return l;
	}
	
	private String name = null;
	
	/**
	 * Gets this object's name.
	 * @return a String
	 */
	public String getName() {
		return name;
	}
	
	public String getName(PropertyScratchpad context) {
		return (String)getProperty(context, PROPERTY_NAME);
	}
	
	/**
	 * Sets this object's name.
	 * @param name the new name for this object
	 */
	protected void setName(String name) {
		String oldName = this.name;
		this.name = name;
		firePropertyChange(PROPERTY_NAME, oldName, name);
	}
	
	public LayoutableChange changeName(String newName) {
		return new ChangeName(newName);
	}
	
	public LayoutableChange changeRemove() {
		return new ChangeRemove();
	}
	
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_NAME.equals(name)) {
			return getName();
		} else if (PROPERTY_PARENT.equals(name)) {
			return getParent();
		} else return super.getProperty(name);
	}
	
	@Override
	public void dispose() {
		name = null;
		parent = null;
		
		super.dispose();
	}
	
	public abstract Identifier getIdentifier();
	public abstract Identifier getIdentifier(PropertyScratchpad context);
	
	public static abstract class Identifier extends ModelObject.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public abstract Layoutable lookup(
				PropertyScratchpad context, Resolver r);
		
		@Override
		public abstract Identifier getRenamed(String name);
	}
	
	public static class ChangeNameDescriptor implements IChangeDescriptor {
		private final Identifier target;
		private final String newName;
		
		public ChangeNameDescriptor(Identifier target, String newName) {
			this.target = target;
			this.newName = newName;
		}
		
		public Identifier getTarget() {
			return target;
		}
		
		public String getNewName() {
			return newName;
		}
		
		@Override
		public boolean equals(Object obj_) {
			if (safeClassCmp(this, obj_)) {
				ChangeNameDescriptor obj = (ChangeNameDescriptor)obj_;
				return
						safeEquals(getTarget(), obj.getTarget()) &&
						safeEquals(getNewName(), obj.getNewName());
			} else return false;
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(
					ChangeNameDescriptor.class, target, newName);
		}
		
		@Override
		public Change createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			Layoutable l = target.lookup(context, r);
			if (l == null)
				throw new ChangeCreationException(this,
						"" + target + " didn't resolve to a Layoutable");
			return l.changeName(newName);
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(set name of " + target + " to " + 
					newName + ")";
		}
	}
	
	public static class ChangeRemoveDescriptor implements IChangeDescriptor {
		private final Identifier target;
		private final Container.Identifier parent;
		
		public ChangeRemoveDescriptor(Identifier target) {
			this.target = target;
			parent = null;
		}
		
		public ChangeRemoveDescriptor(
				Identifier target, Container.Identifier parent) {
			this.target = target;
			this.parent = parent;
		}
		
		public Identifier getTarget() {
			return target;
		}
		
		public Container.Identifier getParent() {
			return parent;
		}
		
		@Override
		public boolean equals(Object obj_) {
			return safeClassCmp(this, obj_) &&
					safeEquals(getTarget(),
							((ChangeRemoveDescriptor)obj_).getTarget());
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(ChangeRemoveDescriptor.class, target);
		}
		
		@Override
		public Change createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			Layoutable l = target.lookup(context, r);
			if (l == null)
				throw new ChangeCreationException(this,
						"" + target + " didn't resolve to a Layoutable");
			return l.changeRemove();
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(remove child " + target + ")";
		}
	}
}
