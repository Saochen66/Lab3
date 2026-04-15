import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;

public class AppCalcShortestPathTest_TC5 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC5_multiTargetHasPath() throws Exception {
    String res = App.calcShortestPath("a", "");
    System.out.println("TC5 actual: " + res);
    assertTrue(res.contains("a->b (length=1)"));
  }
}
