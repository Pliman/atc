/*
 * TestAtc.java 2013-8-22
 */
package net.pliman.atc;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * test compress
 */
public class TestAtc {
	private static final String FROM_FILE = "D:\\MCMS\\atc\\src\\test\\resources\\a.txt";
	private static final String TO_FILE = "D:\\MCMS\\atc\\src\\test\\resources\\to.txt";

	@Test
	public void testAtc () {
		clearFile(TO_FILE);

		//s && !to && !from -- console prestringpost
		String[] param1 = {"-s","string","-pre","pre","-post","post"};
		new Atc().compress(param1);

		//!s && to && !from -- file prepost
		String[] param2 = {"-to",TO_FILE,"-pre","pre","-post","post"};
		new Atc().compress(param2);

		//!s && !to && from --console pre1post
		String[] param3 = {"-from",FROM_FILE,"-pre","pre","-post","post"};
		new Atc().compress(param3);

		//s && to && !from -- file prestringpost
		String[] param4 = {"-s","string","-to",TO_FILE,"-pre","pre","-post","post"};
		new Atc().compress(param4);

		//s && !to && from -- console prestring1post
		String[] param5 = {"-s","string", "-from",FROM_FILE,"-pre","pre","-post","post"};
		new Atc().compress(param5);

		//!s && to && from -- file pre"a"post
		String[] param6 = {"-to",TO_FILE, "-from",FROM_FILE,"-pre","pre","-post","post"};
		new Atc().compress(param6);

		//s && to && from -- file prestring"a"post
		String[] param7 = {"-s","string","-to",TO_FILE, "-from",FROM_FILE,"-pre","pre","-post","post"};
		new Atc().compress(param7);
		assertEquals("prepostprestringpostpre\"a\"postprestring\"a\"post", readFile(TO_FILE));
	}

	private void clearFile (String file) {
		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("");
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (writer != null) {
					writer.close();
				}

			} catch (IOException ignore) {
			}
		}
	}

	private String readFile (String fileName) {
		BufferedReader reader = null;
		StringBuilder builder = new StringBuilder();

		try {
			reader = new BufferedReader(new FileReader(fileName));

			String line = reader.readLine();

			while(line != null){
				builder.append(line);

				line = reader.readLine();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (reader != null) {
					reader.close();
				}

				return builder.toString();
			} catch (IOException ignore) {
				return "";
			}
		}
	}
}

