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
// TBD: shouldn't be under "nar", perhaps create org.apache.nifi.ext_resources
// as new module under nifi-commons ?

import java.io.File;
import java.net.URI;

/**
 * Basic Specification for an artifact that can be resolved and loaded from
 * external repository.  Supports separate access to a documentation stream.
 */
public abstract class AbstractExtensionSpec {

  protected String resourceType; // Example values could be "processor", "template", etc.
  protected String artifactId;   // A of GAV
  protected String groupId;      // G of GAV
  protected String version;      // V of GAV
  protected String packaging;    // Loading a resource depends on its packaging format
  protected AbstractExternalRepository repository; // The repository presenting this artifact
  protected String locatorInfo;  // Additional repo-specific info the repository needs
                                 // to resolve the artifact and its documentation
  protected File resourceFile;   // Cached result of resolving the packaged artifact file
  protected URI  resourceDoc;    // Cached result of resolving the artifact's documentation stream

  // These aren't the only allowed values, but they should be well-known values
  // so define them in constants here in the parent class.
  public static final String PROCESSOR_TYPE = "processor";
  public static final String NAR_PACKAGING = "nar";
  public static final String DOC_PACKAGE = "adoc";


  // Constructors ****************************************************** //

  /**
   *
   */
  public AbstractExtensionSpec() {
    super();
  }

  /**
   * Useful for null constructor for concrete sub-class
   */
  public AbstractExtensionSpec(String resourceType, String packaging) {
    super();
    this.resourceType = resourceType;
    this.packaging = packaging;
  }

/*
 * Constructor with basic required elements
 */
  public AbstractExtensionSpec(String resourceType, String groupId, String artifactId,
                               String version, String packaging, AbstractExternalRepository repository) {
    super();
    this.resourceType = resourceType;
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.packaging = packaging;
    this.repository = repository;
  }

  /*
   * Most AbstractExternalRepository implementations will use required elements
   * plus locatorInfo
   */
  public AbstractExtensionSpec(String resourceType, String groupId, String artifactId,
                               String version, String packaging, AbstractExternalRepository repository,
                               String locatorInfo) {
    super();
    this.resourceType = resourceType;
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.packaging = packaging;
    this.repository = repository;
    this.locatorInfo = locatorInfo;
  }

  /*
   * Support null implementation of File-based test repos by including the
   * resolved file path in the constructed AbstractExtensionSpec
   */
  public AbstractExtensionSpec(String resourceType, String groupId, String artifactId,
                               String version, String packaging, AbstractExternalRepository repository,
                               String locatorInfo, File resourceFile) {
    super();
    this.resourceType = resourceType;
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.packaging = packaging;
    this.repository = repository;
    this.locatorInfo = locatorInfo;
    this.resourceFile = resourceFile;
  }

  /*
   * Support null implementation of File-based test repos by including the
   * resolved path in the constructed AbstractExtensionSpec, for both
   * artifact and documentation
   */
  public AbstractExtensionSpec(String resourceType, String groupId, String artifactId,
                               String version, String packaging, AbstractExternalRepository repository,
                               String locatorInfo, File resourceFile, URI resourceDoc) {
    super();
    this.resourceType = resourceType;
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.packaging = packaging;
    this.repository = repository;
    this.locatorInfo = locatorInfo;
    this.resourceFile = resourceFile;
    this.resourceDoc = resourceDoc;
  }


  // General Use Methods *********************************************** //

  /**
   * Each type of resource must sub-class this AbstractExtensionSpec and provide
   * a load() method appropriate to the type.  If there are multiple packagings
   * available for the resource type, then the load() method may provide
   * multiple cases, or the AbstractExtensionSpec may be further sub-classed for each
   * resource type / packaging combination.
   *
   * @return  true if successful, false if not.
   */
  public abstract boolean load();


  /**
   * @return the artifactId
   */
  public String getResourceType() {
    return resourceType;
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
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @return the packaging
   */
  public String getPackaging() {
    return packaging;
  }

  /**
   * Resolve the artifact and provide a File from which the artifact can
   * be loaded.
   *
   * @return returns resource File
   */
  public File getResourceFile() {
    if (resourceFile == null) {
      // We don't have a local copy of artifact yet
      resourceFile = repository.resolveResource(this);
    }
    return resourceFile;
  }

  /**
   * Resolve the artifact documentation and provide a URI from where
   * the documentation stream can be read
   *
   * @return returns documentation URI.
   */
  public URI getResourceDoc() {
    if (resourceDoc == null) {
      // We don't have a handle on the artifact doc yet
      resourceDoc = repository.resolveDocumentation(this);
    }
    return resourceDoc;
  }

  @Override
  public String toString() {
    return String.format("%s.%s-%s (%s %s) URI=%s DOC=%s", groupId, artifactId,
            version, resourceType, packaging, resourceFile.toString(),
            resourceDoc.toString());
  }

}
