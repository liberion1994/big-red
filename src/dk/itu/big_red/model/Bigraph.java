package dk.itu.big_red.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.assistants.NamespaceManager;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeable;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeAddChild;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeConnect;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeDisconnect;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeEdgeReposition;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeLayout;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeRemoveChild;
import dk.itu.big_red.model.interfaces.IBigraph;
import dk.itu.big_red.model.interfaces.IEdge;
import dk.itu.big_red.model.interfaces.IInnerName;
import dk.itu.big_red.model.interfaces.IOuterName;
import dk.itu.big_red.model.interfaces.IRoot;
import dk.itu.big_red.model.interfaces.ISignature;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.util.HomogeneousIterable;
import dk.itu.big_red.util.resources.ResourceWrapper;

/**
 * The Bigraph is the root of any agent, and contains {@link Root}s, {@link
 * InnerName}s, and {@link OuterName}s.
 * @author alec
 * @see IBigraph
 */
public class Bigraph extends Container implements IBigraph, IChangeable {
	protected ResourceWrapper<Signature> signature =
		new ResourceWrapper<Signature>();
	protected NamespaceManager namespaceManager = new NamespaceManager();
	
	/**
	 * The property name fired when the <i>lower outer name</i> boundary is
	 * changed.
	 */
	public static final String
		PROPERTY_BOUNDARY_LON = "BigraphBoundaryLowerOuterName";
	
	/**
	 * The property name fired when the <i>upper root</i> boundary is changed.
	 */
	public static final String
		PROPERTY_BOUNDARY_UR = "BigraphBoundaryUpperRoot";
	
	/**
	 * The property name fired when the <i>lower root</i> boundary is changed.
	 */
	public static final String
		PROPERTY_BOUNDARY_LR = "BigraphBoundaryLowerRoot";
	
	/**
	 * The property name fired when the <i>upper inner name</i> boundary is
	 * changed.
	 */
	public static final String
		PROPERTY_BOUNDARY_UIN = "BigraphBoundaryUpperInnerName";
	
	protected static int BOUNDARY_MARGIN = 20;
	protected int upperRootBoundary = Integer.MIN_VALUE + BOUNDARY_MARGIN,
	              lowerOuterNameBoundary = Integer.MAX_VALUE - BOUNDARY_MARGIN,
	              upperInnerNameBoundary = Integer.MIN_VALUE + BOUNDARY_MARGIN,
	              lowerRootBoundary = Integer.MAX_VALUE - BOUNDARY_MARGIN;
	
	@Override
	public Container clone() throws CloneNotSupportedException {
		return new Bigraph()._overwrite(this);
	}
	
	/**
	 * Gets the {@link NamespaceManager} for this bigraph.
	 * @return a NamespaceManager
	 */
	public NamespaceManager getNamespaceManager() {
		return namespaceManager;
	}
	
	@Override
	public void applyChange(Change b) {
		if (!validateChange(b))
			return;
		doChange(b);
	}
	
	private ChangeRejectedException lastRejection = null;
	
	@Override
	public boolean validateChange(Change b) {
		try {
			tryValidateChange(b);
		} catch (ChangeRejectedException e) {
			lastRejection = e;
			return false;
		}
		return true;
	}
	
