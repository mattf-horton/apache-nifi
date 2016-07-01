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
import java.util.ArrayList;
import java.util.HashMap;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Basic implementation of a Maven repository instance for NiFi extensions.
 *
 * Note about <b>resource discovery</b> and this implementation's usage of <b>repoBase</b>:
 *
 * Discovery of resources must be supported, specifically the ability to list
 * the NiFi extensions offered by a particular repository, with their metadata.
 * Maven itself doesn't seem to have this concept; before accessing a particular
 * object in maven, you already need to know where it is and that you need it.
 *
 * However, the most common commercially supported maven repository server solutions,
 * including those used by Apache and the Maven Central Repository, provide
 * web apis that can list contents of URI paths in the repository.
 * On the other hand, if a local maven repository is in use, filesystem operations
 * in the local cache directory can be used to do the same listing.
 * Both these special cases are supported.
 *
 * To assist in extension listing, the repoBase member variable is used
 * to store both a base URI and a base groupId for the Maven repository.
 * For example, the current Apache NiFi repository (not yet structured for NiFi
 * extensions) is at URI
 * "https://repository.apache.org/content/groups/public/org/apache/nifi/"
 * and has groupId "org.apache.nifi".  The value of repoBase for this repository,
 * to be used as an external extension repository, would be the comma-separated
 * concatenation of these two strings:
 * "https://repository.apache.org/content/groups/public/org/apache/nifi/,org.apache.nifi"
 * (TBD: Please note the above won't actually work due to lots of unrelated
 * resources in these directories, but we'll replace this example with a better one
 * as soon as we create the official Apache NiFi extension repository.)
 *
 * Note about <b>metadata extraction</b> and this implementation's usage of <b>POM files</b>:
 *
 * All Maven repositories require a POM file to be presented along with
 * each repository resource package.  We take advantage of this by additionally
 * requiring that the POM file for a NiFi extension package <i>must</i> provide
 * meaningful values for the following 6 elements: groupId, artifactId, version,
 * packaging, name, description.  In addition, the &lt;properties /&gt; element
 * in the POM must declare a value for a &lt;nifiExtensionType /&gt; property.
 * We parse these values from the POM to discover NiFi extensions and their metadata.
 *
 */
public class MavenExternalRepository extends AbstractExternalRepository {

  /**
   * per-repository listing of repo contents.  Init to null, fill lazily at need.
   * Top-level map is for nifiExtensionTypes, below that are name/ExtensionSpec pairs.
   * If we refresh this, we spawn a new object, thereby avoiding reentrancy
   * problems, but leaving it up to the clients to notify on refresh.
   */
  private HashMap<String, HashMap<String, BasicExtensionSpec>> internalRepoListing = null;


  // Constructors ****************************************************** //

  /**
   * Basic constructor for a Maven repository instance
   *
   * @param repoId    a unique name for the repository
   * @param repoBase  base structured name for the repository
   */
  public MavenExternalRepository(String repoId, String repoBase) {
    super(repoId, MAVEN_REPO_TYPE, repoBase);
  }

  /**
   * Basic constructor for pre-authenticated and authorized Maven repositories
   * TBD: Provide example implementation of authentication/authorization logic.
   *
   * @param repoId    a unique name for the repository
   * @param repoBase  base structured name for the repository
   * @param authenticated the repoBase has been authenticated as a known
   *                      repository
   * @param authorized  the repository has been acknowledged as an approved
   *                    repository to obtain extensions from
   */
  public MavenExternalRepository(String repoId, String repoBase,
                                 boolean authenticated, boolean authorized) {
    super(repoId, MAVEN_REPO_TYPE, repoBase, authenticated, authorized);
  }


  // General Use Methods *********************************************** //

  @Override
  /**
   * List the set of nifiExtensionTypes this MavenExternalRepository instance offers.
   * Example contents could be "processor", "template", etc.
   *
   * Supporting this with Maven seems to require scanning the whole registry,
   * then extracting the nifiExtensionTypes offered.  We could instead accept
   * a proposal to structure the Maven groupId to include an extension type
   * designation in a predictable location in the structured name.
   *
   * @return list of nifiExtensionType names.  Empty list indicates info failed to
   * load from repository.
   */
  public ArrayList<String> listNifiExtensionTypes() {
    return new ArrayList<String>(getRepoListing(false).keySet());
  }

