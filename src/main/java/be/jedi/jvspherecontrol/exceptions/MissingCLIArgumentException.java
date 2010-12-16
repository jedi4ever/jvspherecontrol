package be.jedi.jvspherecontrol.exceptions;

import org.apache.commons.cli.ParseException;

public class MissingCLIArgumentException extends Exception {

    public MissingCLIArgumentException(String s) {
        super(s);
    }

}
