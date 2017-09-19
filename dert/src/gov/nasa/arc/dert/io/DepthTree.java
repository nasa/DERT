package gov.nasa.arc.dert.io;

import gov.nasa.arc.dert.view.Console;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Provides a data structure to record a landscape tile path/id in a quad tree
 * structure.
 *
 */
public class DepthTree implements Serializable {

	public String id;
	public DepthTree[] child;

	@Override
	public String toString() {
		return (id);
	}
	
	public static DepthTree load(String filePath)
		throws IOException {
		CsvReader reader = new CsvReader(filePath, true, ",", "#");
		return(load(reader));
	}
	
	public static DepthTree load(InputStream inputStream)
		throws IOException {
		CsvReader reader = new CsvReader(inputStream, true, ",", "#");
		return(load(reader));
	}
	
	protected static DepthTree load(CsvReader reader)
		throws IOException {
		ArrayList<String[]> tokenList = new ArrayList<String[]>();
		String[] token = null;
		reader.open();
		token = reader.readLine();
		while (token != null) {
			if (token.length < 6) {
				throw new IllegalArgumentException("Invalid depth tree entry.");
			}
			tokenList.add(token);
			token = reader.readLine();
		}
		if (tokenList.size() == 0) {
			throw new IllegalStateException("Depth tree is empty.");
		}
		reader.close();

		Collections.sort(tokenList, new Comparator<String[]>() {
			public int compare(String[] str1, String[] str2) {
				return(Integer.valueOf(str1[0]).compareTo(Integer.valueOf(str2[0])));
			}
		});
		
		// Find the maximum index
		int maxIndex = 0;
		for (int i=0; i<tokenList.size(); ++i) {
			int val = Integer.valueOf(tokenList.get(i)[0]);
			if (val > maxIndex)
				maxIndex = val;
		}
		
		// Fill an array with tokens, each at its index.
		String[][] tokenArray = new String[maxIndex+1][];
		for (int i=0; i<tokenList.size(); ++i) {
			int index = Integer.valueOf(tokenList.get(i)[0]);
			tokenArray[index] = tokenList.get(i);
		}
		
		// Create the depth tree.
		if (tokenArray[0] == null)
			throw new IllegalStateException("Could not find top level depth tree node.");
		
		DepthTree dt = createLeaf(0, tokenArray);
		return(dt);
	}
	
	protected static DepthTree createLeaf(int index, String[][] token) {
		DepthTree dt = new DepthTree();
		dt.id = token[index][1].trim();
		for (int i=0; i<4; ++i) {
			int ii = Integer.valueOf(token[index][i+2]);
			if (ii >= 0) {
				if (dt.child == null)
					dt.child = new DepthTree[4];
				dt.child[i] = createLeaf(ii, token);
			}
		}
		return(dt);
	}

	public static void store(DepthTree depthTree, String filePath) {
		CsvWriter csvWriter = null;
		try {
			String[] column = { "Index", "Id", "Child1", "Child2", "Child3", "Child4" };
			csvWriter = new CsvWriter(filePath, column);
			csvWriter.open();
			ArrayList<String[]> leafList = new ArrayList<String[]>();
			addLeaf(depthTree, 0, leafList);
			for (int i = 0; i < leafList.size(); ++i) {
				csvWriter.writeLine(leafList.get(i));
			}
			csvWriter.close();
			Console.println(leafList.size() + " records saved to " + filePath);
		} catch (Exception e) {
			e.printStackTrace();
			if (csvWriter != null) {
				try {
					csvWriter.close();
				} catch (Exception e2) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static int addLeaf(DepthTree depthTree, int index, ArrayList<String[]> leafList) {
		String[] token = new String[6];
		token[0] = Integer.toString(index);
		token[1] = depthTree.id;
		leafList.add(token);
		if (depthTree.child == null) {
			token[2] = "-1";
			token[3] = "-1";
			token[4] = "-1";
			token[5] = "-1";
		}
		else {
			index ++;
			token[2] = Integer.toString(index);
			index = addLeaf(depthTree.child[0], index, leafList);
			index ++;
			token[3] = Integer.toString(index);
			index = addLeaf(depthTree.child[1], index, leafList);
			index ++;
			token[4] = Integer.toString(index);
			index = addLeaf(depthTree.child[2], index, leafList);
			index ++;
			token[5] = Integer.toString(index);
			index = addLeaf(depthTree.child[3], index, leafList);
		}
		return(index);
	}
}
