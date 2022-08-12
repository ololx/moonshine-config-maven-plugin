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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final String ROW_FORMAT = "| %-40s | %-60s |";

    private static final String ROW_BORDER = "+"
            + Stream.iterate(0, i -> i++).limit(105).map(i -> "-").reduce(String::concat).get()
            + "+";

    private static final String HEADER = String.format(ROW_FORMAT, "PROPERTY", "VALUE");

    @Parameter(property = "resources", defaultValue = "${project.resources}", required = true, readonly = true)
    private List<Resource> resources = new ArrayList<>();

    @Parameter(property = "testResources", defaultValue = "${project.testResources}", required = true, readonly = true)
    private List<Resource> testResources;

    FileWalker walker = new FileWalker(info -> getLog().info(info));

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
            Map<String, File> configFiles = walker.walk(resource.getDirectory());
            for (Map.Entry<String, File> configFile : configFiles.entrySet()) {
                List<String> lines = Files.readAllLines(configFile.getValue().toPath());
                getLog().info("Reading config file - " + configFile.getKey());

                if (lines.isEmpty()) {
                    getLog().info("EMPTY\n");
                    continue;
                }

                getLog().info(ROW_BORDER);
                getLog().info(HEADER);

                for (String line : lines) {
                    String[] propertyTuples = line.split("=");

                    if (propertyTuples.length < 2) {
                        continue;
                    }

                    printRow(
                            propertyTuples[0].trim(),
                            getStringOrSplitInArray(propertyTuples[1].trim(), 60)
                    );
                }

                getLog().info(ROW_BORDER + "\n");
            }
        }
    }

    private String[] getStringOrSplitInArray(String str, int size) {
        return str == null ? new String[0] : str.split("(?<=\\G.{"+size+"})");
    }

    private void printRow(String first, String[] values) {
        getLog().info(ROW_BORDER);

        for (String value : values) {
            getLog().info(String.format(ROW_FORMAT, first, value));
        }
    }

    private static class FileWalker {

        private final Consumer<String> log;

        private FileWalker(Consumer<String> log) {
            this.log = Objects.requireNonNull(log);
        }

        public Map<String, File> walk(String directory) throws IOException {
            File root = new File(directory);
            if (!root.exists()) {
                return Collections.emptyMap();
            }

            return Files.walk(root.toPath())
                    .filter(file -> file.getFileName().toString().contains(".properties"))
                    .peek(file -> this.log.accept("Walk on property file - " + file))
                    .collect(Collectors.toMap(file -> file.getFileName().toString(), Path::toFile));
        }
    }
}
