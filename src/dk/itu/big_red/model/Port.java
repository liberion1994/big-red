package dk.itu.big_red.model;

import org.eclipse.draw2d.geometry.PointList;

import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IPort;
import dk.itu.big_red.utilities.geometry.Ellipse;
import dk.itu.big_red.utilities.geometry.Line;
import dk.itu.big_red.utilities.geometry.Rectangle;

/**
 * Ports are one of the two kinds of object that can be connected by an
 * {@link Edge} (the other being the {@link InnerName}). Ports are only ever found
 * on a {@link Node}, and inherit their name from a {@link Control}.
 * @author alec
 *
 */
public class Port extends Point implements IPort {
	/**
	 * The property name fired when this Port's {@link #segment} changes. The
	 * property values are {@link Integer}s.
	 */
	public static final String PROPERTY_SEGMENT = "PortSegment";
	
	/**
	 * The property name fired when this Port's {@link #distance} changes. The
	 * property values are {@link Double}s.
	 */
	public static final String PROPERTY_DISTANCE = "PortDistance";
	
	/**
	 * An integer index specifying the line segment on the parent {@link
	 * Node}'s polygon that this Port falls on. Together with {@link
	 * #distance}, it defines this Port's position.
	 * 
	 * <p>(If the {@link Control} defines an {@link Control.Shape#OVAL
	 * oval} appearance, this value will be <code>0</code>.)
	 */
	private int segment = 0;
	
	/**
	 * A value (<code>0 <= distance < 1</code>) specifying this Port's offset
	 * on its {@link #segment}. Together with <code>segment</code>, it defines
	 * this Port's position.
	 */
	private double distance = 0.0;
	
	public Port() {
		setLayout(new Rectangle(5, 5, 10, 10));
	}
	
	@Override
	public void setLayout(Rectangle newLayout) {
		super.setLayout(newLayout.setSize(10, 10));
	}
	
	public Port(String name, int segment, double distance) {
		setLayout(new Rectangle(5, 5, 10, 10));
		
		setName(name);
		setSegment(segment);
		setDistance(distance);
	}
	
	@Override
	public Node getParent() {
		return (Node)super.getParent();
	}
	
	/**
	 * Gets this Port's {@link #distance distance}.
	 * @see #distance
	 */
	public double getDistance() {
		return distance;
	}
	
	/**
	 * Sets this Port's {@link #distance distance}.
	 * @param distance the new distance value
	 * @see #distance
	 */
	public void setDistance(double distance) {
		if (distance >= 0 && distance < 1) {
			double oldDistance = this.distance;
			this.distance = distance;
			firePropertyChange(PROPERTY_DISTANCE, oldDistance, distance);
		}
	}

	/**
	 * Gets this Port's {@link #segment segment}.
	 * @see #segment
	 * @return
	 */
	public int getSegment() {
		return segment;
	}
	
	/**
	 * Sets this Port's {@link #segment segment}.
	 * 
	 * <p>Note that the segment value is <i>not</i> checked against the parent
	 * {@link Node}'s {@link Control} - users of this method must make sure
	 * they pass something sensible.
	 * @param segment the new segment value
	 * @see #segment
	 */
	public void setSegment(int segment) {
		int oldSegment = this.segment;
		this.segment = segment;
		firePropertyChange(PROPERTY_SEGMENT, oldSegment, segment);
	}
	
	@Override
	public Rectangle getLayout() {
		Rectangle r = super.getLayout().getCopy();
		PointList polypt = getParent().getFittedPolygon();
		if (polypt != null) {
			int segment = getSegment();
			org.eclipse.draw2d.geometry.Point p1 = polypt.getPoint(segment),
			      p2 = polypt.getPoint((segment + 1) % polypt.size());
			r.setLocation(new Line(p1, p2).
					getPointFromOffset(getDistance()).translate(-5, -5));
		} else {
			r.setLocation(
				new Ellipse(
						getParent().getLayout().getCopy().setLocation(0, 0)).
					getPointFromOffset(getDistance()).translate(-5, -5));
		}
		return r;
	}

	@Override
	public INode getINode() {
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
	
	@Override
	public Object getProperty(String name) {
		if (name.equals(PROPERTY_DISTANCE)) {
			return getDistance();
		} else if (name.equals(PROPERTY_SEGMENT)) {
			return getSegment();
		} else return super.getProperty(name);
	}
}
