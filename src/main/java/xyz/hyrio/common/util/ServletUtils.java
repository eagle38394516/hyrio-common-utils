package xyz.hyrio.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import xyz.hyrio.common.exception.internal.InternalException;
import xyz.hyrio.common.pojo.vo.CommonVo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.nio.file.Files.*;
import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.getFilenameExtension;
import static org.springframework.util.StringUtils.hasText;

public final class ServletUtils {
    private ServletUtils() {
    }

    public static String getIpAddressFromRequest(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for"); // apache2 / WAF
        if (hasText(ip)) {
            return ip;
        }

        ip = request.getHeader("X-Real-IP"); // nginx
        if (hasText(ip)) {
            return ip;
        }

        return request.getRemoteAddr(); // other
    }

    public record RequestUriAndQueryString(String uri, String query) {
        @Override
        public String toString() {
            return hasText(query) ? uri + "?" + query : uri;
        }
    }

    public static RequestUriAndQueryString getRequestUriAndQueryString(HttpServletRequest request) {
        return new RequestUriAndQueryString(request.getRequestURI(), request.getQueryString());
    }

    public static boolean isUriIn(String method, String uri, String[][] uris) {
        return hasText(method) && hasText(uri) && !isEmpty(uris) && Arrays.stream(uris).anyMatch(u -> method.equalsIgnoreCase(u[0]) && uri.matches(u[1]));
    }

    public static String concatContextPathAndUri(String contextPath, String uri) {
        contextPath = Optional.ofNullable(contextPath).map(s -> s.replaceAll("^/|/$", "")).orElse("");
        uri = Optional.ofNullable(uri).map(s -> s.replaceAll("^/|/$", "")).orElse("");
        boolean contentPathHasText = hasText(contextPath);
        boolean uriHasText = hasText(uri);
        if (contentPathHasText && uriHasText) {
            return "/" + contextPath + "/" + uri;
        } else if (contentPathHasText) {
            return "/" + contextPath;
        } else if (uriHasText) {
            return "/" + uri;
        } else {
            return "/";
        }
    }

    public static void writeToResponse(HttpServletResponse response, ObjectMapper objectMapper, CommonVo<?> commonVo) throws IOException {
        response.setStatus(commonVo.getCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try (Writer writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(commonVo));
            writer.flush();
        }
    }

    public static void writeToResponse(HttpServletResponse response, int code, String json) throws IOException {
        response.setStatus(code);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try (Writer writer = response.getWriter()) {
            writer.write(json);
            writer.flush();
        }
    }

    public static String getContentTypeByExtension(String ext) {
        return hasText(ext) ? switch (ext) {
            case "html" -> MediaType.TEXT_HTML_VALUE;
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "woff" -> "application/font-woff";
            case "woff2" -> "application/font-woff2";
            case "ttf" -> "application/font-ttf";
            case "eot" -> "application/vnd.ms-fontobject";
            default -> MediaType.TEXT_PLAIN_VALUE;
        } : MediaType.TEXT_PLAIN_VALUE;
    }

//    @RestController
//    public class StaticController {
//        @Value("${xxx.static-resources-folder}")
//        private Path staticResourcesFolder;
//
//        @GetMapping("/**")
//        public void serveStaticResource(HttpServletRequest request, HttpServletResponse response) throws IOException {
//            ServletUtils.serveStaticResource(request, response, staticResourcesFolder);
//        }
//    }

    public static void serveStaticResource(HttpServletRequest request, HttpServletResponse response, Path resourceFolder) throws IOException {
        serveStaticResource(request, response, resourceFolder, "index.html");
    }

    public static void serveStaticResource(HttpServletRequest request, HttpServletResponse response, Path resourceFolder, String indexHtmlFileName) throws IOException {
        String requestURI = request.getRequestURI();
        Path indexPath = resourceFolder.resolve(indexHtmlFileName);
        Path path;
        if (hasText(requestURI)) {
            List<String> pathParts = Arrays.stream(requestURI.split("/")).filter(StringUtils::hasText).collect(Collectors.toList());
            if (isEmpty(pathParts)) {
                path = indexPath;
            } else if (pathParts.contains("..")) {
                path = indexPath;
            } else {
                Path uriPath = resourceFolder.resolve(String.join("/", pathParts));
                if (exists(uriPath)) {
                    if (isRegularFile(uriPath)) {
                        path = uriPath;
                    } else {
                        Path index = uriPath.resolve(indexHtmlFileName);
                        if (exists(index)) {
                            path = index;
                        } else {
                            path = indexPath;
                        }
                    }
                } else {
                    path = indexPath;
                }
            }
        } else {
            path = indexPath;
        }
        String ext = getFilenameExtension(path.toString());
        if (!Files.exists(path)) {
            throw new InternalException("file not found: " + path);
        }
        response.setContentType(getContentTypeByExtension(ext));
        try (BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
             BufferedInputStream in = new BufferedInputStream(newInputStream(path))) {
            in.transferTo(out);
        }
    }
}
