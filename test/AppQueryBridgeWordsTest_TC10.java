import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;

public class AppQueryBridgeWordsTest_TC10 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC10_longInputNotPresent() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1100; i++) sb.append('x');
    String longWord = sb.toString();

    String res = App.queryBridgeWords("a", longWord);
    System.out.println("TC10 actual: " + res);
    assertNotNull(res);
    // App 当前实现没有“超长输入”专门报错，通常会表现为“不在图中”
    assertTrue(res.contains("in the graph") || res.contains("No bridge words"));
  }
}
