package dk.itu.big_red.model.changes;

import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;

public class BigraphChangeDisconnect extends Change {
	public Point point;
	public Link link;
	
	public BigraphChangeDisconnect(Point point, Link link) {
		this.point = point;
		this.link = link;
	}

	@Override
	public Change inverse() {
		return new BigraphChangeConnect(point, link);
	}
}