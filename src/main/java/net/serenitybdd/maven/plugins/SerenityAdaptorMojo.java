package net.serenitybdd.maven.plugins;

import net.thucydides.core.guice.Injectors;
import net.thucydides.core.reports.TestOutcomeAdaptorReporter;
import net.thucydides.core.reports.adaptors.AdaptorService;
import net.thucydides.core.reports.adaptors.ExtendedTestOutcomeAdaptor;
import net.thucydides.core.reports.adaptors.TestOutcomeAdaptor;
import net.thucydides.core.util.EnvironmentVariables;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * This plugin generates converts external (e.g. xUnit) files into Serenity reports.
 */
@Mojo( name = "import", requiresProject=false)
public class SerenityAdaptorMojo extends AbstractMojo {

    /**
     * Aggregate reports are generated here
     */
    @Parameter(property = "import.target", defaultValue = "${user.dir}/target/site/serenity", required=true)
    public File outputDirectory;

    /**
     * The format used for the external reports
     */
    @Parameter(property = "import.format", required=true)
    public String format;

    /**
     * The context within which the source test results were generated
     */
    @Parameter(required=false)
    public String sourceContext;


    /**
     * The context within which the source test results were generated
     */
    @Parameter(required=false)
    public String scenarioStatus;

    /**
     * External test reports are read from here if necessary.
     * This could be either a directory or a single file, depending on the adaptor used.
     * For some adaptors (e.g. database connectors), it will not be necessary.
     */
    @Parameter(property = "import.source")
    public File source;

    private final EnvironmentVariables environmentVariables;
    private final AdaptorService adaptorService;
    private final TestOutcomeAdaptorReporter reporter = new TestOutcomeAdaptorReporter();

    public SerenityAdaptorMojo(EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
        this.adaptorService = new AdaptorService(environmentVariables);
    }

    public SerenityAdaptorMojo() {
        this(Injectors.getInjector().getProvider(EnvironmentVariables.class).get() );
    }

    protected File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Importing external test reports");
        getLog().info("Source directory: " + source);
        getLog().info("Output directory: " + getOutputDirectory());

        try {
            TestOutcomeAdaptor adaptor = adaptorService.getAdaptor(format);
            if(adaptor instanceof ExtendedTestOutcomeAdaptor){
                ((ExtendedTestOutcomeAdaptor)adaptor).setSourceContext(sourceContext);
                ((ExtendedTestOutcomeAdaptor)adaptor).setScenarioStatus(scenarioStatus);
            }
            getLog().info("Adaptor: " + adaptor);
            reporter.registerAdaptor(adaptor);
            reporter.setOutputDirectory(outputDirectory);
            reporter.generateReportsFrom(source);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(),e);
        }
    }
}
