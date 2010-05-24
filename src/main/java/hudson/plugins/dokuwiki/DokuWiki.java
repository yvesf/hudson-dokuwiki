package hudson.plugins.dokuwiki;

import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

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

		public void h1(final String header1) {
			stringBuilder.append("======");
			stringBuilder.append(header1);
			stringBuilder.append("======\n");
		}

		public void paragraph(final String paragraph) {
			stringBuilder.append(paragraph);
			stringBuilder.append("\n\n");
		}

		public void ul(final Iterable<String> list) {
			stringBuilder.append("\n");
			for (String o : list) {
				stringBuilder.append("  * ");
				stringBuilder.append(o);
				stringBuilder.append("\n");
			}
			stringBuilder.append("\n");
		}

		@Override
		public String toString() {
			return stringBuilder.toString();
		}

		public void h2(final String header2) {
			stringBuilder.append("=====");
			stringBuilder.append(header2);
			stringBuilder.append("=====\n");
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
