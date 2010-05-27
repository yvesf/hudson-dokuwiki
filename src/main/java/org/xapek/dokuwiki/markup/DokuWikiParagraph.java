package org.xapek.dokuwiki.markup;

/**
 * NOTE: you need to use a empty paragraph to render a real paragraph in dokuWiki
 * 
 * @author yvesf
 *
 */
public class DokuWikiParagraph extends DokuWikiMarkup {
	final String text;

	public DokuWikiParagraph(final String text) {
		this.text = text;
	}

	@Override
	public void toDokuWikiMarkup(StringBuilder sb) {
		sb.append(escapeString(text));
		sb.append("\n");
	}
}
