package com.sap.core.odata.testutil.fit;

import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.core.odata.api.ODataCallback;
import com.sap.core.odata.api.ODataService;
import com.sap.core.odata.api.ODataServiceFactory;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataContext;
import com.sap.core.odata.testutil.server.TestServer;

public class FitStaticServiceFactory extends ODataServiceFactory {

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ODataCallback> T getCallback(final Class<? extends ODataCallback> callbackInterface) {
    if (callbackInterface.isAssignableFrom(FitErrorCallback.class)) {
      return (T) new FitErrorCallback();
    }

    return super.getCallback(callbackInterface);
  }

  private static Map<String, ODataService> HOST_2_SERVICE = Collections.synchronizedMap(new HashMap<String, ODataService>());

  public static void bindService(final TestServer server, final ODataService service) {
    HOST_2_SERVICE.put(createId(server), service);
  }

  public static void unbindService(final TestServer server) {
    HOST_2_SERVICE.remove(createId(server));
  }

  @Override
  public ODataService createService(final ODataContext ctx) throws ODataException {

    assertNotNull(ctx);
    assertNotNull(ctx.getAcceptableLanguages());

    final Map<String, List<String>> requestHeaders = ctx.getRequestHeaders();
    final String host = requestHeaders.get("Host").get(0);
    // access and validation in synchronized block
    synchronized (HOST_2_SERVICE) {
      final ODataService service = HOST_2_SERVICE.get(host);
      if (service == null) {
        throw new IllegalArgumentException("no static service set for JUnit test");
      }
      return service;
    }
  }

  private static String createId(final TestServer server) {
    final URI endpoint = server.getEndpoint();
    if (endpoint == null) {
      throw new IllegalArgumentException("Got TestServer without endpoint.");
    }
    return endpoint.getHost() + ":" + endpoint.getPort();
  }
}
