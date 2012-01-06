package dk.itu.big_red.model.assistants;

import org.eclipse.gef.requests.CreationFactory;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Site;

/**
 * The ModelFactory class creates {@link ModelObject}s on demand.
 * @author alec
 *
 */
public class ModelFactory implements CreationFactory {
	private Class<? extends ModelObject> type;
	
	/**
	 * Creates a new {@link ModelFactory}, ready to produce objects of the
	 * given type.
	 * @param type a {@link Class} (extending {@link ModelObject})
	 */
	public ModelFactory(Class<? extends ModelObject> type) {
		this.type = type;
	}
	
	@Override
	public ModelObject getNewObject() {
		if (type != null) {
			try {
				return type.newInstance();
			} catch (IllegalAccessException e) {
				return null;
			} catch (InstantiationException e) {
				return null;
			}
		} else return null;
	}
	
	@Override
	public Class<? extends ModelObject> getObjectType() {
		return type;
	}

	/**
	 * Creates a new object of the named type.
	 * @param typeName a type name (not case sensitive)
	 * @return a new object of the appropriate type, or <code>null</code> if
	 *          the type name was unrecognised
	 * @see ModelObject#getType()
	 */
	public static ModelObject getNewObject(String typeName) {
		typeName = typeName.toLowerCase();
		if (typeName.equals("bigraph"))
			return new Bigraph();
		else if (typeName.equals("root"))
			return new Root();
		else if (typeName.equals("site"))
			return new Site();
		else if (typeName.equals("innername"))
			return new InnerName();
		else if (typeName.equals("outername"))
			return new OuterName();
		else if (typeName.equals("signature"))
			return new Signature();
		else if (typeName.equals("port"))
			return new Port();
		else if (typeName.equals("control"))
			return new Control();
		else if (typeName.equals("edge"))
			return new Edge();
		else return null;
	}
}
