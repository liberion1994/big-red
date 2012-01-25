package dk.itu.big_red.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.utilities.resources.IFileBackable;

public class ReactionRule extends ModelObject implements IFileBackable {
	private Bigraph redex, reactum;
	private Map<ModelObject, ModelObject> redexToReactum =
		new HashMap<ModelObject, ModelObject>();
	private ChangeGroup changes;
	
	public static final String PROPERTY_REDEX = "ReactionRuleRedex";
	
	public Bigraph getRedex() {
		return redex;
	}
	
	public void setRedex(Bigraph redex) {
		Bigraph old = this.redex;
		this.redex = redex;
		
		reactum = null;
		redexToReactum.clear();
		
		firePropertyChange(PROPERTY_REDEX, old, redex);
	}

	public Bigraph getReactum() {
		if (reactum == null)
			reactum = redex.clone(redexToReactum);
		return reactum;
	}
	
	public Map<ModelObject, ModelObject> getRedexToReactumMap() {
		return redexToReactum;
	}
	
	protected IFile file = null;
	
	@Override
	public IFile getFile() {
		return file;
	}

	@Override
	public ReactionRule setFile(IFile file) {
		this.file = file;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	private static final <T, V extends T> V ac(T o) {
		return (V)o;
	}
	
	@SuppressWarnings("unchecked")
	private static final <T extends ModelObject> T mg(Map<ModelObject, ModelObject> m, T k) {
		return (T)m.get(k);
	}
	
	public static Change translateChange(
			Map<ModelObject, ModelObject> oldToNew, Change change) {
		if (change instanceof ChangeGroup) {
			ChangeGroup cg_ = (ChangeGroup)change,
				cg = new ChangeGroup();
			for (Change i : cg_) {
				Change iP = translateChange(oldToNew, i);
				if (iP != null) {
					cg.add(iP);
				} else {
					cg.clear();
					return null;
				}
			}
			
			return cg;
		} else if (change instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild ch = ac(change);
			
			Container reactumParent = mg(oldToNew, ch.getCreator());
			Layoutable reactumChild = mg(oldToNew, ch.child);
			
			if (reactumParent == null)
				return null;
			if (reactumChild == null)
				reactumChild = ch.child.clone(oldToNew);
			
			/*
			 * XXX: a BigraphScratchpad should really be used here so that
			 * ChangeGroups will actually work properly
			 */
			String reactumName;
			Map<String, Layoutable> reactumNamespace =
				reactumParent.getBigraph().
				getNamespace(Bigraph.getNSI(reactumChild));
			if (reactumNamespace.get(ch.name) == null) {
				reactumName = ch.name;
			} else reactumName = Bigraph.getFirstUnusedName(reactumNamespace);
			
			return reactumParent.changeAddChild(reactumChild, reactumName);
		} else if (change instanceof Layoutable.ChangeLayout) {
			Layoutable.ChangeLayout ch = ac(change);
			
			Layoutable reactumModel = mg(oldToNew, ch.getCreator());
			
			if (reactumModel == null)
				return null;
			
			return reactumModel.changeLayout(ch.newLayout.getCopy());
		} else if (change instanceof Container.ChangeRemoveChild) {
			Container.ChangeRemoveChild ch = ac(change);
			
			Container reactumParent = mg(oldToNew, ch.getCreator());
			Layoutable reactumChild = mg(oldToNew, ch.child);
			
			if (reactumParent == null || reactumChild == null)
				return null;
			
			oldToNew.remove(ch.child);
			return reactumParent.changeRemoveChild(reactumChild);
		} else if (change instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName ch = ac(change);
			
			Layoutable reactumModel = mg(oldToNew, ch.getCreator());
			if (reactumModel == null)
				return null;
			
			return reactumModel.changeName(ch.newName);
		} else if (change instanceof Point.ChangeConnect) {
			Point.ChangeConnect ch = ac(change);
			
			Point reactumPoint = mg(oldToNew, ch.getCreator());
			Link reactumLink = mg(oldToNew, ch.link);
			if (reactumPoint == null || reactumLink == null)
				return null;
			
			return reactumPoint.changeConnect(reactumLink);
		} else if (change instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect ch = ac(change);
			
			Point reactumPoint = mg(oldToNew, ch.getCreator());
			Link reactumLink = mg(oldToNew, ch.link);
			if (reactumPoint == null || reactumLink == null)
				return null;
			
			return reactumPoint.changeDisconnect(reactumLink);
		} else if (change instanceof Site.ChangeAlias) {
			Site.ChangeAlias ch = ac(change);
			
			Site reactumSite = mg(oldToNew, ch.getCreator());
			if (reactumSite == null)
				return null;
			
			return reactumSite.changeAlias(ch.alias);
		} else throw new RuntimeException(change + " unrecognised");
	}
	
	@Override
	public ReactionRule clone(Map<ModelObject, ModelObject> m) {
		if (m == null)
			m = new HashMap<ModelObject, ModelObject>();
		ReactionRule rr = (ReactionRule)super.clone(m);
		
		Map<ModelObject, ModelObject>
			/* redex to reactum */
			rR = getRedexToReactumMap(),
			/* redex to cloned redex */
			rCr = new HashMap<ModelObject, ModelObject>(),
			/* reactum to cloned reactum */
			RCR = new HashMap<ModelObject, ModelObject>(),
			/* cloned redex to cloned reactum */
			CrCR = rr.getRedexToReactumMap();
		
		rr.setFile(getFile());
		rr.setRedex(getRedex().clone(rCr));
		
		try {
			getReactum().tryApplyChange(getChanges().inverse());
		} catch (ChangeRejectedException cre) {
			/* very bad news */
			cre.printStackTrace();
		}
		rr.setReactum(getReactum().clone(RCR));
		
		ChangeGroup cg = rr.getChanges();
		for (Change c : getChanges()) {
			try {
				Change cP = translateChange(RCR, c);
				rr.getReactum().tryApplyChange(cP);
				cg.add(cP);
				
				getReactum().tryApplyChange(c);
			} catch (ChangeRejectedException cre) {
				/* very bad news */
				cre.printStackTrace();
			}
		}
		
		for (Entry<ModelObject, ModelObject> e : rCr.entrySet())
			CrCR.put(e.getValue(), RCR.get(rR.get(e.getKey())));
		
		if (m != null) {
			m.putAll(rCr);
			m.putAll(RCR);
		}
		
		return rr;
	}
	
	private void setReactum(Bigraph b) {
		reactum = b;
	}
	
	public ChangeGroup getChanges() {
		if (changes == null)
			changes = new ChangeGroup();
		return changes;
	}
	
	@Override
	public void dispose() {
		redex.dispose();
		reactum.dispose();
		redex = reactum = null;
		redexToReactum.clear();
		redexToReactum = null;
		changes.clear();
		file = null;
		
		super.dispose();
	}
}
