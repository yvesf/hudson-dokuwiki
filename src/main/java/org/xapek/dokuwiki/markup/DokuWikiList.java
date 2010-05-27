package org.xapek.dokuwiki.markup;

import java.util.ArrayList;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.NOPTransformer;
import org.apache.commons.lang.StringUtils;

public class DokuWikiList extends DokuWikiMarkup {
	private ArrayList<DokuWikiMarkup> markupElements = new ArrayList<DokuWikiMarkup>();

	public void append(DokuWikiParagraph paragraph) {
		markupElements.add(paragraph);
	}

	public void append(DokuWikiList sublist) {
		markupElements.add(sublist);
	}

	@Override
	public void toDokuWikiMarkup(StringBuilder sb) {
		renderSublist(sb, 2);
	}

	private void renderSublist(StringBuilder sb, final int indent) {
		final String padding = StringUtils.leftPad("", indent, " ");
		for (DokuWikiMarkup markup : markupElements) {
			if (markup instanceof DokuWikiParagraph) {
				sb.append(padding);
				sb.append("* ");
				markup.toDokuWikiMarkup(sb);
			} else if (markup instanceof DokuWikiList) {
				renderSublist(sb, indent + 2);
			}
		}
	}

	public static DokuWikiList simpleList(final Iterable<Object> list) {
		return simpleList(list, NOPTransformer.getInstance());
	}

	public static DokuWikiList simpleList(final Iterable<? extends Object> list,
			final Transformer transformer) {
		return new DokuWikiList() {
			{
				for (Object o : list) {
					append(new DokuWikiParagraph(transformer.transform(o).toString()));
				}
			}
		};
	}
}
