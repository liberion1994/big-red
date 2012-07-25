package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ModelObject;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.resources.IFileWrapper;
import org.bigraph.model.resources.IResourceWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.ExtendedDataUtilities;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.SPEC;

public class SimulationSpecXMLLoader extends XMLLoader {
	@Override
	public SimulationSpec importObject() throws LoadFailedException {
		try {
			Document d =
					validate(parse(getInputStream()), "resources/schema/spec.xsd");
			SimulationSpec ss = makeObject(d.getDocumentElement());
			ExtendedDataUtilities.setFile(ss, getFile());
			return ss;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}
	
	private ModelObject tryLoad(String relPath) throws LoadFailedException {
		IResourceWrapper rw = getFile().getParent().getResource(relPath);
		if (!(rw instanceof IFileWrapper))
			throw new LoadFailedException("The path does not identify a file");
		return ((IFileWrapper)rw).load();
	}
	
	private Signature makeSignature(Element e) throws LoadFailedException {
		String signaturePath = org.bigraph.model.loaders.XMLLoader.getAttributeNS(e, SPEC, "src");
		if (signaturePath != null && getFile() != null) {
			ModelObject mo = tryLoad(signaturePath);
			if (mo instanceof Signature) {
				return (Signature)mo;
			} else throw new LoadFailedException(
					"The path does not identify a signature file");
		} else {
			return new SignatureXMLLoader().setFile(getFile()).makeObject(e);
		}
	}
	
	private Bigraph makeBigraph(Element e) throws LoadFailedException {
		String bigraphPath = org.bigraph.model.loaders.XMLLoader.getAttributeNS(e, SPEC, "src");
		if (bigraphPath != null && getFile() != null) {
			ModelObject mo = tryLoad(bigraphPath);
			if (mo instanceof Bigraph) {
				return (Bigraph)mo;
			} else throw new LoadFailedException(
					"The path does not identify a bigraph file");
		} else {
			return new BigraphXMLLoader().setFile(getFile()).makeObject(e);
		}
	}
	
	private ReactionRule makeRule(Element e) throws LoadFailedException {
		String rulePath = org.bigraph.model.loaders.XMLLoader.getAttributeNS(e, SPEC, "src");
		if (rulePath != null && getFile() != null) {
			ModelObject mo = tryLoad(rulePath);
			if (mo instanceof ReactionRule) {
				return (ReactionRule)mo;
			} else throw new LoadFailedException(
					"The path does not identify a reaction rule file");
		} else {
			return new ReactionRuleXMLLoader().setFile(getFile()).makeObject(e);
		}
	}
	
	@Override
	public SimulationSpec makeObject(Element e) throws LoadFailedException {
		SimulationSpec ss = new SimulationSpec();
		
		Element signatureElement = getNamedChildElement(e, SPEC, "signature");
		if (signatureElement != null)
			addChange(ss.changeSignature(makeSignature(signatureElement)));
		
		for (Element i : getNamedChildElements(e, SPEC, "rule"))
			addChange(ss.changeAddRule(makeRule(i)));
		
		Element modelElement = getNamedChildElement(e, SPEC, "model");
		if (modelElement != null)
			addChange(ss.changeModel(makeBigraph(modelElement)));
		
		executeUndecorators(ss, e);
		executeChanges(ss);
		return ss;
	}
	
	@Override
	public SimulationSpecXMLLoader setFile(IFileWrapper f) {
		return (SimulationSpecXMLLoader)super.setFile(f);
	}
}
