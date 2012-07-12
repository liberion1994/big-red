package org.bigraph.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import org.bigraph.model.Layoutable.Identifier;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

/**
 * This is the superclass of everything in Big Red's version of the bigraphical
 * model. It allows {@link PropertyChangeListener}s to register for, and
 * unregister from, change notifications, and has a {@link String} comment
 * which can be set and retrieved.
 * @author alec
 * @see Layoutable
 *
 */
public abstract class ModelObject {
	public abstract class ModelObjectChange extends Change {
		/**
		 * Gets the {@link ModelObject} which created this {@link ModelObjectChange}.
		 * @return
		 */
		public ModelObject getCreator() {
			return ModelObject.this;
		}
	}
	
	public interface ExtendedDataValidator {
		void validate(ChangeExtendedData c, PropertyScratchpad context)
			throws ChangeRejectedException;
	}
	
	public class ChangeExtendedData extends ModelObjectChange {
		public String key;
		public Object newValue;
		public ExtendedDataValidator immediateValidator, finalValidator;
		
		protected ChangeExtendedData(String key, Object newValue,
				ExtendedDataValidator immediateValidator,
				ExtendedDataValidator finalValidator) {
			this.key = key;
			this.newValue = newValue;
			this.immediateValidator = immediateValidator;
			this.finalValidator = finalValidator;
		}
		
		private Object oldValue;
		
		@Override
		public void beforeApply() {
			oldValue = getCreator().getExtendedData(key);
		}
		
		@Override
		public Change inverse() {
			return new ChangeExtendedData(
					key, oldValue, immediateValidator, finalValidator);
		}
		
