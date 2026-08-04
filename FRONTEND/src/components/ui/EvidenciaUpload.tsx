import { useRef, useState } from 'react';
import { useAppStore } from '../../store/useAppStore';
import { evidenciaApi } from '../../api/evidenciaApi';
import { ApiError } from '../../api/client';
import type { EvidenciaFotografica } from '../../types/domain';

// Debe coincidir con app.supabase.evidencia en application.yml (backend).
const TIPOS_PERMITIDOS = ['image/jpeg', 'image/png', 'image/heic'];
const TAMANO_MAX_MB = 10;

// Referencia estable: un `?? []` inline crea un array nuevo en cada render y
// con useSyncExternalStore (Zustand) eso dispara un loop infinito de renders.
const SIN_FOTOS: EvidenciaFotografica[] = [];

interface Props {
  preguntaId: string;
}

/** Adjuntar/tomar foto de evidencia para una pregunta. Sube a POST /api/auditorias/{id}/evidencias. */
export function EvidenciaUpload({ preguntaId }: Props) {
  const auditoriaActivaId = useAppStore((s) => s.auditoriaActivaId);
  const accessToken = useAppStore((s) => s.accessToken);
  const fotos = useAppStore((s) => s.evidenciasPorPregunta[preguntaId] ?? SIN_FOTOS);
  const agregarEvidencia = useAppStore((s) => s.agregarEvidencia);
  const mostrarToast = useAppStore((s) => s.mostrarToast);
  const [subiendo, setSubiendo] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const handleFile = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const archivo = e.target.files?.[0];
    e.target.value = '';
    if (!archivo) return;

    if (!TIPOS_PERMITIDOS.includes(archivo.type)) {
      mostrarToast('Formato no permitido. Usa JPG, PNG o HEIC.', 'warn');
      return;
    }
    if (archivo.size > TAMANO_MAX_MB * 1024 * 1024) {
      mostrarToast(`La foto supera el tamaño máximo de ${TAMANO_MAX_MB} MB.`, 'warn');
      return;
    }
    if (!auditoriaActivaId) {
      mostrarToast('Inicia la auditoría desde "Empresas" para poder adjuntar evidencia.', 'warn');
      return;
    }

    setSubiendo(true);
    try {
      const evidencia = await evidenciaApi.subir(
        auditoriaActivaId,
        archivo,
        { clienteUuid: crypto.randomUUID(), tomadaEn: new Date().toISOString() },
        accessToken,
      );
      agregarEvidencia(preguntaId, evidencia);
      mostrarToast('Foto adjuntada correctamente', 'ok');
    } catch (err) {
      mostrarToast(err instanceof ApiError ? err.message : 'No se pudo subir la foto.', 'warn');
    } finally {
      setSubiendo(false);
    }
  };

  return (
    <div className="q-foto">
      <input
        ref={inputRef}
        type="file"
        accept="image/jpeg,image/png,image/heic"
        capture="environment"
        style={{ display: 'none' }}
        onChange={handleFile}
      />
      {fotos.length > 0 && (
        <div className="q-foto-thumbs">
          {fotos.map((f) => (
            <a key={f.id} href={f.urlArchivo} target="_blank" rel="noreferrer" className="q-foto-thumb" title="Ver foto completa">
              <img src={f.urlArchivo} alt="Evidencia fotográfica" />
            </a>
          ))}
        </div>
      )}
      <button type="button" className="btn btn-sm" onClick={() => inputRef.current?.click()} disabled={subiendo}>
        <i className={`ti ${subiendo ? 'ti-loader-2' : 'ti-camera'}`} />
        {subiendo ? 'Subiendo…' : fotos.length ? 'Adjuntar otra foto' : 'Adjuntar evidencia fotográfica'}
      </button>
    </div>
  );
}
