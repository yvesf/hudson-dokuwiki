package hudson.plugins.dokuwiki;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

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

	/**
	 * @param args
	 * @throws MalformedURLException
	 * @throws XmlRpcException
	 */
	public static void main(String[] args) throws MalformedURLException,
			XmlRpcException {
		final DokuWiki dokuWiki = new DokuWiki(new URL(
				"http://xapek.org/~yvesf/duplo/lib/exe/xmlrpc.php"),
				"yfischer", "53145314");

		dokuWiki.putPage("qs:test", "testbla from java");
	}
}
