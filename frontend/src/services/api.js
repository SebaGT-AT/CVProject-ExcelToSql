const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      ...(options.headers ?? {})
    }
  });

  const isJson = response.headers.get("content-type")?.includes("application/json");
  const payload = isJson ? await response.json() : await response.blob();

  if (!response.ok) {
    const message = isJson ? payload?.message ?? "Request failed" : "Request failed";
    throw new Error(message);
  }

  return isJson ? payload.data : payload;
}

export async function loginRequest(credentials) {
  return request("/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(credentials)
  });
}

export async function getDashboardSummary(token) {
  return request("/dashboard/summary", {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
}

export async function getUsers(token) {
  return request("/users", {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
}

export async function getImportJobs(token, filters = {}) {
  const searchParams = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      searchParams.append(key, value);
    }
  });

  const suffix = searchParams.toString() ? `?${searchParams.toString()}` : "";
  return request(`/import-jobs${suffix}`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
}

export async function getImportJobDetail(token, id) {
  return request(`/import-jobs/${id}`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
}

export async function uploadImportFile(token, file, entityType, importMode) {
  const formData = new FormData();
  formData.append("file", file);

  return request(
    `/import-jobs/upload?entityType=${encodeURIComponent(entityType)}&importMode=${encodeURIComponent(importMode)}`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`
      },
      body: formData
    }
  );
}

export async function downloadImportReport(token, filters, format) {
  const searchParams = new URLSearchParams();
  Object.entries({ ...filters, format }).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      searchParams.append(key, value);
    }
  });

  const response = await fetch(`${API_BASE_URL}/import-jobs/report?${searchParams.toString()}`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error("No fue posible descargar el reporte");
  }

  const blob = await response.blob();
  const disposition = response.headers.get("content-disposition") ?? "";
  const match = disposition.match(/filename=([^;]+)/i);

  return {
    blob,
    fileName: match ? match[1] : `import-report.${format.toLowerCase()}`
  };
}

