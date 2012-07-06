package org.bigraph.model;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.interfaces.INode;
import org.bigraph.model.interfaces.IPort;

/**
 * Ports are one of the two kinds of object that can be connected by an
 * {@link Edge} (the other being the {@link InnerName}). Ports are only ever found
 * on a {@link Node}, and inherit their name from a {@link Control}.
 * @author alec
 *
 */
public class Port extends Point implements IPort {
	private PortSpec spec;
	
	private void setSpec(PortSpec spec) {
		this.spec = spec;
	}
	
	public PortSpec getSpec() {
		return spec;
	}
	
	public Port(PortSpec i) {
		setSpec(i);
	}
	
	@Override
	public Node getParent() {
		return (Node)super.getParent();
	}
	
	@Override
	public Node getParent(PropertyScratchpad context) {
		return (Node)super.getParent(context);
	}
	
	@Override
	public String getName() {
		return getSpec().getName();
	}

	@Override
	public INode getNode() {
		return getParent();
	}
	
	/**
	 * This method should never be called; {@link Port}s are created only when
	 * a {@link Control} is given to a {@link Node}.
	 */
	@Override
	public Point clone() {
		return null;
	}
	
	public static final class Identifier extends Point.Identifier {
		private final Node.Identifier node;
		
		public Identifier(String name, Node.Identifier node) {
			super(name);
			this.node = node;
		}
		
		public Node.Identifier getNode() {
			return node;
		}
		
		@Override
		public Port lookup(Bigraph universe, PropertyScratchpad context) {
			Node n = getNode().lookup(universe, context);
			if (n != null) {
				return n.getPort(getName());
			} else return null;
		}
	}
	
	@Override
	public Identifier getIdentifier() {
		return getIdentifier(null);
	}
	
	@Override
	public Identifier getIdentifier(PropertyScratchpad context) {
		return new Identifier(getName(),
				getParent(context).getIdentifier(context));
	}
}
