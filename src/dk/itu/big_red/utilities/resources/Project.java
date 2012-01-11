package dk.itu.big_red.utilities.resources;

import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IFileEditorInput;

import dk.itu.big_red.utilities.io.IOAdapter;

/**
 * Utility functions for manipulating an Eclipse {@link IProject project} and
 * the {@link IResource resources} they contain.
 * @author alec
 *
 */
public class Project {
	/**
	 * Gets the workspace.
	 * @return the workspace
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	/**
	 * Gets the workspace root.
	 * @return the workspace root
	 */
	public static IWorkspaceRoot getWorkspaceRoot() {
		return getWorkspace().getRoot();
	}
	
	/**
	 * Gets, and opens, an {@link IProject}. If it doesn't exist already, then
	 * it's created.
	 * @param name the project's name
	 * @return an IProject guaranteed to exist
	 * @throws CoreException if {@link
	 * IProject#create(org.eclipse.core.runtime.IProgressMonitor)} goes wrong
	 */
	public static IProject getProject(String name) throws CoreException {
		IProject p = getWorkspaceRoot().getProject(name);
		if (!p.exists())
			p.create(null);
		p.open(null);
		return p;
	}
	
	/**
	 * Indicates whether or not the named project exists.
	 * @param name the project's name
	 * @return whether the project exists or not
	 */
	public static boolean projectExists(String name) {
		return getWorkspaceRoot().getProject(name).exists();
	}
	
	/**
	 * Gets an {@link IFolder} contained by <code>c</code>. If it doesn't exist
	 * already, then it's created.
	 * @param c the parent {@link IContainer}
	 * @param name the folder's name
	 * @return an IFolder guaranteed to exist on the local filesystem
	 * @throws CoreException if {@link IFolder#create(int, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)} goes wrong
	 */
	public static IFolder getFolder(IContainer c, String name) throws CoreException {
		IFolder f = c.getFolder(new Path(name));
		if (!f.exists())
			f.create(0, true, null);
		return f;
	}
	
	/**
	 * Indicates whether or not the named folder exists as a child of
	 * <code>c</code>.
	 * @param c the parent {@link IContainer}
	 * @param name the folder's name
	 * @return whether the folder exists or not
	 */
	public static boolean folderExists(IContainer c, String name) {
		return c.getFolder(new Path(name)).exists();
	}
	
	/**
	 * Gets an {@link IFile} contained by <code>c</code>. If it doesn't exist
	 * already, then it's created (as an empty file).
	 * @param c the parent {@link IContainer}
	 * @param name the file's name
	 * @return an IFile guaranteed to exist on the local filesystem
	 * @throws CoreException if {@link IFile#create(java.io.InputStream,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)} goes wrong
	 */
	public static IFile getFile(IContainer c, String name) throws CoreException {
		IFile f = c.getFile(new Path(name));
		if (!f.exists())
			f.create(IOAdapter.getNullInputStream(), true, null);
		return f;
	}
	
	/**
	 * Indicates whether or not the named file exists as a child of
	 * <code>c</code>.
	 * @param c the parent {@link IContainer}
	 * @param name the file's name
	 * @return whether the file exists or not
	 */
	public static boolean fileExists(IContainer c, String name) {
		return c.getFile(new Path(name)).exists();
	}
	
	/**
	 * Indicates whether or not <code>c</code> contains a resource with the
	 * given path.
	 * @param c an {@link IContainer}
	 * @param path the path of a would-be child
	 * @return whether the path identifies an extant resource or not
	 */
	public static boolean pathExists(IContainer c, IPath path) {
		return (c.findMember(path) != null);
	}
	
	/**
	 * Gets a resource with the given path (relative to <code>c</code>). Note
	 * that this method will <i>not</i> create the resource if it doesn't
	 * already exist.
	 * @param c the container to search in, or - if <code>null</code> - the workspace root
	 * @param path the path of a would-be resource
	 * @return the {@link IResource} requested, or <code>null</code> if it
	 * doesn't exist
	 */
	public static IResource findResourceByPath(IContainer c, IPath path) {
		if (c == null)
			c = getWorkspaceRoot();
		return c.findMember(path);
	}
	
	/**
	 * Gets a container with the given path (relative to <code>c</code>). Note
	 * that this method will <i>not</i> create the container if it doesn't
	 * already exist.
	 * @param c the container to search in, or - if <code>null</code> - the workspace root
	 * @param path the path of a would-be container
	 * @return the {@link IContainer} requested, or <code>null</code> if it
	 * doesn't exist
	 * @see Project#findResourceByPath
	 */
	public static IContainer findContainerByPath(IContainer c, IPath path) {
		IResource r = findResourceByPath(c, path);
		return (r instanceof IContainer ? (IContainer)r : null);
	}
	
	/**
	 * Gets a file with the given path (relative to <code>c</code>). Note that
	 * this method will <i>not</i> create the file if it doesn't already exist.
	 * @param c the container to search in, or - if <code>null</code> - the workspace root
	 * @param path the path of a would-be file
	 * @return the {@link IFile} requested, or <code>null</code> if it doesn't
	 * exist
	 */
	public static IFile findFileByPath(IContainer c, IPath path) {
		IResource r = findResourceByPath(c, path);
		return (r instanceof IFile ? (IFile)r : null);
	}
	
	/**
	 * Tries desperately to get an {@link IResource} out of the given {@link
	 * IStructuredSelection}, by hook or by crook.
	 * @param selection an IStructuredSelection, which is a spectacularly
	 *                  disingenuous name because it has almost no structure at
	 *                  all
	 * @return an IResource, or <code>null</code> if all this method's effort
	 *         was for nothing, you hear me? <i>Nothing</i>
	 */
	public static IResource tryDesperatelyToGetAnIResourceOutOfAnIStructuredSelection(IStructuredSelection selection) {
		for (Object i : selection.toArray()) {
			if (i instanceof IResource)
				return (IResource)i;
			else if (i instanceof EditPart)
				/* Let the Java... BEGIN! */
				return ((IFileEditorInput)(
					     (DefaultEditDomain)(
					      (EditPart)i).getViewer().getEditDomain())
					     .getEditorPart().getEditorInput()).getFile();
		}
		return null;
	}
	
	/**
	 * Indicates whether or not the two {@link IFile}s provided belong to the
	 * same {@link IProject}.
	 * @param one the first {@link IFile} to compare
	 * @param two the second {@link IFile} to compare
	 * @return whether the two IFiles belong to the same project or not
	 */
	public static boolean compareProjects(IFile one, IFile two) {
		return (one != null && two != null &&
				one.getProject().equals(two.getProject()));
	}
	
	public static void setContents(IFile file, InputStream contents) throws CoreException {
		file.setContents(contents, 0, null);
	}
	
	public static IPath getRelativePath(IResource relativeTo, IResource resource) {
		IPath relativeToContainer = null;
		if (relativeTo instanceof IContainer) {
			relativeToContainer = relativeTo.getFullPath();
		} else if (relativeTo instanceof IFile) {
			relativeToContainer = relativeTo.getParent().getFullPath();
		}
		
		if (relativeToContainer != null) {
			return resource.getFullPath().makeRelativeTo(relativeToContainer);
		} else return resource.getFullPath();
	}
}