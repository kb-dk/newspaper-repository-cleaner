package dk.statsbiblioteket.newspaper.repocleaner;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.TreeProcessorAbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;

import javax.mail.MessagingException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

//TODO tests of everything is missing

public class RepoCleanerRunnableComponent extends TreeProcessorAbstractRunnableComponent {

    private final EnhancedFedora eFedora;
    private final SimpleMailer simpleMailer;
    private List<String> fileDeletionsrecipients;
    private String fileDeletionSubject;
    private String fileDeletionBody;


    private String comment;
    private String relationPredicate;

    protected RepoCleanerRunnableComponent(Properties properties, EnhancedFedora eFedora) {
        super(properties);
        this.eFedora = eFedora;
        simpleMailer = setupMailer(properties);
        fileDeletionSubject = properties.getProperty(ConfigConstants.SUBJECT_PATTERN);
        fileDeletionBody = properties.getProperty(ConfigConstants.BODY_PATTERN);
        comment = properties.getProperty(ConfigConstants.DOMS_COMMIT_COMMENT);
        relationPredicate = properties.getProperty(ConfigConstants.RELATION,
                "info:fedora/fedora-system:def/relations-external#hasPart");

        fileDeletionsrecipients = Arrays.asList(
                properties.getProperty(ConfigConstants.ALERT_EMAIL_ADDRESSES)
                          .split("\\s*,\\s*"));

    }

    private SimpleMailer setupMailer(Properties properties) {
        return new SimpleMailer(
                properties.getProperty(ConfigConstants.EMAIL_FROM_ADDRESS),
                properties.getProperty(ConfigConstants.SMTP_HOST),
                properties.getProperty(ConfigConstants.SMTP_PORT));

    }

    @Override
    public String getEventID() {
        return getProperties().getProperty(ConfigConstants.EVENT_ID);
    }

    @Override
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        Integer roundTrip = batch.getRoundTripNumber();
        if (roundTrip > 1) {
            String batchObjectPid = eFedora.findObjectFromDCIdentifier("path:B" + batch.getDomsID()).get(0);
            for (int i = 1; i < roundTrip; i++) {
                Batch oldBatch = new Batch(batch.getBatchID(), i);
                List<String> pids = eFedora.findObjectFromDCIdentifier("path:" + oldBatch.getFullID());
                if (pids.isEmpty()) {
                    continue;
                }

                CollectorHandler collectorHandler = new CollectorHandler();
                List<TreeEventHandler> handlers = Arrays.asList((TreeEventHandler)collectorHandler);
                EventRunner eventRunner = new EventRunner(createIterator(batch));
                eventRunner.runEvents(handlers, resultCollector);

                //TODO try finally, to make sure the mails are sent??
                deleteBatch(batchObjectPid, collectorHandler.getFirst(),collectorHandler.getPids());

                reportFiles(oldBatch, batch, collectorHandler.getFiles());
            }
        }
    }

    private void deleteBatch(String batchObjectPid, String first, Iterable<String> pids) throws
                                                                                             BackendMethodFailedException,
                                                                                             BackendInvalidResourceException,
                                                                                             BackendInvalidCredsException {
        for (String pid : pids) {
            eFedora.deleteObject(pid, comment);
        }
        eFedora.deleteRelation(batchObjectPid, null, relationPredicate, first, false, comment);
    }

    private void reportFiles(Batch oldBatch, Batch batch, Set<String> files) throws MessagingException {
        simpleMailer.sendMail(
                fileDeletionsrecipients,
                formatSubject(fileDeletionSubject, oldBatch, batch),
                formatBody(fileDeletionBody, oldBatch, batch, files));
    }

    private String formatBody(String fileDeletionBody, Batch oldBatch, Batch batch, Set<String> files) {
        return MessageFormat.format(
                fileDeletionBody,
                batch.getBatchID(),
                batch.getRoundTripNumber(),
                oldBatch.getRoundTripNumber(),
                formatSet(files));
    }

    private String formatSet(Set<String> files) {
        StringBuilder result = new StringBuilder();
        for (String file : files) {
            result.append("\n").append(file.replaceAll("/", "_"));
        }
        return result.toString();
    }

    private String formatSubject(String fileDeletionSubject, Batch oldBatch, Batch batch) {
        return MessageFormat.format(
                fileDeletionSubject, batch.getBatchID(), batch.getRoundTripNumber(), oldBatch.getRoundTripNumber());
    }

}
