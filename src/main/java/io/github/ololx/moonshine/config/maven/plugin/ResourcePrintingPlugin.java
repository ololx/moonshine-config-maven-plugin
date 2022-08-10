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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

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
public class ResourcePrintingPlugin extends AbstractMojo {

    /**
     * This element describes all of the classpath resources such as
     * properties files associated with a project. These resources are often
     * included in the final package. The default value is src/main/resources.
     */
    @Parameter(defaultValue = "${project.build.resources/resource*}")
    private List<Resource> mainResources;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info( "Start to load all resources from '../src/main/resources/..' folders" );
        this.printResourcesPath(mainResources);
    }

    private void printResourcesPath(List<Resource> resources) {
        resources.forEach(resource -> {
            getLog().info(resource.getTargetPath());
        });
    }
}
