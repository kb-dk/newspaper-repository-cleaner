package dk.statsbiblioteket.newspaper.repocleaner.util;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for recursively deleting a tree of doms objects by following "hasPart" relations.
 */
public class RecursiveFedoraCleaner extends RecursiveFedoraVisitor<Boolean> {

    private static Logger log = LoggerFactory.getLogger(RecursiveFedoraCleaner.class);

    public RecursiveFedoraCleaner(EnhancedFedora fedora) {
        super(fedora);
    }

    /**
     * Delete the given object from fedora.
     * @param pid the pid of the object.
     * @param doit whether or not to really delete the object.
     * @return whether or not the object was actually deleted.
     */
    @Override
    public Boolean visitObject(String pid, boolean doit)  {
        log.info("About to delete object '" + pid + "'");
        if (doit) {
            try {
                deleteSingleObject(pid);
            } catch (Exception e) {
                log.warn("Could not delete " + pid, e);
                return false;
            }
        } else {
            log.info("Didn't actually delete object '" + pid + "'");
            return false;
        }
       return true;
    }

    private void deleteSingleObject(String pid) throws
                                                BackendInvalidCredsException,
                                                BackendMethodFailedException,
                                                BackendInvalidResourceException {
        fedora.deleteObject(pid, "Deleted in integration test");
    }

}
