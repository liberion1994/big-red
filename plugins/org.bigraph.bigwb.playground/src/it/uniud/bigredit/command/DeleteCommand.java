package it.uniud.bigredit.command;

import it.uniud.bigredit.model.BRS;

import org.bigraph.model.ModelObject;
import org.bigraph.model.changes.ChangeGroup;

import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;

public class DeleteCommand extends ChangeCommand {

	
	private ModelObject target;
	private ChangeGroup cg = new ChangeGroup();
	private ModelObject del;
	
	
	public DeleteCommand() {
		setChange(cg);
	}
	
	@Override
	public void prepare() {
		cg.clear();
		
		cg.add(((BRS)target).changeRemoveChild(del));
	}
	
	public void setTarget(BRS target){
		this.target=target;
		super.setTarget(target);
	}
	
	public void setSon(ModelObject deleteNode){
		this.del=deleteNode;
	}

}
