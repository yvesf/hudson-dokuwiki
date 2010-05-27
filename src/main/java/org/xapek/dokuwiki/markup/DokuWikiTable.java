package org.xapek.dokuwiki.markup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DokuWikiTable extends DokuWikiMarkup {
	private final List<List<DokuWikiMarkup>> rows;
	private final List<DokuWikiParagraph> columnHeaders;

	public DokuWikiTable(List<List<DokuWikiMarkup>> rows) {
		this.rows = rows;
		this.columnHeaders = null;
	}

	public DokuWikiTable(List<DokuWikiParagraph> columnHeaders,
			List<List<DokuWikiMarkup>> rows) {
		this.rows = rows;
		this.columnHeaders = columnHeaders;
	}

	@Override
	public void toDokuWikiMarkup(StringBuilder sb) {
		if (columnHeaders != null) {
			sb.append("^");
			for (DokuWikiMarkup columnHeader : columnHeaders) {
				columnHeader.toDokuWikiMarkup(sb);
				sb.append("^");
			}
			sb.append("\n");
		}

		for (List<DokuWikiMarkup> row : rows) {
			sb.append("|");
			for (DokuWikiMarkup column : row) {
				column.toDokuWikiMarkup(sb);
				sb.append("|");
			}
			sb.append("\n");
		}
	}

	public static DokuWikiTable simpleKeyValueTable(final String[] headers,
			final Map<? extends Object, ? extends Object> keyValueMap) {
		List<List<DokuWikiMarkup>> rows = new ArrayList<List<DokuWikiMarkup>>();
		for (final Object key : keyValueMap.keySet()) {
			ArrayList<DokuWikiMarkup> arrayList = new ArrayList<DokuWikiMarkup>();
			arrayList.add(new DokuWikiParagraph(key.toString()));
			arrayList
					.add(new DokuWikiParagraph(keyValueMap.get(key).toString()));
			rows.add(arrayList);
		}
		List<DokuWikiParagraph> columnHeaders = new ArrayList<DokuWikiParagraph>();
		for (String header : headers) {
			columnHeaders.add(new DokuWikiParagraph(header));
		}
		return new DokuWikiTable(rows);
	}

}
