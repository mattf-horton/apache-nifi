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


/**
 * Basic Specification for an extension package file that can be resolved and delivered
 * from an external repository, then dynamically loaded as an extension in NiFi.
 * All required metadata values must be provided at instantiation time.
 * This "basic" ExtensionSpec meets the needs of Maven and File repositories, and
 * may be sufficient for all repository types since {link #locatorInfo} is delegated
 * to the ExternalRepository implementation.
 *
 * All issues regarding how to resolve and deliver the packaged extension file,
 * how to obtain and provide metadata, and how to check security signatures,
 * belong to the {@link AbstractExternalRepository} sub-classes.
 * Issues regarding how to unpackage and dynamically load the extension, are
 * consigned to core code, since it is very extension-type-specific.
 *
 * TBD: should we add member variables for signature and signing certificate?
 * Or for having passed the security signature check?
 */
public class BasicExtensionSpec {

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

  // Short hash-code or other text value that uniquely identifies a particular
  // instance of ExtensionSpec, for use in Web APIs.  The same extension from
  // two different repos should have two different signatureStrings.
  private String signatureString = null; // TBD: not yet implemented

  // Well-known nifiExtensionType values:
  public static final String PROCESSOR_TYPE = "processor";
  public static final String CONTROLLER_SERVICE_TYPE = "controllerservice";
  public static final String REPORTING_TASK_TYPE = "reportingtask";
  public static final String TEMPLATE_TYPE = "template";

  // Well-known packaging types:
  public static final String NAR_PACKAGING = "nar";
  public static final String XML_PACKAGING = "xml";

  // Used to initialize various data structures:
  protected static final int NUMBER_OF_KNOWN_EXTENSION_TYPES = 4;
  
  // XPath names for ExtensionSpec member fields
  // to assist in parsing metadata from POM or POM-like XML stream.
  protected static final String[] basicXPathNames = new String[]{
          "/groupId", "/artifactId", "/version", "/packaging", "/name",
          "/description", "/properties/nifiExtensionType",
  };


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
  public BasicExtensionSpec(String nifiExtensionType, String groupId, String artifactId,
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
  public BasicExtensionSpec(String nifiExtensionType, String groupId, String artifactId,
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
     * resolved file path in the constructed BasicExtensionSpec
     *
     * <b>WARNING:</b> Using this constructor bypasses the check for security signatures
     * since repository.resolveExtension() method will never be called.
     *
     * @throws IllegalArgumentException if any required metadata value has null
     * or empty value.
     */
  public BasicExtensionSpec(String nifiExtensionType, String groupId, String artifactId,
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
   * @return the ExternalRepository representing the repo containing this extension
   */
  public AbstractExternalRepository getRepository() { return repository; }


  /**
   * Call the ExternalRepository implementation to resolve the extension package
   * file address and provide a File from which the extension can be loaded.
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

  /**
   * Cache and return the signatureString value for this instance.
   * Probably implement via custom hashCode(), but TBD: not yet implemented.
   *
   * @return the signatureString of an instance, for use with Web APIs
   */
  public String getSignatureString() {
    if (signatureString == null) {
      // derive from final info including repo differentiator
      // TBD: not yet implemented
    }
    return signatureString;
  }


  // Utility Methods  ********************************************* //

  @Override
  public String toString() {
    return String.format("%s.%s-%s (%s %s) %s URI:%s DESCRIPTION: %s",
            groupId, artifactId, version, nifiExtensionType, packaging, name,
            (extensionPkg == null ? "UNRESOLVED" : extensionPkg.toString()),
            description);
  }


  private boolean nullOrEmpty(String s) {
    return (s == null || s.isEmpty());
  }

}
