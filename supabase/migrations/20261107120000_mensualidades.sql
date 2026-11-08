-- =====================================================================
-- Módulo 4 · Mensualidades (US-07 pago simulado, US-08 visualizar)
-- Pantallas: 4.01 a 4.05 y 2.05 Notificaciones
--
-- Crea:
--   * mensualidades       -> una cuota por jugador y periodo
--   * pagos_mensualidad   -> pago simulado / comprobante (0..1 por cuota)
--   * notificaciones      -> avisos dentro de la app
--   * pago_operacion_seq  -> genera los números SIM-xxxxxx
--
-- La lógica (marcar la cuota como pagada y crear los avisos) está en la app,
-- en MensualidadRepository. Se puede volver a ejecutar sin duplicar objetos.
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 1. Cuotas mensuales
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.mensualidades (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  academia_id uuid NOT NULL,
  equipo_id uuid NOT NULL,
  jugador_id character varying NOT NULL,
  periodo date NOT NULL,                          -- siempre el día 1 del mes (2026-11-01)
  monto numeric(10,2) NOT NULL,
  fecha_vencimiento date NOT NULL,
  estado character varying NOT NULL DEFAULT 'PENDIENTE'::character varying,
  creado_por character varying,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT mensualidades_pkey PRIMARY KEY (id),
  CONSTRAINT mensualidades_academia_id_fkey FOREIGN KEY (academia_id) REFERENCES public.academias(id),
  CONSTRAINT mensualidades_equipo_id_fkey FOREIGN KEY (equipo_id) REFERENCES public.equipos(id),
  CONSTRAINT mensualidades_jugador_id_fkey FOREIGN KEY (jugador_id) REFERENCES public.jugadores(id),
  CONSTRAINT mensualidades_creado_por_fkey FOREIGN KEY (creado_por) REFERENCES public.usuarios(id),
  CONSTRAINT mensualidades_jugador_periodo_key UNIQUE (jugador_id, periodo),
  CONSTRAINT mensualidades_estado_check CHECK (estado::text = ANY (ARRAY['PENDIENTE'::character varying, 'PAGADO'::character varying]::text[])),
  CONSTRAINT mensualidades_monto_check CHECK (monto > 0),
  CONSTRAINT mensualidades_periodo_check CHECK (EXTRACT(DAY FROM periodo) = 1)
);

CREATE INDEX IF NOT EXISTS mensualidades_academia_periodo_idx ON public.mensualidades (academia_id, periodo);
CREATE INDEX IF NOT EXISTS mensualidades_equipo_periodo_idx ON public.mensualidades (equipo_id, periodo);

-- ---------------------------------------------------------------------
-- 2. Pago simulado (comprobante)
-- ---------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS public.pago_operacion_seq START 100001;

CREATE TABLE IF NOT EXISTS public.pagos_mensualidad (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  mensualidad_id uuid NOT NULL,
  pagado_por character varying NOT NULL,
  metodo character varying NOT NULL,
  monto numeric(10,2) NOT NULL,
  numero_operacion character varying NOT NULL DEFAULT ('SIM-'::text || nextval('public.pago_operacion_seq'::regclass)),
  fecha_pago timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT pagos_mensualidad_pkey PRIMARY KEY (id),
  CONSTRAINT pagos_mensualidad_mensualidad_id_key UNIQUE (mensualidad_id),       -- una cuota solo se paga una vez
  CONSTRAINT pagos_mensualidad_numero_operacion_key UNIQUE (numero_operacion),
  CONSTRAINT pagos_mensualidad_mensualidad_id_fkey FOREIGN KEY (mensualidad_id) REFERENCES public.mensualidades(id),
  CONSTRAINT pagos_mensualidad_pagado_por_fkey FOREIGN KEY (pagado_por) REFERENCES public.usuarios(id),
  CONSTRAINT pagos_mensualidad_metodo_check CHECK (metodo::text = ANY (ARRAY['YAPE_PLIN'::character varying, 'TARJETA'::character varying, 'TRANSFERENCIA'::character varying]::text[])),
  CONSTRAINT pagos_mensualidad_monto_check CHECK (monto > 0)
);

-- ---------------------------------------------------------------------
-- 3. Notificaciones dentro de la app (pantalla 2.05)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.notificaciones (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  usuario_id character varying NOT NULL,
  tipo character varying NOT NULL DEFAULT 'GENERAL'::character varying,   -- 'MENSUALIDAD', 'GENERAL', ...
  titulo character varying NOT NULL,
  mensaje text NOT NULL,
  referencia_id character varying,                                         -- id del registro relacionado (ej. la mensualidad)
  leida boolean NOT NULL DEFAULT false,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT notificaciones_pkey PRIMARY KEY (id),
  CONSTRAINT notificaciones_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id)
);

CREATE INDEX IF NOT EXISTS notificaciones_usuario_leida_idx ON public.notificaciones (usuario_id, leida);

-- ---------------------------------------------------------------------
-- 4. Acceso: igual que el resto de tablas (la app usa Firebase Auth y la
--    llave pública de Supabase, los permisos por rol se validan en la app)
-- ---------------------------------------------------------------------
ALTER TABLE public.mensualidades DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.pagos_mensualidad DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.notificaciones DISABLE ROW LEVEL SECURITY;

COMMIT;
