package hudson.plugins.dokuwiki;

import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Mailer;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author yvesf based on twitter project
 * @author cactusman twitter
 * @author justinedelson twitter
 */
public class DokuWikiPublisher extends Notifier {

	private static final List<String> VALUES_REPLACED_WITH_NULL = Arrays
			.asList("", "(Default)", "(System Default)");

	private static final Logger LOGGER = Logger
			.getLogger(DokuWikiPublisher.class.getName());

	private String username;
	private String password;
	private String pagename;
	private String url;

	@DataBoundConstructor
	public DokuWikiPublisher(final String username, final String password,
			final String pagename, final String url) {
		this.username = cleanToString(username);
		this.password = cleanToString(password);
		this.pagename = cleanToString(pagename);
		this.url = cleanToString(url);
	}

	private static String cleanToString(String string) {
		return VALUES_REPLACED_WITH_NULL.contains(string) ? null : string;
	}

	private static Boolean cleanToBoolean(String string) {
		Boolean result = null;
		if ("true".equals(string) || "Yes".equals(string)) {
			result = Boolean.TRUE;
		} else if ("false".equals(string) || "No".equals(string)) {
			result = Boolean.FALSE;
		}
		return result;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) {
		try {
			String newStatus = createTwitterStatusMessage(build);
			((DescriptorImpl) getDescriptor()).putDokuWikiReport(url, pagename,
					username, password, newStatus);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Unable to send tweet.", e);
		}
		return true;
	}

	private String createTwitterStatusMessage(AbstractBuild<?, ?> build) {
		String projectName = build.getProject().getName();
		String result = build.getResult().toString();
		String toblame = "";
		try {
			if (!build.getResult().equals(Result.SUCCESS)) {
				toblame = getUserString(build);
			}
		} catch (Exception e) {
		}
		return String.format("==== Build Report %s for %s ====\n"
				+ "Build result: %s blaming %s", build.number, projectName,
				result, toblame);
	}

	private String getUserString(AbstractBuild<?, ?> build) throws IOException {
		StringBuilder userString = new StringBuilder("");
		Set<User> culprits = build.getCulprits();
		ChangeLogSet<? extends Entry> changeSet = build.getChangeSet();
		if (culprits.size() > 0) {
			for (User user : culprits) {
				userString.append(user.getDisplayName() + " ");
			}
		} else if (changeSet != null) {
			for (Entry entry : changeSet) {
				User user = entry.getAuthor();
				userString.append(user.getDisplayName() + " ");
			}
		}
		return userString.toString();
	}

	/**
	 * Determine if this build represents a failure or recovery. A build failure
	 * includes both failed and unstable builds. A recovery is defined as a
	 * successful build that follows a build that was not successful. Always
	 * returns false for aborted builds.
	 * 
	 * @param build
	 *            the Build object
	 * @return true if this build represents a recovery or failure
	 */
	protected boolean isFailureOrRecovery(AbstractBuild<?, ?> build) {
		if (build.getResult() == Result.FAILURE
				|| build.getResult() == Result.UNSTABLE) {
			return true;
		} else if (build.getResult() == Result.SUCCESS) {
			AbstractBuild<?, ?> previousBuild = build.getPreviousBuild();
			if (previousBuild != null
					&& previousBuild.getResult() != Result.SUCCESS) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Publisher> {
		private static final Logger LOGGER = Logger
				.getLogger(DescriptorImpl.class.getName());

		public String id;
		public String password;
		public String hudsonUrl;
		public boolean includeUrl;

		public DescriptorImpl() {
			super(DokuWikiPublisher.class);
			load();
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData)
				throws FormException {
			// set the booleans to false as defaults
			includeUrl = false;

			req.bindParameters(this, "twitter.");
			hudsonUrl = Mailer.descriptor().getUrl();
			save();
			return super.configure(req, formData);
		}

		@Override
		public String getDisplayName() {
			return "Twitter";
		}

		public String getId() {
			return id;
		}

		public String getPassword() {
			return password;
		}

		public String getUrl() {
			return hudsonUrl;
		}

		public boolean isIncludeUrl() {
			return includeUrl;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			if (hudsonUrl == null) {
				// if Hudson URL is not configured yet, infer some default
				hudsonUrl = Functions.inferHudsonURL(req);
				save();
			}
			return super.newInstance(req, formData);
		}

		public void putDokuWikiReport(final String url, final String pagename,
				final String username, final String password,
				final String message) throws Exception {

			DokuWiki dokuWiki = new DokuWiki(new URL(url), username, password);
			dokuWiki.putPage(pagename, message);
			LOGGER.info("Updated DokuWiki page: " + pagename);
		}
	}
}