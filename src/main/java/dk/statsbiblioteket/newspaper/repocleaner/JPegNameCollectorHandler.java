package dk.statsbiblioteket.newspaper.repocleaner;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;

import java.util.HashSet;
import java.util.Set;


public class JPegNameCollectorHandler implements TreeEventHandler {

    private Set<String> files = new HashSet<>();
    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (event instanceof DataFileNodeBeginsParsingEvent) {
            DataFileNodeBeginsParsingEvent dataFileNodeBeginsParsingEvent = (DataFileNodeBeginsParsingEvent) event;
            files.add(event.getName());
        }

    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {

    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {

    }

    @Override
    public void handleFinish() {

    }

    public Set<String> getFiles() {
        return files;
    }
}
