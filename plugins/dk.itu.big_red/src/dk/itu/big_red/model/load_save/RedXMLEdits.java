package dk.itu.big_red.model.load_save;

import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.EditXMLLoader;
import org.w3c.dom.Element;

public abstract class RedXMLEdits {
	private RedXMLEdits() {}
	
	public static final class LoadParticipant
			implements EditXMLLoader.Participant {
		@Override
		public IChangeDescriptor getDescriptor(Element descriptor) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IChangeDescriptor getRenameDescriptor(Element id, String name) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
