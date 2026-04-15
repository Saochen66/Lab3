import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.lang.reflect.Field;

public class AppCalcShortestPathTest_TC3 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    g.addEdge("c", "d");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC3_unreachableTarget() throws Exception {
    String res = App.calcShortestPath("a", "c");
    System.out.println("TC3 actual: " + res);
    assertEquals("No path from 'a' to 'c'!", res);
  }
}
