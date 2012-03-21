package dk.itu.big_red.editors.bigraph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import dk.itu.big_red.model.Control;

public class NodeFigure extends AbstractFigure {
	private Control.Shape shape = Control.Shape.POLYGON;
	private PointList points = Control.POINTS_QUAD;
	private Label labelControl = new Label();    
    
	public NodeFigure() {
		super();
		
		labelControl.setForegroundColor(ColorConstants.black);
		add(labelControl, 0);
		setConstraint(labelControl, new Rectangle(1, 1, -1, -1));
	}
	
	public void setLabel(String text) {
		labelControl.setText(text);
	}
	
	public void setPoints(PointList points) {
		this.points = points;
	}
	
	public PointList getPoints() {
		return points;
	}
	
	public void setShape(Control.Shape shape) {
		this.shape = shape;
	}
	
	public Control.Shape getShape() {
		return shape;
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			switch (shape) {
			case OVAL:
				graphics.fillOval(1, 1, a.width - 1, a.height - 1);
				break;
			case POLYGON:
				graphics.fillPolygon(points);
				break;
			}
		} finally {
			stop(graphics);
		}
	}
	
	@Override
	protected void outlineShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.setLineWidth(2);
			graphics.setLineStyle(SWT.LINE_SOLID);
			
			switch (shape) {
			case OVAL:
				graphics.drawOval(1, 1, a.width - 2, a.height - 2);
				break;
			case POLYGON:
				graphics.drawPolygon(points);
				break;
			}
		} finally {
			stop(graphics);
		}
	}
}