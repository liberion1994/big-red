package dk.itu.big_red.model.import_export;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.exceptions.ExportFailedException;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.util.DOM;

public class SignatureXMLExport extends Export<Signature> {
	private Document doc = null;
	
	@Override
	public void exportModel() throws ExportFailedException {
		process(model);
		
		try {
			DOM.write(target, doc);
		} catch (Exception e) {
			throw new ExportFailedException(e);
		}
	}

	private void process(Signature s) {
		DOMImplementation impl = DOM.getImplementation();
		doc = impl.createDocument(
				"http://pls.itu.dk/bigraphs/2010/signature", "signature", null);
		Element e = doc.getDocumentElement();
		DOM.applyAttributesToElement(e,
			"xmlns:big-red", "http://pls.itu.dk/bigraphs/2010/big-red");

		for (Control c : s.getControls())
			DOM.appendChildIfNotNull(e, process(c));
	}
	
	private Element process(Control c) {
		if (c.getLongName().equals("Unknown"))
			return null;
		
		Element e = doc.createElement("control");
		DOM.applyAttributesToElement(e,
				"name", c.getLongName());
		
		for (Port p : c.getPortsArray())
			e.appendChild(process(p));
		
		DOM.appendChildIfNotNull(e, AppearanceGenerator.getShape(doc, c));
		DOM.appendChildIfNotNull(e, AppearanceGenerator.getAppearance(doc, c));
		
		return e;
	}
	
	private Element process(Port p) {
		Element e = doc.createElement("port");
		DOM.applyAttributesToElement(e,
				"name", p.getName());
		
		Element pa = doc.createElement("big-red:port-appearance");
		DOM.applyAttributesToElement(pa,
				"segment", p.getSegment(),
				"distance", p.getDistance());
		e.appendChild(pa);
		
		return e;
	}
}
