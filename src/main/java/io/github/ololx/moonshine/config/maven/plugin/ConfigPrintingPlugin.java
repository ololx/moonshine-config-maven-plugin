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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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

    private static final List<String> HEADER;

    private static final List<PseudoTable.ColumnFormat> COLUMN_FORMATS;

    private static final Set<String> CONFIG_FILES;

    static {
        HEADER = new ArrayList<String>() {{
            add("PROPERTY NAME");
            add("PROPERTY VALUE");
        }};

        COLUMN_FORMATS = new ArrayList<PseudoTable.ColumnFormat>(){{
            add(new PseudoTable.ColumnFormat(0, 40));
            add(new PseudoTable.ColumnFormat(1, 60));
        }};

        CONFIG_FILES = new HashSet<String>() {{
            add(".properties");
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

    FileWalker walker = new FileWalker();

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

    private void walkingThroughConfigFiles(List<String> resources, String alias)
            throws IOException {
        for (String resource : resources) {
            this.walkingThroughConfigFiles(resource, alias);
        }
    }

    private void walkingThroughConfigFiles(String resourceDir, String alias) throws IOException {
        Map<String, File> configFiles = walker.walk(resourceDir)
                .parallelStream()
                .filter(file -> {
                    return CONFIG_FILES.stream()
                            .anyMatch(type -> file.getName().contains(type));
                })
                .peek(file -> getLog().info("Walk on property file - " + file))
                .collect(Collectors.toMap(File::getName, file -> file));

        for (Map.Entry<String, File> configFile : configFiles.entrySet()) {
            Properties properties = this.readConfig(configFile.getValue());
            Set<Map.Entry<Object, Object>> lines = properties.entrySet();
            getLog().info("Reading config file - " + configFile.getValue());

            PseudoTable table = new PseudoTable(COLUMN_FORMATS);
            table.setHeader(HEADER);

            lines.forEach(property -> {
                table.addBodyRow(
                        new ArrayList<Object>() {{
                            add(property.getKey());
                            add(property.getValue());
                        }}
                );
            });

            table.print((row) -> getLog().info(row));
            getLog().info("\n");
        }
    }

    private Properties readConfig(File configFile) {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream(configFile.getPath())) {
            properties.load(input);
        } catch (IOException ex) {
            getLog().warn("Reading config file - " + configFile.getPath());
        }

        return properties;
    }
}
