import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;

public class AppQueryBridgeWordsTest_TC9 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    // normalize("hello1") -> "hello"，因此这里用 hello 构图
    g.addEdge("hello", "b");
    g.addEdge("b", "c");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC9_inputContainsDigitsNormalized() {
    String res = App.queryBridgeWords("hello1", "c");
    System.out.println("TC9 actual: " + res);
    assertNotNull(res);
    assertTrue(res.contains("The bridge words from"));
    assertTrue(res.contains("b"));
  }
}
