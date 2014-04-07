package dk.statsbiblioteket.newspaper.repocleaner;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Collect pids and urls
 */
public class CollectorHandler implements TreeEventHandler {

    private String roundTripPid = null;
    private Set<String> pids = new HashSet<>();
    private Set<String> files = new HashSet<>();


    /**
     * The jp2 filenames collected
     * @return
     */
    public Set<String> getFiles() {
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
    public Set<String> getPids() {
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
        if (roundTripPid == null){
            roundTripPid = pid;
        }
        pids.add(pid);
        if (event instanceof DataFileNodeBeginsParsingEvent) {
            files.add(event.getName());
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