import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;

/**
 * JUnit4 测试：针对 App.queryBridgeWords 的 TC1–TC10
 * 说明：测试通过反射替换 App 类的私有静态字段 `graph`，以便注入自定义图。
 */
public class AppQueryBridgeWordsTest {

    @Before
    public void setUp() throws Exception {
        // 将 App.graph 重置为一个空的 Graph 实例，确保每个测试互不干扰
        Graph g = new Graph();
        Field f = App.class.getDeclaredField("graph");
        f.setAccessible(true);
        f.set(null, g);
    }

    private void setAppGraph(Graph g) throws Exception {
        Field f = App.class.getDeclaredField("graph");
        f.setAccessible(true);
        f.set(null, g);
    }

    @Test
    public void TC1_multipleBridgeWords() throws Exception {
        Graph g = new Graph();
        g.addEdge("a", "b1");
        g.addEdge("a", "b2");
        g.addEdge("b1", "c");
        g.addEdge("b2", "c");
        setAppGraph(g);

        String res = App.queryBridgeWords("a", "c");
        assertNotNull(res);
        assertTrue(res.contains("The bridge words from"));
        assertTrue(res.contains("a"));
        assertTrue(res.contains("c"));
        assertTrue(res.contains("b1"));
        assertTrue(res.contains("b2"));
    }

    @Test
    public void TC2_singleBridgeWord() throws Exception {
        Graph g = new Graph();
        g.addEdge("a", "b");
        g.addEdge("b", "c");
        setAppGraph(g);

        String res = App.queryBridgeWords("a", "c");
        assertNotNull(res);
        assertTrue(res.contains("The bridge words from"));
        assertTrue(res.contains("b"));
    }

    @Test
    public void TC3_noBridgeWords() throws Exception {
        Graph g = new Graph();
        g.addEdge("a", "x");
        g.addEdge("y", "c");
        setAppGraph(g);

        String res = App.queryBridgeWords("a", "c");
        assertNotNull(res);
        assertTrue(res.contains("No bridge words"));
    }

    @Test
    public void TC4_selfTargetWithBridge() throws Exception {
        Graph g = new Graph();
        g.addEdge("a", "b");
        g.addEdge("b", "a");
        setAppGraph(g);

        String res = App.queryBridgeWords("a", "a");
        assertNotNull(res);
        assertTrue(res.contains("The bridge words from"));
        assertTrue(res.contains("b"));
    }

    @Test
    public void TC5_selfTargetNoBridge() throws Exception {
        Graph g = new Graph();
        // only ensure node 'a' exists but no b with a->b and b->a
        g.addEdge("a", "x");
        setAppGraph(g);

        String res = App.queryBridgeWords("a", "a");
        assertNotNull(res);
        assertTrue(res.contains("No bridge words"));
    }

    @Test
    public void TC6_word1NotInGraph() throws Exception {
        Graph g = new Graph();
        g.addEdge("a", "b");
        g.addEdge("b", "c");
        setAppGraph(g);

        String res = App.queryBridgeWords("unknown", "c");
        assertNotNull(res);
        assertTrue(res.contains("in the graph"));
        assertTrue(res.contains("unknown") || res.contains("\"unknown\""));
    }

    @Test
    public void TC7_word2NotInGraph() throws Exception {
        Graph g = new Graph();
        g.addEdge("a", "b");
        setAppGraph(g);

        String res = App.queryBridgeWords("a", "missing");
        assertNotNull(res);
        assertTrue(res.contains("in the graph"));
        assertTrue(res.contains("missing") || res.contains("\"missing\""));
    }

    @Test
    public void TC8_emptyWord1() throws Exception {
        Graph g = new Graph();
        g.addEdge("c", "d"); // make sure 'c' exists in graph
        setAppGraph(g);

        String res = App.queryBridgeWords("", "c");
        assertNotNull(res);
        assertTrue(res.contains("in the graph"));
        assertTrue(res.contains("c") || res.contains("\"c\""));
    }

    @Test
    public void TC9_inputContainsDigitsNormalized() throws Exception {
        Graph g = new Graph();
        // normalized("hello1") -> "hello"
        g.addEdge("hello", "b");
        g.addEdge("b", "c");
        setAppGraph(g);

        String res = App.queryBridgeWords("hello1", "c");
        assertNotNull(res);
        // should find bridge 'b' because normalization removes digits
        assertTrue(res.contains("The bridge words from"));
        assertTrue(res.contains("b"));
    }

    @Test
    public void TC10_longInputNotPresent() throws Exception {
        Graph g = new Graph();
        g.addEdge("a", "b");
        setAppGraph(g);

        // build a long word (>1000 'x')
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1100; i++) sb.append('x');
        String longWord = sb.toString();

        String res = App.queryBridgeWords("a", longWord);
        assertNotNull(res);
        // application does not explicitly reject long inputs; it will likely say node not present
        assertTrue(res.contains("in the graph") || res.contains("No bridge words"));
    }
}
