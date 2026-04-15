import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.lang.reflect.Field;

public class AppCalcShortestPathTest_TC1 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC1_startNodeNotExist() throws Exception {
    String res = App.calcShortestPath("x", "b");
    System.out.println("TC1 actual: " + res);
    assertEquals("No \"x\" in the graph!", res);
  }
}
