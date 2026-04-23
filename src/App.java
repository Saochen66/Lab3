import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

/**
 * Console application for directed-graph text analysis tasks.
 */
public class App {
  private static final Graph GRAPH = new Graph();
  private static final double DAMPING = 0.85;
  private static final Path LOG_PATH = Paths.get("lab_log.txt");
  private static final String NO_IN_GRAPH_PREFIX = "No \"";
  private static final String NO_IN_GRAPH_SUFFIX = "\" in the graph!";
  private static final Random RANDOM = new Random();
  private static final PrintStream OUT = System.out;

  /**
   * Program entry.
   *
   * @param args optional first argument is input text path
   * @throws Exception when file I/O fails
   */
  public static void main(String[] args) throws Exception {
    String filePath = null;
    if (args != null && args.length > 0) {
      filePath = args[0];
    }

    BufferedReader console =
        new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    if (filePath == null || filePath.isEmpty()) {
      print("请输入要加载的文本文件路径：");
      filePath = console.readLine().trim();
    }

    log("开始从文件加载: " + filePath);
    GRAPH.buildFromFile(filePath);
    log("构建完成，节点数=" + GRAPH.getNodes().size());

    while (true) {
      println("\n请选择操作：");
      println("1. 展示并保存有向图");
      println("2. 查询桥接词");
      println("3. 根据桥接词生成新文本");
      println("4. 计算两个单词之间的最短路径");
      println("5. 计算单词的 PageRank");
      println("6. 随机游走");
      println("0. 退出");
      print("输入选项: ");
      String opt = console.readLine().trim();
      if (opt.equals("0")) {
        break;
      }

      switch (opt) {
        case "1":
          showDirectedGraphAndSave(GRAPH);
          break;
        case "2":
          print("输入 word1: ");
          String word1 = console.readLine().trim();
          print("输入 word2: ");
          String word2 = console.readLine().trim();
          println(queryBridgeWords(word1, word2));
          break;
        case "3":
          print("输入一行文本: ");
          String line = console.readLine();
          println(generateNewText(line));
          break;
        case "4":
          print("输入 word1: ");
          String src = console.readLine().trim();
          print("输入 word2: ");
          String dst = console.readLine().trim();
          println(calcShortestPath(src, dst));
          break;
        case "5":
          print("输入单词: ");
          String queryWord = console.readLine().trim();
          Double pageRank = calPageRank(queryWord);
          if (pageRank == null) {
            println("No '" + queryWord + "' in the graph!");
          } else {
            println(queryWord + " PR=" + pageRank);
          }
          break;
        case "6":
          println(randomWalk());
          break;
        default:
          println("无效选项");
      }
    }
    log("程序退出");
  }

  /**
   * Print graph edges to stdout.
   *
   * @param graph graph to print
   */
  public static void showDirectedGraph(Graph graph) {
    println("Directed Graph:");
    for (String source : graph.getNodes()) {
      Map<String, Integer> outgoing = graph.getOutgoing(source);
      if (outgoing.isEmpty()) {
        continue;
      }
      print(source + " -> ");
      boolean first = true;
      for (Entry<String, Integer> edge : outgoing.entrySet()) {
        if (!first) {
          print(", ");
        }
        print(edge.getKey() + "(" + edge.getValue() + ")");
        first = false;
      }
      println("");
    }
  }