		@Override
		public String toString() {
			return "Change(set extended data field " + key + " of " +
					getCreator() + " to " + newValue + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			context.setProperty(getCreator(), key, newValue);
		}
	}
	
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	/**
	 * Registers a {@link PropertyChangeListener} to receive property change
	 * notifications from this object.
	 * @param listener the PropertyChangeListener
	 */
	public final void addPropertyChangeListener(
			PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	/**
	 * Unregisters a {@link PropertyChangeListener} from receiving property
	 * change notifications from this object.
	 * @param listener the PropertyChangeListener
	 */
	public final void removePropertyChangeListener(
			PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

	/**
	 * Notifies all associated {@link PropertyChangeListener}s of a property
	 * change.
	 * @param propertyName the ID of the changed property
	 * @param oldValue its old value
	 * @param newValue its new value
	 */
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		listeners.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	/**
	 * Returns a new instance of this {@link ModelObject}'s class,
	 * created as though by <code>this.getClass().newInstance()</code>.
	 * @return a new instance of this ModelObject's class, or
	 * <code>null</code>
	 */
	public ModelObject newInstance() {
		try {
			return getClass().newInstance();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Creates and returns a new copy of this {@link ModelObject}.
	 * <p>(Although the returned copy is a {@link ModelObject}, it's
	 * really an instance of whatever subclass this object is.)
	 * @param m a {@link CloneMap} to be notified of the new copy, or
	 * <code>null</code>
	 * @return a new copy of this {@link ModelObject}
	 */
	public ModelObject clone(Map<ModelObject, ModelObject> m) {
		ModelObject i = newInstance();
		if (m != null)
			m.put(this, i);
		i.setExtendedDataFrom(this);
		return i;
	}
	
	@Override
	public ModelObject clone() {
		return clone(null);
	}
	
	protected Object getProperty(String name) {
		return null;
	}
	
	protected Object getProperty(PropertyScratchpad context, String name) {
		if (context == null || !context.hasProperty(this, name)) {
			return getProperty(name);
		} else return context.getProperty(this, name);
	}
	
	@Override
	public String toString() {
		return "<" + getType() + "@" + System.identityHashCode(this) + ">";
	}
	
	/**
	 * Returns the name of this object's type.
	 * @return the name, as a {@link String}
	 */
	public String getType() {
		return getClass().getSimpleName();
	}
	
	public Change changeExtendedData(String key, Object newValue) {
		return changeExtendedData(key, newValue, null);
	}
	
	public Change changeExtendedData(
			String key, Object newValue, ExtendedDataValidator validator) {
		return changeExtendedData(key, newValue, validator, null);
	}
	
	public Change changeExtendedData(String key, Object newValue,
			ExtendedDataValidator immediateValidator,
			ExtendedDataValidator finalValidator) {
		return new ChangeExtendedData(
				key, newValue, immediateValidator, finalValidator);
	}
	
	public void dispose() {
		PropertyChangeListener[] pls =
			listeners.getPropertyChangeListeners().clone();
		for (PropertyChangeListener i : pls)
			listeners.removePropertyChangeListener(i);
		listeners = null;
		
		if (extendedData != null) {
			extendedData.clear();
			extendedData = null;
		}
	}
	
	private Map<String, Object> extendedData;
	
	/**
	 * Retrieves a piece of extended data from this object.
	 * @param key a key
	 * @return an {@link Object}, or <code>null</code> if the key has no
	 * associated data
	 */
	public Object getExtendedData(String key) {
		return (extendedData != null ? extendedData.get(key) : null);
	}
	
	/**
	 * Adds a piece of extended data to this object.
	 * @param key a key
	 * @param value an {@link Object} to associate with the key, or
	 * <code>null</code> to remove an existing association
	 */
	public void setExtendedData(String key, Object value) {
		if (key == null)
			return;
		Object oldValue;
		if (value == null) {
			if (extendedData == null)
				return;
			if ((oldValue = extendedData.remove(key)) != null) {
				if (extendedData.isEmpty())
					extendedData = null;
			}
		} else {
			if (extendedData == null)
				extendedData = new HashMap<String, Object>();
			oldValue = extendedData.put(key, value);
		}
		firePropertyChange(key, oldValue, value);
	}
	
	/**
	 * Overwrites this object's extended data with the data from another
	 * object.
	 * @param m a {@link ModelObject} (can be <code>null</code>)
	 */
	protected void setExtendedDataFrom(ModelObject m) {
		if (m != null && m.extendedData != null) {
			extendedData = new HashMap<String, Object>(m.extendedData);
		} else extendedData = null;
	}
	
	protected boolean doChange(Change c_) {
		c_.beforeApply();
		if (c_ instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)c_)
				if (!doChange(c))
					throw new Error("Couldn't apply " + c +
							" (how did it pass validation?)");
		} else if (c_ instanceof ChangeExtendedData) {
			ChangeExtendedData c = (ChangeExtendedData)c_;
			c.getCreator().setExtendedData(c.key, c.newValue);
		} else return false;
		return true;
	}
	
	public static abstract class Identifier {
		public interface Resolver {
			Object lookup(
					PropertyScratchpad context, Object type, String name);
		}
		
		private final String name;
		
		public Identifier(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		protected static <T> T require(Object o, Class<? extends T> klass) {
			return (klass.isInstance(o) ? klass.cast(o) : null);
		}
		
		@Override
		public boolean equals(Object obj_) {
			return safeClassCmp(this, obj_) &&
					safeEquals(getName(), ((Identifier)obj_).getName());
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(getClass(), getName());
		}
		
		public abstract ModelObject lookup(
				PropertyScratchpad context, Resolver r);
	}
	
	public static class ChangeExtendedDataDescriptor
			implements IChangeDescriptor {
		private final Identifier target;

		private final String key;
		private final Object newValue;
		private final ExtendedDataValidator immediateValidator, finalValidator;

		public ChangeExtendedDataDescriptor(
				Identifier target, String key, Object newValue,
				ExtendedDataValidator immediateValidator,
				ExtendedDataValidator finalValidator) {
			this.target = target;
			this.key = key;
			this.newValue = newValue;
			this.immediateValidator = immediateValidator;
			this.finalValidator = finalValidator;
		}

		public Identifier getTarget() {
			return target;
		}

		public String getKey() {
			return key;
		}

		public Object getNewValue() {
			return newValue;
		}

		@Override
		public boolean equals(Object obj_) {
			if (safeClassCmp(this, obj_)) {
				ChangeExtendedDataDescriptor obj =
						(ChangeExtendedDataDescriptor)obj_;
				return
						safeEquals(getTarget(), obj.getTarget()) &&
						safeEquals(getKey(), obj.getKey()) &&
						safeEquals(getNewValue(), obj.getNewValue());
			} else return false;
		}

		@Override
		public int hashCode() {
			return compositeHashCode(
					ChangeExtendedDataDescriptor.class, target, key, newValue);
		}

		@Override
		public Change createChange(
				PropertyScratchpad context, Resolver r) {
			return target.lookup(context, r).changeExtendedData(
					key, newValue, immediateValidator, finalValidator);
		}

		@Override
		public String toString() {
			return "ChangeDescriptor(set extended data field " + key + " of " +
					target + " to " + newValue + ")"; 
		}
	}
	
	public static boolean safeClassCmp(Object o1, Object o2) {
		return safeEquals(
				o1 != null ? o1.getClass() : null,
				o2 != null ? o2.getClass() : null);
	}
	
	public static boolean safeEquals(Object o1, Object o2) {
		return (o1 != null ? o1.equals(o2) : o2 == null);
	}
	
	public static int compositeHashCode(Object... objs) {
		if (objs != null && objs.length > 0) {
			int total = 123;
			for (Object i : objs)
				total += (i != null ? i.hashCode() : 0);
			return total;
		} else return 0;
	}
}
