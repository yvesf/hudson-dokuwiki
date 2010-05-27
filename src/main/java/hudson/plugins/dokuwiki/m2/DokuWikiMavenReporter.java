package hudson.plugins.dokuwiki.m2;

import hudson.Extension;
import hudson.Launcher;
import hudson.maven.ExecutedMojo;
import hudson.maven.MavenBuild;
import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.util.FormValidation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.xmlrpc.XmlRpcException;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.xapek.dokuwiki.DokuWiki;
import org.xapek.dokuwiki.TypedTransformer;
import org.xapek.dokuwiki.markup.DokuWikiList;
import org.xapek.dokuwiki.markup.DokuWikiPage;
import org.xapek.dokuwiki.markup.DokuWikiParagraph;
import org.xapek.dokuwiki.markup.DokuWikiSection;
import org.xapek.dokuwiki.markup.DokuWikiTable;

import com.jhlabs.image.AverageFilter;

/**
 * DokuWiki Reporter for M2 project
 * 
 * @author yvesf
 */
public class DokuWikiMavenReporter extends MavenReporter {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger
			.getLogger(DokuWikiMavenReporter.class.getName());

	@Extension
	public static final MavenReporterDescriptor DESCRIPTOR = new DokuWikiMavenReporterDescriptor();

	public String username;
	public String password;
	public String pagename;
	public String xmlrpcurl;

	public DokuWikiMavenReporter() {
		System.out.println("DokuWikiMavenReporter.DokuWikiMavenReporter()");
	}

	@Override
	public MavenReporterDescriptor getDescriptor() {
		return DESCRIPTOR;
	}

	// @Override
	// public Action getProjectAction(final MavenModule module) {
	// // final Map<String, Document> map = new LinkedHashMap<String,
	// // Document>();
	// // for (final Document doc : documents) {
	// // map.put(doc.getId(), doc);
	// // }
	// // return new DocLinksMavenAction(module, map);
	// return null;
	// }