  @Override
  /**
   * List the set of extensions of a particular type available from this
   * MavenExternalRepository, with their metadata.  Obtain info from the
   * scan of the whole registry.
   *
   * @param nifiExtensionType  which type of extension to list.  If null, indicates
   *                           request for list of ALL extensions available,
   *                           regardless of type.
   * @return    Map of extensions of the requested type.  The map keys are either
   * the human-readable extension name if available {@link BasicExtensionSpec#name}
   * or "groupId.artifactId" if not, and the values are full {@link BasicExtensionSpec}
   * for each extension.
   */
  public HashMap<String, BasicExtensionSpec> listExtensions(String nifiExtensionType) {
    getRepoListing(false);
    if (nifiExtensionType != null) {
      if (internalRepoListing.containsKey(nifiExtensionType)) {
        return new HashMap<String, BasicExtensionSpec>(internalRepoListing.get(nifiExtensionType));
      } else {
        return new HashMap<String, BasicExtensionSpec>(0);
      }
    }
    else { //nifiExtensionType is null, so concat all the extension type listings
      int count = 0;
      for (HashMap<String, BasicExtensionSpec> m : internalRepoListing.values()) {
        count += m.size();
      }
      HashMap<String, BasicExtensionSpec>  result =
              new HashMap<String, BasicExtensionSpec>(count);
      for (HashMap<String, BasicExtensionSpec> m : internalRepoListing.values()) {
        result.putAll(m);
      }
      return result;
    }
  }

  @Override
  /**
   * Acquire a particular extension package from the external repository, or
   * present the local file if already locally available.
   *
   * TBD: should this instead always re-resolve, even if the File value has already
   * been set?
   *
   * Implementations of resolveExtension() MUST include a validation step that
   * confirms the resolved extension package file matches its signature, and the
   * signing authority is recognized and accepted.  This appears to be implicit in the
   * Maven resolver, assuming the extension package files were signed before releasing.
   * TBD: check for it!  Exception handling if skipped?
   *
   * @param extensionSpec  what extension we want to get, and where to get it from
   * @return a package File, from which the extension can be loaded.  Throw exception
   * (after logging) if we fail to resolve, since by this point we should know
   * it exists and we want it.
   */
  public File resolveExtension(BasicExtensionSpec extensionSpec) {
    File result = extensionSpec.extensionPkg;
           // Note deliberate bypass of the getter method {@link extensionSpec#getExtensionPkg()},
           // which would infinite loop.
    if (result.equals(null)) {
      final String gav = extensionSpec.getGroupId() + ":" + extensionSpec.getArtifactId() +
              ":" + extensionSpec.getPackaging() + ":" + extensionSpec.getVersion();
      try {
        result = Maven.resolver().resolve(gav).withoutTransitivity().asSingleFile();
      } catch (Exception e) {
        /* TBD: log this.  What context?
           getLogger().error("Failed to maven resolve extension package file for gav: "+gav+" due to {}", e);
         */
        throw e;
      }
    }
    return result;
  }

  @Override
  public void refreshRepoListing() {
    getRepoListing(true);
  }


  // Utility Methods  ******************************************** //

  /**
   * Lazily load the repo listing from the actual external repository
   * @param reload If true will force a reload even if the current value
   *               is non-null
   * @return the resulting or cached value of internalRepoListing
   */
  private synchronized HashMap<String, HashMap<String, BasicExtensionSpec>>
          getRepoListing(boolean reload) {
    if (internalRepoListing == null || reload) {
      internalRepoListing = new HashMap<String, HashMap<String, BasicExtensionSpec>>(
              BasicExtensionSpec.NUMBER_OF_KNOWN_EXTENSION_TYPES);
      // scan the repository for all Nifi Extensions, building the hashmap as we go
      // TBD: not implemented yet.
    }
    return internalRepoListing;
  }

}
