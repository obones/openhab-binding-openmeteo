package com.obones.binding.openmeteo.internal.connection;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.RawType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class OpenMeteoHttpUtil {
    private @NonNullByDefault({}) static final Logger logger = LoggerFactory.getLogger(OpenMeteoHttpConnection.class);

    @Nullable
    private static HttpClientFactory httpClientFactory;

    public OpenMeteoHttpUtil() {
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        OpenMeteoHttpUtil.httpClientFactory = httpClientFactory;
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        OpenMeteoHttpUtil.httpClientFactory = null;
    }

    private static ContentResponse executeUrlAndGetResponse(String url, int timeout, @Nullable String proxyHost,
            @Nullable Integer proxyPort, @Nullable String proxyUser, @Nullable String proxyPassword)
            throws IOException {
        var localHttpClientFactory = httpClientFactory;
        if (localHttpClientFactory == null)
            throw new IllegalStateException("Http client factory not available");

        HttpClient httpClient = localHttpClientFactory.getCommonHttpClient();
        URI uri = null;

        try {
            uri = new URI(url);
        } catch (URISyntaxException | NullPointerException e) {
            logger.debug("String {} can not be parsed as URI reference", url);
            throw new IOException(e);
        }

        HttpProxy proxy = null;
        if (proxyHost != null && !proxyHost.isBlank() && proxyPort != null) {
            AuthenticationStore authStore = httpClient.getAuthenticationStore();
            ProxyConfiguration proxyConfig = httpClient.getProxyConfiguration();
            List<ProxyConfiguration.Proxy> proxies = proxyConfig.getProxies();
            proxy = new HttpProxy(proxyHost, proxyPort);
            proxies.add(proxy);
            authStore.addAuthentication(
                    new BasicAuthentication(proxy.getURI(), "<<ANY_REALM>>", proxyUser, proxyPassword));
        }

        HttpMethod method = HttpMethod.GET;
        Request request = httpClient.newRequest(uri).method(method).timeout(timeout, TimeUnit.MILLISECONDS);

        if (uri.getUserInfo() != null) {
            String[] userInfo = uri.getUserInfo().split(":");
            String user = userInfo[0];
            String password = userInfo[1];
            Base64.Encoder var10000 = Base64.getEncoder();
            String var10001 = user + ":" + password;
            String basicAuthentication = "Basic " + var10000.encodeToString(var10001.getBytes());
            request.header(HttpHeader.AUTHORIZATION, basicAuthentication);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("About to execute {}", request.getURI());
        }

        ContentResponse result;
        try {
            ContentResponse response = request.send();
            int statusCode = response.getStatus();
            if (logger.isDebugEnabled() && statusCode >= 400) {
                String statusLine = statusCode + " " + response.getReason();
                logger.debug("Method failed: {}", statusLine);
            }

            result = response;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (proxy != null) {
                httpClient.getProxyConfiguration().getProxies().remove(proxy);
            }

        }

        return result;
    }

    public static @Nullable RawType downloadData(String url, @Nullable String proxyHost, @Nullable Integer proxyPort,
            @Nullable String proxyUser, @Nullable String proxyPassword) {
        RawType rawData = null;
        try {
            ContentResponse response = OpenMeteoHttpUtil.executeUrlAndGetResponse(url, 5000, proxyHost, proxyPort,
                    proxyUser, proxyPassword);
            byte[] data = response.getContent();
            if (data == null) {
                data = new byte[0];
            }

            long length = (long) data.length;
            String mediaType = response.getMediaType();
            logger.debug("Media download response: status {} content length {} media type {} (URL {})",
                    new Object[] { response.getStatus(), length, mediaType, url });
            if (response.getStatus() != 200 || length == 0L) {
                logger.debug("Media download failed: unexpected return code {} (URL {})", response.getStatus(), url);
                return null;
            }

            String contentType = "application/octet-stream";

            rawData = new RawType(data, contentType);
            logger.debug("Media downloaded: size {} type {} (URL {})",
                    new Object[] { rawData.getBytes().length, rawData.getMimeType(), url });
        } catch (IOException e) {
            logger.debug("Media download failed (URL {}) : {}", url, e.getMessage());
        }

        return rawData;
    }
}
