package dk.statsbiblioteket.newspaper.repocleaner;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.*;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
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
        Properties properties = SBOIDomsAutonomousComponentUtils.parseArgs(args);

        Credentials creds = new Credentials(
                properties.getProperty(ConfigConstants.DOMS_USERNAME),
                properties.getProperty(ConfigConstants.DOMS_PASSWORD));
        String fedoraLocation = properties.getProperty(ConfigConstants.DOMS_URL);
        EnhancedFedoraImpl eFedora = new EnhancedFedoraImpl(
                creds, fedoraLocation, null, null);


        //make a new runnable component from the properties
        RunnableComponent component = new RepoCleanerRunnableComponent(properties,eFedora);

        CallResult result = SBOIDomsAutonomousComponentUtils.startAutonomousComponent(properties, component);
        log.info(result.toString());
        return result.containsFailures();
    }
}
