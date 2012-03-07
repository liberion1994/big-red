package dk.itu.big_red.model.namespaces;

public class PositiveIntegerNamePolicy implements INamePolicy {
	@Override
	public boolean validate(String name) {
		boolean r;
		try {
			int i = Integer.parseInt(name);
			r = (i >= 1 && name.charAt(0) != '0');
		} catch (NumberFormatException e) {
			r = false;
		}
		return r;
	}

	@Override
	public String getName(int value) {
		return Integer.toString(Math.abs(value) + 1);
	}
}