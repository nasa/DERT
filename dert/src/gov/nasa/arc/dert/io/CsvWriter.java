package gov.nasa.arc.dert.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Write a comma separated value (CSV) formatted file.
 *
 */
public class CsvWriter {

	// The file to be written to.
	protected String filename;

	// The writer
	protected PrintWriter writer;

	// Titles for columns
	protected String columnNames;

	/**
	 * Constructor
	 * 
	 * @param filename
	 * @param columnName
	 */
	public CsvWriter(String filename, String[] columnName) {
		this.filename = filename;
		columnNames = columnName[0];
		for (int i = 1; i < columnName.length; ++i) {
			columnNames += "," + columnName[i];
		}
	}

	/**
	 * Open the file and write the column titles.
	 * 
	 * @throws IOException
	 */
	public void open() throws IOException {
		writer = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
		writer.println(columnNames);
	}

	/**
	 * Close the file
	 */
	public void close() {
		writer.flush();
		writer.close();
	}

	/**
	 * Flush all output to the file
	 */
	public void flush() {
		writer.flush();
	}

	/**
	 * Write a line of values to the file
	 * 
	 * @param column
	 */
	public void writeLine(String[] column) {
		String str = column[0];
		for (int i = 1; i < column.length; ++i) {
			str += ", " + column[i];
		}
		writer.println(str);
	}

	/**
	 * Write a line of values to the file
	 * 
	 * @param column
	 */
	public void writeLine(double[] column) {
		String str = Double.toString(column[0]);
		for (int i = 1; i < column.length; ++i) {
			str += ", " + column[i];
		}
		writer.println(str);
	}

	/**
	 * Write a line of values to the file
	 * 
	 * @param column
	 */
	public void writeLine(float[] column) {
		String str = Float.toString(column[0]);
		for (int i = 1; i < column.length; ++i) {
			str += ", " + column[i];
		}
		writer.println(str);
	}

	/**
	 * Write a line of values to the file with a timestamp
	 * 
	 * @param column
	 */
	public void writeLine(long time, double[] column) {
		String str = Long.toString(time);
		for (int i = 0; i < column.length; ++i) {
			str += ", " + column[i];
		}
		writer.println(str);
	}

	/**
	 * Write a line of values to the file with a timestamp
	 * 
	 * @param column
	 */
	public void writeLine(long time, float[] column) {
		String str = Long.toString(time);
		for (int i = 0; i < column.length; ++i) {
			str += ", " + column[i];
		}
		writer.println(str);
	}

	/**
	 * Write a line of values to the file with a timestamp and a type string
	 * 
	 * @param column
	 */
	public void writeLine(long time, String type, float[] column) {
		String str = Long.toString(time);
		str += ", " + type;
		for (int i = 0; i < column.length; ++i) {
			str += ", " + column[i];
		}
		writer.println(str);
	}

}
