package dk.itu.big_red.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;

public class EdgeFigure extends AbstractFigure {
	public EdgeFigure() {
		super();
		
		setFillColour(new RGB(0, 127, 0));
	}
	
	public void setToolTip(String content) {
		String labelText = "Edge";
		if (content != null)
			labelText += "\n\n" + content;
		super.setToolTip(labelText);
	}
	
	@Override
	protected void fillShape(Graphics graphics) {
		Rectangle a = start(graphics);
		try {
			graphics.setAlpha(32);
			graphics.fillRectangle(a);
		} finally {
			stop(graphics);
		}
	}

	@Override
	protected void outlineShape(Graphics graphics) {
	}
}
