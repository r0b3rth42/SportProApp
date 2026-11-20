-- =====================================================================
-- Módulo Partidos · Consola en Vivo (Registro de incidencias del partido)
-- Archivo: supabase/migrations/20261108120000_eventos_partido.sql
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 1. Tabla principal de eventos/incidencias del partido
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.eventos_partido (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  partido_id bigint NOT NULL,
  equipo_id uuid NOT NULL,
  jugador_id character varying,                             -- Jugador principal (quien anota, recibe tarjeta, etc.)
  tipo_evento character varying NOT NULL,                  -- GOL, ASISTENCIA, TARJETA_AMARILLA, TARJETA_ROJA, CAMBIO, FALTA, TIRO, CORNER
  minuto integer NOT NULL DEFAULT 0,                        -- Minuto del suceso (ej. 62)
  detalle text,                                            -- Descripción / nota (ej. "Remate cruzado tras asistencia de Méndez.")
  jugador_sale_id character varying,                        -- Jugador sustituido en eventos de tipo 'CAMBIO'
  created_at timestamp with time zone NOT NULL DEFAULT now(),

  CONSTRAINT eventos_partido_pkey PRIMARY KEY (id),
  CONSTRAINT eventos_partido_partido_id_fkey FOREIGN KEY (partido_id)
      REFERENCES public.partido(id) ON DELETE CASCADE,
  CONSTRAINT eventos_partido_equipo_id_fkey FOREIGN KEY (equipo_id)
      REFERENCES public.equipos(id) ON DELETE CASCADE,
  CONSTRAINT eventos_partido_jugador_id_fkey FOREIGN KEY (jugador_id)
      REFERENCES public.jugadores(id) ON DELETE SET NULL,
  CONSTRAINT eventos_partido_jugador_sale_id_fkey FOREIGN KEY (jugador_sale_id)
      REFERENCES public.jugadores(id) ON DELETE SET NULL,
  CONSTRAINT eventos_partido_tipo_evento_check CHECK (tipo_evento::text = ANY (ARRAY[
    'GOL'::character varying,
    'ASISTENCIA'::character varying,
    'TARJETA_AMARILLA'::character varying,
    'TARJETA_ROJA'::character varying,
    'CAMBIO'::character varying,
    'FALTA'::character varying,
    'TIRO'::character varying,
    'CORNER'::character varying
  ]::text[]))
);

-- ---------------------------------------------------------------------
-- 2. Índices de rendimiento
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS eventos_partido_partido_idx ON public.eventos_partido (partido_id, minuto ASC);
CREATE INDEX IF NOT EXISTS eventos_partido_equipo_idx ON public.eventos_partido (equipo_id);

-- ---------------------------------------------------------------------
-- 3. Desactivar RLS (políticas gestionadas a nivel de aplicación)
-- ---------------------------------------------------------------------
ALTER TABLE public.eventos_partido DISABLE ROW LEVEL SECURITY;

COMMIT;
