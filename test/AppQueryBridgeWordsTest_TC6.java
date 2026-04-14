import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;

public class AppQueryBridgeWordsTest_TC6 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    g.addEdge("b", "c");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC6_word1NotInGraph() {
    String res = App.queryBridgeWords("unknown", "c");
    System.out.println("TC6 actual: " + res);
    assertNotNull(res);
    assertTrue(res.contains("in the graph"));
    assertTrue(res.contains("unknown") || res.contains("\"unknown\""));
  }
}
