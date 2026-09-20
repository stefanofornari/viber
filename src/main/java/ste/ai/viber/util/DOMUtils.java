/*
 * Copyright 2026 the original author or authors from the LLMTooliy project
 * (https://stefanofornari.github.io/llm-toolify).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ste.ai.viber.util;

import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 *
 */
public class DOMUtils {
    public static String prettify(final Document document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            // Optional: Protect against XML External Entity (XXE) vulnerabilities
            transformerFactory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

            Transformer transformer = transformerFactory.newTransformer();

            // Set properties for pretty-printing
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://apache.org}indent-amount", "4"); // 4 spaces
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error formatting DOM Document to String", e);
        }
    }
}
