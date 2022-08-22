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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * project moonshine-config-maven-plugin
 * created 20.08.2022 22:27
 *
 * @author Alexander A. Kropotin
 */
public class FileWalker {

    public List<File> walk(String rootDirectory) {
        File file = new File(rootDirectory);

        if (!file.exists() || !file.isDirectory()) {
            return Collections.emptyList();
        }

        return this.walk(file);
    }

    public List<File> walk(File... files) {
        if (files == null) {
            return Collections.emptyList();
        }

        return this.walk(Arrays.asList(files));
    }

    public List<File> walk(Iterable<File> files) {
        if (files == null) {
            return Collections.emptyList();
        }

        List<File> walked = new ArrayList<>();

        files.forEach(file -> {
            if (file.isDirectory()) {
                walked.addAll(walk(file.listFiles()));
            } else {
                walked.add(file);
            }
        });

        return walked;
    }
}
