package dk.itu.big_red.model.changes;

/**
 * A Change is a reversible modification.
 * @author alec
 *
 */
public abstract class Change {
	/**
	 * Gets a new {@link Change} which, when applied, will reverse this one.
	 * <p><strong>Depending on the {@link Change}, it is possible that this
	 * function will only have a meaningful result <i>after</i> this {@link
	 * Change} has been applied. See {@link #canInvert()}.</strong>
	 * @return this Change's inverse
	 * @see #canInvert()
	 */
	public abstract Change inverse();
	
	/**
	 * Indicates whether or not this {@link Change} needs more information to
	 * be reversible. For example, inverting the change "resize X to 40x40"
	 * requires knowledge of the size of X before the change was made.
	 * @return <code>true</code> if {@link #inverse()} will work, or
	 * <code>false</code> if more information is needed first
	 * @see #beforeApply()
	 */
	public boolean canInvert() {
		return true /* by default; subclasses can override */;
	}
	
	/**
	 * Called by {@link IChangeable}s just before they apply this {@link
	 * Change}.
	 * <p>(Subclasses should override this method if they need to save some
	 * properties of an object before a change in order to be able to {@link
	 * #inverse() reverse} it.)
	 */
	public void beforeApply() {
	}
}
