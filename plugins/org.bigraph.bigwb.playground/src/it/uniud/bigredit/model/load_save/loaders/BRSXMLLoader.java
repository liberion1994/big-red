package it.uniud.bigredit.model.load_save.loaders;


import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.loaders.BigraphXMLLoader;
import dk.itu.big_red.model.load_save.loaders.ReactionRuleXMLLoader;
import dk.itu.big_red.model.load_save.loaders.SignatureXMLLoader;
import dk.itu.big_red.model.load_save.loaders.SimulationSpecXMLLoader;
import dk.itu.big_red.model.load_save.loaders.XMLLoader;

import it.uniud.bigredit.model.BRS;



	/**
	 * XMLImport reads a XML document and produces a corresponding {@link BRS}.
	 * we use the actual structure for load and save bigraphs, plus we add some information.
	 * 
	 * @author carlo
	 * @see XMLSaver
	 *
	 */
	public class BRSXMLLoader  extends XMLLoader {

		
		public static final String BRS =
				"http://www.itu.dk/research/pls/xmlns/2012/testing";
		
		@Override
		public BRS importObject() throws LoadFailedException {
			try {
				Document d =
						validate(parse(source), "resources/schema/brs.xsd");
				BRS ss = makeObject(d.getDocumentElement());
				//ExtendedDataUtilities.setFile(ss, getFile());
				return ss;
			} catch (Exception e) {
				throw new LoadFailedException(e);
			}
		}
		
		private Signature makeSignature(Element e) throws LoadFailedException {
			String signaturePath = getAttributeNS(e, BRS, "src");
			SignatureXMLLoader l = newLoader(SignatureXMLLoader.class);
			if (signaturePath != null && getFile() != null) {
				IFile f = Project.findFileByPath(
						getFile().getParent(), new Path(signaturePath));
				try {
					l.setFile(f).setInputStream(f.getContents());
				} catch (CoreException ex) {
					throw new LoadFailedException(ex);
				}
				return l.importObject();
			} else {
				return l.setFile(getFile()).makeObject(e);
			}
		}
		
		private Bigraph makeBigraph(Element e) throws LoadFailedException {
			String bigraphPath = getAttributeNS(e, BRS, "src");
			BigraphXMLLoader l = newLoader(BigraphXMLLoader.class);
			if (bigraphPath != null && getFile() != null) {
				
				
				IFile f =  Project.findFileByPath(getFile().getParent(), new Path(bigraphPath));    
				try {
					l.setFile(f).setInputStream(f.getContents());
				} catch (CoreException ex) {
					throw new LoadFailedException(ex);
				}
				return l.importObject();
			} else {
				return l.setFile(getFile()).makeObject(e);
			}
		}
		
		private ReactionRule makeRule(Element e) throws LoadFailedException {
			String rulePath = getAttributeNS(e, BRS, "src");
			ReactionRuleXMLLoader l = newLoader(ReactionRuleXMLLoader.class);
			if (rulePath != null && getFile() != null) {
				IFile f = Project.findFileByPath(
						getFile().getParent(), new Path(rulePath));
				try {
					l.setFile(f).setInputStream(f.getContents());
				} catch (CoreException ex) {
					throw new LoadFailedException(ex);
				}
				return l.importObject();
			} else {
				return l.setFile(getFile()).makeObject(e);
			}
		}
		
		@Override
		public BRS makeObject(Element e) throws LoadFailedException {
			BRS ss = new BRS();
			ChangeGroup cg = new ChangeGroup();
			
//			Element signatureElement = getNamedChildElement(e, BRS, "signature");
//			if (signatureElement != null)
//				cg.add(ss.setSignature(makeSignature(signatureElement)));
			
//TODO:RULE			for (Element i : getNamedChildElements(e, BRS, "rule"))
//				cg.add(ss.changeAddRule(makeRule(i)));
//			
			Element modelElement = getNamedChildElement(e, BRS, "model");
			if (modelElement != null)
				cg.add(ss.changeAddChild((ModelObject)makeBigraph(modelElement),""));
			
			try {
				if (cg.size() != 0)
					ss.tryApplyChange(cg);
			} catch (ChangeRejectedException cre) {
				throw new LoadFailedException(cre);
			}
			
			return executeUndecorators(ss, e);
		}
		
		@Override
		public BRSXMLLoader setFile(IFile f) {
			return (BRSXMLLoader)super.setFile(f);
		}
		
		
		
		
		
	}
