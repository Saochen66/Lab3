import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AppCalcShortestPathTest_TC4 {
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
    addEdges("a->a");
  }

  @Test
  public void testPath4AllTargetsNoReachableNode() {
    String result = App.calcShortestPath("a", "");
    System.out.println("TC4 actual: " + result);
    assertEquals("No reachable nodes from 'a'.", result);
  }
}
