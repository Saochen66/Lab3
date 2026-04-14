import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;

public class AppQueryBridgeWordsTest_TC8 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    // 让归一化后的 word2=c 在图中存在
    g.addEdge("c", "d");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC8_emptyWord1() {
    String res = App.queryBridgeWords("", "c");
    System.out.println("TC8 actual: " + res);
    assertNotNull(res);
    // App 的实现对空 word1 会 normalize 为 ""，因此会走“不在图中”分支
    assertTrue(res.contains("in the graph"));
  }
}
