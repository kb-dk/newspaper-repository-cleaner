package dk.statsbiblioteket.newspaper.repocleaner.util;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generified visitor which can recurse of a tree of objects ingested via prompt-doms-ingester.
 */
public abstract class RecursiveFedoraVisitor<T> {

    static String hasPartRelation = "info:fedora/fedora-system:def/relations-external#hasPart";
    private static Logger log = LoggerFactory.getLogger(RecursiveFedoraVisitor.class);
    private Map<String, T> results;


    protected EnhancedFedora fedora;

    protected RecursiveFedoraVisitor(EnhancedFedora fedora) {
        this.fedora = fedora;
        results = new HashMap<>();
    }

    /**
     * Recursively visits every object with the given label and every object reachable
     * from them via a hasPart relation. The descendants of a given object are always
     * determined before the object itself is processed.
     * @param label the label of the root objects to be visited.
     * @param doit boolean to be passed on to visitObject method which can be used to tell the
     *             method whether or not to actually carry out the operation.
     * @return a map listing all the pids visited with the result of the visit.
     */
    public Map<String, T> visitTree(String label, boolean doit) throws
                                                                BackendInvalidCredsException,
                                                                BackendMethodFailedException,
                                                                BackendInvalidResourceException {
        List<String> pids = fedora.findObjectFromDCIdentifier(label);
        if (pids.isEmpty()) {
            log.info("Nothing to visit with label " + label);
            return results;
        }
        return visitTreeByPid(pids.get(0), doit);
    }

    private Map<String, T> visitTreeByPid(String pid, boolean doit) throws
                                                                    BackendInvalidCredsException,
                                                                    BackendMethodFailedException,
                                                                    BackendInvalidResourceException {
        List<FedoraRelation> relations = fedora.getNamedRelations(pid, hasPartRelation, new Date().getTime());
        for (FedoraRelation relation: relations) {
            String nextPid = relation.getObject();
            if (!pid.equals(nextPid)) {
                visitTreeByPid(nextPid, doit);
            }
            results.put(pid, visitObject(pid, doit));
        }
        return results;
    }

    /**
     * Method used by implementing classes to carry out the actual behaviour of the visit.
     * @param pid the pid of the object.
     * @param doit Whether or not to carry out the operation.
     * @return the result of the visit.
     * @throws dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException
     * @throws dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException
     * @throws dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException
     */
    protected abstract T visitObject(String pid, boolean doit)
            throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException;

}
