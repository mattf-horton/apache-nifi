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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * There are multiple types of repository for external resources, with
 * different mechanisms for resolving and retrieving, and presenting
 * meta-data.  But all must present these basic methods.
 *
 * TBD:  We should have a registry of known and accepted external repository
 * instances. These could be automatically registered upon instantiation.
 * However, Maven repositories are already managed through external configurations,
 * and local file-based repositories may arguably not need much management.
 */
public abstract class AbstractExternalRepository {

  protected final String repoId;    // a unique name for the repository
  protected final String repoType;  // example values could be "file", "maven"
  protected final String repoBase;  // depending on the repoType, a URI or base
                                    // structured name for the repository
  protected boolean authenticated = false;
    // set if the repoBase has been authenticated as a known repository
  protected boolean authorized = false;
    // set if the repository has been acknowledged as an approved repository
    // to obtain resources from


  // These aren't the only allowed values, but they should be well-known values
  // so define them in constants here in the parent class.
  public static final String FILE_REPO_TYPE = "file";
  public static final String MAVEN_REPO_TYPE = "maven";

  // Used in repo implementations due to need to separately resolve documentation:
  public static final String DOC_PACKAGE = AbstractExtensionSpec.DOC_PACKAGE;


  // Constructors ****************************************************** //

  /**
   * Null constructor
   */
  public AbstractExternalRepository() {
    super();
  }

  /**
   * Basic super constructor for an external repository instance
   *
   * @param repoId    a unique name for the repository
   * @param repoType  example values could be "file", "maven"
   * @param repoBase  depending on the repoType, a URI or base structured name
   *                  for the repository
   */
  public AbstractExternalRepository(String repoId, String repoType, String repoBase) {
    this.repoId = repoId;
    this.repoType = repoType;
    this.repoBase = repoBase;
  }

  /**
   * Basic super constructor for pre-authenticated and authorized repositories
   * TBD: Provide example implementation of authentication/authorization logic.
   *
   * @param repoId    a unique name for the repository
   * @param repoType  example values could be "file", "maven"
   * @param repoBase  depending on the repoType, a URI or base structured name
   *                  for the repository
   * @param authenticated the repoBase has been authenticated as a known
   *                      repository
   * @param authorized  the repository has been acknowledged as an approved
   *                    repository to obtain resources from
   */
  public AbstractExternalRepository(String repoId, String repoType, String repoBase,
                                    boolean authenticated, boolean authorized) {
    this.repoId = repoId;
    this.repoType = repoType;
    this.repoBase = repoBase;
    this.authenticated = authenticated;
    this.authorized = authorized;
  }


  // General Use Methods *********************************************** //

  /**
   * List the set of resource types this external repository offers.
   * Example contents could be "processor", "template", etc.
   *
   * Some repositories may not be able to provide this information, and will
   * return an empty list.
   *
   * @return    list of resource type names.  List may be empty, indicating
   *            information is not available.
   */
  public abstract ArrayList<String> listResourceTypes();

  /**
   * List the set of resources of a particular type available from this
   * AbstractExternalRepository, with their metadata.
   *
   * @param resourceType  which type of resource to list.  If null, indicates
   *                      request for list of ALL resources available,
   *                      regardless of type.
   * @return    Map of resources of the requested type.  The map keys are either
   * artifactIds or a more human-readable variant of the artifact name for use
   * with GUI, and the values are full AbstractExtensionSpec for each artifact.
   */
  public abstract HashMap<String, AbstractExtensionSpec> listResources(String resourceType);

  /**
   * Acquire the short-form documentation for a particular external resource.
   * This is typically for GUI purposes, so each artifact is self-documenting.
   * The short-form or preview doc should include enough info so user can tell
   * if they want to load and use the resource itself.
   *
   * A URI for a stream is preferred rather than a file, as it is assumed the
   * user has not yet decided whether to download the resource, and therefore
   * we shouldn't clutter non-cache storage with its documentation.
   *
   * If document preview is not available for a particular resource, return
   * an empty string (not null) rather than throwing an exception.
   *
   * @param resourceSpec  what resource we want to get the docs for, and where to get it from
   * @return    a URI from which the docs can be read.  Returns empty string upon failure
   * or unavailability.
   */
  public abstract URI resolveDocumentation(AbstractExtensionSpec resourceSpec);

  /**
   * Acquire a particular external resource from the remote repository, or
   * present the local file if already locally available.
   *
   * Implementations of resolveResource() MUST include a validation step that
   * confirms the resolved resource file matches its signature, and the signing
   * authority is recognized and accepted.
   *
   * @param resourceSpec  what resource we want to get, and where to get it from
   * @return    a File, from which the resource can be loaded
   */
  public abstract File resolveResource(AbstractExtensionSpec resourceSpec);


  // TBD: In UI code need generic accessors that list approved repositories,
  // their offerings, and short-form documentation for each available artifact.

}
