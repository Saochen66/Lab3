import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AppCalcShortestPathTest_TC6 {
  private static Graph appGraph() throws Exception {
    Field field = App.class.getDeclaredField("GRAPH");
    field.setAccessible(true);
    return (Graph) field.get(null);
  }

  private static void resetGraph() throws Exception {
    Graph graph = appGraph();
    Field adjField = Graph.class.getDeclaredField("adj");
    adjField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Map<String, Integer>> adjacency =
        (Map<String, Map<String, Integer>>) adjField.get(graph);
    adjacency.clear();
  }

  private static void addEdges(String... edges) throws Exception {
    Graph graph = appGraph();
    for (String edge : edges) {
      String[] parts = edge.split("->");
      if (parts.length == 2) {
        graph.addEdge(parts[0], parts[1]);
      }
    }
  }

  @Before
  public void setUp() throws Exception {
    resetGraph();
    addEdges("a->b", "b->c");
  }

  @Test
  public void testPath6SingleTargetReachable() {
    String result = App.calcShortestPath("a", "c");
    System.out.println("TC6 actual: " + result);
    assertEquals("a->b->c (length=2)", result);
  }
}
