package dk.itu.big_red.model.load_save.loaders;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.load_save.Loader;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.IRedNamespaceConstants;
import dk.itu.big_red.model.load_save.savers.BigraphXMLSaver;
import dk.itu.big_red.utilities.geometry.Rectangle;
import dk.itu.big_red.utilities.resources.Project;

/**
 * XMLImport reads a XML document and produces a corresponding {@link Bigraph}.
 * @author alec
 * @see BigraphXMLSaver
 *
 */
public class BigraphXMLLoader extends XMLLoader {
	private enum Tristate {
		TRUE,
		FALSE,
		UNKNOWN;
		
		private static Tristate fromBoolean(boolean b) {
			return (b ? TRUE : FALSE);
		}
	}
	
	private boolean partialAppearanceWarning;
	private Tristate appearanceAllowed;
	private ChangeGroup cg = new ChangeGroup();
	
	@Override
	public Bigraph importObject() throws LoadFailedException {
		try {
			Document d =
				validate(parse(source),
					RedPlugin.getResource("resources/schema/bigraph.xsd"));
			return makeObject(d.getDocumentElement()).setFile(getFile());
		} catch (Exception e) {
			if (e instanceof LoadFailedException) {
				throw (LoadFailedException)e;
			} else throw new LoadFailedException(e);
		}
	}
	
	private Bigraph bigraph = null;
	
	@Override
	public Bigraph makeObject(Element e) throws LoadFailedException {
		if (e == null)
			throw new LoadFailedException("Element is null");
		
		bigraph = new Bigraph();
		
		partialAppearanceWarning = false;
		appearanceAllowed = Tristate.UNKNOWN;
		cg.clear();
		
		Element signatureElement =
			removeNamedChildElement(e, IRedNamespaceConstants.BIGRAPH, "signature");
		
		String signaturePath;
		if (signatureElement != null) {
			signaturePath =
				getAttributeNS(signatureElement, IRedNamespaceConstants.BIGRAPH, "src");
		} else {
			signaturePath = getAttributeNS(e, IRedNamespaceConstants.BIGRAPH, "signature");
		}
		
		if (signaturePath != null) {
			IFile sigFile = null;
			if (getFile() != null)
				sigFile =
					Project.findFileByPath(getFile().getParent(),
							new Path(signaturePath));
			
			if (sigFile == null) { /* backwards compatibility */
				sigFile = 
					Project.findFileByPath(null, new Path(signaturePath));
				if (sigFile == null)
					throw new LoadFailedException("The signature \"" + signaturePath + "\" does not exist.");
			}
				
			Signature sig = (Signature)Loader.fromFile(sigFile);
			bigraph.setSignature(sig);
		} else if (signatureElement != null) {
			SignatureXMLLoader si = new SignatureXMLLoader();
			bigraph.setSignature(si.makeObject(signatureElement));
		} else {
			throw new LoadFailedException("The bigraph does not define or reference a signature.");
		}
		
		processContainer(e, bigraph);
		
		try {
			if (cg.size() != 0)
				bigraph.tryApplyChange(cg);
			if (appearanceAllowed == Tristate.FALSE)
				bigraph.tryApplyChange(bigraph.relayout());
		} catch (ChangeRejectedException f) {
			throw new LoadFailedException(f);
		}
		
		return bigraph;
	}
	
	private Container processContainer(Element e, Container model) throws LoadFailedException {
		for (Element i : getChildElements(e))
			addChild(model, i);
		return model;
	}
	
	private HashMap<String, Link> links =
			new HashMap<String, Link>();
	
	private Link processLink(Element e, Link model) throws LoadFailedException {
		String name = getAttributeNS(e, IRedNamespaceConstants.BIGRAPH, "name");
		links.put(name, model);
		
		return model;
	}
	
	private Point processPoint(Element e, Point model) throws LoadFailedException {
		String link = getAttributeNS(e, IRedNamespaceConstants.BIGRAPH, "link");
		if (link != null)
			cg.add(model.changeConnect(links.get(link)));
		return model;
	}
	
	private Site processSite(Element e, Site model) throws LoadFailedException {
		String alias = getAttributeNS(e, IRedNamespaceConstants.BIGRAPH, "alias");
		if (alias != null)
			cg.add(model.changeAlias(alias));
		return model;
	}
	
	private void addChild(Container context, Element e) throws LoadFailedException {
		ModelObject model = null;
		boolean port = false;
		if (e.getLocalName().equals("node")) {
			String controlName =
					getAttributeNS(e, IRedNamespaceConstants.BIGRAPH, "control");
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

		if (model instanceof Layoutable) {
			Layoutable l = (Layoutable)model;
			cg.add(context.changeAddChild(l,
					getAttributeNS(e, IRedNamespaceConstants.BIGRAPH, "name")));
			
			Element appearance =
				removeNamedChildElement(e, IRedNamespaceConstants.BIG_RED, "appearance");
			if (appearanceAllowed == Tristate.UNKNOWN) {
				appearanceAllowed = Tristate.fromBoolean(appearance != null);
			} else if (!partialAppearanceWarning &&
					    (appearanceAllowed == Tristate.FALSE &&
					     appearance != null) ||
					    (appearanceAllowed == Tristate.TRUE &&
					     appearance == null)) {
				addNotice(new Status(IStatus.WARNING, RedPlugin.PLUGIN_ID,
					"The layout data for this bigraph is incomplete and so " +
					"has been ignored."));
				appearanceAllowed = Tristate.FALSE;
				partialAppearanceWarning = true;
			}
			
			if (appearance != null && appearanceAllowed == Tristate.TRUE)
				elementToAppearance(appearance, model, cg);
		}
		
		if (model instanceof Container) {
			processContainer(e, (Container)model);
		} else if (port) {
			Node n = (Node)context;
			processPoint(e,
				n.getPort(getAttributeNS(e, IRedNamespaceConstants.BIGRAPH, "name")));
		} else if (model instanceof Link) {
			processLink(e, (Link)model);
		} else if (model instanceof InnerName) {
			processPoint(e, (InnerName)model);
		} else if (model instanceof Site) {
			processSite(e, (Site)model);
		}
	}

	@Override
	public BigraphXMLLoader setFile(IFile f) {
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

	protected static void elementToAppearance(
			Element e, Object o, ChangeGroup cg) {
		if (!(e.getNamespaceURI().equals(IRedNamespaceConstants.BIG_RED) &&
				e.getLocalName().equals("appearance")))
			return;
		
		if (o instanceof Layoutable) {
			Layoutable l = (Layoutable)o;
			Rectangle r = AppearanceGenerator.elementToRectangle(e);
			cg.add(l.changeLayout(r));
		}
		
		if (o instanceof Colourable) {
			Colourable c = (Colourable)o;
			cg.add(
				c.changeFillColour(
						getColorAttribute(e,
							IRedNamespaceConstants.BIG_RED, "fillColor")),
				c.changeOutlineColour(
						getColorAttribute(e,
							IRedNamespaceConstants.BIG_RED, "outlineColor")));
		}
		
		if (o instanceof ModelObject)
			((ModelObject)o).setComment(getAttributeNS(e,
					IRedNamespaceConstants.BIG_RED, "comment"));
	}
}