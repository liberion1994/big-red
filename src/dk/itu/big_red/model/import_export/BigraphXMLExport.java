package dk.itu.big_red.model.import_export;

import org.w3c.dom.Element;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.XMLExport;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.util.DOM;
import dk.itu.big_red.util.Utility;

/**
 * XMLExport writes a {@link Bigraph} out as an XML document.
 * @author alec
 * @see BigraphXMLImport
 *
 */
public class BigraphXMLExport extends XMLExport<Bigraph> {
	/**
	 * An array of model {@link Class}es in the appropriate order for the
	 * <code>&lt;bigraph&gt;</code> XML schema, suitable for giving as the
	 * last argument to {@link Utility#groupListByClass(java.util.List,
	 * Object...)}.
	 */
	public static final Object SCHEMA_ORDER[] = {
		Edge.class, OuterName.class, Root.class, InnerName.class,
		Port.class, Node.class, Site.class
	};
	
	private boolean exportAppearance = true,
			exportPersistentID = true;
	
	public static final String
		OPTION_APPEARANCE = "BigraphXMLExportAppearance",
		OPTION_PERSISTENT_ID = "BigraphXMLExportPersistentID";
	
	{
		addOption(OPTION_APPEARANCE, "Export Big Red-specific appearance data");
		addOption(OPTION_PERSISTENT_ID, "Export a persistent ID for each object");
	}
	
	@Override
	public Object getOption(String id) {
		if (id.equals(OPTION_APPEARANCE)) {
			return exportAppearance;
		} else if (id.equals(OPTION_PERSISTENT_ID)) {
			return exportPersistentID;
		} else return super.getOption(id);
	}
	
	@Override
	public void setOption(String id, Object value) {
		if (id.equals(OPTION_APPEARANCE)) {
			exportAppearance = (Boolean)value;
		} else if (id.equals(OPTION_PERSISTENT_ID)) {
			exportPersistentID = (Boolean)value;
		} else super.setOption(id, value);
	}
	
	private Element elem(String name) {
		return newElement(XMLNS.BIGRAPH, "bigraph:" + name);
	}
	
	@Override
	public void exportObject() throws ExportFailedException {
		setDocument(DOM.createDocument(XMLNS.BIGRAPH, "bigraph:bigraph"));
		processBigraph(getDocumentElement(), getModel());
		finish();
	}

	private Element processSignature(Signature s) throws ExportFailedException {
		Element e = elem("signature");
		if (s.getFile() != null) {
			DOM.applyAttributes(e,
				"src", s.getFile().getFullPath().makeRelative().toString());	
		} else {
			throw new ExportFailedException("Bigraphs with an embedded signature are currently read-only.");
		}
		return e;
	}
	
	public Element processBigraph(Element e, Bigraph obj) throws ExportFailedException {
		if (exportAppearance || exportPersistentID)
			DOM.applyAttributes(getDocumentElement(), "xmlns:big-red", XMLNS.BIG_RED);
		DOM.appendChildIfNotNull(e, processSignature(obj.getSignature()));
		
		for (Layoutable i :
			Utility.groupListByClass(obj.getChildren(),
					BigraphXMLExport.SCHEMA_ORDER)) {
			Element f = null;
			if (i instanceof Edge) {
				f = newElement(XMLNS.BIGRAPH, "bigraph:edge");
			} else if (i instanceof OuterName) {
				f = newElement(XMLNS.BIGRAPH, "bigraph:outername");
			} else if (i instanceof Root) {
				f = processRoot(
						newElement(XMLNS.BIGRAPH, "bigraph:root"), (Root)i);
			} else if (i instanceof InnerName) {
				f = processPoint(
						newElement(XMLNS.BIGRAPH, "bigraph:innername"), (Point)i);
			}
			DOM.appendChildIfNotNull(e, applyCommonProperties(f, i));
		}
		
		return e;
	}
	
	private Element processRoot(Element e, Root r) throws ExportFailedException {
		for (Layoutable i :
			Utility.groupListByClass(r.getChildren(),
					BigraphXMLExport.SCHEMA_ORDER)) {
			Element f = null;
			if (i instanceof Node) {
				f = processNode(
						newElement(XMLNS.BIGRAPH, "bigraph:node"), (Node)i);
			} else if (i instanceof Site) {
				f = processSite(
						newElement(XMLNS.BIGRAPH, "bigraph:site"), (Site)i);
			}
			DOM.appendChildIfNotNull(e, applyCommonProperties(f, i));
		}
		return e;
	}
	
	private Element processSite(Element e, Site s) {
		return e;
	}
	
	private Element processNode(Element e, Node n) throws ExportFailedException {
		e.setAttributeNS(null, "control", n.getControl().getLongName());
		e.setAttributeNS(null, "name", n.getName());
		
		for (Port p : n.getPorts()) 
			DOM.appendChildIfNotNull(e, process(p));
		
		return e;
	}
	
	private Element processPoint(Element e, Point p) throws ExportFailedException {
		Link link = p.getLink();
		if (link != null) {
			return DOM.applyAttributes(
					e,
					"name", p.getName(),
					"link", link.getName());
		} else if (p instanceof InnerName) {
			throw new ExportFailedException("Inner name \"" + p.getName() + "\" isn't connected to anything.");
		}
		return null;
	}
		
	private Element applyCommonProperties(Element e, Layoutable l) {
		if (!(l instanceof Bigraph))
			DOM.applyAttributes(e, "name", l.getName());
		if (exportAppearance)
			DOM.appendChildIfNotNull(e, AppearanceGenerator.getAppearance(getDocument(), l));
		if (exportPersistentID)
			DOM.applyAttributesNS(e, XMLNS.BIG_RED, "big-red:pid", l.getPersistentID());
		return e;
	}
	
	private Element process(Layoutable obj) throws ExportFailedException {
		Element e = null;
		if (obj instanceof Node) {
			e = process(obj);
		} else if (obj instanceof Point) {
			e = process(obj);
		} else {
			e = elem(obj.getClass().getSimpleName().toLowerCase());
		}
		
		if (!(obj instanceof Bigraph))
			DOM.applyAttributes(e, "name", obj.getName());
		
		if (obj instanceof Container) {
			Container c = (Container)obj;
			for (Layoutable i : Utility.groupListByClass(c.getChildren(),
					BigraphXMLExport.SCHEMA_ORDER))
				e.appendChild(process(i));
		}
		
		
					
		return e;
	}
}
