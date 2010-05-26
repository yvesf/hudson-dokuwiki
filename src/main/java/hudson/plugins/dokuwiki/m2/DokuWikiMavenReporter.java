package hudson.plugins.dokuwiki.m2;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor.FormException;
import hudson.model.Job;
import hudson.model.Result;
import hudson.util.FormValidation;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Reporter for M2 project
 * 
 * @author Seiji Sogabe
 */
public class DokuWikiMavenReporter extends MavenReporter {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger
			.getLogger(DokuWikiMavenReporter.class.getName());
	@Extension
	public static final MavenReporterDescriptor DESCRIPTOR = new DokuWikiMavenReporterDescriptor();

	
	public String username;
	public String password;
	
	public DokuWikiMavenReporter() {
		System.out.println("DokuWikiMavenReporter.DokuWikiMavenReporter()");
	}

	@Override
	public MavenReporterDescriptor getDescriptor() {
		return DESCRIPTOR;
	}

	@Override
	public Action getProjectAction(final MavenModule module) {
		// final Map<String, Document> map = new LinkedHashMap<String,
		// Document>();
		// for (final Document doc : documents) {
		// map.put(doc.getId(), doc);
		// }
		// return new DocLinksMavenAction(module, map);
		return null;
	}

	@Override
	public boolean end(final MavenBuild build, final Launcher launcher,
			final BuildListener listener) throws InterruptedException,
			IOException {
		final PrintStream logger = listener.getLogger();

		logger.println("FOOOOOO"+password+username);

		if (build.getResult().isWorseOrEqualTo(Result.FAILURE)) {
			return true;
		}

		return true;
		//		
		// final FilePath ws = build.getWorkspace();
		// final FilePath docLinksDir = new FilePath(getDocLinksDir(build
		// .getParent()));
		//
		// try {
		// docLinksDir.deleteRecursive();
		// for (final Document doc : documents) {
		// DocLinksUtils.publishDocument(doc, ws, docLinksDir, logger);
		// }
		// } catch (final IOException e) {
		// Util.displayIOException(e, listener);
		// build.setResult(Result.UNSTABLE);
		// return true;
		// }
		//
		// build.registerAsProjectAction(this);
		//
		// return true;
	}

	public static class DokuWikiMavenReporterDescriptor extends
			MavenReporterDescriptor {

		public DokuWikiMavenReporterDescriptor() {
			super(DokuWikiMavenReporter.class);
		}

		@Override
		public DokuWikiMavenReporter newInstance(final StaplerRequest req,
				final JSONObject formData) throws FormException {
			final DokuWikiMavenReporter docLinksMavenReporter = new DokuWikiMavenReporter();
			req.bindParameters(docLinksMavenReporter, "dokuwiki.");
			return docLinksMavenReporter;
		}

		/**
		 * check to see if title is not null.
		 */
		public FormValidation doCheckTitle(
				@AncestorInPath final AbstractProject<?, ?> project,
				@QueryParameter final String title) throws IOException,
				ServletException {
			project.checkPermission(Job.CONFIGURE);
			// return DocLinksUtils.validateTitle(title);
			return FormValidation.ok();
		}

		/**
		 * check to see if directory is valid and exists.
		 */
		public FormValidation doCheckDirectory(
				@AncestorInPath final AbstractProject<?, ?> project,
				@QueryParameter final String dir) throws IOException,
				ServletException {
			project.checkPermission(Job.CONFIGURE);
			// return DocLinksUtils.validateDirectory(project, dir);
			return FormValidation.ok();
		}

		/**
		 * check to see if file exists.
		 */
		public FormValidation doCheckFile(
				@AncestorInPath final AbstractProject<?, ?> project,
				@QueryParameter final String dir,
				@QueryParameter final String file) throws IOException,
				ServletException {
			project.checkPermission(Job.CONFIGURE);
			// return DocLinksUtils.validateFile(project, dir, file);
			return FormValidation.ok();
		}

		@Override
		public String getDisplayName() {
			return "DokuWiki";
		}
	}
}
