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
// TBD: could be under "nar", but perhaps better under either
// org.apache.nifi.nifiExtensions.processor (in nifi-commons module)
// or org.apache.nifi.processor.extensions (in the nifi-api module)?


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

/**
 * The existence of this ExtensionSpec-for-ExternalRepositories allows new
 * classes of ExternalRepository to be sideloaded as extensions themselves.
 * Thus the set of ExternalRepository sub-classes is extensible without modifying
 * core code.
 *
 * Only NAR_PACKAGING is supported.
 *
 */
public class ExternalRepoExtensionSpec extends AbstractExtensionSpec {

  // Constructors ****************************************************** //

  /*
   * Constructor with basic required elements
   */
  public ExternalRepoExtensionSpec(String groupId, String artifactId, String version,
                                   String name, String description,
                                   AbstractExternalRepository repository,
                                   String locatorInfo) {
    super(EXTERNAL_REPO_TYPE, groupId, artifactId, version, NAR_PACKAGING,
            name, description, repository, locatorInfo);
  }

  /*
   * Constructor with basic required elements, plus resolved POM
   */
  public ExternalRepoExtensionSpec(String groupId, String artifactId, String version,
                                   String name, String description,
                                   AbstractExternalRepository repository,
                                   String locatorInfo, URI extensionPom) {
    super(EXTERNAL_REPO_TYPE, groupId, artifactId, version, NAR_PACKAGING,
            name, description, repository, locatorInfo, extensionPom);
  }

  /*
   * Support null implementation of File-based test repos by including the
   * resolved file path in the constructed ProcessorExtensionSpec
   */
  public ExternalRepoExtensionSpec(String groupId, String artifactId, String version,
                                   String name, String description,
                                   AbstractExternalRepository repository,
                                   String locatorInfo, URI extensionPom,
                                   File extensionPkg) {
    super(EXTERNAL_REPO_TYPE, groupId, artifactId, version, NAR_PACKAGING,
            name, description, repository, locatorInfo, extensionPom, extensionPkg);
  }


  // General Use Methods *********************************************** //

  @Override
  /**
   * load() method appropriate to ExternalRepository extensions (representing new
   * ExternalRepository types) wrapped in NAR packaging
   *
   * @return  true if successful, false if not.
   */
  public boolean load() throws FileNotFoundException, IOException {
    /* TBD: connect to jetty server (or obtain current jetty object) and invoke
     *    sideLoadNar(getExtensionPkg());
     * */
    return false; //TBD: not implemented yet
  }

}