  /**
   * Save DOT/HTML graph visualization files.
   *
   * @param graph graph to render
   */
  public static void showDirectedGraphAndSave(Graph graph) {
    StringBuilder dot = new StringBuilder();
    dot.append("digraph G {\n");
    dot.append("  rankdir=LR;\n");
    for (String source : graph.getNodes()) {
      Map<String, Integer> outgoing = graph.getOutgoing(source);
      if (outgoing.isEmpty()) {
        continue;
      }
      for (Entry<String, Integer> edge : outgoing.entrySet()) {
        dot.append("  \"")
            .append(source)
            .append("\" -> \"")
            .append(edge.getKey())
            .append("\" [label=\"")
            .append(edge.getValue())
            .append("\"];\n");
      }
    }
    dot.append("}\n");

    try {
      Files.writeString(Paths.get("graph.dot"), dot.toString(), StandardCharsets.UTF_8);
      String html =
          "<!doctype html><html><head><meta charset=\"utf-8\"><title>Graph</title></head>"
              + "<body><div id=\"graph\"></div>\n"
              + "<script src=\"https://unpkg.com/viz.js@2.1.2/viz.js\"></script>"
              + "<script src=\"https://unpkg.com/viz.js@2.1.2/full.render.js\"></script>\n"
              + "<script>var viz = new Viz();viz.renderSVGElement(`"
              + dot.toString().replace("`", "\\`")
              + "`).then(function(element){document.getElementById('graph').appendChild(element)})"
              + ".catch(function(e){console.error(e)});</script></body></html>";
      Files.writeString(Paths.get("graph.html"), html, StandardCharsets.UTF_8);
      log("Wrote graph.dot and graph.html");

      generateGraphPngIfPossible();
      println("Saved graph.dot and graph.html (and graph.png if Graphviz present).");
    } catch (java.io.IOException ioException) {
      println("Failed to write graph files: " + ioException.getMessage());
    }
  }

  /**
   * Query bridge words from word1 to word2.
   *
   * @param word1 source word
   * @param word2 target word
   * @return human-readable result
   */
  public static String queryBridgeWords(String word1, String word2) {
    word1 = normalize(word1);
    word2 = normalize(word2);

    if (!GRAPH.containsNode(word1) || !GRAPH.containsNode(word2)) {
      return NO_IN_GRAPH_PREFIX + word1 + "\" or \"" + word2 + NO_IN_GRAPH_SUFFIX;
    }

    List<String> bridges = GRAPH.getBridgeWords(word1, word2);
    if (bridges.isEmpty()) {
      return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
    }

    StringBuilder builder = new StringBuilder();
    builder
        .append("The bridge words from \"")
        .append(word1)
        .append("\" to \"")
        .append(word2)
        .append("\" are: ");
    for (int i = 0; i < bridges.size(); i++) {
      if (i > 0 && i == bridges.size() - 1) {
        builder.append(" and ");
      } else if (i > 0) {
        builder.append(", ");
      }
      builder.append(bridges.get(i));
    }
    builder.append(".");
    return builder.toString();
  }

  /**
   * Generate text by inserting random bridge words between adjacent tokens.
   *
   * @param inputText source sentence
   * @return generated sentence
   */
  public static String generateNewText(String inputText) {
    String[] tokens = inputText.replaceAll("[^A-Za-z]+", " ").trim().split("\\s+");
    StringBuilder output = new StringBuilder();

    for (int i = 0; i < tokens.length; i++) {
      output.append(tokens[i]);
      if (i < tokens.length - 1) {
        List<String> bridges = GRAPH.getBridgeWords(normalize(tokens[i]), normalize(tokens[i + 1]));
        if (!bridges.isEmpty()) {
          String picked = bridges.get(RANDOM.nextInt(bridges.size()));
          output.append(" ").append(picked);
        }
        output.append(' ');
      }
    }

    String result = output.toString();
    log("generateNewText: input='" + inputText + "' output='" + result + "'");
    return result;
  }

  private static final class PathResult {
    private final List<String> path;
    private final int totalWeight;
    private final boolean reachable;

    private PathResult(List<String> path, int totalWeight, boolean reachable) {
      this.path = path;
      this.totalWeight = totalWeight;
      this.reachable = reachable;
    }
  }

  private static PathResult dijkstra(String src, String dst) {
    if (!GRAPH.containsNode(src) || !GRAPH.containsNode(dst)) {
      return new PathResult(new ArrayList<>(), 0, false);
    }

    Map<String, Integer> distance = new HashMap<>();
    final Map<String, String> previous = new HashMap<>();
    for (String node : GRAPH.getNodes()) {
      distance.put(node, Integer.MAX_VALUE / 4);
    }
    distance.put(src, 0);

    PriorityQueue<Entry<String, Integer>> queue =
        new PriorityQueue<>((a, b) -> Integer.compare(a.getValue(), b.getValue()));
    queue.add(Map.entry(src, 0));

    while (!queue.isEmpty()) {
      Entry<String, Integer> current = queue.poll();
      String u = current.getKey();
      int d = current.getValue();
      if (d != distance.get(u)) {
        continue;
      }
      if (u.equals(dst)) {
        break;
      }
      relaxEdges(u, d, distance, previous, queue);
    }

    if (distance.get(dst) >= Integer.MAX_VALUE / 8) {
      return new PathResult(new ArrayList<>(), 0, false);
    }

    List<String> path = buildPath(previous, dst);
    return new PathResult(path, distance.get(dst), true);
  }

