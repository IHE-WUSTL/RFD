/*******************************************************************************
 * Copyright (c) 2015 Washington University in St. Louis All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. The License is available at:
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. Contributors:
 * Initial author: Ralph Moulton / MIR WUSM IHE Development Project
 * moultonr@mir.wustl.edu
 ******************************************************************************/
package edu.wustl.mir.erl.ihe.rfd.cda.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

/**
 * Helper class for xml namespace contexts for CDA Documents
 */
public class CDANamespaceContext implements Serializable, NamespaceContext {
   private static final long serialVersionUID = 1L;

   QName[] gnames;

   /**
    * Create an instance implementing {@link NamespaceContext} using the passed
    * List of {@link QName}s.
    * 
    * @param namespaces QName list.
    */
   public CDANamespaceContext(QName[] namespaces) {
      this.gnames = namespaces;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
    */
   @Override
   public String getNamespaceURI(String prefix) {
      for (QName qname : gnames) {
         if (qname.getPrefix().equals(prefix)) return qname.getNamespaceURI();
      }
      return "";
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
    */
   @Override
   public String getPrefix(String namespaceURI) {
      for (QName qname : gnames) {
         if (qname.getNamespaceURI().equals(namespaceURI))
            return qname.getPrefix();
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
    */
   @Override
   public Iterator<String> getPrefixes(String namespaceURI) {
      List <String> list = new ArrayList <>();
      for (QName qname : gnames) {
         if (qname.getNamespaceURI().equals(namespaceURI))
            list.add(qname.getPrefix());
      }
      return list.iterator();
   }

}
