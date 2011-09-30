package dk.itu.big_red.import_export;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.eclipse.draw2d.IFigure;


/**
 * Classes extending Export can write objects to an {@link OutputStream}. (The
 * export process can do anything it wants - one class might export an {@link
 * IFigure} to a PNG image, and another might export a {@link Signature} to a
 * XML document.)
 * 
 * <p>The existence of an Export class for a given format does <i>not</i> imply
 * that a {@link Import} class should exist for that format - in most cases,
 * that'd be impossible (try importing a bigraph from a PNG!).
 * @see Import
 * @author alec
 *
 */
public abstract class Export<T> {
	private T model = null;
	
	/**
	 * Returns the model object previously set with {@link #setModel(T)}.
	 * @return the model object
	 */
	public T getModel() {
		return model;
	}

	/**
	 * Sets the model object to be exported.
	 * @param model
	 * @return <code>this</code>, for convenience
	 */
	public Export<T> setModel(T model) {
		this.model = model;
		return this;
	}
	
	protected OutputStream target = null;
	
	/**
	 * Sets the target of the export to the given {@link OutputStream}. The
	 * OutputStream will be closed once the output has been written.
	 * @param os an OutputStream
	 * @return <code>this</code>, for convenience
	 */
	public Export<T> setOutputStream(OutputStream os) {
		if (os != null)
			this.target = os;
		return this;
	}
	
	/**
	 * Sets the target of the export to the file specified.
	 * 
	 * <p>Equivalent to
	 * <code>setOutputStream(new FileOutputStream(path))</code>.
	 * @param path a path to a file
	 * @throws FileNotFoundException if
	 *         {@link FileOutputStream#FileOutputStream(String)} fails
	 * @return <code>this</code>, for convenience
	 */
	public Export<T> setOutputFile(String path) throws FileNotFoundException {
		return setOutputStream(new FileOutputStream(path));
	}
	
	/**
	 * Indicates whether or not the object is ready to be exported.
	 * @return <code>true</code> if the object is ready to be exported, or
	 *         <code>false</code> otherwise
	 */
	public boolean canExport() {
		return (this.model != null && this.target != null);
	}
	
	/**
	 * Exports the object. This function should not be called unless {@link
	 * Export#canExport canExport} returns <code>true</code>.
	 * @throws ExportFailedException if the export failed
	 */
	public abstract void exportObject() throws ExportFailedException;
}
