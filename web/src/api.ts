import type {
  ActivityRequest,
  ActivityResponse,
  ActivitySummary,
  AssetAttributeRequest,
  AssetAttributeResponse,
  AssetRequest,
  AssetResponse,
  BatchRequest,
  BatchResponse,
  BatchSummary,
  CyberEventRequest,
  CyberEventResponse,
  IpAddressRequest,
  IpAddressResponse,
  NarrativeRequest,
  NarrativeResponse,
  PartyRequest,
  PartyResponse,
  PatchActivityHeaderRequest,
  PatchFilingTypeRequest,
  PatchNarrativeRequest,
  PatchSuspiciousActivityRequest,
  SuspiciousActivityRequest,
  SuspiciousActivityResponse,
} from './types'

const BASE = '/api/v1'

class ApiError extends Error {
  status: number
  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

function getAuthToken(): string | null {
  try {
    const stored = localStorage.getItem('sar_auth')
    if (stored) {
      const parsed = JSON.parse(stored)
      return parsed.token || null
    }
  } catch { /* ignore */ }
  return null
}

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    Accept: 'application/json',
    ...((init?.headers as Record<string, string>) || {}),
  }

  const token = getAuthToken()
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const res = await fetch(url, { ...init, headers })
  if (res.status === 401) {
    window.dispatchEvent(new CustomEvent('auth-error', { detail: 401 }))
    throw new ApiError(401, 'Session expired — please log in again.')
  }
  if (!res.ok) {
    const text = await res.text().catch(() => res.statusText)
    let message = text
    try {
      const json = JSON.parse(text)
      message = json.message || text
    } catch { /* use raw text */ }
    throw new ApiError(res.status, message)
  }
  const contentType = res.headers.get('content-type')
  if (contentType?.includes('application/json')) {
    return (await res.json()) as T
  }
  return undefined as unknown as T
}

// ── Batches ───────────────────────────────────────────────────────────────────

export function fetchBatches(): Promise<BatchSummary[]> {
  return request<BatchSummary[]>(`${BASE}/batches`)
}

export function fetchBatch(id: number): Promise<BatchResponse> {
  return request<BatchResponse>(`${BASE}/batches/${id}`)
}

