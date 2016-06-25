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
// TBD: shouldn't be under "nar", perhaps create org.apache.nifi.nifiExtensions
// as new module under nifi-commons ?

import java.io.File;
import java.net.URI;
import java.util.HashMap;

/**
 * Basic Specification for an extension package file that can be resolved and delivered
 * from an external repository, then dynamically loaded as an extension in NiFi.
 * All required metadata values must be provided at instantiation time.
 *
 * All issues regarding how to resolve and deliver the packaged extension file,
 * how to obtain and provide metadata, and how to check security signatures,
 * belong to the {AbstractExternalRepository} sub-classes.
 * Issues regarding how to unpackage and dynamically load the extension, belong
 * to the sub-classes of this {AbstractExtensionSpec} class.
 *
 * As part of the loading logic, each extension type will need to be added to the
 * GUI list of available extensions of that type, if such a list exists.
 * That logic is very nifiExtensionType dependent, so see the sub-classes for examples.
 *
 * TBD: should we add member variables for signature and signing certificate?
 * Or for having passed the security signature check?
 */
public abstract class AbstractExtensionSpec {

  // Required Meta-Data (obtained from repository) ******************** //
  protected final String nifiExtensionType; // Example values could be "processor", "template", etc.
  protected final String groupId;      // G of GAV
  protected final String artifactId;   // A of GAV
  protected final String version;      // V of GAV
  protected final String packaging;    // Loading an extension depends on its packaging format;
                                       // "nar" is popular for many NiFi extension types.
  protected final AbstractExternalRepository repository; // The repository presenting this extension
  protected final String locatorInfo;  // (may be optional, depending on repository sub-class) -
                                       // Additional repo-specific info the repository may need
                                       // to resolve the extension package file.  May be null.

  // Optional Meta-Data ********************************************** //
  protected final String name;         // (optional) human-readable name, defaults to value of artifactId
  protected final String description;  // (optional) brief description suitable for use in GUI,
                                       // to assist the user in selecting the extension.
                                       // May be null, but this significantly impacts usability.

  // Info from resolving the extension package file, provided by repository **************** //
  protected File extensionPkg;   // Cached result of resolving the extension package file
  protected URI  extensionPom;   // (may be optional, depending on repository sub-class) -
                                 // Cached result of resolving the extension's POM.
                                 // May be an external stream, since metadata doesnt
                                 // need to be security-signed.

  // Well-known nifiExtensionType values:
  public static final String PROCESSOR_TYPE = "processor";
  public static final String TEMPLATE_TYPE = "template";

  // These pre-defined nifiExtensionType values support extensibility of the
  // ExtensionSpec + ExternalRepository framework:
  public static final String EXTENSION_SPEC_TYPE = "ExtensionSpec";
  public static final String EXTERNAL_REPO_TYPE = "ExternalRepository";

  // Well-known packaging types:
  public static final String NAR_PACKAGING = "nar";

  // Used to initialize various data structures:
  protected static final int NUMBER_OF_KNOWN_EXTENSION_TYPES = 8;


  // Constructors ****************************************************** //

  /**
   * Constructor with basic required elements
   *
   * @param name and locatorInfo may be null.  If name is null it will default
   *             to artifactId value.
   * @param description may be null, but shouldn't be, for the sake of usability.
   *                    If null, description will default to "groupId.artifactId".
   *
   * @throws IllegalArgumentException if any required metadata value has null
   * or empty value.
   */
  public AbstractExtensionSpec(String nifiExtensionType, String groupId, String artifactId,
                               String version, String packaging, String name,
                               String description, AbstractExternalRepository repository,
                               String locatorInfo)
          throws IllegalArgumentException {
    super();
    if (nullOrEmpty(nifiExtensionType) || nullOrEmpty(groupId) || nullOrEmpty(artifactId) ||
            nullOrEmpty(version) || nullOrEmpty(packaging) || repository == null) {
      throw new IllegalArgumentException("Null or empty value for required metadata value");
    }
    if (nullOrEmpty(name)) {
      name = artifactId;
    }
    if (nullOrEmpty(description)) {
      description = groupId + "." + artifactId;
    }
    this.nifiExtensionType = nifiExtensionType;
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.packaging = packaging;
    this.name = name;
    this.description = description;
    this.repository = repository;
    this.locatorInfo = locatorInfo;
  }

  /**
   * Most ExternalRepository implementations will resolve a POM to read the metadata,
   * prior to constructing the ExtensionSpec instance.  This constructor supports
   * that usage.
   *
   * @throws IllegalArgumentException if any required metadata value has null
   * or empty value.
   */
  public AbstractExtensionSpec(String nifiExtensionType, String groupId, String artifactId,
                               String version, String packaging, String name,
                               String description, AbstractExternalRepository repository,
                               String locatorInfo, URI extensionPom)
          throws IllegalArgumentException {
    this(nifiExtensionType, groupId, artifactId, version, packaging,
            name, description, repository, locatorInfo);
    this.extensionPom = extensionPom;
  }

  /**
     * Support null implementation of File-based test repos by including the
     * resolved file path in the constructed AbstractExtensionSpec
     *
     * <b>WARNING:</b> Using this constructor bypasses the check for security signatures
     * since repository.resolveExtension() method will never be called.
     *
     * @throws IllegalArgumentException if any required metadata value has null
     * or empty value.
     */
  public AbstractExtensionSpec(String nifiExtensionType, String groupId, String artifactId,
                               String version, String packaging, String name,
                               String description, AbstractExternalRepository repository,
                               String locatorInfo, URI extensionPom, File extensionPkg)
          throws IllegalArgumentException {
    this(nifiExtensionType, groupId, artifactId, version, packaging,
            name, description, repository, locatorInfo);
    this.extensionPom = extensionPom;
    this.extensionPkg = extensionPkg;
  }


  // General Use Methods *********************************************** //

  /**
   * Each type of extension must sub-class this AbstractExtensionSpec and provide
   * a load() method appropriate to the type and packaging.  If there are multiple
   * packagings supported for the extension type, then the load() method must provide
   * multiple cases.
   * TBD: (question) Should we allow multiple sub-classes with the same
   * nifiExtensionType but different packagings?  This improves extensibility,
   * since new packagings could be added by third parties, but complicates
   * extension management.
   *
   * As part of the {load()} logic, each extension instance will need to be added
   * to the GUI list of available extensions of that type, if such a list exists.
   * That logic is very nifiExtensionType dependent, so see the sub-classes for examples.
   * Also see {@link org.apache.nifi.nar.ExtensionMapping}
   * TBD: resolve the above paragraph after design decisions are finalized.
   *
   * @return  true if successful, false if not.
   */
  public abstract boolean load();


  /**
   * @return the nifiExtensionType
   */
  public String getNifiExtensionType() {
    return nifiExtensionType;
  }

  /**
   * @return the groupId
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * @return the artifactId
   */
  public String getArtifactId() {
    return artifactId;
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
   * @return the human-readable name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the brief description for GUI use
   */
  public String getDescription() {
    return description;
  }


  /**
   * Call the ExternalRepository to resolve the extension package file address
   * and provide a File from which the extension can be loaded.
   *
   * @return returns extension package file
   */
  public File getExtensionPkg() {
    if (extensionPkg == null) {
      // We don't have a local copy of the package yet
      extensionPkg = repository.resolveExtension(this);
    }
    return extensionPkg;
  }


  // Utility Methods  ********************************************* //

  private boolean nullOrEmpty(String s) {
    return (s == null || s.isEmpty());
  }

}
