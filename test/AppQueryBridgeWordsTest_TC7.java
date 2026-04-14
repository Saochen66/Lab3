import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;

public class AppQueryBridgeWordsTest_TC7 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC7_word2NotInGraph() {
    String res = App.queryBridgeWords("a", "missing");
    System.out.println("TC7 actual: " + res);
    assertNotNull(res);
    assertTrue(res.contains("in the graph"));
    assertTrue(res.contains("missing") || res.contains("\"missing\""));
  }
}
