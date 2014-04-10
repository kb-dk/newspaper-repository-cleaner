package dk.statsbiblioteket.newspaper.repocleaner;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorageFactory;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.promptdomsingester.component.RunnablePromptDomsIngester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.assertTrue;

public class RepoCleanerRunnableComponentIT {

    private static Logger logger = LoggerFactory.getLogger(RepoCleanerRunnableComponentIT.class);
    private EnhancedFedoraImpl fedora;
    private GreenMail greenMail;
    private String fileDeletionSubjectPattern;
    private String fileDeletionBodyPattern;
    private DomsEventStorage domsEventClient;
    private Properties props;
    Batch oldbatch;
    Batch newbatch;


    public static String BATCH_ID = new Date().getTime() + "";
    public static final int NEW_ROUNDTRIP_NO = 3;
    public static final int OLD_ROUNDTRIP_NO = 1;


    @BeforeMethod(groups = "integrationTest")
    public void setUp() throws Exception {
        logger.debug("Doing setUp.");
        props = new Properties();
        try {
            final String propsfile = System.getProperty("integration.test.newspaper.properties");
            logger.debug("Loading properties from {}.", propsfile);
            props.load(new FileReader(new File(propsfile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        fileDeletionSubjectPattern = props.getProperty(dk.statsbiblioteket.newspaper.repocleaner.ConfigConstants.SUBJECT_PATTERN);
        fileDeletionBodyPattern = props.getProperty(dk.statsbiblioteket.newspaper.repocleaner.ConfigConstants.BODY_PATTERN);


        ServerSetup serverSetup = new ServerSetup(
                Integer.parseInt(props.getProperty(dk.statsbiblioteket.newspaper.repocleaner.ConfigConstants.SMTP_PORT)),
                props.getProperty(dk.statsbiblioteket.newspaper.repocleaner.ConfigConstants.SMTP_HOST),
                ServerSetup.SMTP.getProtocol());
        greenMail = new GreenMail(serverSetup);
        greenMail.stop();
        greenMail.start();


        Credentials creds = new Credentials(
                props.getProperty(ConfigConstants.DOMS_USERNAME), props.getProperty(
                ConfigConstants.DOMS_PASSWORD)
        );
        String fedoraLocation = props.getProperty(ConfigConstants.DOMS_URL);
        fedora = new EnhancedFedoraImpl(
                creds, fedoraLocation, props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL), null);

        oldbatch = new Batch(BATCH_ID, OLD_ROUNDTRIP_NO);
        newbatch = new Batch(BATCH_ID, NEW_ROUNDTRIP_NO);


        props.setProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER, "target");


        DomsEventStorageFactory domsEventClientFactory = new DomsEventStorageFactory();
        domsEventClientFactory.setFedoraLocation(props.getProperty(ConfigConstants.DOMS_URL));
        domsEventClientFactory.setUsername(props.getProperty(ConfigConstants.DOMS_USERNAME));
        domsEventClientFactory.setPassword(props.getProperty(ConfigConstants.DOMS_PASSWORD));
        domsEventClientFactory.setPidGeneratorLocation(props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL));
        logger.debug("Creating doms client");
        domsEventClient = domsEventClientFactory.createDomsEventStorage();
        domsEventClient.addEventToBatch(
                newbatch.getBatchID(),
                newbatch.getRoundTripNumber(),
                "repoCleanerIntegrationTest",
                new Date(),
                "",
                "Data_Received",
                false);
        domsEventClient.addEventToBatch(
                oldbatch.getBatchID(),
                oldbatch.getRoundTripNumber(),
                "repoCleanerIntegrationTest",
                new Date(),
                "",
                "Data_Received",
                false);


        generateTestBatch(oldbatch);
        IngestRoundtripInDoms(oldbatch);

        generateTestBatch(newbatch);
        IngestRoundtripInDoms(newbatch);

    }

    @AfterMethod(groups = "integrationTest")
    public void tearDown() throws Exception {
        logger.debug("Doing tearDown.");
        try {
            props.setProperty(
                    ConfigConstants.ITERATOR_USE_FILESYSTEM, "false");
            RepoCleanerRunnableComponent cleaner = new RepoCleanerRunnableComponent(props, fedora);
            ResultCollector resultCollector = new ResultCollector("foo", "bar");
            cleaner.doWorkOnBatch(new Batch(newbatch.getBatchID(), newbatch.getRoundTripNumber() + 1), resultCollector);
            List<String> batches = fedora.findObjectFromDCIdentifier("path:B" + newbatch.getBatchID());
            for (String batch : batches) {
                fedora.deleteObject(batch,"deleting after repo cleaner integration test");
            }
        } finally {
            greenMail.stop();
        }
    }


    private void generateTestBatch(Batch batch) throws IOException, InterruptedException {
        String testdataDir = System.getProperty("integration.test.newspaper.testdata");
        logger.debug("Reading testdata from " + testdataDir);
        String generateBatchScript
                = new File(testdataDir).getAbsolutePath() + "/generate-test-batch/bin/generateTestData.sh";

        ProcessBuilder processBuilder = new ProcessBuilder(generateBatchScript);
        Map<String, String> env = processBuilder.environment();
        env.put("outputDir", "target");
        env.put("numberOfFilms", "1");
        env.put("filmNoOfPictures", "5");
        env.put("avisID", "colinstidende");
        env.put("batchID", batch.getBatchID());
        env.put("roundtripID", batch.getRoundTripNumber() + "");
        env.put("startDate", "1964-03-27");
        env.put("workshiftTargetSerialisedNumber", "000001");
        env.put("workshiftTargetPages", "1");
        env.put("isoTargetPages", "2");
        env.put("pagesPerEdition", "2");
        env.put("editionsPerDate", "1");
        env.put("pagesPerUnmatched", "1");
        env.put("probabilitySplit", "50");
        env.put("probabilityBrik", "20");
        logger.debug("Generating batch: B{}-RT{}", batch.getBatchID(), batch.getRoundTripNumber());
        Process process = processBuilder.start();
        process.waitFor();
        logger.debug("Batch generation finished.");
    }


    private void IngestRoundtripInDoms(Batch batch) {
        props.setProperty(
                ConfigConstants.ITERATOR_USE_FILESYSTEM, "true");
        RunnablePromptDomsIngester ingester = new RunnablePromptDomsIngester(props, fedora);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        ingester.doWorkOnBatch(batch, resultCollector);
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
        //TODO addDataReceived failure
    }


    /**
     * Clean one batch.
     *
     * @throws Exception
     */
    @Test(groups = "integrationTest")
    public void testDoWorkOnBatch() throws Exception {
        props.setProperty(
                ConfigConstants.ITERATOR_USE_FILESYSTEM, "false");
        RepoCleanerRunnableComponent cleaner = new RepoCleanerRunnableComponent(props, fedora);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        cleaner.doWorkOnBatch(newbatch, resultCollector);
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());

        assertMailSent(oldbatch,newbatch);

    }

    private void assertMailSent(Batch oldbatch, Batch newbatch) throws MessagingException {
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        //There could be other batches that trigger emails so check that there is one from us
        boolean found = false;
        for (MimeMessage message : receivedMessages) {
            if (message.getSubject().contains(
                    RepoCleanerRunnableComponent.formatSubject(
                            fileDeletionSubjectPattern,
                            oldbatch,
                            newbatch))){
                found = true;
            }
        }
        assertTrue(found);
    }


}


