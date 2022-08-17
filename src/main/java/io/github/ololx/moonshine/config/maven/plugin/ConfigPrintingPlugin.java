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

import org.apache.maven.model.FileSet;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static final String ROW_FORMAT = '\u2502' + " %-40s " + '\u2502' + " %-60s " + '\u2502';

    private static final String TOP_ROW_BORDER = '\u250C'
            + Stream.iterate(0, i -> i++).limit(42).map(i -> "\u2500").reduce(String::concat).get()
            + "\u252C"
            + Stream.iterate(0, i -> i++).limit(62).map(i -> "\u2500").reduce(String::concat).get()
            + '\u2510';

    private static final String MIDDLE_ROW_BORDER = '\u251C'
            + Stream.iterate(0, i -> i++).limit(42).map(i -> "\u2500").reduce(String::concat).get()
            + "\u253C"
            + Stream.iterate(0, i -> i++).limit(62).map(i -> "\u2500").reduce(String::concat).get()
            + '\u2524';

    private static final String BOTTOM_ROW_BORDER = '\u2514'
            + Stream.iterate(0, i -> i++).limit(42).map(i -> "\u2500").reduce(String::concat).get()
            + "\u2534"
            + Stream.iterate(0, i -> i++).limit(62).map(i -> "\u2500").reduce(String::concat).get()
            + '\u2518';

    private static final String HEADER = String.format(ROW_FORMAT, "PROPERTY", "VALUE");

    @Parameter(
            property = "resources",
            defaultValue = "${project.build.resources}",
            required = true,
            readonly = true
    )
    private final List<String> resources = new ArrayList<>();

    @Parameter(
            property = "testResources",
            defaultValue = "${project.build.testResources}",
            required = true,
            readonly = true
    )
    private final List<String> testResources = new ArrayList<>();

    @Parameter(
            property = "outputDirectory",
            defaultValue = "${project.build.outputDirectory}",
            required = true,
            readonly = true
    )
    private String outputDirectory = "";

    FileWalker walker = new FileWalker(info -> getLog().info(info));

    public void setResources(List<Resource> resources) {
        this.resources.addAll(
                Objects.requireNonNull(resources).stream()
                        .map(FileSet::getDirectory)
                        .collect(Collectors.toList())
        );
    }

    public void setTestResources(List<Resource> testResources) {
        this.testResources.addAll(
                Objects.requireNonNull(testResources).stream()
                        .map(FileSet::getDirectory)
                        .collect(Collectors.toList())
        );
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = Objects.requireNonNull(outputDirectory);
    }

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info( "Walking through resource directories...");
        try {
            this.walkingThroughConfigFiles(resources, "resources");
            this.walkingThroughConfigFiles(testResources, "testResources");
            this.walkingThroughConfigFiles(outputDirectory, "outputDirectory");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void walkingThroughConfigFiles(List<String> files, String alias) throws IOException {
        for (String resource : files) {
            this.walkingThroughConfigFiles(resource, alias);
        }
    }

    private void walkingThroughConfigFiles(String files, String alias) throws IOException {
        getLog().info("Walking through " + alias + " - " + files);
        Map<String, File> configFiles = walker.walk(files);
        for (Map.Entry<String, File> configFile : configFiles.entrySet()) {
            List<String> lines = Files.readAllLines(configFile.getValue().toPath());
            getLog().info("Reading config file - " + configFile.getKey());

            if (lines.isEmpty()) {
                getLog().info("EMPTY\n");
                continue;
            }

            PseudoTwoColumnTable table = new PseudoTwoColumnTable();
            table.setHeader(
                    new ArrayList<String>() {{
                        add("PROPERTY NAME");
                        add("PROPERTY VALUE");
                    }}
            );

            for (String line : lines) {
                List<Object> propertyTuples = Arrays.stream(line.split("=")).map(v -> (Object) v).collect(Collectors.toList());
                table.addBodyRow(propertyTuples);
            }

            table.print((row) -> getLog().info(row));
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
