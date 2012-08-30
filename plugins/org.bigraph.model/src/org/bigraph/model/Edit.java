package org.bigraph.model;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

@Deprecated
public class Edit extends ModelObject
		implements IChangeDescriptor, IChangeExecutor {
	@RedProperty(fired = Edit.class, retrieved = Edit.class)
	public static final String PROPERTY_PARENT = "EditParent";
	private Edit parent;
	
	public Edit getParent() {
		return parent;
	}
	
	public Edit getParent(PropertyScratchpad context) {
		return (Edit)getProperty(context, PROPERTY_PARENT);
	}
	
	protected void setParent(Edit parent) {
		Edit oldParent = this.parent;
		this.parent = parent;
		firePropertyChange(PROPERTY_PARENT, oldParent, parent);
	}
	
	@RedProperty(fired = IChangeDescriptor.class,
			retrieved = ChangeDescriptorGroup.class)
	public static final String PROPERTY_CHILD = "EditChildren";
	private ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
	
	public ChangeDescriptorGroup getChildren() {
		return cdg;
	}
	
	public ChangeDescriptorGroup getChildren(PropertyScratchpad context) {
		return (ChangeDescriptorGroup)getProperty(context, PROPERTY_CHILD);
	}
	
	protected ChangeDescriptorGroup getModifiableChildren(
			PropertyScratchpad context) {
		if (context.hasProperty(this, PROPERTY_CHILD)) {
			return (ChangeDescriptorGroup)
					context.getProperty(this, PROPERTY_CHILD);
		} else {
			ChangeDescriptorGroup cdg = getChildren().clone();
			context.setProperty(this, PROPERTY_CHILD, cdg);
			return cdg;
		}
	}
	
	protected void addDescriptor(int index, IChangeDescriptor cd) {
		cdg.add(index, cd);
		firePropertyChange(PROPERTY_CHILD, null, cd);
	}
	
	protected void removeDescriptor(int index) {
		IChangeDescriptor cd = cdg.remove(index);
		firePropertyChange(PROPERTY_CHILD, cd, null);
	}
	
	protected abstract class EditChange extends ModelObjectChange {
		@Override
		public Edit getCreator() {
			return Edit.this;
		}
	}
	
	public class ChangeAddChild extends EditChange {
		public final int index;
		public final IChangeDescriptor descriptor;
		
		public ChangeAddChild(int index, IChangeDescriptor descriptor) {
			this.index = index;
			this.descriptor = descriptor;
		}
		
		@Override
		public ChangeRemoveChild inverse() {
			return new ChangeRemoveChild(index, descriptor);
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			getCreator().getModifiableChildren(context).add(index, descriptor);
			if (descriptor instanceof Edit)
				context.setProperty(descriptor, PROPERTY_PARENT, getCreator());
		}
	}
	
	public class ChangeRemoveChild extends EditChange {
		public final int index;
		public final IChangeDescriptor descriptor;
		
		public ChangeRemoveChild(int index, IChangeDescriptor descriptor) {
			this.index = index;
			this.descriptor = descriptor;
		}
		
		@Override
		public ChangeAddChild inverse() {
			return new ChangeAddChild(index, descriptor);
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			getCreator().getModifiableChildren(context).remove(index);
			if (descriptor instanceof Edit)
				context.setProperty(descriptor, PROPERTY_PARENT, null);
		}
	}
	
	@Override
	public IChange createChange(PropertyScratchpad context, Resolver r)
			throws ChangeCreationException {
		return cdg.createChange(context, r);
	}

	@Override
	public void tryValidateChange(IChange b) throws ChangeRejectedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tryApplyChange(IChange b) throws ChangeRejectedException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_CHILD.equals(name)) {
			return getChildren();
		} else if (PROPERTY_PARENT.equals(name)) {
			return getParent();
		} else return super.getProperty(name);
	}
	
	public IChange changeAddChild(int index,
			IChangeDescriptor descriptor) {
		return new ChangeAddChild(index, descriptor);
	}
	
	public IChange changeRemoveChild(int index, IChangeDescriptor descriptor) {
		return new ChangeRemoveChild(index, descriptor);
	}
}