import { useEffect, useEffectEvent, useState } from "react";

// URL relativa — pasa por el proxy del dev server (package.json "proxy").
// Así el navegador nunca hace requests directos al puerto 8080 → sin CORS.
const API_BASE_URL = "/api";

// URL directa solo para el enlace de Swagger (abre en nueva pestaña).
const SWAGGER_DIRECT_URL = "http://localhost:8080/api";

const HERO_IMAGE = "/assets/educational-control-hero.png";
const LOGO_IMAGE = "/assets/educational-control-logo.png";

const emptyEmployee = {
  nombre: "",
  apellido: "",
  identificacion: "",
  direccion: "",
  tipo: "DOCENTE",
  salarioBaseVigente: 0,
};

const emptyDiscount = {
  tipo: "ISSS",
  porcentaje: 0,
  vigencia: new Date().toISOString().slice(0, 10),
};

const emptyPayroll = {
  idEmpleado: "",
  periodo: "",
  horasTrabajadas: 160,
  bonificacion: 0,
};

// ─── Generador de boleta PDF (abre ventana de impresión del navegador) ───────
function generarBoletaPDF(row) {
  const descuentos  = row.descuentosAplicados || [];
  const montoIsss   = descuentos.find(d => d.tipo?.toUpperCase() === "ISSS")?.montoDescuento  ?? 0;
  const montoAfp    = descuentos.find(d => d.tipo?.toUpperCase() === "AFP")?.montoDescuento   ?? 0;
  const montoRenta  = descuentos.find(d => d.tipo?.toUpperCase() === "RENTA")?.montoDescuento ?? 0;
  const horasOrd    = Math.min(row.horasTrabajadas, 160);
  const horasExt    = Math.max(0, row.horasTrabajadas - 160);

  const fmt = (n) => `$${Number(n).toFixed(2)}`;
  const fecha = new Date().toLocaleDateString("es-SV", { year:"numeric", month:"long", day:"numeric" });

  const html = `<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8"/>
<title>Boleta de Pago — ${row.nombreEmpleado} — ${row.periodo}</title>
<style>
  @import url('https://fonts.googleapis.com/css2?family=Barlow:wght@400;500;600;700&display=swap');
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: 'Barlow', Arial, sans-serif; font-size: 13px; color: #1a1f36; background: #fff; padding: 40px; }
  .header { display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 3px solid #0a2540; padding-bottom: 16px; margin-bottom: 24px; }
  .brand h1 { font-size: 20px; font-weight: 700; color: #0a2540; }
  .brand p  { font-size: 11px; color: #697386; margin-top: 2px; }
  .boleta-title { text-align: right; }
  .boleta-title h2 { font-size: 16px; font-weight: 700; color: #0570de; }
  .boleta-title p  { font-size: 11px; color: #697386; margin-top: 2px; }
  .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px 24px; background: #f6f9fc; border-radius: 8px; padding: 16px; margin-bottom: 20px; }
  .info-row { display: flex; flex-direction: column; }
  .info-label { font-size: 10px; font-weight: 600; color: #697386; text-transform: uppercase; letter-spacing: .5px; }
  .info-value { font-size: 13px; font-weight: 600; color: #1a1f36; margin-top: 2px; }
  table { width: 100%; border-collapse: collapse; margin-bottom: 16px; }
  th { background: #0a2540; color: #fff; font-size: 11px; font-weight: 600; padding: 8px 12px; text-align: left; }
  td { padding: 7px 12px; border-bottom: 1px solid #e3e8ef; font-size: 12.5px; }
  tr:last-child td { border-bottom: none; }
  .amount { text-align: right; font-weight: 600; }
  .deduction { color: #df1b41; }
  .section-title { font-size: 12px; font-weight: 700; color: #0a2540; margin: 18px 0 6px; text-transform: uppercase; letter-spacing: .4px; }
  .neto-box { background: #0a2540; color: #fff; border-radius: 8px; padding: 14px 20px; display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
  .neto-box span:first-child { font-size: 14px; font-weight: 600; }
  .neto-box span:last-child  { font-size: 22px; font-weight: 700; }
  .footer { margin-top: 28px; border-top: 1px solid #e3e8ef; padding-top: 12px; display: flex; justify-content: space-between; font-size: 10px; color: #8898aa; }
  .legal { font-size: 9.5px; color: #8898aa; margin-top: 10px; line-height: 1.5; }
  @media print {
    body { padding: 20px; }
    .no-print { display: none !important; }
  }
</style>
</head>
<body>
<div class="header">
  <div class="brand">
    <h1>Educational Control</h1>
    <p>Sistema de Planillas Institucional · El Salvador</p>
  </div>
  <div class="boleta-title">
    <h2>BOLETA DE PAGO</h2>
    <p>Período: ${row.periodo} · Emitida: ${fecha}</p>
  </div>
</div>

<div class="info-grid">
  <div class="info-row"><span class="info-label">Empleado</span><span class="info-value">${row.nombreEmpleado}</span></div>
  <div class="info-row"><span class="info-label">Período</span><span class="info-value">${row.periodo}</span></div>
  <div class="info-row"><span class="info-label">Horas ordinarias (≤ 160 h)</span><span class="info-value">${horasOrd} h</span></div>
  <div class="info-row"><span class="info-label">Horas extraordinarias (Art. 168 CT)</span><span class="info-value">${horasExt > 0 ? horasExt + " h" : "—"}</span></div>
</div>

<p class="section-title">Ingresos</p>
<table>
  <thead><tr><th>Concepto</th><th style="text-align:right">Monto</th></tr></thead>
  <tbody>
    <tr><td>Salario base por horas ordinarias (${horasOrd} h)</td><td class="amount">${fmt(row.salarioBruto - row.bonificacion - (horasExt > 0 ? montoAfp : 0))}</td></tr>
    ${horasExt > 0 ? `<tr><td>Recargo horas extraordinarias — 100 % (Art. 168 CT)</td><td class="amount" style="color:#0570de">+${fmt(row.salarioBruto - (row.salarioBruto / (1 + 0)) )}</td></tr>` : ""}
    ${row.bonificacion > 0 ? `<tr><td>Bonificación adicional</td><td class="amount">+${fmt(row.bonificacion)}</td></tr>` : ""}
    <tr style="background:#f6f9fc"><td style="font-weight:700">Salario bruto</td><td class="amount" style="font-weight:700">${fmt(row.salarioBruto)}</td></tr>
  </tbody>
</table>

<p class="section-title">Deducciones de ley</p>
<table>
  <thead><tr><th>Descuento</th><th>Base legal</th><th style="text-align:right">Monto</th></tr></thead>
  <tbody>
    <tr><td>ISSS — Seguro Social</td><td>3 % s/ salario, tope $1 000 (Ley del ISSS, Art. 29)</td><td class="amount deduction">−${fmt(montoIsss)}</td></tr>
    <tr><td>AFP — Fondo de Pensiones</td><td>7.25 % s/ salario, sin tope (Ley SAP)</td><td class="amount deduction">−${fmt(montoAfp)}</td></tr>
    <tr><td>ISR — Impuesto sobre la Renta</td><td>Tabla DGII mensual (Cód. Tributario Art. 156)</td><td class="amount deduction">−${fmt(montoRenta)}</td></tr>
    <tr style="background:#f6f9fc"><td colspan="2" style="font-weight:700">Total deducciones</td><td class="amount deduction" style="font-weight:700">−${fmt(row.totalDescuentos)}</td></tr>
  </tbody>
</table>

<div class="neto-box">
  <span>Salario neto a pagar</span>
  <span>${fmt(row.salarioNeto)}</span>
</div>

<div class="legal">
  Cálculo aplicado bajo el Código de Trabajo de la República de El Salvador (CT) y legislación fiscal vigente.
  Art. 161 CT: jornada ordinaria 8 h/día, 44 h/semana. Art. 168 CT: horas extras al doble de tarifa ordinaria.
  Ley del ISSS Art. 29: cuota empleado 3 %, base máxima $1 000/mes. Ley SAP: AFP empleado 7.25 %.
  Código Tributario Art. 156: retención ISR mensual según tabla DGII.
</div>

<div class="footer">
  <span>Educational Control · Sistema de Planillas Institucional</span>
  <span>Documento generado el ${fecha}</span>
</div>

<script>window.onload = function(){ window.print(); }</script>
</body>
</html>`;

  const win = window.open("", "_blank", "width=800,height=900");
  win.document.write(html);
  win.document.close();
}
// ──────────────────────────────────────────────────────────────────────────

