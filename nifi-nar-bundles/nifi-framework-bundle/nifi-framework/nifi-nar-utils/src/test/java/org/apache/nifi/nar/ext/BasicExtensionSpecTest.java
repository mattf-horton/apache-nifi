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

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the apis of {@link BasicExtensionSpec} while by-passing the need for a
 * repository.  Utilizes test resources dummyExtension.txt and dummyExtension.pom.
 */
public class BasicExtensionSpecTest {

  final Path resourcesPath = Paths.get("./src/test/resources/BasicExtensionSpec");
  final Path repoPath = resourcesPath.resolve("private.nifi.extensions/dummy");
  final File extensionPkg = new File(repoPath.toString(), "dummyExtension.txt");
  final URI  extensionPom = (new File(repoPath.toString(), "dummyExtension.pom")).toURI();

  @Test
  public void testDummyExtensionSpec() throws FileNotFoundException, IOException {
    DummyRepo dr = new DummyRepo();
    HashMap<String, String> pp = AbstractExternalRepository.parsePom(
            AbstractExternalRepository.getNewParseMap(), extensionPom);
    DummyExtensionSpec des = new DummyExtensionSpec(
            pp.get("/groupId"),
            pp.get("/artifactId"),
            pp.get("/version"),
            pp.get("/name"),
            pp.get("/description"),
            new DummyRepo(), null,
            extensionPom, extensionPkg
            );

    Assert.assertEquals("dummy", des.getNifiExtensionType());
    Assert.assertEquals("dummyExtension1", des.getName());
    Assert.assertEquals("private.nifi.extensions.dummy.dummyExtension1",
            des.getDescription());
    Assert.assertSame(extensionPkg, des.getExtensionPkg());
    Assert.assertTrue(des.load());
  }

  @Test
  public void testToString() throws IOException {
    DummyRepo dr = new DummyRepo();
    HashMap<String, String> pp = AbstractExternalRepository.parsePom(
            AbstractExternalRepository.getNewParseMap(), extensionPom);
    DummyExtensionSpec des = new DummyExtensionSpec(
            pp.get("/groupId"),
            pp.get("/artifactId"),
            pp.get("/version"),
            pp.get("/name"),
            pp.get("/description"),
            new DummyRepo(), null,
            extensionPom, extensionPkg
    );

    String ts = des.toString();
    Assert.assertEquals("private.nifi.extensions.dummy.dummyExtension1-1.0.1.0 (dummy txt) dummyExtension1 URI:" +
            extensionPkg.toString() + " DESCRIPTION: private.nifi.extensions.dummy.dummyExtension1", ts);
  }


  private class DummyExtensionSpec extends BasicExtensionSpec {

    private static final String TXT_PACKAGING = "txt";
    private static final String DUMMY_SPEC_TYPE = "dummy";

    /**
     * Support null implementation of File-based test repos by including the
     * resolved file path in the constructed BasicExtensionSpec
     */
    private DummyExtensionSpec(String groupId, String artifactId, String version,
                             String name, String description,
                             AbstractExternalRepository repository,
                             String locatorInfo, URI extensionPom,
                             File extensionPkg) {
      super(DUMMY_SPEC_TYPE, groupId, artifactId, version, TXT_PACKAGING,
              name, description, repository, locatorInfo, extensionPom, extensionPkg);
    }

    /**
     * Fake load() method for this dummy.  It just checks if the expected file is there.
     */
    public boolean load() throws FileNotFoundException, IOException {

      String contents = new BufferedReader(new FileReader(this.getExtensionPkg())).readLine();

      Assert.assertEquals("Cool but useless example number 1", contents);

      return true;
    }

  }

  /**
   * As close to a null implementation as we can do.
   */
  private class DummyRepo extends AbstractExternalRepository {

    public DummyRepo() {
      super("Dummy_Repo_One", "dummy", "", true, true);
    }

    @Override
    public ArrayList<String> listNifiExtensionTypes() {
      return new ArrayList<String>(Arrays.asList("dummy"));
    }

    @Override
    public HashMap<String, BasicExtensionSpec> listExtensions(String nifiExtensionType){
      HashMap<String, BasicExtensionSpec> result = new HashMap<String, BasicExtensionSpec>();
      return result;
    }

    @Override
    public File resolveExtension(BasicExtensionSpec extensionSpec) {
      File result = null;
      return result;
    }

    @Override
    public void refreshRepoListing() {
    }

  }


}
