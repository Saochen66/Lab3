import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.lang.reflect.Field;

public class AppCalcShortestPathTest_TC6 {
  @Before
  public void setUp() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    g.addEdge("b", "c");
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC6_singleTargetPath() throws Exception {
    String res = App.calcShortestPath("a", "c");
    System.out.println("TC6 actual: " + res);
    assertEquals("a->b->c (length=2)", res);
  }
}
