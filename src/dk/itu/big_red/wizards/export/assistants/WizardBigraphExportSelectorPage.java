package dk.itu.big_red.wizards.export.assistants;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.import_export.Export;
import dk.itu.big_red.utilities.ui.UI;
import dk.itu.big_red.utilities.ui.jface.ConfigurationElementLabelProvider;
import dk.itu.big_red.utilities.ui.jface.ListContentProvider;
import dk.itu.big_red.wizards.export.BigraphExportWizard;

public class WizardBigraphExportSelectorPage extends WizardPage {
	@Override
	public BigraphExportWizard getWizard() {
		return (BigraphExportWizard)super.getWizard();
	}
	
	public WizardBigraphExportSelectorPage(String pageName) {
		super(pageName);
		setPageComplete(false);
		setTitle("Export bigraph to text format");
		setMessage("Select an export format.");
	}

	public void setSelectedExporter(IConfigurationElement m) {
		getWizard().setExporter(m);
		setPageComplete(true);
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite form = new Composite(parent, SWT.NONE);
		
		GridLayout l = new GridLayout();
		l.numColumns = 1;
		form.setLayout(l);
		
		UI.newLabel(form, SWT.NONE, "&Select an export format:");
		
		TableViewer tree = new TableViewer(form, SWT.BORDER);
		tree.setLabelProvider(new ConfigurationElementLabelProvider());
		tree.setContentProvider(new ListContentProvider());
		tree.setInput(RedPlugin.getConfigurationElementsFor(Export.EXTENSION_POINT));
		tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IConfigurationElement e = 
					(IConfigurationElement)
						((IStructuredSelection)event.getSelection()).
							getFirstElement();
				setSelectedExporter(e);
			}
		});
		
		tree.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (isPageComplete())
					getContainer().showPage(getNextPage());
			}
		});
		
		setControl(form);
	}
}
