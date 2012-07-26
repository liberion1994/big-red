package dk.itu.big_red.model.load_save.loaders;

import java.util.HashMap;
import java.util.Locale;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Root;
import org.bigraph.model.Signature;
import org.bigraph.model.Site;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.loaders.LoaderNotice;
import org.bigraph.model.names.policies.INamePolicy;
import org.bigraph.model.resources.IFileWrapper;
import org.bigraph.model.resources.IResourceWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.ExtendedDataUtilities;
import dk.itu.big_red.model.load_save.savers.BigraphXMLSaver;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIGRAPH;

/**
 * XMLImport reads a XML document and produces a corresponding {@link Bigraph}.
 * @author alec
 * @see BigraphXMLSaver
 */
public class BigraphXMLLoader extends XMLLoader {
	@Override
	public Bigraph importObject() throws LoadFailedException {
		try {
			Document d =
					validate(parse(getInputStream()), "resources/schema/bigraph.xsd");
			Bigraph b = makeObject(d.getDocumentElement());
			/* XXX: this is a hilariously awful hack */
			FileData.setFile(b, getFile());
			return b;
		} catch (LoadFailedException e) {
			throw e;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}
	
	private Bigraph bigraph = null;
	
	public Bigraph makeObject(Element e) throws LoadFailedException {
		if (e == null)
			throw new LoadFailedException("Element is null");
		
		bigraph = new Bigraph();
		
		Element signatureElement =
			getNamedChildElement(e, BIGRAPH, "signature");
		
		String signaturePath;
		if (signatureElement != null) {
			signaturePath = org.bigraph.model.loaders.XMLLoader.getAttributeNS(signatureElement, BIGRAPH, "src");
		} else {
			signaturePath = org.bigraph.model.loaders.XMLLoader.getAttributeNS(e, BIGRAPH, "signature");
		}
		
		if (signaturePath != null) {
			IResourceWrapper sigR = null;
			IFileWrapper sigFile = null;
			if (getFile() != null) {
				sigR = getFile().getParent().getResource(signaturePath);
				if (sigR instanceof IFileWrapper) {
					sigFile = (IFileWrapper)sigR;
				} else throw new LoadFailedException(
						"The path given does not identify a file");
			} else throw new Error("BUG: relative path to resolve, " +
					"but no IFileWrapper set on BigraphXMLLoader");
			
			ModelObject mo = sigFile.load();
			if (!(mo instanceof Signature))
				throw new LoadFailedException(
						"The path given does not identify a signature file");
			bigraph.setSignature((Signature)mo);
		} else if (signatureElement != null) {
			SignatureXMLLoader si = new SignatureXMLLoader();
			bigraph.setSignature(
					si.setFile(getFile()).makeObject(signatureElement));
		} else {
			throw new LoadFailedException("The bigraph does not define or reference a signature.");
		}
		
		processContainer(e, bigraph);
		
		executeUndecorators(bigraph, e);
		executeChanges(bigraph);
		return bigraph;
	}
	
	private void processContainer(Element e, Container model) throws LoadFailedException {
		for (Element i : getChildElements(e))
			addChild(model, i);
	}
	
	private HashMap<String, Link> links =
			new HashMap<String, Link>();
	
	private void processLink(Element e, Link model) throws LoadFailedException {
		links.put(org.bigraph.model.loaders.XMLLoader.getAttributeNS(e, BIGRAPH, "name"), model);
	}
	
	private void processPoint(Element e, Point model) throws LoadFailedException {
		if (model != null) {
			String linkName = org.bigraph.model.loaders.XMLLoader.getAttributeNS(e, BIGRAPH, "link");
			if (linkName != null) {
				Link link = links.get(linkName);
				if (link != null)
					addChange(model.changeConnect(link));
			}
		} else {
			addNotice(LoaderNotice.Type.WARNING,
					"Invalid point referenced; skipping.");
		}
	}
	
	private void processSite(Element e, Site model) throws LoadFailedException {
		String alias = org.bigraph.model.loaders.XMLLoader.getAttributeNS(e, BIGRAPH, "alias");
		if (alias != null)
			addChange(ExtendedDataUtilities.changeAlias(model, alias));
	}
	
	private void processNode(Element e, Node model) throws LoadFailedException {
		String parameter = org.bigraph.model.loaders.XMLLoader.getAttributeNS(e, BIGRAPH, "parameter");
		INamePolicy policy = ExtendedDataUtilities.getParameterPolicy(model.getControl());
		
		 /* FIXME - details */
		if (parameter != null && policy == null) {
			addNotice(LoaderNotice.Type.WARNING,
					"Spurious parameter value ignored.");
		} else if (parameter == null && policy != null) {
			addNotice(LoaderNotice.Type.WARNING,
					"Default parameter value assigned.");
			addChange(
				ExtendedDataUtilities.changeParameter(model, policy.get(0)));
		} else if (parameter != null && policy != null) {
			addChange(
					ExtendedDataUtilities.changeParameter(model, parameter));
		}
		
		processContainer(e, model);
	}
	
	private void addChild(Container context, Element e) throws LoadFailedException {
		ModelObject model = null;
		boolean port = false;
		if (e.getLocalName().equals("node")) {
			String controlName = org.bigraph.model.loaders.XMLLoader.getAttributeNS(e, BIGRAPH, "control");
			Control c = bigraph.getSignature().getControl(controlName);
			if (c == null)
				throw new LoadFailedException(
					"The control \"" + controlName + "\" isn't defined by " +
							"this bigraph's signature.");
			
			model = new Node(c);
		} else if (e.getLocalName().equals("port") && context instanceof Node) {
			/*
			 * <port /> tags shouldn't actually create anything, so let the
			 * special handling commence!
			 */
			port = true;
		} else {
			model = BigraphXMLLoader.getNewObject(e.getLocalName());
		}

		if (model instanceof Layoutable)
			addChange(context.changeAddChild(
					(Layoutable)model, org.bigraph.model.loaders.XMLLoader.getAttributeNS(e, BIGRAPH, "name")));
		
		if (model instanceof Node) {
			processNode(e, (Node)model);
		} else if (model instanceof Container) {
			processContainer(e, (Container)model);
		} else if (port) {
			Node n = (Node)context;
			processPoint(e,
				n.getPort(org.bigraph.model.loaders.XMLLoader.getAttributeNS(e, BIGRAPH, "name")));
		} else if (model instanceof Link) {
			processLink(e, (Link)model);
		} else if (model instanceof InnerName) {
			processPoint(e, (InnerName)model);
		} else if (model instanceof Site) {
			processSite(e, (Site)model);
		}
		
		if (model != null)
			executeUndecorators(model, e);
	}

	@Override
	public BigraphXMLLoader setFile(IFileWrapper f) {
		return (BigraphXMLLoader)super.setFile(f);
	}

	/**
	 * Creates a new object of the named type.
	 * @param typeName a type name (not case sensitive)
	 * @return a new object of the appropriate type, or <code>null</code> if
	 *          the type name was unrecognised
	 * @see ModelObject#getType()
	 */
	static ModelObject getNewObject(String typeName) {
		typeName = typeName.toLowerCase(Locale.ENGLISH);
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
		else if (typeName.equals("control"))
			return new Control();
		else if (typeName.equals("edge"))
			return new Edge();
		else return null;
	}
}
