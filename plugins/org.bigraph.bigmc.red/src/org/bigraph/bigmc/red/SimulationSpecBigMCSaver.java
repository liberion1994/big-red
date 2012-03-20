package org.bigraph.bigmc.red;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.interfaces.IChild;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IOuterName;
import dk.itu.big_red.model.interfaces.IPort;
import dk.itu.big_red.model.interfaces.IRoot;
import dk.itu.big_red.model.interfaces.ISite;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.Saver;

public class SimulationSpecBigMCSaver extends Saver {
	private OutputStreamWriter osw = null;
	
	private boolean namedRules = true;
	
	{
		addOption("NameRules", "Export named rules");
	}
	
	@Override
	public Object getOption(String id) {
		if (id.equals("NameRules")) {
			return namedRules;
		} else return super.getOption(id);
	}
	
	@Override
	public void setOption(String id, Object value) {
		if (id.equals("NameRules")) {
			namedRules = (Boolean)value;
		} else super.setOption(id, value);
	}
	
	private static Pattern p = Pattern.compile("[^a-zA-Z0-9_]");
	
	private String normaliseName(String name) {
		return p.matcher(name).replaceAll("_");
	}
	
	@Override
	public SimulationSpec getModel() {
		return (SimulationSpec)super.getModel();
	}
	
	@Override
	public SimulationSpecBigMCSaver setModel(ModelObject model) {
		if (model instanceof SimulationSpec)
			super.setModel(model);
		return this;
	}
	
	private void write(String str) throws SaveFailedException {
		try {
			osw.write(str);
		} catch (IOException e) {
			throw new SaveFailedException(e);
		}
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		osw = new OutputStreamWriter(getOutputStream());
		processSimulationSpec(getModel());
		try {
			osw.close();
		} catch (IOException e) {
			throw new SaveFailedException(e);
		}
	}

	private void processSignature(Signature s) throws SaveFailedException {
		write("# Controls\n");
		for (Control c : s.getControls()) {
			switch (c.getKind()) {
			case ACTIVE:
			case ATOMIC:
			default:
				write("%active ");
				break;
			case PASSIVE:
				write("%passive ");
				break;
			}
			write(normaliseName(c.getName()) + " : ");
			write(c.getPorts().size() + ";\n");
		}
		write("\n");
	}
	
	private void processNames(SimulationSpec s) throws SaveFailedException {
		ArrayList<String> names = new ArrayList<String>();
		for (IOuterName o : s.getModel().getIOuterNames())
			names.add(normaliseName(o.getName()));
		Collections.sort(names);
		
		if (names.size() == 0)
			return;
		write("# Names\n");
		for (String name : names)
			write("%name " + name + ";\n");
		write("\n");
	}
	
	private String getPortString(ILink l) {
		return (l != null ? normaliseName(l.getName()) : "-");
	}
	
	private void processChild(IChild i) throws SaveFailedException {
		if (i instanceof ISite) {
			processSite((ISite)i);
		} else if (i instanceof INode) {
			processNode((INode)i);
		}
	}
	
	private void processSite(ISite i) throws SaveFailedException {
		Site s = (Site)i; /* XXX!! */
		write("$" + (s.getAlias() == null ? s.getName() : s.getAlias()));
	}
	
	private void processNode(INode i) throws SaveFailedException {
		write(normaliseName(i.getControl().getName()));
		
		Iterator<? extends IPort> it = i.getPorts().iterator();
		if (it.hasNext()) {
			write("[" + getPortString(it.next().getLink()));
			while (it.hasNext())
				write("," + getPortString(it.next().getLink()));
			write("]");
		}
		
		Iterator<? extends IChild> in = i.getIChildren().iterator();
		if (in.hasNext()) {
			write(".");
			IChild firstChild = in.next();
			if (in.hasNext()) {
				write("(");
				processChild(firstChild);
				while (in.hasNext()) {
					write(" | ");
					processChild(in.next());
				}
				write(")");
			} else processChild(firstChild);
		}
	}
	
	private void processRoot(IRoot i) throws SaveFailedException {
		Iterator<? extends INode> in = i.getINodes().iterator();
		boolean anyNodes = in.hasNext();
		if (anyNodes) {
			processNode(in.next());
			while (in.hasNext()) {
				write(" | ");
				processNode(in.next());
			}
		}
		
		Iterator<? extends ISite> is = i.getISites().iterator();
		if (is.hasNext()) {
			if (anyNodes)
				write(" | ");
			processSite(is.next());
			while (is.hasNext()) {
				write(" | ");
				processSite(is.next());
			}
		}
	}
	
	private void processBigraph(Bigraph b) throws SaveFailedException {
		Iterator<? extends IRoot> ir = b.getIRoots().iterator();
		if (ir.hasNext()) {
			processRoot(ir.next());
			while (ir.hasNext()) {
				write(" || ");
				processRoot(ir.next());
			}
		}
	}
	
	private static <T, V>
	boolean iteratorsMatched(Iterator<T> i, Iterator<V> j) {
		while (i.hasNext() && j.hasNext()) {
			i.next(); j.next();
		}
		return (i.hasNext() == j.hasNext());
	}
	
	private void processRule(ReactionRule r) throws SaveFailedException {
		if (!iteratorsMatched(
				r.getRedex().getIRoots().iterator(),
				r.getReactum().getIRoots().iterator()))
			throw new SaveFailedException("Bananas");
		if (namedRules)
			write("%rule " +
			      normaliseName(
			    		  r.getFile().getProjectRelativePath().toString()) +
			      " ");
		processBigraph(r.getRedex());
		write(" -> ");
		processBigraph(r.getReactum());
		write(";\n");
	}
	
	private void processModel(Bigraph b) throws SaveFailedException {
		processBigraph(b);
		write(";\n");
	}
	
	private void processSimulationSpec(SimulationSpec s) throws SaveFailedException {
		processSignature(s.getSignature());
		processNames(s);
		
		List<ReactionRule> rules = s.getRules();
		if (rules.size() != 0) {
			write("# Rules\n");
			for (ReactionRule r : s.getRules())
				processRule(r);
			write("\n");
		}
		
		write("# Model\n");
		processModel(s.getModel());
		
		write("\n# Go!\n%check;\n");
	}
}
