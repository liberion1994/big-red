package dk.itu.big_red.import_export;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class XMLExport<T> extends Export<T> {
	private Document doc = null;
	
	public Document getDocument() {
		return doc;
	}
	
	public XMLExport<T> setDocument(Document doc) {
		this.doc = doc;
		return this;
	}
	
	protected Element newElement(String nsURI, String qualifiedName) {
		return doc.createElementNS(nsURI, qualifiedName);
	}
}