import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Field;

public class AppCalcShortestPathTest_All {

  private void setGraph(Graph g) throws Exception {
    Field f = App.class.getDeclaredField("graph");
    f.setAccessible(true);
    f.set(null, g);
  }

  @Test
  public void test_TC1_startNodeNotExist() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    setGraph(g);
    String res = App.calcShortestPath("x", "b");
    System.out.println("TC1 actual: " + res);
    assertEquals("No \"x\" in the graph!", res);
  }

  @Test
  public void test_TC2_targetNotExist() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    setGraph(g);
    String res = App.calcShortestPath("a", "c");
    System.out.println("TC2 actual: " + res);
    assertEquals("No \"c\" in the graph!", res);
  }

  @Test
  public void test_TC3_unreachableTarget() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    g.addEdge("c", "d");
    setGraph(g);
    String res = App.calcShortestPath("a", "c");
    System.out.println("TC3 actual: " + res);
    assertEquals("No path from 'a' to 'c'!", res);
  }

  @Test
  public void test_TC4_noReachable() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "a");
    setGraph(g);
    String res = App.calcShortestPath("a", "");
    System.out.println("TC4 actual: " + res);
    assertEquals("No reachable nodes from 'a'.", res);
  }

  @Test
  public void test_TC5_multiTargetHasPath() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    setGraph(g);
    String res = App.calcShortestPath("a", "");
    System.out.println("TC5 actual: " + res);
    assertTrue(res.contains("a->b (length=1)"));
  }

  @Test
  public void test_TC6_singleTargetPath() throws Exception {
    Graph g = new Graph();
    g.addEdge("a", "b");
    g.addEdge("b", "c");
    setGraph(g);
    String res = App.calcShortestPath("a", "c");
    System.out.println("TC6 actual: " + res);
    assertEquals("a->b->c (length=2)", res);
  }
}
