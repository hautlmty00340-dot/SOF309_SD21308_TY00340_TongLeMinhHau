package com.aubadminton.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.http.CacheControl;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(@NonNull HttpServletRequest request,
                    @NonNull HttpServletResponse response, @NonNull Object handler) throws IOException {
                String path = request.getRequestURI();
                String method = request.getMethod();
                if (path.startsWith("/admin/")) {
                    HttpSession siteSession = request.getSession(false);
                    if (siteSession == null || !"admin".equals(siteSession.getAttribute("role"))) {
                        response.sendRedirect("/login.html?next=" + java.net.URLEncoder.encode(path, java.nio.charset.StandardCharsets.UTF_8));
                        return false;
                    }
                    return true;
                }
                if (!path.startsWith("/api/")) return true;
                if (path.startsWith("/api/auth/") || path.equals("/api/health") || path.equals("/api/version") || path.equals("/api")) return true;
                if (path.equals("/api/courts") && method.equals("GET")) return true;
                if (path.equals("/api/settings/public") && method.equals("GET")) return true;
                if (path.matches("/api/courts/[0-9]+") && method.equals("GET")) return true;
                if (path.equals("/api/courts/suggest") && method.equals("POST")) return true;

                HttpSession session = request.getSession(false);
                if (session == null || session.getAttribute("user_id") == null) {
                    reject(response, HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập");
                    return false;
                }
                boolean isAdmin = "admin".equals(session.getAttribute("role"));
                if ((path.startsWith("/api/courts") || path.startsWith("/api/bookings/stats/") || path.startsWith("/api/settings") ||
                        path.matches("/api/bookings/[0-9]+/notes")) && !isAdmin) {
                    reject(response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền quản trị");
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/api/**", "/admin/**");
    }

    private static void reject(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        Path project = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (project.getFileName() != null && project.getFileName().toString().equalsIgnoreCase("java-backend"))
            project = project.getParent();
        String webRoot = project.toUri().toString();
        registry.addResourceHandler("/assets/**")
                .addResourceLocations(webRoot + "assets/").setCacheControl(CacheControl.noStore());
        registry.addResourceHandler("/admin/**")
                .addResourceLocations(webRoot + "admin/").setCacheControl(CacheControl.noStore());
        registry.addResourceHandler("/*.html")
                .addResourceLocations(webRoot).setCacheControl(CacheControl.noStore());
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(Paths.get(System.getProperty("user.dir"), "uploads").toUri().toString())
                .setCacheControl(CacheControl.noStore());
    }
}
