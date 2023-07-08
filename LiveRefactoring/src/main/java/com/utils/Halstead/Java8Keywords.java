package com.utils.Halstead;

/*
 * NOTE: The following keywords are taken from http://docs.oracle.com/javase/tutorial/java/nutsandbolts/_keywords.html
 * @author Harish
 * Total 50 Words
 */
public class Java8Keywords {
	private static String[] keywords = { "abstract", "continue", "for", "new", "switch", "assert", "default", "goto",
			"package", "synchronized", "boolean", "do", "if", "private", "this", "break", "double", "implements",
			"protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum", "instanceof", "return",
			"transient", "catch", "extends", "int", "short", "try", "char", "final", "interface", "static", "void",
			"class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while" };

	private Java8Keywords() {

	}

	public static int getKeywordsCount() {
		return keywords.length;
	}

	public static boolean isKeyword(String keyword) {
		boolean result = false;
		for (int i = 0; i < getKeywordsCount(); i++) {
			if (Java8Keywords.keywords[i].equals(keyword)) {
				result = true;
				break;
			}
		}
		return result;
	}
}
