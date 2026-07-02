function LoginPage() {
  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-page px-3">
      <div className="auth-shell row g-0 overflow-hidden shadow-sm">
        <div className="col-lg-6 p-5 d-flex flex-column justify-content-center bg-white">
          <span className="text-uppercase text-secondary small fw-semibold">Data Importer Pro</span>
          <h1 className="mt-2 mb-3 auth-title">Acceso seguro a la plataforma</h1>
          <p className="text-secondary mb-4">
            Autenticacion JWT, control por roles y trazabilidad de importaciones desde el primer dia.
          </p>

          <form className="d-grid gap-3">
            <div>
              <label className="form-label">Correo electrónico</label>
              <input className="form-control form-control-lg" type="email" placeholder="admin@datapro.com" />
            </div>
            <div>
              <label className="form-label">Contrasena</label>
              <input className="form-control form-control-lg" type="password" placeholder="••••••••" />
            </div>
            <button className="btn btn-primary btn-lg" type="button">Ingresar</button>
          </form>
        </div>

        <div className="col-lg-6 text-white hero-panel p-5 d-flex flex-column justify-content-between">
          <div>
            <span className="badge text-bg-light text-primary-emphasis">Portfolio Project</span>
          </div>
          <div>
            <h2 className="display-6 fw-semibold">Importaciones confiables, auditables y listas para escalar.</h2>
            <p className="mt-3 mb-0 text-white-50">
              Disenado para procesos masivos con validaciones, historial y reportes operativos.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
