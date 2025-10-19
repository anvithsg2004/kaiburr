package com.example.kaiburr.k8s;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class K8sConfig {
    @Bean
    public KubernetesClient kubernetesClient() {
        // Uses in-cluster config automatically; falls back to KUBECONFIG if running locally.
        return new KubernetesClientBuilder().build();
    }
}
