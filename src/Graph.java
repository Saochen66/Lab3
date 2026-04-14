import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple directed graph storage. 仅保存邻接关系与读入方法。
 */
public class Graph {
	// adjacency: from -> (to -> weight)
	private final Map<String, Map<String, Integer>> adj = new HashMap<>();

	public void addEdge(String a, String b) {
		if (a == null || b == null || a.isEmpty() || b.isEmpty()) return;
		adj.computeIfAbsent(a, k -> new HashMap<>());
		Map<String, Integer> m = adj.get(a);
		m.put(b, m.getOrDefault(b, 0) + 1);
		// ensure target exists in node set
		adj.computeIfAbsent(b, k -> new HashMap<>());
	}

	public void buildFromFile(String filePath) throws Exception {
		String text = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
		text = text.replaceAll("[^A-Za-z]+", " ").toLowerCase();
		String[] tokens = text.trim().split("\\s+");
		for (int i = 0; i + 1 < tokens.length; i++) {
			String a = tokens[i];
			String b = tokens[i + 1];
			addEdge(a, b);
		}
	}

	public Set<String> getNodes() {
		return Collections.unmodifiableSet(adj.keySet());
	}

	public boolean containsNode(String node) {
		return adj.containsKey(node);
	}

	public Map<String, Integer> getOutgoing(String node) {
		Map<String, Integer> m = adj.get(node);
		if (m == null) return Collections.emptyMap();
		return Collections.unmodifiableMap(m);
	}

	public List<String> getBridgeWords(String w1, String w2) {
		List<String> res = new java.util.ArrayList<>();
		Map<String, Integer> outs1 = adj.get(w1);
		if (outs1 == null) return res;
		for (String mid : outs1.keySet()) {
			Map<String, Integer> outsMid = adj.get(mid);
			if (outsMid != null && outsMid.containsKey(w2)) res.add(mid);
		}
		return res;
	}

	// give access to raw adjacency map (read-only copy)
	public Map<String, Map<String, Integer>> adjacency() {
		Map<String, Map<String, Integer>> copy = new HashMap<>();
		for (Map.Entry<String, Map<String, Integer>> e : adj.entrySet()) {
			copy.put(e.getKey(), Collections.unmodifiableMap(e.getValue()));
		}
		return Collections.unmodifiableMap(copy);
	}
}
