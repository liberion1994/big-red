package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeExecutor;

public class SimulationSpec extends ModelObject implements IChangeExecutor {
	abstract class SimulationSpecChange extends ModelObjectChange {
		@Override
		public SimulationSpec getCreator() {
			return SimulationSpec.this;
		}
	}
	
	public class ChangeSignature extends SimulationSpecChange {
		public Signature signature;
		
		protected ChangeSignature(Signature signature) {
			this.signature = signature;
		}
		
		private Signature oldSignature;
		
		@Override
		public void beforeApply() {
			oldSignature = getCreator().getSignature();
		}
		
		@Override
		public ChangeSignature inverse() {
			return new ChangeSignature(oldSignature);
		}
		
		@Override
		public String toString() {
			return "Change(set signature of " + getCreator() +
					" to " + signature + ")";
		}
	}
	
	public class ChangeAddRule extends SimulationSpecChange {
		public ReactionRule rule;
		
		protected ChangeAddRule(ReactionRule rule) {
			this.rule = rule;
		}
		
		@Override
		public ChangeRemoveRule inverse() {
			return new ChangeRemoveRule(rule);
		}
		
		@Override
		public String toString() {
			return "Change(add reaction rule " + rule + " to " +
					getCreator() + ")";
		}
	}
	
	public class ChangeRemoveRule extends SimulationSpecChange {
		public ReactionRule rule;
		
		protected ChangeRemoveRule(ReactionRule rule) {
			this.rule = rule;
		}
		
		@Override
		public ChangeAddRule inverse() {
			return new ChangeAddRule(rule);
		}
		
		@Override
		public String toString() {
			return "Change(remove reaction rule " + rule + " from " +
					getCreator() + ")";
		}
	}
	
	public class ChangeModel extends SimulationSpecChange {
		public Bigraph model;
		
		protected ChangeModel(Bigraph model) {
			this.model = model;
		}
		
		private Bigraph oldModel;
		
		@Override
		public void beforeApply() {
			oldModel = getCreator().getModel();
		}
		
		@Override
		public ChangeModel inverse() {
			return new ChangeModel(oldModel);
		}
		
		@Override
		public String toString() {
			return "Change(set model of " + getCreator() +
					" to " + model + ")";
		}
	}
	
	/**
	 * The property name fired when the signature changes. The property values
	 * are {@link Signature}s.
	 */
	public static final String PROPERTY_SIGNATURE = "SimulationSpecSignature";
	
	/**
	 * The property name fired when a rule is added or removed. The property
	 * values are {@link ReactionRule}s.
	 */
	public static final String PROPERTY_RULE = "SimulationSpecRule";
	
	/**
	 * The property name fired when the model changes. The property values
	 * are {@link Bigraph}s.
	 */
	public static final String PROPERTY_MODEL = "SimulationSpecModel";
	
	private Signature signature;
	
	protected SimulationSpec setSignature(Signature signature) {
		Signature oldSignature = this.signature;
		this.signature = signature;
		firePropertyChange(PROPERTY_SIGNATURE, oldSignature, signature);
		return this;
	}
	
	public Signature getSignature() {
		return signature;
	}
	
	@Override
	public SimulationSpec clone(Map<ModelObject, ModelObject> m) {
		SimulationSpec ss = (SimulationSpec)super.clone(m);
		ss.setFile(getFile());
		
		ss.setSignature(getSignature().clone(m));
		for (ReactionRule r : getRules())
			ss.addRule(r.clone(m));
		ss.setModel(getModel().clone(m));
		
		return ss;
	}
	
	private ArrayList<ReactionRule> rules = new ArrayList<ReactionRule>();
	
	protected SimulationSpec addRule(ReactionRule r) {
		getRules().add(r);
		firePropertyChange(PROPERTY_RULE, null, r);
		return this;
	}
	
	protected SimulationSpec removeRule(ReactionRule r) {
		getRules().remove(r);
		firePropertyChange(PROPERTY_RULE, r, null);
		return this;
	}
	
	public List<ReactionRule> getRules() {
		return rules;
	}
	
	private Bigraph model;

	public static final String CONTENT_TYPE = "dk.itu.big_red.simulation_spec";
	
	protected SimulationSpec setModel(Bigraph model) {
		Bigraph oldModel = this.model;
		this.model = model;
		firePropertyChange(PROPERTY_MODEL, oldModel, model);
		return this;
	}
	
	public Bigraph getModel() {
		return model;
	}
	
	public ChangeSignature changeSignature(Signature signature) {
		return new ChangeSignature(signature);
	}
	
	public ChangeAddRule changeAddRule(ReactionRule rule) {
		return new ChangeAddRule(rule);
	}
	
	public ChangeRemoveRule changeRemoveRule(ReactionRule rule) {
		return new ChangeRemoveRule(rule);
	}

	public ChangeModel changeModel(Bigraph model) {
		return new ChangeModel(model);
	}
	
	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		if (b instanceof ChangeGroup) {
			for (Change i : (ChangeGroup)b)
				tryValidateChange(i);
		} else if (b instanceof ChangeSignature ||
				b instanceof ChangeAddRule ||
				b instanceof ChangeRemoveRule ||
				b instanceof ChangeModel) {
			/* do nothing */
		} else {
			throw new ChangeRejectedException(this, b, this,
					"The Change was not recognised");
		}
	}

	@Override
	public void tryApplyChange(Change b) throws ChangeRejectedException {
		tryValidateChange(b);
		doChange(b);
	}
	
	private void doChange(Change b) {
		b.beforeApply();
		if (b instanceof ChangeGroup) {
			for (Change i : (ChangeGroup)b)
				doChange(i);
		} else if (b instanceof ChangeSignature) {
			setSignature(((ChangeSignature) b).signature);
		} else if (b instanceof ChangeAddRule) {
			addRule(((ChangeAddRule) b).rule);
		} else if (b instanceof ChangeRemoveRule) {
			removeRule(((ChangeRemoveRule) b).rule);
		} else if (b instanceof ChangeModel) {
			setModel(((ChangeModel) b).model);
		}
	}
	
	@Override
	public void dispose() {
		if (model != null) {
			model.dispose();
			model = null;
		}
		
		if (signature != null) {
			signature.dispose();
			signature = null;
		}
		
		if (rules != null) {
			for (ReactionRule r : rules)
				r.dispose();
			rules.clear();
			rules = null;
		}
		
		super.dispose();
	}
	
	/**
	 * {@inheritDoc}
	 * <p><strong>Special notes for {@link SimulationSpec}:</strong>
	 * <ul>
	 * <li>Passing {@link #PROPERTY_RULE} will return a {@link List}&lt;{@link
	 * ReactionRule}&gt;, <strong>not</strong> a {@link ReactionRule}.
	 * </ul>
	 */
	@Override
	public Object getProperty(String name) {
		if (PROPERTY_SIGNATURE.equals(name)) {
			return getSignature();
		} else if (PROPERTY_MODEL.equals(name)) {
			return getModel();
		} else if (PROPERTY_RULE.equals(name)) {
			return getRules();
		} else return super.getProperty(name);
	}
	
	@Override
	public SimulationSpec setFile(IFile file) {
		return (SimulationSpec)super.setFile(file);
	}
}
