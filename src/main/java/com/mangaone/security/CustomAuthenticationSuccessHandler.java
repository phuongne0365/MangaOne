package com.mangaone.security;

import com.mangaone.entity.User;
import com.mangaone.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Xử lý redirect sau khi đăng nhập thành công.
 *
 * THỨ TỰ ƯU TIÊN:
 *  1. ADMIN          → /admin/dashboard (luôn luôn)
 *  2. SavedRequest   → trang bị chặn trước đó (vd: cố vào /cart)
 *  3. PREVIOUS_URL   → trang người dùng tự bấm đăng nhập từ đó
 *  4. Mặc định       → /
 */
@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    // Dùng để đọc SavedRequest từ session của Spring Security
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    public CustomAuthenticationSuccessHandler() {
        // Tắt redirect mặc định của parent class,
        // ta sẽ tự quyết định redirect URL bên dưới
        setAlwaysUseDefaultTargetUrl(false);
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        HttpSession session = request.getSession();

        // ──────────────────────────────────────────────
        // ĐẶT loggedInUser vào session để header.html hoạt động
        // (header.html kiểm tra ${session.loggedInUser})
        // ──────────────────────────────────────────────
        String email = authentication.getName(); // email là username
        User user = userRepository.findByEmail(email);
        if (user != null) {
            session.setAttribute("loggedInUser", user);
        }

        // ──────────────────────────────────────────────
        // ƯU TIÊN 1: Admin → /admin/dashboard
        // ──────────────────────────────────────────────
        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (isAdmin) {
            requestCache.removeRequest(request, response); // Xóa saved request để không bị redirect nhầm
            getRedirectStrategy().sendRedirect(request, response, "/admin/dashboard");
            return;
        }

        // ──────────────────────────────────────────────
        // ƯU TIÊN 2: SavedRequest (bị Spring Security chặn khi cố vào /cart v.v.)
        // ──────────────────────────────────────────────
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            requestCache.removeRequest(request, response);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }

        // ──────────────────────────────────────────────
        // ƯU TIÊN 3: PREVIOUS_URL (trang người dùng đang ở trước khi tự bấm đăng nhập)
        // ──────────────────────────────────────────────
        String previousUrl = (String) session.getAttribute("PREVIOUS_URL");
        if (previousUrl != null && !previousUrl.isBlank()) {
            session.removeAttribute("PREVIOUS_URL"); // Dọn dẹp sau khi dùng
            getRedirectStrategy().sendRedirect(request, response, previousUrl);
            return;
        }

        // ──────────────────────────────────────────────
        // ƯU TIÊN 4: Mặc định → Trang chủ
        // ──────────────────────────────────────────────
        getRedirectStrategy().sendRedirect(request, response, "/");
    }
}
