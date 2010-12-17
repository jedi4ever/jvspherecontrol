package be.jedi.jvspherecontrol;

import java.util.ArrayList;

import org.apache.commons.cli.Option;

/* this class expands the CLI option class */

public class EOption extends Option {

	/* we can provide possible values */
	ArrayList<String> possibleValues = new ArrayList<String>();
	
	public ArrayList<String> getPossibleValues() {
		return possibleValues;
	}

	public void setPossibleValues(String[] possibleValues) {
		this.possibleValues.clear();
		for (int s=0; s<possibleValues.length; s++) {
			this.possibleValues.add(possibleValues[s]);
		}
	}

	public boolean hasPossibleValues() {
		return (possibleValues.size() > 1);
	}
	
	/**
	 * This class extends the Option with possible value
	 */
	private static final long serialVersionUID = 251539803554115856L;

	public EOption(String opt, boolean hasArg, String description)
			throws IllegalArgumentException {
		super(opt, hasArg, description);

	}

	
}
