import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class App {
    @SuppressWarnings("checkstyle:Indentation")
    private static Graph graph = new Graph();
    private static final double DAMPING = 0.85;
    private static final Path LOG_PATH = Paths.get("lab_log.txt");

    public static void main(String[] args) throws Exception {
        String filePath = null;
        if (args != null && args.length > 0) filePath = args[0];

        BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        if (filePath == null || filePath.isEmpty()) {
            System.out.print("请输入要加载的文本文件路径：");
            filePath = console.readLine().trim();
        }

        log("开始从文件加载: " + filePath);
        graph.buildFromFile(filePath);
        log("构建完成，节点数=" + graph.getNodes().size());

        while (true) {
            System.out.println("\n请选择操作：");
            System.out.println("1. 展示并保存有向图");
            System.out.println("2. 查询桥接词");
            System.out.println("3. 根据桥接词生成新文本");
            System.out.println("4. 计算两个单词之间的最短路径");
            System.out.println("5. 计算单词的 PageRank");
            System.out.println("6. 随机游走");
            System.out.println("0. 退出");
            System.out.print("输入选项: ");
            String opt = console.readLine().trim();
            if (opt.equals("0")) break;
            switch (opt) {
                case "1":
                    showDirectedGraphAndSave(graph);
                    break;
                case "2":
                    System.out.print("输入 word1: ");
                    String w1 = console.readLine().trim();
                    System.out.print("输入 word2: ");
                    String w2 = console.readLine().trim();
                    String res = queryBridgeWords(w1, w2);
                    System.out.println(res);
                    break;
                case "3":
                    System.out.print("输入一行文本: ");
                    String line = console.readLine();
                    System.out.println(generateNewText(line));
                    break;
                case "4":
                    System.out.print("输入 word1: ");
                    String s1 = console.readLine().trim();
                    System.out.print("输入 word2: ");
                    String s2 = console.readLine().trim();
                    System.out.println(calcShortestPath(s1, s2));
                    break;
                case "5":
                    System.out.print("输入单词: ");
                    String qw = console.readLine().trim();
                    Double pr = calPageRank(qw);
                    if (pr == null) System.out.println("No '" + qw + "' in the graph!");
                    else System.out.println(qw + " PR=" + pr);
                    break;
                case "6":
                    String walk = randomWalk();
                    System.out.println(walk);
                    break;
                default:
                    System.out.println("无效选项");
            }
        }
        log("程序退出");
    }

    public static void showDirectedGraph(Graph G) {
        System.out.println("Directed Graph:");
        for (String u : G.getNodes()) {
            Map<String, Integer> outs = G.getOutgoing(u);
            if (outs == null || outs.isEmpty()) continue;
            System.out.print(u + " -> ");
            boolean first = true;
            for (Map.Entry<String, Integer> e : outs.entrySet()) {
                if (!first) System.out.print(", ");
                System.out.print(e.getKey() + "(" + e.getValue() + ")");
                first = false;
            }
            System.out.println();
        }
    }

    // write DOT and HTML visualization files; try to call Graphviz if available
    public static void showDirectedGraphAndSave(Graph G) {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph G {\n");
        dot.append("  rankdir=LR;\n");
        for (String u : G.getNodes()) {
            Map<String, Integer> outs = G.getOutgoing(u);
            if (outs == null || outs.isEmpty()) continue;
            for (Entry<String, Integer> e : outs.entrySet()) {
                dot.append("  \"").append(u).append("\" -> \"").append(e.getKey()).append("\" [label=\"").append(e.getValue()).append("\"];\n");
            }
        }
        dot.append("}\n");
        try {
            Files.writeString(Paths.get("graph.dot"), dot.toString(), StandardCharsets.UTF_8);
            // write simple HTML using viz.js CDN to render DOT
            String html = "<!doctype html><html><head><meta charset=\"utf-8\"><title>Graph</title></head>"
                    + "<body><div id=\"graph\"></div>\n<script src=\"https://unpkg.com/viz.js@2.1.2/viz.js\"></script>"
                    + "<script src=\"https://unpkg.com/viz.js@2.1.2/full.render.js\"></script>\n<script>var viz = new Viz();viz.renderSVGElement(`"
                    + dot.toString().replace("`", "\\`")
                    + "`).then(function(element){document.getElementById('graph').appendChild(element)}).catch(function(e){console.error(e)});</script></body></html>";
            Files.writeString(Paths.get("graph.html"), html, StandardCharsets.UTF_8);
            log("Wrote graph.dot and graph.html");
            // try to run dot to produce PNG (optional)
            try {
                Process p = new ProcessBuilder("dot", "-Tpng", "graph.dot", "-o", "graph.png").start();
                int code = p.waitFor();
                if (code == 0) log("graph.png generated via Graphviz");
            } catch (Throwable ex) {
                // Graphviz not available or failed; ignore
            }
            System.out.println("Saved graph.dot and graph.html (and graph.png if Graphviz present).");
        } catch (Exception e) {
            System.out.println("Failed to write graph files: " + e.getMessage());
        }
    }

    public static String queryBridgeWords(String word1, String word2) {
        word1 = normalize(word1);
        word2 = normalize(word2);
        if (!graph.containsNode(word1) || !graph.containsNode(word2)) {
            return "No \"" + word1 + "\" or \"" + word2 + "\" in the graph!";
        }
        List<String> bridges = graph.getBridgeWords(word1, word2);
        if (bridges.isEmpty()) return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        StringBuilder sb = new StringBuilder();
        sb.append("The bridge words from \"").append(word1).append("\" to \"").append(word2).append(" are: ");
        for (int i = 0; i < bridges.size(); i++) {
            if (i > 0 && i == bridges.size() - 1) sb.append(" and ");
            else if (i > 0) sb.append(", ");
            sb.append(bridges.get(i));
        }
        sb.append(".");
        return sb.toString();
    }

    public static String generateNewText(String inputText) {
        String[] tokens = inputText.replaceAll("[^A-Za-z]+", " ").trim().split("\\s+");
        StringBuilder out = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < tokens.length; i++) {
            String t = tokens[i];
            out.append(t);
            if (i < tokens.length - 1) {
                List<String> bridges = graph.getBridgeWords(normalize(tokens[i]), normalize(tokens[i + 1]));
                if (!bridges.isEmpty()) {
                    String pick = bridges.get(rnd.nextInt(bridges.size()));
                    out.append(" ").append(pick);
                }
            }
            if (i < tokens.length - 1) out.append(' ');
        }
        String res = out.toString();
        log("generateNewText: input='" + inputText + "' output='" + res + "'");
        return res;
    }

    private static class PathResult {
        public final List<String> path;
        public final int totalWeight;
        public final boolean reachable;
        public PathResult(List<String> p, int w, boolean r) { path = p; totalWeight = w; reachable = r; }
    }

    // Dijkstra using graph.getOutgoing
    private static PathResult dijkstra(String src, String dst) {
        if (!graph.containsNode(src) || !graph.containsNode(dst)) return new PathResult(new ArrayList<>(), 0, false);
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        for (String n : graph.getNodes()) dist.put(n, Integer.MAX_VALUE / 4);
        dist.put(src, 0);
        PriorityQueue<Entry<String, Integer>> pq = new PriorityQueue<>((a,b)->Integer.compare(a.getValue(), b.getValue()));
        pq.add(Map.entry(src, 0));
        while (!pq.isEmpty()) {
            Entry<String, Integer> cur = pq.poll();
            String u = cur.getKey();
            int d = cur.getValue();
            if (d != dist.get(u)) continue;
            if (u.equals(dst)) break;
            Map<String, Integer> outs = graph.getOutgoing(u);
            for (Entry<String, Integer> e : outs.entrySet()) {
                String v = e.getKey(); int w = e.getValue();
                int nd = d + w;
                if (nd < dist.get(v)) { dist.put(v, nd); prev.put(v, u); pq.add(Map.entry(v, nd)); }
            }
        }
        if (dist.get(dst) >= Integer.MAX_VALUE / 8) return new PathResult(new ArrayList<>(), 0, false);
        List<String> path = new ArrayList<>(); String cur = dst;
        while (cur != null) { path.add(cur); cur = prev.get(cur); }
        java.util.Collections.reverse(path);
        return new PathResult(path, dist.get(dst), true);
    }

    public static String calcShortestPath(String word1, String word2) {
        word1 = normalize(word1);
        word2 = normalize(word2);
        if (!graph.containsNode(word1)) return "No \"" + word1 + "\" in the graph!";
        if (word2 == null || word2.isEmpty()) {
            // compute shortest path from word1 to every other node
            StringBuilder out = new StringBuilder();
            for (String target : graph.getNodes()) {
                if (target.equals(word1)) continue;
                PathResult pr = dijkstra(word1, target);
                if (!pr.reachable) {
                    out.append("No path from '").append(word1).append("' to '").append(target).append("'.\n");
                } else {
                    for (int i = 0; i < pr.path.size(); i++) {
                        if (i > 0) out.append("->");
                        out.append(pr.path.get(i));
                    }
                    out.append(" (length=").append(pr.totalWeight).append(")\n");
                }
            }
            String s = out.toString(); log("shortestPath from " + word1 + " to all computed");
            return s.isEmpty() ? ("No reachable nodes from '" + word1 + "'.") : s;
        } else {
            if (!graph.containsNode(word2)) return "No \"" + word2 + "\" in the graph!";
            PathResult pr = dijkstra(word1, word2);
            if (!pr.reachable) return "No path from '" + word1 + "' to '" + word2 + "'!";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pr.path.size(); i++) {
                if (i > 0) sb.append("->");
                sb.append(pr.path.get(i));
            }
            sb.append(" (length=").append(pr.totalWeight).append(")");
            log("shortestPath: " + word1 + " -> " + word2 + " = " + sb.toString());
            return sb.toString();
        }
    }

    public static Double calPageRank(String word) {
        word = normalize(word);
        if (!graph.containsNode(word)) return null;
        // compute PageRank here using adjacency
        Map<String, Map<String, Integer>> adj = graph.adjacency();
        Set<String> nodes = adj.keySet();
        int N = nodes.size();
        Map<String, Double> pr = new HashMap<>();
        double init = 1.0 / N;
        for (String n : nodes) pr.put(n, init);
        int maxIter = 100; double tol = 1e-6; double d = DAMPING;
        for (int it = 0; it < maxIter; it++) {
            Map<String, Double> next = new HashMap<>();
            double sinkPR = 0.0;
            for (String n : nodes) {
                Map<String,Integer> outs = adj.get(n);
                if (outs == null || outs.isEmpty()) sinkPR += pr.get(n);
            }
            for (String u : nodes) {
                double val = (1 - d) / N;
                val += d * sinkPR / N;
                double sum = 0.0;
                for (String v : nodes) {
                    Map<String,Integer> outs = adj.get(v);
                    if (outs == null || outs.isEmpty()) continue;
                    if (outs.containsKey(u)) {
                        int outSum = outs.values().stream().mapToInt(Integer::intValue).sum();
                        sum += pr.get(v) * ((double) outs.get(u) / outSum);
                    }
                }
                val += d * sum;
                next.put(u, val);
            }
            double diff = 0.0;
            for (String n : nodes) diff += Math.abs(next.get(n) - pr.get(n));
            pr = next;
            if (diff < tol) break;
        }
        log("PageRank computed for '" + word + "' = " + pr.get(word));
        return pr.get(word);
    }

    public static String randomWalk() throws Exception {
        Random rnd = new Random();
        Set<String> nodes = graph.getNodes();
        if (nodes.isEmpty()) return "graph empty";
        String[] arr = nodes.toArray(new String[0]);
        String cur = arr[rnd.nextInt(arr.length)];
        StringBuilder sb = new StringBuilder();
        sb.append(cur);
        java.util.Set<String> seenEdges = new java.util.HashSet<>();
        while (true) {
            Map<String, Integer> outs = graph.getOutgoing(cur);
            if (outs == null || outs.isEmpty()) break;
            // choose random outgoing edge uniformly by weight
            int total = outs.values().stream().mapToInt(Integer::intValue).sum();
            int r = rnd.nextInt(total);
            int acc = 0;
            String next = null;
            for (Map.Entry<String, Integer> e : outs.entrySet()) {
                acc += e.getValue();
                if (r < acc) { next = e.getKey(); break; }
            }
            String edgeId = cur + "->" + next;
            sb.append(' ').append(next);
            if (seenEdges.contains(edgeId)) break;
            seenEdges.add(edgeId);
            cur = next;
        }
        String result = sb.toString();
        Files.writeString(Paths.get("random_walk.txt"), result + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log("randomWalk wrote to random_walk.txt");
        return result;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("[^A-Za-z]+", " ").trim().toLowerCase();
    }

    private static void log(String msg) {
        try {
            String line = java.time.LocalDateTime.now() + " - " + msg + System.lineSeparator();
            Files.writeString(LOG_PATH, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            // ignore logging errors
        }
    }
}
