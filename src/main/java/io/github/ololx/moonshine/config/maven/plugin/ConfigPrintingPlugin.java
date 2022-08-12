/**
 * Copyright 2022 the project moonshine-config-maven-plugin authors
 * and the original author or authors annotated by {@author}
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ololx.moonshine.config.maven.plugin;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * project moonshine-config-maven-plugin
 * created 07.08.2022 22:52
 *
 * @author Alexander A. Kropotin
 */
@Mojo(
        name = "print",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES
)
public final class ConfigPrintingPlugin extends AbstractMojo {

    @Parameter(property = "resources", defaultValue = "${project.resources}", required = true, readonly = true)//${project.resources}
    private List<Resource> resources = new ArrayList<>();

    @Parameter(property = "testResources", defaultValue = "${project.testResources}", required = true, readonly = true)
    private List<Resource> testResources;

    FileWalker walker = new FileWalker(getLog());

    public void setResources(List<Resource> resources) {
        this.resources.addAll(Objects.requireNonNull(resources));
    }

    public void setTestResources(List<Resource> testResources) {
        this.testResources.addAll(Objects.requireNonNull(testResources));
    }

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info( "Walking through resource directories...");
        try {
            this.walkingThroughConfigFiles(resources, "resources");
            this.walkingThroughConfigFiles(testResources, "testResources");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void walkingThroughConfigFiles(List<Resource> files, String alias) throws IOException {
        for (Resource resource : files) {
            getLog().info("Walking through " + alias + " - " + resource.getDirectory());
            walker.walk(resource.getDirectory());
        }
    }

    private static class FileWalker {

        private final Log log;

        private FileWalker(Log log) {
            this.log = Objects.requireNonNull(log);
        }

        public Map<String, File> walk(String directory) throws IOException {
            File root = new File(directory);
            if (!root.exists()) {
                return Collections.emptyMap();
            }

            return Files.walk(root.toPath())
                    .filter(file -> file.getFileName().toString().contains(".properties"))
                    .peek(file -> this.log.info("Walk on property file - " + file))
                    .collect(Collectors.toMap(file -> file.getFileName().toString(), Path::toFile));
        }
    }
}
