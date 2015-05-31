package cr.processor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

public class MainTest {
    private Author testAuthor;

    @Before
    public void setup() {
        testAuthor = new Author();
        testAuthor.setName("Littlefinger");
        testAuthor.setEmail("littlefinger@kingslanding.net");
        testAuthor.setCommits(new ArrayList<>());
        testAuthor.getCommits().add(new Commit(
                "a",
                new String[]{},
                "Initial commit",
                new HashMap<>()
        ));
        testAuthor.getCommits().add(new Commit(
                "b",
                new String[]{"a"},
                "Removes member Varys from small council",
                new HashMap<>()
        ));
        testAuthor.getCommits().add(new Commit(
                "c",
                new String[]{"b"},
                "Adds kill signal to marriage process",
                new HashMap<>()
        ));
        testAuthor.getCommits().add(new Commit(
                "d",
                new String[]{"c"},
                "Moves member tyrion to volantis domain",
                new HashMap<>()
        ));
    }

    @After
    public void tearDown() {
        testAuthor = null;
    }

    @Test
    public void testAverageCommitMessageLength() {
        assertEquals(31f, cr.processor.Main.averageCommitMessageLength(testAuthor.getCommits()), 0);
    }
}
