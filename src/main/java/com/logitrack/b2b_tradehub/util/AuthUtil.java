package com.logitrack.b2b_tradehub.util;

import com.logitrack.b2b_tradehub.controller.AuthController;
import com.logitrack.b2b_tradehub.entity.enums.UserRole;
import com.logitrack.b2b_tradehub.entity.User;
import com.logitrack.b2b_tradehub.exception.ForbiddenException; // Import the correct exception class
import com.logitrack.b2b_tradehub.exception.UnauthorizedException; // Import the correct exception class
import jakarta.servlet.http.HttpSession;

public class AuthUtil {

    // Simplification pour l'exemple. Dans un vrai projet, utiliser Spring Security.

    public static User getAuthenticatedUser(HttpSession session) {
        User user = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (user == null) {
            // Lever une exception qui sera gérée par @ControllerAdvice pour retourner 401
            throw new UnauthorizedException("Utilisateur non authentifié.");
        }
        return user;
    }

    public static void checkAuthenticated(HttpSession session) {
        getAuthenticatedUser(session);
    }

    public static void checkRole(HttpSession session, UserRole requiredRole) {
        User user = getAuthenticatedUser(session);
        if (user.getRole() != requiredRole) {
            // Lever une exception pour retourner 403 Forbidden
            throw new ForbiddenException("Accès refusé. Rôle requis : " + requiredRole.name());
        }
    }

    public static boolean isRole(HttpSession session, UserRole role) {
        User user = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        return user != null && user.getRole() == role;
    }

    public static void checkClientOrAdmin(HttpSession session, Long clientId) {
        User user = getAuthenticatedUser(session);
        if (user.getRole() == UserRole.ADMIN) {
            return; // L'ADMIN peut tout voir
        }
        // Si CLIENT, vérifier que l'ID demandé correspond à son propre ID client
        if (user.getRole() == UserRole.CLIENT && user.getAssociatedClientId() != null && user.getAssociatedClientId().equals(clientId)) {
            return;
        }

        // 🎯 CORRECTION: Utilisation du package du projet: com.logitrack.b2b_tradehub.exception.ForbiddenException
        throw new ForbiddenException("Accès refusé. Vous ne pouvez consulter que votre propre profil.");
    }
}