import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;

public class AppQueryBridgeWordsTest_TC4 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    g.addEdge("b", "a");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC4_selfTargetWithBridge() {
    String res = App.queryBridgeWords("a", "a");
    System.out.println("TC4 actual: " + res);
    assertNotNull(res);
    assertTrue(res.contains("The bridge words from"));
    assertTrue(res.contains("b"));
  }
}
