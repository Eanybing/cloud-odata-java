package com.sap.core.odata.core.ep.consumer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

import com.sap.core.odata.api.edm.EdmEntitySet;

public class XmlLinkConsumerTest extends AbstractConsumerTest {

  // -> http://ldcigmd.wdf.sap.corp:50055/sap/bc/odata/Managers('1')/$links/nm_Employees

  private static final String MANAGER_1_EMPLOYEES =
      "<links xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices\">" +
          "<uri>http://ldcigmd.wdf.sap.corp:50055/sap/bc/odata/Employees('1')</uri>" +
          "<uri>http://ldcigmd.wdf.sap.corp:50055/sap/bc/odata/Employees('2')</uri>" +
          "<uri>http://ldcigmd.wdf.sap.corp:50055/sap/bc/odata/Employees('3')</uri>" +
          "<uri>http://ldcigmd.wdf.sap.corp:50055/sap/bc/odata/Employees('6')</uri>" +
          "</links>";

  private static final String SINGLE_LINK = "<uri>http://ldcigmd.wdf.sap.corp:50055/sap/bc/odata/Employees('6')</uri>";

  @Test
  public void testReadLink() throws Exception {
    XmlLinkConsumer xlc = new XmlLinkConsumer();

    XMLStreamReader reader = createReaderForTest(SINGLE_LINK);
    EdmEntitySet entitySet = null;
    String link = xlc.readLink(reader, entitySet);

    assertEquals("http://ldcigmd.wdf.sap.corp:50055/sap/bc/odata/Employees('6')", link);
  }

  @Test
  public void testReadLinks() throws Exception {
    XmlLinkConsumer xlc = new XmlLinkConsumer();

    XMLStreamReader reader = createReaderForTest(MANAGER_1_EMPLOYEES);
    EdmEntitySet entitySet = null;
    List<String> links = xlc.readLinks(reader, entitySet);

    assertEquals(4, links.size());
    assertEquals("http://ldcigmd.wdf.sap.corp:50055/sap/bc/odata/Employees('1')", links.get(0));
    assertEquals("http://ldcigmd.wdf.sap.corp:50055/sap/bc/odata/Employees('2')", links.get(1));
    assertEquals("http://ldcigmd.wdf.sap.corp:50055/sap/bc/odata/Employees('3')", links.get(2));
    assertEquals("http://ldcigmd.wdf.sap.corp:50055/sap/bc/odata/Employees('6')", links.get(3));
  }
}
