package dk.itu.big_red.editors.bigraph;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.assistants.BigraphCanvas;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;

public class RuleDialog extends Dialog {
	private Bigraph lhs;
	private ArrayList<Change> changes;
	
	public RuleDialog(Shell parentShell) {
		super(parentShell);
		setBlockOnOpen(true);
		setShellStyle(SWT.RESIZE);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setMinimumSize(400, 300);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(600, 500);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite)super.createDialogArea(parent);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		BigraphCanvas bd = new BigraphCanvas(c, SWT.NONE);
		bd.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		lhs.applyChange(lhs.relayout());
		
		ChangeGroup cg = new ChangeGroup();
		for (Layoutable i : lhs.getChildren())
			cg.add(i.changeLayout(i.getLayout().getCopy().translate(-25, -25)));
		lhs.applyChange(cg);
		
		bd.setContents(lhs);
		return c;
	}
	
	public void setLHS(Bigraph clone) {
		lhs = clone;
	}
	
	public void setChanges(ArrayList<Change> changes) {
		this.changes = changes;
	}
	
	public void calculateRHS() {
		
	}
}