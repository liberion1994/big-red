package dk.itu.big_red.model.load_save.loaders;

import dk.itu.big_red.model.load_save.LoaderUtilities;

public abstract class XMLLoader extends org.bigraph.model.loaders.XMLLoader {
	public XMLLoader() {
		new Error("Nullary constructor of XMLLoader").
				fillInStackTrace().printStackTrace();
		LoaderUtilities.installUndecorators(this);
	}
}
