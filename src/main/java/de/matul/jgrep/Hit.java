package de.matul.jgrep;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

public class Hit implements Comparable<Hit> {

	private static final Color ERROR_COLOR = new Color(0xa00000);
	private static final Color LINENO_COLOR = new Color(0x8f8f8f);
	private static final int MAX_LENGTH = 100;
	public static final Color MATCH_COLOR = Color.MAGENTA;
	private final String filename;
	private final int lineno;
	private final int pos;
	private int lineOffset;
	private final int length;
	private final String line;
	private String visibleString;
	private int visibleOffset;
	private final boolean error;


	public Hit(String filename, int lineno, int pos, int lineOffset, int length, String line) {
		
		assert pos + length <= line.length() : 
			line + ":" + line.length() + ":" + pos + ":" + length;
		
		this.filename = filename;
		this.lineno = lineno;
		this.pos = pos;
		this.lineOffset = lineOffset;
		this.length = length;
		this.line = line;
		this.error = false;
		
		this.visibleString = line;
		this.visibleOffset = pos;
		if(line.length() > MAX_LENGTH) {
			int from = Math.max(0, pos - MAX_LENGTH/2);
			int upto = Math.min(line.length(), pos + MAX_LENGTH/2);
			this.visibleString = line.substring(from, upto);
			this.visibleOffset = pos - from;
		}
	}

	public Hit(String filename, String errorMessage) {
		this.filename = filename;
		this.length = this.lineOffset = this.pos = this.lineno = -1;
		this.line = errorMessage;
		this.error = true;
	}
	
	@Override
	public String toString() {
		return toAscii();
	}
	
	private AttributedString cachedString;
	
	public AttributedString getAttributedString(boolean onlyMatch) {
		if(cachedString == null) {
			if(isError()) {
				String msg = filename + ": " + line;
				cachedString = new AttributedString(msg);
				cachedString.addAttribute(TextAttribute.FOREGROUND, ERROR_COLOR, 0, msg.length());
			} else if(onlyMatch) {
				String prefix = filename + ":" + lineno + ": ";
				String match = visibleString.substring(visibleOffset, visibleOffset + length);
				cachedString = new AttributedString(prefix + match);
				cachedString.addAttribute(TextAttribute.FOREGROUND, MATCH_COLOR, prefix.length(), prefix.length() + length);
			} else {
				String prefix = filename + ":" + lineno + ": ";
				if (line != visibleString) {
					// really use == here!
					prefix += "[...]";
				}
				cachedString = new AttributedString(prefix + visibleString);
				int before = prefix.length();
				int begin = before + visibleOffset;
				int end = begin + length;
				cachedString.addAttribute(TextAttribute.FOREGROUND, LINENO_COLOR, 0, before);
				cachedString.addAttribute(TextAttribute.FOREGROUND, MATCH_COLOR, begin, end);
			}
		}
		return cachedString;
	}
	
	public String toAscii() {
		return filename + ":" + lineno + ": " + visibleString;
	}

	public int compareTo(Hit o) {
		int filenameComp = filename.compareTo(o.filename);
		if(filenameComp == 0) {
			return lineno - o.lineno;
		} else {
			return filenameComp;
		}
	}
	
	public boolean isError() { 
		return error;
	}
	
	public String getFilename() {
		return filename;
	}

	public int getLineno() {
		return lineno;
	}

	public int getPos() {
		return pos;
	}

	public int getLength() {
		return length;
	}

	public String getLine() {
		return line;
	}

	public int getLineOffset() {
		return lineOffset;
	}

}
