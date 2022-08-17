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

    private static final List<String> HEADER;

    private static final List<PseudoTable.ColumnFormat> COLUMN_FORMATS;

    static {
        HEADER = new ArrayList<String>() {{
            add("PROPERTY NAME");
            add("PROPERTY VALUE");
        }};

        COLUMN_FORMATS = new ArrayList<PseudoTable.ColumnFormat>(){{
            add(new PseudoTable.ColumnFormat(0, 40));
            add(new PseudoTable.ColumnFormat(1, 60));
        }};
    }

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
        Map<String, File> configFiles = walker.walk(files);
        for (Map.Entry<String, File> configFile : configFiles.entrySet()) {
            List<String> lines = Files.readAllLines(configFile.getValue().toPath());
            getLog().info("Reading config file - " + configFile.getValue());

            PseudoTable table = new PseudoTable(COLUMN_FORMATS);
            table.setHeader(HEADER);

            for (String line : lines) {
                List<Object> propertyTuples = Arrays.stream(line.split("=")).map(v -> (Object) v).collect(Collectors.toList());
                if (propertyTuples.size() < 2) {
                    continue;
                }
                table.addBodyRow(propertyTuples);
            }

            table.print((row) -> getLog().info(row));
            getLog().info("\n");
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
