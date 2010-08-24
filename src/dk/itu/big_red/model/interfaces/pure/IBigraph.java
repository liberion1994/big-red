package dk.itu.big_red.model.interfaces.pure;

public interface IBigraph {
	public Iterable<IEdge> getIEdges();
	public Iterable<IRoot> getIRoots();
	public Iterable<IOuterName> getIOuterNames();
}
