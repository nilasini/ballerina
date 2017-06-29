/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.natives.annotation.processor;

import org.ballerinalang.natives.SystemPackageRepository;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.repository.BuiltinExtendedPackageRepository;
import org.ballerinalang.util.repository.BuiltinPackageRepository;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;


/**
 * Builder class to generate the ballerina builtin package repository provider class.
 * The class generated by this builder will act as provider for builtin package loading and
 * package names exposed to ballerina tooling, this is registered via java SPI enabling
 * external package exporters to expose builtin packages.
 */
public class BuiltinPackageRepositoryClassBuilder {

    private static final String SERVICES = "services/";
    private static final String META_INF = "META-INF/";

    private Writer sourceFileWriter;
    private String pkgRepositoryClass;
    private boolean isSystemPackageRepo;

    public BuiltinPackageRepositoryClassBuilder(Filer filer,
                                                String pkgRepositoryClass,
                                                boolean isSystemPackageRepo) {
        this.pkgRepositoryClass = pkgRepositoryClass;
        //TODO remove interface SystemPackageRepository with proper way
        this.isSystemPackageRepo = isSystemPackageRepo;
        try {
            JavaFileObject javaFile = filer.createSourceFile(pkgRepositoryClass);
            sourceFileWriter = javaFile.openWriter();
        } catch (IOException e) {
            throw new BallerinaException("failed to initialize builtin package " +
                    "repository source generator: " + e.getMessage());
        }
        createBuiltinPackageRepositoryServiceMetaFile(filer);
    }

    /**
     * Build the builtin repository class and write the source file to the maven build target
     * relevant package location.
     */
    public void build() {
        String className = pkgRepositoryClass.substring(pkgRepositoryClass.lastIndexOf(".") + 1,
                pkgRepositoryClass.length());
        String pkgName = pkgRepositoryClass.substring(0, pkgRepositoryClass.lastIndexOf("."));
        String pkgStr = "package " + pkgName + ";\n\n";
        String importRepoStr = "import " + BuiltinExtendedPackageRepository.class.getCanonicalName() + ";\n";
        String importSystemRepoStr = "import " + SystemPackageRepository.class.getCanonicalName() + ";\n";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(pkgStr);
        if (isSystemPackageRepo) {
            stringBuilder.append(importSystemRepoStr);
        }
        stringBuilder.append(importRepoStr);
        stringBuilder.append("\n/**\n" +
                " * Generated Source -  Do not Edit manually.\n" +
                " */");
        stringBuilder.append("\npublic class " + className + " extends "
                + BuiltinExtendedPackageRepository.class.getSimpleName());
        if (isSystemPackageRepo) {
            stringBuilder.append("\n implements " + SystemPackageRepository.class.getSimpleName());
        }
        stringBuilder.append(" { \n");
        stringBuilder.append("\n  public " + className + "() {\n");
        stringBuilder.append("    super(" + className + ".class);\n  }");
        stringBuilder.append("\n}\n");
        try {
            sourceFileWriter.write(stringBuilder.toString());
        } catch (IOException e) {
            throw new BallerinaException("error while writing source to file: " + e.getMessage());
        } finally {
            if (sourceFileWriter != null) {
                try {
                    sourceFileWriter.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    /**
     * Create the configuration file in META-INF/services, required for java service
     * provider api related to built in package repository.
     *
     * @param filer {@link Filer} associated with this annotation processor.
     * @param
     */
    private void createBuiltinPackageRepositoryServiceMetaFile(Filer filer) {
        if (pkgRepositoryClass == null || pkgRepositoryClass.isEmpty()) {
            return;
        }
        Writer configWriter = null;
        try {
            //Find the location of the resource/META-INF directory.
            FileObject metaFile = filer.createResource(StandardLocation.CLASS_OUTPUT, "", META_INF + SERVICES +
                    BuiltinPackageRepository.class.getCanonicalName());
            configWriter = metaFile.openWriter();
            configWriter.write(pkgRepositoryClass);
        } catch (IOException e) {
            throw new BallerinaException("error while generating config file: " + e.getMessage());
        } finally {
            if (configWriter != null) {
                try {
                    configWriter.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

}