export function createBatch(data: BatchRequest): Promise<BatchResponse> {
  return request<BatchResponse>(`${BASE}/batches`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function deleteBatch(id: number): Promise<void> {
  return request<void>(`${BASE}/batches/${id}`, { method: 'DELETE' })
}

// ── Activities ────────────────────────────────────────────────────────────────

export function fetchActivities(batchId: number): Promise<ActivitySummary[]> {
  return request<ActivitySummary[]>(`${BASE}/batches/${batchId}/activities`)
}

export function fetchActivity(id: number): Promise<ActivityResponse> {
  return request<ActivityResponse>(`${BASE}/activities/${id}`)
}

export function createActivity(batchId: number, data: ActivityRequest): Promise<ActivityResponse> {
  return request<ActivityResponse>(`${BASE}/batches/${batchId}/activities`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function deleteActivity(id: number): Promise<void> {
  return request<void>(`${BASE}/activities/${id}`, { method: 'DELETE' })
}

// ── Activity Patches (Step-by-step editing) ───────────────────────────────────

export function patchActivityHeader(
  id: number,
  data: PatchActivityHeaderRequest,
): Promise<ActivityResponse> {
  return request<ActivityResponse>(`${BASE}/activities/${id}/header`, {
    method: 'PATCH',
    body: JSON.stringify(data),
  })
}

export function patchFilingType(
  id: number,
  data: PatchFilingTypeRequest,
): Promise<ActivityResponse> {
  return request<ActivityResponse>(`${BASE}/activities/${id}/filing-type`, {
    method: 'PATCH',
    body: JSON.stringify(data),
  })
}

export function patchSuspiciousActivity(
  id: number,
  data: PatchSuspiciousActivityRequest,
): Promise<ActivityResponse> {
  return request<ActivityResponse>(`${BASE}/activities/${id}/suspicious-activity`, {
    method: 'PATCH',
    body: JSON.stringify(data),
  })
}

export function patchNarrative(
  activityId: number,
  narrativeId: number,
  data: PatchNarrativeRequest,
): Promise<NarrativeResponse> {
  return request<NarrativeResponse>(
    `${BASE}/activities/${activityId}/narratives/${narrativeId}`,
    { method: 'PATCH', body: JSON.stringify(data) },
  )
}

// ── Parties ───────────────────────────────────────────────────────────────────

export function addParty(activityId: number, data: PartyRequest): Promise<PartyResponse> {
  return request<PartyResponse>(`${BASE}/activities/${activityId}/parties`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function deleteParty(activityId: number, partyId: number): Promise<void> {
  return request<void>(`${BASE}/activities/${activityId}/parties/${partyId}`, {
    method: 'DELETE',
  })
}

// ── Suspicious Activity ───────────────────────────────────────────────────────

export function setSuspiciousActivity(
  activityId: number,
  data: SuspiciousActivityRequest,
): Promise<SuspiciousActivityResponse> {
  return request<SuspiciousActivityResponse>(
    `${BASE}/activities/${activityId}/suspicious-activity`,
    { method: 'PUT', body: JSON.stringify(data) },
  )
}

// ── IP Addresses ──────────────────────────────────────────────────────────────

export function addIpAddress(
  activityId: number,
  data: IpAddressRequest,
): Promise<IpAddressResponse> {
  return request<IpAddressResponse>(`${BASE}/activities/${activityId}/ip-addresses`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function deleteIpAddress(activityId: number, ipId: number): Promise<void> {
  return request<void>(`${BASE}/activities/${activityId}/ip-addresses/${ipId}`, {
    method: 'DELETE',
  })
}

// ── Cyber Events ──────────────────────────────────────────────────────────────

export function addCyberEvent(
  activityId: number,
  data: CyberEventRequest,
): Promise<CyberEventResponse> {
  return request<CyberEventResponse>(`${BASE}/activities/${activityId}/cyber-events`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function deleteCyberEvent(activityId: number, eventId: number): Promise<void> {
  return request<void>(`${BASE}/activities/${activityId}/cyber-events/${eventId}`, {
    method: 'DELETE',
  })
}

// ── Assets ────────────────────────────────────────────────────────────────────

export function addAsset(activityId: number, data: AssetRequest): Promise<AssetResponse> {
  return request<AssetResponse>(`${BASE}/activities/${activityId}/assets`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function deleteAsset(activityId: number, assetId: number): Promise<void> {
  return request<void>(`${BASE}/activities/${activityId}/assets/${assetId}`, {
    method: 'DELETE',
  })
}

export function addAssetAttribute(
  activityId: number,
  data: AssetAttributeRequest,
): Promise<AssetAttributeResponse> {
  return request<AssetAttributeResponse>(`${BASE}/activities/${activityId}/asset-attributes`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function deleteAssetAttribute(activityId: number, attrId: number): Promise<void> {
  return request<void>(`${BASE}/activities/${activityId}/asset-attributes/${attrId}`, {
    method: 'DELETE',
  })
}

// ── Narratives ────────────────────────────────────────────────────────────────

export function addNarrative(
  activityId: number,
  data: NarrativeRequest,
): Promise<NarrativeResponse> {
  return request<NarrativeResponse>(`${BASE}/activities/${activityId}/narratives`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

export function deleteNarrative(activityId: number, narrativeId: number): Promise<void> {
  return request<void>(`${BASE}/activities/${activityId}/narratives/${narrativeId}`, {
    method: 'DELETE',
  })
}

// ── Filing Workflow ───────────────────────────────────────────────────────────

export function submitForReview(batchId: number): Promise<BatchResponse> {
  return request<BatchResponse>(`${BASE}/batches/${batchId}/workflow/review`, { method: 'POST' })
}

export function returnToDraft(batchId: number): Promise<BatchResponse> {
  return request<BatchResponse>(`${BASE}/batches/${batchId}/workflow/draft`, { method: 'POST' })
}

export function submitToFincen(batchId: number): Promise<BatchResponse> {
  return request<BatchResponse>(`${BASE}/batches/${batchId}/workflow/submit`, { method: 'POST' })
}

export function acknowledgeBatch(batchId: number): Promise<BatchResponse> {
  return request<BatchResponse>(`${BASE}/batches/${batchId}/workflow/acknowledge`, {
    method: 'POST',
  })
}

export function rejectBatch(batchId: number): Promise<BatchResponse> {
  return request<BatchResponse>(`${BASE}/batches/${batchId}/workflow/reject`, { method: 'POST' })
}

// ── BSA XML ───────────────────────────────────────────────────────────────────

export async function downloadBsaXml(batchId: number): Promise<Blob> {
  const headers: Record<string, string> = { Accept: 'application/xml' }
  const token = getAuthToken()
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  const res = await fetch(`${BASE}/batches/${batchId}/xml`, { headers })
  if (!res.ok) {
    throw new ApiError(res.status, `XML generation failed: ${res.statusText}`)
  }
  return res.blob()
}