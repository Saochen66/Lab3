import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;

public class AppQueryBridgeWordsTest_TC5 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    // 确保节点 a 存在，但不存在 a->mid 且 mid->a 的 mid
    g.addEdge("a", "x");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC5_selfTargetNoBridge() {
    String res = App.queryBridgeWords("a", "a");
    System.out.println("TC5 actual: " + res);
    assertNotNull(res);
    assertTrue(res.contains("No bridge words"));
  }
}
