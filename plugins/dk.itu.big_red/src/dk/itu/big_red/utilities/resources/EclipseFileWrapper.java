package dk.itu.big_red.utilities.resources;

import java.io.InputStream;

import org.bigraph.model.ModelObject;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.loaders.Loader;
import org.bigraph.model.resources.IFileWrapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;

import dk.itu.big_red.model.load_save.LoaderUtilities;

public class EclipseFileWrapper extends EclipseResourceWrapper
		implements IFileWrapper {
	private final IFile file;

	@Override
	public IFile getResource() {
		return file;
	}
	
	public EclipseFileWrapper(IFile file) {
		this.file = file;
	}

	@Override
	public ModelObject load() throws LoadFailedException {
		try {
			IContentType ct =
					getResource().getContentDescription().getContentType();
			for (IConfigurationElement ice :
				RegistryFactory.getRegistry().
					getConfigurationElementsFor(LoaderUtilities.EXTENSION_POINT)) {
				if (ct.getId().equals(ice.getAttribute("contentType"))) {
					Loader i = (Loader)ice.createExecutableExtension("class");
					i.setFile(this).setInputStream(getContents());
					if (i.canImport()) {
						return i.importObject();
					} else {
						throw new LoadFailedException("The loader for " +
								file + " could not be configured properly");
					}
				}
			}
			throw new LoadFailedException("No loader was found for " + file);
		} catch (CoreException ce) {
			throw new LoadFailedException(ce);
		}
	}
	
	@Override
	public InputStream getContents() throws LoadFailedException {
		try {
			return getResource().getContents();
		} catch (CoreException ce) {
			throw new LoadFailedException(ce);
		}
	}
}