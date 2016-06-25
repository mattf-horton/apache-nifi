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
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Basic implementation of a filesystem based repository instance for NiFi extensions.
 *
 * Note that filesystem may be local, but may also be NFS or other shared FS based.
 * Thus, repository authentication and authorization should not be presumed.
 *
 * TBD: Current proposal is to use Maven style directory structure, in which case most
 * of the logic is same as {@link MavenExternalRepository} class.  Alternatively, we
 * could specify a directory structure with metadata location more obviously and
 * conveniently placed.
 */
public class FileExternalRepository extends AbstractExternalRepository {


  // Constructors ****************************************************** //

  /**
   * Basic constructor for a filesystem repository instance
   *
   * @param repoId    a unique name for the repository
   * @param repoBase  base directory path for the repository
   */
  public FileExternalRepository(String repoId, String repoBase) {
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
   *                    repository to obtain extensions from
   */
  public FileExternalRepository(String repoId, String repoBase,
                                boolean authenticated, boolean authorized) {
    super(repoId, FILE_REPO_TYPE, repoBase, authenticated, authorized);
  }


  // General Use Methods *********************************************** //

  @Override
  /**
   * List the set of nifiExtensionTypes this FileExternalRepository instance offers.
   * Example contents could be "processor", "template", etc.
   *
   * If Maven directory structure is used, see {MavenExternalRepository},
   * otherwise perhaps we specify per-type subdirectories under the base directory.
   * Metadata will be extracted from accompanying POM files, as with MavenExternalRepository.
   *
   * @return list of nifiExtensionType names.  Empty list indicates info not available.
   */
  public ArrayList<String> listNifiExtensionTypes() {
    return new ArrayList<String>(0); // TBD: not yet implemented
  }

  @Override
  /**
   * List the set of extensions of a particular type available from this
   * FileExternalRepository, with their metadata.
   *
   * @param nifiExtensionType  which type of extension to list.
   * @return    Map of extensions of the requested type.  The map keys are either
   * the human-readable extension name if available {@link AbstractExtensionSpec:name}
   * or "groupId.artifactId" if not, and the values are full {AbstractExtensionSpec}
   * for each extension.
   */
  public HashMap<String, AbstractExtensionSpec> listExtensions(String nifiExtensionType) {
    HashMap<String, AbstractExtensionSpec> result = new HashMap<String, AbstractExtensionSpec>();
    {
      /*
          See {MavenExternalRepository} and above comments.
        */
    }
    return result;  // TBD: not yet implemented.
  }

  @Override
  /**
   * Return the File object for the extension package file.
   * See {MavenExternalRepository} for structure of method.
   *
   * Implementations of resolveExtension() MUST include a validation step that
   * confirms the resolved extension package file matches its signature, and the
   * signing authority is recognized and accepted.  This needs to be done in this
   * method, since it isn't automatic in a directory-based repo.
   *
   * @param extensionSpec  what extension we want to get, and where to get it from
   * @return a File, from which the extension can be loaded.  Throw exception
   * (after logging) if we fail to resolve, since by this point we should know
   * it exists and we want it.
   */
  public File resolveExtension(AbstractExtensionSpec extensionSpec) {
    File result = null;
    return result;  // TBD: not yet implemented.
  }

  @Override
  public void refreshRepoListing() {
    // TBD: not yet implemented
  }
}
