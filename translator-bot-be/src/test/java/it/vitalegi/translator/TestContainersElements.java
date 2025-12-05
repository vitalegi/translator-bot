package it.vitalegi.translator;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class TestContainersElements {
    public static LocalStackContainer S3 = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.2")).withServices(LocalStackContainer.Service.S3);


    public static void startS3() {
        var start = System.currentTimeMillis();
        safeStart(S3);
        System.setProperty("AWS_REGION", S3.getRegion());
        System.setProperty("AWS_ACCESS_KEY_ID", S3.getAccessKey());
        System.setProperty("AWS_SECRET_ACCESS_KEY", S3.getSecretKey());
        System.setProperty("AWS_BUCKET", "test-bucket");
        log.info("S3 TestContainers instances started in {}ms", System.currentTimeMillis() - start);
    }

    public static void stopS3() {
        var start = System.currentTimeMillis();
        safeStop(S3);
        System.clearProperty("AWS_REGION");
        System.clearProperty("AWS_ACCESS_KEY_ID");
        System.clearProperty("AWS_SECRET_ACCESS_KEY");
        System.clearProperty("AWS_BUCKET");
        log.info("S3 TestContainers instances stopped in {}ms", System.currentTimeMillis() - start);
    }

    protected static void safeStart(Startable startable) {
        try {
            startable.start();
        } catch (Throwable e) {
            log.error("Failed to initialize container", e);
        }
    }

    protected static void safeStop(Startable startable) {
        if (startable != null) {
            startable.stop();
        }
    }
}
