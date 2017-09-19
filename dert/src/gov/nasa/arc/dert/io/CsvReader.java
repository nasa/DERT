package gov.nasa.arc.dert.io;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Read a comma separated value (CSV) formatted file.
 *
 */
public class CsvReader {

	// The file to be read
	protected String filename;
	
	// The input stream to be read;
	protected InputStream inputStream;

	// The reader
	protected BufferedReader reader;

	// Titles for the file columns
	protected String[] columnName;

	// Flag indicating the first line has the column titles
	protected boolean firstLineNames;

	// The value delimiter (defaults to a comma)
	protected String delimiter;

	// Indicates that line is to be ignored (defaults to a null)
	protected String ignore;

	/**
	 * Constructor
	 * 
	 * @param filename
	 *            the CSV file
	 * @param firstLineNames
	 *            the first line contains column titles
	 */
	public CsvReader(String filename, boolean firstLineNames) {
		this(filename, firstLineNames, ",", null);
	}

	/**
	 * Constructor
	 * 
	 * @param inputStream
	 *            the CSV file
	 * @param firstLineNames
	 *            the first line contains column titles
	 */
	public CsvReader(InputStream inputStream, boolean firstLineNames) {
		this(inputStream, firstLineNames, ",", null);
	}

	/**
	 * Constructor
	 * 
	 * @param filename
	 *            the CSV file
	 * @param firstLineNames
	 *            the first line contains column titles
	 * @param delimiter
	 *            the value delimiter (defaults to comma)
	 * @param ignore
	 *            if found in first column, ignore this line. (null = disabled)
	 */
	public CsvReader(String filename, boolean firstLineNames, String delimiter, String ignore) {
		this.filename = filename;
		this.firstLineNames = firstLineNames;
		this.delimiter = delimiter;
		this.ignore = ignore;
	}

	/**
	 * Constructor
	 * 
	 * @param inputStream
	 *            the CSV file
	 * @param firstLineNames
	 *            the first line contains column titles
	 * @param delimiter
	 *            the value delimiter (defaults to comma)
	 * @param ignore
	 *            if found in first column, ignore this line. (null = disabled)
	 */
	public CsvReader(InputStream inputStream, boolean firstLineNames, String delimiter, String ignore) {
		this.inputStream = inputStream;
		this.firstLineNames = firstLineNames;
		this.delimiter = delimiter;
		this.ignore = ignore;
	}

	/**
	 * Open the file and read the first line if it contains column titles.
	 * 
	 * @throws IOException
	 * @throws EOFException
	 */
	public void open() throws IOException, EOFException {
		if (filename != null)
			reader = new BufferedReader(new FileReader(filename));
		else
			reader = new BufferedReader(new InputStreamReader(inputStream));
		if (firstLineNames) {
			String str = reader.readLine();
			if (str == null) {
				throw new EOFException();
			}
			columnName = str.split(delimiter);
			for (int i = 0; i < columnName.length; ++i) {
				columnName[i] = columnName[i].trim();
			}
		}
	}

	/**
	 * Close the file
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		reader.close();
	}

	/**
	 * Get the number of columns
	 * 
	 * @return
	 */
	public int getNumColumns() {
		if (columnName == null) {
			return (0);
		}
		return (columnName.length);
	}

	/**
	 * Given an index, get the column name
	 * 
	 * @param i
	 * @return
	 */
	public String getColumnName(int i) {
		if (columnName == null) {
			return (null);
		}
		return (columnName[i]);
	}

	/**
	 * Read a line of the file and return an array of tokens. Return null if
	 * EOF. Each token is a string representing a value.
	 * 
	 * @return
	 * @throws IOException
	 */
	public String[] readLine() throws IOException {
		String str = null;
		do {
			str = reader.readLine();
			if (str == null) {
				return (null);
			}
		} while (isIgnored(str));
		
		String[] token = str.split(delimiter);
		for (int i = 0; i < token.length; ++i) {
			token[i] = token[i].trim();
		}
		return (token);
	}
		
	private boolean isIgnored(String str) {
		if (ignore == null)
			return(false);
		if (str.startsWith(ignore))
			return(true);
		return(false);
	}

}
