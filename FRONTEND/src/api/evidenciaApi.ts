// ============================================================
// Evidencia fotográfica de auditorías.
// Refleja EvidenciaController/EvidenciaSubidaRequest del backend:
// POST multipart con partes "archivo" (binario) y "metadata" (JSON).
// ============================================================
import { api } from './client';
import type { EvidenciaFotografica } from '../types/domain';

export interface EvidenciaMetadata {
  clienteUuid: string;
  respuestaId?: string;
  descripcion?: string;
  tomadaEn?: string;
}

export const evidenciaApi = {
  listar: (auditoriaId: string, token?: string | null) =>
    api.get<EvidenciaFotografica[]>(`/api/auditorias/${auditoriaId}/evidencias`, token),

  subir: (auditoriaId: string, archivo: File, metadata: EvidenciaMetadata, token?: string | null) => {
    const formData = new FormData();
    formData.append('archivo', archivo);
    // Debe llevar Content-Type application/json explícito: @RequestPart lo necesita para deserializar el DTO.
    formData.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));
    return api.postMultipart<EvidenciaFotografica>(`/api/auditorias/${auditoriaId}/evidencias`, formData, token);
  },
};
