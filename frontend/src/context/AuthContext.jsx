import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { loginRequest } from "../services/api";

const STORAGE_KEY = "data-importer-pro-auth";
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [session, setSession] = useState(null);
  const [isBootstrapping, setIsBootstrapping] = useState(true);

  useEffect(() => {
    const saved = window.localStorage.getItem(STORAGE_KEY);
    if (saved) {
      try {
        setSession(JSON.parse(saved));
      } catch {
        window.localStorage.removeItem(STORAGE_KEY);
      }
    }
    setIsBootstrapping(false);
  }, []);

  const login = async (credentials) => {
    const response = await loginRequest(credentials);
    const nextSession = {
      accessToken: response.accessToken,
      fullName: response.fullName,
      email: response.email,
      role: response.role
    };

    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(nextSession));
    setSession(nextSession);
    return nextSession;
  };

  const logout = () => {
    window.localStorage.removeItem(STORAGE_KEY);
    setSession(null);
  };

  const value = useMemo(
    () => ({
      session,
      login,
      logout,
      isBootstrapping,
      isAuthenticated: Boolean(session?.accessToken)
    }),
    [session, isBootstrapping]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}

