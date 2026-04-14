import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;

public class AppQueryBridgeWordsTest_TC3 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "x");
    g.addEdge("y", "c");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC3_noBridgeWords() {
    String res = App.queryBridgeWords("a", "c");
    assertNotNull(res);
    assertTrue(res.contains("No bridge words"));
  }
}
