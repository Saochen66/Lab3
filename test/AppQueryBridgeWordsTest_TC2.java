import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;

public class AppQueryBridgeWordsTest_TC2 {
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
  public void test_TC2_singleBridgeWord() {
    String res = App.queryBridgeWords("a", "c");
    System.out.println("TC2 actual: " + res);
    assertNotNull(res);
    assertTrue(res.contains("The bridge words from"));
    assertTrue(res.contains("b"));
  }
}
