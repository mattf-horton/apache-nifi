/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.nar.ext;

import java.net.URI;

/**
 * REVIEWERS -- PLEASE IGNORE.  THIS IS AN OBSOLETE FILE THAT WILL BE DELETED SOON.
 * REVIEWERS -- PLEASE IGNORE.  THIS IS AN OBSOLETE FILE THAT WILL BE DELETED SOON.
 * REVIEWERS -- PLEASE IGNORE.  THIS IS AN OBSOLETE FILE THAT WILL BE DELETED SOON.
 *
 * Basic Specification for an artifact/nar that can be resolved and loaded from
 * registry. This basic implementation relies on file system based repository
 * structure as per maven specification.
 * Every registry/repository provider should to extend this class and implement
 * their own way of resolving an artifact by overriding {@link #resolve()} .
 */
public class NarExtensionSpec {

    private String artifactId;
    private String groupId;
    private String packaging = NAR_PACKAGING;
    private URI resourceURI;
    private String version;
    public static final String NAR_PACKAGING = "nar";


    /**
     *
     */
    public NarExtensionSpec() {
        super();
    }

    /**
     * @param groupId
     *            G of GAV
     * @param artifactId
     *            A of GAV
     * @param version
     *            version number if any
     */
    public NarExtensionSpec(String groupId, String artifactId, String version) {
        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }


    /**
     * @param groupId
     *            G of GAV
     * @param artifactId
     *            A of GAV
     * @param version
     *            version number if any
     * @param packaging
     *            type of packaging e.g nar
     */
    public NarExtensionSpec(String groupId, String artifactId, String version, String packaging) {
        this(groupId, artifactId, version);
        this.packaging=packaging;
    }

    /**
     * @param groupId
     *            G of GAV
     * @param artifactId
     *            A of GAV
     * @param version
     *            version number if any
     * @param packaging
     *            type of packaging e.g nar
     * @param resourceURI
     *            Specific {@link URI} for resource
     */
    public NarExtensionSpec(String groupId, String artifactId, String version, String packaging, URI resourceURI) {
        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
        this.resourceURI = resourceURI;
    }

    /**
     * @return the artifactId
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return the packaging
     */
    public String getPackaging() {
        return packaging;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Resolves the artifact/nar and provides a URI from where the artifact can
     * be read as File/Stream etc.
     *
     * @return returns resource URI if available else null
     */
    public URI resolve() {
        return resourceURI;

    }

    @Override
    public String toString() {
        return resourceURI.toString();
    }

}