	@Override
	public ChangeRejectedException getLastRejection() {
		return lastRejection;
	}
	
	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b) {
				tryValidateChange(c);
			}
		} else if (b instanceof BigraphChangeConnect) {
			BigraphChangeConnect c = (BigraphChangeConnect)b;
			if (c.point.getLink() != null)
				throw new ChangeRejectedException(this, b, this,
					"Connections can only be established to Points that " +
					"aren't already connected");
		}
	}
	
	private void doChange(Change b) {
		b.beforeApply();
		if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b)
				doChange(c);
		} else if (b instanceof BigraphChangeConnect) {
			BigraphChangeConnect c = (BigraphChangeConnect)b;
			c.link.addPoint(c.point);
		} else if (b instanceof BigraphChangeDisconnect) {
			BigraphChangeDisconnect c = (BigraphChangeDisconnect)b;
			c.link.removePoint(c.point);
		} else if (b instanceof BigraphChangeAddChild) {
			BigraphChangeAddChild c = (BigraphChangeAddChild)b;
			c.parent.addChild(c.child);
		} else if (b instanceof BigraphChangeRemoveChild) {
			BigraphChangeRemoveChild c = (BigraphChangeRemoveChild)b;
			c.parent.removeChild(c.child);
		} else if (b instanceof BigraphChangeLayout) {
			BigraphChangeLayout c = (BigraphChangeLayout)b;
			c.model.setLayout(c.newLayout);
		} else if (b instanceof BigraphChangeEdgeReposition) {
			BigraphChangeEdgeReposition c = (BigraphChangeEdgeReposition)b;
			c.edge.averagePosition();
		}
	}
	
	@Override
	public boolean canContain(ILayoutable child) {
		Class<? extends ILayoutable> c = child.getClass();
		return (c == Root.class || c == InnerName.class || c == OuterName.class);
	}
	
	@Override
	public Bigraph getBigraph() {
		return this;
	}
	
	public void setSignature(IFile file, Signature signature) {
		if (file != null && signature != null) {
			this.signature.setResource(file);
			this.signature.setModel(signature);
		}
	}
	
	public Signature getSignature() {
		return signature.getModel();
	}
	
	public IFile getSignatureFile() {
		return signature.getResource();
	}
	
	@Override
	public void setParent(Container parent) {
		/* do nothing */
	}
	
	@Override
	public void setLayout(Rectangle newLayout) {
		super.setLayout(newLayout);
		updateBoundaries();
	}
	
	@Override
	public Container getParent() {
		return null;
	}
	
	@Override
	public Rectangle getRootLayout() {
		return new Rectangle();
	}
	
	@Override
	public void addChild(LayoutableModelObject child) {
		super.addChild(child);
		updateBoundaries();
	}
	
	@Override
	public void removeChild(LayoutableModelObject child) {
		super.removeChild(child);
		updateBoundaries();
	}
	
	/**
	 * Recalculates the boundaries that govern the placement of {@link
	 * OuterName}s, {@link Root}s, and {@link InnerName}s.
	 */
	public void updateBoundaries() {
		int oldLON = upperRootBoundary,
		    oldHR = lowerOuterNameBoundary,
		    oldLR = upperInnerNameBoundary,
		    oldHIN = lowerRootBoundary;
		upperRootBoundary = Integer.MIN_VALUE;
		lowerOuterNameBoundary = Integer.MAX_VALUE;
		upperInnerNameBoundary = Integer.MIN_VALUE;
		lowerRootBoundary = Integer.MAX_VALUE;
		
		for (ILayoutable i : children) {
			int top = i.getLayout().getTopLeft().y,
				bottom = i.getLayout().getBottomLeft().y;
			if (i instanceof OuterName) {
				if (bottom > upperRootBoundary)
					upperRootBoundary = bottom;
			} else if (i instanceof Root) {
				if (top < lowerOuterNameBoundary)
					lowerOuterNameBoundary = top;
				if (bottom > upperInnerNameBoundary)
					upperInnerNameBoundary = bottom;
			} else if (i instanceof InnerName) {
				if (top < lowerRootBoundary)
					lowerRootBoundary = top;
			}
		}
		
		lowerOuterNameBoundary -= BOUNDARY_MARGIN;
		upperRootBoundary += BOUNDARY_MARGIN;
		lowerRootBoundary -= BOUNDARY_MARGIN;
		upperInnerNameBoundary += BOUNDARY_MARGIN;
		
		if (oldHR != lowerOuterNameBoundary)
			firePropertyChange(PROPERTY_BOUNDARY_LON, oldHR, lowerOuterNameBoundary);
		if (oldLON != upperRootBoundary)
			firePropertyChange(PROPERTY_BOUNDARY_UR, oldLON, upperRootBoundary);
		if (oldHIN != lowerRootBoundary)
			firePropertyChange(PROPERTY_BOUNDARY_LR, oldHIN, lowerRootBoundary);
		if (oldLR != upperInnerNameBoundary)
			firePropertyChange(PROPERTY_BOUNDARY_UIN, oldLR, upperInnerNameBoundary);
	}
	
	public int getLowerOuterNameBoundary() {
		return lowerOuterNameBoundary;
	}
	
	public int getUpperRootBoundary() {
		return upperRootBoundary;
	}
	
	public int getLowerRootBoundary() {
		return lowerRootBoundary;
	}
	
	public int getUpperInnerNameBoundary() {
		return upperInnerNameBoundary;
	}

	@Override
	public Iterable<IEdge> getIEdges() {
		return new HomogeneousIterable<IEdge>(children, IEdge.class);
	}

	@Override
	public Iterable<IRoot> getIRoots() {
		return new HomogeneousIterable<IRoot>(children, IRoot.class);
	}

	@Override
	public Iterable<IInnerName> getIInnerNames() {
		return new HomogeneousIterable<IInnerName>(children, IInnerName.class);
	}
	
	@Override
	public Iterable<IOuterName> getIOuterNames() {
		return new HomogeneousIterable<IOuterName>(children, IOuterName.class);
	}

	@Override
	public ISignature getISignature() {
		return signature.getModel();
	}
}