  private static void relaxEdges(
      String source,
      int currentDistance,
      Map<String, Integer> distance,
      Map<String, String> previous,
      PriorityQueue<Entry<String, Integer>> queue) {
    Map<String, Integer> outgoing = GRAPH.getOutgoing(source);
    for (Entry<String, Integer> edge : outgoing.entrySet()) {
      String target = edge.getKey();
      int nextDistance = currentDistance + edge.getValue();
      if (nextDistance < distance.get(target)) {
        distance.put(target, nextDistance);
        previous.put(target, source);
        queue.add(Map.entry(target, nextDistance));
      }
    }
  }

  private static List<String> buildPath(Map<String, String> previous, String dst) {
    List<String> path = new ArrayList<>();
    String cursor = dst;
    while (cursor != null) {
      path.add(cursor);
      cursor = previous.get(cursor);
    }
    Collections.reverse(path);
    return path;
  }

  /**
   * Compute shortest path between words.
   *
   * @param word1 source word
   * @param word2 target word, or empty to compute to all nodes
   * @return path description
   */
  public static String calcShortestPath(String word1, String word2) {
    word1 = normalize(word1);
    word2 = normalize(word2);

    if (!GRAPH.containsNode(word1)) {
      return NO_IN_GRAPH_PREFIX + word1 + NO_IN_GRAPH_SUFFIX;
    }

    if (word2 == null || word2.isEmpty()) {
      StringBuilder output = new StringBuilder();
      for (String target : GRAPH.getNodes()) {
        if (target.equals(word1)) {
          continue;
        }
        PathResult result = dijkstra(word1, target);
        if (!result.reachable) {
          output.append("No path from '")
              .append(word1)
              .append("' to '")
              .append(target)
              .append("'.\n");
        } else {
          output.append(formatPath(result.path)).append(" (length=").append(result.totalWeight)
              .append(")\n");
        }
      }
      String summary = output.toString();
      log("shortestPath from " + word1 + " to all computed");
      if (summary.isEmpty()) {
        return "No reachable nodes from '" + word1 + "'.";
      }
      return summary;
    }

    if (!GRAPH.containsNode(word2)) {
      return NO_IN_GRAPH_PREFIX + word2 + NO_IN_GRAPH_SUFFIX;
    }

    PathResult result = dijkstra(word1, word2);
    if (!result.reachable) {
      return "No path from '" + word1 + "' to '" + word2 + "'!";
    }

    StringBuilder builder = new StringBuilder(formatPath(result.path));
    builder.append(" (length=").append(result.totalWeight).append(")");
    log("shortestPath: " + word1 + " -> " + word2 + " = " + builder);
    return builder.toString();
  }