	@Override
	public boolean end(final MavenBuild build, final Launcher launcher,
			final BuildListener listener) throws InterruptedException,
			IOException {
		LOGGER.entering(getClass().getName(), "end", new Object[] { build,
				launcher, listener });

		// final PrintStream mavenPrintStream = listener.getLogger();
		final URL url = new URL(xmlrpcurl);
		final DokuWiki dokuWiki = new DokuWiki(url, username, password);
		final DokuWikiPage dokuWikiPage = new DokuWikiPage("Hudson Report for "
				+ build.getProject().getName());

		Map<String, String> buildStats = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put(Messages.DokuWikiMavenReporter_lastStable(), build
						.getProject().getLastStableBuild().getId()
						+ "- "
						+ build.getProject().getLastStableBuild()
								.getTimestamp().getTime());
				put(Messages.DokuWikiMavenReporter_lastSuccessful(), build
						.getProject().getLastSuccessfulBuild().getId()
						+ " - "
						+ build.getProject().getLastSuccessfulBuild()
								.getTimestamp().getTime());
				put(Messages.DokuWikiMavenReporter_lastCompleted(), build
						.getProject().getLastCompletedBuild().getId()
						+ " - "
						+ build.getProject().getLastCompletedBuild()
								.getTimestamp().getTime());
				put(Messages.DokuWikiMavenReporter_lastFailed(), build
						.getProject().getLastFailedBuild().getId()
						+ " - "
						+ build.getProject().getLastFailedBuild()
								.getTimestamp().getTime());
			}
		};
		dokuWikiPage.append(DokuWikiTable.simpleKeyValueTable(new String[] {
				"Typ", "last Build" }, buildStats));

		// iterate through all builds
		MavenBuild currentBuild = build;
		do {
			final DokuWikiSection buildSummary = DokuWikiSection.level5("#"
					+ currentBuild.getNumber() + " - "
					+ currentBuild.getResult().toString());
			dokuWikiPage.append(buildSummary);

			buildSummary.append(new DokuWikiParagraph("Built in "
					+ currentBuild.getDurationString() + " starting "
					+ currentBuild.getTimestamp().getTime().toString()));

			{ // Mojos
				final DokuWikiSection mojoSummary = DokuWikiSection
						.level4(Messages.DokuWikiMavenReporter_executedMojos());
				buildSummary.append(mojoSummary);

				mojoSummary.append(DokuWikiList.simpleList(currentBuild
						.getExecutedMojos(),
						new TypedTransformer<ExecutedMojo, String>() {
							@Override
							public String typedTransform(ExecutedMojo input) {
								return input.goal + "("
										+ input.getDurationString() + ")";
							}
						}));
			}
			{// Changesets
				final DokuWikiSection changesets = DokuWikiSection
						.level4(Messages.DokuWikiMavenReporter_changesets());
				buildSummary.append(changesets);

				changesets.append(DokuWikiList.simpleList(currentBuild
						.getChangeSet(),
						new TypedTransformer<ChangeLogSet.Entry, String>() {
							@Override
							public String typedTransform(Entry input) {
								return input.getAuthor() + ": "
										+ input.getMsg();
							}
						}));
			}
		} while ((currentBuild = currentBuild.getPreviousBuild()) != null);

		// Put page
		LOGGER.info(Messages.DokuWikiMavenReporter_putPage(pagename));
		try {
			dokuWiki.putPage(pagename, dokuWikiPage);
		} catch (XmlRpcException e) {
			LOGGER.severe(e.getMessage());
			throw new IOException(e);
		}

		LOGGER.exiting(getClass().getName(), "end", new Object[] { build,
				launcher, listener });
		return true;
	}

	public static class DokuWikiMavenReporterDescriptor extends
			MavenReporterDescriptor {

		public DokuWikiMavenReporterDescriptor() {
			super(DokuWikiMavenReporter.class);
		}

		@Override
		public DokuWikiMavenReporter newInstance(final StaplerRequest req,
				final JSONObject formData) throws FormException {
			doCheck(formData);

			final DokuWikiMavenReporter docLinksMavenReporter = new DokuWikiMavenReporter();
			req.bindParameters(docLinksMavenReporter, "dokuwiki.");
			return docLinksMavenReporter;
		}

		private void doCheck(JSONObject formData) throws FormException {
			try {
				String xmlrpcurl= formData.getString("xmlrpcurl");
				URL url = new URL(xmlrpcurl);
				if (!"http".equals(url.getProtocol())) {
					throw new FormException("RPC-Url should begin with http://", "xmlrpcurl");
				}
			} catch (MalformedURLException e) {
				throw new FormException("RPC-Url should begin with http://", "xmlrpcurl");
			} catch (JSONException e) {
				throw new FormException(e.getMessage(), "xmlrpcurl");
			}
		}

		// /**
		// * check to see if title is not null.
		// */
		// public FormValidation doCheckTitle(
		// @AncestorInPath final AbstractProject<?, ?> project,
		// @QueryParameter final String title) throws IOException,
		// ServletException {
		// project.checkPermission(Job.CONFIGURE);
		// // return DocLinksUtils.validateTitle(title);
		// return FormValidation.ok();
		// }
		//
		// /**
		// * check to see if directory is valid and exists.
		// */
		// public FormValidation doCheckDirectory(
		// @AncestorInPath final AbstractProject<?, ?> project,
		// @QueryParameter final String dir) throws IOException,
		// ServletException {
		// project.checkPermission(Job.CONFIGURE);
		// // return DocLinksUtils.validateDirectory(project, dir);
		// return FormValidation.ok();
		// }
		//
		// /**
		// * check to see if file exists.
		// */
		// public FormValidation doCheckFile(
		// @AncestorInPath final AbstractProject<?, ?> project,
		// @QueryParameter final String dir,
		// @QueryParameter final String file) throws IOException,
		// ServletException {
		// project.checkPermission(Job.CONFIGURE);
		// // return DocLinksUtils.validateFile(project, dir, file);
		// return FormValidation.ok();
		// }

		@Override
		public String getDisplayName() {
			return "DokuWiki";
		}
	}
}