// ─── Calculadora en tiempo real (replica la lógica del backend) ────────────
function calcularPreviewPlanilla(salarioBase, horasTrabajadas, bonificacion) {
  const base   = Number(salarioBase)    || 0;
  const horas  = Number(horasTrabajadas) || 0;
  const bono   = Number(bonificacion)   || 0;
  const tarifa = base / 160;

  const horasOrd = Math.min(horas, 160);
  const horasExt = Math.max(0, horas - 160);
  const salOrd   = tarifa * horasOrd;
  const salExt   = tarifa * 2 * horasExt;
  const bruto    = salOrd + salExt + bono;

  // ISSS: 3 % tope $1 000
  const isss = Math.min(bruto, 1000) * 0.03;
  // AFP: 7.25 % sin tope
  const afp  = bruto * 0.0725;
  // ISR: tabla DGII mensual (base = bruto - isss - afp)
  const baseRenta = bruto - isss - afp;
  let renta = 0;
  if (baseRenta > 2038.10) {
    renta = (baseRenta - 2038.10) * 0.30 + 288.57;
  } else if (baseRenta > 895.24) {
    renta = (baseRenta - 895.24) * 0.20 + 60.00;
  } else if (baseRenta > 472.00) {
    renta = (baseRenta - 472.00) * 0.10 + 17.67;
  }

  const totalDesc = isss + afp + renta;
  const neto      = bruto - totalDesc;

  return {
    tarifa: round2(tarifa),
    horasOrd,
    horasExt,
    salOrd:  round2(salOrd),
    salExt:  round2(salExt),
    bruto:   round2(bruto),
    isss:    round2(isss),
    afp:     round2(afp),
    renta:   round2(renta),
    total:   round2(totalDesc),
    neto:    round2(neto),
  };
}
function round2(n) { return Math.round(n * 100) / 100; }
// ──────────────────────────────────────────────────────────────────────────

