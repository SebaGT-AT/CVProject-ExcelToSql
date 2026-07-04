import { useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const INITIAL_FORM = {
  email: "admin@datapro.com",
  password: "Admin123!"
};

function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState(INITIAL_FORM);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setIsSubmitting(true);
    setError("");

    try {
      await login(form);
      navigate(location.state?.from ?? "/dashboard", { replace: true });
    } catch (submissionError) {
      setError(submissionError.message || "No fue posible iniciar sesion");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-login px-3 py-4">
      <div className="login-shell shadow-sm">
        <div className="row g-0 h-100">
          <div className="col-lg-5 bg-white p-4 p-lg-5 d-flex flex-column justify-content-center">
            <div className="mb-4">
              <div className="text-uppercase text-secondary small fw-semibold">Data Importer Pro</div>
              <h1 className="login-title mt-2 mb-2">Acceso seguro para operaciones de importacion</h1>
              <p className="text-secondary mb-0">
                Inicia sesion para cargar archivos, seguir el historial y descargar reportes.
              </p>
            </div>

            <form className="d-grid gap-3" onSubmit={handleSubmit}>
              <div>
                <label className="form-label">Correo electronico</label>
                <input
                  className="form-control form-control-lg"
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={handleChange}
                  autoComplete="username"
                  required
                />
              </div>

              <div>
                <label className="form-label">Contrasena</label>
                <input
                  className="form-control form-control-lg"
                  type="password"
                  name="password"
                  value={form.password}
                  onChange={handleChange}
                  autoComplete="current-password"
                  required
                />
              </div>

              {error ? <div className="alert alert-danger py-2 mb-0">{error}</div> : null}

              <button className="btn btn-primary btn-lg" type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Validando acceso..." : "Ingresar"}
              </button>
            </form>

            <div className="login-hint mt-4">
              <div className="small text-secondary">Credenciales demo</div>
              <div className="small"><code>admin@datapro.com</code> / <code>Admin123!</code></div>
              <div className="small"><code>operator@datapro.com</code> / <code>Operator123!</code></div>
            </div>
          </div>

          <div className="col-lg-7 login-hero text-white p-4 p-lg-5 d-flex flex-column justify-content-between">
            <div className="hero-chip">Portfolio Backend + Frontend</div>
            <div>
              <h2 className="hero-title mb-3">Importaciones seguras, trazables y listas para presentar en entrevistas.</h2>
              <div className="row g-3">
                <div className="col-sm-6">
                  <div className="hero-stat">
                    <div className="hero-stat-value">CSV / XLSX</div>
                    <div className="hero-stat-label">Carga operativa</div>
                  </div>
                </div>
                <div className="col-sm-6">
                  <div className="hero-stat">
                    <div className="hero-stat-value">JWT</div>
                    <div className="hero-stat-label">Seguridad real</div>
                  </div>
                </div>
                <div className="col-sm-6">
                  <div className="hero-stat">
                    <div className="hero-stat-value">Reportes</div>
                    <div className="hero-stat-label">CSV y PDF</div>
                  </div>
                </div>
                <div className="col-sm-6">
                  <div className="hero-stat">
                    <div className="hero-stat-value">Dashboard</div>
                    <div className="hero-stat-label">Metricas en vivo</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
