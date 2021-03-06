package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jvnet.hudson.test.Issue;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.plot.PlotPublisher;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;

@WithPlugins("plot")
public class PlotPluginTest extends AbstractJUnitTest {

    private FreeStyleJob job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create();
    }

    @Test
    public void generate_simple_plot() {
        job.configure();
        job.copyResource(resource("/plot_plugin/plot.csv"));
        PlotPublisher pub = job.addPublisher(PlotPublisher.class);
        pub.group.set("My group");
        pub.title.set("My plot");
        pub.source("csv", "plot.csv");
        job.save();

        job.startBuild().shouldSucceed();
        assertThatBuildHasPlot("My plot","My group");
    }

    @Test @Issue({"JENKINS-18585","JENKINS-18674"})
    public void postbuild_rendering_should_work() {
        job.configure();
        PlotPublisher pub = job.addPublisher(PlotPublisher.class);
        pub.group.set("Plots");
        pub.title.set("Some plot");
        job.save();

        job.configure();
        pub.source("csv", "plot.csv");
        job.save();

        job.startBuild().shouldSucceed();
        assertThatBuildHasPlot("Some plot","Plots");
    }

    private void assertThatBuildHasPlot(String title, String group) {
        job.visit("plot");
        find(by.xpath("//h1[contains(text(), '%s')]", group));
        find(by.xpath("//select[@name='choice']/option[contains(text(), '%s')]",title));
    }
}
