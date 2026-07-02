function DashboardPage() {
  const metrics = [
    { label: "Importaciones hoy", value: "12" },
    { label: "Registros procesados", value: "18,420" },
    { label: "Tasa de exito", value: "96.3%" },
    { label: "Errores frecuentes", value: "Emails invalidos" }
  ];

  const recentImports = [
    { file: "clientes_junio.xlsx", status: "Completada", rows: 4200, duration: "01:12" },
    { file: "productos_catalogo.csv", status: "Parcial", rows: 12500, duration: "02:54" },
    { file: "inventario_sucursal_a.csv", status: "Fallida", rows: 3000, duration: "00:24" }
  ];

  return (
    <div className="dashboard-page">
      <header className="border-bottom bg-white">
        <div className="container py-3 d-flex justify-content-between align-items-center">
          <div>
            <h1 className="h3 mb-1">Dashboard</h1>
            <p className="text-secondary mb-0">Vista inicial del sistema de importaciones.</p>
          </div>
          <button className="btn btn-outline-primary">Nuevo usuario</button>
        </div>
      </header>

      <main className="container py-4 d-grid gap-4">
        <section className="row g-3">
          {metrics.map((metric) => (
            <div className="col-md-6 col-xl-3" key={metric.label}>
              <div className="metric-card p-3 h-100">
                <div className="text-secondary small">{metric.label}</div>
                <div className="metric-value mt-2">{metric.value}</div>
              </div>
            </div>
          ))}
        </section>

        <section className="row g-4">
          <div className="col-xl-7">
            <div className="panel p-4">
              <div className="d-flex justify-content-between align-items-center mb-3">
                <div>
                  <h2 className="h5 mb-1">Subida de archivos</h2>
                  <p className="text-secondary mb-0">Base visual para el flujo drag & drop.</p>
                </div>
                <button className="btn btn-primary">Seleccionar archivo</button>
              </div>

              <div className="upload-zone d-flex flex-column align-items-center justify-content-center text-center">
                <strong>Arrastra un CSV o XLSX aqui</strong>
                <span className="text-secondary mt-2">Productos, clientes, proveedores, empleados o inventario.</span>
              </div>

              <div className="mt-4">
                <div className="d-flex justify-content-between small mb-2">
                  <span>Progreso de importacion</span>
                  <span>64%</span>
                </div>
                <div className="progress" role="progressbar" aria-label="Import progress">
                  <div className="progress-bar" style={{ width: "64%" }} />
                </div>
              </div>
            </div>
          </div>

          <div className="col-xl-5">
            <div className="panel p-4 h-100">
              <h2 className="h5 mb-3">Ultimos archivos importados</h2>
              <div className="list-group list-group-flush">
                {recentImports.map((item) => (
                  <div className="list-group-item px-0" key={item.file}>
                    <div className="d-flex justify-content-between align-items-start gap-3">
                      <div>
                        <div className="fw-semibold">{item.file}</div>
                        <div className="text-secondary small">{item.rows.toLocaleString()} registros</div>
                      </div>
                      <div className="text-end">
                        <div className="small fw-semibold">{item.status}</div>
                        <div className="text-secondary small">{item.duration}</div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

export default DashboardPage;