function App() {
  const [token, setToken] = useState(() => localStorage.getItem("planilla.token") || "");
  const [profile, setProfile] = useState(null);
  const [activeSection, setActiveSection] = useState("resumen");
  const [banner, setBanner] = useState(null);
  const [loading, setLoading] = useState(false);
  const [payrollProcessing, setPayrollProcessing] = useState(false);

  const [loginForm, setLoginForm] = useState({ usuario: "", contrasena: "" });
  const [employeeForm, setEmployeeForm] = useState(emptyEmployee);
  const [discountForm, setDiscountForm] = useState(emptyDiscount);
  const [payrollForm, setPayrollForm] = useState(emptyPayroll);

  const [editingEmployeeId, setEditingEmployeeId] = useState(null);
  const [editingDiscountId, setEditingDiscountId] = useState(null);

  const [employees, setEmployees] = useState([]);
  const [discounts, setDiscounts] = useState([]);
  const [employeePayrolls, setEmployeePayrolls] = useState([]);
  const [myPayrolls, setMyPayrolls] = useState([]);
  const [periodReport, setPeriodReport] = useState(null);
  const [dashboard, setDashboard] = useState(null);

  const roles = profile?.roles || [];
  const canManage = roles.includes("ROLE_ADMIN") || roles.includes("ROLE_RRHH");
  const isEmployee = roles.includes("ROLE_EMPLEADO");
  const sectionTitle =
    {
      resumen: canManage ? "Dashboard ejecutivo" : "Centro de control",
      empleados: "Gestión de talento",
      descuentos: "Motor de deducciones",
      planillas: "Operación de planillas",
      reportes: isEmployee ? "Mis boletas" : "Reportes ejecutivos",
    }[activeSection] || "Centro de control";

  const hydrateSession = useEffectEvent(async () => {
    if (!token) {
      return;
    }

    try {
      setLoading(true);
      const me = await apiRequest("/auth/me");
      setProfile(me);
      if (me.roles.includes("ROLE_ADMIN") || me.roles.includes("ROLE_RRHH")) {
        await Promise.all([loadEmployees(), loadDiscounts()]);
        loadDashboard().catch(() => {});   // no bloquea el login
      }
      if (me.roles.includes("ROLE_EMPLEADO")) {
        await loadMyPayrolls();
      }
    } catch (error) {
      clearSession();
      showBanner(error.message, "error");
    } finally {
      setLoading(false);
    }
  });

  useEffect(() => {
    if (!token) {
      return;
    }

    const run = async () => {
      try {
        await hydrateSession();
      } catch {
        // La gestión de errores ya se realiza dentro del effect event.
      }
    };

    run();
  }, [token, hydrateSession]);

  useEffect(() => {
    if (!canManage || employees.length === 0 || payrollForm.idEmpleado) {
      return;
    }

    setPayrollForm((current) => ({
      ...current,
      idEmpleado: String(employees[0].id_empleado),
    }));
  }, [canManage, employees, payrollForm.idEmpleado]);

  async function apiRequest(path, options = {}) {
    const headers = {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    };

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    let response;
    try {
      response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers,
      });
    } catch {
      throw new Error(`No fue posible conectar con la API en ${API_BASE_URL}. Verifica que el backend Spring Boot esté ejecutándose.`);
    }

    if (!response.ok) {
      const contentType = response.headers.get("content-type") || "";
      if (contentType.includes("application/json")) {
        const errorBody = await response.json();
        // Si hay errores por campo, mostrarlos uno a uno en vez del mensaje genérico
        if (errorBody.validations && Object.keys(errorBody.validations).length > 0) {
          const details = Object.entries(errorBody.validations)
            .map(([field, msg]) => `${msg}`)
            .join(" · ");
          throw new Error(details);
        }
        throw new Error(errorBody.message || errorBody.error || "Ocurrió un error inesperado");
      }
      throw new Error(await response.text());
    }

    if (response.status === 204) {
      return null;
    }

    return response.json();
  }

  function showBanner(message, tone = "success") {
    setBanner({ message, tone });
    // Auto-dismiss: errores a los 6 s, éxitos a los 4 s
    const delay = tone === "error" ? 6000 : 4000;
    setTimeout(() => setBanner(null), delay);
  }

  function clearSession() {
    localStorage.removeItem("planilla.token");
    setToken("");
    setProfile(null);
    setEmployees([]);
    setDiscounts([]);
    setEmployeePayrolls([]);
    setMyPayrolls([]);
    setPeriodReport(null);
    setDashboard(null);
    setActiveSection("resumen");
  }

  async function handleLogin(event) {
    event.preventDefault();
    setBanner(null); // limpiar error anterior antes de intentar
    try {
      setLoading(true);
      const session = await apiRequest("/auth/login", {
        method: "POST",
        body: JSON.stringify(loginForm),
      });
      localStorage.setItem("planilla.token", session.token);
      setToken(session.token);
      showBanner(`Bienvenido, ${session.nombreCompleto || session.usuario}.`);
    } catch (error) {
      showBanner(error.message, "error");
    } finally {
      setLoading(false);
    }
  }

  async function loadEmployees() {
    const data = await apiRequest("/empleados");
    setEmployees(data);
    return data;
  }

  async function loadDiscounts() {
    const data = await apiRequest("/descuentos");
    setDiscounts(data);
    return data;
  }

  async function loadMyPayrolls() {
    const data = await apiRequest("/reportes/mis-boletas");
    setMyPayrolls(data);
    return data;
  }

  async function handleEmployeeSubmit(event) {
    event.preventDefault();
    const path = editingEmployeeId ? `/empleados/${editingEmployeeId}` : "/empleados";
    const method = editingEmployeeId ? "PUT" : "POST";

    try {
      setLoading(true);
      await apiRequest(path, {
        method,
        body: JSON.stringify({
          ...employeeForm,
          salarioBaseVigente: Number(employeeForm.salarioBaseVigente),
        }),
      });
      setEmployeeForm(emptyEmployee);
      setEditingEmployeeId(null);
      await loadEmployees();
      showBanner(editingEmployeeId ? "Empleado actualizado correctamente." : "Empleado creado correctamente.");
    } catch (error) {
      showBanner(error.message, "error");
    } finally {
      setLoading(false);
    }
  }

  async function handleDiscountSubmit(event) {
    event.preventDefault();
    const path = editingDiscountId ? `/descuentos/${editingDiscountId}` : "/descuentos";
    const method = editingDiscountId ? "PUT" : "POST";

    try {
      setLoading(true);
      await apiRequest(path, {
        method,
        body: JSON.stringify({
          ...discountForm,
          porcentaje: Number(discountForm.porcentaje),
        }),
      });
      setDiscountForm(emptyDiscount);
      setEditingDiscountId(null);
      await loadDiscounts();
      showBanner(editingDiscountId ? "Descuento actualizado." : "Descuento creado.");
    } catch (error) {
      showBanner(error.message, "error");
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(path, onComplete, successMessage) {
    try {
      setLoading(true);
      await apiRequest(path, { method: "DELETE" });
      await onComplete();
      showBanner(successMessage);
    } catch (error) {
      showBanner(error.message, "error");
    } finally {
      setLoading(false);
    }
  }

  async function handleProcessPayroll(event) {
    event.preventDefault();
    try {
      setPayrollProcessing(true);   // estado propio: no comparte con otras ops
      const response = await apiRequest("/planillas/procesar", {
        method: "POST",
        body: JSON.stringify({
          idEmpleado: Number(payrollForm.idEmpleado),
          periodo: payrollForm.periodo,
          horasTrabajadas: Number(payrollForm.horasTrabajadas),
          bonificacion: 0,   // sin bonificación — el cálculo es solo por horas
        }),
      });
      showBanner(`✅ Planilla procesada — ${response.nombreEmpleado} · Neto: $${response.salarioNeto.toFixed(2)}`);
      // Carga el historial en segundo plano sin bloquear el botón
      loadEmployeePayrolls(payrollForm.idEmpleado).catch(() => {});
    } catch (error) {
      showBanner(error.message, "error");
    } finally {
      setPayrollProcessing(false);   // siempre libera el botón, pase lo que pase
    }
  }

  async function loadEmployeePayrolls(idEmpleado) {
    if (!idEmpleado) {
      return;
    }
    try {
      setLoading(true);
      const data = await apiRequest(`/planillas/empleado/${idEmpleado}`);
      setEmployeePayrolls(data);
      setActiveSection("reportes");
    } catch (error) {
      showBanner(error.message, "error");
    } finally {
      setLoading(false);
    }
  }

  async function loadDashboard() {
    try {
      const data = await apiRequest("/reportes/dashboard");
      setDashboard(data);
    } catch {
      // silencioso — el dashboard es un complemento, no bloquea flujo
    }
  }

  async function loadPeriodReport(periodo) {
    if (!periodo || !String(periodo).trim()) {
      showBanner("Escribe un período válido, por ejemplo: 05-2026", "error");
      return;
    }
    try {
      setLoading(true);
      const data = await apiRequest(`/reportes/periodo/${String(periodo).trim()}`);
      setPeriodReport(data);
      setActiveSection("reportes");
    } catch (error) {
      showBanner(error.message, "error");
    } finally {
      setLoading(false);
    }
  }

  function selectEmployeeForEdit(employee) {
    setEditingEmployeeId(employee.id_empleado);
    setEmployeeForm({
      nombre: employee.nombre,
      apellido: employee.apellido,
      identificacion: employee.identificacion,
      direccion: employee.direccion || "",
      tipo: employee.tipo,
      salarioBaseVigente: employee.salarioBaseVigente,
    });
    setActiveSection("empleados");
  }

  function selectDiscountForEdit(discount) {
    setEditingDiscountId(discount.id_descuento);
    setDiscountForm({
      tipo: discount.tipo,
      porcentaje: discount.porcentaje,
      vigencia: discount.vigencia,
    });
    setActiveSection("descuentos");
  }

  function renderPayrollRows(rows) {
    if (!rows.length) {
      return <p className="empty">Aún no hay registros disponibles.</p>;
    }

    return (
      <div className="table-card">
        <table>
          <thead>
            <tr>
              <th>Empleado</th>
              <th>Período</th>
              <th title="Horas ordinarias (≤ 160 h)">H. Ord.</th>
              <th title="Horas extraordinarias — recargo 100 % (Art. 168 CT)">H. Extra</th>
              <th>Salario bruto</th>
              <th title="ISSS empleado: 3 % s/ salario, tope $1 000 (Ley del ISSS Art. 29)">ISSS</th>
              <th title="AFP empleado: 7.25 % s/ salario, sin tope (Ley SAP)">AFP</th>
              <th title="Retención ISR mensual según tabla DGII (Cód. Tributario Art. 156)">ISR</th>
              <th>Salario neto</th>
              <th>Boleta</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => {
              const horasOrd = Math.min(row.horasTrabajadas, 160);
              const horasExt = Math.max(0, row.horasTrabajadas - 160);
              const descuentos = row.descuentosAplicados || [];
              const montoIsss  = descuentos.find(d => d.tipo?.toUpperCase() === "ISSS")?.montoDescuento ?? 0;
              const montoAfp   = descuentos.find(d => d.tipo?.toUpperCase() === "AFP")?.montoDescuento ?? 0;
              const montoRenta = descuentos.find(d => d.tipo?.toUpperCase() === "RENTA")?.montoDescuento ?? 0;
              return (
                <tr key={row.id_planilla}>
                  <td>{row.nombreEmpleado}</td>
                  <td>{row.periodo}</td>
                  <td>{horasOrd}</td>
                  <td>
                    {horasExt > 0
                      ? <strong style={{ color: "var(--accent)" }}>{horasExt} h</strong>
                      : <span style={{ color: "var(--light-muted)" }}>—</span>}
                  </td>
                  <td>${Number(row.salarioBruto).toFixed(2)}</td>
                  <td style={{ color: "var(--danger)" }}>−${montoIsss.toFixed(2)}</td>
                  <td style={{ color: "var(--danger)" }}>−${montoAfp.toFixed(2)}</td>
                  <td style={{ color: "var(--danger)" }}>−${montoRenta.toFixed(2)}</td>
                  <td><strong style={{ color: "var(--success)" }}>${Number(row.salarioNeto).toFixed(2)}</strong></td>
                  <td>
                    <button
                      className="ghost-button"
                      style={{ fontSize: "0.75rem", padding: "4px 10px" }}
                      onClick={() => generarBoletaPDF(row)}
                      title="Descargar boleta en PDF"
                    >
                      📄 PDF
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    );
  }

  if (!token || !profile) {
    return (
      <main className="login-shell">
        <section className="hero-panel">
          <div className="brand-lockup">
            <img className="brand-wordmark" src={LOGO_IMAGE} alt="Logo de Educational Control" />
          </div>
          <p className="eyebrow">Gestion educativa de planillas</p>
          <h1>Planillas institucionales con una imagen moderna, clara y profesional.</h1>
          <p className="lead">
            Educational Control es una plataforma generica para colegios, universidades, academias y redes educativas
            que necesitan administrar personal docente y administrativo, procesar periodos de pago y consultar boletas
            con una experiencia confiable y escalable.
          </p>
          <div className="hero-highlights">
            <span>Colegios</span>
            <span>Universidades</span>
            <span>Academias</span>
            <span>Instituciones multisede</span>
          </div>
          <div className="benefit-grid">
            <article className="benefit-card">
              <strong>Gestion de talento educativo</strong>
              <span>Controla docentes, administrativos y estructura salarial desde un solo entorno.</span>
            </article>
            <article className="benefit-card">
              <strong>Procesamiento por periodo</strong>
              <span>Organiza horas, bonificaciones, deducciones y boletas con una logica lista para operar.</span>
            </article>
            <article className="benefit-card">
              <strong>Experiencia replicable</strong>
              <span>Un producto adaptable para distintas instituciones sin perder claridad visual ni orden operativo.</span>
            </article>
          </div>
          <div className="credential-card">
            <strong>Cuentas demo disponibles</strong>
            <span>Administrador: <code>admin</code></span>
            <span>Recursos humanos: <code>rrhh</code></span>
            <span>Empleado: <code>empleado</code></span>
            <p className="muted">Contacte al administrador del sistema para obtener su contraseña.</p>
          </div>
        </section>
        <section className="login-panel">
          <div className="login-stack">
            <article className="panel hero-visual">
              <div className="hero-image-frame">
                <img src={HERO_IMAGE} alt="Vista comercial del sistema Educational Control" />
              </div>
              <div className="hero-stat-grid">
                <div>
                  <strong>Personal docente y administrativo</strong>
                  <span>Flujo claro para la operacion diaria institucional</span>
                </div>
                <div>
                  <strong>Boletas y reportes</strong>
                  <span>Consulta por periodo y seguimiento ordenado</span>
                </div>
                <div>
                  <strong>Diseño listo para vender</strong>
                  <span>Una experiencia profesional pensada para producto real</span>
                </div>
              </div>
            </article>

            <form className="panel login-card" onSubmit={handleLogin}>
              <h2>Acceso institucional</h2>
              <p className="muted">Ingresa para gestionar personal, procesar planillas y consultar boletas dentro de una operacion educativa ordenada.</p>
              <label>
                Usuario
                <input
                  value={loginForm.usuario}
                  onChange={(event) => setLoginForm({ ...loginForm, usuario: event.target.value })}
                  placeholder="rrhh"
                />
              </label>
              <label>
                Contraseña
                <input
                  type="password"
                  value={loginForm.contrasena}
                  onChange={(event) => setLoginForm({ ...loginForm, contrasena: event.target.value })}
                  placeholder="Tu contraseña"
                />
              </label>
              <button className="primary-button" type="submit" disabled={loading}>
                {loading ? "Validando..." : "Entrar al sistema"}
              </button>
              {banner && (
                <p className={`banner ${banner.tone}`} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 8 }}>
                  <span>{banner.message}</span>
                  <button onClick={() => setBanner(null)} style={{ background: "none", border: "none", cursor: "pointer", fontSize: "1rem", opacity: 0.7, padding: "0 4px", color: "inherit" }}>✕</button>
                </p>
              )}
            </form>
          </div>
        </section>
      </main>
    );
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div>
          <img className="sidebar-logo" src={LOGO_IMAGE} alt="Educational Control" />
          <p className="eyebrow">Plataforma de gestion educativa</p>
          <h2>{profile.nombreCompleto}</h2>
          <p className="muted">{roles.join(" · ")}</p>
        </div>
        <nav className="nav-grid">
          <button className={activeSection === "resumen" ? "nav-active" : ""} onClick={() => setActiveSection("resumen")}>Resumen</button>
          {canManage && <button className={activeSection === "empleados" ? "nav-active" : ""} onClick={() => setActiveSection("empleados")}>Empleados</button>}
          {canManage && <button className={activeSection === "descuentos" ? "nav-active" : ""} onClick={() => setActiveSection("descuentos")}>Descuentos</button>}
          {canManage && <button className={activeSection === "planillas" ? "nav-active" : ""} onClick={() => setActiveSection("planillas")}>Procesar planilla</button>}
          <button className={activeSection === "reportes" ? "nav-active" : ""} onClick={() => setActiveSection("reportes")}>{isEmployee ? "Mis boletas" : "Reportes"}</button>
          <a href={`${SWAGGER_DIRECT_URL}/swagger-ui/index.html`} target="_blank" rel="noreferrer">
            Swagger API
          </a>
        </nav>
        <button className="ghost-button" onClick={clearSession}>
          Cerrar sesión
        </button>
      </aside>

      <main className="content">
        <header className="topbar">
          <div>
            <p className="eyebrow">Sistema de Planillas Educational Control</p>
            <h1>{sectionTitle}</h1>
          </div>
          <div className="badge-group">
            <span className="status-badge">Operacion educativa</span>
            <span className="status-badge">Gestion institucional</span>
            <span className="status-badge">Plataforma escalable</span>
          </div>
        </header>

        {banner && (
          <div className={`banner ${banner.tone}`} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 12 }}>
            <span>{banner.message}</span>
            <button onClick={() => setBanner(null)} style={{ background: "none", border: "none", cursor: "pointer", fontSize: "1rem", opacity: 0.7, padding: "0 4px", color: "inherit" }}>✕</button>
          </div>
        )}

        {activeSection === "resumen" && !canManage && (
          <section className="grid two-up">
            <article className="panel stat-panel">
              <h3>Tu cuenta está activa</h3>
              <p>Accede a tus boletas de pago desde la sección <strong>Mis boletas</strong> en el menú lateral.</p>
            </article>
            <article className="panel stat-panel">
              <h3>Descargas disponibles</h3>
              <p>Cada boleta puede descargarse en PDF con todos los detalles de ley incluidos.</p>
            </article>
          </section>
        )}

        {activeSection === "resumen" && canManage && (() => {
          // ── helpers de visualización ──────────────────────────────────
          const kpi = (label, value, sub, color) => (
            <article className="panel" style={{ borderTop: `3px solid ${color}`, padding: "20px 22px" }}>
              <p style={{ fontSize: "0.72rem", fontWeight: 700, color: "var(--muted)", textTransform: "uppercase", letterSpacing: ".6px", margin: 0 }}>{label}</p>
              <p style={{ fontSize: "2rem", fontWeight: 800, color, margin: "6px 0 2px", lineHeight: 1 }}>{value}</p>
              {sub && <p style={{ fontSize: "0.75rem", color: "var(--muted)", margin: 0 }}>{sub}</p>}
            </article>
          );

          const maxNeto = dashboard?.resumenPorPeriodo?.length
            ? Math.max(...dashboard.resumenPorPeriodo.map(p => p.totalSalarioNeto))
            : 1;

          const fmt = n => `$${Number(n).toFixed(2)}`;

          return (
            <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>

              {/* ── KPI row ─────────────────────────────────────────────── */}
              <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))", gap: 14 }}>
                {kpi("Empleados activos",   employees.length,                                         `${employees.filter(e => e.tipo === "DOCENTE").length} docentes · ${employees.filter(e => e.tipo === "ADMINISTRATIVO").length} admin`, "#0570de")}
                {kpi("Planillas procesadas", dashboard?.totalPlanillas ?? "—",                         "histórico acumulado", "#0a2540")}
                {kpi("Neto total pagado",    dashboard ? fmt(dashboard.totalSalarioNeto) : "—",        dashboard ? `Bruto ${fmt(dashboard.totalSalarioBruto)}` : "cargando...", "#05a27e")}
                {kpi("Períodos activos",     dashboard?.periodosActivos ?? "—",                        dashboard ? `Descuentos ${fmt(dashboard.totalDescuentos)}` : "cargando...", "#7b5ea7")}
              </div>

              {/* ── segunda fila: barras por período + actividad reciente ─ */}
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>

                {/* gráfico de barras por período */}
                <article className="panel" style={{ padding: "20px 22px" }}>
                  <h3 style={{ marginBottom: 16, fontSize: "0.9rem" }}>Neto pagado por período</h3>
                  {dashboard?.resumenPorPeriodo?.length ? (
                    <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
                      {dashboard.resumenPorPeriodo.map(p => (
                        <div key={p.periodo}>
                          <div style={{ display: "flex", justifyContent: "space-between", fontSize: "0.8rem", marginBottom: 5 }}>
                            <span style={{ fontWeight: 600, color: "var(--ink)" }}>{p.periodo}</span>
                            <span style={{ color: "var(--muted)" }}>{p.cantidadPlanillas} planilla{p.cantidadPlanillas !== 1 ? "s" : ""} · <strong style={{ color: "#05a27e" }}>{fmt(p.totalSalarioNeto)}</strong></span>
                          </div>
                          <div style={{ height: 8, background: "var(--line)", borderRadius: 99, overflow: "hidden" }}>
                            <div style={{
                              height: "100%",
                              width: `${Math.round((p.totalSalarioNeto / maxNeto) * 100)}%`,
                              background: "linear-gradient(90deg, #0570de, #05a27e)",
                              borderRadius: 99,
                              transition: "width .6s ease",
                            }} />
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="empty">Aún no hay planillas procesadas.</p>
                  )}
                  <button
                    className="ghost-button"
                    style={{ marginTop: 16, fontSize: "0.78rem" }}
                    onClick={() => loadDashboard()}
                  >↺ Actualizar</button>
                </article>

                {/* actividad reciente */}
                <article className="panel" style={{ padding: "20px 22px" }}>
                  <h3 style={{ marginBottom: 12, fontSize: "0.9rem" }}>Actividad reciente</h3>
                  {dashboard?.ultimasPlanillas?.length ? (
                    <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                      {dashboard.ultimasPlanillas.map(p => (
                        <div key={p.id_planilla} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "8px 0", borderBottom: "1px solid var(--line)", fontSize: "0.82rem" }}>
                          <div>
                            <strong style={{ color: "var(--ink)" }}>{p.nombreEmpleado}</strong>
                            <span style={{ color: "var(--muted)", marginLeft: 8 }}>{p.periodo}</span>
                          </div>
                          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                            <strong style={{ color: "#05a27e" }}>{fmt(p.salarioNeto)}</strong>
                            <button
                              className="ghost-button"
                              style={{ fontSize: "0.7rem", padding: "2px 7px" }}
                              onClick={() => generarBoletaPDF(p)}
                              title="PDF"
                            >📄</button>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="empty">Sin actividad reciente.</p>
                  )}
                </article>
              </div>

              {/* ── plantilla de empleados ──────────────────────────────── */}
              <article className="panel" style={{ padding: "20px 22px" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 14 }}>
                  <h3 style={{ fontSize: "0.9rem", margin: 0 }}>Plantilla · {employees.length} empleados</h3>
                  <button className="ghost-button" style={{ fontSize: "0.78rem" }} onClick={() => setActiveSection("empleados")}>Gestionar →</button>
                </div>
                <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))", gap: 10 }}>
                  {employees.map(emp => (
                    <div key={emp.id_empleado} style={{ background: "var(--surface)", border: "1px solid var(--line)", borderRadius: "var(--radius)", padding: "12px 14px" }}>
                      <strong style={{ fontSize: "0.85rem", color: "var(--ink)" }}>{emp.nombreCompleto}</strong>
                      <div style={{ display: "flex", justifyContent: "space-between", marginTop: 4, fontSize: "0.75rem", color: "var(--muted)" }}>
                        <span style={{ background: emp.tipo === "DOCENTE" ? "#e8f4fd" : "#fdf0e8", color: emp.tipo === "DOCENTE" ? "#0570de" : "#c25b0a", borderRadius: 99, padding: "1px 8px", fontWeight: 600 }}>{emp.tipo}</span>
                        <span style={{ fontWeight: 700, color: "var(--ink)" }}>${Number(emp.salarioBaseVigente).toFixed(2)}<span style={{ fontWeight: 400, color: "var(--muted)" }}>/mes</span></span>
                      </div>
                    </div>
                  ))}
                </div>
              </article>

            </div>
          );
        })()}

        {canManage && activeSection === "empleados" && (
          <section className="grid management-layout">
            <form className="panel" onSubmit={handleEmployeeSubmit}>
              <h3>{editingEmployeeId ? "Editar empleado" : "Nuevo empleado"}</h3>
              <div className="field-grid">
                <label>
                  Nombre
                  <input value={employeeForm.nombre} onChange={(event) => setEmployeeForm({ ...employeeForm, nombre: event.target.value })} />
                </label>
                <label>
                  Apellido
                  <input value={employeeForm.apellido} onChange={(event) => setEmployeeForm({ ...employeeForm, apellido: event.target.value })} />
                </label>
                <label>
                  DUI <span style={{fontWeight:400, color:"var(--muted)", fontSize:"0.8rem"}}>(formato: 00000000-0)</span>
                  <input
                    value={employeeForm.identificacion}
                    onChange={(event) => setEmployeeForm({ ...employeeForm, identificacion: event.target.value })}
                    placeholder="00000000-0"
                    maxLength={10}
                  />
                </label>
                <label>
                  Tipo
                  <select value={employeeForm.tipo} onChange={(event) => setEmployeeForm({ ...employeeForm, tipo: event.target.value })}>
                    <option value="DOCENTE">DOCENTE</option>
                    <option value="ADMINISTRATIVO">ADMINISTRATIVO</option>
                  </select>
                </label>
                <label className="field-span">
                  Dirección
                  <input value={employeeForm.direccion} onChange={(event) => setEmployeeForm({ ...employeeForm, direccion: event.target.value })} />
                </label>
                <label>
                  Salario base mensual
                  <span style={{ fontWeight: 400, color: "var(--muted)", fontSize: "0.78rem" }}> (para 160 h ordinarias)</span>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={employeeForm.salarioBaseVigente}
                    onChange={(event) => setEmployeeForm({ ...employeeForm, salarioBaseVigente: event.target.value })}
                  />
                  {Number(employeeForm.salarioBaseVigente) > 0 && (
                    <span style={{ fontSize: "0.78rem", color: "var(--accent)", marginTop: 4, display: "block" }}>
                      Tarifa/hora resultante: <strong>${(Number(employeeForm.salarioBaseVigente) / 160).toFixed(4)}</strong> · horas extras al doble: <strong>${(Number(employeeForm.salarioBaseVigente) / 160 * 2).toFixed(4)}</strong>
                    </span>
                  )}
                </label>
              </div>
              <div className="action-row">
                <button className="primary-button" type="submit">{editingEmployeeId ? "Guardar cambios" : "Crear empleado"}</button>
                <button className="ghost-button" type="button" onClick={() => { setEmployeeForm(emptyEmployee); setEditingEmployeeId(null); }}>
                  Limpiar
                </button>
              </div>
            </form>

            <section className="panel">
              <div className="section-header">
                <h3>Plantilla registrada</h3>
                <button className="ghost-button" onClick={loadEmployees}>Actualizar</button>
              </div>
              {employees.length === 0 ? (
                <p className="empty">No hay empleados registrados.</p>
              ) : (
                <div className="list-stack">
                  {employees.map((employee) => (
                    <article className="list-card" key={employee.id_empleado}>
                      <div>
                        <strong>{employee.nombreCompleto}</strong>
                        <p>{employee.tipo} · DUI {employee.identificacion}</p>
                        <span style={{ display: "flex", gap: 12, flexWrap: "wrap", alignItems: "center" }}>
                          <span>Base mensual: <strong>${Number(employee.salarioBaseVigente).toFixed(2)}</strong></span>
                          <span style={{ color: "var(--accent)", fontSize: "0.82rem" }}>
                            Tarifa/hora: <strong>${(Number(employee.salarioBaseVigente) / 160).toFixed(4)}</strong>
                          </span>
                        </span>
                      </div>
                      <div className="action-row compact">
                        <button className="ghost-button" onClick={() => selectEmployeeForEdit(employee)}>Editar</button>
                        <button className="ghost-button" onClick={() => loadEmployeePayrolls(employee.id_empleado)}>Boletas</button>
                        <button className="danger-button" onClick={() => handleDelete(`/empleados/${employee.id_empleado}`, loadEmployees, "Empleado eliminado.")}>Eliminar</button>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </section>
          </section>
        )}

        {canManage && activeSection === "descuentos" && (
          <section className="grid management-layout">
            <form className="panel" onSubmit={handleDiscountSubmit}>
              <h3>{editingDiscountId ? "Editar descuento" : "Nuevo descuento"}</h3>
              <div className="field-grid">
                <label>
                  Tipo
                  <input value={discountForm.tipo} onChange={(event) => setDiscountForm({ ...discountForm, tipo: event.target.value })} />
                </label>
                <label>
                  Porcentaje
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={discountForm.porcentaje}
                    onChange={(event) => setDiscountForm({ ...discountForm, porcentaje: event.target.value })}
                  />
                </label>
                <label>
                  Vigencia
                  <input type="date" value={discountForm.vigencia} onChange={(event) => setDiscountForm({ ...discountForm, vigencia: event.target.value })} />
                </label>
              </div>
              <div className="action-row">
                <button className="primary-button" type="submit">{editingDiscountId ? "Guardar cambios" : "Registrar descuento"}</button>
                <button className="ghost-button" type="button" onClick={() => { setDiscountForm(emptyDiscount); setEditingDiscountId(null); }}>
                  Limpiar
                </button>
              </div>
            </form>

            <section className="panel">
              <div className="section-header">
                <h3>Catálogo vigente</h3>
                <button className="ghost-button" onClick={loadDiscounts}>Actualizar</button>
              </div>
              <div className="list-stack">
                {discounts.map((discount) => (
                  <article className="list-card" key={discount.id_descuento}>
                    <div>
                      <strong>{discount.tipo}</strong>
                      <p>{discount.vigencia}</p>
                      <span>{Number(discount.porcentaje).toFixed(2)}%</span>
                    </div>
                    <div className="action-row compact">
                      <button className="ghost-button" onClick={() => selectDiscountForEdit(discount)}>Editar</button>
                      <button className="danger-button" onClick={() => handleDelete(`/descuentos/${discount.id_descuento}`, loadDiscounts, "Descuento eliminado.")}>Eliminar</button>
                    </div>
                  </article>
                ))}
              </div>
            </section>
          </section>
        )}

        {canManage && activeSection === "planillas" && (() => {
          const selectedEmp = employees.find(e => String(e.id_empleado) === String(payrollForm.idEmpleado));
          const preview = selectedEmp
            ? calcularPreviewPlanilla(selectedEmp.salarioBaseVigente, payrollForm.horasTrabajadas, payrollForm.bonificacion)
            : null;

          const rowStyle = { display: "flex", justifyContent: "space-between", alignItems: "center", padding: "5px 0", borderBottom: "1px solid var(--line)", fontSize: "0.83rem" };
          const labelStyle = { color: "var(--muted)" };
          const valueStyle = { fontWeight: 600, color: "var(--ink)" };
          const dangerStyle = { fontWeight: 600, color: "var(--danger)" };
          const successStyle = { fontWeight: 700, color: "var(--success)", fontSize: "1rem" };

          return (
            <section className="grid two-up">
              <form className="panel" onSubmit={handleProcessPayroll}>
                <h3>Procesar nueva planilla</h3>

                {/* Chip de tarifa/hora del empleado seleccionado */}
                {selectedEmp && (
                  <div style={{ background: "var(--accent-soft)", border: "1px solid var(--line)", borderRadius: "var(--radius)", padding: "8px 12px", marginBottom: 12, fontSize: "0.82rem", display: "flex", gap: 16, flexWrap: "wrap" }}>
                    <span style={{ color: "var(--muted)" }}>Empleado seleccionado:</span>
                    <strong style={{ color: "var(--ink)" }}>{selectedEmp.nombreCompleto}</strong>
                    <span style={{ color: "var(--muted)" }}>Salario base:</span>
                    <strong>${Number(selectedEmp.salarioBaseVigente).toFixed(2)}</strong>
                    <span style={{ color: "var(--muted)" }}>Tarifa/hora:</span>
                    <strong style={{ color: "var(--accent)" }}>${preview?.tarifa.toFixed(4)}</strong>
                    <span style={{ color: "var(--muted)" }}>H. extra:</span>
                    <strong style={{ color: "var(--accent)" }}>${preview ? (preview.tarifa * 2).toFixed(4) : "—"}</strong>
                  </div>
                )}

                <div className="field-grid">
                  <label>
                    Empleado
                    <select value={payrollForm.idEmpleado} onChange={(event) => setPayrollForm({ ...payrollForm, idEmpleado: event.target.value })}>
                      {employees.map((employee) => (
                        <option key={employee.id_empleado} value={employee.id_empleado}>
                          {employee.nombreCompleto}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    Período (MM-YYYY)
                    <input value={payrollForm.periodo} onChange={(event) => setPayrollForm({ ...payrollForm, periodo: event.target.value })} placeholder="05-2026" />
                  </label>
                  <label className="field-span">
                    Horas trabajadas
                    <input
                      type="number"
                      min="1"
                      max="744"
                      step="0.5"
                      value={payrollForm.horasTrabajadas}
                      onChange={(event) => setPayrollForm({ ...payrollForm, horasTrabajadas: event.target.value })}
                    />
                    <span style={{ fontSize: "0.76rem", color: "var(--muted)", marginTop: 4, display: "block" }}>
                      Ordinarias: hasta 160 h · Extraordinarias: más de 160 h (al doble, Art. 168 CT)
                    </span>
                  </label>
                </div>
                <button className="primary-button" type="submit" disabled={payrollProcessing}>
                  {payrollProcessing ? "Procesando..." : "Procesar y guardar"}
                </button>
              </form>

              {/* Panel de cálculo en tiempo real */}
              <section className="panel">
                <h3>Vista previa del cálculo</h3>
                {preview ? (
                  <div style={{ display: "flex", flexDirection: "column", gap: 0 }}>
                    {/* Horas */}
                    <div style={{ ...rowStyle, borderBottom: "none", paddingBottom: 2 }}>
                      <span style={{ ...labelStyle, fontWeight: 600, color: "var(--ink)" }}>Horas trabajadas</span>
                    </div>
                    <div style={rowStyle}>
                      <span style={labelStyle}>Horas ordinarias (≤ 160 h)</span>
                      <span style={valueStyle}>{preview.horasOrd} h × ${preview.tarifa.toFixed(4)}</span>
                    </div>
                    {preview.horasExt > 0 && (
                      <div style={rowStyle}>
                        <span style={labelStyle}>Horas extraordinarias (Art. 168 CT)</span>
                        <span style={{ ...valueStyle, color: "var(--accent)" }}>{preview.horasExt} h × ${(preview.tarifa * 2).toFixed(4)}</span>
                      </div>
                    )}

                    {/* Ingresos */}
                    <div style={{ ...rowStyle, marginTop: 8, borderBottom: "none", paddingBottom: 2 }}>
                      <span style={{ ...labelStyle, fontWeight: 600, color: "var(--ink)" }}>Ingresos</span>
                    </div>
                    <div style={rowStyle}>
                      <span style={labelStyle}>Salario ordinario</span>
                      <span style={valueStyle}>${preview.salOrd.toFixed(2)}</span>
                    </div>
                    {preview.salExt > 0 && (
                      <div style={rowStyle}>
                        <span style={labelStyle}>Recargo horas extras (100 %)</span>
                        <span style={{ ...valueStyle, color: "var(--accent)" }}>+${preview.salExt.toFixed(2)}</span>
                      </div>
                    )}
                    <div style={{ ...rowStyle, borderTop: "2px solid var(--line)", marginTop: 4, paddingTop: 6 }}>
                      <span style={{ fontWeight: 700, color: "var(--ink)" }}>Salario bruto</span>
                      <span style={valueStyle}>${preview.bruto.toFixed(2)}</span>
                    </div>

                    {/* Descuentos de ley */}
                    <div style={{ ...rowStyle, marginTop: 10, borderBottom: "none", paddingBottom: 2 }}>
                      <span style={{ ...labelStyle, fontWeight: 600, color: "var(--ink)" }}>Descuentos de ley</span>
                    </div>
                    <div style={rowStyle}>
                      <span style={labelStyle}>ISSS (3 %, tope $1 000)</span>
                      <span style={dangerStyle}>−${preview.isss.toFixed(2)}</span>
                    </div>
                    <div style={rowStyle}>
                      <span style={labelStyle}>AFP (7.25 %)</span>
                      <span style={dangerStyle}>−${preview.afp.toFixed(2)}</span>
                    </div>
                    <div style={rowStyle}>
                      <span style={labelStyle}>ISR / Renta (tabla DGII)</span>
                      <span style={dangerStyle}>−${preview.renta.toFixed(2)}</span>
                    </div>
                    <div style={rowStyle}>
                      <span style={labelStyle}>Total descuentos</span>
                      <span style={dangerStyle}>−${preview.total.toFixed(2)}</span>
                    </div>

                    {/* Neto */}
                    <div style={{ background: "var(--accent-soft)", borderRadius: "var(--radius)", padding: "10px 14px", marginTop: 10, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                      <span style={{ fontWeight: 700, color: "var(--ink)", fontSize: "0.95rem" }}>Salario neto a pagar</span>
                      <span style={successStyle}>${preview.neto.toFixed(2)}</span>
                    </div>

                    <p style={{ fontSize: "0.72rem", color: "var(--light-muted)", marginTop: 8, lineHeight: 1.5 }}>
                      Vista previa estimada · Los valores exactos los confirma el backend al procesar.
                    </p>
                  </div>
                ) : (
                  <p className="empty">Selecciona un empleado para ver el cálculo.</p>
                )}

                <div style={{ marginTop: 16, borderTop: "1px solid var(--line)", paddingTop: 12 }}>
                  <p style={{ fontSize: "0.78rem", color: "var(--muted)", marginBottom: 8, fontWeight: 600 }}>Accesos rápidos</p>
                  <div className="quick-actions">
                    <button className="ghost-button" onClick={() => payrollForm.idEmpleado && loadEmployeePayrolls(payrollForm.idEmpleado)}>
                      Ver historial del empleado
                    </button>
                    <button className="ghost-button" onClick={() => payrollForm.periodo && loadPeriodReport(payrollForm.periodo)}>
                      Ver reporte del período
                    </button>
                  </div>
                </div>
              </section>
            </section>
          );
        })()}

        {activeSection === "reportes" && (
          <section className="grid">
            {canManage && (
              <article className="panel">
                <h3>Consulta rápida</h3>
                <div className="field-grid">
                  <label>
                    Empleado
                    <select value={payrollForm.idEmpleado} onChange={(event) => setPayrollForm({ ...payrollForm, idEmpleado: event.target.value })}>
                      {employees.map((employee) => (
                        <option key={employee.id_empleado} value={employee.id_empleado}>
                          {employee.nombreCompleto}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    Período
                    <input value={payrollForm.periodo} onChange={(event) => setPayrollForm({ ...payrollForm, periodo: event.target.value })} placeholder="05-2026" />
                  </label>
                </div>
                <div className="action-row">
                  <button className="ghost-button" onClick={() => loadEmployeePayrolls(payrollForm.idEmpleado)}>Historial por empleado</button>
                  <button className="ghost-button" onClick={() => loadPeriodReport(payrollForm.periodo)}>Reporte por período</button>
                </div>
              </article>
            )}

            <article className="panel">
              <h3>{isEmployee ? "Mis boletas" : "Historial por empleado"}</h3>
              {renderPayrollRows(isEmployee ? myPayrolls : employeePayrolls)}
            </article>

            {canManage && periodReport && (
              <article className="panel">
                <h3>Consolidado del período {periodReport.periodo}</h3>
                <div className="metrics">
                  <div><strong>{periodReport.cantidadPlanillas}</strong><span>Planillas</span></div>
                  <div><strong>${Number(periodReport.totalSalarioBruto).toFixed(2)}</strong><span>Total bruto</span></div>
                  <div><strong>${Number(periodReport.totalDescuentos).toFixed(2)}</strong><span>Total descuentos</span></div>
                  <div><strong>${Number(periodReport.totalSalarioNeto).toFixed(2)}</strong><span>Total neto</span></div>
                </div>
                {renderPayrollRows(periodReport.planillas || [])}
              </article>
            )}
          </section>
        )}
      </main>
    </div>
  );
}

export default App;
