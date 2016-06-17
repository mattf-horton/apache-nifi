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

import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Basic implementation of a Maven repository instance for NiFi remote resources.
 *
 * Note about the <b>short-form documentation package</b> for remote resources:
 *
 * We expect to require external resource compilations to produce not only the
 * "nar" or other resource package, but also an "adoc" or other documentation
 * preview package.  This should be posted into the maven repository alongside
 * the resource package. This implementation presumes the existence of such a package.
 * If it does not exist, the routines will return empty-string (not null) values
 * rather than exceptions, but the document preview aspect of resource discovery
 * will not be fully supported in this case.
 *
 * Note about <b>resource discovery</b> and this implementation's usage of <b>repoBase</b>:
 *
 * Discovery of resources must be supported, specifically the ability to list
 * the resources offered by a particular repository, with their metadata.
 * Maven itself doesn't seem to have this concept; before accessing a particular
 * object in maven, you already need to know where it is and that you need it.
 * Apparently only its dependencies are required in the pom files.
 *
 * Perhaps with the assistance of Maven experts, we may start encoding metadata
 * in pom files in the maven repo, in a form useful for discovery.
 * Better yet, we can put the five basic elements of metadata (resource type,
 * group id, artifact id, version, and packaging) at the very beginning of
 * each resource's documentation file, and parse it from there.
 *
 * For now, Nexus is overwhelmingly the most common commercially supported
 * maven repository server, and they support opensource, and Apache uses Nexus.
 * Nexus provides web apis that can list contents of paths in the repository.
 * On the other hand, if a local test repository is in use, filesystem operations
 * in the cache directory can be used to do the same listing.  Both these special
 * cases are supported.
 *
 * This implementation also infers metadata from path information.  To assist in
 * this, the repoBase member variable is used to store both a base URI and a base
 * groupId for the NiFi remote resource repository.  For example, the current
 * Apache NiFi repository (not yet structured for remote resources) is at URI
 * "https://repository.apache.org/content/groups/public/org/apache/nifi/"
 * and has groupId "org.apache.nifi".  The value of repoBase for this repository,
 * to be used as a remote resource repository, would be
 * "https://repository.apache.org/content/groups/public/org/apache/nifi/,org.apache.nifi"
 * It must be noted that the above won't actually work due to lots of non-remote-resource
 * "noise" in these directories, but we'll replace this example with a better one
 * as soon as we create the official Apache NiFi remote resource Maven repository.
 *
 */
public class MavenResourceRepository extends AbstractExternalRepository {


  // Constructors ****************************************************** //

