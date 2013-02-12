package org.bigraph.model.changes;

import java.util.ArrayList;
import java.util.Collection;

import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;

/**
 * A <strong>ChangeGroup</strong> is a collection of {@link IChange}s.
 * @author alec
 * @see ArrayList
 */
public class ChangeGroup extends ArrayList<IChange> implements IChange.Group {
	private static final long serialVersionUID = -5459931168098216972L;

	public ChangeGroup() {
		super();
	}
	
	public ChangeGroup(Collection<? extends IChange> c) {
		super(c);
	}

	public ChangeGroup(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public ChangeGroup clone() {
		return new ChangeGroup(this);
	}
	
	@Override
	public ChangeGroup inverse() {
		ChangeGroup changes = new ChangeGroup();
		for (IChange c : this)
			if (c != null)
				changes.add(0, c.inverse());
		return changes;
	}

	@Override
	public void simulate(PropertyScratchpad context, Resolver resolver) {
		for (IChange c : this)
			if (c != null)
				c.simulate(context, null);
	}

	@Override
	public void beforeApply() {
		/* do nothing */
	}
}
