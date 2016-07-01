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
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;

/**
 * This abstract class specifies the basic functionality of an ExternalRepository
 * class suitable for interfacing with repositories, of any type, that store and
 * retrieve extension packages that can be dynamically loaded into NiFi.
 *
 * All issues regarding how to resolve and deliver the extension package file,
 * how to obtain and provide metadata, and how to check security signatures,
 * belong to the sub-classes of this {@link AbstractExternalRepository}.
 * Issues regarding how to unpackage and dynamically load and register the
 * extension, belong to core code specific to each type of extension.
 *
 * Many or perhaps most ExternalRepository implementations will require that each
 * extension package have an associated POM file from which to read the metadata.
 * Therefore we provide utility methods that support extracting metadata from a
 * POM file or POM-like xml stream.  The use of these methods is optional.
 *
 * It is critical that all ExternalRepository implementations correctly support
 * security signature checking, and successfully resolve only package files that
 * have a correct signature, with a signing certificate from a source configured
 * as acceptable at the system level.  For Maven-based repositories, this
 * functionality is implicit in the "resolve" logic.  For other repository types,
 * the functionality must be explicitly provided.
 *
 * TBD:  We should have a registry of known and accepted external repository
 * instances. These could be automatically registered upon instantiation.
 * However, Maven repositories are already managed through external configurations,
 * and local file-based repositories may arguably not need much management.
 *
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
    // to obtain extensions from


  // Well-known ExternalRepository types:
  public static final String FILE_REPO_TYPE = "file";
  public static final String MAVEN_REPO_TYPE = "maven";

  // Special packaging type used to fetch associated POM files in Maven-derived
  // repositories:
  public static final String POM_PACKAGE = "pom";
  // For XPath parsing of POM files:
  public static final String POM_TOP_LEVEL_NODE_NAME = "/project";


  // Constructors ****************************************************** //

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
   *                    repository to obtain extensions from
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
   * List the set of nifiExtensionTypes this external repository offers.
   * Example contents could be "processor", "template", etc.
   *
   * Some repositories may not be able to provide this information, and will
   * return an empty list.  This will impact discoverability of extensions in the GUI.
   *
   * TBD: suggest changing the Maven naming convention (groupId) for NiFi extensions,
   * so that extensions of different types are in different branches of the name space.
   *
   * @return    list of nifiExtensionType names.  List may be empty, indicating
   *            information is not available.
   */
  public abstract ArrayList<String> listNifiExtensionTypes();

  /**
   * List the set of extensions of a particular type available from this
   * AbstractExternalRepository, with their metadata.
   *
   * @param nifiExtensionType  which type of extension to list.  If null, indicates
   *                           request for list of ALL extensions available,
   *                           regardless of type.
   * @return    Map of extensions of the requested type.  The map keys are either
   * the human-readable extension name if available {@link BasicExtensionSpec#name}
   * or "groupId.artifactId" if not, and the values are full {@link BasicExtensionSpec}
   * for each extension.
   */
  public abstract HashMap<String, BasicExtensionSpec> listExtensions(String nifiExtensionType);

  /**
   * Acquire a particular extension package from the external repository, or
   * present the local file if already locally available.
   *
   * TBD: should this instead always re-resolve, even if the File value has already
   * been set?
   *
   * Implementations of resolveExtension() MUST include a validation step that
   * confirms the resolved extension package file matches its signature, and the
   * signing authority is recognized and accepted.
   *
   * @param extensionSpec  what extension we want to get, and where to get it from
   * @return    a package File, from which the extension can be loaded
   */
  public abstract File resolveExtension(BasicExtensionSpec extensionSpec);

  /**
   * Refresh internal data structures related to the listing of extensions
   * in the repository.  Doesn't return anything, but subsequent calls to the
   * above "list" methods may return different results after than before.
   */
  public abstract void refreshRepoListing();

  /**
   * Class-level method to lookup a particular ExtensionSpec by its signatureString.
   * This is required so that Web APIs can use a purely textual representation
   * of ExtensionSpecs.  This static final method is here in the AbstractExternalRepository
   * class because ExternalRepository implementations instantiate all
   * ExtensionSpecs, and so can maintain the needed lookup facility in a
   * singleton data structure registry.
   */
  public static final BasicExtensionSpec lookupExtensionBySignature(String signature) {
    // TBD: not yet implemented
    return null;
  }


  // Utility Methods **************************************************** //

  /**
   * Many or perhaps most ExternalRepository implementations will require that each
   * extension package have an associated POM file from which to read the metadata.
   * Therefore we provide this utility method that supports extracting metadata
   * needed for ExtensionSpecs from a POM file or POM-like xml stream.  The use
   * of this method is optional.
   *
   * TBD: Should switch implementation from DOM parser to SAX.  More work needed.
   *
   * @param parseMap caller must provide a new HashMap object, with keys set to the
   *            XPath evaluation keys desired to be extracted from the POM.
   *            Keys are implicitly within a "/project" top-level node.
   *            Values will be ignored and overwritten.
   *            Callers may start with a template map such as from getNewParseMap(),
   *            or re-use an existing parseMap object (watching out to avoid
   *            re-entrancy).
   *            Relevant node names in the POM should be unique.  Multi-valued results
   *            will return whatever XPath returns.
   * @param pomStream - URI for the POM or POM-like xml stream or file to be parsed.
   *
   * @return same parseMap object, with values filled in from the POM.  Any key
   * not found in the POM will have a null value.
   */
  public static HashMap<String, String> parsePom(final HashMap<String, String> parseMap,
                                                 URI pomStream) throws IOException {
    Document doc;
    try {
      DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
      doc = dbfactory.newDocumentBuilder().parse(pomStream.toString());
    } catch (Exception e) {
      throw new IOException(e);
    }
    XPath xpath = XPathFactory.newInstance().newXPath();

    for (String key : parseMap.keySet()) {
      String result = null;
      try {
        result = (String) xpath.evaluate(POM_TOP_LEVEL_NODE_NAME +key, doc);
        // TBD: is it ok to assume the top-level node is "<project />"?
        // If not, we can probe the document for the name of the top-level node.
        // TBD: Also, is it ok to be not-NamespaceContext-aware?  We can create a
        // NamespaceResolver from the file, but I haven't been able to make it work.
        // TBD: Finally, this uses DOM model parser.  Trying to use SAX form
        // doesn't work, probably due to need for NamespaceContext.
        if (result != null && result.isEmpty()) {result = null;}
      } catch (XPathExpressionException xpe) {
        // swallow the exception after logging it
        // TBD: log the exception
        result = null;
      }
      parseMap.put(key, result);
    }
    return parseMap;
  }

  /**
   * Template HashMap for the set of required elements in a parse map to be
   * provided to {@link #parsePom}.  If no additionalNames provided, then it
   * just contains the seven mandatory metadata keys (groupId, artifactId, version,
   * packaging, name, description, and properties.nifiExtensionType).  The use
   * of this method is optional.
   *
   * TBD: does this belong in {@link BasicExtensionSpec}, so if it gets subclassed,
   * an override method can provide additional parse keys?
   *
   * @param additionalNames optional sequence of additional namestrings desired
   *                        in the template.  Format must be "XPath" format, eg
   *                        &lt;groupId /&gt; becomes "/groupId", and
   *                        properties.foo becomes "/properties/foo".
   *
   * @return new HashMap pre-loaded with the desired keys for use with parsePom().
   * All map values are nulls.
   */
  public static HashMap<String, String> getNewParseMap(String... additionalNames) {
    HashMap<String, String> map =
            new HashMap<String, String>(BasicExtensionSpec.basicXPathNames.length + additionalNames.length);

    for (String name : BasicExtensionSpec.basicXPathNames) {
      map.put(name, null);
    }
    for (String name : additionalNames) {
      map.put(name, null);
    }
    return map;
  }



  // TBD: In UI code need generic accessors that list approved repositories,
  // their offerings, and descriptions for each available extension.

}
