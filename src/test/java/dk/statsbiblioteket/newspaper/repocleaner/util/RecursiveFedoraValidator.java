package dk.statsbiblioteket.newspaper.repocleaner.util;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.generated.Validation;

/**
 * Class for validating all fedora objects in a tree navigable via "hasPart" relations,
 */
public class RecursiveFedoraValidator extends RecursiveFedoraVisitor<Validation> {

    public RecursiveFedoraValidator(EnhancedFedora fedora) {
        super(fedora);
    }

    /**
     * Validate an object.
     * @param pid the object to validate.
     * @param doit ignored.
     * @return the result of the validation.
     * @throws dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException
     * @throws dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException
     * @throws dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException
     */
    @Override
    protected Validation visitObject(String pid, boolean doit) throws
                                                               BackendInvalidCredsException,
                                                               BackendMethodFailedException,
                                                               BackendInvalidResourceException {
        return fedora.validate(pid);
    }
}
