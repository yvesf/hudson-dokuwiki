package org.xapek.dokuwiki.markup;

public abstract class DokuWikiMarkup {
	public String toDokuWikiMarkup() {
		StringBuilder sb = new StringBuilder();
		toDokuWikiMarkup(sb);
		return sb.toString();
	}

	public abstract void toDokuWikiMarkup(final StringBuilder sb);
	
	
	// TODO implement this ;)
	protected String escapeString(final String text) {
		return text;
	}
}
