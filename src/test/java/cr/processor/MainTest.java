package cr.processor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

public class MainTest {
    private Author testAuthor;

    @Before
    public void setup() {
        testAuthor = new Author();
        testAuthor.setName(new String[]{"Littlefinger"});
        testAuthor.setEmail(new String[]{"littlefinger@kingslanding.net"});
        testAuthor.setCommits(new ArrayList<>());
        testAuthor.getCommits().add(new Commit("a", new String[]{}, "Initial commit"));
        testAuthor.getCommits().add(new Commit("b", new String[]{"a"}, "Removes member Varys from small council"));
        testAuthor.getCommits().add(new Commit("c", new String[]{"b"}, "Adds kill signal to marriage process"));
        testAuthor.getCommits().add(new Commit("d", new String[]{"c"}, "Moves member tyrion to volantis domain"));
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
