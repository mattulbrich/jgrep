package de.matul.jgrep;

import picocli.CommandLine.*;

import java.util.List;
import java.util.regex.Pattern;

@Command(name = "jgrep")
public class Options {

	@Option(names = { "-r", "--recursive" }, description = "Recurse through subdirectories")
	public boolean recursive;

	@Option(names = { "-c", "--charset" }, description = "Which character set to use")
	public String charset = "utf-8";

	@Option(names = { "-i", "--ignorecase" }, description = "Do not distinct between upper and lower case")
	public boolean ignoreCase;

	@Option(names = { "-F", "--fixed" }, description = "Fixed")
	public boolean fixed;

	@Option(names = "--include", description="Search only files whose base name matches GLOB", paramLabel = "GLOB",
	        converter = GlobConverter.class)
	public Pattern include;

	@Option(names = { "-m", "--max-count" }, paramLabel = "NUM", description = "Stop reading a file after NUM matching lines.")
	public int maxCount = Integer.MAX_VALUE;

	@Option(names = { "-o", "--only-matching" },
			description = "Print only the matched (non-empty) parts of a matching line, with each such part on a separate output line.")
	public boolean onlyMatching = false;

	@Option(names = "--editor", description = "The GUI text editor to be used for the 'edit' button.")
	public String editor = JGrep.DEFAULT_EDITOR;

	@Parameters(index = "0", description = "regular expression")
	public String regex;

	@Parameters(index = "1..*")
	public List<String> files;

	static class GlobConverter implements ITypeConverter<Pattern> {
		@Override
		public Pattern convert(String s) {
			String re = s.replace("\\", "\\\\").
					replace(".", "\\.").
					replace("*", ".*").
					replace("?", ".?");
			return Pattern.compile(re);
		}
	}
}