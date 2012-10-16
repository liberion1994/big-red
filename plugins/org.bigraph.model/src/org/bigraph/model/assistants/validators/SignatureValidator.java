package org.bigraph.model.assistants.validators;

import org.bigraph.model.Control;
import org.bigraph.model.PortSpec;
import org.bigraph.model.PortSpec.ChangeRemovePort;
import org.bigraph.model.Signature;
import org.bigraph.model.Control.ChangeAddPort;
import org.bigraph.model.Control.ChangeKind;
import org.bigraph.model.Control.ChangeName;
import org.bigraph.model.Signature.ChangeAddControl;
import org.bigraph.model.Signature.ChangeAddSignature;
import org.bigraph.model.Signature.ChangeRemoveSignature;
import org.bigraph.model.Control.ChangeRemoveControl;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;

public class SignatureValidator extends ModelObjectValidator<Signature> {
	public SignatureValidator(Signature changeable) {
		super(changeable);
	}
	
	private void checkEligibility(
			PropertyScratchpad context, IChange b, Control c)
			throws ChangeRejectedException {
		if (c.getSignature(context) != getChangeable())
			throw new ChangeRejectedException(b,
					"The control " + c + " is not part of this Signature");
	}
	
	@Override
	public IChange doValidateChange(Process process, IChange b)
			throws ChangeRejectedException {
		final PropertyScratchpad context = process.getScratch();
		if (super.doValidateChange(process, b) == null) {
			return null;
		} else if (b instanceof ChangeAddControl) {
			ChangeAddControl c = (ChangeAddControl)b;
			checkName(context, c, c.control,
					getChangeable().getNamespace(), c.name);
		} else if (b instanceof ChangeRemoveControl) {
			ChangeRemoveControl c = (ChangeRemoveControl)b;
			checkEligibility(context, b, c.getCreator());
		} else if (b instanceof ChangeAddPort) {
			ChangeAddPort c = (ChangeAddPort)b;
			checkEligibility(context, b, c.getCreator());
			checkName(context, c, c.port,
					c.getCreator().getNamespace(), c.name);
		} else if (b instanceof ChangeRemovePort) {
			ChangeRemovePort c = (ChangeRemovePort)b;
			Control co = c.getCreator().getControl();
			checkEligibility(context, b, co);
		} else if (b instanceof ChangeKind) {
			/* do nothing */
		} else if (b instanceof PortSpec.ChangeName) {
			PortSpec.ChangeName c = (PortSpec.ChangeName)b;
			checkName(context, c, c.getCreator(),
					c.getCreator().getControl(context).getNamespace(),
					c.name);
		} else if (b instanceof ChangeName) {
			ChangeName c = (ChangeName)b;
			checkEligibility(context, b, c.getCreator());
			checkName(context, c, c.getCreator(),
					getChangeable().getNamespace(), c.name);
		} else if (b instanceof ChangeAddSignature) {
			ChangeAddSignature c = (ChangeAddSignature)b;
			if (c.signature.getParent(context) != null)
				throw new ChangeRejectedException(b,
						"Signature " + c.signature + " already has a parent");
		} else if (b instanceof ChangeRemoveSignature) {
			ChangeRemoveSignature c = (ChangeRemoveSignature)b;
			if (c.getCreator().getParent(context) == null)
				throw new ChangeRejectedException(b,
						"Signature " + c.getCreator() + " doesn't have a parent");
		} else return b;
		b.simulate(context);
		return null;
	}
}
