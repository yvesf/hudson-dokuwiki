package org.xapek.dokuwiki.markup;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

public class DokuWikiSection extends DokuWikiMarkup {

	final int level;
	final String title;
	final private ArrayList<DokuWikiMarkup> markupElements = new ArrayList<DokuWikiMarkup>();

	public DokuWikiSection(final int level, final String title) {
		this.level = level;
		this.title = title;
	}

	public void append(DokuWikiMarkup markup) {
		markupElements.add(markup);
	}

	public void toDokuWikiMarkup(final StringBuilder sb) {
		final String fooMarkup = StringUtils.leftPad("", level, "=");
		sb.append(fooMarkup);
		sb.append(escapeString(title));
		sb.append(fooMarkup);
		sb.append("\n");

		for (DokuWikiMarkup markup : markupElements) {
			markup.toDokuWikiMarkup(sb);
		}
	}

	public static DokuWikiSection level5(final String title) {
		return new DokuWikiSection(5, title);
	}

	public static DokuWikiSection level4(final String title) {
		return new DokuWikiSection(4, title);
	}

	public static DokuWikiSection level3(final String title) {
		return new DokuWikiSection(3, title);
	}
}
