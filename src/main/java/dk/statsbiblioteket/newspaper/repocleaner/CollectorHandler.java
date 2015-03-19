package dk.statsbiblioteket.newspaper.repocleaner;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collect pids and urls
 */
public class CollectorHandler extends DefaultTreeEventHandler {
    private static Logger log = LoggerFactory.getLogger(CollectorHandler.class);

    private String roundTripPid = null;
    private Collection<String> pids = new HashSet<>();
    private SortedSet<String> files = new TreeSet<>();


    /**
     * The jp2 filenames collected
     * @return
     */
    public Collection<String> getFiles() {
        return files;
    }

    /**
     * The roundTripPid pid collected. This shold be the round trip obejct
     * @return
     */
    public String getRoundTripPid() {
        return roundTripPid;
    }

    /**
     * The list of pids collected, including the roundTripPid
     * @return
     */
    public Collection<String> getPids() {
        return pids;
    }


    /**
     * Collect the pid by the getLocation() method on the event. If the event is a DataFileNodeBeginsParsingEvent, collect
     * the event name.
     * @param event the event
     */
    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        String pid = event.getLocation();
        log.trace("Found pid '{}' for event with name: '{}'", pid, event.getName());
        if (roundTripPid == null){
            roundTripPid = pid;
        }
        pids.add(pid);
        if (event instanceof DataFileNodeBeginsParsingEvent) {
            //TODO remember that the file might not have been ingested in the bit repository just because the file object is in doms
            files.add(event.getName());
            log.trace("Marking pid '{}' for deletion in bitrepository", pid);
        }


    }

    /**
     * Empty
     * @param event
     */
    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        //nothing
    }

    /**
     * Empty
     *
     * @param event
     */
    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        //nothing
    }

    /**
     * Empty
     *
     */
    @Override
    public void handleFinish() {
    }

}
