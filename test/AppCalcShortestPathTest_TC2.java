import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.lang.reflect.Field;

public class AppCalcShortestPathTest_TC2 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC2_targetNotExist() throws Exception {
    String res = App.calcShortestPath("a", "c");
    System.out.println("TC2 actual: " + res);
    assertEquals("No \"c\" in the graph!", res);
  }
}
