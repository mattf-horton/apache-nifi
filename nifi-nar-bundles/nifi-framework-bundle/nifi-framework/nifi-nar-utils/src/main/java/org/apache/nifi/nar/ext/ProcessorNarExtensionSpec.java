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
// TBD: could be under "nar", but perhaps better under org.apache.nifi.processor.external
// in the nifi-api module?  But the dependency tree doesn't seem to work with that.


import java.io.File;
import java.net.URI;

/**
 *
 */
public class ProcessorNarExtensionSpec extends AbstractExtensionSpec {

  // Constructors ****************************************************** //

  /**
   * Null constructor
   */
  public ProcessorNarExtensionSpec() {
    super(PROCESSOR_TYPE, NAR_PACKAGING);
  }

  /*
   * Constructor with basic required elements
   */
  public ProcessorNarExtensionSpec(String groupId, String artifactId, String version,
                       AbstractExternalRepository repository) {
    super(PROCESSOR_TYPE, groupId, artifactId, version, NAR_PACKAGING, repository);
  }

  /*
   * Most AbstractExternalRepository implementations will use required elements
   * plus locatorInfo
   */
  public ProcessorNarExtensionSpec(String groupId, String artifactId, String version,
                                   AbstractExternalRepository repository, String locatorInfo) {
    super(PROCESSOR_TYPE, groupId, artifactId, version, NAR_PACKAGING,
            repository, locatorInfo);
  }

  /*
   * Support null implementation of File-based test repos by including the
   * resolved file path in the constructed ProcessorNarExtensionSpec
   */
  public ProcessorNarExtensionSpec(String groupId, String artifactId, String version,
                                   AbstractExternalRepository repository, String locatorInfo,
                                   File resourceFile) {
    super(PROCESSOR_TYPE, groupId, artifactId, version, NAR_PACKAGING,
            repository, locatorInfo, resourceFile);
  }

  /*
   * Support null implementation of File-based test repos by including the
   * resolved path in the constructed AbstractExtensionSpec, for both
   * artifact and documentation
   */
  public ProcessorNarExtensionSpec(String groupId, String artifactId, String version,
                                   AbstractExternalRepository repository, String locatorInfo,
                                   File resourceFile, URI resourceDoc) {
    super(PROCESSOR_TYPE, groupId, artifactId, version, NAR_PACKAGING,
            repository, locatorInfo, resourceFile, resourceDoc);
  }



  // General Use Methods *********************************************** //


  @Override
  /**
   * load() method appropriate to processor resources wrapped in NAR packaging
   *
   * @return  true if successful, false if not.
   */
  public boolean load(){
    /* TBD: connect to jetty server (or obtain current jetty object) and invoke
     *    sideLoadNar(getResourceFile());
     * */
    return false; //not implemented yet
  }


}
