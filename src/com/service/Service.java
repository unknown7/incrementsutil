package com.service;

import javax.swing.JTextField;

public interface Service {
	
	public enum Type {
		DIR, FILE
	}
	
	public enum FileType {
		JAVA("/src/com/", ".class"),
		TEST_JAVA("/src/test/", ".class"),
		CONFIG("/src/", ""),
		FILE("/WebRoot/", ""),
		NULL("", "");
		private final String matcherBeforeCompile;
		private final String matcherAfterCompile;
		private FileType (String matcherBeforeCompile, String matcherAfterCompile) {
			this.matcherBeforeCompile = matcherBeforeCompile;
			this.matcherAfterCompile = matcherAfterCompile;
		}
		public String getMatcherBeforeCompile() {
			return matcherBeforeCompile;
		}
		public String getMatcherAfterCompile() {
			return matcherAfterCompile;
		}
	}
	
	public enum SpecialSymbol {
		SLANT("/"), BACK_SLANT("\\"), INNER_CLASS_SEPARATOR("$");
		private final String symbol;
		private SpecialSymbol(String symbol) {
			this.symbol = symbol;
		}
		public String getSymbol() {
			return symbol;
		}
	}
	
	public enum Config {
		COFNIG("config.properties"), PROJECTPATH("projectPath"), CREATEPATH("createPath");
		private final String path;
		private Config(String path) {
			this.path = path;
		}
		public String getPath() {
			return path;
		}
	}
	
	void createIncrementsFolder(String filePath, String projectPath, String createPath) throws Exception;
	
	void updateProps(String projectPath, String createPath);
	
	void fillInText(JTextField projectPath, JTextField createPath);
}
