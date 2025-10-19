package com.example.kaiburr.service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class K8sPodExecutor {

    private final KubernetesClient client;

    public K8sPodExecutor(KubernetesClient client) {
        this.client = client;
    }

    public String runInPod(String command) throws Exception {
        String ns = client.getNamespace();
        if (ns == null || ns.isBlank()) ns = "default";

        Pod pod = new PodBuilder()
                .withNewMetadata()
                .withGenerateName("taskrun-exec-")
                .addToLabels("app", "taskrun-exec")
                .endMetadata()
                .withNewSpec()
                .withRestartPolicy("Never")
                .addNewContainer()
                .withName("runner")
                .withImage("busybox:1.36")
                .withCommand("sh", "-c", command)
                .endContainer()
                .withActiveDeadlineSeconds(300L)
                .endSpec()
                .build();

        pod = client.pods().inNamespace(ns).resource(pod).create();
        String podName = pod.getMetadata().getName();

        client.pods().inNamespace(ns).withName(podName).waitUntilCondition(
                p -> p != null && p.getStatus() != null && (
                        "Succeeded".equals(p.getStatus().getPhase()) || "Failed".equals(p.getStatus().getPhase())),
                5, TimeUnit.MINUTES
        );

        String logs = client.pods().inNamespace(ns).withName(podName).inContainer("runner").getLog(true);

        client.pods().inNamespace(ns).withName(podName).delete();
        return logs == null ? "" : logs.trim();
    }
}