  private static String formatPath(List<String> path) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < path.size(); i++) {
      if (i > 0) {
        builder.append("->");
      }
      builder.append(path.get(i));
    }
    return builder.toString();
  }

  /**
   * Compute PageRank score for a word.
   *
   * @param word query word
   * @return rank value or null when absent
   */
  public static Double calPageRank(String word) {
    word = normalize(word);
    if (!GRAPH.containsNode(word)) {
      return null;
    }

    Map<String, Map<String, Integer>> adjacency = GRAPH.adjacency();
    Set<String> nodes = adjacency.keySet();
    int size = nodes.size();

    Map<String, Double> rank = new HashMap<>();
    double init = 1.0 / size;
    for (String node : nodes) {
      rank.put(node, init);
    }

    int maxIter = 100;
    double tolerance = 1e-6;
    double damping = DAMPING;

    for (int iteration = 0; iteration < maxIter; iteration++) {
      Map<String, Double> next = new HashMap<>();
      double sinkRank = 0.0;
      for (String node : nodes) {
        Map<String, Integer> outgoing = adjacency.get(node);
        if (outgoing == null || outgoing.isEmpty()) {
          sinkRank += rank.get(node);
        }
      }

      updatePageRankForTargets(nodes, adjacency, rank, next, sinkRank, damping, size);

      double diff = 0.0;
      for (String node : nodes) {
        diff += Math.abs(next.get(node) - rank.get(node));
      }
      rank = next;
      if (diff < tolerance) {
        break;
      }
    }

    log("PageRank computed for '" + word + "' = " + rank.get(word));
    return rank.get(word);
  }

  /**
   * Run random walk on graph and save output into random_walk.txt.
   *
   * @return generated walk sequence
   * @throws Exception when file write fails
   */
  public static String randomWalk() throws java.io.IOException {
    Set<String> nodes = GRAPH.getNodes();
    if (nodes.isEmpty()) {
      return "graph empty";
    }

    String[] array = nodes.toArray(new String[0]);
    String current = array[RANDOM.nextInt(array.length)];
    StringBuilder output = new StringBuilder(current);
    Set<String> seenEdges = new HashSet<>();

    boolean keepWalking = true;
    while (keepWalking) {
      Map<String, Integer> outgoing = GRAPH.getOutgoing(current);
      if (outgoing.isEmpty()) {
        keepWalking = false;
      } else {
        int total = outgoing.values().stream().mapToInt(Integer::intValue).sum();
        int pick = RANDOM.nextInt(total);
        int acc = 0;
        String next = null;
        for (Entry<String, Integer> edge : outgoing.entrySet()) {
          acc += edge.getValue();
          if (pick < acc) {
            next = edge.getKey();
            break;
          }
        }

        String edgeId = current + "->" + next;
        output.append(' ').append(next);
        if (seenEdges.contains(edgeId)) {
          keepWalking = false;
        } else {
          seenEdges.add(edgeId);
          current = next;
        }
      }
    }

    String result = output.toString();
    Files.writeString(
        Paths.get("random_walk.txt"),
        result + System.lineSeparator(),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
    log("randomWalk wrote to random_walk.txt");
    return result;
  }

  private static String normalize(String value) {
    if (value == null) {
      return "";
    }
    return value.replaceAll("[^A-Za-z]+", " ").trim().toLowerCase();
  }

  private static void log(String message) {
    try {
      String line = LocalDateTime.now() + " - " + message + System.lineSeparator();
      Files.writeString(
          LOG_PATH,
          line,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND);
    } catch (java.io.IOException _) {
      // Ignore logging failure.
    }
  }

  private static void print(String text) {
    OUT.print(text);
  }

  private static void println(String text) {
    OUT.println(text);
  }

  private static void generateGraphPngIfPossible() {
    try {
      Process process = new ProcessBuilder("dot", "-Tpng", "graph.dot", "-o", "graph.png").start();
      int code = process.waitFor();
      if (code == 0) {
        log("graph.png generated via Graphviz");
      }
    } catch (InterruptedException _) {
      Thread.currentThread().interrupt();
      log("Graphviz execution interrupted");
    } catch (Exception _) {
      // Graphviz not available.
    }
  }

  private static void updatePageRankForTargets(
      Set<String> nodes,
      Map<String, Map<String, Integer>> adjacency,
      Map<String, Double> rank,
      Map<String, Double> next,
      double sinkRank,
      double damping,
      int size) {
    for (String target : nodes) {
      double value = (1 - damping) / size;
      value += damping * sinkRank / size;
      double contribution = computeContribution(nodes, adjacency, rank, target);
      value += damping * contribution;
      next.put(target, value);
    }
  }

  private static double computeContribution(
      Set<String> nodes,
      Map<String, Map<String, Integer>> adjacency,
      Map<String, Double> rank,
      String target) {
    double contribution = 0.0;
    for (String from : nodes) {
      Map<String, Integer> outgoing = adjacency.get(from);
      if (outgoing == null || outgoing.isEmpty() || !outgoing.containsKey(target)) {
        continue;
      }
      int outWeightSum = outgoing.values().stream().mapToInt(Integer::intValue).sum();
      contribution += rank.get(from) * ((double) outgoing.get(target) / outWeightSum);
    }
    return contribution;
  }
}
