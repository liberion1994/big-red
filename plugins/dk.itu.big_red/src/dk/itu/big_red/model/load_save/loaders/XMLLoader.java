package dk.itu.big_red.model.load_save.loaders;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.bigraph.model.loaders.IXMLUndecorator;
import org.bigraph.model.loaders.LoadFailedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import dk.itu.big_red.application.plugin.RedPlugin;

public abstract class XMLLoader extends org.bigraph.model.loaders.XMLLoader {
	public static final String EXTENSION_POINT = "dk.itu.big_red.xml";
	
	public XMLLoader() {
		IExtensionRegistry r = RegistryFactory.getRegistry();
		for (IConfigurationElement ice :
			r.getConfigurationElementsFor(EXTENSION_POINT)) {
			if ("undecorator".equals(ice.getName())) {
				try {
					IXMLUndecorator u = (IXMLUndecorator)
							ice.createExecutableExtension("class");
					addUndecorator(u);
				} catch (CoreException e) {
					e.printStackTrace();
					/* do nothing */
				}
			}
		}
	}
	
	/**
	 * Validates the given {@link Document} with the {@link Schema} constructed
	 * from the given {@link InputStream}.
	 * @param d a Document
	 * @param schema an InputStream
	 * @return <code>d</code>, for convenience
	 * @throws LoadFailedException if the validation (or the validator's
	 *         initialisation and configuration) failed
	 */
	protected static Document validate(Document d, String schema)
			throws LoadFailedException {
		try {
			getSharedSchemaFactory().newSchema(
					new StreamSource(RedPlugin.getResource(schema))).
				newValidator().validate(new DOMSource(d));
			return d;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}

	/**
	 * Attempts to parse the specified {@link InputStream} into a DOM {@link
	 * Document}.
	 * @param is an InputStream, which will be closed &mdash; even in the
	 * event of an exception
	 * @return a Document
	 * @throws SAXException as {@link DocumentBuilder#parse(File)}
	 * @throws IOException as {@link DocumentBuilder#parse(File)} or
	 * {@link InputStream#close}
	 * @throws ParserConfigurationException as {@link
	 * DocumentBuilderFactory#newDocumentBuilder()}
	 */
	protected static Document parse(InputStream is)
			throws SAXException, IOException {
		try {
			return getSharedDocumentBuilder().parse(is);
		} finally {
			is.close();
		}
	}

	/**
	 * Returns all the child {@link Node}s of the specified {@link Element}
	 * which are themselves {@link Element}s.
	 * @param e an Element containing children
	 * @return a list of child {@link Element}s
	 */
	public static List<Element> getChildElements(Element e) {
		ArrayList<Element> children = new ArrayList<Element>();
		int length = e.getChildNodes().getLength();
		for (int h = 0; h < length; h++) {
			Node i = e.getChildNodes().item(h);
			if (i instanceof Element)
				children.add((Element)i);
		}
		return children;
	}

	/**
	 * Gets all the children of the specified element with the given name and
	 * namespace.
	 * (Note that this method only searches immediate children.)
	 * @param d an Element containing children
	 * @param nsURI the namespace to search in
	 * @param n the tag name to search for
	 * @return an ArrayList of child elements
	 */
	protected static ArrayList<Element> getNamedChildElements(
			Element d, String ns, String n) {
		ArrayList<Element> r = new ArrayList<Element>();
		for (Element t : getChildElements(d))
			if (t.getNamespaceURI().equals(ns) && t.getLocalName().equals(n))
				r.add(t);
		return r;
	}

	/**
	 * Returns the unique child of the specified Element which has the given
	 * tag name.
	 * @param d an Element containing children
	 * @param n the tag name to search for
	 * @return the unique named child, or <code>null</code> if there were zero
	 *         or more than one matches
	 * @see XMLLoader#getNamedChildElements
	 */
	protected static Element getNamedChildElement(
			Element d, String nsURI, String n) {
		ArrayList<Element> r = getNamedChildElements(d, nsURI, n);
		if (r.size() == 1)
			return r.get(0);
		else return null;
	}
}
