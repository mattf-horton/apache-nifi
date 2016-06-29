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
import org.junit.Assert.*;
import org.junit.Test;


/**
 * Tests the apis of {@link AbstractExtensionSpec} while by-passing the need
 * for a repository.  Utilizes test resources dummyExtension.txt and
 * dummyExtension.pom.
 */
public class AbstractExtensionSpecTest {

  final Path resourcesPath = Paths.get("./src/test/resources/AbstractExtensionSpec");
  final Path repoPath = resourcesPath.resolve("private.nifi.extensions/dummy");
  final File extensionPkg = new File(repoPath.toString(), "dummyExtension.txt");
  final URI  extensionPom = (new File(repoPath.toString(), "dummyExtension.pom")).toURI();

  @Test
  public void testGetNewParseMap() {
    HashMap<String, String> pm1 = AbstractExtensionSpec.getNewParseMap();
    Assert.assertEquals("bad size of newParseMap()", 7, pm1.size());
    Assert.assertTrue("default value missing from newParseMap()", pm1.containsKey("/packaging"));
    Assert.assertNull("non-null values in newParseMap()", pm1.get("/packaging"));

    HashMap<String, String> pm2 = AbstractExtensionSpec.getNewParseMap("/foo", "/bar");
    Assert.assertEquals("bad size of newParseMap(foo, bar)", 9, pm2.size());
    Assert.assertTrue("default value missing from newParseMap(foo, bar)", pm2.containsKey("/packaging"));
    Assert.assertTrue("extra value missing from newParseMap(foo, bar)", pm2.containsKey("/foo"));
    Assert.assertNull("non-null values in newParseMap(foo, bar)", pm2.get("/foo"));
  }

  @Test
  public void testParsePom() throws IOException {
    HashMap<String, String> pp = AbstractExtensionSpec.parsePom(
            AbstractExtensionSpec.getNewParseMap("/properties/extraDummyProp"),
            extensionPom);
    Assert.assertEquals("bad size of parsePom dict", 8, pp.size());
    Assert.assertEquals("txt", pp.get("/packaging"));
    Assert.assertEquals("private.nifi.extensions.dummy", pp.get("/groupId"));
    Assert.assertEquals("dummy", pp.get("/properties/nifiExtensionType"));
    Assert.assertEquals("extra value wrong in parsePom", "mega1",
            pp.get("/properties/extraDummyProp"));
    Assert.assertNull(pp.get("/name"));
    Assert.assertNull(pp.get("/description"));
  }

  @Test
  public void testDummyExtensionSpec() throws FileNotFoundException, IOException {
    DummyRepo dr = new DummyRepo();
    HashMap<String, String> pp = AbstractExtensionSpec.parsePom(
            AbstractExtensionSpec.getNewParseMap(), extensionPom);
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
    HashMap<String, String> pp = AbstractExtensionSpec.parsePom(
            AbstractExtensionSpec.getNewParseMap(), extensionPom);
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


  private class DummyExtensionSpec extends AbstractExtensionSpec {

    private static final String TXT_PACKAGING = "txt";
    private static final String DUMMY_SPEC_TYPE = "dummy";

    /**
     * Support null implementation of File-based test repos by including the
     * resolved file path in the constructed ProcessorExtensionSpec
     */
    private DummyExtensionSpec(String groupId, String artifactId, String version,
                             String name, String description,
                             AbstractExternalRepository repository,
                             String locatorInfo, URI extensionPom,
                             File extensionPkg) {
      super(DUMMY_SPEC_TYPE, groupId, artifactId, version, TXT_PACKAGING,
              name, description, repository, locatorInfo, extensionPom, extensionPkg);
    }

    @Override
    /**
     * load() method for this dummy.  It just checks if the expected file is there.
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
    public HashMap<String, AbstractExtensionSpec> listExtensions(String nifiExtensionType){
      HashMap<String, AbstractExtensionSpec> result = new HashMap<String, AbstractExtensionSpec>();
      return result;
    }

    @Override
    public File resolveExtension(AbstractExtensionSpec extensionSpec) {
      File result = null;
      return result;
    }

    @Override
    public void refreshRepoListing() {
    }

  }


}
