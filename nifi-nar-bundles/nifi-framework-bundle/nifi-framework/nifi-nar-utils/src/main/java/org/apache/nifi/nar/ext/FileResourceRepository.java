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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Basic implementation of a filesystem based repository instance for NiFi remote resources.
 *
 * Note that filesystem may be local, but may also be NFS or other shared FS based.
 * Thus, repository authentication and authorization should not be presumed.
 *
 * Current proposal is to use Maven style directory structure, in which case most
 * of the logic is same as {MavenResourceRepository} class.  Alternatively, we
 * could specify a directory structure with metadata location more obviously and
 * conveniently placed.
 */
public class FileResourceRepository extends AbstractExternalRepository {


  // Constructors ****************************************************** //

  /**
   * Basic constructor for a filesystem repository instance
   *
   * @param repoId    a unique name for the repository
   * @param repoBase  base directory path for the repository
   */
  public FileResourceRepository(String repoId, String repoBase) {
    super(repoId, FILE_REPO_TYPE, repoBase);
  }

  /**
   * Basic constructor for pre-authenticated and authorized filesystem repositories
   * TBD: Provide example implementation of authentication/authorization logic.
   *
   * @param repoId    a unique name for the repository
   * @param repoBase  base directory path for the repository
   * @param authenticated the repoBase has been authenticated as a known
   *                      repository
   * @param authorized  the repository has been acknowledged as an approved
   *                    repository to obtain resources from
   */
  public FileResourceRepository(String repoId, String repoBase,
                                 boolean authenticated, boolean authorized) {
    super(repoId, FILE_REPO_TYPE, repoBase, authenticated, authorized);
  }


  // General Use Methods *********************************************** //

  @Override
  /**
   * List the set of resource types this FileResourceRepository instance offers.
   * Example contents could be "processor", "template", etc.
   *
   * If Maven directory structure is used, see {MavenResourceRepository},
   * otherwise perhaps we specify per-type subdirectories under the base directory.
   * If the adoc file can be required to provide the metadata,
   * then we could afford to scan a repository for all its highest-version
   * DOC_PACKAGEs, and extract the set of offered resource types from them.
   *
   * @return list of resource type names.  Empty list indicates info not available.
   */
  public ArrayList<String> listResourceTypes() {
    return new ArrayList<String>(0); // TBD: not yet implemented
  }

  @Override
  /**
   * List the set of resources of a particular type available from this
   * FileResourceRepository, with their metadata.
   *
   * @param resourceType  which type of resource to list.
   * @return    Map of resources of the requested type.  The map keys are either
   * artifactIds or a more human-readable variant of the artifact name for use
   * with GUI, and the values are full AbstractExtensionSpec for each artifact.
   */
  public HashMap<String, AbstractExtensionSpec> listResources(String resourceType) {
    HashMap<String, AbstractExtensionSpec> result = new HashMap<String, AbstractExtensionSpec>();
    {
      /*
          See {MavenResourceRepository} and above comments.
          If we use simple directory structure, then listing will obtain
          artifact names, and metadata can be read from .adoc files or
          inferred from Maven-like directory paths.
        */
    }
    return result;  // TBD: not yet implemented.
  }

  @Override
  /**
   * Return the URI for the adoc file, constructing from repoBase.
   * See {MavenResourceRepository} for structure of method.
   *
   * @param resourceSpec  what resource we want to get the docs for, and where to get it from
   * @return a URI from which the docs can be read
   */
  public URI resolveDocumentation(AbstractExtensionSpec resourceSpec) {
    URI result = "";
    return result;  // TBD: not yet implemented.
  }

  @Override
  /**
   * Return the File object for the resource package file.
   * See {MavenResourceRepository} for structure of method.
   *
   * Implementations of resolveResource() MUST include a validation step that
   * confirms the resolved resource file matches its signature, and the signing
   * authority is recognized and accepted.  This needs to be done in this method,
   * since it isn't automatic in a directory-based repo.
   *
   * @param resourceSpec  what resource we want to get, and where to get it from
   * @return a File, from which the resource can be loaded.  Throw exception
   * (after logging) if we fail to resolve, since by this point we should know
   * it exists and we want it.
   */
  public File resolveResource(AbstractExtensionSpec resourceSpec) {
    File result = null;
    return result;  // TBD: not yet implemented.
  }


}
