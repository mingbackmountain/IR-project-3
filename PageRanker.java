//Name: Thanakorn Pasangthien: ID: 6088109 Section: 1
//Name: Nontapat Pintira ID: 6088118 Section: 1
//Name: Tanaporn Rojanaridpiched ID:6088146 Section: 3

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.*;

/**
 * This class implements PageRank algorithm on simple graph structure. Put your
 * name(s), ID(s), and section here.
 */

public class PageRanker {
	public class Node {
		int pageId;
		double pageRank;
		List<Node> inLinks;
		Set<Node> outLinks;

		public Node(int id) {
			pageId = id;
			inLinks = new ArrayList<Node>();
			outLinks = new HashSet<Node>();
		}
	}

	public Map<Integer, Node> pageGraph;
	public List<Node> listNodes;
	public List<Double> perplexity;
	public int numberOfNodes;

	/**
	 * This class reads the direct graph stored in the file "inputLinkFilename" into
	 * memory. Each line in the input file should have the following format: <pid_1>
	 * <pid_2> <pid_3> .. <pid_n>
	 *
	 * Where pid_1, pid_2, ..., pid_n are the page IDs of the page having links to
	 * page pid_1. You can assume that a page ID is an integer.
	 *
	 */

	public void loadData(final String inputLinkFilename) throws IOException {

		pageGraph = new HashMap<Integer, Node>();
		String line;

		try {
			BufferedReader buffer = new BufferedReader(new FileReader(new File(inputLinkFilename)));

			while ((line = buffer.readLine()) != null) {
				int pageId;
				int inLinkId;
				Node nodePage, nodeInLink;

				String[] tokens = line.split(" ");
				pageId = Integer.parseInt(tokens[0]);
				if (!pageGraph.containsKey(pageId)) {
					nodePage = new Node(pageId);
					pageGraph.put(pageId, nodePage);
				} else {
					nodePage = pageGraph.get(pageId);
				}
				for (int i = 1; i < tokens.length; i++) {
					inLinkId = Integer.parseInt(tokens[i]);
					if (!pageGraph.containsKey(inLinkId)) {
						nodeInLink = new Node(inLinkId);
						pageGraph.put(inLinkId, nodeInLink);
					} else {
						nodeInLink = pageGraph.get(inLinkId);
					}
					if (!nodePage.inLinks.contains(nodeInLink))
						nodePage.inLinks.add(nodeInLink);
					nodeInLink.outLinks.add(nodePage);
				}
			}
			buffer.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method will be called after the graph is loaded into the memory. This
	 * method initialize the parameters for the PageRank algorithm including setting
	 * an initial weight to each page.
	 */
	public void initialize() {
		listNodes = new ArrayList<Node>();
		perplexity = new ArrayList<Double>();
		numberOfNodes = pageGraph.size();
		double initValue = 1.0 / numberOfNodes;

		for (Node n : pageGraph.values()) {
			n.pageRank = initValue;

			if (n.outLinks.size() == 0) {
				listNodes.add(n);
			}
		}
	}

	/**
	 * Computes the perplexity of the current state of the graph. The definition of
	 * perplexity is given in the project specs.
	 */
	public double getPerplexity() {
		double sum = 0;
		for (Node n : pageGraph.values()) {
			sum += n.pageRank * (Math.log(n.pageRank) / Math.log(2));
		}

		return Math.pow(2, -sum);
	}

	/**
	 * Returns true if the perplexity converges (hence, terminate the PageRank
	 * algorithm). Returns false otherwise (and PageRank algorithm continue to
	 * update the page scores).
	 */
	public boolean isConverge() {
		if (perplexity.size() < 4)
			return false;
		for (int i = perplexity.size() - 1; i > perplexity.size() - 4; i--) {
			double perplexityOne = perplexity.get(i);
			double perplexityTwo = perplexity.get(i - 1);
			if ((int) perplexityOne % 10 != (int) perplexityTwo % 10 || perplexityOne - perplexityTwo > 1.0)
				return false;
		}
		return true;
	}

	/**
	 * The main method of PageRank algorithm. Can assume that initialize() has been
	 * called before this method is invoked. While the algorithm is being run, this
	 * method should keep track of the perplexity after each iteration.
	 *
	 * Once the algorithm terminates, the method generates two output files. [1]
	 * "perplexityOutFilename" lists the perplexity after each iteration on each
	 * line. The output should look something like:
	 * 
	 * 183811 79669.9 86267.7 72260.4 75132.4
	 * 
	 * Where, for example,the 183811 is the perplexity after the first iteration.
	 *
	 * [2] "prOutFilename" prints out the score for each page after the algorithm
	 * terminate. The output should look something like:
	 * 
	 * 1 0.1235 2 0.3542 3 0.236
	 * 
	 * Where, for example, 0.1235 is the PageRank score of page 1.
	 * 
	 */
	public void runPageRank(final String perplexityOutFilename, final String prOutFilename) {
		double accPageRank;
		double newPageRank[] = new double[numberOfNodes];
		double d = 0.85;
		int index = 0;
		while (!isConverge()) {
			accPageRank = 0;
			index = 0;
			for (Node s : listNodes) {
				accPageRank += s.pageRank;
			}
			for (Node n : pageGraph.values()) {
				newPageRank[index] = ((1.0 - d) + (d * accPageRank)) / numberOfNodes;
				for (Node in : n.inLinks) {
					newPageRank[index] += d * in.pageRank / in.outLinks.size();
				}
				index++;
			}
			index = 0;
			for (Node n : pageGraph.values()) {
				n.pageRank = newPageRank[index++];
			}
			perplexity.add(getPerplexity());
		}
		StringBuilder perplexityLine = new StringBuilder();
		StringBuilder pageRankLine = new StringBuilder();
		ArrayList<Integer> sortedPid = new ArrayList<Integer>(pageGraph.keySet());
		Collections.sort(sortedPid);
		for (double p : perplexity) {
			perplexityLine.append(p + "\n");
		}

		for (int pid : sortedPid) {
			pageRankLine.append(pageGraph.get(pid).pageId + " " + pageGraph.get(pid).pageRank + "\n");
		}
		BufferedWriter perplexityFile;
		BufferedWriter pageRankFile;
		try {
			perplexityFile = new BufferedWriter(new FileWriter(perplexityOutFilename));
			pageRankFile = new BufferedWriter(new FileWriter(prOutFilename));
			perplexityFile.write(perplexityLine.toString());
			pageRankFile.write(pageRankLine.toString());
			perplexityFile.close();
			pageRankFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return the top K page IDs, whose scores are highest.
	 */
	public Integer[] getRankedPages(int K) {
		if (pageGraph.size() < K) {
			K = numberOfNodes;
		}
		double[][] ranking = new double[numberOfNodes][2];
		int i = 0;
		for (Node n : pageGraph.values()) {
			ranking[i][0] = n.pageId;
			ranking[i][1] = n.pageRank;
			i++;
		}

		Arrays.sort(ranking, new Comparator<double[]>() {
			public int compare(double[] val1, double[] val2) {
				return Double.compare(val2[1], val1[1]);
			}
		});
		Integer[] result = new Integer[K];
		for (i = 0; i < K; i++) {
			result[i] = (int) ranking[i][0];
		}
		return result;
	}

	public static void main(final String args[]) {
		final long startTime = System.currentTimeMillis();
		final PageRanker pageRanker = new PageRanker();
		try {
			pageRanker.loadData("test.dat");
			pageRanker.initialize();
			pageRanker.runPageRank("perplexity.out", "pr_scores.out");
			final Integer[] rankedPages = pageRanker.getRankedPages(100);
			final double estimatedTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;

			System.out.println("Top 100 Pages are:\n" + Arrays.toString(rankedPages));
			System.out.println("Proccessing time: " + estimatedTime + " seconds");
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}
}
