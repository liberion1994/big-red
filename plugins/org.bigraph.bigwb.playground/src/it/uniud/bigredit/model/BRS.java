package it.uniud.bigredit.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.Rectangle;


//import it.uniud.bigredit.PlayEditor;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Signature;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeExecutor;


public class BRS extends ModelObject implements IChangeExecutor{
	
	private static ArrayList< Bigraph > diagrams = new ArrayList< Bigraph >();
	private HashMap<ModelObject,Rectangle> children = new HashMap<ModelObject,Rectangle>();
	
	private Signature signature = new Signature();
	private String name;
	
	public String getName(){
		return name;
	}
	
	
	//private AbstractGefEditor editor = null;
	
//	public BRS( PlayEditor editor )
//	{
//		this.editor = editor;
//		this.setLayout( new Rectangle( 0, 0, 1000000, 100000 ) );
//		//diagrams.add( this );
//		
//	}

	
	
	
//	public void setEditor (PlayEditor editor){
//		this.editor = editor;
//	}
	
	/*@Override
	public BRS getBRS() {
		return this;
	}*/
	
	public BRS()
	{
		//setLayout( new Rectangle( 0, 0,  1000000, 100000 ) );
		//diagrams.add( this );
		//Bigraph big= new Bigraph();
		//big.setLayout(new Rectangle (100,100,50,50));
		//this.addBigraph(big);
	}
	
//	public PlayEditor getEditor()
//	{
//		return editor;
//	}
	
	@Override
	public void finalize()
	{
		diagrams.remove( this );
	}
	
	public static ArrayList< Bigraph > getBigraphs()
	{
		return diagrams;
	}
	

	public List<ModelObject> getChildren(){
		
		return (List)children.keySet();
	}
	
	public void addChild(Layoutable child) {
		//addChild(child);
	}

	public void addBigraph( Bigraph bigraph )
	{
		diagrams.add(bigraph);
		//bigraph.setBRS(this);
	}
	
	public Signature getSignature() {
		return signature;
	}

	public void setSignature(Signature signature) {
		this.signature = signature;
	}
	
	
	private void doChange(Change b) {
		
		b.beforeApply();
		if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b)
				doChange(c);
		} /*else if (b instanceof Point.ChangeConnect) {
			Point.ChangeConnect c = (Point.ChangeConnect)b;
			c.link.addPoint(c.getCreator());
		} else if (b instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect c = (Point.ChangeDisconnect)b;
			c.link.removePoint(c.getCreator());
		} else if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			c.getCreator().addChild(c.child);
			c.child.setName(c.name);
			//getNamespace(getNSI(c.child)).put(c.name, c.child);
		} else if (b instanceof Container.ChangeRemoveChild) {
			Container.ChangeRemoveChild c = (Container.ChangeRemoveChild)b;
			c.getCreator().removeChild(c.child);
			//getNamespace(getNSI(c.child)).remove(c.child.getName());
		} else if (b instanceof Layoutable.ChangeLayout) {
			Layoutable.ChangeLayout c = (Layoutable.ChangeLayout)b;
			c.getCreator().setLayout(c.newLayout);
			if (c.getCreator().getParent() instanceof Bigraph)
				((Bigraph)c.getCreator().getParent()).updateBoundaries();
		} else if (b instanceof Edge.ChangeReposition) {
			Edge.ChangeReposition c = (Edge.ChangeReposition)b;
			c.getCreator().averagePosition();
		} else if (b instanceof Colourable.ChangeOutlineColour) {
			Colourable.ChangeOutlineColour c = (Colourable.ChangeOutlineColour)b;
			//c.getCreator().setOutlineColour(c.newColour);
		} else if (b instanceof Colourable.ChangeFillColour) {
			Colourable.ChangeFillColour c = (Colourable.ChangeFillColour)b;
			//c.getCreator().setFillColour(c.newColour);
		} else if (b instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName c = (Layoutable.ChangeName)b;
			//getNamespace(getNSI(c.getCreator())).remove(c.getCreator().getName());
			c.getCreator().setName(c.newName);
			//getNamespace(getNSI(c.getCreator())).put(c.newName, c.getCreator());
		} else if (b instanceof ModelObject.ChangeComment) {
			ModelObject.ChangeComment c = (ModelObject.ChangeComment)b;
			c.getCreator().setComment(c.comment);
		} else if (b instanceof Site.ChangeAlias) {
			Site.ChangeAlias c = (Site.ChangeAlias)b;
			//c.getCreator().setAlias(c.alias);
		}*/
		System.out.println("executed");
		
	}
	

	@Override
	public IFile getFile() {
		// TODO Auto-generated method stub
		return null;
	}


	public Change changeLayoutChild(ModelObject node, Rectangle rectangle) {
		// TODO Auto-generated method stub
		return null;
	}


	public Change changeAddChild(ModelObject node, String string) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void tryApplyChange(Change b) throws ChangeRejectedException {
		// TODO Auto-generated method stub
		
	}







	
}