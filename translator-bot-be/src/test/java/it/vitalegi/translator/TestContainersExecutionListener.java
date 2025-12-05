package it.vitalegi.translator;

import lombok.extern.slf4j.Slf4j;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

@Slf4j
public class TestContainersExecutionListener implements TestExecutionListener {

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        TestContainersElements.startS3();
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        TestContainersElements.stopS3();
    }

}
