package dk.itu.big_red.model.interfaces.pure;

public interface ILink extends IEntity {
	public Iterable<IPoint> getIPoints();
	
	public String getName();
	public void setName(String name);
}