  /**
   * Basic constructor for a Maven repository instance
   *
   * @param repoId    a unique name for the repository
   * @param repoBase  base structured name for the repository
   */
  public MavenResourceRepository(String repoId, String repoBase) {
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
   *                    repository to obtain resources from
   */
  public MavenResourceRepository(String repoId, String repoBase,
                                    boolean authenticated, boolean authorized) {
    super(repoId, MAVEN_REPO_TYPE, repoBase, authenticated, authorized);
  }


  // General Use Methods *********************************************** //

  @Override
  /**
   * List the set of resource types this MavenResourceRepository instance offers.
   * Example contents could be "processor", "template", etc.
   *
   * Supporting this with Maven seems difficult, which will make the GUI
   * implementation difficult.  For now, unsupport by returning empty list.
   * If the "alternative implementation" for {listResources()} is accepted,
   * then we could afford to scan a repository for all its highest-version
   * DOC_PACKAGEs, and extract the set of offered resource types from them.
   *
   * @return list of resource type names.  Empty list indicates info not available.
   */
  public ArrayList<String> listResourceTypes() {
    return new ArrayList<String>(0);
  }

  @Override
  /**
   * List the set of resources of a particular type available from this
   * MavenResourceRepository, with their metadata.
   *
   * @param resourceType  which type of resource to list.  Since this repoType
   *                      doesn't seem to support {listResourceTypes()}, the
   *                      resourceType argument will probably always be null.
   * @return    Map of resources of the requested type.  The map keys are either
   * artifactIds or a more human-readable variant of the artifact name for use
   * with GUI, and the values are full AbstractExtensionSpec for each artifact.
   */
  public HashMap<String, AbstractExtensionSpec> listResources(String resourceType) {
    HashMap<String, AbstractExtensionSpec> result = new HashMap<String, AbstractExtensionSpec>();
    {
      /*
          Parse repoBase into URI and groupId.  If URI is local filesystem,
           use directory listings to obtain names, versions, and packagings;
           else assume Nexus web apis and parse resulting HTML hierarchical
           listings to obtain names, versions, and packagings.

           For each resource listed, infer resource type.  If unavailable, and
           object has suffix ".nar", assume PROCESSOR_TYPE.  For processors
           with nar packaging, instantiate a ProcessorNarExtensionSpec with:
              resourceType = PROCESSOR_TYPE;
              artifactId inferred from listing name;
              groupId = this.groupId;
              version as obtained from listing;
              packaging = NAR_PACKAGING;
              repository = this;
              locatorInfo = null;
              resourceDoc = URL of DOC_PACKAGE package (from listing);
              resourceFile = null;
           Insert ProcessorNarExtensionSpec into <i>result</i> map, using
           listing name as key.

           Alternative implementation:  Require all remotable resources to have
           a DOC_PACKAGE, and require the adocs file to have the first 5 key/value
           pairs at the start of the document.  They should be trivial to parse,
           so no inference would be necessary, other than finding all conforming
           DOC_PACKAGEs.
        */
    }
    return result;
  }

  @Override
  /**
   * Acquire the short-form documentation for a particular external resource.
   * This is typically for GUI purposes, so each artifact is self-documenting.
   * The short-form doc should include enough info so user can tell if they
   * want to load and use the resource itself.
   *
   * A URI for a stream is preferred rather than a file, as it is assumed the
   * user has not yet decided whether to download the resource, and therefore
   * we shouldn't clutter non-cache storage with its documentation.
   *
   * This method should never be called in this implementation, since we provide
   * the DOC_PACKAGE URI as part of resource discovery in {listResources()}.
   * But, if needed, the following should pull the DOC_PACKAGE file to local
   * maven cache, and return the URI.
   *
   * @param resourceSpec  what resource we want to get the docs for, and where to get it from
   * @return a URI from which the docs can be read
   */
  public URI resolveDocumentation(AbstractExtensionSpec resourceSpec) {
    URI result = resourceSpec.resourceDoc;
          // Note deliberate bypass of the getter method {resourceSpec.getResourceDoc()},
          // which would infinite loop.
    if (result.equals(null) || result.equals("")) {
      final String gav = resourceSpec.getGroupId() + ":" + resourceSpec.getArtifactId() +
              ":" + DOC_PACKAGE + ":" + resourceSpec.getVersion();
      try {
        result = Maven.resolver().resolve(gav).withoutTransitivity().asSingleFile().toURI();
      } catch (Exception e) {
        /* TBD: log this.  What context?
           getLogger().error("Failed to maven resolve DOC_PACKAGE for gav: "+gav+" due to {}", e);
         */
        result = ""; //supress exception
      } finally {
        if (result.equals(null)) {result = "";}
      }
    }
    return result;
  }

  @Override
  /**
   * Acquire a particular external resource from the remote repository, or
   * present the local file if already locally available.
   *
   * Implementations of resolveResource() MUST include a validation step that
   * confirms the resolved resource file matches its signature, and the signing
   * authority is recognized and accepted.  This appears to be implicit in the
   * Maven resolver, assuming the resource packages were signed before releasing.
   *
   * @param resourceSpec  what resource we want to get, and where to get it from
   * @return a File, from which the resource can be loaded.  Throw exception
   * (after logging) if we fail to resolve, since by this point we should know
   * it exists and we want it.
   */
  public File resolveResource(AbstractExtensionSpec resourceSpec) {
    File result = resourceSpec.resourceFile;
           // Note deliberate bypass of the getter method {resourceSpec.getResourceFile()},
           // which would infinite loop.
    if (result.equals(null)) {
      final String gav = resourceSpec.getGroupId() + ":" + resourceSpec.getArtifactId() +
              ":" + resourceSpec.getPackaging() + ":" + resourceSpec.getVersion();
      try {
        result = Maven.resolver().resolve(gav).withoutTransitivity().asSingleFile();
      } catch (Exception e) {
        /* TBD: log this.  What context?
           getLogger().error("Failed to maven resolve resource package for gav: "+gav+" due to {}", e);
         */
        throw e;
      }
    }
    return result;
  }

}
