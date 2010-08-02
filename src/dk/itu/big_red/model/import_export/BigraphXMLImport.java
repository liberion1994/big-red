package dk.itu.big_red.model.import_export;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.exceptions.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.ModelFactory;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.model.interfaces.INameable;
import dk.itu.big_red.util.DOM;
import dk.itu.big_red.util.Project;

/**
 * XMLImport reads a XML document and produces a corresponding {@link Bigraph}.
 * @author alec
 * @see BigraphXMLExport
 *
 */
public class BigraphXMLImport extends ModelImport<Bigraph> {
	private HashMap<String, ArrayList<Point>> potentialConnections =
		new HashMap<String, ArrayList<Point>>();
	
	@Override
	public Bigraph importObject() throws ImportFailedException {
		try {
			Document d = DOM.parse(source);
			return (Bigraph)process(null, d.getDocumentElement());
		} catch (Exception e) {
			throw new ImportFailedException(e);
		}
	}
	
	private void registerConnection(String linkName, Point object) {
		Link actualLink =
			(Link)bigraph.getNamespaceManager().getObject(Link.class, linkName);
		if (actualLink == null) {
			ArrayList<Point> connections = potentialConnections.get(linkName);
			if (connections == null) {
				connections = new ArrayList<Point>();
				potentialConnections.put(linkName, connections);
			}
			connections.add(object);
		} else {
			actualLink.addPoint(object);
		}
	}
	
	private void getPendingConnections(Link object) {
		ArrayList<Point> connections = potentialConnections.get(object.getName());
		if (connections == null)
			return;
		for (Point p : connections)
			object.addPoint(p);
		potentialConnections.remove(object.getName());
	}
	
	private Bigraph bigraph = null;
	
	private void processBigraph(Element e, Bigraph model) throws ImportFailedException {
		String signaturePath = e.getAttribute("signature");
		
		IFile sigFile =
			Project.findFileByPath(null, new Path(signaturePath));
		SignatureXMLImport si = new SignatureXMLImport();
		try {
			si.setInputStream(sigFile.getContents());
			model.setSignature(sigFile, si.importObject());
		} catch (Exception ex) {
			throw new ImportFailedException(ex);
		}
		
    	bigraph = model;
		processThing(e, model);
	}
	
	private void processThing(Element e, Thing model) throws ImportFailedException {
		Element el = DOM.removeNamedChildElement(e, "big-red:appearance");
		if (el != null)
			AppearanceGenerator.setAppearance(el, model);
		
		if (model instanceof Node) {
			Node node = (Node)model;
			node.setControl(bigraph.getSignature().getControl(DOM.getAttribute(e, "control")));
		} else if (model instanceof INameable) {
			bigraph.getNamespaceManager().setName(model.getClass(), DOM.getAttribute(e, "name"), model);
		}
		
		for (int j = 0; j < e.getChildNodes().getLength(); j++) {
			if (!(e.getChildNodes().item(j) instanceof Element))
				continue;
			Object i = process(model, (Element)e.getChildNodes().item(j));
			if (i instanceof ILayoutable)
				model.addChild((ILayoutable)i);
		}
	}
	
	private void processPort(Element e, Port model) throws ImportFailedException {
		String name = DOM.getAttribute(e, "name"),
	           link = DOM.getAttribute(e, "link");
		model.setName(name);
		
		registerConnection(link, model);
	}
	
	private boolean processLink(Element e, Link model) throws ImportFailedException {
		boolean rv = false;
		model.setName(DOM.getAttribute(e, "name"));
		
		getPendingConnections(model);
		
		Element el = DOM.removeNamedChildElement(e, "big-red:appearance");
		if (el != null)
			AppearanceGenerator.setAppearance(el, model);
		return rv;
	}
	
	private void processInnerName(Element e, InnerName model) throws ImportFailedException {
		Element el = DOM.removeNamedChildElement(e, "big-red:appearance");
		if (el != null)
			AppearanceGenerator.setAppearance(el, model);

		String name = DOM.getAttribute(e, "name"),
        link = DOM.getAttribute(e, "link");
		model.setName(name);
		
		registerConnection(link, model);
	}
	
	private Object process(ILayoutable context, Element e) throws ImportFailedException {
		Object model = ModelFactory.getNewObject(e.getNodeName());
		
		if (model instanceof ILayoutable)
			((ILayoutable)model).setParent(context);
		
		if (model instanceof Bigraph) {
			processBigraph(e, (Bigraph)model);
		} else if (model instanceof Thing) {
			processThing(e, (Thing)model);
		} else if (model instanceof Port) {
			if (context instanceof Node) {
				Node n = (Node)context;
				for (Port p : n.getPorts()) {
					if (p.getName().equals(e.getAttribute("name"))) {
						processPort(e, p);
						break;
					}
				}
			}
			model = null;
		} else if (model instanceof Link) {
			processLink(e, (Link)model);
		} else if (model instanceof InnerName) {
			processInnerName(e, (InnerName)model);
		}
		
		return model;
	}
}
