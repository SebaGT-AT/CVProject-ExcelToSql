import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import {
  downloadImportReport,
  getDashboardSummary,
  getImportJobDetail,
  getImportJobs,
  getUsers,
  uploadImportFile
} from "../services/api";

const ENTITY_OPTIONS = ["PRODUCT", "CUSTOMER", "SUPPLIER", "EMPLOYEE", "INVENTORY"];
const MODE_OPTIONS = ["PARTIAL_ALLOWED", "FULL", "CANCEL_ON_CRITICAL"];
const STATUS_OPTIONS = ["COMPLETED", "PARTIALLY_COMPLETED", "FAILED", "CANCELLED", "RECEIVED", "VALIDATING", "PROCESSING"];

const INITIAL_FILTERS = {
  page: 0,
  size: 8,
  fileName: "",
  entityType: "",
  status: "",
  initiatedByEmail: "",
  dateFrom: "",
  dateTo: ""
};

function DashboardPage() {
  const { session, logout } = useAuth();
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  const [activeView, setActiveView] = useState("overview");
  const [summary, setSummary] = useState(null);
  const [history, setHistory] = useState(null);
  const [users, setUsers] = useState([]);
  const [selectedJob, setSelectedJob] = useState(null);
  const [filters, setFilters] = useState(INITIAL_FILTERS);
  const [uploadState, setUploadState] = useState({
    file: null,
    entityType: "PRODUCT",
    importMode: "PARTIAL_ALLOWED",
    loading: false,
    progress: 0,
    message: "",
    error: ""
  });
  const [loadingState, setLoadingState] = useState({
    summary: true,
    history: true,
    users: false
  });
  const [globalError, setGlobalError] = useState("");

  const isAdmin = session?.role === "ROLE_ADMIN";
  const statCards = useMemo(() => {
    if (!summary) {
      return [];
    }
    return [
      { label: "Importaciones hoy", value: summary.importsToday },
      { label: "Registros procesados", value: summary.recordsProcessedToday },
      { label: "Tasa de exito", value: `${summary.successRate.toFixed(1)}%` },
      { label: "Duracion promedio", value: `${summary.averageDurationMs} ms` }
    ];
  }, [summary]);

  useEffect(() => {
    loadSummary();
    loadHistory(INITIAL_FILTERS);
    if (isAdmin) {
      loadUsers();
    }
  }, []);

  const loadSummary = async () => {
    setLoadingState((current) => ({ ...current, summary: true }));
    try {
      const data = await getDashboardSummary(session.accessToken);
      setSummary(data);
    } catch (error) {
      setGlobalError(error.message);
    } finally {
      setLoadingState((current) => ({ ...current, summary: false }));
    }
  };

  const loadHistory = async (nextFilters = filters) => {
    setLoadingState((current) => ({ ...current, history: true }));
    try {
      const data = await getImportJobs(session.accessToken, nextFilters);
      setHistory(data);

      if (data.content.length > 0) {
        await openJobDetail(data.content[0].id);
      } else {
        setSelectedJob(null);
      }
    } catch (error) {
      setGlobalError(error.message);
    } finally {
      setLoadingState((current) => ({ ...current, history: false }));
    }
  };

  const loadUsers = async () => {
    setLoadingState((current) => ({ ...current, users: true }));
    try {
      const data = await getUsers(session.accessToken);
      setUsers(data);
    } catch (error) {
      setGlobalError(error.message);
    } finally {
      setLoadingState((current) => ({ ...current, users: false }));
    }
  };

  const openJobDetail = async (jobId) => {
    try {
      const data = await getImportJobDetail(session.accessToken, jobId);
      setSelectedJob(data);
    } catch (error) {
      setGlobalError(error.message);
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  const handleFilterChange = (event) => {
    const { name, value } = event.target;
    setFilters((current) => ({ ...current, [name]: value }));
  };

  const applyFilters = async (event) => {
    event.preventDefault();
    const nextFilters = { ...filters, page: 0 };
    setFilters(nextFilters);
    await loadHistory(nextFilters);
  };

  const changePage = async (direction) => {
    if (!history) {
      return;
    }

    const nextPage = filters.page + direction;
    if (nextPage < 0 || nextPage >= history.totalPages) {
      return;
    }

    const nextFilters = { ...filters, page: nextPage };
    setFilters(nextFilters);
    await loadHistory(nextFilters);
  };

  const handleFileSelection = (event) => {
    const file = event.target.files?.[0] ?? null;
    setUploadState((current) => ({ ...current, file, message: "", error: "" }));
  };

  const triggerFileSelection = () => {
    fileInputRef.current?.click();
  };

  const handleUploadFieldChange = (event) => {
    const { name, value } = event.target;
    setUploadState((current) => ({ ...current, [name]: value }));
  };

  const handleDrop = (event) => {
    event.preventDefault();
    const file = event.dataTransfer.files?.[0] ?? null;
    setUploadState((current) => ({ ...current, file, message: "", error: "" }));
  };

  const submitUpload = async (event) => {
    event.preventDefault();
    if (!uploadState.file) {
      setUploadState((current) => ({ ...current, error: "Selecciona un archivo antes de importar" }));
      return;
    }

    setUploadState((current) => ({
      ...current,
      loading: true,
      progress: 20,
      message: "",
      error: ""
    }));

    try {
      const progressMarks = [45, 70, 100];
      const dataPromise = uploadImportFile(
        session.accessToken,
        uploadState.file,
        uploadState.entityType,
        uploadState.importMode
      );

      for (const mark of progressMarks.slice(0, -1)) {
        await wait(180);
        setUploadState((current) => ({ ...current, progress: mark }));
      }

      const result = await dataPromise;
      setUploadState((current) => ({
        ...current,
        loading: false,
        progress: 100,
        message: `Importacion ${result.status} para ${result.fileName}`,
        file: null
      }));

      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }

      await Promise.all([loadSummary(), loadHistory({ ...filters, page: 0 })]);
      setActiveView("history");
      setSelectedJob(result);
    } catch (error) {
      setUploadState((current) => ({
        ...current,
        loading: false,
        progress: 0,
        error: error.message || "No fue posible procesar el archivo"
      }));
    }
  };

  const handleDownloadReport = async (format) => {
    try {
      const report = await downloadImportReport(session.accessToken, filters, format);
      const url = window.URL.createObjectURL(report.blob);
      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = report.fileName;
      anchor.click();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      setGlobalError(error.message);
    }
  };

  return (
    <div className="dashboard-shell bg-app min-vh-100">
      <header className="topbar border-bottom bg-white">
        <div className="container-fluid px-4 py-3 d-flex flex-wrap justify-content-between align-items-center gap-3">
          <div>
            <div className="small text-secondary text-uppercase fw-semibold">Data Importer Pro</div>
            <h1 className="h4 mb-1">Centro operativo de importaciones</h1>
            <div className="text-secondary small">
              {session.fullName} | {session.role === "ROLE_ADMIN" ? "Administrador" : "Operador"}
            </div>
          </div>

          <div className="d-flex align-items-center gap-2">
            <button className="btn btn-outline-secondary" type="button" onClick={() => setActiveView("history")}>
              Historial
            </button>
            <button className="btn btn-outline-primary" type="button" onClick={() => setActiveView("upload")}>
              Nueva importacion
            </button>
            <button className="btn btn-dark" type="button" onClick={handleLogout}>
              Salir
            </button>
          </div>
        </div>
      </header>

      <main className="container-fluid px-4 py-4 d-grid gap-4">
        {globalError ? <div className="alert alert-danger mb-0">{globalError}</div> : null}

        <section className="row g-3">
          {statCards.map((card) => (
            <div className="col-sm-6 col-xxl-3" key={card.label}>
              <div className="surface p-3 h-100">
                <div className="small text-secondary">{card.label}</div>
                <div className="metric-number mt-2">{formatValue(card.value)}</div>
              </div>
            </div>
          ))}
        </section>

        <section className="surface p-2">
          <div className="segmented-control d-flex flex-wrap gap-2">
            {[
              ["overview", "Resumen"],
              ["upload", "Subida"],
              ["history", "Historial"],
              ["reports", "Reportes"],
              ...(isAdmin ? [["users", "Usuarios"]] : [])
            ].map(([key, label]) => (
              <button
                key={key}
                className={`btn ${activeView === key ? "btn-primary" : "btn-light"}`}
                type="button"
                onClick={() => setActiveView(key)}
              >
                {label}
              </button>
            ))}
          </div>
        </section>

        <section className="row g-4">
          <div className="col-12 col-xxl-8 d-grid gap-4">
            {(activeView === "overview" || activeView === "upload") && (
              <div className="surface p-4">
                <div className="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
                  <div>
                    <h2 className="h5 mb-1">Carga de archivos</h2>
                    <div className="text-secondary">
                      Procesa archivos CSV o XLSX con reglas por entidad y reporte inmediato.
                    </div>
                  </div>
                  <button className="btn btn-primary" type="button" onClick={triggerFileSelection}>
                    Seleccionar archivo
                  </button>
                </div>

                <form className="d-grid gap-3" onSubmit={submitUpload}>
                  <div className="row g-3">
                    <div className="col-md-4">
                      <label className="form-label">Tipo de entidad</label>
                      <select
                        className="form-select"
                        name="entityType"
                        value={uploadState.entityType}
                        onChange={handleUploadFieldChange}
                      >
                        {ENTITY_OPTIONS.map((option) => (
                          <option key={option} value={option}>
                            {option}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="col-md-4">
                      <label className="form-label">Modo de importacion</label>
                      <select
                        className="form-select"
                        name="importMode"
                        value={uploadState.importMode}
                        onChange={handleUploadFieldChange}
                      >
                        {MODE_OPTIONS.map((option) => (
                          <option key={option} value={option}>
                            {option}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="col-md-4">
                      <label className="form-label">Archivo actual</label>
                      <div className="form-control upload-file-name">
                        {uploadState.file?.name ?? "Sin archivo seleccionado"}
                      </div>
                    </div>
                  </div>

                  <input
                    ref={fileInputRef}
                    type="file"
                    accept=".csv,.xlsx"
                    className="d-none"
                    onChange={handleFileSelection}
                  />

                  <div
                    className="upload-dropzone"
                    onDragOver={(event) => event.preventDefault()}
                    onDrop={handleDrop}
                  >
                    <div className="fw-semibold">Arrastra un CSV o XLSX aqui</div>
                    <div className="text-secondary small mt-2">
                      Productos, clientes, proveedores, empleados o inventario
                    </div>
                  </div>

                  <div>
                    <div className="d-flex justify-content-between small mb-2">
                      <span>Progreso</span>
                      <span>{uploadState.progress}%</span>
                    </div>
                    <div className="progress" role="progressbar" aria-label="Upload progress">
                      <div
                        className={`progress-bar ${uploadState.loading ? "progress-bar-striped progress-bar-animated" : ""}`}
                        style={{ width: `${uploadState.progress}%` }}
                      />
                    </div>
                  </div>

                  {uploadState.error ? <div className="alert alert-danger mb-0">{uploadState.error}</div> : null}
                  {uploadState.message ? <div className="alert alert-success mb-0">{uploadState.message}</div> : null}

                  <div className="d-flex flex-wrap gap-2">
                    <button className="btn btn-primary" type="submit" disabled={uploadState.loading}>
                      {uploadState.loading ? "Procesando..." : "Procesar importacion"}
                    </button>
                    <button
                      className="btn btn-outline-secondary"
                      type="button"
                      onClick={() =>
                        setUploadState((current) => ({
                          ...current,
                          file: null,
                          progress: 0,
                          message: "",
                          error: ""
                        }))
                      }
                    >
                      Limpiar
                    </button>
                  </div>
                </form>
              </div>
            )}

            {(activeView === "overview" || activeView === "history" || activeView === "reports") && (
              <div className="surface p-4">
                <div className="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
                  <div>
                    <h2 className="h5 mb-1">Historial de importaciones</h2>
                    <div className="text-secondary">Filtros, detalle por corrida y exportacion del listado.</div>
                  </div>
                  <div className="d-flex gap-2">
                    <button className="btn btn-outline-primary" type="button" onClick={() => handleDownloadReport("CSV")}>
                      Descargar CSV
                    </button>
                    <button className="btn btn-outline-dark" type="button" onClick={() => handleDownloadReport("PDF")}>
                      Descargar PDF
                    </button>
                  </div>
                </div>

                <form className="row g-3 mb-4" onSubmit={applyFilters}>
                  <div className="col-md-4">
                    <label className="form-label">Archivo</label>
                    <input
                      className="form-control"
                      name="fileName"
                      value={filters.fileName}
                      onChange={handleFilterChange}
                      placeholder="clientes, productos..."
                    />
                  </div>
                  <div className="col-md-2">
                    <label className="form-label">Entidad</label>
                    <select className="form-select" name="entityType" value={filters.entityType} onChange={handleFilterChange}>
                      <option value="">Todas</option>
                      {ENTITY_OPTIONS.map((option) => (
                        <option key={option} value={option}>
                          {option}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="col-md-2">
                    <label className="form-label">Estado</label>
                    <select className="form-select" name="status" value={filters.status} onChange={handleFilterChange}>
                      <option value="">Todos</option>
                      {STATUS_OPTIONS.map((option) => (
                        <option key={option} value={option}>
                          {option}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Usuario</label>
                    <input
                      className="form-control"
                      name="initiatedByEmail"
                      value={filters.initiatedByEmail}
                      onChange={handleFilterChange}
                      placeholder="admin@datapro.com"
                    />
                  </div>
                  <div className="col-md-3">
                    <label className="form-label">Desde</label>
                    <input className="form-control" type="date" name="dateFrom" value={filters.dateFrom} onChange={handleFilterChange} />
                  </div>
                  <div className="col-md-3">
                    <label className="form-label">Hasta</label>
                    <input className="form-control" type="date" name="dateTo" value={filters.dateTo} onChange={handleFilterChange} />
                  </div>
                  <div className="col-md-6 d-flex align-items-end gap-2">
                    <button className="btn btn-primary" type="submit">
                      Aplicar filtros
                    </button>
                    <button
                      className="btn btn-outline-secondary"
                      type="button"
                      onClick={() => {
                        setFilters(INITIAL_FILTERS);
                        loadHistory(INITIAL_FILTERS);
                      }}
                    >
                      Limpiar
                    </button>
                  </div>
                </form>

                <div className="table-responsive">
                  <table className="table align-middle">
                    <thead>
                      <tr>
                        <th>Archivo</th>
                        <th>Entidad</th>
                        <th>Estado</th>
                        <th>Registros</th>
                        <th>Exito</th>
                        <th>Usuario</th>
                      </tr>
                    </thead>
                    <tbody>
                      {loadingState.history ? (
                        <tr>
                          <td colSpan="6" className="text-center py-4 text-secondary">
                            Cargando historial...
                          </td>
                        </tr>
                      ) : history?.content?.length ? (
                        history.content.map((job) => (
                          <tr key={job.id} className="clickable-row" onClick={() => openJobDetail(job.id)}>
                            <td>
                              <div className="fw-semibold">{job.fileName}</div>
                              <div className="small text-secondary">{formatDate(job.createdAt)}</div>
                            </td>
                            <td>{job.entityType}</td>
                            <td>
                              <span className={`status-badge status-${job.status.toLowerCase()}`}>{job.status}</span>
                            </td>
                            <td>{job.totalRecords}</td>
                            <td>{job.successRate.toFixed(1)}%</td>
                            <td>{job.initiatedByName}</td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td colSpan="6" className="text-center py-4 text-secondary">
                            No hay importaciones para esos filtros.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>

                <div className="d-flex justify-content-between align-items-center pt-2">
                  <div className="small text-secondary">
                    {history ? `Pagina ${history.page + 1} de ${Math.max(history.totalPages, 1)}` : "Sin datos"}
                  </div>
                  <div className="d-flex gap-2">
                    <button className="btn btn-outline-secondary" type="button" onClick={() => changePage(-1)}>
                      Anterior
                    </button>
                    <button className="btn btn-outline-secondary" type="button" onClick={() => changePage(1)}>
                      Siguiente
                    </button>
                  </div>
                </div>
              </div>
            )}

            {activeView === "users" && isAdmin && (
              <div className="surface p-4">
                <div className="d-flex justify-content-between align-items-start gap-3 mb-3">
                  <div>
                    <h2 className="h5 mb-1">Gestion de usuarios</h2>
                    <div className="text-secondary">Vista administrativa basica apoyada en el backend protegido.</div>
                  </div>
                </div>

                <div className="table-responsive">
                  <table className="table align-middle">
                    <thead>
                      <tr>
                        <th>Nombre</th>
                        <th>Correo</th>
                        <th>Rol</th>
                      </tr>
                    </thead>
                    <tbody>
                      {loadingState.users ? (
                        <tr>
                          <td colSpan="3" className="text-center py-4 text-secondary">
                            Cargando usuarios...
                          </td>
                        </tr>
                      ) : (
                        users.map((user) => (
                          <tr key={user.id}>
                            <td>{user.fullName}</td>
                            <td>{user.email}</td>
                            <td>{user.role}</td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>

          <div className="col-12 col-xxl-4 d-grid gap-4">
            <div className="surface p-4">
              <h2 className="h5 mb-3">Resumen visual</h2>
              {loadingState.summary ? (
                <div className="text-secondary">Actualizando metricas...</div>
              ) : summary ? (
                <div className="d-grid gap-3">
                  <MetricList title="Errores frecuentes" items={summary.topErrors} labelKey="errorType" valueKey="total" />
                  <MetricList title="Importaciones por estado" items={summary.importsByStatus} labelKey="status" valueKey="total" />
                  <MetricList title="Importaciones por entidad" items={summary.importsByEntityType} labelKey="entityType" valueKey="total" />
                </div>
              ) : (
                <div className="text-secondary">No fue posible cargar el resumen.</div>
              )}
            </div>

            <div className="surface p-4 detail-surface">
              <h2 className="h5 mb-3">Detalle de la importacion</h2>
              {selectedJob ? (
                <div className="d-grid gap-3">
                  <div>
                    <div className="fw-semibold">{selectedJob.fileName}</div>
                    <div className="small text-secondary">{selectedJob.entityType} | {selectedJob.fileType}</div>
                  </div>

                  <div className="detail-grid">
                    <DetailItem label="Estado" value={selectedJob.status} />
                    <DetailItem label="Modo" value={selectedJob.importMode} />
                    <DetailItem label="Exitosos" value={selectedJob.successfulRecords} />
                    <DetailItem label="Fallidos" value={selectedJob.failedRecords} />
                    <DetailItem label="Duracion" value={`${selectedJob.durationMs} ms`} />
                    <DetailItem label="Usuario" value={selectedJob.initiatedByEmail} />
                  </div>

                  <div>
                    <div className="small text-secondary mb-2">Errores detectados</div>
                    <div className="error-list">
                      {selectedJob.errors?.length ? (
                        selectedJob.errors.slice(0, 6).map((error) => (
                          <div key={`${error.id}-${error.rowNumber}`} className="error-row">
                            <div className="fw-semibold">Fila {error.rowNumber}</div>
                            <div className="small text-secondary">
                              {error.fieldName || "general"} | {error.errorType}
                            </div>
                            <div className="small">{error.message}</div>
                          </div>
                        ))
                      ) : (
                        <div className="text-secondary small">Sin errores registrados.</div>
                      )}
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-secondary">Selecciona una importacion para ver su detalle.</div>
              )}
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

function MetricList({ title, items, labelKey, valueKey }) {
  return (
    <div>
      <div className="small text-secondary mb-2">{title}</div>
      <div className="d-grid gap-2">
        {items?.length ? (
          items.map((item) => (
            <div key={item[labelKey]} className="metric-inline">
              <span>{item[labelKey]}</span>
              <strong>{item[valueKey]}</strong>
            </div>
          ))
        ) : (
          <div className="text-secondary small">Sin datos disponibles.</div>
        )}
      </div>
    </div>
  );
}

function DetailItem({ label, value }) {
  return (
    <div className="detail-item">
      <div className="small text-secondary">{label}</div>
      <div className="fw-semibold">{value}</div>
    </div>
  );
}

function formatDate(value) {
  if (!value) {
    return "-";
  }
  return new Intl.DateTimeFormat("es-CL", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}

function formatValue(value) {
  if (typeof value === "number") {
    return new Intl.NumberFormat("es-CL").format(value);
  }
  return value;
}

function wait(ms) {
  return new Promise((resolve) => window.setTimeout(resolve, ms));
}

export default DashboardPage;
