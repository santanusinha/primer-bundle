/*
 * Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dropwizard.primer;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.primer.model.PrimerBundleConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.val;
import org.junit.Before;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author phaneesh
 */
public class BaseTest {

    protected final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
    protected final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    protected final LifecycleEnvironment lifecycleEnvironment = new LifecycleEnvironment();
    protected static final Environment environment = mock(Environment.class);
    protected final Bootstrap<?> bootstrap = mock(Bootstrap.class);
    protected final Configuration configuration = mock(Configuration.class);

    protected final PrimerBundle<Configuration> bundle = new PrimerBundle<Configuration>() {

        @Override
        public PrimerBundleConfiguration getPrimerConfiguration(Configuration configuration) {
            return primerBundleConfiguration;
        }
    };

    protected PrimerBundleConfiguration primerBundleConfiguration;


    protected static BundleTestResource bundleTestResource = new BundleTestResource();

    @Before
    public void setup() throws Exception {
        when(jerseyEnvironment.getResourceConfig()).thenReturn(new DropwizardResourceConfig());
        when(environment.jersey()).thenReturn(jerseyEnvironment);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.getObjectMapper()).thenReturn(new ObjectMapper());
        when(bootstrap.getObjectMapper()).thenReturn(new ObjectMapper());
        when(environment.getApplicationContext()).thenReturn(new MutableServletContextHandler());

        primerBundleConfiguration = PrimerBundleConfiguration.builder()
                .cacheExpiry(30)
                .cacheMaxSize(100)
                .clockSkew(60)
                .host("localhost")
                .port(9999)
                .privateKey("thisisatestkey")
                .prefix("Bearer")
                .whiteList("simple/noauth/test")
                .build();

        bundle.initialize(bootstrap);

        bundle.run(configuration, environment);

        lifecycleEnvironment.getManagedObjects().forEach(object -> {
            try {
                object.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        environment.jersey().register(bundleTestResource);
    }
}