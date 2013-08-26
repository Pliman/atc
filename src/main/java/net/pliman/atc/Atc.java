/**
 * Atc.java 2013-8-23
 */
package net.pliman.atc;

import static java.lang.System.out;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * cat angular template files with given prefix and post fix, get PARAMS from console
 * <pre>
 *     s, to, from
 *     only s: s=>console
 *     only to: doing nothing
 *     only from: from=>console
 *     s+to: s=>to
 *     s+from: s+from=>console
 *     from+to: from=>to
 *     s+to+from: s+from=>to
 * </pre>
 * please make sure file encoding are all utf-8
 */
public class Atc {
	private static final String[] PARAMS = {"s", "to", "from", "pre", "post", "end", "prePath"};
	private static final int BUFSIZE = 1024 * 1024;

	private Map<String, String> paramMap = new HashMap<String, String>(7);

	private Map<String, Pattern> patternMap = new HashMap<String, Pattern>(7);

	public static void main(String[] args) {
		new Atc().compress(args);
	}

	private void init () {
		patternMap.put("s",Pattern.compile("-s\\s\\[(.+?)\\]\\s"));
		patternMap.put("to",Pattern.compile("-to\\s(.+?)\\s"));
		patternMap.put("from",Pattern.compile("-from\\s(.+?)\\s"));
		patternMap.put("pre",Pattern.compile("-pre\\s\\[(.+?)\\]\\s"));
		patternMap.put("post",Pattern.compile("-post\\s\\[(.+?)\\]\\s"));
		patternMap.put("end",Pattern.compile("-(end)\\s"));
		patternMap.put("prePath",Pattern.compile("-prePath\\s(.+?)\\s"));
	}

	public void compress(String[] args) {
		this.init();

		String allArgs = join(args, " ") + " ";
		out.println("*****Command is: " + allArgs);

		for (String param : PARAMS) {
			Pattern paramPattern = patternMap.get(param);

			Matcher m = paramPattern.matcher(allArgs);

			while (m.find()) {
				paramMap.put(param, m.group(1));
			}
		}

		boolean s = (paramMap.get("s") != null);
		boolean to = (paramMap.get("to") != null);
		boolean from = (paramMap.get("from") != null);
		boolean pre = (paramMap.get("pre") != null);
		paramMap.put("pre", pre ? paramMap.get("pre") : "");
		boolean post = (paramMap.get("post") != null);
		paramMap.put("post", post ? paramMap.get("post") : "");
		boolean end = (paramMap.get("end") != null);
		boolean prePath = (paramMap.get("prePath") != null);
		paramMap.put("prePath", prePath ? paramMap.get("prePath") : "");

		// if no "to", sink to console
		if (end) {
			mergeString(paramMap.get("to"), "\"\"];", "", "");
		}else if (s && !to && !from) {
			out.println("*****s && !to && !from. No dest file designated, sink to console");

			out.print(paramMap.get("pre"));
			out.print(paramMap.get("s"));
			out.println(paramMap.get("post"));
		} else if (!s && to && !from) {
			out.println("*****!s && to && !from. No origin designated, sinking");

			mergeString(paramMap.get("to"), "", paramMap.get("pre"), paramMap.get("post"));
		} else if (!s && !to && from) {
			out.println("*****!s && !to && from. No dest files designated, sink to console");

			printFile(paramMap.get("from"), paramMap.get("pre"), paramMap.get("post"));
		} else if (s && to && !from) {
			out.println("*****s && to && !from. Sinking");

			mergeString(paramMap.get("to"), paramMap.get("s"), paramMap.get("pre"), paramMap.get("post"));
		} else if (s && !to && from) {
			out.println("*****s && !to && from. No dest files designated, sink to console");

			out.print(paramMap.get("pre"));
			out.print(paramMap.get("s"));
			printFile(paramMap.get("from"), "", paramMap.get("post"));
		} else if (!s && to && from) {
			out.println("*****!s && to && from. Sinking");

			mergeFile(paramMap.get("to"), paramMap.get("from"), paramMap.get("pre"), paramMap.get("post"), paramMap.get("prePath"));
		} else if (s && to && from) {
			out.println("*****s && to && from. Sinking");

			mergeString(paramMap.get("to"), paramMap.get("s"), paramMap.get("pre"), "");
			mergeFile(paramMap.get("to"), paramMap.get("from"), "", paramMap.get("post"), paramMap.get("prePath"));
		}
	}

	private String join(String[] arr, String joiner) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0, length = arr.length; i < length; i++) {
			if (i > 0) {
				builder.append(joiner).append(arr[i]);
			} else {
				builder.append(arr[i]);
			}
		}

		return builder.toString();
	}

	private void printFile(String from, String prefix, String postfix) {
		BufferedReader reader = null;
		try {
			out.print(prefix);
			reader = new BufferedReader(new FileReader(from));

			String line = reader.readLine();

			while (line != null) {
				out.print(line);

				line = reader.readLine();
			}
			out.println(postfix);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ignore) {
			}
		}
	}

	private void mergeString(String outFile, String str, String prefix, String postfix) {
		BufferedWriter writer = null;
		out.println("*****Merge " + str + " into " + outFile);
		try {
			writer = new BufferedWriter(new FileWriter(outFile, true));
			writer.write(prefix);
			writer.write(str);
			writer.write(postfix);
			out.println("*****Merged!! ");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException ignore) {
			}
		}
	}

	private void mergeFile(String outFile, String from, String prefix, String postfix, String prePath) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		StringBuilder builder = new StringBuilder();

		try {
			out.print(prefix);
			reader = new BufferedReader(new FileReader(from));
			writer = new BufferedWriter(new FileWriter(outFile, true));
			writer.write(prefix);

			if (!"".equals(prePath)) {
				writer.write("\"" + (outFile.indexOf(prePath)!=-1?outFile.substring(prePath.length(), outFile.length()):outFile).replaceAll("\\\\","/") + "\":\"");
			}else {
				writer.write("\"" + outFile.replaceAll("\\\\","/") + "\":\"");
			}


			String line = reader.readLine();

			while (line != null) {
				builder.append(line);
				line = reader.readLine();
			}

			writer.write(builder.toString().replaceAll("\"","\\\\\""));

			writer.write("\",");
			writer.write(postfix);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (IOException ignore) {
			}
		}
	}
}
