package dk.itu.big_red.editors.bigraph.commands;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;

public class LayoutableDeleteCommand extends Command {
	private LayoutableModelObject model = null;
	private Container parentModel = null;
	
	public void setModel(Object model) {
		if (model instanceof LayoutableModelObject)
			this.model = (LayoutableModelObject)model;
	}
	
	public void setParentModel(Object model) {
		if (model instanceof Container)
			parentModel = (Container)model;
	}
	
	@Override
	public void execute() {
		parentModel.removeChild(model);
	}
	
	@Override
	public void undo() {
		parentModel.addChild(model);
	}
}
