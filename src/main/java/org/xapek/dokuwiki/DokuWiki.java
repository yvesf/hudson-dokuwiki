package org.xapek.dokuwiki;

import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.NOPTransformer;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class DokuWiki {
	private final XmlRpcClientConfigImpl config;
	private final XmlRpcClient client;

	/**
	 * 
	 * @param url
	 *            e.g. http://example.com/lib/exe/xmlrpc.php
	 * @param username
	 * @param password
	 */
	public DokuWiki(final URL url, final String username, final String password) {
		config = new XmlRpcClientConfigImpl();
		config.setServerURL(url);
		config.setBasicUserName(username);
		config.setBasicPassword(password);

		client = new XmlRpcClient();
	}

	public void putPage(final String pagename, final String rawText)
			throws XmlRpcException {
		putPage(pagename, rawText, "", false);
	}

	public void putPage(final String pagename, final String rawText,
			final String summary, final boolean minor) throws XmlRpcException {
		final Hashtable<String, Object> attrs = new Hashtable<String, Object>();
		attrs.put("sum", summary);
		attrs.put("minor", minor);
		client.execute(config, "wiki.putPage", new Object[] { pagename,
				rawText, attrs });
	}

	public static class DokuWikiPageBuilder extends Object {
		final StringBuilder stringBuilder;

		public DokuWikiPageBuilder() {
			stringBuilder = new StringBuilder();
		}

		public void paragraph(final String paragraph) {
			stringBuilder.append(paragraph);
			stringBuilder.append("\n\n");
		}

		public void ul(final Iterable<String> list) {
			ul(list, NOPTransformer.getInstance());
		}

		public <T> void ul(final Iterable<T> list, final Transformer transformer) {
			stringBuilder.append("\n");
			for (Object o : list) {
				stringBuilder.append("  * ");
				stringBuilder.append(transformer.transform(o));
				stringBuilder.append("\n");
			}
			stringBuilder.append("\n");
		}

		@Override
		public String toString() {
			return stringBuilder.toString();
		}

		public void h1(final String header1) {
			h(6, header1);
		}

		public void h2(final String header2) {
			h(5, header2);
		}

		public void h3(final String header2) {
			h(4, header2);
		}

		public void h4(final String header2) {
			h(3, header2);
		}

		public void h(final int dokuWikiLevel, final String header) {
			final StringBuilder sb = new StringBuilder(6);
			for (int i = 0; i <= dokuWikiLevel && i <= 6; i++) {
				sb.append("=");
			}
			stringBuilder.append(sb);
			stringBuilder.append(header);
			stringBuilder.append(sb);
			stringBuilder.append("\n");
		}

		public void simpleKeyValueTable(final Map<String, String> map) {
			stringBuilder.append("^ Key ^ Value ^\n");
			for (final String key : map.keySet()) {
				stringBuilder
						.append("| " + key + " | " + map.get(key) + " |\n");
			}
			stringBuilder.append("\n");
		}

		public void pre(final String code) {
			stringBuilder.append("<code>\n" + code + "\n</code>\n");
		}
	}
}
