package dk.statsbiblioteket.newspaper.repocleaner;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperDomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RepoCleanerRunnableComponentTest {

    private static final String MOCKUUID = "uuid:21767d13-68ed-4455-ab6b-090473e7e51a";

    @Test
    public void testFailOnLargerRoundtrip() throws Exception {
        Batch batch1 = new Batch("1234", 1);
        Batch batch2 = new Batch("1234", 2);

        EnhancedFedora efedora = mock(EnhancedFedora.class);
        when(efedora.findObjectFromDCIdentifier(anyString())).thenReturn(Arrays.asList(MOCKUUID));

        NewspaperDomsEventStorage domsStorage = mock(NewspaperDomsEventStorage.class);
        when(domsStorage.getAllRoundTrips("1234")).thenReturn(Arrays.asList(batch1, batch2));

        SimpleMailer mailer = mock(SimpleMailer.class);

        Properties props = new Properties();
        props.setProperty(ConfigConstants.ALERT_EMAIL_ADDRESSES, "test@example.org");

        ResultCollector resultCollector = new ResultCollector("tool", "version", 100);
        new RepoCleanerRunnableComponent(props, efedora, domsStorage, mailer).doWorkOnItem(batch1, resultCollector);
        assertFalse(resultCollector.isSuccess());
        assertTrue(resultCollector.toReport().contains("higher roundtrip"));
    }

    @Test
    public void testDeleteBatch() throws Exception {
        Batch batch1 = new Batch("1234", 1);
        Batch batch2 = new Batch("1234", 2);

        EnhancedFedora efedora = mock(EnhancedFedora.class);
        when(efedora.findObjectFromDCIdentifier(anyString())).thenReturn(Arrays.asList(MOCKUUID));

        NewspaperDomsEventStorage domsStorage = mock(NewspaperDomsEventStorage.class);
        when(domsStorage.getAllRoundTrips("1234")).thenReturn(Arrays.asList(batch1, batch2));

        SimpleMailer mailer = mock(SimpleMailer.class);

        Properties props = new Properties();
        props.setProperty(ConfigConstants.ALERT_EMAIL_ADDRESSES, "test@example.org");
        props.setProperty(ConfigConstants.SUBJECT_PATTERN, "Test");
        props.setProperty(ConfigConstants.BODY_PATTERN, "Batch B{0}-RT{1,number,integer} approved. Please delete the following files from B{0}-RT{2,number,integer} from the bit repository\n\n{3}");
        props.setProperty(
                dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER,
                Thread.currentThread().getContextClassLoader().getResource("batches").getPath());

        ResultCollector resultCollector = new ResultCollector("tool", "version", 100);
        new RepoCleanerRunnableComponent(props, efedora, domsStorage, mailer).doWorkOnItem(batch2, resultCollector);
        assertTrue(resultCollector.isSuccess());
        //5 xml objects should be deleted
        verify(efedora, times(5)).deleteObject(anyString(), anyString());
        //1 file should be sent in mail should be deleted
        verify(mailer, times(1)).sendMail((List<String>) any(), anyString(), contains("adresseavisen1759-1795-06-01-0006.jp2"));
    }

    @Test
    public void testFormatBody() throws Exception {
        String defaultPattern
                = "Batch B{0}-RT{1,number,integer} approved. Please delete the following files from B{0}-RT{2,number,integer} from the bit repository\n\n{3}";
        String batchID = "4000";
        int oldRoundTrip = 2;
        int newRoundTrip = 4;

        Set<String> files = new HashSet<>(
                Arrays.asList(
                        "B" + batchID + "-RT" + oldRoundTrip + "/" + batchID + "-01/2001-10-02-01/morgenavisenjyllandsposten-2001-10-02-01-0035A.jp2",
                        "B" + batchID + "-RT" + oldRoundTrip + "/" + batchID + "-01/2001-10-02-01/morgenavisenjyllandsposten-2001-10-02-01-0036A.jp2",
                        "B" + batchID + "-RT" + oldRoundTrip + "/" + batchID + "-01/2001-10-02-01/morgenavisenjyllandsposten-2001-10-02-01-0037A.jp2",
                        "B" + batchID + "-RT" + oldRoundTrip + "/" + batchID + "-01/2001-10-02-01/morgenavisenjyllandsposten-2001-10-02-01-0038A.jp2")
        ); String result = RepoCleanerRunnableComponent.formatBody(
                defaultPattern, new Batch(batchID, oldRoundTrip), new Batch(batchID, newRoundTrip), files);
        System.out.println(result);

        assertEquals(
                result,"Batch B"+batchID + "-RT"+newRoundTrip+" approved. Please delete the following files from B"+batchID + "-RT"+oldRoundTrip+" from the bit repository\n" +
                       "\n" +
                       "\n" +
                       "B"+batchID + "-RT"+oldRoundTrip+"_4000-01_2001-10-02-01_morgenavisenjyllandsposten-2001-10-02-01-0037A.jp2\n" +
                       "B"+batchID + "-RT"+oldRoundTrip+"_4000-01_2001-10-02-01_morgenavisenjyllandsposten-2001-10-02-01-0036A.jp2\n" +
                       "B"+batchID + "-RT"+oldRoundTrip+"_4000-01_2001-10-02-01_morgenavisenjyllandsposten-2001-10-02-01-0035A.jp2\n" +
                       "B"+batchID + "-RT"+oldRoundTrip+"_4000-01_2001-10-02-01_morgenavisenjyllandsposten-2001-10-02-01-0038A.jp2");

    }

    @Test
    public void testFormatSet() throws Exception {
        String batchID = "4000";
        int oldRoundTrip = 2;

        Set<String> files = new HashSet<>(
                Arrays.asList(
                        "B" + batchID + "-RT" + oldRoundTrip + "/" + batchID + "-01/2001-10-02-01/morgenavisenjyllandsposten-2001-10-02-01-0035A.jp2",
                        "B" + batchID + "-RT" + oldRoundTrip + "/" + batchID + "-01/2001-10-02-01/morgenavisenjyllandsposten-2001-10-02-01-0036A.jp2",
                        "B" + batchID + "-RT" + oldRoundTrip + "/" + batchID + "-01/2001-10-02-01/morgenavisenjyllandsposten-2001-10-02-01-0037A.jp2",
                        "B" + batchID + "-RT" + oldRoundTrip + "/" + batchID + "-01/2001-10-02-01/morgenavisenjyllandsposten-2001-10-02-01-0038A.jp2")
        );
        String result = RepoCleanerRunnableComponent.formatFiles(files);

        assertEquals(
                result,
                "\n"+
                "B" + batchID + "-RT" + oldRoundTrip + "_4000-01_2001-10-02-01_morgenavisenjyllandsposten-2001-10-02-01-0037A.jp2\n" +
                "B" + batchID + "-RT" + oldRoundTrip + "_4000-01_2001-10-02-01_morgenavisenjyllandsposten-2001-10-02-01-0036A.jp2\n" +
                "B" + batchID + "-RT" + oldRoundTrip + "_4000-01_2001-10-02-01_morgenavisenjyllandsposten-2001-10-02-01-0035A.jp2\n" +
                "B" + batchID + "-RT" + oldRoundTrip + "_4000-01_2001-10-02-01_morgenavisenjyllandsposten-2001-10-02-01-0038A.jp2"
                    );

    }

    @Test
    public void testFormatSubject() throws Exception {
        String defaultSubjetPattern
                = "Batch B{0}-RT{1,number,integer} approved, please delete files from B{0}-RT{2,number,integer}";
        String batchID = "4000";
        int oldRoundTrip = 2;
        int newRoundTrip = 4;
        String result = RepoCleanerRunnableComponent.formatSubject(
                defaultSubjetPattern, new Batch(batchID, oldRoundTrip), new Batch(batchID, newRoundTrip));

        assertEquals(
                result,
                "Batch B" + batchID + "-RT" + newRoundTrip + " approved, please delete files from B" + batchID + "-RT" + oldRoundTrip + "");
    }
}
