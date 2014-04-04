package dk.statsbiblioteket.newspaper.repocleaner;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;

import java.util.HashSet;
import java.util.Set;

public class CollectorHandler implements TreeEventHandler {

    private String first = null;
    private Set<String> pids = new HashSet<>();
    private Set<String> files = new HashSet<>();


    public Set<String> getFiles() {
        return files;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        String pid = event.getLocation();
        if (first == null){
            first = pid;
        }
        pids.add(pid);
        if (event instanceof DataFileNodeBeginsParsingEvent) {
            files.add(event.getName());
        }


    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        //nothing
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        //nothing
    }

    @Override
    public void handleFinish() {

    }

    public String getFirst() {
        return first;
    }

    public Set<String> getPids() {
        return pids;
    }
}
