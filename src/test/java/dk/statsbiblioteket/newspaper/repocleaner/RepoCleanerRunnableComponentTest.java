package dk.statsbiblioteket.newspaper.repocleaner;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;

public class RepoCleanerRunnableComponentTest {
    @Test
    public void testDeleteBatch() throws Exception {

    }

    @Test
    public void testReportFiles() throws Exception {

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
        String result = RepoCleanerRunnableComponent.formatSet(files);

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
