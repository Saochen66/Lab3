import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;

public class AppQueryBridgeWordsTest_TC1 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b1");
    g.addEdge("a", "b2");
    g.addEdge("b1", "c");
    g.addEdge("b2", "c");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC1_multipleBridgeWords() {
    String res = App.queryBridgeWords("a", "c");
    System.out.println("TC1 actual: " + res);
    assertNotNull(res);
    assertTrue(res.contains("The bridge words from"));
    assertTrue(res.contains("b1"));
    assertTrue(res.contains("b2"));
  }
}
