package org.xapek.dokuwiki;

import java.net.URL;
import java.util.Hashtable;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.xapek.dokuwiki.markup.DokuWikiPage;

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

	public void putPage(final String pagename, final DokuWikiPage page)
			throws XmlRpcException {
		putPage(pagename, page, "", false);
	}

	public void putPage(final String pagename, final DokuWikiPage page,
			final String summary, final boolean minor) throws XmlRpcException {
		final Hashtable<String, Object> attrs = new Hashtable<String, Object>();
		attrs.put("sum", summary);
		attrs.put("minor", minor);
		client.execute(config, "wiki.putPage", new Object[] { pagename,
				page.toDokuWikiMarkup(), attrs });
	}
}
