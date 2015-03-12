package de.uni.bremen.monty.moco.visitor.typeinf;

import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.exception.MontyBaseException;

public class TypeInferenceException extends MontyBaseException {

    /** */
    private static final long serialVersionUID = 1L;

    TypeInferenceException(Location location, String message) {
        super(location, message);
    }

}
