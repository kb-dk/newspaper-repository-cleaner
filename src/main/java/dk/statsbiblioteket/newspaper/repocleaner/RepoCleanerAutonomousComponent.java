package dk.statsbiblioteket.newspaper.repocleaner;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.BatchItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorageFactory;
import dk.statsbiblioteket.medieplatform.autonomous.CallResult;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperBatchAutonomousComponentUtils;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperDomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperDomsEventStorageFactory;
import dk.statsbiblioteket.medieplatform.autonomous.RunnableComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;

import java.io.IOException;
import java.util.Properties;

public class RepoCleanerAutonomousComponent {


    private static Logger log = LoggerFactory.getLogger(RepoCleanerAutonomousComponent.class);

    /**
     * The class must have a main method, so it can be started as a command line tool
     *
     * @param args the arguments.
     *
     * @throws Exception
     * @see dk.statsbiblioteket.medieplatform.autonomous.SBOIDomsAutonomousComponentUtils#parseArgs(String[])
     */
    public static void main(String... args) throws Exception {
        System.exit(doMain(args));
    }

    private static int doMain(String[] args) throws IOException, JAXBException, PIDGeneratorException {
        log.info("Starting with args {}", new Object[]{args});

        //Parse the args to a properties construct
        Properties properties = NewspaperBatchAutonomousComponentUtils.parseArgs(args);

        Credentials creds = new Credentials(
                properties.getProperty(ConfigConstants.DOMS_USERNAME),
                properties.getProperty(ConfigConstants.DOMS_PASSWORD));
        String fedoraLocation = properties.getProperty(ConfigConstants.DOMS_URL);
        int fedoraRetries = Integer.parseInt(properties.getProperty(ConfigConstants.FEDORA_RETRIES, "1"));
        int fedoraDelayBetweenRetries = Integer.parseInt(properties.getProperty(ConfigConstants.FEDORA_DELAY_BETWEEN_RETRIES, "100"));
        EnhancedFedoraImpl eFedora = new EnhancedFedoraImpl(
                creds, fedoraLocation, null, null, fedoraRetries, fedoraRetries, fedoraRetries, fedoraDelayBetweenRetries);

        NewspaperDomsEventStorageFactory domsEventStorageFactory = new NewspaperDomsEventStorageFactory();
        domsEventStorageFactory.setFedoraLocation(properties.getProperty(ConfigConstants.DOMS_URL));
        domsEventStorageFactory.setUsername(properties.getProperty(ConfigConstants.DOMS_USERNAME));
        domsEventStorageFactory.setPassword(properties.getProperty(ConfigConstants.DOMS_PASSWORD));
        domsEventStorageFactory.setItemFactory(new BatchItemFactory());
        NewspaperDomsEventStorage domsEventStorage = domsEventStorageFactory.createDomsEventStorage();

        //make a new runnable component from the properties
        RunnableComponent component = new RepoCleanerRunnableComponent(properties, eFedora, domsEventStorage, setupMailer(properties));

        CallResult result = NewspaperBatchAutonomousComponentUtils.startAutonomousComponent(properties, component);
        log.info(result.toString());
        return result.containsFailures();
    }

    private static SimpleMailer setupMailer(Properties properties) {
        return new SimpleMailer(
                properties.getProperty(dk.statsbiblioteket.newspaper.repocleaner.ConfigConstants.EMAIL_FROM_ADDRESS),
                properties.getProperty(dk.statsbiblioteket.newspaper.repocleaner.ConfigConstants.SMTP_HOST),
                properties.getProperty(dk.statsbiblioteket.newspaper.repocleaner.ConfigConstants.SMTP_PORT));

    }


}
