package dk.itu.big_red.model.assistants;

import java.util.ArrayList;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.ModelObject.ExtendedDataValidator;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.ChangeValidator;
import org.bigraph.model.changes.IChangeExecutor;

abstract class ModelObjectValidator<T extends ModelObject & IChangeExecutor>
		extends ChangeValidator<T> {
	private PropertyScratchpad scratch = new PropertyScratchpad();
	
	protected PropertyScratchpad getScratch() {
		return scratch;
	}
	
	private ArrayList<ChangeExtendedData> finalChecks =
			new ArrayList<ChangeExtendedData>();
	
	public ModelObjectValidator(T changeable) {
		super(changeable);
	}

	protected Change doValidateChange(Change b)
			throws ChangeRejectedException {
		if (!b.isReady()) {
			rejectChange(b, "The Change is not ready");
		} else if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b)
				if ((c = doValidateChange(c)) != null)
					return c;
		} else if (b instanceof ChangeExtendedData) {
			ChangeExtendedData c = (ChangeExtendedData)b;
			ExtendedDataValidator v = c.immediateValidator;
			if (v != null) {
				String rationale = v.validate(c, scratch);
				if (rationale != null)
					rejectChange(c, rationale);
			}
			if (c.finalValidator != null)
				finalChecks.add(c);
			scratch.setProperty(c.getCreator(), c.key, c.newValue);
		} else return b;
		return null;
	}
	
	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		getScratch().clear();
		finalChecks.clear();
		
		b = doValidateChange(b);
		if (b != null)
			rejectChange(b, "The change was not recognised by the validator");
		
		for (ChangeExtendedData i : finalChecks) {
			String rationale = i.finalValidator.validate(i, scratch);
			if (rationale != null)
				rejectChange(i, rationale);
		}
	}
}
