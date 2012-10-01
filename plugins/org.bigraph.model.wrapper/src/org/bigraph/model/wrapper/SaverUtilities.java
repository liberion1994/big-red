package org.bigraph.model.wrapper;

import org.bigraph.model.savers.ISaver;
import org.bigraph.model.savers.Saver;
import org.bigraph.model.savers.ISaver.Participant;
import org.bigraph.model.savers.ISaver.InheritableParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;

public abstract class SaverUtilities {
	private static final class ParticipantContributor
			implements InheritableParticipant {
		@Override
		public void setSaver(ISaver saver) {
			IExtensionRegistry r = RegistryFactory.getRegistry();
			for (IConfigurationElement ice :
					r.getConfigurationElementsFor(EXTENSION_POINT)) {
				if ("participant".equals(ice.getName())) {
					try {
						saver.addParticipant((Participant)
								ice.createExecutableExtension("class"));
					} catch (CoreException e) {
						e.printStackTrace();
						/* do nothing */
					}
				}
			}
		}
		
		@Override
		public InheritableParticipant newInstance() {
			return new ParticipantContributor();
		}
	}
	
	private SaverUtilities() {}
	
	public static final String EXTENSION_POINT =
			"org.bigraph.model.wrapper.export";

	public static Saver installParticipants(Saver saver) {
		if (saver != null)
			saver.addParticipant(new ParticipantContributor());
		return saver;
	}
	
	public static Saver forContentType(String ct) throws CoreException {
		return forContentType(
				Platform.getContentTypeManager().getContentType(ct));
	}
	
	public static Saver forContentType(IContentType ct) throws CoreException {
		Saver s = null;
		for (IConfigurationElement ice :
				RegistryFactory.getRegistry().
				getConfigurationElementsFor(EXTENSION_POINT)) {
			if (ct.getId().equals(ice.getAttribute("contentType"))) {
				s = (Saver)ice.createExecutableExtension("class");
				break;
			}
		}
		return installParticipants(s);
	}
}
