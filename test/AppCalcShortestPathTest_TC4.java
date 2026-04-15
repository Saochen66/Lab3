import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.lang.reflect.Field;

public class AppCalcShortestPathTest_TC4 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "a");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC4_noReachable() throws Exception {
    String res = App.calcShortestPath("a", "");
    System.out.println("TC4 actual: " + res);
    assertEquals("No reachable nodes from 'a'.", res);
  }
}
